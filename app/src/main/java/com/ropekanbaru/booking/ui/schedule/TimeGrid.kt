package com.ropekanbaru.booking.ui.schedule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ropekanbaru.booking.data.CalendarRange
import com.ropekanbaru.booking.data.GridSlots
import com.ropekanbaru.booking.data.TimeSlots
import com.ropekanbaru.booking.data.remote.BookingDto
import com.ropekanbaru.booking.ui.components.bookingBlockColors
import com.ropekanbaru.booking.ui.components.monthTitle
import com.ropekanbaru.booking.ui.components.weekdayDate
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime

private val ROW_H = 28.dp
private val TIME_COL = 64.dp
private val MIN_COL = 112.dp
private val Slate50 = Color(0xFFF8FAFC)
private val Slate100 = Color(0xFFF1F5F9)
private val Slate200 = Color(0xFFE2E8F0)
private val Indigo50 = Color(0xFFEEF2FF)
private val Indigo200 = Color(0xFFC7D2FE)
private val Indigo500 = Color(0xFF6366F1)
private val Red500 = Color(0xFFEF4444)

/** Satu kolom grid: satu ruangan (tampilan hari) atau satu hari (tampilan minggu). */
data class GridColumn(
    val key: String,
    val label: String,
    val sublabel: String?,
    val highlight: Boolean,
    val date: LocalDate,
    val roomId: Long,
    val bookings: List<BookingDto>,
)

private data class Selection(val key: String, val anchor: Int, val current: Int) {
    val from get() = minOf(anchor, current)
    val to get() = maxOf(anchor, current)
}

/**
 * Grid jadwal vertikal seperti versi web: baris = slot waktu, kolom = ruangan / hari.
 * Ketuk slot kosong untuk booking 1 slot (durasi bawaan), atau tekan lama lalu seret untuk memilih rentang.
 */
