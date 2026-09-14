# Fix CropSaathiApp Scaffold and NavigationBar
awk '
/fun CropSaathiApp\(\) \{/ {
    in_app = 1
    print "@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)"
    print $0
    next
}
in_app == 1 && /Scaffold\(/ {
    print "    Scaffold("
    print "        modifier = Modifier.fillMaxSize(),"
    print "        topBar = {"
    print "            androidx.compose.material3.TopAppBar("
    print "                title = { "
    print "                    Row(verticalAlignment = Alignment.CenterVertically) {"
    print "                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary) {"
    print "                            Icon(Icons.Filled.Agriculture, contentDescription = null, tint = Color.White, modifier = Modifier.padding(6.dp).size(20.dp))"
    print "                        }"
    print "                        Spacer(modifier = Modifier.width(8.dp))"
    print "                        Text(\"CropSathi\", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)"
    print "                    }"
    print "                },"
    print "                actions = {"
    print "                    IconButton(onClick = { /* TODO */ }) {"
    print "                        Icon(Icons.Filled.Notifications, contentDescription = \"Notifications\")"
    print "                    }"
    print "                    IconButton(onClick = { /* TODO */ }) {"
    print "                        Icon(Icons.Filled.Person, contentDescription = \"Profile\")"
    print "                    }"
    print "                },"
    print "                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors("
    print "                    containerColor = MaterialTheme.colorScheme.surface,"
    print "                    titleContentColor = MaterialTheme.colorScheme.onSurface"
    print "                )"
    print "            )"
    print "        },"
    next
}
/modifier = Modifier\.fillMaxSize\(\),/ && in_app == 1 {
    # Skip original modifier line because we injected it
    next
}
{ print }
' app/src/main/java/com/example/MainActivity.kt > tmp.kt && mv tmp.kt app/src/main/java/com/example/MainActivity.kt
