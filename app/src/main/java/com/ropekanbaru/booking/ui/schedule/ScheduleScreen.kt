package com.ropekanbaru.booking.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ropekanbaru.booking.data.CalendarRange
import com.ropekanbaru.booking.data.ScheduleMode
import com.ropekanbaru.booking.data.remote.BookingDto
import com.ropekanbaru.booking.data.remote.RoomScheduleDto
import com.ropekanbaru.booking.ui.components.Badge
import com.ropekanbaru.booking.ui.components.DatePickerModal
import com.ropekanbaru.booking.ui.components.ErrorCard
import com.ropekanbaru.booking.ui.components.SectionLabel
import com.ropekanbaru.booking.ui.components.bookingTypeColor
import com.ropekanbaru.booking.ui.components.longDate
import com.ropekanbaru.booking.ui.components.monthTitle
import com.ropekanbaru.booking.ui.components.roomColor
import com.ropekanbaru.booking.ui.components.weekTitle
import com.ropekanbaru.booking.ui.components.weekdayDate
import com.ropekanbaru.booking.ui.theme.Emerald600
import java.time.LocalDate

/** Booking dari jadwal tidak memuat relasi ruangan; pasangkan dengan ruangannya. */
internal data class RoomBooking(val room: RoomScheduleDto, val booking: BookingDto)

