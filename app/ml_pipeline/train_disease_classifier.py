"""
CropSaathi -- Disease Classifier Training Pipeline (v2)
=======================================================
Architecture : MobileNetV2 (ImageNet pretrained) -> Custom Head
Dataset      : PlantVillage subset -- 3 crops (Tomato, Potato, Corn), 10 classes
Target       : >=85% validation accuracy
Output       : saved_models/crop_disease_model/    (SavedModel format)
               saved_models/disease_labels.txt      (class index -> name)

QUICK START
-----------
1.  pip install tensorflow scikit-learn matplotlib
2.  Download PlantVillage: https://www.kaggle.com/datasets/emmarex/plantdisease
3.  Rename/organize folders under dataset/ to exactly:
      Corn_Common_Rust, Corn_Northern_Leaf_Blight, Corn_Healthy
      Potato_Early_Blight, Potato_Late_Blight, Potato_Healthy
      Tomato_Early_Blight, Tomato_Late_Blight, Tomato_Leaf_Mold, Tomato_Healthy
4.  python train_disease_classifier.py
5.  python convert_to_tflite.py
6.  python test_model.py

Training strategy (two-phase):
  Phase 1 -- Freeze MobileNetV2 base, train only the classification head.
  Phase 2 -- Unfreeze top 30 layers, fine-tune with 10x lower LR.
"""

import os
import json
import numpy as np
import tensorflow as tf
from tensorflow.keras.applications import MobileNetV2
from tensorflow.keras.layers import Dense, GlobalAveragePooling2D, Dropout, BatchNormalization
from tensorflow.keras.models import Model
from tensorflow.keras.callbacks import EarlyStopping, ReduceLROnPlateau, ModelCheckpoint, TensorBoard
from tensorflow.keras.preprocessing.image import ImageDataGenerator

try:
    from sklearn.utils.class_weight import compute_class_weight
    SKLEARN_AVAILABLE = True
except ImportError:
    print("WARNING: scikit-learn not found. Class weights will be uniform.")
    SKLEARN_AVAILABLE = False

# ---- Configuration ----------------------------------------------------------
IMG_SIZE             = (224, 224)
BATCH_SIZE           = 32
EPOCHS_FROZEN        = 15     # Phase 1: train head only
EPOCHS_FINETUNE      = 10     # Phase 2: fine-tune top layers
LR_PHASE1            = 1e-3
LR_PHASE2            = 1e-5   # Must be very low to avoid catastrophic forgetting
DROPOUT_RATE1        = 0.40
DROPOUT_RATE2        = 0.30
CONFIDENCE_THRESHOLD = 0.60   # Below this -> "Unclear -- Retake Photo" in app

CLASS_NAMES = [
    "Corn_Common_Rust",
    "Corn_Northern_Leaf_Blight",
    "Corn_Healthy",
    "Potato_Early_Blight",
    "Potato_Late_Blight",
    "Potato_Healthy",
    "Tomato_Early_Blight",
    "Tomato_Late_Blight",
    "Tomato_Leaf_Mold",
    "Tomato_Healthy",
]
NUM_CLASSES     = len(CLASS_NAMES)
DATASET_DIR     = "dataset"
SAVE_DIR        = "saved_models/crop_disease_model"
LABELS_PATH     = "saved_models/disease_labels.txt"
BEST_MODEL_PATH = "saved_models/best_checkpoint.keras"
LOG_DIR         = "saved_models/logs"


# ---- Model ------------------------------------------------------------------
def build_model():
    """MobileNetV2 + custom head. Returns (full_model, base_model)."""
    base = MobileNetV2(weights="imagenet", include_top=False, input_shape=(*IMG_SIZE, 3))
    base.trainable = False  # Phase 1: entire base frozen

    x = base.output
    x = GlobalAveragePooling2D(name="gap")(x)
    x = BatchNormalization(name="bn1")(x)
    x = Dense(256, activation="relu", name="dense1")(x)
    x = Dropout(DROPOUT_RATE1, name="drop1")(x)
    x = Dense(128, activation="relu", name="dense2")(x)
    x = Dropout(DROPOUT_RATE2, name="drop2")(x)
    output = Dense(NUM_CLASSES, activation="softmax", name="predictions")(x)

    model = Model(inputs=base.input, outputs=output)
    model.compile(
        optimizer=tf.keras.optimizers.Adam(learning_rate=LR_PHASE1),
        loss="categorical_crossentropy",
        metrics=["accuracy", tf.keras.metrics.TopKCategoricalAccuracy(k=3, name="top3_acc")]
    )
    return model, base


