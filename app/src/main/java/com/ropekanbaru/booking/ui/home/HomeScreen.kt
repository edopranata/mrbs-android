package com.ropekanbaru.booking.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ropekanbaru.booking.data.RoomStatus
import com.ropekanbaru.booking.data.remote.DashboardAdmin
import com.ropekanbaru.booking.data.remote.UserDto
import com.ropekanbaru.booking.data.roomStatuses
import com.ropekanbaru.booking.ui.components.BookingListRow
import com.ropekanbaru.booking.ui.components.CardHeader
import com.ropekanbaru.booking.ui.components.EmptyState
import com.ropekanbaru.booking.ui.components.ErrorCard
import com.ropekanbaru.booking.ui.components.IconBox
import com.ropekanbaru.booking.ui.components.Pill
import com.ropekanbaru.booking.ui.components.Tw
import com.ropekanbaru.booking.ui.components.WebCard
import com.ropekanbaru.booking.ui.components.longDate
import com.ropekanbaru.booking.ui.components.roomColor
import com.ropekanbaru.booking.ui.icons.Apartment
import com.ropekanbaru.booking.ui.icons.DoorFront
import com.ropekanbaru.booking.ui.icons.EventAvailable
import com.ropekanbaru.booking.ui.icons.Group
import com.ropekanbaru.booking.ui.icons.Schedule
import java.time.LocalDateTime
import java.time.LocalTime

/** Sapaan sesuai jam, seperti versi web ("Selamat pagi, Budi"). */
private fun greeting(now: LocalTime = LocalTime.now()): String = when (now.hour) {
    in 4..10 -> "Selamat pagi"
    in 11..14 -> "Selamat siang"
    in 15..17 -> "Selamat sore"
    else -> "Selamat malam"
}

