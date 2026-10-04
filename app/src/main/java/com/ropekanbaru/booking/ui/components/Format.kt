package com.ropekanbaru.booking.ui.components

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val localeId: Locale = Locale.forLanguageTag("id-ID")
private val shortDate = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", localeId)
private val longDate = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", localeId)

/** "2026-10-05" -> "Sen, 5 Okt 2026" (atau "Hari ini" / "Besok"). */
fun friendlyDate(date: String, today: LocalDate = LocalDate.now()): String = runCatching {
    val d = LocalDate.parse(date)
    when (d) {
        today -> "Hari ini"
        today.plusDays(1) -> "Besok"
        else -> d.format(shortDate)
    }
}.getOrDefault(date)

private val dayMonth = DateTimeFormatter.ofPattern("d MMM", localeId)
private val dayMonthYear = DateTimeFormatter.ofPattern("d MMM yyyy", localeId)
private val monthYear = DateTimeFormatter.ofPattern("MMMM yyyy", localeId)
private val weekdayDayMonth = DateTimeFormatter.ofPattern("EEEE, d MMM", localeId)

/** "4 – 10 Okt 2026" atau "27 Sep – 3 Okt 2026". */
fun weekTitle(from: LocalDate, to: LocalDate): String =
    if (from.month == to.month) "${from.dayOfMonth} – ${to.format(dayMonthYear)}"
    else "${from.format(dayMonth)} – ${to.format(dayMonthYear)}"

/** "Oktober 2026". */
fun monthTitle(date: LocalDate): String = date.format(monthYear).replaceFirstChar { it.titlecase(localeId) }

/** "Kamis, 8 Okt". */
fun weekdayDate(date: LocalDate): String = date.format(weekdayDayMonth).replaceFirstChar { it.titlecase(localeId) }

/** 90 -> "1 jam 30 menit". */
fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return listOfNotNull(h.takeIf { it > 0 }?.let { "$it jam" }, m.takeIf { it > 0 || h == 0 }?.let { "$it menit" }).joinToString(" ")
}

fun longDate(date: LocalDate = LocalDate.now()): String =
    date.format(longDate).replaceFirstChar { it.titlecase(localeId) }

/** Warna ruangan dari backend ("#4f46e5"), dengan cadangan bila tidak valid. */
fun roomColor(hex: String?, fallback: Color): Color = runCatching {
    Color(android.graphics.Color.parseColor(hex))
}.getOrDefault(fallback)