# ---- Data -------------------------------------------------------------------
def get_data_generators():
    """
    Strong augmentation pipeline for real-world robustness.
    - rotation/flip      : leaves photographed at any angle in the field
    - brightness_range   : morning shade vs midday sun, different phones
    - channel_shift      : different camera sensors
    - zoom/shear         : variable shooting distance and perspective
    """
    train_aug = ImageDataGenerator(
        rescale=1.0 / 255,
        rotation_range=40,
        width_shift_range=0.15,
        height_shift_range=0.15,
        shear_range=0.15,
        zoom_range=0.25,
        horizontal_flip=True,
        brightness_range=[0.65, 1.35],
        channel_shift_range=25.0,
        fill_mode="nearest",
        validation_split=0.20
    )
    val_aug = ImageDataGenerator(rescale=1.0 / 255, validation_split=0.20)

    common_kwargs = dict(
        directory=DATASET_DIR,
        target_size=IMG_SIZE,
        batch_size=BATCH_SIZE,
        class_mode="categorical",
        seed=42,
    )

    train_flow = train_aug.flow_from_directory(subset="training", shuffle=True, **common_kwargs)
    val_flow   = val_aug.flow_from_directory(subset="validation", shuffle=False, **common_kwargs)
    return train_flow, val_flow


# ---- Class Weights ----------------------------------------------------------
def compute_class_weights(train_flow):
    if not SKLEARN_AVAILABLE:
        return {i: 1.0 for i in range(len(train_flow.class_indices))}

    y       = train_flow.classes
    classes = np.arange(len(train_flow.class_indices))
    weights = compute_class_weight(class_weight="balanced", classes=classes, y=y)
    cw      = dict(enumerate(weights.astype(float)))
    idx_to_name = {v: k for k, v in train_flow.class_indices.items()}
    print("\nClass weights (balanced to counter PlantVillage imbalance):")
    for idx, w in cw.items():
        print(f"  [{idx:2d}] {idx_to_name.get(idx, '?'):35s}  weight={w:.3f}")
    return cw


# ---- Callbacks --------------------------------------------------------------
def make_callbacks(phase):
    return [
        EarlyStopping(monitor="val_accuracy", patience=5, restore_best_weights=True, verbose=1, min_delta=0.002),
        ReduceLROnPlateau(monitor="val_loss", factor=0.4, patience=3, min_lr=1e-7, verbose=1),
        ModelCheckpoint(BEST_MODEL_PATH, monitor="val_accuracy", save_best_only=True, verbose=1),
        TensorBoard(log_dir=os.path.join(LOG_DIR, f"phase{phase}"), histogram_freq=0)
    ]


# ---- Phase 2: Fine-Tuning ---------------------------------------------------
def fine_tune_model(model, base, train_flow, val_flow, class_weights):
    """
    Unfreeze top 30 MobileNetV2 layers, fine-tune with very low LR.
    Early layers (edges/textures from ImageNet) stay frozen.
    """
    print("\n" + "="*60)
    print("  Phase 2: Fine-tuning (top 30 MobileNetV2 layers unfrozen)")
    print("="*60)

    base.trainable = True
    for layer in base.layers[:-30]:
        layer.trainable = False

    frozen   = sum(1 for l in base.layers if not l.trainable)
    unfrozen = sum(1 for l in base.layers if l.trainable)
    print(f"  Base: {frozen} layers frozen, {unfrozen} layers trainable")

    model.compile(
        optimizer=tf.keras.optimizers.Adam(learning_rate=LR_PHASE2, clipnorm=1.0),
        loss="categorical_crossentropy",
        metrics=["accuracy", tf.keras.metrics.TopKCategoricalAccuracy(k=3, name="top3_acc")]
    )

    model.fit(
        train_flow, validation_data=val_flow,
        epochs=EPOCHS_FINETUNE, class_weight=class_weights,
        callbacks=make_callbacks(phase=2)
    )
    return model


