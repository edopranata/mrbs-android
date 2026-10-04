package com.ropekanbaru.booking.data.remote

import com.ropekanbaru.booking.BuildConfig
import retrofit2.HttpException
import java.io.IOException

/** Mengubah error jaringan/API menjadi pesan yang ramah untuk pengguna. */
object ApiErrors {

    /**
     * Build debug yang memakai server lokal lewat `adb reverse`: pesan koneksi gagal diberi petunjuk,
     * karena penerusan port itu hilang setiap emulator/HP dimulai ulang atau kabel USB dicabut.
     */
    private val localDevServer = BuildConfig.DEBUG && BuildConfig.API_BASE_URL.contains("127.0.0.1")

    fun message(error: Throwable): String = when (error) {
        is HttpException -> fromHttp(error.code(), error.response()?.errorBody()?.string())
        is IOException -> connectionMessage(localDevServer)
        else -> "Terjadi kesalahan. Silakan coba lagi."
    }

    fun connectionMessage(devHint: Boolean): String =
        "Tidak dapat terhubung ke server. Periksa koneksi internet Anda." + if (devHint) {
            "\n\nMode develop: pastikan `php artisan serve` berjalan di Mac, lalu jalankan " +
                "`./gradlew adbReverse` (atau scripts/adb-reverse-watch.sh) dan ketuk Coba lagi."
        } else ""

    fun fromHttp(code: Int, body: String?): String {
        val parsed = body?.let { runCatching { ApiJson.decodeFromString<ApiErrorBody>(it) }.getOrNull() }

        // Pesan validasi Laravel (sudah berbahasa Indonesia), mis. "Username atau password salah."
        parsed?.errors?.values?.firstOrNull()?.firstOrNull()?.let { return it }

        return when (code) {
            429 -> "Terlalu banyak percobaan. Tunggu sebentar lalu coba lagi."
            in 500..599 -> "Server sedang bermasalah. Silakan coba lagi nanti."
            else -> parsed?.message?.takeIf { it.isNotBlank() } ?: "Terjadi kesalahan ($code)."
        }
    }
}
