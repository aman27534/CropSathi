sed -i 's/val googleSearchRetrieval: GoogleSearchRetrieval? = null/val googleSearchRetrieval: GoogleSearchRetrieval? = null,\n    val googleMaps: GoogleMaps? = null/g' app/src/main/java/com/example/GeminiService.kt
sed -i '/class GoogleSearchRetrieval/a \
@JsonClass(generateAdapter = true)\
class GoogleMaps\
' app/src/main/java/com/example/GeminiService.kt
