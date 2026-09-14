sed -i 's/modifier = Modifier.weight(1f),/modifier = Modifier.weight(1f).androidx.compose.foundation.clickable { onNavigateToHistory() },/g' app/src/main/java/com/example/IrrigationDashboardScreen.kt
sed -i 's/onClick = onNavigateToHistory//g' app/src/main/java/com/example/IrrigationDashboardScreen.kt
