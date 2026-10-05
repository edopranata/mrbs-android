package com.ropekanbaru.booking.ui.schedule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ropekanbaru.booking.data.ScheduleMode
import com.ropekanbaru.booking.data.remote.BookingDto
import com.ropekanbaru.booking.data.remote.RoomScheduleDto
import com.ropekanbaru.booking.ui.components.DatePickerModal
import com.ropekanbaru.booking.ui.components.ErrorCard
import com.ropekanbaru.booking.ui.components.bookingBlockColors
import com.ropekanbaru.booking.ui.components.longDate
import com.ropekanbaru.booking.ui.components.monthTitle
import com.ropekanbaru.booking.ui.components.weekTitle
import com.ropekanbaru.booking.ui.components.weekdayDate
import java.time.LocalDate
import com.ropekanbaru.booking.ui.components.PrevTodayNext
import com.ropekanbaru.booking.ui.components.Segmented
import com.ropekanbaru.booking.ui.components.SelectField
import com.ropekanbaru.booking.ui.components.Tw
import com.ropekanbaru.booking.ui.icons.CalendarMonth
import java.time.format.DateTimeFormatter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.clickable

private val Slate200 = androidx.compose.ui.graphics.Color(0xFFE2E8F0)

/**
 * Jadwal Ruangan, mengikuti versi web: kalender kecil di kiri (bila cukup lebar), grid waktu ×
 * ruangan/hari (bergeser ke samping di layar sempit), kalender bulan, dan legenda jenis rapat.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ScheduleContent(
    vm: ScheduleViewModel,
    currentUserId: Long,
    slotMinutes: Int,
    onOpenBooking: (Long) -> Unit,
    /** null = akun View Only: slot kosong tidak bisa dipilih untuk booking. */
    onSelectSlot: ((roomId: Long?, date: LocalDate, start: String?, end: String?) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val state = vm.state
    val today = LocalDate.now()
    val rooms = state.schedule?.rooms.orEmpty()
    var pickDate by rememberSaveable { mutableStateOf(false) }
    var calMonth by rememberSaveable { mutableStateOf(state.date.withDayOfMonth(1)) }

    // Kalender kecil mengikuti tanggal terpilih bila tanggal itu di luar dua bulan yang tampil.
    LaunchedEffect(state.date) {
        val first = state.date.withDayOfMonth(1)
        if (first != calMonth && first != calMonth.plusMonths(1)) calMonth = first
    }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val showMiniCalendars = maxWidth >= 900.dp
        Row(Modifier.fillMaxSize().padding(if (maxWidth < 600.dp) 16.dp else 20.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            if (showMiniCalendars) {
                Column(Modifier.width(240.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    listOf(0L, 1L).forEach { offset ->
                        MiniCalendar(
                            month = calMonth.plusMonths(offset),
                            selected = state.date,
                            today = today,
                            highlightWeek = state.mode == ScheduleMode.Week,
                            onSelect = vm::setDate,
                            onPrev = { calMonth = calMonth.minusMonths(1) },
                            onNext = { calMonth = calMonth.plusMonths(1) },
                        )
                    }
                }
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Toolbar (seperti web: ‹ Hari ini ›, judul, tanggal, ruangan, Hari/Minggu/Bulan)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    itemVerticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val (prevLabel, nextLabel) = when (state.mode) {
                        ScheduleMode.Day -> "Hari sebelumnya" to "Hari berikutnya"
                        ScheduleMode.Week -> "Minggu sebelumnya" to "Minggu berikutnya"
                        ScheduleMode.Month -> "Bulan sebelumnya" to "Bulan berikutnya"
                    }
                    PrevTodayNext(onPrev = { vm.shift(-1) }, onToday = { vm.setDate(today) }, onNext = { vm.shift(1) }, prevLabel = prevLabel, nextLabel = nextLabel)
                    Text(
                        when (state.mode) {
                            ScheduleMode.Day -> longDate(state.date)
                            ScheduleMode.Week -> state.range.let { weekTitle(it.start, it.endInclusive) }
                            ScheduleMode.Month -> monthTitle(state.date)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Tw.Slate900,
                    )
                    if (!showMiniCalendars) {
                        SelectField(
                            value = state.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            options = emptyList<Unit>(),
                            label = { "" },
                            onSelect = {},
                            leadingIcon = Icons.Outlined.CalendarMonth,
                            modifier = Modifier.width(160.dp).clickable { pickDate = true }.semantics { contentDescription = "Pilih tanggal" },
                        )
                    }
                    if (state.mode != ScheduleMode.Day && rooms.isNotEmpty()) {
                        RoomPicker(rooms, selectedRoomId(state.mode, state.roomId, rooms), allowAll = state.mode == ScheduleMode.Month, onSelect = vm::setRoom)
                    }
                    Segmented(
                        options = ScheduleMode.entries.map { it.label },
                        selected = state.mode.ordinal,
                        onSelect = { vm.setMode(ScheduleMode.entries[it]) },
                        modifier = Modifier.width(260.dp),
                    )
                }

                // Grid / kalender
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                ) {
                    val schedule = state.schedule
                    Box {
                        when {
                            schedule == null && state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                            schedule == null -> Box(Modifier.padding(16.dp)) { ErrorCard(state.error ?: "Gagal memuat jadwal.", onRetry = vm::refresh) }
                            state.mode == ScheduleMode.Month -> MonthCalendar(
                                date = state.date,
                                today = today,
                                byDate = visibleBookings(schedule.rooms, state.mode, state.roomId).groupBy { it.booking.date },
                                currentUserId = currentUserId,
                                highlightMine = state.highlightMine,
                                onPickDate = { day ->
                                    vm.setDate(day)
                                    vm.setMode(ScheduleMode.Day)
                                },
                                onOpenBooking = onOpenBooking,
                            )
                            schedule.rooms.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Tidak ada ruangan aktif") }
                            else -> TimeGrid(
                                columns = gridColumns(schedule.rooms, state, today),
                                openTime = schedule.openTime,
                                closeTime = schedule.closeTime,
                                slotMinutes = slotMinutes,
                                currentUserId = currentUserId,
                                highlightMine = state.highlightMine,
                                onOpenBooking = onOpenBooking,
                                onSelect = onSelectSlot?.let { pick -> { col, start, end -> pick(col.roomId, col.date, start, end) } },
                            )
                        }
                        if (state.refreshing || (state.loading && schedule != null)) {
                            LinearProgressIndicator(Modifier.fillMaxWidth().height(2.dp))
                        }
                    }
                }

                // Legenda
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    itemVerticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    listOf("internal" to "Internal", "external" to "Eksternal").forEach { (type, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(width = 28.dp, height = 14.dp).background(bookingBlockColors(type).background, RoundedCornerShape(2.dp)))
                            Spacer(Modifier.width(6.dp))
                            Text(label, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.toggleable(value = state.highlightMine, role = Role.Checkbox, onValueChange = { vm.toggleHighlight() }),
                    ) {
                        Checkbox(checked = state.highlightMine, onCheckedChange = null)
                        Text("Sorot booking saya", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        if (state.mode == ScheduleMode.Month) "Ketuk tanggal untuk melihat jadwal harian."
                        else "Ketuk slot kosong, atau tekan lama lalu seret beberapa slot, untuk membuat booking.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    if (pickDate) {
        DatePickerModal(selected = state.date, onDismiss = { pickDate = false }, onConfirm = vm::setDate)
    }
}

@Composable
private fun RoomPicker(rooms: List<RoomScheduleDto>, selected: Long?, allowAll: Boolean, onSelect: (Long?) -> Unit) {
    val options: List<RoomScheduleDto?> = (if (allowAll) listOf(null) else emptyList<RoomScheduleDto?>()) + rooms
    val label = { r: RoomScheduleDto? -> r?.let { "${it.name} [Lt ${it.floor}]" } ?: "Semua ruangan" }
    SelectField(
        value = label(rooms.firstOrNull { it.id == selected }),
        options = options,
        label = label,
        onSelect = { onSelect(it?.id) },
        modifier = Modifier.widthIn(min = 180.dp, max = 260.dp),
    )
}

/** Kolom grid: tampilan hari = semua ruangan; tampilan minggu = 7 hari untuk satu ruangan. */
private fun gridColumns(rooms: List<RoomScheduleDto>, state: ScheduleUiState, today: LocalDate): List<GridColumn> =
    if (state.mode == ScheduleMode.Week) {
        val room = rooms.firstOrNull { it.id == selectedRoomId(ScheduleMode.Week, state.roomId, rooms) }
        if (room == null) emptyList() else (0L..6L).map { state.range.start.plusDays(it) }.map { day ->
            GridColumn(
                key = day.toString(),
                label = weekdayDate(day).substringBefore(","),
                sublabel = day.dayOfMonth.toString() + " " + monthTitle(day).take(3),
                highlight = day == today,
                date = day,
                roomId = room.id,
                bookings = room.bookings.filter { it.date == day.toString() },
            )
        }
    } else {
        rooms.map { room ->
            GridColumn(
                key = "room-${room.id}",
                label = room.name,
                sublabel = "[Lt ${room.floor}] (${room.capacity})",
                highlight = false,
                date = state.date,
                roomId = room.id,
                bookings = room.bookings.filter { it.date == state.date.toString() },
            )
        }
    }

/** Booking dari jadwal tidak memuat relasi ruangan; pasangkan dengan ruangannya. */
internal data class RoomBooking(val room: RoomScheduleDto, val booking: BookingDto)

internal val WEEKDAYS = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")

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
