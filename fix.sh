sed -i 's/recommendedCrop/crop/g' app/src/main/java/com/example/MainActivity.kt

awk '
NR==381, NR==384 {
    if (NR==381) { print "@OptIn(ExperimentalMaterial3Api::class)\n@Composable" }
    next
}
NR==548 { next }
{ print }
' app/src/main/java/com/example/MainActivity.kt > tmp.kt && mv tmp.kt app/src/main/java/com/example/MainActivity.kt
