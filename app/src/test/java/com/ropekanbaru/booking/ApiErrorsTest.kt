package com.ropekanbaru.booking

import com.ropekanbaru.booking.data.remote.ApiErrors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiErrorsTest {

    @Test
    fun `pesan validasi Laravel dipakai apa adanya`() {
        val body = """{"message":"Username atau password salah.","errors":{"username":["Username atau password salah."]}}"""
        assertEquals("Username atau password salah.", ApiErrors.fromHttp(422, body))
    }

    @Test
    fun `terlalu banyak percobaan login`() {
        assertEquals(
            "Terlalu banyak percobaan. Tunggu sebentar lalu coba lagi.",
            ApiErrors.fromHttp(429, """{"message":"Too Many Attempts."}"""),
        )
    }

    @Test
    fun `error server dan body tidak valid`() {
        assertEquals("Server sedang bermasalah. Silakan coba lagi nanti.", ApiErrors.fromHttp(500, "<html>"))
        assertEquals("Akun Anda telah dinonaktifkan. Hubungi admin.", ApiErrors.fromHttp(403, """{"message":"Akun Anda telah dinonaktifkan. Hubungi admin."}"""))
        assertEquals("Terjadi kesalahan (404).", ApiErrors.fromHttp(404, null))
    }

    @Test
    fun `pesan koneksi gagal memberi petunjuk hanya di mode develop`() {
        assertEquals("Tidak dapat terhubung ke server. Periksa koneksi internet Anda.", ApiErrors.connectionMessage(devHint = false))
        assertTrue(ApiErrors.connectionMessage(devHint = true).contains("./gradlew adbReverse"))
    }
}
