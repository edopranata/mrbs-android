package com.ropekanbaru.booking

import com.ropekanbaru.booking.data.CalendarRange
import com.ropekanbaru.booking.data.GridSlots
import com.ropekanbaru.booking.data.ScheduleMode
import com.ropekanbaru.booking.data.remote.BookingDto
import com.ropekanbaru.booking.data.remote.RoomScheduleDto
import com.ropekanbaru.booking.data.roomStatuses
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

class CalendarTest {

    private val thursday = LocalDate.of(2026, 10, 8)

    @Test
    fun `minggu dimulai hari Minggu seperti versi web`() {
        val range = CalendarRange.range(ScheduleMode.Week, thursday)
        assertEquals(LocalDate.of(2026, 10, 4), range.start)
        assertEquals(LocalDate.of(2026, 10, 10), range.endInclusive)
        assertEquals(LocalDate.of(2026, 10, 4), CalendarRange.weekStart(LocalDate.of(2026, 10, 4)))
    }

    @Test
    fun `tampilan bulan selalu 42 hari mulai hari Minggu dan memuat seluruh bulan`() {
        val days = CalendarRange.monthDays(thursday)
        assertEquals(42, days.size)
        assertEquals(DayOfWeek.SUNDAY, days.first().dayOfWeek)
        assertEquals(LocalDate.of(2026, 9, 27), days.first())
        assertEquals(true, days.containsAll((1..31).map { LocalDate.of(2026, 10, it) }))
        // Rentang tidak melebihi batas API jadwal (42 hari).
        val range = CalendarRange.range(ScheduleMode.Month, thursday)
        assertEquals(41, range.endInclusive.toEpochDay() - range.start.toEpochDay())
    }

    @Test
    fun `tombol geser mengikuti tampilan`() {
        assertEquals(thursday.plusDays(1), CalendarRange.shift(ScheduleMode.Day, thursday, 1))
        assertEquals(thursday.minusWeeks(1), CalendarRange.shift(ScheduleMode.Week, thursday, -1))
        assertEquals(LocalDate.of(2026, 11, 1), CalendarRange.shift(ScheduleMode.Month, thursday, 1))
        // 31 Januari + 1 bulan tidak boleh melompati Februari.
        assertEquals(LocalDate.of(2026, 2, 1), CalendarRange.shift(ScheduleMode.Month, LocalDate.of(2026, 1, 31), 1))
    }

    private fun booking(id: Long, start: String, end: String, date: String = "2026-10-08", status: String = "confirmed") =
        BookingDto(id = id, title = "Rapat $id", date = date, startTime = start, endTime = end, status = status)

    @Test
    fun `status ruangan saat ini dan booking berikutnya`() {
        val room = RoomScheduleDto(
            id = 1,
            name = "WAR ROOM",
            floor = 3,
            bookings = listOf(
                booking(1, "08:00", "09:00"),
                booking(2, "09:30", "11:00"),
                booking(3, "13:00", "14:00"),
                booking(4, "11:00", "12:00", status = "cancelled"),
                booking(5, "15:00", "16:00"),
            ),
        )
        val at10 = roomStatuses(listOf(room), LocalDateTime.of(2026, 10, 8, 10, 0)).single()
        assertEquals(2L, at10.current?.id)
        assertEquals(3L, at10.next?.id)

        // Tepat di jam selesai: sudah kosong.
        val at11 = roomStatuses(listOf(room), LocalDateTime.of(2026, 10, 8, 11, 0)).single()
        assertNull(at11.current)
        assertEquals(3L, at11.next?.id)

        val evening = roomStatuses(listOf(room), LocalDateTime.of(2026, 10, 8, 17, 0)).single()
        assertNull(evening.current)
        assertNull(evening.next)
    }

    @Test
    fun `grid jadwal - slot, status terpakai dan sudah lewat`() {
        val grid = GridSlots(open = 7 * 60, close = 22 * 60 + 30, slot = 30)
        assertEquals(31, grid.starts.size) // 07:00 s.d. 22:00
        assertEquals(7 * 60, grid.slotAt(0.4f))
        assertEquals(9 * 60 + 30, grid.slotAt(5.9f))
        assertEquals(22 * 60, grid.slotAt(99f)) // di bawah grid: slot terakhir

        val bookings = listOf(booking(1, "09:00", "10:00"), booking(2, "11:00", "12:00", status = "cancelled"))
        assertEquals(true, grid.isBooked(bookings, 9 * 60 + 30))
        assertEquals(false, grid.isBooked(bookings, 10 * 60)) // tepat setelah selesai
        assertEquals(false, grid.isBooked(bookings, 11 * 60)) // booking batal tidak menempati slot

        val now = LocalDateTime.of(2026, 10, 8, 10, 15)
        assertEquals(true, grid.isPast(thursday.minusDays(1), 20 * 60, now))
        assertEquals(true, grid.isPast(thursday, 10 * 60, now))
        assertEquals(false, grid.isPast(thursday, 10 * 60 + 30, now))
        assertEquals(false, grid.isPast(thursday.plusDays(1), 7 * 60, now))
    }

    @Test
    fun `grid jadwal - seret berhenti sebelum slot terpakai`() {
        val grid = GridSlots(open = 7 * 60, close = 22 * 60, slot = 30)
        val bookings = listOf(booking(1, "12:00", "13:00"))
        val now = LocalDateTime.of(2026, 10, 1, 8, 0)
        val free = { m: Int -> grid.isFree(thursday, bookings, m, now) }
        assertEquals(11 * 60 + 30, grid.extend(10 * 60, 14 * 60, free)) // ke bawah, berhenti sebelum 12:00
        assertEquals(7 * 60, grid.extend(9 * 60, 6 * 60, free)) // ke atas, berhenti di jam buka
        assertEquals(10 * 60, grid.extend(10 * 60, 10 * 60, free))
    }
}
