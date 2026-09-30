"""
CropSaathi -- Standalone TFLite Model Test
==========================================
Run this BEFORE installing the model in the Android app to verify:
  1. Model loads correctly
  2. Output shape matches expected (1, NUM_CLASSES)
  3. Predictions on sample images are sensible
  4. Confidence threshold logic triggers properly on ambiguous/blank images

Usage:
  python test_model.py [--image path/to/leaf.jpg]

If no image provided, tests with a generated solid-color image
(should trigger the confidence threshold -- 'Unclear' response).
"""

import argparse
import os
import sys
import numpy as np

TFLITE_PATH = "saved_models/disease_classifier_quantized.tflite"
LABELS_PATH = "saved_models/disease_labels.txt"
IMG_SIZE    = (224, 224)
CONFIDENCE_THRESHOLD = 0.60


def load_labels():
    if not os.path.exists(LABELS_PATH):
        print(f"ERROR: Labels not found at {LABELS_PATH}")
        sys.exit(1)
    with open(LABELS_PATH) as f:
        return [l.strip() for l in f.readlines() if l.strip()]


def load_tflite():
    if not os.path.exists(TFLITE_PATH):
        print(f"ERROR: TFLite model not found at {TFLITE_PATH}")
        print("Run train_disease_classifier.py then convert_to_tflite.py first.")
        sys.exit(1)

    try:
        import tensorflow as tf
        interpreter = tf.lite.Interpreter(model_path=TFLITE_PATH)
        interpreter.allocate_tensors()
        return interpreter
    except Exception as e:
        print(f"ERROR loading TFLite model: {e}")
        sys.exit(1)


def preprocess_image(image_path=None):
    """
    Load and preprocess an image exactly like the Android app does:
    1. Resize to 224x224
    2. Convert to float32
    3. Normalize to [0, 1]
    4. Add batch dimension
    """
    try:
        from PIL import Image
        import numpy as np

        if image_path and os.path.exists(image_path):
            img = Image.open(image_path).convert('RGB')
            img = img.resize(IMG_SIZE, Image.BILINEAR)
            arr = np.array(img, dtype=np.float32) / 255.0
        else:
            print("No image provided -- generating solid green image (should be UNCLEAR).")
            arr = np.zeros((*IMG_SIZE, 3), dtype=np.float32)
            arr[:, :, 1] = 0.5  # green channel

        return np.expand_dims(arr, axis=0)  # (1, 224, 224, 3)

    except ImportError:
        print("PIL not found. Install with: pip install Pillow")
        print("Generating random tensor instead...")
        return np.random.rand(1, *IMG_SIZE, 3).astype(np.float32)


def run_inference(interpreter, input_array):
    input_details  = interpreter.get_input_details()
    output_details = interpreter.get_output_details()

    print(f"\nInput  tensor shape : {input_details[0]['shape']}  dtype={input_details[0]['dtype']}")
    print(f"Output tensor shape : {output_details[0]['shape']}  dtype={output_details[0]['dtype']}")

    interpreter.set_tensor(input_details[0]['index'], input_array)
    interpreter.invoke()
    output = interpreter.get_tensor(output_details[0]['index'])
    return output[0]  # shape: (NUM_CLASSES,)


def print_results(scores, labels):
    print("\n" + "="*55)
    print("  Inference Results")
    print("="*55)

    sorted_idx = np.argsort(scores)[::-1]
    top_idx    = sorted_idx[0]
    top_conf   = scores[top_idx]
    top_label  = labels[top_idx] if top_idx < len(labels) else f"class_{top_idx}"

    print(f"\n  Top prediction : {top_label}")
    print(f"  Confidence     : {top_conf:.2%}")

    if top_conf < CONFIDENCE_THRESHOLD:
        print(f"\n  [App would show]: UNCLEAR -- Retake Photo")
        print(f"  (confidence {top_conf:.2%} < threshold {CONFIDENCE_THRESHOLD:.2%})")
    else:
        print(f"\n  [App would show]: {top_label.replace('_', ' ')}")

    print("\n  Full ranking:")
    for i, idx in enumerate(sorted_idx[:5]):
        label = labels[idx] if idx < len(labels) else f"class_{idx}"
        bar   = '#' * int(scores[idx] * 40)
        print(f"    {i+1}. {label:35s}  {scores[idx]:.4f}  {bar}")


def test_model(image_path=None):
    print("="*55)
    print("  CropSaathi TFLite Model Verification")
    print("="*55)

    labels      = load_labels()
    interpreter = load_tflite()
    input_array = preprocess_image(image_path)

    print(f"\nModel    : {TFLITE_PATH}")
    print(f"Classes  : {len(labels)}")
    print(f"Labels   : {labels}")
    print(f"Input    : {input_array.shape}  min={input_array.min():.3f}  max={input_array.max():.3f}")

    scores = run_inference(interpreter, input_array)
    assert len(scores) == len(labels), f"Mismatch: model output {len(scores)} != labels {len(labels)}"
    assert abs(scores.sum() - 1.0) < 0.01, f"Softmax sanity fail: sum={scores.sum()}"

    print_results(scores, labels)

    print("\n  [OK] Model loaded and ran inference successfully.")
    print("  [OK] Output shape correct.")
    print("  [OK] Softmax outputs sum to ~1.0")
    print("\n  If predictions look wrong, check that label order in disease_labels.txt")
    print("  matches the training class_indices order exactly.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Test CropSaathi TFLite model")
    parser.add_argument("--image", type=str, default=None, help="Path to a leaf image")
    args = parser.parse_args()
    test_model(args.image)
