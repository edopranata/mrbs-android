package com.ropekanbaru.booking.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ropekanbaru.booking.data.RoomStatus
import com.ropekanbaru.booking.data.remote.DashboardAdmin
import com.ropekanbaru.booking.data.remote.UserDto
import com.ropekanbaru.booking.data.roomStatuses
import com.ropekanbaru.booking.ui.components.Badge
import com.ropekanbaru.booking.ui.components.BookingCard
import com.ropekanbaru.booking.ui.components.ErrorCard
import com.ropekanbaru.booking.ui.components.SectionLabel
import com.ropekanbaru.booking.ui.components.isWideLayout
import com.ropekanbaru.booking.ui.components.longDate
import com.ropekanbaru.booking.ui.components.roomColor
import com.ropekanbaru.booking.ui.theme.Emerald600
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    user: UserDto,
    vm: HomeViewModel,
    onOpenBooking: (Long) -> Unit,
    onOpenSchedule: () -> Unit,
    onOpenMyBookings: () -> Unit,
    onBookRoom: (roomId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = vm.state

    if (isWideLayout()) {
        HomeWide(user, vm, onOpenBooking, onOpenSchedule, onOpenMyBookings, onBookRoom, modifier)
        return
    }

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

                    // Booking saya berikutnya
                    item { SectionHeader("Booking Saya Berikutnya", "Semua", onOpenMyBookings) }
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

                    // Status ruangan saat ini
                    if (state.rooms.isNotEmpty()) {
                        item { SectionHeader("Status Ruangan Saat Ini", "Lihat jadwal", onOpenSchedule) }
                        roomStatuses(state.rooms, LocalDateTime.now()).groupBy { it.room.floor }.toSortedMap().forEach { (floor, rooms) ->
                            item(key = "floor-$floor") { SectionLabel("LANTAI $floor") }
                            items(rooms, key = { "status-${it.room.id}" }) { status ->
                                RoomStatusCard(status, onOpenBooking = onOpenBooking, onBook = { onBookRoom(status.room.id) })
                            }
                        }
                    }

                    dashboard.admin?.let { admin -> item { RoomUsageCard(admin) } }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, action: String, onAction: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        TextButton(onClick = onAction) { Text(action) }
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
private fun RoomStatusCard(status: RoomStatus, onOpenBooking: (Long) -> Unit, onBook: () -> Unit) {
    val current = status.current
    val busyColor = MaterialTheme.colorScheme.error
    Card(
        onClick = { if (current != null) onOpenBooking(current.id) else onBook() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(roomColor(status.room.color, MaterialTheme.colorScheme.primary)))
                Spacer(Modifier.width(8.dp))
                Text(status.room.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                if (current != null) Badge("Dipakai", busyColor) else Badge("Kosong", Emerald600)
            }
            if (current != null) {
                Text(
                    "${current.startTime}–${current.endTime} · ${current.title}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                status.next?.let { "Berikutnya ${it.startTime}: ${it.title}" } ?: "Tidak ada booking lagi hari ini",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (current == null) {
                Text("+ Pesan ruangan ini", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun RoomUsageCard(admin: DashboardAdmin) {
    val max = admin.roomUsage.maxOfOrNull { it.bookingsCount }?.coerceAtLeast(1) ?: 1
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Pemakaian Ruangan Bulan Ini", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "${admin.bookingsThisMonth} booking · ${admin.cancelledThisMonth} dibatalkan · ${admin.usersActive}/${admin.usersTotal} user aktif",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            admin.roomUsage.forEach { room ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row {
                        Text("${room.name} · Lt ${room.floor}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text("${room.bookingsCount}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                    Box(
                        Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Box(
                            Modifier.fillMaxWidth(room.bookingsCount / max.toFloat()).fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(roomColor(room.color, MaterialTheme.colorScheme.primary)),
                        )
                    }
                }
            }
        }
    }
}

/** Dashboard tablet, mengikuti tata letak versi web. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeWide(
    user: UserDto,
    vm: HomeViewModel,
    onOpenBooking: (Long) -> Unit,
    onOpenSchedule: () -> Unit,
    onOpenMyBookings: () -> Unit,
    onBookRoom: (roomId: Long) -> Unit,
    modifier: Modifier,
) {
    val state = vm.state
    PullToRefreshBox(isRefreshing = state.refreshing, onRefresh = vm::refresh, modifier = modifier.fillMaxSize()) {
      BoxWithConstraints {
        // Seperti versi web: dua kolom hanya bila area konten cukup lebar (mis. tablet landscape).
        val roomy = maxWidth >= 840.dp
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        ) {
            Column {
                Text("Halo, ${user.name}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Text(longDate(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val dashboard = state.dashboard
            when {
                state.loading -> Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                dashboard == null -> ErrorCard(state.error ?: "Gagal memuat data.", onRetry = vm::refresh)
                else -> {
                    state.error?.let { ErrorCard(it, onRetry = vm::refresh) }
                    val stats = dashboard.stats
                    val statItems = listOf(
                        "${stats.roomsInUse} / ${stats.roomsTotal}" to "Ruangan dipakai sekarang",
                        "${stats.bookingsToday}" to "Booking hari ini",
                        "${stats.myUpcoming}" to "Booking saya (mendatang)",
                        dashboard.admin?.let { "${it.usersActive}" to "User aktif" } ?: ("${stats.roomsTotal}" to "Total ruang rapat"),
                    )
                    statItems.chunked(if (roomy) 4 else 2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { (value, label) -> StatCard(value, label, Modifier.weight(1f)) }
                        }
                    }
                    val panels: @Composable (Modifier, Modifier) -> Unit = { statusModifier, mineModifier ->
                        // Status ruangan (2 kolom per lantai)
                        Panel("Status Ruangan Saat Ini", "Lihat jadwal →", onOpenSchedule, statusModifier) {
                            roomStatuses(state.rooms, LocalDateTime.now()).groupBy { it.room.floor }.toSortedMap().forEach { (floor, rooms) ->
                                SectionLabel("LANTAI $floor")
                                rooms.chunked(2).forEach { pair ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        pair.forEach { status ->
                                            Box(Modifier.weight(1f)) {
                                                RoomStatusCard(status, onOpenBooking = onOpenBooking, onBook = { onBookRoom(status.room.id) })
                                            }
                                        }
                                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                        // Booking saya berikutnya
                        Panel("Booking Saya Berikutnya", "Semua →", onOpenMyBookings, mineModifier) {
                            if (dashboard.myUpcoming.isEmpty()) {
                                Text("Belum ada booking. Booking ruang rapat Anda akan tampil di sini.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            dashboard.myUpcoming.forEach { BookingCard(it, onClick = { onOpenBooking(it.id) }) }
                        }
                    }
                    if (roomy) {
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) { panels(Modifier.weight(2f), Modifier.weight(1f)) }
                    } else {
                        panels(Modifier.fillMaxWidth(), Modifier.fillMaxWidth())
                    }
                    dashboard.admin?.let { RoomUsageCard(it) }
                }
            }
        }
      }
    }
}

@Composable
private fun Panel(title: String, action: String, onAction: () -> Unit, modifier: Modifier, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            TextButton(onClick = onAction) { Text(action) }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { content() }
    }
}
