package com.ropekanbaru.booking.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ropekanbaru.booking.data.TimeSlots
import com.ropekanbaru.booking.data.TodayProgress
import com.ropekanbaru.booking.data.remote.BookingDto
import com.ropekanbaru.booking.data.remote.MrbsApi
import com.ropekanbaru.booking.data.remote.RoomDto
import com.ropekanbaru.booking.ui.components.CountPill
import com.ropekanbaru.booking.ui.components.EmptyState
import com.ropekanbaru.booking.ui.components.ErrorCard
import com.ropekanbaru.booking.ui.components.IconBox
import com.ropekanbaru.booking.ui.components.LabeledTextField
import com.ropekanbaru.booking.ui.components.Pill
import com.ropekanbaru.booking.ui.components.SelectField
import com.ropekanbaru.booking.ui.components.Tw
import com.ropekanbaru.booking.ui.components.WebCard
import com.ropekanbaru.booking.ui.components.bookingBlockColors
import com.ropekanbaru.booking.ui.components.formatDuration
import com.ropekanbaru.booking.ui.components.longDate
import com.ropekanbaru.booking.ui.components.rememberLoader
import com.ropekanbaru.booking.ui.components.roomColor
import com.ropekanbaru.booking.ui.icons.DoorFront
import com.ropekanbaru.booking.ui.icons.Groups
import com.ropekanbaru.booking.ui.icons.Inbox
import com.ropekanbaru.booking.ui.icons.PersonOutline
import com.ropekanbaru.booking.ui.icons.PlayCircle
import com.ropekanbaru.booking.ui.icons.Schedule
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private fun nowMinutes(): Int = LocalTime.now().let { it.hour * 60 + it.minute }

