package com.ropekanbaru.booking.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ropekanbaru.booking.data.remote.BookingDto
import com.ropekanbaru.booking.data.remote.RoomScheduleDto
import com.ropekanbaru.booking.ui.components.Badge
import com.ropekanbaru.booking.ui.components.DatePickerModal
import com.ropekanbaru.booking.ui.components.ErrorCard
import com.ropekanbaru.booking.ui.components.bookingTypeColor
import com.ropekanbaru.booking.ui.components.longDate
import com.ropekanbaru.booking.ui.components.roomColor
import com.ropekanbaru.booking.ui.theme.Emerald600
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleContent(
    vm: ScheduleViewModel,
    currentUserId: Long,
    onOpenBooking: (Long) -> Unit,
    onBookRoom: (roomId: Long, date: LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = vm.state
    var pickDate by rememberSaveable { mutableStateOf(false) }
    val today = LocalDate.now()

    Column(modifier.fillMaxSize()) {
        // Navigasi tanggal
        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
            ) {
                IconButton(onClick = { vm.shift(-1) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Hari sebelumnya") }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).clickable { pickDate = true }.padding(vertical = 4.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(6.dp))
                        Text(longDate(state.date), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }
                    val count = state.schedule?.rooms?.sumOf { it.bookings.size }
                    Text(
                        if (count != null) "$count booking · ketuk untuk pilih tanggal" else "Ketuk untuk pilih tanggal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { vm.shift(1) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Hari berikutnya") }
                TextButton(onClick = { vm.setDate(today) }, enabled = state.date != today) { Text("Hari ini") }
            }
        }

        PullToRefreshBox(isRefreshing = state.refreshing, onRefresh = vm::refresh, modifier = Modifier.fillMaxSize()) {
            val schedule = state.schedule
            when {
                state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                schedule == null -> Column(Modifier.padding(16.dp)) { ErrorCard(state.error ?: "Gagal memuat jadwal.", onRetry = vm::refresh) }
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val isPast = state.date.isBefore(today)
                    schedule.rooms.groupBy { it.floor }.toSortedMap().forEach { (floor, rooms) ->
                        item(key = "floor-$floor") {
                            Text(
                                "LANTAI $floor",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                        }
                        items(rooms, key = { "room-${it.id}" }) { room ->
                            RoomCard(
                                room = room,
                                currentUserId = currentUserId,
                                canBook = !isPast,
                                onOpenBooking = onOpenBooking,
                                onBook = { onBookRoom(room.id, state.date) },
                            )
                        }
                    }
                    item {
                        Text(
                            "Jam operasional ${schedule.openTime}–${schedule.closeTime}. Tarik ke bawah untuk memperbarui.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    if (pickDate) {
        DatePickerModal(selected = state.date, onDismiss = { pickDate = false }, onConfirm = vm::setDate)
    }
}

@Composable
private fun RoomCard(
    room: RoomScheduleDto,
    currentUserId: Long,
    canBook: Boolean,
    onOpenBooking: (Long) -> Unit,
    onBook: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(roomColor(room.color, MaterialTheme.colorScheme.primary)))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(room.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "Lt ${room.floor} · ${room.capacity} orang · " +
                        if (room.bookings.isEmpty()) "kosong sepanjang hari" else "${room.bookings.size} booking",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (canBook) OutlinedButton(onClick = onBook, contentPadding = PaddingValues(horizontal = 14.dp)) { Text("Pesan") }
        }
        if (room.bookings.isNotEmpty()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            room.bookings.forEach { booking ->
                BookingRow(booking, mine = booking.userId == currentUserId, onClick = { onOpenBooking(booking.id) })
            }
        }
    }
}

@Composable
private fun BookingRow(booking: BookingDto, mine: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Box(Modifier.width(4.dp).height(34.dp).clip(RoundedCornerShape(2.dp)).background(bookingTypeColor(booking.type)))
        Spacer(Modifier.width(10.dp))
        Text(
            "${booking.startTime}\n${booking.endTime}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(44.dp),
        )
        Column(Modifier.weight(1f)) {
            Text(
                (if (booking.isRecurring) "↻ " else "") + booking.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                booking.user?.name ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        when {
            booking.isOngoing -> Badge("Berlangsung", Emerald600)
            mine -> Badge("Anda", MaterialTheme.colorScheme.primary)
        }
    }
}
