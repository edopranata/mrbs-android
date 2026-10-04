// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // Juga menetapkan versi Kotlin Gradle Plugin yang dipakai AGP (lihat gradle/libs.versions.toml).
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
