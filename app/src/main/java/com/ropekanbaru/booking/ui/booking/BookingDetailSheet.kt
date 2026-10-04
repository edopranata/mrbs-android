package com.ropekanbaru.booking.ui.booking

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ropekanbaru.booking.data.remote.ApiErrors
import com.ropekanbaru.booking.data.remote.BookingDetailResponse
import com.ropekanbaru.booking.data.remote.CancelRequest
import com.ropekanbaru.booking.data.remote.MrbsApi
import com.ropekanbaru.booking.ui.components.Badge
import com.ropekanbaru.booking.ui.components.ErrorCard
import com.ropekanbaru.booking.ui.components.bookingTypeColor
import com.ropekanbaru.booking.ui.components.friendlyDate
import com.ropekanbaru.booking.ui.theme.Emerald600
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailSheet(
    api: MrbsApi,
    bookingId: Long,
    currentUserId: Long,
    onDismiss: () -> Unit,
    onChanged: (message: String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var detail by remember { mutableStateOf<BookingDetailResponse?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var cancelMode by remember { mutableStateOf(false) }
    var scopeFollowing by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    LaunchedEffect(bookingId) {
        runCatching { api.booking(bookingId) }
            .onSuccess { detail = it }
            .onFailure { error = ApiErrors.message(it) }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
        ) {
            val booking = detail?.data
            when {
                booking == null && error != null -> ErrorCard(error!!)
                booking == null -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                else -> {
                    val series = detail?.series
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Badge(booking.typeLabel ?: booking.type, bookingTypeColor(booking.type))
                        if (series != null) Badge("↻ Minggu ke-${series.position} dari ${series.total}", MaterialTheme.colorScheme.primary)
                        when {
                            booking.isCancelled -> Badge("Dibatalkan", MaterialTheme.colorScheme.error)
                            booking.isOngoing -> Badge("Berlangsung", Emerald600)
                            booking.hasEnded -> Badge("Selesai", MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text(booking.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)

                    InfoRow("Ruangan", booking.room?.let { "${it.name} · Lantai ${it.floor}" } ?: "-")
                    InfoRow("Tanggal", friendlyDate(booking.date))
                    InfoRow("Waktu", "${booking.startTime} – ${booking.endTime}")
                    InfoRow(
                        "Pemesan",
                        (booking.user?.name ?: "-") +
                            (booking.user?.department?.let { " · $it" } ?: "") +
                            (if (booking.userId == currentUserId) " (Anda)" else ""),
                    )
                    InfoRow("Peserta", "${booking.participants} orang")
                    booking.description?.takeIf { it.isNotBlank() }?.let {
                        Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                            Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(12.dp))
                        }
                    }
                    if (booking.isCancelled) {
                        Text(
                            "Dibatalkan" + (booking.cancelledBy?.let { " oleh $it" } ?: "") + (booking.cancelReason?.let { ". Alasan: $it" } ?: ""),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    error?.let { ErrorCard(it) }

                    if (booking.can.cancel) {
                        if (!cancelMode) {
                            OutlinedButton(
                                onClick = { cancelMode = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("Batalkan booking") }
                        } else {
                            val following = series?.followingCancellable ?: 1
                            if (following > 1) {
                                ChoiceRow("Hanya booking ini", !scopeFollowing) { scopeFollowing = false }
                                ChoiceRow("Booking ini & minggu-minggu berikutnya ($following booking)", scopeFollowing) { scopeFollowing = true }
                            }
                            OutlinedTextField(
                                value = reason,
                                onValueChange = { reason = it },
                                label = { Text("Alasan pembatalan (opsional)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { cancelMode = false }, enabled = !busy, modifier = Modifier.weight(1f)) { Text("Kembali") }
                                Button(
                                    onClick = {
                                        busy = true
                                        scope.launch {
                                            runCatching {
                                                api.cancelBooking(
                                                    booking.id,
                                                    CancelRequest(reason.trim().ifBlank { null }, if (scopeFollowing) "following" else "single"),
                                                )
                                            }.onSuccess {
                                                onChanged(if (it.cancelledCount > 1) "${it.cancelledCount} booking mingguan dibatalkan." else "Booking berhasil dibatalkan.")
                                            }.onFailure { error = ApiErrors.message(it) }
                                            busy = false
                                        }
                                    },
                                    enabled = !busy,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    if (busy) CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                                    else Text(if (scopeFollowing) "Batalkan $following booking" else "Ya, batalkan")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(84.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ChoiceRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
