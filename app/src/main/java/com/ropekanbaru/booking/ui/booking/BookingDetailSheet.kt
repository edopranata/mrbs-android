package com.ropekanbaru.booking.ui.booking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.ropekanbaru.booking.data.TimeSlots
import com.ropekanbaru.booking.data.remote.ApiErrors
import com.ropekanbaru.booking.data.remote.BookingDetailResponse
import com.ropekanbaru.booking.data.remote.BookingDto
import com.ropekanbaru.booking.data.remote.CancelRequest
import com.ropekanbaru.booking.data.remote.MrbsApi
import com.ropekanbaru.booking.ui.components.ConfirmDialog
import com.ropekanbaru.booking.ui.components.ErrorCard
import com.ropekanbaru.booking.ui.components.FieldLabel
import com.ropekanbaru.booking.ui.components.GhostButton
import com.ropekanbaru.booking.ui.components.LabeledTextField
import com.ropekanbaru.booking.ui.components.Pill
import com.ropekanbaru.booking.ui.components.PrimaryButton
import com.ropekanbaru.booking.ui.components.SecondaryButton
import com.ropekanbaru.booking.ui.components.StatusBadge
import com.ropekanbaru.booking.ui.components.Tw
import com.ropekanbaru.booking.ui.components.WebModal
import com.ropekanbaru.booking.ui.components.bookingBlockColors
import com.ropekanbaru.booking.ui.components.formatDuration
import com.ropekanbaru.booking.ui.components.longDate
import com.ropekanbaru.booking.ui.components.roomColor
import com.ropekanbaru.booking.ui.icons.Apartment
import com.ropekanbaru.booking.ui.icons.CalendarMonth
import com.ropekanbaru.booking.ui.icons.Cancel
import com.ropekanbaru.booking.ui.icons.DeleteOutline
import com.ropekanbaru.booking.ui.icons.Groups
import com.ropekanbaru.booking.ui.icons.PersonOutline
import com.ropekanbaru.booking.ui.icons.Schedule
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Detail booking seperti versi web: status, info, pembatalan (termasuk seri mingguan), ubah, hapus. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookingDetailSheet(
    api: MrbsApi,
    bookingId: Long,
    currentUserId: Long,
    isAdmin: Boolean,
    onDismiss: () -> Unit,
    onEdit: (BookingDto) -> Unit,
    onChanged: (message: String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var detail by remember { mutableStateOf<BookingDetailResponse?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var cancelMode by remember { mutableStateOf(false) }
    var scopeFollowing by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(bookingId) {
        runCatching { api.booking(bookingId) }
            .onSuccess { detail = it }
            .onFailure { error = ApiErrors.message(it) }
    }

    val booking = detail?.data
    val series = detail?.series
    val following = series?.followingCancellable ?: 1

    val cancel: () -> Unit = {
        val b = booking
        if (b != null) {
            busy = true
            scope.launch {
                runCatching { api.cancelBooking(b.id, CancelRequest(reason.trim().ifBlank { null }, if (scopeFollowing) "following" else "single")) }
                    .onSuccess { onChanged(if (it.cancelledCount > 1) "${it.cancelledCount} booking mingguan dibatalkan." else "Booking berhasil dibatalkan.") }
                    .onFailure { error = ApiErrors.message(it) }
                busy = false
            }
        }
    }

    WebModal(
        title = "Detail Booking",
        onDismiss = onDismiss,
        titleExtra = { booking?.let { StatusBadge(it) } },
        footer = if (booking != null && !cancelMode && (booking.can.update || booking.can.cancel || canDelete(booking, isAdmin))) {
            {
                if (canDelete(booking, isAdmin)) GhostButton("Hapus", onClick = { confirmDelete = true }, icon = Icons.Outlined.DeleteOutline, danger = true)
                Spacer(Modifier.weight(1f))
                if (booking.can.cancel) SecondaryButton("Batalkan", onClick = { cancelMode = true }, icon = Icons.Outlined.Cancel, danger = true)
                if (booking.can.update) PrimaryButton("Ubah", onClick = { onEdit(booking) }, icon = Icons.Outlined.Edit)
            }
        } else null,
    ) {
        when {
            booking == null && error != null -> ErrorCard(error!!)
            booking == null -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(booking.title, style = MaterialTheme.typography.titleLarge, color = Tw.Slate900)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val type = bookingBlockColors(booking.type)
                        Pill("Rapat ${booking.typeLabel ?: if (booking.type == "external") "Eksternal" else "Internal"}", type.background, type.content)
                        if (booking.isRecurring) {
                            Pill("↻ Mingguan" + (series?.let { " · minggu ke-${it.position} dari ${it.total}" } ?: ""), Tw.Indigo50, Tw.Indigo700)
                        }
                        if (booking.isLegacy) Pill("Dari MRBS lama", Tw.Amber50, Color(0xFFB45309))
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoRow(Icons.Outlined.Apartment) {
                        booking.room?.let {
                            Box(Modifier.size(10.dp).clip(CircleShape).background(roomColor(it.color, Tw.Indigo600)))
                            Spacer(Modifier.width(8.dp))
                            Text("${it.name} · Lantai ${it.floor}", style = MaterialTheme.typography.bodyMedium, color = Tw.Slate900)
                        } ?: Text("Memuat…", color = Tw.Slate400)
                    }
                    InfoRow(Icons.Outlined.CalendarMonth) {
                        Text(longDate(LocalDate.parse(booking.date)), style = MaterialTheme.typography.bodyMedium, color = Tw.Slate900)
                    }
                    InfoRow(Icons.Outlined.Schedule) {
                        val minutes = TimeSlots.toMinutes(booking.endTime) - TimeSlots.toMinutes(booking.startTime)
                        Text("${booking.startTime} – ${booking.endTime} (${formatDuration(minutes)})", style = MaterialTheme.typography.bodyMedium, color = Tw.Slate900)
                    }
                    InfoRow(Icons.Outlined.PersonOutline) {
                        Text(booking.user?.name ?: "-", style = MaterialTheme.typography.bodyMedium, color = Tw.Slate900)
                        booking.user?.department?.let { Text(" · $it", style = MaterialTheme.typography.bodyMedium, color = Tw.Slate500) }
                        if (booking.userId == currentUserId) {
                            Spacer(Modifier.width(6.dp))
                            Pill("Anda", Tw.Slate100, Tw.Slate600)
                        }
                    }
                    InfoRow(Icons.Outlined.Groups) {
                        Text("${booking.participants} peserta", style = MaterialTheme.typography.bodyMedium, color = Tw.Slate900)
                    }
                }

                booking.description?.takeIf { it.isNotBlank() }?.let {
                    Surface(shape = RoundedCornerShape(8.dp), color = Tw.Slate50, modifier = Modifier.fillMaxWidth()) {
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = Tw.Slate600, modifier = Modifier.padding(12.dp))
                    }
                }
                if (booking.legacyLocked) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Tw.Amber50, border = BorderStroke(1.dp, Color(0xFFFDE68A)), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Booking ini berasal dari MRBS lama. Selama masa transisi, ubah atau batalkan di MRBS lama; perubahannya akan tersinkron otomatis.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF92400E),
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }
                if (booking.isCancelled) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Tw.Red50, border = BorderStroke(1.dp, Tw.Red200), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Dibatalkan" + (booking.cancelledBy?.let { " oleh $it" } ?: "") + "." + (booking.cancelReason?.let { " Alasan: $it" } ?: ""),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Tw.Red700,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }
                error?.let { ErrorCard(it) }

                if (cancelMode && booking.can.cancel) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color.White, border = BorderStroke(1.dp, Tw.Slate200), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (following > 1) {
                                FieldLabel("Batalkan")
                                val day = LocalDate.parse(booking.date).format(DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("id-ID")))
                                ChoiceRow("Hanya booking ini ($day)", !scopeFollowing) { scopeFollowing = false }
                                ChoiceRow("Booking ini & minggu-minggu berikutnya ($following booking)", scopeFollowing) { scopeFollowing = true }
                            }
                            LabeledTextField(
                                label = "Alasan pembatalan (opsional)",
                                value = reason,
                                onValueChange = { reason = it },
                                placeholder = "mis. Rapat ditunda",
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End), modifier = Modifier.fillMaxWidth()) {
                                SecondaryButton("Kembali", onClick = { cancelMode = false }, enabled = !busy)
                                PrimaryButton(
                                    if (busy) "Membatalkan…" else if (scopeFollowing) "Ya, batalkan $following booking" else "Ya, batalkan booking",
                                    onClick = cancel,
                                    enabled = !busy,
                                    danger = true,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmDelete && booking != null) {
        ConfirmDialog(
            title = "Hapus booking",
            message = "Booking \"${booking.title}\" akan dihapus permanen dari sistem. Lanjutkan?",
            confirmText = "Hapus",
            danger = true,
            onDismiss = { confirmDelete = false },
            onConfirm = {
                scope.launch {
                    runCatching { api.deleteBooking(booking.id) }
                        .onSuccess { onChanged(it.message ?: "Booking berhasil dihapus.") }
                        .onFailure { error = ApiErrors.message(it) }
                }
            },
        )
    }
}

@Composable
private fun InfoRow(icon: ImageVector, content: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Tw.Slate400, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) { content() }
    }
}

@Composable
private fun ChoiceRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).clickable(role = Role.RadioButton, onClick = onClick),
    ) {
        RadioButton(selected = selected, onClick = null, modifier = Modifier.padding(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Tw.Slate700)
    }
}

/** Hapus permanen: mengikuti izin dari server (can.delete); server lama tanpa field itu → admin. */
private fun canDelete(booking: BookingDto, isAdmin: Boolean): Boolean = booking.can.delete ?: isAdmin
