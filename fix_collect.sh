sed -i 's/viewModel.uiState.androidx.lifecycle.compose.collectAsStateWithLifecycle()/viewModel.uiState.collectAsStateWithLifecycle()/g' app/src/main/java/com/example/MainActivity.kt
sed -i '/import androidx.compose.runtime.Composable/a import androidx.lifecycle.compose.collectAsStateWithLifecycle' app/src/main/java/com/example/MainActivity.kt
