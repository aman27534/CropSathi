sed -i 's/letterSpacing = 1.dp/letterSpacing = 1.sp/g' app/src/main/java/com/example/AiAssistantScreen.kt
sed -i 's/import androidx.compose.ui.unit.dp/import androidx.compose.ui.unit.dp\nimport androidx.compose.ui.unit.sp/g' app/src/main/java/com/example/AiAssistantScreen.kt
sed -i 's/val response = GeminiNetwork.api.generateContent(apiKey, request)/val response = GeminiNetwork.api.generateContent(model = "gemini-3.1-pro-preview", apiKey = apiKey, request = request)/g' app/src/main/java/com/example/IrrigationDatabase.kt