/**
 * Pantauan admin seperti menu Semua Booking di web: booking hari ini yang sedang berlangsung dan
 * yang akan datang. Diperbarui otomatis tiap menit; yang selesai langsung hilang dari daftar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayBookingsContent(
    api: MrbsApi,
    bookingChanges: Int,
    currentUserId: Long,
    onOpenBooking: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = rememberLoader { api.todayBookings() }
    val rooms = rememberLoader { api.rooms().data.filter { it.isActive } }
    var search by rememberSaveable { mutableStateOf("") }
    var roomId by rememberSaveable { mutableStateOf<Long?>(null) }
    var now by remember { mutableStateOf(nowMinutes()) }
    var updatedAt by remember { mutableStateOf(LocalTime.now()) }

    LaunchedEffect(today.data) { if (today.data != null) updatedAt = LocalTime.now() }
    LaunchedEffect(bookingChanges) { if (today.data != null) today.load() }
    LaunchedEffect(Unit) {
        var tick = 0
        while (true) {
            delay(15_000)
            now = nowMinutes()
            if (++tick % 4 == 0) today.load() // tiap 60 detik
        }
    }

    PullToRefreshBox(isRefreshing = today.refreshing, onRefresh = { today.load(refresh = true) }, modifier = modifier.fillMaxSize()) {
        BoxWithConstraints {
            val wide = maxWidth >= 600.dp
            val extraWide = maxWidth >= 1100.dp
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(if (wide) 24.dp else 16.dp),
            ) {
                Column {
                    Text("Booking Hari Ini", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = Tw.Slate900)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(longDate(), style = MaterialTheme.typography.bodyMedium, color = Tw.Slate500)
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.size(8.dp).clip(CircleShape).background(Tw.Emerald600))
                        Spacer(Modifier.width(4.dp))
                        Text("Diperbarui otomatis · ${updatedAt.format(DateTimeFormatter.ofPattern("HH.mm"))}", fontSize = 12.sp, color = Tw.Emerald700)
                    }
                }
                LabeledTextField(
                    label = "Cari",
                    value = search,
                    onValueChange = { search = it },
                    placeholder = "Cari rapat, pemesan, atau ruangan…",
                    trailingIcon = { Icon(Icons.Outlined.Search, null, tint = Tw.Slate400) },
                )
                val roomOptions: List<RoomDto?> = listOf<RoomDto?>(null) + rooms.data.orEmpty()
                SelectField(
                    value = rooms.data?.firstOrNull { it.id == roomId }?.let { "${it.name} (Lt ${it.floor})" } ?: "Semua ruangan",
                    options = roomOptions,
                    label = { it?.let { r -> "${r.name} (Lt ${r.floor})" } ?: "Semua ruangan" },
                    onSelect = { roomId = it?.id },
                    modifier = Modifier.fillMaxWidth(),
                )

                val data = today.data
                when {
                    today.loading -> Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    data == null -> ErrorCard(today.error ?: "Gagal memuat booking.", onRetry = { today.load() })
                    else -> {
                        today.error?.let { ErrorCard(it) }
                        val term = search.trim()
                        // Disaring ulang setiap "tick" agar yang baru selesai langsung hilang dan yang baru
                        // mulai pindah ke "Sedang Berlangsung" tanpa menunggu muat ulang.
                        val visible = (data.ongoing + data.upcoming).filter { b ->
                            TimeSlots.toMinutes(b.endTime) > now &&
                                (roomId == null || b.room?.id == roomId) &&
                                (term.isEmpty() || listOfNotNull(b.title, b.user?.name, b.room?.name, b.user?.department).any { it.contains(term, ignoreCase = true) })
                        }
                        val ongoing = visible.filter { TimeSlots.toMinutes(it.startTime) <= now }
                        val upcoming = visible.filter { TimeSlots.toMinutes(it.startTime) > now }
                        val roomsInUse = ongoing.mapNotNull { it.room?.id }.distinct().size

                        val stats = listOf(
                            Triple(Icons.Outlined.PlayCircle, "${ongoing.size}", "Sedang berlangsung") to (Tw.Emerald50 to Tw.Emerald600),
                            Triple(Icons.Outlined.Schedule, "${upcoming.size}", "Akan datang hari ini") to (Tw.Indigo50 to Tw.Indigo600),
                            Triple(Icons.Outlined.DoorFront, "$roomsInUse / ${rooms.data?.size ?: "-"}", "Ruangan terpakai sekarang") to (Tw.Amber50 to Tw.Amber600),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            stats.chunked(if (wide) 3 else 1).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    row.forEach { (info, colors) ->
                                        WebCard(Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                                                IconBox(info.first, colors.first, colors.second)
                                                Spacer(Modifier.width(12.dp))
                                                Column {
                                                    Text(info.second, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = Tw.Slate900)
                                                    Text(info.third, fontSize = 12.sp, color = Tw.Slate500)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Sedang berlangsung
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(Tw.Red500))
                            Spacer(Modifier.width(8.dp))
                            Text("Sedang Berlangsung", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Tw.Slate900)
                            Spacer(Modifier.width(8.dp))
                            CountPill(ongoing.size)
                        }
                        if (ongoing.isEmpty()) {
                            WebCard(Modifier.fillMaxWidth()) {
                                EmptyState(Icons.Outlined.Inbox, "Tidak ada rapat yang sedang berlangsung", "Semua ruangan sedang kosong saat ini.")
                            }
                        } else {
                            val columns = when {
                                extraWide -> 3
                                wide -> 2
                                else -> 1
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                ongoing.chunked(columns).forEach { row ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        row.forEach { OngoingCard(it, now, onClick = { onOpenBooking(it.id) }, modifier = Modifier.weight(1f)) }
                                        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                                    }
                                }
                            }
                        }

                        // Akan datang
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Schedule, null, tint = Tw.Indigo600, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Akan Datang Hari Ini", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Tw.Slate900)
                            Spacer(Modifier.width(8.dp))
                            CountPill(upcoming.size)
                        }
                        WebCard(Modifier.fillMaxWidth()) {
                            if (upcoming.isEmpty()) EmptyState(Icons.Outlined.Inbox, "Tidak ada booking lagi hari ini")
                            upcoming.forEachIndexed { i, b ->
                                if (i > 0) HorizontalDivider(color = Tw.Slate100)
                                UpcomingRow(b, now, mine = b.userId == currentUserId, onClick = { onOpenBooking(b.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OngoingCard(b: BookingDto, now: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val start = TimeSlots.toMinutes(b.startTime)
    val end = TimeSlots.toMinutes(b.endTime)
    val type = bookingBlockColors(b.type)
    WebCard(modifier, onClick = onClick) {
        Box(Modifier.fillMaxWidth().height(6.dp).background(roomColor(b.room?.color, Tw.Indigo600)))
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(b.room?.name.orEmpty(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Tw.Slate700, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                Spacer(Modifier.width(6.dp))
                b.room?.let { Pill("Lt ${it.floor}", Tw.Slate100, Tw.Slate600) }
                Spacer(Modifier.weight(1f))
                Pill(if (b.type == "external") "Eksternal" else "Internal", type.background, type.content)
            }
            Text((if (b.isRecurring) "↻ " else "") + b.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = Tw.Slate900, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row {
                Icon(Icons.Outlined.Schedule, null, tint = Tw.Slate500, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("${b.startTime}–${b.endTime}", fontSize = 12.sp, color = Tw.Slate500, modifier = Modifier.weight(1f))
                Text("Sisa ${formatDuration(TodayProgress.remaining(end, now))}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Tw.Emerald700)
            }
            Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)).background(Tw.Slate100)) {
                Box(Modifier.fillMaxWidth(TodayProgress.percent(start, end, now) / 100f).fillMaxHeight().clip(RoundedCornerShape(50)).background(Tw.Emerald600))
            }
            HorizontalDivider(color = Tw.Slate100)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.PersonOutline, null, tint = Tw.Slate500, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    (b.user?.name ?: "-") + (b.user?.department?.let { " · $it" } ?: ""),
                    fontSize = 12.sp,
                    color = Tw.Slate500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(Icons.Outlined.Groups, null, tint = Tw.Slate500, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("${b.participants}", fontSize = 12.sp, color = Tw.Slate500)
            }
        }
    }
}

@Composable
private fun UpcomingRow(b: BookingDto, now: Int, mine: Boolean, onClick: () -> Unit) {
    val start = TimeSlots.toMinutes(b.startTime)
    val minutes = TodayProgress.startsIn(start, now)
    val type = bookingBlockColors(b.type)
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp)) {
        Column(Modifier.width(76.dp)) {
            Text(b.startTime, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = Tw.Slate900)
            Text(
                if (minutes < 60) "dalam $minutes menit" else "dalam ${formatDuration(minutes)}",
                fontSize = 12.sp,
                fontWeight = if (TodayProgress.startsSoon(start, now)) FontWeight.Medium else FontWeight.Normal,
                color = if (TodayProgress.startsSoon(start, now)) Tw.Amber600 else Tw.Slate500,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text((if (b.isRecurring) "↻ " else "") + b.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Tw.Slate900, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${b.startTime}–${b.endTime} · ", fontSize = 12.sp, color = Tw.Slate500)
                Pill(if (b.type == "external") "Eksternal" else "Internal", type.background, type.content)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(roomColor(b.room?.color, Tw.Indigo600)))
                Spacer(Modifier.width(6.dp))
                Text(b.room?.name.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = Tw.Slate700, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                Spacer(Modifier.width(6.dp))
                b.room?.let { Pill("Lt ${it.floor}", Tw.Slate100, Tw.Slate600) }
            }
            Text(
                (b.user?.name ?: "-") + (b.user?.department?.let { " · $it" } ?: "") + if (mine) " (Anda)" else "",
                style = MaterialTheme.typography.bodySmall,
                color = Tw.Slate600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
