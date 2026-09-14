sed -i 's/val contents: List<Content>,/val contents: List<Content>,\n    val systemInstruction: Content? = null,/g' app/src/main/java/com/example/GeminiService.kt
sed -i 's/val parts: List<Part>/val parts: List<Part>,\n    val role: String? = null/g' app/src/main/java/com/example/GeminiService.kt
sed -i 's/val googleSearch: GoogleSearch? = null/val googleSearch: GoogleSearch? = null,\n    val googleSearchRetrieval: GoogleSearchRetrieval? = null/g' app/src/main/java/com/example/GeminiService.kt
sed -i '/class GoogleSearch/a \
@JsonClass(generateAdapter = true)\
class GoogleSearchRetrieval\
' app/src/main/java/com/example/GeminiService.kt
