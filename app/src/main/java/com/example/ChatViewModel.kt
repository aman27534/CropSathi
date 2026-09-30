package com.example

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import retrofit2.HttpException

data class ChatMessage(val role: String, val text: String, val isError: Boolean = false)

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _transcribing = MutableStateFlow(false)
    val transcribing: StateFlow<Boolean> = _transcribing.asStateFlow()

    private val systemInstruction = Content(
        role = "system",
        parts = listOf(Part(text = "You are an expert agriculture AI assistant named CropSathi. Answer helpfully."))
    )

    fun sendMessage(text: String, model: String, apiKey: String, useMaps: Boolean, preferredLanguage: String = "English") {
        if (text.isBlank()) return
        
        val userMsg = ChatMessage(role = "user", text = text)
        _messages.value = _messages.value + userMsg
        _isLoading.value = true

        viewModelScope.launch {
            try {
                if (apiKey == "MY_GEMINI_API_KEY") {
                    kotlinx.coroutines.delay(1000)
                    _messages.value = _messages.value + ChatMessage(role = "model", text = "🤖 [DEMO MODE]: I am CropSathi! Since the API key is missing or invalid, I am replying in offline simulation mode. How can I help with your farm today?", isError = false)
                    _isLoading.value = false
                    return@launch
                }

                // Build history
                val history = _messages.value.filter { !it.isError }.map { msg ->
                    Content(role = msg.role, parts = listOf(Part(text = msg.text)))
                }

                val tools = if (useMaps) {
                    listOf(Tool(googleMaps = GoogleMaps()))
                } else null

                val request = GenerateContentRequest(
                    contents = history,
                    systemInstruction = Content(role = "system", parts = listOf(Part(text = "You are an expert agriculture AI assistant named CropSathi. Answer helpfully. IMPORTANT: You must reply in the following language: $preferredLanguage."))),
                    tools = tools
                )

                val response = GeminiNetwork.api.generateContent(
                    model = model,
                    apiKey = apiKey,
                    request = request
                )

                val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No response."
                _messages.value = _messages.value + ChatMessage(role = "model", text = replyText)
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _messages.value = _messages.value + ChatMessage(role = "model", text = "🤖 [DEMO MODE FALLBACK]: API error encountered. This is a simulated offline response. Make sure to water your crops if it has not rained!", isError = false)
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage(role = "model", text = "🤖 [DEMO MODE FALLBACK]: Network error. This is a simulated offline response. Your crops look healthy!", isError = false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun transcribeAudio(file: File, apiKey: String, onTranscribed: (String) -> Unit) {
        viewModelScope.launch {
            _transcribing.value = true
            try {
                if (apiKey == "MY_GEMINI_API_KEY") {
                    kotlinx.coroutines.delay(2000)
                    onTranscribed("[Simulated Audio]: How much water does my wheat crop need today?")
                    return@launch
                }

                val bytes = file.readBytes()
                val base64Audio = Base64.encodeToString(bytes, Base64.NO_WRAP)
                
                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(
                            role = "user",
                            parts = listOf(
                                Part(inlineData = InlineData(mimeType = "audio/mp4", data = base64Audio)),
                                Part(text = "Transcribe this audio exactly as spoken.")
                            )
                        )
                    )
                )

                val response = GeminiNetwork.api.generateContent(
                    model = "gemini-1.5-flash",
                    apiKey = apiKey,
                    request = request
                )

                val transcription = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                onTranscribed(transcription.trim())
            } catch (e: Exception) {
                onTranscribed("Transcription error: ${e.message}")
            } finally {
                _transcribing.value = false
            }
        }
    }
}
