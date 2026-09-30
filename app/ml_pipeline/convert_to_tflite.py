import tensorflow as tf
import os
import numpy as np
import json

SAVED_MODEL_DIR = 'saved_models/crop_disease_model'
LABELS_PATH     = 'saved_models/disease_labels.txt'
TFLITE_OUT      = 'saved_models/disease_classifier_quantized.tflite'
ANDROID_ASSETS  = '../src/main/assets'
IMG_SIZE        = (224, 224)
DATASET_DIR     = 'dataset'

def convert_model():
    print('='*60)
    print('  CropSaathi -- TFLite Conversion Pipeline')
    print('='*60)

    if not os.path.exists(SAVED_MODEL_DIR):
        print(f'ERROR: {SAVED_MODEL_DIR} not found. Run train_disease_classifier.py first.')
        return

    print('Loading SavedModel...')
    converter = tf.lite.TFLiteConverter.from_saved_model(SAVED_MODEL_DIR)

    # FP16 quantization: halves model size with minimal accuracy loss.
    # For INT8 (even smaller, faster), representative_dataset needed -- see below.
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    converter.target_spec.supported_types = [tf.float16]

    # Optional: full INT8 quantization using representative dataset
    # Uncomment this block if you want INT8 (faster inference on edge devices):
    #
    # if os.path.exists(DATASET_DIR):
    #     from tensorflow.keras.preprocessing.image import ImageDataGenerator
    #     val_gen = ImageDataGenerator(rescale=1./255)
    #     val_flow = val_gen.flow_from_directory(DATASET_DIR, target_size=IMG_SIZE,
    #                                             batch_size=1, shuffle=True)
    #     def representative_dataset():
    #         for i, (img, _) in enumerate(val_flow):
    #             if i >= 200: break
    #             yield [img.astype(np.float32)]
    #
    #     converter.representative_dataset = representative_dataset
    #     converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]
    #     converter.inference_input_type = tf.float32
    #     converter.inference_output_type = tf.float32

    print('Converting to TFLite (FP16 quantization)...')
    tflite_model = converter.convert()

    os.makedirs('saved_models', exist_ok=True)
    with open(TFLITE_OUT, 'wb') as f:
        f.write(tflite_model)

    size_mb = os.path.getsize(TFLITE_OUT) / (1024 * 1024)
    print(f'TFLite model saved -> {TFLITE_OUT}')
    print(f'Model size: {size_mb:.2f} MB  (target < 6 MB for on-device inference)')

    # Copy to Android assets directory
    if os.path.exists(ANDROID_ASSETS) or True:
        os.makedirs(ANDROID_ASSETS, exist_ok=True)
        import shutil
        tflite_dst = os.path.join(ANDROID_ASSETS, 'disease_classifier_quantized.tflite')
        labels_dst = os.path.join(ANDROID_ASSETS, 'disease_labels.txt')
        shutil.copy2(TFLITE_OUT, tflite_dst)
        print(f'Copied TFLite model -> {tflite_dst}')
        if os.path.exists(LABELS_PATH):
            shutil.copy2(LABELS_PATH, labels_dst)
            print(f'Copied labels      -> {labels_dst}')

    print('')
    print('Next step: python test_model.py')
    print('Then copy saved_models/disease_classifier_quantized.tflite')
    print('     into app/src/main/assets/')

if __name__ == '__main__':
    convert_model()