@Composable
fun TimeGrid(
    columns: List<GridColumn>,
    openTime: String,
    closeTime: String,
    slotMinutes: Int,
    currentUserId: Long,
    highlightMine: Boolean,
    onOpenBooking: (Long) -> Unit,
    onSelect: (column: GridColumn, start: String, end: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val grid = remember(openTime, closeTime, slotMinutes) {
        GridSlots(TimeSlots.toMinutes(openTime), TimeSlots.toMinutes(closeTime), slotMinutes)
    }
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            now = LocalDateTime.now()
        }
    }
    var selection by remember { mutableStateOf<Selection?>(null) }
    val select by rememberUpdatedState(onSelect)
    val rowPx = with(LocalDensity.current) { ROW_H.toPx() }
    val bodyHeight = ROW_H * grid.starts.size

    BoxWithConstraints(modifier.fillMaxSize()) {
        val colWidth = maxOf(MIN_COL, (maxWidth - TIME_COL) / columns.size.coerceAtLeast(1))
        val vertical = rememberScrollState()
        // Kolom "Waktu" tetap terlihat di kiri saat grid digeser ke samping (seperti versi web).
        // Header ruangan/hari & isi grid memakai ScrollState yang sama agar selalu sejajar; header
        // hanya mengikuti (tidak bisa digeser sendiri).
        val horizontal = rememberScrollState()
        Column(Modifier.fillMaxSize()) {
            // Header
            Row(Modifier.height(56.dp)) {
                Box(Modifier.width(TIME_COL).fillMaxHeight().background(Color.White).padding(8.dp), contentAlignment = Alignment.BottomStart) {
                    Text("Waktu", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }
                Box(Modifier.width(1.dp).fillMaxHeight().background(Slate200))
                Row(Modifier.weight(1f).horizontalScroll(horizontal, enabled = false)) {
                    columns.forEach { col ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .width(colWidth)
                                .fillMaxHeight()
                                .background(if (col.highlight) Indigo50 else MaterialTheme.colorScheme.surface)
                                .border(BorderStroke(0.5.dp, Slate200))
                                .padding(horizontal = 4.dp),
                        ) {
                            Text(col.label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            col.sublabel?.let { Text(it, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1) }
                        }
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFCBD5E1))

            // Isi: kolom waktu tetap, kolom ruangan/hari bisa digeser ke samping
            Row(Modifier.weight(1f).verticalScroll(vertical)) {
                Column(Modifier.width(TIME_COL)) {
                    grid.starts.forEachIndexed { i, m ->
                        Text(
                            TimeSlots.format(m),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.height(ROW_H).fillMaxWidth().background(if (i % 2 == 0) Slate50 else Color.White).padding(start = 8.dp, top = 5.dp),
                        )
                    }
                }
                Box(Modifier.width(1.dp).height(bodyHeight).background(Slate200))
                Row(Modifier.weight(1f).horizontalScroll(horizontal)) {
                    columns.forEach { col ->
                        val free = { m: Int -> grid.isFree(col.date, col.bookings, m, now) }
                        Box(
                            Modifier
                                .width(colWidth)
                                .height(bodyHeight)
                                .border(BorderStroke(0.5.dp, Slate200))
                                .pointerInput(col, grid, now) {
                                    detectTapGestures { offset ->
                                        val m = grid.slotAt(offset.y / rowPx)
                                        if (free(m)) select(col, TimeSlots.format(m), null)
                                    }
                                }
                                .pointerInput(col, grid, now) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { offset ->
                                            val m = grid.slotAt(offset.y / rowPx)
                                            selection = if (free(m)) Selection(col.key, m, m) else null
                                        },
                                        onDrag = { change, _ ->
                                            val sel = selection ?: return@detectDragGesturesAfterLongPress
                                            change.consume()
                                            selection = sel.copy(current = grid.extend(sel.anchor, grid.slotAt(change.position.y / rowPx), free))
                                        },
                                        onDragEnd = {
                                            selection?.let { sel ->
                                                select(
                                                    col,
                                                    TimeSlots.format(sel.from),
                                                    if (sel.to > sel.from) TimeSlots.format(sel.to + grid.slot) else null,
                                                )
                                            }
                                            selection = null
                                        },
                                        onDragCancel = { selection = null },
                                    )
                                },
                        ) {
                            // Latar slot: abu-abu = sudah lewat, selang-seling = bisa dipesan.
                            Column {
                                grid.starts.forEachIndexed { i, m ->
                                    Box(
                                        Modifier
                                            .height(ROW_H)
                                            .fillMaxWidth()
                                            .background(
                                                when {
                                                    grid.isPast(col.date, m, now) -> Slate100
                                                    i % 2 == 0 -> Slate50
                                                    else -> Color.White
                                                },
                                            ),
                                    )
                                }
                            }

                            // Pilihan yang sedang diseret
                            selection?.takeIf { it.key == col.key }?.let { sel ->
                                Box(
                                    Modifier
                                        .offset(y = ROW_H * ((sel.from - grid.open) / grid.slot))
                                        .height(ROW_H * ((sel.to - sel.from) / grid.slot + 1))
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Indigo200.copy(alpha = 0.6f))
                                        .border(2.dp, Indigo500, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp),
                                ) {
                                    Text(
                                        "${TimeSlots.format(sel.from)}–${TimeSlots.format(sel.to + grid.slot)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF3730A3),
                                    )
                                }
                            }

                            // Blok booking
                            col.bookings.filter { !it.isCancelled }.forEach { b ->
                                val start = TimeSlots.toMinutes(b.startTime).coerceAtLeast(grid.open)
                                val end = TimeSlots.toMinutes(b.endTime).coerceAtMost(grid.close)
                                if (end <= start) return@forEach
                                val rows = (end - start) / grid.slot.toFloat()
                                val colors = bookingBlockColors(b.type)
                                val mine = b.userId == currentUserId
                                Column(
                                    Modifier
                                        .offset(y = ROW_H * ((start - grid.open) / grid.slot.toFloat()))
                                        .height(maxOf(ROW_H * rows - 1.dp, ROW_H / 2))
                                        .fillMaxWidth()
                                        .padding(horizontal = 2.dp)
                                        .alpha(if (highlightMine && !mine) 0.3f else 1f)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(colors.background)
                                        .then(if (mine) Modifier.border(2.dp, Indigo500, RoundedCornerShape(3.dp)) else Modifier)
                                        .clickable { onOpenBooking(b.id) }
                                        .semantics { contentDescription = "${b.title}, ${b.startTime}–${b.endTime}, ${b.user?.name.orEmpty()}" },
                                ) {
                                    Row(Modifier.fillMaxSize()) {
                                        Box(Modifier.width(4.dp).fillMaxHeight().background(colors.border))
                                        Column(Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) {
                                            Text(
                                                (if (b.isRecurring) "↻ " else "") + b.title,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = colors.content,
                                                maxLines = if (rows < 2) 1 else 3,
                                                overflow = TextOverflow.Ellipsis,
                                                lineHeight = 15.sp,
                                            )
                                            if (rows >= 2) {
                                                Text(
                                                    "${b.startTime}–${b.endTime} · ${b.user?.name.orEmpty()}",
                                                    fontSize = 11.sp,
                                                    color = colors.content.copy(alpha = 0.8f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Garis waktu sekarang
                            val nowMin = now.hour * 60 + now.minute
                            if (col.date == now.toLocalDate() && nowMin > grid.open && nowMin < grid.close) {
                                Box(
                                    Modifier
                                        .offset(y = ROW_H * ((nowMin - grid.open) / grid.slot.toFloat()))
                                        .height(2.dp)
                                        .fillMaxWidth()
                                        .background(Red500),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Kalender bulan besar seperti versi web: maks. 3 booking per tanggal, ketuk tanggal untuk tampilan hari. */
@Composable
internal fun MonthCalendar(
    date: LocalDate,
    today: LocalDate,
    byDate: Map<String, List<RoomBooking>>,
    currentUserId: Long,
    highlightMine: Boolean,
    onPickDate: (LocalDate) -> Unit,
    onOpenBooking: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val days = CalendarRange.monthDays(date)
    // Seperti web: kalender minimal 720dp; di layar sempit bisa digeser ke samping.
    BoxWithConstraints(modifier.fillMaxSize()) {
    Column(
        Modifier
            .horizontalScroll(rememberScrollState())
            .width(maxOf(maxWidth, 720.dp))
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
    ) {
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            WEEKDAYS.forEach {
                Text(it, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }
        }
        HorizontalDivider(color = Color(0xFFCBD5E1))
        days.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    val inMonth = day.month == date.month
                    val bookings = byDate[day.toString()].orEmpty().sortedBy { it.booking.startTime }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 112.dp)
                            .background(if (inMonth) Color.White else Slate50)
                            .border(BorderStroke(0.5.dp, Slate100))
                            .clickable { onPickDate(day) }
                            .semantics { contentDescription = "${weekdayDate(day)}, ${bookings.size} booking" }
                            .padding(4.dp),
                    ) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            val selected = day == date
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent),
                            ) {
                                Text(
                                    "${day.dayOfMonth}",
                                    fontSize = 12.sp,
                                    fontWeight = if (selected || day == today) FontWeight.SemiBold else FontWeight.Normal,
                                    color = when {
                                        selected -> Color.White
                                        day == today -> MaterialTheme.colorScheme.primary
                                        !inMonth -> Color(0xFFCBD5E1)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                )
                            }
                        }
                        bookings.take(3).forEach { (room, b) ->
                            val colors = bookingBlockColors(b.type)
                            val mine = b.userId == currentUserId
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .alpha(if (highlightMine && !mine) 0.3f else 1f)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(colors.background)
                                    .then(if (mine) Modifier.border(1.dp, Indigo500, RoundedCornerShape(2.dp)) else Modifier)
                                    .clickable { onOpenBooking(b.id) },
                            ) {
                                Box(Modifier.width(2.dp).height(18.dp).background(colors.border))
                                Text(
                                    "${b.startTime} ${room.code ?: room.name} · ${b.title}",
                                    fontSize = 11.sp,
                                    color = colors.content,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp),
                                )
                            }
                        }
                        if (bookings.size > 3) {
                            Text("+${bookings.size - 3} lainnya", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
    }
}

/** Kalender kecil di samping jadwal (tablet lebar), seperti versi web. */
@Composable
fun MiniCalendar(
    month: LocalDate,
    selected: LocalDate,
    today: LocalDate,
    highlightWeek: Boolean,
    onSelect: (LocalDate) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    val selectedWeek = CalendarRange.range(com.ropekanbaru.booking.data.ScheduleMode.Week, selected)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Slate200),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrev, modifier = Modifier.size(32.dp)) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Bulan sebelumnya") }
                Text(monthTitle(month), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                IconButton(onClick = onNext, modifier = Modifier.size(32.dp)) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Bulan berikutnya") }
            }
            Row {
                WEEKDAYS.forEach {
                    Text(it, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(2.dp))
            CalendarRange.monthDays(month).chunked(7).forEach { week ->
                val inSelectedWeek = highlightWeek && week.first() == selectedWeek.start
                Row(Modifier.clip(RoundedCornerShape(6.dp)).background(if (inSelectedWeek) Indigo50 else Color.Transparent)) {
                    week.forEach { day ->
                        val isSelected = day == selected
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { onSelect(day) }
                                .semantics {
                                    role = Role.Button
                                    contentDescription = weekdayDate(day) + " " + day.year
                                    onClick { onSelect(day); true }
                                },
                        ) {
                            Text(
                                "${day.dayOfMonth}",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected || day == today) FontWeight.SemiBold else FontWeight.Normal,
                                color = when {
                                    isSelected -> Color.White
                                    day == today -> MaterialTheme.colorScheme.primary
                                    day.month != month.month -> Color(0xFFCBD5E1)
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