internal val WEEKDAYS = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleContent(
    vm: ScheduleViewModel,
    currentUserId: Long,
    onOpenBooking: (Long) -> Unit,
    onBookRoom: (roomId: Long?, date: LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = vm.state
    var pickDate by rememberSaveable { mutableStateOf(false) }
    val today = LocalDate.now()
    val rooms = state.schedule?.rooms.orEmpty()

    Column(modifier.fillMaxSize()) {
        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
            Column(Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                // Hari / Minggu / Bulan
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    ScheduleMode.entries.forEachIndexed { i, mode ->
                        SegmentedButton(
                            selected = state.mode == mode,
                            onClick = { vm.setMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(i, ScheduleMode.entries.size),
                            label = { Text(mode.label) },
                        )
                    }
                }

                // Navigasi tanggal
                val (prevLabel, nextLabel) = when (state.mode) {
                    ScheduleMode.Day -> "Hari sebelumnya" to "Hari berikutnya"
                    ScheduleMode.Week -> "Minggu sebelumnya" to "Minggu berikutnya"
                    ScheduleMode.Month -> "Bulan sebelumnya" to "Bulan berikutnya"
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                    IconButton(onClick = { vm.shift(-1) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, prevLabel) }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).clickable { pickDate = true }.padding(vertical = 4.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                when (state.mode) {
                                    ScheduleMode.Day -> longDate(state.date)
                                    ScheduleMode.Week -> state.range.let { weekTitle(it.start, it.endInclusive) }
                                    ScheduleMode.Month -> monthTitle(state.date)
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        // Tampilan bulan juga memuat tanggal bulan sebelum/sesudahnya; hitung bulan ini saja.
                        val count = state.schedule?.let { schedule ->
                            visibleBookings(schedule.rooms, state.mode, state.roomId).count {
                                state.mode != ScheduleMode.Month || it.booking.date.startsWith(state.date.toString().take(7))
                            }
                        }
                        Text(
                            if (count != null) "$count booking · ketuk untuk pilih tanggal" else "Ketuk untuk pilih tanggal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { vm.shift(1) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, nextLabel) }
                    TextButton(onClick = { vm.setDate(today) }, enabled = state.date != today) { Text("Hari ini") }
                }

                // Filter ruangan & sorot booking saya
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                ) {
                    FilterChip(selected = state.highlightMine, onClick = vm::toggleHighlight, label = { Text("Sorot booking saya") })
                    if (state.mode != ScheduleMode.Day && rooms.isNotEmpty()) {
                        val selected = selectedRoomId(state.mode, state.roomId, rooms)
                        if (state.mode == ScheduleMode.Month) {
                            FilterChip(selected = selected == null, onClick = { vm.setRoom(null) }, label = { Text("Semua ruangan") })
                        }
                        rooms.forEach { room ->
                            FilterChip(selected = selected == room.id, onClick = { vm.setRoom(room.id) }, label = { Text(room.name) })
                        }
                    }
                }
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
                    val dim = { b: BookingDto -> state.highlightMine && b.userId != currentUserId }
                    when (state.mode) {
                        ScheduleMode.Day -> dayView(schedule.rooms, state.date, today, currentUserId, dim, onOpenBooking, onBookRoom)
                        ScheduleMode.Week -> weekView(schedule.rooms, state, today, currentUserId, dim, onOpenBooking, onBookRoom)
                        ScheduleMode.Month -> monthView(schedule.rooms, state, today, currentUserId, dim, vm::setDate, onOpenBooking, onBookRoom)
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

/** Tampilan minggu wajib satu ruangan (default ruangan pertama); bulan boleh semua ruangan. */
internal fun selectedRoomId(mode: ScheduleMode, roomId: Long?, rooms: List<RoomScheduleDto>): Long? = when (mode) {
    ScheduleMode.Day -> null
    ScheduleMode.Week -> rooms.firstOrNull { it.id == roomId }?.id ?: rooms.firstOrNull()?.id
    ScheduleMode.Month -> rooms.firstOrNull { it.id == roomId }?.id
}

internal fun visibleBookings(rooms: List<RoomScheduleDto>, mode: ScheduleMode, roomId: Long?): List<RoomBooking> {
    val selected = selectedRoomId(mode, roomId, rooms)
    return rooms.filter { selected == null || it.id == selected }.flatMap { room -> room.bookings.map { RoomBooking(room, it) } }
}

// ---------------------------------------------------------------- Hari

private fun LazyListScope.dayView(
    rooms: List<RoomScheduleDto>,
    date: LocalDate,
    today: LocalDate,
    currentUserId: Long,
    dim: (BookingDto) -> Boolean,
    onOpenBooking: (Long) -> Unit,
    onBookRoom: (Long?, LocalDate) -> Unit,
) {
    rooms.groupBy { it.floor }.toSortedMap().forEach { (floor, floorRooms) ->
        item(key = "floor-$floor") { SectionLabel("LANTAI $floor") }
        items(floorRooms, key = { "room-${it.id}" }) { room ->
            val bookings = room.bookings.filter { it.date == date.toString() }
            ScheduleCard(
                title = room.name,
                subtitle = "Lt ${room.floor} · ${room.capacity} orang · " +
                    if (bookings.isEmpty()) "kosong sepanjang hari" else "${bookings.size} booking",
                dotColor = room.color,
                canBook = !date.isBefore(today),
                onBook = { onBookRoom(room.id, date) },
                hasItems = bookings.isNotEmpty(),
            ) {
                bookings.forEach { BookingRow(it, mine = it.userId == currentUserId, dimmed = dim(it), onClick = { onOpenBooking(it.id) }) }
            }
        }
    }
}

// ---------------------------------------------------------------- Minggu

private fun LazyListScope.weekView(
    rooms: List<RoomScheduleDto>,
    state: ScheduleUiState,
    today: LocalDate,
    currentUserId: Long,
    dim: (BookingDto) -> Boolean,
    onOpenBooking: (Long) -> Unit,
    onBookRoom: (Long?, LocalDate) -> Unit,
) {
    val room = rooms.firstOrNull { it.id == selectedRoomId(ScheduleMode.Week, state.roomId, rooms) } ?: return
    item(key = "week-room") {
        Text(
            "${room.name} · Lt ${room.floor} · ${room.capacity} orang",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
    val start = state.range.start
    items((0L..6L).map { start.plusDays(it) }, key = { "day-$it" }) { day ->
        val bookings = room.bookings.filter { it.date == day.toString() }
        ScheduleCard(
            title = weekdayDate(day),
            subtitle = if (bookings.isEmpty()) "Kosong" else "${bookings.size} booking",
            badge = if (day == today) "Hari ini" else null,
            canBook = !day.isBefore(today),
            onBook = { onBookRoom(room.id, day) },
            bookLabel = "Pesan ${room.name} ${weekdayDate(day)}",
            hasItems = bookings.isNotEmpty(),
        ) {
            bookings.forEach { BookingRow(it, mine = it.userId == currentUserId, dimmed = dim(it), onClick = { onOpenBooking(it.id) }) }
        }
    }
}

// ---------------------------------------------------------------- Bulan

private fun LazyListScope.monthView(
    rooms: List<RoomScheduleDto>,
    state: ScheduleUiState,
    today: LocalDate,
    currentUserId: Long,
    dim: (BookingDto) -> Boolean,
    onSelectDay: (LocalDate) -> Unit,
    onOpenBooking: (Long) -> Unit,
    onBookRoom: (Long?, LocalDate) -> Unit,
) {
    val roomId = selectedRoomId(ScheduleMode.Month, state.roomId, rooms)
    val byDate = visibleBookings(rooms, ScheduleMode.Month, state.roomId).groupBy { it.booking.date }
    item(key = "month-grid") {
        MonthGrid(
            days = CalendarRange.monthDays(state.date),
            month = state.date.monthValue,
            selected = state.date,
            today = today,
            byDate = byDate,
            onSelect = onSelectDay,
        )
    }
    val selected = byDate[state.date.toString()].orEmpty().sortedBy { it.booking.startTime }
    item(key = "month-day") {
        ScheduleCard(
            title = weekdayDate(state.date),
            subtitle = if (selected.isEmpty()) "Tidak ada booking" else "${selected.size} booking",
            badge = if (state.date == today) "Hari ini" else null,
            canBook = !state.date.isBefore(today),
            onBook = { onBookRoom(roomId, state.date) },
            hasItems = selected.isNotEmpty(),
        ) {
            selected.forEach { (room, b) ->
                BookingRow(b, mine = b.userId == currentUserId, dimmed = dim(b), roomName = room.name, onClick = { onOpenBooking(b.id) })
            }
        }
    }
}

@Composable
private fun MonthGrid(
    days: List<LocalDate>,
    month: Int,
    selected: LocalDate,
    today: LocalDate,
    byDate: Map<String, List<RoomBooking>>,
    onSelect: (LocalDate) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(8.dp)) {
            Row {
                WEEKDAYS.forEach {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            days.chunked(7).forEach { week ->
                Row {
                    week.forEach { day ->
                        val bookings = byDate[day.toString()].orEmpty()
                        val isSelected = day == selected
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.9f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                                .clickable { onSelect(day) }
                                .alpha(if (day.monthValue == month) 1f else 0.4f)
                                .semantics { contentDescription = "${weekdayDate(day)}, ${bookings.size} booking" }
                                .padding(top = 4.dp),
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(24.dp)
                                    .then(if (day == today) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier),
                            ) {
                                Text(
                                    "${day.dayOfMonth}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected || day == today) FontWeight.Bold else FontWeight.Normal,
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                bookings.take(3).forEach { (room, _) ->
                                    Box(Modifier.size(5.dp).clip(CircleShape).background(roomColor(room.color, MaterialTheme.colorScheme.primary)))
                                }
                            }
                            if (bookings.size > 3) {
                                Text("+${bookings.size - 3}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- Komponen bersama

@Composable
private fun ScheduleCard(
    title: String,
    subtitle: String,
    canBook: Boolean,
    onBook: () -> Unit,
    dotColor: String? = null,
    badge: String? = null,
    bookLabel: String? = null,
    hasItems: Boolean = true,
    content: @Composable () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)) {
            if (dotColor != null) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(roomColor(dotColor, MaterialTheme.colorScheme.primary)))
                Spacer(Modifier.width(10.dp))
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    badge?.let { Badge(it, MaterialTheme.colorScheme.primary) }
                }
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (canBook) {
                OutlinedButton(
                    onClick = onBook,
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    modifier = if (bookLabel != null) Modifier.semantics { contentDescription = bookLabel } else Modifier,
                ) { Text("Pesan") }
            }
        }
        if (hasItems) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            content()
        }
    }
}

@Composable
private fun BookingRow(booking: BookingDto, mine: Boolean, dimmed: Boolean, onClick: () -> Unit, roomName: String? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (dimmed) 0.35f else 1f)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
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
                listOfNotNull(roomName, booking.user?.name).joinToString(" · "),
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
