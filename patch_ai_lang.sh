sed -i 's/val context = LocalContext.current/val context = LocalContext.current\n    val prefLanguage = LanguageManager.getLanguage(context)/g' app/src/main/java/com/example/AiAssistantScreen.kt
sed -i 's/Keep it practical and concise."/Keep it practical and concise. IMPORTANT: You must reply in the following language: $prefLanguage."/g' app/src/main/java/com/example/AiAssistantScreen.kt
