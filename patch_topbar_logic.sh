sed -i 's/val navController = rememberNavController()/val navController = rememberNavController()\n    val navBackStackEntry by navController.currentBackStackEntryAsState()\n    val currentDestination = navBackStackEntry?.destination\n    val isProfile = currentDestination?.route == "profile"/g' app/src/main/java/com/example/MainActivity.kt
sed -i 's/val navBackStackEntry by navController.currentBackStackEntryAsState()//g' app/src/main/java/com/example/MainActivity.kt
sed -i 's/val currentDestination = navBackStackEntry?.destination//g' app/src/main/java/com/example/MainActivity.kt
sed -i 's/val isProfile = currentDestination?.route == "profile"//g' app/src/main/java/com/example/MainActivity.kt
sed -i '/fun CropSaathiApp() {/a \    val navController = rememberNavController()\n    val navBackStackEntry by androidx.navigation.compose.currentBackStackEntryAsState(navController)\n    val currentDestination = navBackStackEntry?.destination\n    val isProfile = currentDestination?.route == "profile"' app/src/main/java/com/example/MainActivity.kt
sed -i '/val navController = rememberNavController()/d' app/src/main/java/com/example/MainActivity.kt
