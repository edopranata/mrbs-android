# MRBS Android — Meeting Room Booking System

Aplikasi Android (Kotlin) untuk pemesanan ruang rapat kantor. Terhubung ke REST API di
repository [mrbs-backend](https://github.com/edopranata/mrbs-backend); versi web ada di
[mrbs-vue](https://github.com/edopranata/mrbs-vue).

| | |
|---|---|
| Package / applicationId | `com.ropekanbaru.booking` |
| Min SDK / Target SDK | 26 (Android 8.0) / 37 |
| Build | Gradle 9.6 (wrapper), Android Gradle Plugin 9.4, Kotlin DSL |
| JDK | JDK bawaan Android Studio (JBR 25) |

## Menjalankan

1. Buka folder ini di **Android Studio** (2026.2 atau lebih baru). Android Studio membuat
   `local.properties` berisi lokasi Android SDK secara otomatis (file ini tidak di-commit).
2. Pilih emulator atau HP (USB debugging aktif), lalu klik **Run**.

Dari terminal (butuh `JAVA_HOME` ke JDK Android Studio dan `ANDROID_HOME` ke Android SDK):

```bash
./gradlew assembleDebug        # APK debug: app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug         # pasang ke emulator/HP yang tersambung
./gradlew test                 # unit test
```

## Keamanan

File keystore untuk rilis (`*.jks`, `*.keystore`) dan `keystore.properties` sudah diabaikan oleh
`.gitignore` — **jangan pernah di-commit**. Simpan cadangannya di tempat aman: tanpa keystore yang
sama, aplikasi tidak bisa diperbarui di Play Store.
