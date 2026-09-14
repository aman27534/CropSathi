sed -i 's/object AI : Screen("ai_assistant", "Assistant", Icons.Filled.AutoAwesome)/object AI : Screen("ai_assistant", "Diagnosis", Icons.Filled.BugReport)/g' app/src/main/java/com/example/MainActivity.kt
sed -i 's/object Irrigate : Screen("irrigate", "Irrigate", Icons.Filled.WaterDrop)/object Irrigate : Screen("irrigate", "Irrigation", Icons.Filled.WaterDrop)\n    object Calculator : Screen("calculator", "Calculator", Icons.Filled.Calculate)/g' app/src/main/java/com/example/MainActivity.kt
sed -i '/val items = listOf(/,/Screen.Seeds/c\
val items = listOf(\
    Screen.Dashboard,\
    Screen.AI,\
    Screen.Calculator,\
    Screen.Irrigate,\
    Screen.Seeds\
)' app/src/main/java/com/example/MainActivity.kt
sed -i 's/composable(Screen.Seeds.route) { SeedCalculatorScreen() }/composable(Screen.Calculator.route) { val vm: IrrigationViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as android.app.Application)); IrrigateScreen(viewModel = vm) }\n            composable(Screen.Seeds.route) { SeedCalculatorScreen() }/g' app/src/main/java/com/example/MainActivity.kt
sed -i 's/composable("irrigation_calculator") { val vm: IrrigationViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as android.app.Application)); IrrigateScreen(viewModel = vm) }//g' app/src/main/java/com/example/MainActivity.kt
sed -i 's/navController.navigate("irrigation_calculator")/navController.navigate(Screen.Calculator.route)/g' app/src/main/java/com/example/MainActivity.kt
