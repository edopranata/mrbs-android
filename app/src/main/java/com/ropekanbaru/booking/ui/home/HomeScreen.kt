package com.ropekanbaru.booking.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ropekanbaru.booking.data.remote.BookingDto
import com.ropekanbaru.booking.data.remote.UserDto
import com.ropekanbaru.booking.ui.components.Badge
import com.ropekanbaru.booking.ui.components.ErrorCard
import com.ropekanbaru.booking.ui.components.friendlyDate
import com.ropekanbaru.booking.ui.components.longDate
import com.ropekanbaru.booking.ui.components.roomColor
import com.ropekanbaru.booking.ui.theme.Emerald600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    user: UserDto,
    vm: HomeViewModel,
    onOpenBooking: (Long) -> Unit,
    onOpenSchedule: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = vm.state

    PullToRefreshBox(isRefreshing = state.refreshing, onRefresh = vm::refresh, modifier = modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                Column {
                    Text("Halo, ${user.name}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        listOfNotNull(user.roleLabel.ifBlank { null }, user.department).joinToString(" · ") +
                            " — ${longDate()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            when {
                state.loading -> item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                state.dashboard == null -> item {
                    ErrorCard(state.error ?: "Gagal memuat data.", onRetry = vm::refresh)
                }

                else -> {
                    val dashboard = state.dashboard
                    val stats = dashboard.stats
                    state.error?.let { item { ErrorCard(it, onRetry = vm::refresh) } }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatCard("${stats.roomsInUse} / ${stats.roomsTotal}", "Ruangan dipakai", Modifier.weight(1f))
                            StatCard("${stats.bookingsToday}", "Booking hari ini", Modifier.weight(1f))
                            StatCard("${stats.myUpcoming}", "Booking saya", Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                            Text(
                                "Booking Saya Berikutnya",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(onClick = onOpenSchedule) { Text("Lihat jadwal") }
                        }
                    }
                    if (dashboard.myUpcoming.isEmpty()) {
                        item {
                            Text(
                                "Belum ada booking mendatang. Ketuk \"Buat Booking\" untuk memesan ruangan.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }
                    }
                    items(dashboard.myUpcoming, key = { it.id }) { BookingCard(it, onClick = { onOpenBooking(it.id) }) }
                }
            }
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BookingCard(booking: BookingDto, onClick: () -> Unit) {
    val accent = roomColor(booking.room?.color, MaterialTheme.colorScheme.primary)
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.height(androidx.compose.foundation.layout.IntrinsicSize.Min)) {
            Box(Modifier.width(5.dp).fillMaxHeight().background(accent))
            Column(Modifier.padding(12.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        (if (booking.isRecurring) "↻ " else "") + booking.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (booking.isOngoing) Badge("Berlangsung", Emerald600)
                }
                Text(
                    "${friendlyDate(booking.date)}, ${booking.startTime}–${booking.endTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                booking.room?.let {
                    Text(
                        "${it.name} · Lt ${it.floor}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
