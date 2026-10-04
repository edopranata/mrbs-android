package com.ropekanbaru.booking.ui.admin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.ropekanbaru.booking.data.remote.ApiErrors
import com.ropekanbaru.booking.data.remote.MrbsApi
import com.ropekanbaru.booking.data.remote.UserDto
import com.ropekanbaru.booking.data.remote.UserSaveRequest
import com.ropekanbaru.booking.ui.components.CardColumns
import com.ropekanbaru.booking.ui.components.FullSpan
import com.ropekanbaru.booking.ui.components.Badge
import com.ropekanbaru.booking.ui.components.ConfirmDialog
import com.ropekanbaru.booking.ui.components.EmptyText
import com.ropekanbaru.booking.ui.components.ErrorCard
import com.ropekanbaru.booking.ui.components.FormDialog
import com.ropekanbaru.booking.ui.components.LabeledDropdown
import com.ropekanbaru.booking.ui.components.rememberPagedLoader
import com.ropekanbaru.booking.ui.theme.Emerald600
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Level user beserta labelnya, urut dari tertinggi. */
private val ROLES = listOf("system_admin" to "System Admin", "admin" to "Admin", "user" to "User")
private fun roleLabel(role: String) = ROLES.firstOrNull { it.first == role }?.second ?: role

/** Manajemen user (Admin & System Admin). Hanya System Admin yang boleh mengelola akun System Admin. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersContent(
    api: MrbsApi,
    currentUser: UserDto,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var search by rememberSaveable { mutableStateOf("") }
    var query by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf<UserDto?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<UserDto?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    // Cari setelah berhenti mengetik sebentar.
    LaunchedEffect(search) {
        delay(350)
        query = search.trim()
    }
    val users = rememberPagedLoader(query, role) { page -> api.users(search = query.ifEmpty { null }, role = role, page = page) }
    val canManage = { u: UserDto -> currentUser.isSystemAdmin || !u.isSystemAdmin }

    Box(modifier.fillMaxSize()) {
        PullToRefreshBox(isRefreshing = users.refreshing, onRefresh = { users.reload(refresh = true) }, modifier = Modifier.fillMaxSize()) {
            LazyVerticalGrid(
                    columns = CardColumns,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item(span = FullSpan) {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        placeholder = { Text("Cari nama, username, email, atau divisi…") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item(span = FullSpan) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        FilterChip(selected = role == null, onClick = { role = null }, label = { Text("Semua level") })
                        ROLES.forEach { (value, label) ->
                            FilterChip(selected = role == value, onClick = { role = value }, label = { Text(label) })
                        }
                    }
                }
                error?.let { item(span = FullSpan) { ErrorCard(it) } }
                when {
                    users.loading -> item(span = FullSpan) { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                    !users.loaded -> item(span = FullSpan) { ErrorCard(users.error ?: "Gagal memuat user.", onRetry = { users.reload() }) }
                    else -> {
                        item(span = FullSpan) { Text("${users.total} user", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        if (users.items.isEmpty()) item(span = FullSpan) { EmptyText("Tidak ada user.") }
                        items(users.items, key = { it.id }) { u ->
                            UserCard(
                                user = u,
                                isSelf = u.id == currentUser.id,
                                canManage = canManage(u),
                                onEdit = { editing = u },
                                onDelete = { deleting = u },
                            )
                        }
                        users.error?.let { item(span = FullSpan) { ErrorCard(it) } }
                        if (users.hasMore) {
                            item(span = FullSpan) {
                                OutlinedButton(onClick = users::loadMore, enabled = !users.loadingMore, modifier = Modifier.fillMaxWidth()) {
                                    Text(if (users.loadingMore) "Memuat…" else "Muat lebih banyak")
                                }
                            }
                        }
                    }
                }
            }
        }
        SmallFloatingActionButton(
            onClick = { creating = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) { Icon(Icons.Default.Add, contentDescription = "Tambah User") }
    }

    if (creating || editing != null) {
        UserFormDialog(
            api = api,
            user = editing,
            currentUser = currentUser,
            onDismiss = {
                creating = false
                editing = null
            },
            onSaved = { message ->
                creating = false
                editing = null
                users.reload()
                onMessage(message)
            },
        )
    }

    deleting?.let { u ->
        ConfirmDialog(
            title = "Hapus user",
            message = "Akun \"${u.name}\" (${u.username}) akan dihapus beserta sesi login-nya. Lanjutkan?",
            confirmText = "Hapus",
            danger = true,
            onDismiss = { deleting = null },
            onConfirm = {
                scope.launch {
                    error = null
                    runCatching { api.deleteUser(u.id) }
                        .onSuccess {
                            users.reload()
                            onMessage(it.message ?: "User berhasil dihapus.")
                        }
                        .onFailure { error = ApiErrors.message(it) }
                }
            },
        )
    }
}

@Composable
private fun UserCard(user: UserDto, isSelf: Boolean, canManage: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().alpha(if (user.isActive) 1f else 0.7f),
    ) {
        Column(Modifier.padding(start = 14.dp, end = 6.dp, top = 12.dp, bottom = 4.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(user.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f, fill = false))
                if (isSelf) Badge("Anda", MaterialTheme.colorScheme.primary)
            }
            Text(
                listOfNotNull(user.username, user.department).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Badge(roleLabel(user.role), if (user.isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                if (user.isActive) Badge("Aktif", Emerald600) else Badge("Nonaktif", MaterialTheme.colorScheme.error)
                user.bookingsCount?.let {
                    Text("$it booking", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (canManage) {
                Row {
                    TextButton(onClick = onEdit) { Text("Ubah") }
                    if (!isSelf) TextButton(onClick = onDelete) { Text("Hapus", color = MaterialTheme.colorScheme.error) }
                }
            } else {
                Text(
                    "Hanya System Admin yang dapat mengelola akun ini.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserFormDialog(
    api: MrbsApi,
    user: UserDto?,
    currentUser: UserDto,
    onDismiss: () -> Unit,
    onSaved: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val isSelf = user?.id == currentUser.id
    var name by remember { mutableStateOf(user?.name.orEmpty()) }
    var username by remember { mutableStateOf(user?.username.orEmpty()) }
    var email by remember { mutableStateOf(user?.email.orEmpty()) }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(user?.role ?: "user") }
    var department by remember { mutableStateOf(user?.department.orEmpty()) }
    var phone by remember { mutableStateOf(user?.phone.orEmpty()) }
    var active by remember { mutableStateOf(user?.isActive ?: true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Admin biasa tidak boleh memberi level System Admin.
    val roleOptions = ROLES.map { it.first }.filter { currentUser.isSystemAdmin || it != "system_admin" }

    val save: () -> Unit = save@{
        error = when {
            name.isBlank() || username.isBlank() || email.isBlank() -> "Isi nama, username, dan email."
            user == null && password.length < 8 -> "Password minimal 8 karakter."
            password.isNotEmpty() && password.length < 8 -> "Password minimal 8 karakter."
            else -> null
        }
        if (error != null) return@save
        saving = true
        scope.launch {
            val body = UserSaveRequest(
                name = name.trim(),
                username = username.trim().lowercase(),
                email = email.trim(),
                password = password.ifEmpty { null },
                role = role,
                department = department.trim(),
                phone = phone.trim(),
                isActive = active,
            )
            runCatching { if (user == null) api.createUser(body) else api.updateUser(user.id, body) }
                .onSuccess { onSaved(if (user == null) "User berhasil ditambahkan." else "User berhasil diperbarui.") }
                .onFailure { error = ApiErrors.message(it) }
            saving = false
        }
    }

    FormDialog(onDismiss) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (user == null) "Tambah User" else "Ubah User") },
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
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it.lowercase().filter { c -> c.isLetterOrDigit() || c in "._-" }.take(50) },
                    label = { Text("Username") },
                    placeholder = { Text("mis. budi.santoso") },
                    supportingText = { Text("2–50 karakter: huruf, angka, titik, garis bawah, atau strip") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (user == null) "Password" else "Password baru (opsional)") },
                    supportingText = { Text(if (user == null) "Minimal 8 karakter." else "Kosongkan bila tidak diubah.") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (isSelf) {
                    Text(
                        "Level: ${roleLabel(role)} (tidak dapat mengubah level akun sendiri)",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    LabeledDropdown(label = "Level", value = role, options = roleOptions, onSelect = { role = it }, display = ::roleLabel)
                }
                OutlinedTextField(value = department, onValueChange = { department = it }, label = { Text("Divisi") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("No. telepon") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (!isSelf) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().toggleable(value = active, role = Role.Switch, onValueChange = { active = it }),
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Akun aktif", fontWeight = FontWeight.Medium)
                            Text(
                                "Akun nonaktif tidak bisa login dan langsung dikeluarkan dari semua perangkat.",
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
}
