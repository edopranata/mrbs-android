import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Alamat API backend MRBS (Laravel).
// - release : server produksi.
// - debug   : server lokal `php artisan serve` (port 8000) lewat `adb reverse`, sehingga
//             localhost di emulator/HP diteruskan ke Mac. Jalankan sekali setiap emulator/HP
//             tersambung:  ./gradlew adbReverse   (atau: adb reverse tcp:8000 tcp:8000)
//             Catatan: alias emulator 10.0.2.2 tidak bisa dipakai aplikasi yang menargetkan
//             Android 17 (API 37). Alamat bisa diganti lewat local.properties, mis.:
//             mrbs.apiUrl=https://booking.ropekanbaru.com/api/
val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
val productionApiUrl = "https://booking.ropekanbaru.com/api/"
val developmentApiUrl = localProperties.getProperty("mrbs.apiUrl") ?: "http://127.0.0.1:8000/api/"

android {
    namespace = "com.ropekanbaru.booking"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.ropekanbaru.booking"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"$developmentApiUrl\"")
        }
        release {
            buildConfigField("String", "API_BASE_URL", "\"$productionApiUrl\"")
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// Teruskan port 8000 emulator/HP (localhost) ke server lokal di Mac.
tasks.register<Exec>("adbReverse") {
    group = "mrbs"
    description = "adb reverse tcp:8000 tcp:8000 agar build debug terhubung ke php artisan serve."
    executable = androidComponents.sdkComponents.adb.get().asFile.absolutePath
    args("reverse", "tcp:8000", "tcp:8000")
}
