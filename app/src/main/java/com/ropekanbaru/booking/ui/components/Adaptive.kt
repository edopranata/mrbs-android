package com.ropekanbaru.booking.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Tampilan tablet (lebar ≥ 600dp, mis. tablet atau HP lipat yang dibuka): sidebar seperti versi web,
 * grid jadwal, dan dialog di tengah layar. Di bawah itu memakai tampilan HP (navigasi bawah).
 */
@Composable
fun isWideLayout(): Boolean = LocalConfiguration.current.screenWidthDp >= 600

/** Warna blok booking per jenis rapat, sama dengan versi web (lime = internal, teal = eksternal). */
data class BlockColors(val background: Color, val border: Color, val content: Color)

fun bookingBlockColors(type: String): BlockColors =
    if (type == "external") BlockColors(Color(0xFF99F6E4), Color(0xFF14B8A6), Color(0xFF042F2E))
    else BlockColors(Color(0xFFD9F99D), Color(0xFF84CC16), Color(0xFF1A2E05))

/**
 * Dialog form: layar penuh di HP, kotak di tengah layar (seperti modal versi web) di tablet.
 * Isinya biasanya Scaffold dengan TopAppBar Tutup/Simpan.
 */
@Composable
fun FormDialog(onDismiss: () -> Unit, maxWidth: Dp = 680.dp, content: @Composable () -> Unit) {
    val wide = isWideLayout()
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            if (wide) Modifier.widthIn(max = maxWidth).fillMaxWidth(0.92f).fillMaxHeight(0.92f).clip(RoundedCornerShape(20.dp))
            else Modifier.fillMaxSize(),
        ) { content() }
    }
}

/** Item grid yang memenuhi satu baris penuh (judul bagian, pesan kosong, tombol muat lagi). */
val FullSpan: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }

/** Kolom daftar kartu: 1 kolom di HP, 2–3 kolom di tablet. */
val CardColumns = GridCells.Adaptive(minSize = 340.dp)
