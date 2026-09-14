sed -i '/class MainActivity/i \
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "map_prefs")\n\
object MapPreferencesKeys {\n\
    val LATITUDE = doublePreferencesKey("latitude")\n\
    val LONGITUDE = doublePreferencesKey("longitude")\n\
    val ZOOM = floatPreferencesKey("zoom")\n\
}\n\
' app/src/main/java/com/example/MainActivity.kt
