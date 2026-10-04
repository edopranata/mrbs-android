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

fun longDate(date: LocalDate = LocalDate.now()): String =
    date.format(longDate).replaceFirstChar { it.titlecase(localeId) }

/** Warna ruangan dari backend ("#4f46e5"), dengan cadangan bila tidak valid. */
fun roomColor(hex: String?, fallback: Color): Color = runCatching {
    Color(android.graphics.Color.parseColor(hex))
}.getOrDefault(fallback)
