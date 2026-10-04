package com.ropekanbaru.booking.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Tampilan tablet (lebar ≥ 600dp, mis. tablet atau HP lipat yang dibuka): sidebar tetap dan dialog
 * di tengah layar. Di bawah itu tampilan HP seperti web mobile: sidebar sebagai laci ☰ dan modal dari bawah.
 */
@Composable
fun isWideLayout(): Boolean = LocalConfiguration.current.screenWidthDp >= 600

/** Warna blok booking per jenis rapat, sama dengan versi web (lime = internal, teal = eksternal). */
data class BlockColors(val background: Color, val border: Color, val content: Color)

fun bookingBlockColors(type: String): BlockColors =
    if (type == "external") BlockColors(Color(0xFF99F6E4), Color(0xFF14B8A6), Color(0xFF042F2E))
    else BlockColors(Color(0xFFD9F99D), Color(0xFF84CC16), Color(0xFF1A2E05))
