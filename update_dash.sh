awk '
/import androidx.compose.material.icons.filled.MyLocation/ {
    print $0
    print "import androidx.compose.material.icons.filled.*"
    print "import androidx.compose.foundation.verticalScroll"
    print "import androidx.compose.foundation.rememberScrollState"
    print "import androidx.compose.foundation.shape.RoundedCornerShape"
    next
}
/fun DashboardScreen\(viewModel: DashboardViewModel = viewModel\(\)\) \{/ {
    in_dash = 1
}
in_dash == 1 && /^}$/ {
    in_dash = 0
    print "    // DashboardScreen UI Replaced\n}"
    next
}
in_dash == 1 { next }
{ print }
' app/src/main/java/com/example/MainActivity.kt > tmp.kt && mv tmp.kt app/src/main/java/com/example/MainActivity.kt
