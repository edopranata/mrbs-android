package com.ropekanbaru.booking.ui.rooms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ropekanbaru.booking.data.remote.ApiErrors
import com.ropekanbaru.booking.data.remote.MrbsApi
import com.ropekanbaru.booking.data.remote.RoomDto
import com.ropekanbaru.booking.data.remote.RoomSaveRequest
import com.ropekanbaru.booking.ui.components.CheckboxRow
import com.ropekanbaru.booking.ui.components.ConfirmDialog
import com.ropekanbaru.booking.ui.components.ErrorCard
import com.ropekanbaru.booking.ui.components.FieldLabel
import com.ropekanbaru.booking.ui.components.FormRow
import com.ropekanbaru.booking.ui.components.GhostButton
import com.ropekanbaru.booking.ui.components.LabeledTextField
import com.ropekanbaru.booking.ui.components.Pill
import com.ropekanbaru.booking.ui.components.PrimaryButton
import com.ropekanbaru.booking.ui.components.SecondaryButton
import com.ropekanbaru.booking.ui.components.Tw
import com.ropekanbaru.booking.ui.components.WebCard
import com.ropekanbaru.booking.ui.components.WebModal
import com.ropekanbaru.booking.ui.components.rememberLoader
import com.ropekanbaru.booking.ui.components.roomColor
import com.ropekanbaru.booking.ui.icons.CalendarMonth
import com.ropekanbaru.booking.ui.icons.DeleteOutline
import com.ropekanbaru.booking.ui.icons.EditCalendar
import com.ropekanbaru.booking.ui.icons.Group
import kotlinx.coroutines.launch

/** Pilihan warna ruangan, sama dengan versi web. */
private val ROOM_COLORS = listOf("#4f46e5", "#0891b2", "#059669", "#d97706", "#db2777", "#7c3aed", "#dc2626", "#475569")

