import java.io.ByteArrayOutputStream
import java.util.Properties
import javax.inject.Inject

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Alamat API backend MRBS (Laravel).
// - release : server produksi.
// - debug   : server lokal `php artisan serve` (port 8000) lewat `adb reverse`, sehingga
//             localhost di emulator/HP diteruskan ke Mac. Dijalankan otomatis setiap build
//             debug; manual:  ./gradlew adbReverse   (atau: adb reverse tcp:8000 tcp:8000)
//             Catatan: alias emulator 10.0.2.2 tidak bisa dipakai aplikasi yang menargetkan
//             Android 17 (API 37). Alamat bisa diganti lewat local.properties, mis.:
//             mrbs.apiUrl=https://booking.ropekanbaru.com/api/
val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
val productionApiUrl = "https://booking.ropekanbaru.com/api/"

// Kunci rilis dibaca dari keystore.properties (tidak di-commit; lihat README bagian "Build rilis").
// Tanpa file itu, build release tetap jalan tetapi APK-nya tidak ditandatangani.
val keystoreProperties = Properties().apply {
    rootProject.file("keystore.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
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
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (!keystoreProperties.isEmpty) {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"$developmentApiUrl\"")
        }
        release {
            buildConfigField("String", "API_BASE_URL", "\"$productionApiUrl\"")
            signingConfig = signingConfigs.findByName("release")
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
    implementation(libs.androidx.compose.material.icons.core)
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

// Teruskan port 8000 semua emulator/HP yang tersambung (localhost) ke server lokal di Mac.
// Dijalankan otomatis setiap build debug (tombol Run Android Studio), karena pengaturan
// `adb reverse` hilang setiap kali emulator/HP dimulai ulang atau dicabut.
abstract class AdbReverseTask @Inject constructor(private val execOps: ExecOperations) : DefaultTask() {
    @get:Input abstract val adbPath: Property<String>

    @TaskAction
    fun run() {
        val adb = adbPath.get()
        val list = ByteArrayOutputStream()
        val listed = execOps.exec {
            commandLine(adb, "devices")
            standardOutput = list
            isIgnoreExitValue = true
        }
        val devices = if (listed.exitValue == 0) {
            list.toString().lines().drop(1).map { it.split('\t') }
                .filter { it.size == 2 && it[1].trim() == "device" }.map { it[0] }
        } else emptyList()
        if (devices.isEmpty()) {
            logger.lifecycle("adbReverse: tidak ada emulator/HP tersambung, dilewati.")
            return
        }
        devices.forEach { serial ->
            val result = execOps.exec {
                commandLine(adb, "-s", serial, "reverse", "tcp:8000", "tcp:8000")
                standardOutput = ByteArrayOutputStream()
                isIgnoreExitValue = true
            }
            logger.lifecycle("adbReverse: $serial " + if (result.exitValue == 0) "OK (tcp:8000)" else "gagal")
        }
    }
}

val adbReverse = tasks.register<AdbReverseTask>("adbReverse") {
    group = "mrbs"
    description = "adb reverse tcp:8000 tcp:8000 agar build debug terhubung ke php artisan serve."
    adbPath = androidComponents.sdkComponents.adb.map { it.asFile.absolutePath }
}
tasks.matching { it.name == "assembleDebug" }.configureEach { finalizedBy(adbReverse) }
