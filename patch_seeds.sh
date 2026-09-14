awk '
/fun SeedCalculatorScreen/ { in_func = 1 }
in_func == 1 && /^}/ { in_func = 0; print "    // SeedCalculatorScreen Replaced\n}"; next }
in_func == 1 { next }
{ print }
' app/src/main/java/com/example/MainActivity.kt > tmp.kt && mv tmp.kt app/src/main/java/com/example/MainActivity.kt
