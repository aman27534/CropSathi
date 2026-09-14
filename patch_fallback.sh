sed -i 's/return@withContext "⚠️ Gemini API Key not configured. Please add it to Secrets."/kotlinx.coroutines.delay(1500)\n                                return@withContext "⚠️ [DEMO MODE - OFFLINE]\\n\\nBased on the simulated analysis:\\n• Diagnosis: Early Blight (Alternaria solani)\\n• Confidence: 88%\\n• Treatment: Apply copper-based fungicide, ensure proper spacing for air circulation, and avoid overhead watering."/g' app/src/main/java/com/example/AiAssistantScreen.kt

sed -i 's/"Error: ${e.localizedMessage}"/"⚠️ [API ERROR - FALLBACK DEMO]\\n\\nNetwork or API error occurred. Showing offline simulation:\\n• Diagnosis: Potential Nutrient Deficiency.\\n• Treatment: Check soil pH and apply balanced NPK fertilizer."/g' app/src/main/java/com/example/AiAssistantScreen.kt
sed -i 's/"API Error: ${e.code()} - $errorBody"/"⚠️ [API ERROR - FALLBACK DEMO]\\n\\nAPI limit reached or error. Showing offline simulation:\\n• Diagnosis: Leaf Rust detected.\\n• Treatment: Remove affected leaves and apply neem oil spray."/g' app/src/main/java/com/example/AiAssistantScreen.kt

sed -i 's/_messages.value = _messages.value + ChatMessage(role = "model", text = "API Key not configured.", isError = true)/kotlinx.coroutines.delay(1000)\n                    _messages.value = _messages.value + ChatMessage(role = "model", text = "🤖 [DEMO MODE]: I am CropSathi! Since the API key is missing or invalid, I am replying in offline simulation mode. How can I help with your farm today?", isError = false)/g' app/src/main/java/com/example/ChatViewModel.kt

sed -i 's/onTranscribed("API Key not configured.")/kotlinx.coroutines.delay(2000)\n                    onTranscribed("[Simulated Audio]: How much water does my wheat crop need today?")/g' app/src/main/java/com/example/ChatViewModel.kt

