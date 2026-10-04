package com.ropekanbaru.booking.data.remote

import retrofit2.HttpException
import java.io.IOException

/** Mengubah error jaringan/API menjadi pesan yang ramah untuk pengguna. */
object ApiErrors {

    fun message(error: Throwable): String = when (error) {
        is HttpException -> fromHttp(error.code(), error.response()?.errorBody()?.string())
        is IOException -> "Tidak dapat terhubung ke server. Periksa koneksi internet Anda."
        else -> "Terjadi kesalahan. Silakan coba lagi."
    }

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
