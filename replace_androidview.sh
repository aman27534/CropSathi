awk '
/AndroidView\(/ {
    print "                var previewView by remember { mutableStateOf<PreviewView?>(null) }"
    print "                LaunchedEffect(cameraSelector, previewView) {"
    print "                    val pv = previewView ?: return@LaunchedEffect"
    print "                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)"
    print "                    cameraProviderFuture.addListener({"
    print "                        val cameraProvider = cameraProviderFuture.get()"
    print "                        val preview = androidx.camera.core.Preview.Builder().build().also {"
    print "                            it.setSurfaceProvider(pv.surfaceProvider)"
    print "                        }"
    print "                        val imageAnalysis = ImageAnalysis.Builder()"
    print "                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)"
    print "                            .build()"
    print "                            .also {"
    print "                                it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->"
    print "                                    val bitmap = imageProxy.toBitmap()"
    print "                                    val result = classifier.classify(bitmap)"
    print "                                    pathogenResult = result"
    print "                                    imageProxy.close()"
    print "                                }"
    print "                            }"
    print "                        try {"
    print "                            cameraProvider.unbindAll()"
    print "                            cameraProvider.bindToLifecycle("
    print "                                lifecycleOwner,"
    print "                                cameraSelector,"
    print "                                preview,"
    print "                                imageAnalysis"
    print "                            )"
    print "                        } catch (e: Exception) {"
    print "                            Log.e(\"CameraX\", \"Binding failed\", e)"
    print "                            pathogenResult = \"Camera Initialization Failed\""
    print "                        }"
    print "                    }, ContextCompat.getMainExecutor(context))"
    print "                }"
    print "                AndroidView("
    print "                    factory = { ctx ->"
    print "                        PreviewView(ctx).also { previewView = it }"
    print "                    },"
    print "                    modifier = Modifier.fillMaxSize()"
    print "                )"
    print "                FloatingActionButton("
    print "                    onClick = {"
    print "                        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {"
    print "                            CameraSelector.DEFAULT_FRONT_CAMERA"
    print "                        } else {"
    print "                            CameraSelector.DEFAULT_BACK_CAMERA"
    print "                        }"
    print "                    },"
    print "                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),"
    print "                    containerColor = MaterialTheme.colorScheme.primary"
    print "                ) {"
    print "                    Icon("
    print "                        imageVector = Icons.Default.FlipCameraAndroid,"
    print "                        contentDescription = \"Switch Camera\""
    print "                    )"
    print "                }"
    in_av = 1
    next
}
in_av && /modifier = Modifier.fillMaxSize\(\)/ {
    in_av = 2
    next
}
in_av == 2 && /\)/ {
    in_av = 0
    next
}
in_av == 1 { next }
{ print }
' app/src/main/java/com/example/MainActivity.kt > tmp.kt && mv tmp.kt app/src/main/java/com/example/MainActivity.kt
