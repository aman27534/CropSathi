sed -i '/implementation(libs.androidx.compose.ui)/a \  implementation(libs.vico.compose)\n  implementation(libs.vico.compose.m3)\n  implementation(libs.vico.core)' app/build.gradle.kts
