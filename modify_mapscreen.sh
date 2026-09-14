sed -i '/val indore = LatLng(22.7196, 75.8577)/i \
    val context = LocalContext.current\n\
    var isMapInitialized by remember { mutableStateOf(false) }\n' app/src/main/java/com/example/MainActivity.kt
