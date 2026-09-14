awk '
/bottomBar = \{/ {
    print $0
    print "            NavigationBar("
    print "                containerColor = MaterialTheme.colorScheme.surface,"
    print "                contentColor = MaterialTheme.colorScheme.onSurfaceVariant"
    print "            ) {"
    in_nav = 1
    next
}
in_nav == 1 && /NavigationBar \{/ {
    # Skip original NavigationBar line
    next
}
in_nav == 1 && /NavigationBarItem\(/ {
    print $0
    print "                        colors = androidx.compose.material3.NavigationBarItemDefaults.colors("
    print "                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,"
    print "                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,"
    print "                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,"
    print "                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,"
    print "                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant"
    print "                        ),"
    next
}
{ print }
' app/src/main/java/com/example/MainActivity.kt > tmp.kt && mv tmp.kt app/src/main/java/com/example/MainActivity.kt
