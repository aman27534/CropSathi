awk '
/import androidx.compose.ui.viewinterop.AndroidView/ {
    print $0
    print "import androidx.compose.ui.input.pointer.pointerInput"
    print "import androidx.compose.foundation.gestures.detectTransformGestures"
    print "import androidx.camera.core.Camera"
    next
}
/var previewView by remember \{ mutableStateOf<PreviewView\?>\(null\) \}/ {
    print "                var camera by remember { mutableStateOf<Camera?>(null) }"
    print $0
    next
}
/cameraProvider\.bindToLifecycle\(/ {
    print "                            camera = cameraProvider.bindToLifecycle("
    next
}
/AndroidView\(/ {
    in_android_view = 1
    print $0
    next
}
/modifier = Modifier\.fillMaxSize\(\)/ {
    if (in_android_view) {
        print "                    modifier = Modifier"
        print "                        .fillMaxSize()"
        print "                        .pointerInput(camera) {"
        print "                            detectTransformGestures { _, _, zoom, _ ->"
        print "                                camera?.let { cam ->"
        print "                                    val currentZoom = cam.cameraInfo.zoomState.value?.zoomRatio ?: 1f"
        print "                                    cam.cameraControl.setZoomRatio(currentZoom * zoom)"
        print "                                }"
        print "                            }"
        print "                        }"
        next
    }
    print $0
    next
}
/FloatingActionButton\(/ {
    in_android_view = 0
    print $0
    next
}
{ print }
' app/src/main/java/com/example/MainActivity.kt > tmp.kt && mv tmp.kt app/src/main/java/com/example/MainActivity.kt
