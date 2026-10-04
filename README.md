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
2. Jalankan backend lokal dari repository mrbs-backend: `php artisan serve` (port 8000).
3. Nyalakan emulator atau sambungkan HP (USB debugging aktif).
4. Klik **Run** di Android Studio, lalu login dengan akun backend (mis. `user` / `password`).
   Setiap build debug otomatis menjalankan `adb reverse tcp:8000 tcp:8000` ke semua
   emulator/HP yang tersambung, sehingga `127.0.0.1:8000` di perangkat diteruskan ke Mac.

> Muncul "Tidak dapat terhubung ke server" padahal `php artisan serve` jalan? Emulator/HP
> kemungkinan baru dinyalakan ulang setelah build. Klik Run lagi atau jalankan
> `./gradlew adbReverse`.

### Alamat server API

| Build | Alamat |
|---|---|
| debug | `http://127.0.0.1:8000/api/` (server lokal lewat `adb reverse`) |
| release | `https://booking.ropekanbaru.com/api/` |

Alamat debug bisa diganti tanpa mengubah kode lewat `local.properties`, mis.
`mrbs.apiUrl=https://booking.ropekanbaru.com/api/`. Alias emulator `10.0.2.2` tidak dipakai karena
aplikasi yang menargetkan Android 17 (API 37) tidak bisa menjangkaunya.

### Dari terminal

Butuh `JAVA_HOME` ke JDK Android Studio dan `ANDROID_HOME` ke Android SDK:

```bash
./gradlew assembleDebug        # APK debug: app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug         # pasang ke emulator/HP yang tersambung
./gradlew testDebugUnitTest    # unit test
```

## Fitur

Sama dengan versi web, sesuai level akun:

- **Semua user:** Dashboard (status ruangan saat ini, booking saya berikutnya), Jadwal Ruangan
  (Hari / Minggu / Bulan, "Sorot booking saya"), buat booking (termasuk berulang mingguan dengan
  pratinjau tiap tanggal), ubah & batalkan booking, Booking Saya (mendatang / riwayat / dibatalkan),
  Ruangan, Profil & ubah password.
- **Admin:** Semua Booking (hari ini), hapus booking permanen, kelola ruangan, Manajemen User,
  statistik pemakaian ruangan bulan ini.
- **System Admin:** Pengaturan (nama aplikasi, jam operasional, aturan booking).

### Tampilan HP & tablet

Tata letak menyesuaikan lebar layar (`ui/components/Adaptive.kt`):

| Lebar | Tampilan |
|---|---|
| < 600dp (HP) | Navigasi bawah (Beranda, Jadwal, Booking Saya, Lainnya), form layar penuh, detail dari bawah |
| ≥ 600dp (tablet, HP lipat dibuka) | Seperti versi web: sidebar menu, grid jadwal waktu × ruangan (ketuk slot, atau tekan lama lalu seret untuk memilih jam), kalender bulan, daftar kartu multi-kolom, form & detail sebagai dialog di tengah |

Kalender kecil di samping jadwal muncul bila area konten ≥ 900dp (mis. tablet landscape).

## Struktur kode

```
app/src/main/java/com/ropekanbaru/booking/
  AppContainer.kt          # dependensi aplikasi (API, sesi, pengaturan)
  data/remote/             # Retrofit API, model JSON, pesan error
  data/session/            # token login terenkripsi (Android Keystore + DataStore)
  data/                    # login/sesi, aturan jam booking, rentang kalender & grid jadwal, aturan Pengaturan
  ui/MainScreen.kt         # kerangka: navigasi bawah (HP) atau sidebar (tablet)
  ui/home, schedule, mybookings, booking, rooms, profile, admin, more   # layar per menu
  ui/components/           # komponen bersama (kartu, dialog, loader, tata letak adaptif)
```

## Keamanan

File keystore untuk rilis (`*.jks`, `*.keystore`) dan `keystore.properties` sudah diabaikan oleh
`.gitignore` — **jangan pernah di-commit**. Simpan cadangannya di tempat aman: tanpa keystore yang
sama, aplikasi tidak bisa diperbarui di Play Store.
