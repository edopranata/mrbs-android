package com.ropekanbaru.booking.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ropekanbaru.booking.data.remote.AvailabilityRoomDto
import com.ropekanbaru.booking.data.remote.MrbsApi
import com.ropekanbaru.booking.data.remote.SettingsDto
import com.ropekanbaru.booking.ui.components.DatePickerModal
import com.ropekanbaru.booking.ui.components.ErrorCard
import com.ropekanbaru.booking.ui.components.bookingTypeColor
import com.ropekanbaru.booking.ui.components.friendlyDate
import com.ropekanbaru.booking.ui.components.roomColor
import com.ropekanbaru.booking.ui.theme.Emerald600
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFormDialog(
    api: MrbsApi,
    settings: SettingsDto,
    isAdmin: Boolean,
    defaults: BookingDefaults,
    onDismiss: () -> Unit,
    onCreated: (message: String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val form = remember { BookingFormState(api, scope, settings, isAdmin, defaults) }
    var pickDate by remember { mutableStateOf(false) }
    val submit: () -> Unit = {
        scope.launch { form.submit()?.let(onCreated) }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Buat Booking") },
                    navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Tutup") } },
                    actions = { TextButton(onClick = submit, enabled = !form.saving) { Text("Simpan") } },
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) { padding ->
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                if (form.saving) LinearProgressIndicator(Modifier.fillMaxWidth())
                form.error?.let { ErrorCard(it) }

                OutlinedTextField(
                    value = form.title,
                    onValueChange = { form.title = it },
                    label = { Text("Judul rapat") },
                    placeholder = { Text("mis. Rapat Koordinasi Mingguan") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )

                // Tanggal
                OutlinedButton(onClick = { pickDate = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.DateRange, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Tanggal: ${friendlyDate(form.date.toString())}", modifier = Modifier.weight(1f))
                }

                // Jam
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TimeDropdown("Mulai", form.start, form.startOptions, form::updateStart, Modifier.weight(1f))
                    TimeDropdown("Selesai", form.end, form.endOptions, form::updateEnd, Modifier.weight(1f))
                }
                if (form.startOptions.isEmpty()) {
                    Text("Jam operasional hari ini sudah lewat. Pilih tanggal lain.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                } else {
                    Text(
                        "Jam operasional ${settings.openTime}–${settings.closeTime}, interval ${settings.slotMinutes} menit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Peserta & jenis rapat
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = form.participants,
                        onValueChange = form::updateParticipants,
                        label = { Text("Peserta") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(110.dp),
                    )
                    listOf("internal" to "Internal", "external" to "Eksternal").forEach { (value, label) ->
                        FilterChip(
                            selected = form.type == value,
                            onClick = { form.type = value },
                            label = { Text(label) },
                            leadingIcon = { Box(Modifier.size(10.dp).clip(CircleShape).background(bookingTypeColor(value))) },
                        )
                    }
                }

                // Berulang mingguan
                if (settings.maxRepeatWeeks > 1) {
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.toggleable(value = form.repeat, role = Role.Switch, onValueChange = form::updateRepeat),
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text("↻ Ulangi setiap minggu", fontWeight = FontWeight.Medium)
                                    Text(
                                        if (form.repeat) "Sampai ${friendlyDate(form.lastDate.toString())}" else "Hari & jam yang sama setiap minggu",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Switch(checked = form.repeat, onCheckedChange = null)
                            }
                            if (form.repeat) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Selama", modifier = Modifier.padding(end = 8.dp))
                                    OptionDropdown(
                                        value = "${form.repeatWeeks} minggu",
                                        options = form.weekOptions,
                                        label = { "$it minggu" },
                                        onSelect = form::updateRepeatWeeks,
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.toggleable(value = form.skipConflicts, role = Role.Checkbox, onValueChange = { form.skipConflicts = it }),
                                ) {
                                    Checkbox(checked = form.skipConflicts, onCheckedChange = null)
                                    Text("Lewati tanggal yang bentrok", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                // Ruangan
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Pilih ruangan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    if (form.checking) CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                }
                form.rooms.groupBy { it.floor }.toSortedMap().forEach { (floor, rooms) ->
                    Text("LANTAI $floor", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    rooms.forEach { room ->
                        RoomOption(
                            room = room,
                            selected = form.roomId == room.id,
                            selectable = form.isSelectable(room),
                            weeks = form.effectiveWeeks,
                            onSelect = { form.roomId = room.id },
                        )
                    }
                }
                if (form.rooms.isEmpty() && !form.checking) {
                    Text("Memuat daftar ruangan…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                OutlinedTextField(
                    value = form.description,
                    onValueChange = { form.description = it },
                    label = { Text("Catatan / agenda (opsional)") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )

                Button(onClick = submit, enabled = !form.saving, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    if (form.saving) {
                        CircularProgressIndicator(strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                    } else {
                        val room = form.selectedRoom
                        Text(if (room != null) "Buat Booking · ${room.name}" else "Buat Booking")
                    }
                }
            }
        }
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

@Composable
private fun TimeDropdown(label: String, value: String?, options: List<String>, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OptionDropdown(value = value ?: "--:--", options = options, label = { it }, onSelect = onSelect, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun <T> OptionDropdown(value: String, options: List<T>, label: (T) -> String, onSelect: (T) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(onClick = { open = true }, enabled = options.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
            Text(value, modifier = Modifier.weight(1f, fill = false))
            Text("  ▾")
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(label(option)) }, onClick = {
                    onSelect(option)
                    open = false
                })
            }
        }
    }
}

@Composable
private fun RoomOption(room: AvailabilityRoomDto, selected: Boolean, selectable: Boolean, weeks: Int, onSelect: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val (status, statusColor) = when {
        room.available -> "Tersedia" to Emerald600
        !room.fitsCapacity -> "Kapasitas tidak cukup (${room.capacity} orang)" to MaterialTheme.colorScheme.error
        weeks > 1 && room.conflictDates.isNotEmpty() ->
            "Bentrok ${room.conflictDates.size} dari $weeks minggu" to if (selectable) Color(0xFFD97706) else MaterialTheme.colorScheme.error
        room.conflicts.isNotEmpty() -> room.conflicts.first().let {
            "Terpakai ${it.startTime}–${it.endTime}: ${it.title}"
        } to MaterialTheme.colorScheme.error
        else -> "Tidak tersedia" to MaterialTheme.colorScheme.error
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (selectable || selected) 1f else 0.55f)
            .border(if (selected) 2.dp else 1.dp, if (selected) primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = selectable, onClick = onSelect),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(roomColor(room.color, primary)))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("${room.name}  ·  Lt ${room.floor}", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                Text(status, color = statusColor, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
            Text("${room.capacity} org", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

