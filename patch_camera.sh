sed -i '/val classifier = remember { LeafDiseaseClassifier(context) }/i \
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }' app/src/main/java/com/example/MainActivity.kt
