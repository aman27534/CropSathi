sed -i 's/androidx.compose.material3.androidx.compose.material3.RadioButton/androidx.compose.material3.RadioButton/g' app/src/main/java/com/example/MainActivity.kt
sed -i 's/androidx.compose.material3.ExposedDropdownMenu(/ExposedDropdownMenu(/g' app/src/main/java/com/example/MainActivity.kt
sed -i 's/androidx.compose.material3.DropdownMenuItem(/DropdownMenuItem(/g' app/src/main/java/com/example/MainActivity.kt
sed -i '/import androidx.compose.material3.ExposedDropdownMenu/d' app/src/main/java/com/example/MainActivity.kt
sed -i '/import androidx.compose.material3.DropdownMenuItem/d' app/src/main/java/com/example/MainActivity.kt