/** Daftar ruangan per lantai seperti versi web; admin bisa menambah, mengubah, dan menghapus. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsContent(
    api: MrbsApi,
    isAdmin: Boolean,
    onOpenSchedule: (roomId: Long) -> Unit,
    onBook: (roomId: Long) -> Unit,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val rooms = rememberLoader { api.rooms().data }
    var editing by remember { mutableStateOf<RoomDto?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<RoomDto?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    PullToRefreshBox(isRefreshing = rooms.refreshing, onRefresh = { rooms.load(refresh = true) }, modifier = modifier.fillMaxSize()) {
        BoxWithConstraints {
            val columns = if (maxWidth >= 600.dp) 2 else 1
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(if (maxWidth < 600.dp) 16.dp else 24.dp),
            ) {
                val list = rooms.data
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        list?.let { "${it.size} ruang rapat di ${it.map { r -> r.floor }.distinct().size} lantai" } ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Tw.Slate500,
                        modifier = Modifier.weight(1f),
                    )
                    if (isAdmin) PrimaryButton("Tambah Ruangan", onClick = { creating = true }, icon = Icons.Default.Add)
                }
                error?.let { ErrorCard(it) }
                when {
                    rooms.loading -> Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    list == null -> ErrorCard(rooms.error ?: "Gagal memuat ruangan.", onRetry = { rooms.load() })
                    else -> list.groupBy { it.floor }.toSortedMap().forEach { (floor, floorRooms) ->
                        Text("LANTAI $floor", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Tw.Slate500)
                        floorRooms.chunked(columns).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                row.forEach { room ->
                                    RoomCard(
                                        room = room,
                                        isAdmin = isAdmin,
                                        onOpenSchedule = { onOpenSchedule(room.id) },
                                        onBook = { onBook(room.id) },
                                        onEdit = { editing = room },
                                        onDelete = { deleting = room },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (creating || editing != null) {
        RoomFormDialog(
            api = api,
            room = editing,
            suggestedColor = ROOM_COLORS[(rooms.data?.size ?: 0) % ROOM_COLORS.size],
            onDismiss = {
                creating = false
                editing = null
            },
            onSaved = { message ->
                creating = false
                editing = null
                rooms.load()
                onMessage(message)
            },
        )
    }

    deleting?.let { room ->
        ConfirmDialog(
            title = "Hapus ruangan",
            message = "Ruangan \"${room.name}\" akan dihapus. Ruangan yang punya riwayat booking tidak bisa dihapus; nonaktifkan sebagai gantinya.",
            confirmText = "Hapus",
            danger = true,
            onDismiss = { deleting = null },
            onConfirm = {
                scope.launch {
                    error = null
                    runCatching { api.deleteRoom(room.id) }
                        .onSuccess {
                            rooms.load()
                            onMessage(it.message ?: "Ruangan berhasil dihapus.")
                        }
                        .onFailure { error = ApiErrors.message(it) }
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoomCard(
    room: RoomDto,
    isAdmin: Boolean,
    onOpenSchedule: () -> Unit,
    onBook: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WebCard(modifier.alpha(if (room.isActive) 1f else 0.6f)) {
        Box(Modifier.fillMaxWidth().height(6.dp).background(roomColor(room.color, Tw.Indigo600)))
        Column(Modifier.padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(room.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Tw.Slate900)
                    Text("${room.code} · Lantai ${room.floor}", fontSize = 12.sp, color = Tw.Slate500)
                }
                if (!room.isActive) {
                    Pill("Nonaktif", Tw.Slate200, Tw.Slate600)
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clip(androidx.compose.foundation.shape.RoundedCornerShape(50)).background(Tw.Slate100).padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Icon(Icons.Outlined.Group, null, tint = Tw.Slate600, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${room.capacity} orang", fontSize = 12.sp, color = Tw.Slate600)
                    }
                }
            }
            room.description?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = Tw.Slate600) }
            if (room.facilities.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    room.facilities.forEach { Pill(it, Tw.Indigo50, Tw.Indigo700) }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (room.isActive) {
                    SecondaryButton("Pesan", onClick = onBook, icon = Icons.Outlined.EditCalendar)
                    GhostButton("Jadwal", onClick = onOpenSchedule, icon = Icons.Outlined.CalendarMonth)
                }
                Spacer(Modifier.weight(1f))
                if (isAdmin) {
                    GhostButton("Ubah", onClick = onEdit, icon = Icons.Outlined.Edit)
                    IconButton(onClick = onDelete) { Icon(Icons.Outlined.DeleteOutline, "Hapus ${room.name}", tint = Tw.Red600) }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoomFormDialog(
    api: MrbsApi,
    room: RoomDto?,
    suggestedColor: String,
    onDismiss: () -> Unit,
    onSaved: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var code by remember { mutableStateOf(room?.code.orEmpty()) }
    var name by remember { mutableStateOf(room?.name.orEmpty()) }
    var floor by remember { mutableStateOf(room?.floor?.toString().orEmpty()) }
    var capacity by remember { mutableStateOf(room?.capacity?.toString().orEmpty()) }
    var facilities by remember { mutableStateOf(room?.facilities?.joinToString(", ").orEmpty()) }
    var description by remember { mutableStateOf(room?.description.orEmpty()) }
    var color by remember { mutableStateOf(room?.color ?: suggestedColor) }
    var active by remember { mutableStateOf(room?.isActive ?: true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val save: () -> Unit = save@{
        error = when {
            code.isBlank() || name.isBlank() -> "Isi kode dan nama ruangan."
            floor.toIntOrNull() == null -> "Isi lantai dengan angka."
            (capacity.toIntOrNull() ?: 0) < 1 -> "Kapasitas minimal 1 orang."
            else -> null
        }
        if (error != null) return@save
        saving = true
        scope.launch {
            val body = RoomSaveRequest(
                code = code.trim(),
                name = name.trim(),
                floor = floor.toInt(),
                capacity = capacity.toInt(),
                facilities = facilities.split(',').map { it.trim() }.filter { it.isNotEmpty() },
                description = description.trim(),
                color = color,
                isActive = active,
            )
            runCatching { if (room == null) api.createRoom(body) else api.updateRoom(room.id, body) }
                .onSuccess { onSaved(if (room == null) "Ruangan berhasil ditambahkan." else "Ruangan berhasil diperbarui.") }
                .onFailure { error = ApiErrors.message(it) }
            saving = false
        }
    }

    WebModal(
        title = if (room == null) "Tambah Ruangan" else "Ubah Ruangan",
        onDismiss = onDismiss,
        footer = {
            SecondaryButton("Batal", onClick = onDismiss)
            PrimaryButton(if (saving) "Menyimpan…" else "Simpan", onClick = save, enabled = !saving)
        },
    ) {
        error?.let { ErrorCard(it) }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LabeledTextField(
                label = "Kode",
                value = code,
                onValueChange = { code = it.take(20) },
                placeholder = "R3A",
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                modifier = Modifier.weight(1f),
            )
            LabeledTextField(
                label = "Lantai",
                value = floor,
                onValueChange = { floor = it.filter(Char::isDigit).take(3) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
        }
        LabeledTextField(
            label = "Nama ruangan",
            value = name,
            onValueChange = { name = it },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
        )
        FormRow(
            { m ->
                LabeledTextField(
                    label = "Kapasitas (orang)",
                    value = capacity,
                    onValueChange = { capacity = it.filter(Char::isDigit).take(4) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = m,
                )
            },
            { m ->
                Column(m) {
                    FieldLabel("Warna")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                        ROOM_COLORS.forEach { c ->
                            val selected = c.equals(color, ignoreCase = true)
                            Box(
                                Modifier
                                    .size(32.dp)
                                    .then(if (selected) Modifier.border(2.dp, Tw.Slate900, CircleShape).padding(3.dp) else Modifier)
                                    .clip(CircleShape)
                                    .background(roomColor(c, Tw.Indigo600))
                                    .clickable { color = c }
                                    .semantics { contentDescription = "Warna $c" + if (selected) " (dipilih)" else "" },
                            )
                        }
                    }
                }
            },
        )
        LabeledTextField(
            label = "Fasilitas (pisahkan dengan koma)",
            value = facilities,
            onValueChange = { facilities = it },
            placeholder = "Proyektor, AC, Whiteboard",
        )
        LabeledTextField(label = "Deskripsi", value = description, onValueChange = { description = it }, singleLine = false, minLines = 2)
        CheckboxRow(
            label = "Ruangan aktif (dapat dipesan)",
            checked = active,
            onCheckedChange = { active = it },
            description = "Ruangan nonaktif tidak tampil di jadwal dan tidak bisa dipesan.",
        )
    }
}
