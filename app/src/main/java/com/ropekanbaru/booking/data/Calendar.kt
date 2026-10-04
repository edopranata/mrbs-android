package com.ropekanbaru.booking.data

import com.ropekanbaru.booking.data.remote.BookingDto
import com.ropekanbaru.booking.data.remote.RoomScheduleDto
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

/** Tampilan jadwal, sama dengan versi web (Hari / Minggu / Bulan). */
enum class ScheduleMode(val label: String) { Day("Hari"), Week("Minggu"), Month("Bulan") }

/**
 * Rentang tanggal tiap tampilan. Minggu dimulai hari Minggu seperti versi web, dan tampilan
 * bulan selalu 6 baris × 7 hari (42 tanggal, sesuai batas rentang API jadwal).
 */
object CalendarRange {

    fun weekStart(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))

    fun monthDays(date: LocalDate): List<LocalDate> {
        val first = weekStart(date.withDayOfMonth(1))
        return List(42) { first.plusDays(it.toLong()) }
    }

    fun range(mode: ScheduleMode, date: LocalDate): ClosedRange<LocalDate> = when (mode) {
        ScheduleMode.Day -> date..date
        ScheduleMode.Week -> weekStart(date).let { it..it.plusDays(6) }
        ScheduleMode.Month -> monthDays(date).let { it.first()..it.last() }
    }

    /** Tombol ‹ › berpindah satu hari, satu minggu, atau satu bulan. */
    fun shift(mode: ScheduleMode, date: LocalDate, steps: Long): LocalDate = when (mode) {
        ScheduleMode.Day -> date.plusDays(steps)
        ScheduleMode.Week -> date.plusWeeks(steps)
        ScheduleMode.Month -> date.plusMonths(steps).withDayOfMonth(1)
    }
}

/** Status satu ruangan saat ini, untuk kartu "Status Ruangan Saat Ini" di Beranda. */
data class RoomStatus(val room: RoomScheduleDto, val current: BookingDto?, val next: BookingDto?)

fun roomStatuses(rooms: List<RoomScheduleDto>, now: LocalDateTime): List<RoomStatus> = rooms.map { room ->
    val today = room.bookings.filter { it.date == now.toLocalDate().toString() && !it.isCancelled }
    fun BookingDto.at(time: String) = LocalDateTime.of(LocalDate.parse(date), LocalTime.parse(time))
    RoomStatus(
        room = room,
        current = today.firstOrNull { !now.isBefore(it.at(it.startTime)) && now.isBefore(it.at(it.endTime)) },
        next = today.filter { it.at(it.startTime).isAfter(now) }.minByOrNull { it.startTime },
    )
}

/**
 * Logika grid jadwal (tablet): satu baris = satu interval slot. Sama dengan TimeGrid versi web.
 * Semua nilai waktu dalam menit sejak 00:00.
 */
class GridSlots(val open: Int, val close: Int, val slot: Int) {

    val starts: List<Int> = if (slot <= 0) emptyList() else (open until close step slot).filter { it + slot <= close }

    /** Slot pada posisi vertikal [rows] (diukur dalam tinggi baris dari atas grid). */
    fun slotAt(rows: Float): Int = open + (rows.toInt().coerceIn(0, (starts.size - 1).coerceAtLeast(0))) * slot

    fun isBooked(bookings: List<BookingDto>, m: Int): Boolean = bookings.any {
        !it.isCancelled && TimeSlots.toMinutes(it.startTime) < m + slot && TimeSlots.toMinutes(it.endTime) > m
    }

    /** Hari yang sudah lewat seluruhnya terkunci; hari ini terkunci sampai jam sekarang. */
    fun isPast(date: LocalDate, m: Int, now: LocalDateTime): Boolean = when {
        date.isBefore(now.toLocalDate()) -> true
        date.isAfter(now.toLocalDate()) -> false
        else -> m < now.hour * 60 + now.minute
    }

    fun isFree(date: LocalDate, bookings: List<BookingDto>, m: Int, now: LocalDateTime): Boolean =
        m >= open && m + slot <= close && !isPast(date, m, now) && !isBooked(bookings, m)

    /** Perluas pilihan dari [anchor] ke arah [target], berhenti sebelum slot terpakai / sudah lewat. */
    fun extend(anchor: Int, target: Int, free: (Int) -> Boolean): Int {
        val step = if (target >= anchor) slot else -slot
        var current = anchor
        while (current != target && free(current + step)) current += step
        return current
    }
}

/** Hitungan untuk pantauan "Semua Booking" hari ini (sama dengan versi web). Menit sejak 00:00. */
object TodayProgress {
    /** Persentase berjalan (0–100) sebuah booking yang sedang berlangsung. */
    fun percent(start: Int, end: Int, now: Int): Int =
        if (end <= start) 100 else (((now - start) * 100f) / (end - start)).toInt().coerceIn(0, 100)

    /** Sisa waktu dalam menit (tidak negatif). */
    fun remaining(end: Int, now: Int): Int = (end - now).coerceAtLeast(0)

    /** Menit sampai mulai; dianggap "segera" bila ≤ 15 menit. */
    fun startsIn(start: Int, now: Int): Int = start - now

    fun startsSoon(start: Int, now: Int): Boolean = start - now <= 15
}
