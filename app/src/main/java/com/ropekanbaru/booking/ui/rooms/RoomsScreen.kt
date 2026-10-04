package com.ropekanbaru.booking.ui.rooms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.ropekanbaru.booking.data.remote.ApiErrors
import com.ropekanbaru.booking.data.remote.MrbsApi
import com.ropekanbaru.booking.data.remote.RoomDto
import com.ropekanbaru.booking.data.remote.RoomSaveRequest
import com.ropekanbaru.booking.ui.components.CardColumns
import com.ropekanbaru.booking.ui.components.FullSpan
import com.ropekanbaru.booking.ui.components.Badge
import com.ropekanbaru.booking.ui.components.ConfirmDialog
import com.ropekanbaru.booking.ui.components.ErrorCard
import com.ropekanbaru.booking.ui.components.FormDialog
import com.ropekanbaru.booking.ui.components.SectionLabel
import com.ropekanbaru.booking.ui.components.rememberLoader
import com.ropekanbaru.booking.ui.components.roomColor
import kotlinx.coroutines.launch

/** Pilihan warna ruangan, sama dengan versi web. */
private val ROOM_COLORS = listOf("#4f46e5", "#0891b2", "#059669", "#d97706", "#db2777", "#7c3aed", "#dc2626", "#475569")

/** Daftar ruangan; admin bisa menambah, mengubah, menonaktifkan, dan menghapus. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    Box(modifier.fillMaxSize()) {
        PullToRefreshBox(isRefreshing = rooms.refreshing, onRefresh = { rooms.load(refresh = true) }, modifier = Modifier.fillMaxSize()) {
            val list = rooms.data
            when {
                rooms.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                list == null -> Column(Modifier.padding(16.dp)) { ErrorCard(rooms.error ?: "Gagal memuat ruangan.", onRetry = { rooms.load() }) }
                else -> LazyVerticalGrid(
                    columns = CardColumns,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    error?.let { item(span = FullSpan) { ErrorCard(it) } }
                    list.groupBy { it.floor }.toSortedMap().forEach { (floor, floorRooms) ->
                        item(span = FullSpan, key = "floor-$floor") { SectionLabel("LANTAI $floor") }
                        items(floorRooms, key = { it.id }) { room ->
                            RoomCard(
                                room = room,
                                isAdmin = isAdmin,
                                onOpenSchedule = { onOpenSchedule(room.id) },
                                onBook = { onBook(room.id) },
                                onEdit = { editing = room },
                                onDelete = { deleting = room },
                            )
                        }
                    }
                }
            }
        }
        if (isAdmin) {
            SmallFloatingActionButton(
                onClick = { creating = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).semantics { contentDescription = "Tambah Ruangan" },
            ) { Icon(Icons.Default.Add, contentDescription = "Tambah Ruangan") }
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
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().alpha(if (room.isActive) 1f else 0.7f),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).clip(CircleShape).background(roomColor(room.color, MaterialTheme.colorScheme.primary)))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(room.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${room.code} · Lantai ${room.floor} · ${room.capacity} orang",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (!room.isActive) Badge("Nonaktif", MaterialTheme.colorScheme.error)
            }
            room.description?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (room.facilities.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    room.facilities.forEach { Badge(it, MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (room.isActive) {
                    OutlinedButton(onClick = onOpenSchedule) { Text("Lihat jadwal") }
                    OutlinedButton(onClick = onBook) { Text("Pesan") }
                }
                if (isAdmin) {
                    TextButton(onClick = onEdit) { Text("Ubah") }
                    TextButton(onClick = onDelete) { Text("Hapus", color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    FormDialog(onDismiss) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (room == null) "Tambah Ruangan" else "Ubah Ruangan") },
                    navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Tutup") } },
                    actions = { TextButton(onClick = save, enabled = !saving) { Text("Simpan") } },
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) { padding ->
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                if (saving) LinearProgressIndicator(Modifier.fillMaxWidth())
                error?.let { ErrorCard(it) }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.take(20) },
                        label = { Text("Kode") },
                        placeholder = { Text("R3A") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = floor,
                        onValueChange = { floor = it.filter(Char::isDigit).take(3) },
                        label = { Text("Lantai") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama ruangan") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = capacity,
                    onValueChange = { capacity = it.filter(Char::isDigit).take(4) },
                    label = { Text("Kapasitas (orang)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = facilities,
                    onValueChange = { facilities = it },
                    label = { Text("Fasilitas (pisahkan dengan koma)") },
                    placeholder = { Text("Proyektor, AC, Whiteboard") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("Warna", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ROOM_COLORS.forEach { c ->
                        val selected = c.equals(color, ignoreCase = true)
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(if (selected) 3.dp else 0.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                .padding(if (selected) 5.dp else 0.dp)
                                .clip(CircleShape)
                                .background(roomColor(c, MaterialTheme.colorScheme.primary))
                                .clickable { color = c }
                                .semantics { contentDescription = "Warna $c" + if (selected) " (dipilih)" else "" },
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().toggleable(value = active, role = Role.Switch, onValueChange = { active = it }),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Ruangan aktif", fontWeight = FontWeight.Medium)
                        Text(
                            "Ruangan nonaktif tidak tampil di jadwal dan tidak bisa dipesan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = active, onCheckedChange = null)
                }
            }
        }
    }
}
