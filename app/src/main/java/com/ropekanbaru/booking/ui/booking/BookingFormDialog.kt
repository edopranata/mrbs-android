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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ropekanbaru.booking.data.TimeSlots
import com.ropekanbaru.booking.data.remote.AvailabilityRoomDto
import com.ropekanbaru.booking.data.remote.MrbsApi
import com.ropekanbaru.booking.data.remote.OccurrenceDto
import com.ropekanbaru.booking.data.remote.SettingsDto
import com.ropekanbaru.booking.ui.components.DatePickerModal
import com.ropekanbaru.booking.ui.components.ErrorCard
import com.ropekanbaru.booking.ui.components.FieldLabel
import com.ropekanbaru.booking.ui.components.LabeledTextField
import com.ropekanbaru.booking.ui.components.Pill
import com.ropekanbaru.booking.ui.components.PrimaryButton
import com.ropekanbaru.booking.ui.components.SecondaryButton
import com.ropekanbaru.booking.ui.components.Segmented
import com.ropekanbaru.booking.ui.components.SelectField
import com.ropekanbaru.booking.ui.components.Tw
import com.ropekanbaru.booking.ui.components.WebModal
import com.ropekanbaru.booking.ui.components.formatDuration
import com.ropekanbaru.booking.ui.components.friendlyDate
import com.ropekanbaru.booking.ui.components.isWideLayout
import com.ropekanbaru.booking.ui.components.roomColor
import com.ropekanbaru.booking.ui.icons.CalendarMonth
import com.ropekanbaru.booking.ui.icons.Cancel
import com.ropekanbaru.booking.ui.icons.Group
import com.ropekanbaru.booking.ui.icons.Repeat
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val localeId = Locale.forLanguageTag("id-ID")
private val inputDate = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val weekdayName = DateTimeFormatter.ofPattern("EEEE", localeId)
private val dayMonth = DateTimeFormatter.ofPattern("d MMM", localeId)
private val Amber800 = Color(0xFF92400E)
private val Amber900 = Color(0xFF78350F)

