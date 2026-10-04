package com.ropekanbaru.booking

import com.ropekanbaru.booking.data.SettingsRules
import com.ropekanbaru.booking.data.remote.ApiJson
import com.ropekanbaru.booking.data.remote.BookingDto
import com.ropekanbaru.booking.data.remote.PageResponse
import com.ropekanbaru.booking.data.remote.SettingsDto
import com.ropekanbaru.booking.data.remote.UserSaveRequest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsRulesTest {

    private val saved = SettingsDto(
        appName = "Region 02 Pekanbaru",
        appSubtitle = "Meeting Room Booking System",
        openTime = "07:00",
        closeTime = "22:30",
        slotMinutes = 30,
        minDuration = 30,
        maxDuration = 480,
        maxAdvanceDays = 60,
        maxRepeatWeeks = 8,
    )

    @Test
    fun `hanya kolom yang berubah yang dikirim`() {
        assertTrue(SettingsRules.changes(saved, saved).isEmpty())

        // 07:00 & 30 sama dengan nilai default kelas SettingsDto, tetapi tetap harus terkirim bila berubah.
        val changed = saved.copy(openTime = "08:00", closeTime = "20:00", appSubtitle = "")
        val body = SettingsRules.changes(saved.copy(openTime = "07:30"), changed.copy(openTime = "07:00"))
        assertEquals(setOf("open_time", "close_time", "app_subtitle"), body.keys)
        assertEquals(JsonPrimitive("07:00"), body["open_time"])
        assertEquals(JsonPrimitive(""), body["app_subtitle"])
    }

    @Test
    fun `ganti interval menyelaraskan jam dan durasi`() {
        val s = SettingsRules.withSlot(saved.copy(minDuration = 30, maxDuration = 450), 60)
        assertEquals(60, s.slotMinutes)
        assertEquals("07:00", s.openTime)
        assertEquals("22:00", s.closeTime) // 22:30 dibulatkan ke bawah
        assertEquals(60, s.minDuration)
        assertEquals(420, s.maxDuration)
    }

    @Test
    fun `pilihan durasi mengikuti interval dan jam operasional`() {
        val s = saved.copy(openTime = "08:00", closeTime = "12:00", slotMinutes = 60, minDuration = 120)
        assertEquals(listOf(60, 120, 180, 240), SettingsRules.minDurationOptions(s))
        assertEquals(listOf(120, 180, 240), SettingsRules.maxDurationOptions(s))
        assertEquals(96, SettingsRules.dayTimes(15).size)
        assertEquals("23:30", SettingsRules.dayTimes(30).last())
    }

    @Test
    fun `daftar berhalaman Laravel terbaca`() {
        val json = """
            {"data":[{"id":1,"title":"A","date":"2026-10-08","start_time":"09:00","end_time":"10:00"}],
             "links":{"first":"x"},"meta":{"current_page":1,"last_page":3,"per_page":20,"total":45}}
        """.trimIndent()
        val page = ApiJson.decodeFromString<PageResponse<BookingDto>>(json)
        assertEquals(1, page.data.size)
        assertEquals(3, page.meta.lastPage)
        assertEquals(45, page.meta.total)
    }

    @Test
    fun `password kosong tidak dikirim saat mengubah user`() {
        val body = UserSaveRequest("Budi", "budi", "budi@kantor.test", null, "user", "", "", true)
        val json = ApiJson.encodeToJsonElement(UserSaveRequest.serializer(), body).jsonObject
        assertFalse("password" in json)
        // Divisi kosong tetap dikirim agar bisa dikosongkan.
        assertEquals(JsonPrimitive(""), json["department"])
        assertEquals(JsonPrimitive(true), json["is_active"])
    }
}
