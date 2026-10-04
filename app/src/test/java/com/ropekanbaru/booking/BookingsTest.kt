package com.ropekanbaru.booking

import com.ropekanbaru.booking.data.CreateBookingResult
import com.ropekanbaru.booking.data.TimeSlots
import com.ropekanbaru.booking.data.isSelectable
import com.ropekanbaru.booking.data.remote.ApiJson
import com.ropekanbaru.booking.data.remote.AvailabilityRoomDto
import com.ropekanbaru.booking.data.remote.SettingsDto
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class BookingsTest {

    private val settings = SettingsDto(openTime = "07:00", closeTime = "20:00", slotMinutes = 30, minDuration = 30, maxDuration = 480)
    private val today = LocalDate.of(2026, 10, 5)

    @Test
    fun `jam mulai mengikuti jam operasional dan interval`() {
        val options = TimeSlots.startOptions(settings, today.plusDays(1), today, LocalTime.of(10, 0))
        assertEquals("07:00", options.first())
        assertEquals("19:30", options.last()) // tutup 20:00 dikurangi durasi minimal 30 menit
        assertTrue(options.all { it.endsWith(":00") || it.endsWith(":30") })
    }

    @Test
    fun `hari ini jam yang sudah lewat tidak ditawarkan`() {
        val options = TimeSlots.startOptions(settings, today, today, LocalTime.of(10, 10))
        assertEquals("10:30", options.first())
    }

    @Test
    fun `jam selesai dibatasi durasi minimal, maksimal, dan jam tutup`() {
        assertEquals("09:30", TimeSlots.endOptions(settings, "09:00").first())
        assertEquals("17:00", TimeSlots.endOptions(settings, "09:00").last()) // maks 8 jam
        assertEquals("20:00", TimeSlots.endOptions(settings, "18:00").last()) // jam tutup
        assertEquals("10:00", TimeSlots.defaultEnd(settings, "09:00"))
    }

    @Test
    fun `hasil buat booking tunggal dan berulang`() {
        val single = ApiJson.decodeFromString<JsonObject>("""{"data":{"id":1}}""")
        assertEquals("Booking berhasil dibuat.", CreateBookingResult.from(single).message)

        val weekly = ApiJson.decodeFromString<JsonObject>(
            """{"data":[{"id":1},{"id":2},{"id":3}],"skipped":[{"date":"2026-10-19","reason":"Bentrok"}]}""",
        )
        val result = CreateBookingResult.from(weekly)
        assertEquals(3, result.created)
        assertEquals("2026-10-19", result.skipped.single().date)
        assertEquals("3 booking mingguan dibuat, 1 tanggal dilewati.", result.message)
    }

    @Test
    fun `ruangan bentrok sebagian bisa dipilih saat berulang`() {
        val partial = AvailabilityRoomDto(id = 1, name = "R", floor = 3, available = false, conflictDates = listOf("2026-10-19"))
        assertFalse(partial.isSelectable(repeatWeeks = 1))
        assertTrue(partial.isSelectable(repeatWeeks = 4))
        assertFalse(partial.copy(fitsCapacity = false).isSelectable(repeatWeeks = 4))
    }
}
