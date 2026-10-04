package com.ropekanbaru.booking.data

import com.ropekanbaru.booking.data.remote.ApiJson
import com.ropekanbaru.booking.data.remote.AvailabilityRoomDto
import com.ropekanbaru.booking.data.remote.SettingsDto
import com.ropekanbaru.booking.data.remote.SkippedDate
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import java.time.LocalDate
import java.time.LocalTime

/** Hasil POST /bookings: jumlah booking yang dibuat & tanggal yang dilewati (booking berulang). */
data class CreateBookingResult(val created: Int, val skipped: List<SkippedDate>) {
    val message: String
        get() = when {
            created > 1 && skipped.isNotEmpty() -> "$created booking mingguan dibuat, ${skipped.size} tanggal dilewati."
            created > 1 -> "$created booking mingguan berhasil dibuat."
            else -> "Booking berhasil dibuat."
        }

    companion object {
        fun from(json: JsonObject): CreateBookingResult {
            val created = (json["data"] as? JsonArray)?.size ?: 1
            val skipped = json["skipped"]?.let { ApiJson.decodeFromJsonElement<List<SkippedDate>>(it) } ?: emptyList()
            return CreateBookingResult(created, skipped)
        }
    }
}

/** Pilihan jam booking mengikuti Pengaturan backend (jam operasional, interval, durasi). */
object TimeSlots {

    fun toMinutes(time: String): Int = time.substring(0, 2).toInt() * 60 + time.substring(3, 5).toInt()

    fun format(minutes: Int): String = "%02d:%02d".format(minutes / 60, minutes % 60)

    /** Jam mulai yang valid; untuk hari ini, jam yang sudah lewat tidak ditawarkan. */
    fun startOptions(s: SettingsDto, date: LocalDate, today: LocalDate, now: LocalTime): List<String> {
        val earliest = if (date == today) now.hour * 60 + now.minute else Int.MIN_VALUE
        return range(toMinutes(s.openTime), toMinutes(s.closeTime) - s.minDuration, s.slotMinutes)
            .filter { it >= earliest }
            .map(::format)
    }

    /** Jam selesai yang valid untuk jam mulai tertentu. */
    fun endOptions(s: SettingsDto, start: String): List<String> {
        val startMin = toMinutes(start)
        val last = minOf(startMin + s.maxDuration, toMinutes(s.closeTime))
        return range(startMin + s.minDuration, last, s.slotMinutes).map(::format)
    }

    /** Jam selesai bawaan: 1 jam setelah mulai bila tersedia. */
    fun defaultEnd(s: SettingsDto, start: String): String? {
        val options = endOptions(s, start)
        return options.firstOrNull { toMinutes(it) == toMinutes(start) + 60 } ?: options.firstOrNull()
    }

    private fun range(from: Int, to: Int, step: Int): List<Int> =
        if (step <= 0 || from > to) emptyList() else (from..to step step).toList()
}

/**
 * Ruangan boleh dipilih bila tersedia di semua minggu, atau (saat berulang) kapasitas cukup dan
 * masih ada minggu yang kosong — minggu yang bentrok nanti bisa dilewati.
 */
fun AvailabilityRoomDto.isSelectable(repeatWeeks: Int): Boolean =
    available || (repeatWeeks > 1 && fitsCapacity && conflictDates.size < repeatWeeks)
