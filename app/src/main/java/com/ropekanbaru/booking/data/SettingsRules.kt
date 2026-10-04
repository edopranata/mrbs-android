package com.ropekanbaru.booking.data

import com.ropekanbaru.booking.data.remote.SettingsDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Pilihan & penyesuaian di halaman Pengaturan, sama dengan versi web: semua jam dan durasi
 * mengikuti kelipatan interval slot agar lolos validasi backend.
 */
object SettingsRules {

    val slotChoices = listOf(15, 30, 60)

    /** Semua kolom ikut di-encode (termasuk yang sama dengan nilai default kelas). */
    private val encoder = Json { encodeDefaults = true }

    /** Jam buka/tutup yang bisa dipilih (00:00 s.d. 23:xx, kelipatan slot). */
    fun dayTimes(slot: Int): List<String> = (0 until 24 * 60 step slot.coerceAtLeast(1)).map(TimeSlots::format)

    private fun span(s: SettingsDto) = TimeSlots.toMinutes(s.closeTime) - TimeSlots.toMinutes(s.openTime)

    private fun durations(slot: Int, from: Int, to: Int): List<Int> =
        if (slot <= 0) emptyList() else (slot..to step slot).filter { it >= from }

    fun minDurationOptions(s: SettingsDto): List<Int> = durations(s.slotMinutes, s.slotMinutes, minOf(240, span(s)))

    fun maxDurationOptions(s: SettingsDto): List<Int> = durations(s.slotMinutes, s.minDuration, span(s))

    /** Saat interval berubah, bulatkan nilai lain ke kelipatan terdekat agar tetap valid. */
    fun withSlot(s: SettingsDto, slot: Int): SettingsDto {
        val up = { m: Int -> (m + slot - 1) / slot * slot }
        val down = { m: Int -> m / slot * slot }
        val min = maxOf(slot, up(s.minDuration))
        return s.copy(
            slotMinutes = slot,
            openTime = TimeSlots.format(up(TimeSlots.toMinutes(s.openTime))),
            closeTime = TimeSlots.format(down(TimeSlots.toMinutes(s.closeTime))),
            minDuration = min,
            maxDuration = maxOf(min, down(s.maxDuration)),
        )
    }

    /**
     * Kolom yang berbeda dari nilai tersimpan, untuk PUT /settings. Dibandingkan per kolom dengan
     * semua kolom terisi, jadi nilai yang kebetulan sama dengan default kelas tetap terkirim.
     */
    fun changes(saved: SettingsDto, form: SettingsDto): JsonObject {
        val before = encoder.encodeToJsonElement(SettingsDto.serializer(), saved.copy(appSubtitle = saved.appSubtitle.orEmpty())).jsonObject
        val after = encoder.encodeToJsonElement(SettingsDto.serializer(), form.copy(appSubtitle = form.appSubtitle.orEmpty())).jsonObject
        return JsonObject(after.filter { (key, value) -> before[key] != value })
    }

    /** Ringkasan aturan dalam bahasa sehari-hari. */
    fun summary(s: SettingsDto, duration: (Int) -> String): String =
        "Ruang rapat bisa dipesan pukul ${s.openTime}–${s.closeTime} dengan interval ${s.slotMinutes} menit. " +
            "Durasi satu booking ${duration(s.minDuration)} sampai ${duration(s.maxDuration)}. " +
            "User biasa bisa memesan hingga ${s.maxAdvanceDays} hari ke depan, " +
            "dan booking berulang maksimal ${s.maxRepeatWeeks} minggu."
}