/** Dashboard, mengikuti tata letak versi web (HP: bertumpuk; tablet lebar: dua kolom). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    user: UserDto,
    vm: HomeViewModel,
    onOpenBooking: (Long) -> Unit,
    onOpenSchedule: () -> Unit,
    onOpenMyBookings: () -> Unit,
    /** null = akun View Only: tanpa tombol pesan & panel Booking Saya. */
    onBookRoom: ((roomId: Long) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val state = vm.state
    PullToRefreshBox(isRefreshing = state.refreshing, onRefresh = vm::refresh, modifier = modifier.fillMaxSize()) {
        BoxWithConstraints {
            val wide = maxWidth >= 600.dp
            // Dua kolom hanya bila area konten cukup lebar (mis. tablet landscape), sama seperti web.
            val roomy = maxWidth >= 840.dp
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(if (wide) 24.dp else 16.dp),
            ) {
                Column {
                    Text(
                        "${greeting()}, ${user.name.substringBefore(' ')}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Tw.Slate900,
                    )
                    Text(longDate(), style = MaterialTheme.typography.bodyMedium, color = Tw.Slate500)
                }
                val dashboard = state.dashboard
                when {
                    state.loading -> Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    dashboard == null -> ErrorCard(state.error ?: "Gagal memuat data.", onRetry = vm::refresh)
                    else -> {
                        state.error?.let { ErrorCard(it, onRetry = vm::refresh) }
                        val stats = dashboard.stats
                        val statItems = listOf(
                            Stat(Icons.Outlined.DoorFront, Tw.Emerald50, Tw.Emerald600, "${stats.roomsInUse} / ${stats.roomsTotal}", "Ruangan dipakai sekarang"),
                            Stat(Icons.Outlined.EventAvailable, Tw.Indigo50, Tw.Indigo600, "${stats.bookingsToday}", "Booking hari ini"),
                            if (user.isViewer) {
                                Stat(Icons.Outlined.Schedule, Tw.Amber50, Tw.Amber600, "${dashboard.ongoing.size}", "Booking sedang berlangsung")
                            } else {
                                Stat(Icons.Outlined.Schedule, Tw.Amber50, Tw.Amber600, "${stats.myUpcoming}", "Booking saya (mendatang)")
                            },
                            dashboard.admin?.let { Stat(Icons.Outlined.Group, Tw.Sky50, Tw.Sky600, "${it.usersActive}", "User aktif") }
                                ?: Stat(Icons.Outlined.Apartment, Tw.Sky50, Tw.Sky600, "${stats.roomsTotal}", "Total ruang rapat"),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            statItems.chunked(if (roomy) 4 else 2).forEach { row ->
                                // Grid seragam: lebar kolom sama, kartu sebaris sama tinggi.
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(IntrinsicSize.Min)) {
                                    row.forEach { StatCard(it, Modifier.weight(1f).fillMaxHeight()) }
                                }
                            }
                        }

                        val roomPanel: @Composable (Modifier) -> Unit = { m ->
                            WebCard(m) {
                                CardHeader("Status Ruangan Saat Ini", "Lihat jadwal", onOpenSchedule)
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    roomStatuses(state.rooms, LocalDateTime.now()).groupBy { it.room.floor }.toSortedMap().forEach { (floor, rooms) ->
                                        Text("LANTAI $floor", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Tw.Slate500, modifier = Modifier.padding(top = 4.dp))
                                        // Dua kolom bila cukup lebar (web: sm:grid-cols-2), satu kolom di HP.
                                        rooms.chunked(if (wide) 2 else 1).forEach { pair ->
                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(IntrinsicSize.Min)) {
                                                pair.forEach { status ->
                                                    Box(Modifier.weight(1f).fillMaxHeight()) { RoomStatusCard(status, onOpenBooking, onBook = onBookRoom?.let { book -> { book(status.room.id) } }) }
                                                }
                                                if (wide && pair.size == 1) Spacer(Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        val minePanel: @Composable (Modifier) -> Unit = { m ->
                            WebCard(m) {
                                CardHeader("Booking Saya Berikutnya", "Semua", onOpenMyBookings)
                                if (dashboard.myUpcoming.isEmpty()) {
                                    EmptyState(Icons.Outlined.DateRange, "Belum ada booking", "Booking ruang rapat Anda akan tampil di sini.")
                                }
                                dashboard.myUpcoming.forEachIndexed { i, b ->
                                    if (i > 0) HorizontalDivider(color = Tw.Slate100)
                                    BookingListRow(b, onClick = { onOpenBooking(b.id) })
                                }
                            }
                        }
                        if (onBookRoom == null) {
                            roomPanel(Modifier.fillMaxWidth())
                        } else if (roomy) {
                            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                                roomPanel(Modifier.weight(2f))
                                minePanel(Modifier.weight(1f))
                            }
                        } else {
                            roomPanel(Modifier.fillMaxWidth())
                            minePanel(Modifier.fillMaxWidth())
                        }
                        dashboard.admin?.let { RoomUsageCard(it) }
                    }
                }
            }
        }
    }
}

private data class Stat(val icon: ImageVector, val background: Color, val tint: Color, val value: String, val label: String)

@Composable
private fun StatCard(stat: Stat, modifier: Modifier = Modifier) {
    WebCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize().padding(14.dp)) {
            IconBox(stat.icon, stat.background, stat.tint)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(stat.value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = Tw.Slate900)
                // Label selalu 2 baris agar semua kartu statistik sama tinggi.
                Text(stat.label, fontSize = 12.sp, lineHeight = 15.sp, color = Tw.Slate500, minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** Kartu status ruangan: hijau bila kosong, merah bila sedang dipakai (sama dengan web). */
@Composable
private fun RoomStatusCard(status: RoomStatus, onOpenBooking: (Long) -> Unit, onBook: (() -> Unit)?) {
    val current = status.current
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (current != null) Tw.Red50 else Tw.Emerald50,
        border = BorderStroke(1.dp, if (current != null) Tw.Red200 else Tw.Emerald200),
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(roomColor(status.room.color, Tw.Indigo600)))
                Spacer(Modifier.width(8.dp))
                Text(status.room.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Tw.Slate900, modifier = Modifier.weight(1f))
                if (current != null) Pill("Dipakai", Tw.Red100, Tw.Red700, dot = Tw.Red500) else Pill("Kosong", Tw.Emerald100, Tw.Emerald700)
            }
            // Struktur tetap (judul + 2 baris) agar kartu "Dipakai" dan "Kosong" sama tinggi.
            val next = status.next?.let { "Berikutnya ${it.startTime}: ${it.title}" } ?: "Tidak ada booking lagi hari ini"
            if (current != null) {
                StatusLine("${current.startTime}–${current.endTime} · ${current.title}", Tw.Slate600, onClick = { onOpenBooking(current.id) })
                StatusLine(next, Tw.Slate500)
            } else {
                StatusLine(next, Tw.Slate500)
                // View Only: baris kosong agar tinggi kartu tetap sama.
                if (onBook != null) StatusLine("+ Pesan ruangan ini", Tw.Indigo600, bold = true, onClick = onBook) else StatusLine("", Tw.Slate500)
            }
        }
    }
}

@Composable
private fun StatusLine(text: String, color: Color, bold: Boolean = false, onClick: (() -> Unit)? = null) {
    Text(
        text,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        fontWeight = if (bold) FontWeight.Medium else FontWeight.Normal,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clip(RoundedCornerShape(4.dp)).clickable(onClick = onClick) else Modifier),
    )
}

@Composable
private fun RoomUsageCard(admin: DashboardAdmin) {
    val max = admin.roomUsage.maxOfOrNull { it.bookingsCount }?.coerceAtLeast(1) ?: 1
    WebCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
            Text("Pemakaian Ruangan Bulan Ini", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Tw.Slate900)
            Text("${admin.bookingsThisMonth} booking · ${admin.cancelledThisMonth} dibatalkan", style = MaterialTheme.typography.bodyMedium, color = Tw.Slate500)
        }
        HorizontalDivider(color = Tw.Slate200)
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            admin.roomUsage.forEach { room ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        room.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Tw.Slate700,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(110.dp),
                    )
                    Text("· Lt ${room.floor}", style = MaterialTheme.typography.bodySmall, color = Tw.Slate400, modifier = Modifier.width(44.dp))
                    Box(Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(50)).background(Tw.Slate100)) {
                        Box(
                            Modifier.fillMaxWidth(room.bookingsCount / max.toFloat()).fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(roomColor(room.color, Tw.Indigo600)),
                        )
                    }
                    Text(
                        "${room.bookingsCount}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Tw.Slate900,
                        modifier = Modifier.width(36.dp).padding(start = 8.dp),
                    )
                }
            }
        }
    }
}
