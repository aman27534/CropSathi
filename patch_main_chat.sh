sed -i 's/object AI : Screen("ai_assistant", "Diagnosis", Icons.Filled.BugReport)/object AI : Screen("ai_assistant", "Diagnosis", Icons.Filled.BugReport)\n    object Chat : Screen("chat", "Chatbot", Icons.Filled.Chat)/g' app/src/main/java/com/example/MainActivity.kt
sed -i '/val items = listOf(/,/Screen.Seeds/c\
val items = listOf(\
    Screen.Dashboard,\
    Screen.Chat,\
    Screen.AI,\
    Screen.Calculator,\
    Screen.Irrigate,\
    Screen.Seeds\
)' app/src/main/java/com/example/MainActivity.kt
sed -i 's/composable(Screen.AI.route) { AiAssistantScreen() }/composable(Screen.AI.route) { AiAssistantScreen() }\n            composable(Screen.Chat.route) { ChatScreen() }/g' app/src/main/java/com/example/MainActivity.kt