/** Form Buat/Ubah Booking, mengikuti modal booking versi web. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookingFormDialog(
    api: MrbsApi,
    settings: SettingsDto,
    isAdmin: Boolean,
    defaults: BookingDefaults,
    onDismiss: () -> Unit,
    onSaved: (message: String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val form = remember { BookingFormState(api, scope, settings, isAdmin, defaults) }
    var pickDate by remember { mutableStateOf(false) }
    val submit: () -> Unit = { scope.launch { form.submit()?.let(onSaved) } }
    val wide = isWideLayout()

    WebModal(
        title = if (form.isEditing) "Ubah Booking" else "Buat Booking Baru",
        onDismiss = onDismiss,
        maxWidth = 720.dp,
        footer = {
            val room = form.selectedRoom
            if (wide && room != null) {
                Text(
                    "${room.name} (Lt ${room.floor}) · ${form.start}–${form.end}" + if (form.effectiveWeeks > 1) " · ${form.effectiveWeeks}× mingguan" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Tw.Slate500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            SecondaryButton("Batal", onClick = onDismiss)
            PrimaryButton(
                if (form.saving) "Menyimpan…" else if (form.isEditing) "Simpan Perubahan" else "Buat Booking",
                onClick = submit,
                enabled = !form.saving,
            )
        },
    ) {
        form.error?.let { ErrorCard(it) }

        LabeledTextField(
            label = "Judul rapat",
            value = form.title,
            onValueChange = { form.title = it },
            placeholder = "mis. Rapat Koordinasi Mingguan",
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        )

        // Tanggal (baris sendiri di HP), lalu jam mulai & selesai
        Column {
            FieldLabel("Tanggal")
            SelectField(
                value = form.date.format(inputDate),
                options = emptyList<Unit>(),
                label = { "" },
                onSelect = {},
                leadingIcon = Icons.Outlined.CalendarMonth,
                modifier = Modifier.fillMaxWidth().clickable { pickDate = true }.semantics { contentDescription = "Tanggal: ${friendlyDate(form.date.toString())}" },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                FieldLabel("Mulai")
                SelectField(form.start ?: "--:--", form.startOptions, { it }, form::updateStart, Modifier.fillMaxWidth())
            }
            Column(Modifier.weight(1f)) {
                FieldLabel("Selesai")
                SelectField(form.end ?: "--:--", form.endOptions, { it }, form::updateEnd, Modifier.fillMaxWidth())
            }
        }
        val duration = form.start?.let { s -> form.end?.let { e -> TimeSlots.toMinutes(e) - TimeSlots.toMinutes(s) } }
        Text(
            when {
                form.startOptions.isEmpty() -> "Jam operasional hari ini sudah lewat, silakan pilih tanggal lain."
                duration != null && duration > 0 -> "Durasi ${formatDuration(duration)} · Jam operasional ${settings.openTime}–${settings.closeTime}"
                else -> "Jam operasional ${settings.openTime}–${settings.closeTime}"
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (form.startOptions.isEmpty()) Tw.Red600 else Tw.Slate500,
        )

        // Berulang mingguan (hanya saat membuat; mengubah satu booking tidak mengubah seri)
        if (settings.maxRepeatWeeks > 1 && !form.isEditing) RepeatBox(form)

        // Jenis rapat & jumlah peserta (berdampingan di tablet)
        val typeField: @Composable (Modifier) -> Unit = { m ->
            Column(m) {
                FieldLabel("Jenis rapat")
                val types = listOf("internal" to "Internal", "external" to "Eksternal")
                Segmented(
                    options = types.map { it.second },
                    selected = types.indexOfFirst { it.first == form.type },
                    onSelect = { form.type = types[it].first },
                    selectedColor = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        val participantsField: @Composable (Modifier) -> Unit = { m ->
            LabeledTextField(
                label = "Jumlah peserta",
                value = form.participants,
                onValueChange = form::updateParticipants,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = m,
            )
        }
        if (wide) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                typeField(Modifier.weight(1f))
                participantsField(Modifier.weight(1f))
            }
        } else {
            typeField(Modifier.fillMaxWidth())
            participantsField(Modifier.fillMaxWidth())
        }

        // Ruangan
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FieldLabel("Pilih ruangan", modifier = Modifier.weight(1f))
                if (form.checking) CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
            }
            form.rooms.groupBy { it.floor }.toSortedMap().forEach { (floor, rooms) ->
                Text("LANTAI $floor", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Tw.Slate500)
                rooms.chunked(if (wide) 2 else 1).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { room ->
                            RoomOption(
                                room = room,
                                selected = form.roomId == room.id,
                                selectable = form.isSelectable(room),
                                weeks = form.effectiveWeeks,
                                onSelect = { form.selectRoom(room.id) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (wide && row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            if (form.rooms.isEmpty() && !form.checking) Text("Memuat daftar ruangan…", style = MaterialTheme.typography.bodyMedium, color = Tw.Slate500)
        }

        LabeledTextField(
            label = "Catatan / agenda (opsional)",
            value = form.description,
            onValueChange = { form.description = it },
            placeholder = "Agenda rapat, kebutuhan konsumsi, dsb.",
            singleLine = false,
            minLines = 3,
        )
    }

    if (pickDate) {
        DatePickerModal(
            selected = form.date,
            minDate = LocalDate.now(),
            maxDate = form.maxDate,
            onDismiss = { pickDate = false },
            onConfirm = form::updateDate,
        )
    }
}

/** Kotak "Ulangi setiap minggu" seperti web: sakelar, jumlah minggu, dan status tiap tanggal. */
@Composable
private fun RepeatBox(form: BookingFormState) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (form.repeat) Tw.Indigo50 else Color.White,
        border = BorderStroke(1.dp, if (form.repeat) Color(0xFFC7D2FE) else Tw.Slate200),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().toggleable(value = form.repeat, role = Role.Switch, onValueChange = form::updateRepeat),
            ) {
                Switch(
                    checked = form.repeat,
                    onCheckedChange = null,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = Tw.Indigo600,
                        uncheckedTrackColor = Tw.Slate300,
                        uncheckedThumbColor = Color.White,
                        uncheckedBorderColor = Color.Transparent,
                    ),
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Repeat, null, tint = Tw.Indigo600, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Ulangi setiap minggu", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Tw.Slate900)
                    }
                    Text(
                        "Setiap ${form.date.format(weekdayName).replaceFirstChar { it.titlecase(localeId) }}, ${form.start ?: "--:--"}–${form.end ?: "--:--"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Tw.Slate500,
                    )
                }
            }
            if (form.repeat) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Selama", style = MaterialTheme.typography.bodyMedium, color = Tw.Slate700)
                    SelectField("${form.repeatWeeks} minggu", form.weekOptions, { "$it minggu" }, form::updateRepeatWeeks, Modifier.width(130.dp))
                    Text("· sampai ${friendlyDate(form.lastDate.toString())}", style = MaterialTheme.typography.bodyMedium, color = Tw.Slate500)
                }
                if (form.roomId == null) {
                    Text("Pilih ruangan untuk melihat ketersediaan tiap tanggal.", style = MaterialTheme.typography.bodySmall, color = Tw.Slate500)
                } else if (form.occurrences.isNotEmpty()) {
                    OccurrenceChips(form.occurrences)
                    val blocked = form.occurrences.filter { !it.available }
                    if (blocked.isEmpty()) {
                        Text("Semua ${form.occurrences.size} tanggal tersedia", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Tw.Emerald600)
                    } else {
                        Surface(shape = RoundedCornerShape(6.dp), color = Tw.Amber50, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                blocked.take(3).forEach {
                                    Text("${friendlyDate(it.date)}: ${it.reason ?: "Tidak tersedia"}", fontSize = 12.sp, color = Amber800)
                                }
                                if (blocked.size > 3) Text("dan ${blocked.size - 3} tanggal lainnya.", fontSize = 12.sp, color = Amber800)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.toggleable(value = form.skipConflicts, role = Role.Checkbox, onValueChange = { form.skipConflicts = it }),
                                ) {
                                    Checkbox(checked = form.skipConflicts, onCheckedChange = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Lewati tanggal yang bentrok (${blocked.size} dari ${form.occurrences.size})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Amber900,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Tanggal-tanggal booking mingguan: hijau tersedia, merah (dicoret) bentrok. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OccurrenceChips(items: List<OccurrenceDto>) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { o ->
            Surface(
                shape = RoundedCornerShape(50),
                color = if (o.available) Tw.Emerald50 else Tw.Red50,
                border = BorderStroke(1.dp, if (o.available) Tw.Emerald200 else Tw.Red200),
                modifier = Modifier.semantics {
                    contentDescription = "${friendlyDate(o.date)}, ${o.startTime}–${o.endTime}: " + if (o.available) "tersedia" else (o.reason ?: "bentrok")
                },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Icon(
                        if (o.available) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel,
                        null,
                        tint = if (o.available) Tw.Emerald700 else Tw.Red700,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        LocalDate.parse(o.date).format(dayMonth),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (o.available) Tw.Emerald700 else Tw.Red700,
                        textDecoration = if (o.available) null else TextDecoration.LineThrough,
                    )
                }
            }
        }
    }
}

@Composable
private fun RoomOption(
    room: AvailabilityRoomDto,
    selected: Boolean,
    selectable: Boolean,
    weeks: Int,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (icon, status, statusColor) = when {
        room.available -> Triple(Icons.Outlined.CheckCircle, "Tersedia", Tw.Emerald600)
        weeks > 1 && room.conflictDates.isNotEmpty() && room.fitsCapacity ->
            Triple(Icons.Outlined.Info, "Bentrok ${room.conflictDates.size} dari $weeks minggu", if (selectable) Tw.Amber600 else Tw.Red600)
        room.conflicts.isNotEmpty() -> room.conflicts.first().let {
            Triple(Icons.Outlined.Cancel, "Terpakai ${it.startTime}–${it.endTime}: ${it.title}", Tw.Red600)
        }
        else -> Triple(Icons.Outlined.Info, "Kapasitas tidak cukup", Tw.Amber600)
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) Tw.Indigo50 else Color.White,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) Tw.Indigo500 else Tw.Slate200),
        modifier = modifier
            .alpha(if (selectable || selected) 1f else 0.6f)
            .clip(RoundedCornerShape(8.dp))
            .selectable(selected = selected, enabled = selectable, role = Role.RadioButton, onClick = onSelect),
    ) {
        Row(Modifier.padding(12.dp)) {
            Box(Modifier.padding(top = 4.dp).size(12.dp).clip(CircleShape).background(roomColor(room.color, Tw.Indigo600)))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        room.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Tw.Slate900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.width(6.dp))
                    Pill("Lt ${room.floor}", Tw.Slate100, Tw.Slate600)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Outlined.Group, null, tint = Tw.Slate500, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${room.capacity}", fontSize = 12.sp, color = Tw.Slate500)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Icon(icon, null, tint = statusColor, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(status, fontSize = 12.sp, color = statusColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
