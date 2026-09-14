sed -i '/@JsonClass(generateAdapter = true)/, /data class Part/c\
@JsonClass(generateAdapter = true)\
data class InlineData(\
    val mimeType: String,\
    val data: String\
)\
\
@JsonClass(generateAdapter = true)\
data class Part(\
    val text: String? = null,\
    val inlineData: InlineData? = null\
)' app/src/main/java/com/example/GeminiService.kt
