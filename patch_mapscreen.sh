sed -i '/val coroutineScope = rememberCoroutineScope()/a \
\
    LaunchedEffect(Unit) {\
        val prefs = context.dataStore.data.first()\
        val lat = prefs[MapPreferencesKeys.LATITUDE] ?: indore.latitude\
        val lng = prefs[MapPreferencesKeys.LONGITUDE] ?: indore.longitude\
        val zoom = prefs[MapPreferencesKeys.ZOOM] ?: 15f\
        cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(lat, lng), zoom)\
        isMapInitialized = true\
    }\
\
    LaunchedEffect(cameraPositionState.isMoving) {\
        if (!cameraPositionState.isMoving && isMapInitialized) {\
            val pos = cameraPositionState.position\
            context.dataStore.edit { prefs ->\
                prefs[MapPreferencesKeys.LATITUDE] = pos.target.latitude\
                prefs[MapPreferencesKeys.LONGITUDE] = pos.target.longitude\
                prefs[MapPreferencesKeys.ZOOM] = pos.zoom\
            }\
        }\
    }\
' app/src/main/java/com/example/MainActivity.kt