# ---- Per-Class Evaluation ---------------------------------------------------
def evaluate_per_class(model, val_flow, labels):
    """
    Per-class accuracy report. Any class below 80% accuracy is flagged.
    These are the classes where you need more data or targeted augmentation.
    """
    print("\n" + "="*60)
    print("  Per-Class Validation Report")
    print("="*60)

    val_flow.reset()
    all_probs, all_labels = [], []
    for i, (x_batch, y_batch) in enumerate(val_flow):
        all_probs.append(model.predict(x_batch, verbose=0))
        all_labels.append(np.argmax(y_batch, axis=1))
        if i + 1 >= len(val_flow):
            break

    all_probs  = np.concatenate(all_probs, axis=0)
    all_labels = np.concatenate(all_labels, axis=0)
    pred_classes = np.argmax(all_probs, axis=1)

    for i, label in enumerate(labels):
        mask = all_labels == i
        if mask.sum() == 0:
            continue
        acc          = (pred_classes[mask] == i).mean()
        avg_conf     = all_probs[mask, i].mean()
        below_thresh = (all_probs[mask, i] < CONFIDENCE_THRESHOLD).mean()
        flag = "WARNING WEAK" if acc < 0.80 else "OK"
        print(f"  [{flag}] [{i:2d}] {label:35s} acc={acc:.1%}  avg_conf={avg_conf:.1%}  below_60%={below_thresh:.1%}")

    overall = (pred_classes == all_labels).mean()
    print(f"\n  Overall validation accuracy: {overall:.2%}")
    if overall < 0.85:
        print("  BELOW 85% TARGET. Suggestions:")
        print("    - Ensure weak classes have >=500 images each")
        print("    - Increase EPOCHS_FINETUNE to 15-20")
        print("    - Check for mislabelled images in weak class folders")


# ---- Save Metadata ----------------------------------------------------------
def save_labels(train_flow):
    idx_to_class = {v: k for k, v in train_flow.class_indices.items()}
    labels = [idx_to_class[i] for i in range(len(idx_to_class))]
    os.makedirs("saved_models", exist_ok=True)
    with open(LABELS_PATH, "w") as f:
        f.write("\n".join(labels))
    print(f"\nLabels saved -> {LABELS_PATH}")
    return labels


def save_metadata(train_flow, val_loss, val_acc, labels):
    meta = {
        "model_architecture": "MobileNetV2",
        "input_size": list(IMG_SIZE),
        "num_classes": NUM_CLASSES,
        "class_names": labels,
        "confidence_threshold": CONFIDENCE_THRESHOLD,
        "training_samples": train_flow.samples,
        "val_accuracy": float(val_acc),
        "val_loss": float(val_loss),
        "training_strategy": "two-phase (frozen head + fine-tune top-30 layers)",
    }
    with open("saved_models/training_metadata.json", "w") as f:
        json.dump(meta, f, indent=2)
    print("Metadata saved -> saved_models/training_metadata.json")


# ---- Main -------------------------------------------------------------------
def train():
    print("="*60)
    print("  CropSaathi Disease Classifier -- Training Pipeline")
    print(f"  MobileNetV2 | {NUM_CLASSES} classes | IMG_SIZE={IMG_SIZE}")
    print("="*60)

    if not os.path.exists(DATASET_DIR):
        print(f"\nERROR: Dataset not found at '{DATASET_DIR}/'")
        print("Download PlantVillage from: https://www.kaggle.com/datasets/emmarex/plantdisease")
        print("Required subdirectory names:")
        for c in CLASS_NAMES:
            print(f"  dataset/{c}/")
        return

    os.makedirs(SAVE_DIR, exist_ok=True)
    os.makedirs(LOG_DIR, exist_ok=True)

    gpus = tf.config.list_physical_devices("GPU")
    print(f"\nGPU(s) detected: {gpus if gpus else 'None (CPU -- consider Google Colab for faster training)'}")

    # Data
    train_flow, val_flow = get_data_generators()
    print(f"\nTraining samples  : {train_flow.samples}")
    print(f"Validation samples: {val_flow.samples}")
    print(f"Class mapping     : {train_flow.class_indices}")

    labels       = save_labels(train_flow)
    class_weights = compute_class_weights(train_flow)

    # Phase 1
    print("\n" + "="*60)
    print("  Phase 1: Training classification head (base frozen)")
    print("="*60)
    model, base = build_model()
    model.fit(
        train_flow, validation_data=val_flow,
        epochs=EPOCHS_FROZEN, class_weight=class_weights,
        callbacks=make_callbacks(phase=1)
    )

    # Phase 2
    model = fine_tune_model(model, base, train_flow, val_flow, class_weights)

    # Evaluate
    val_loss, val_acc, _ = model.evaluate(val_flow, verbose=0)
    print(f"\nFinal Validation Accuracy : {val_acc:.2%}  (target >=85%)")
    print(f"Final Validation Loss     : {val_loss:.4f}")

    evaluate_per_class(model, val_flow, labels)
    model.save(SAVE_DIR)
    save_metadata(train_flow, val_loss, val_acc, labels)

    print("\n" + "="*60)
    print("  Training complete!")
    print(f"  Model -> {SAVE_DIR}/")
    print(f"  Confidence threshold -> {CONFIDENCE_THRESHOLD:.0%} (below = 'Retake Photo')")
    print("  Next: python convert_to_tflite.py")
    print("="*60)


if __name__ == "__main__":
    train()
