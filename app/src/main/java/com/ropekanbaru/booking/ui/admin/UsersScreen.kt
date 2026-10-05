package com.ropekanbaru.booking.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ropekanbaru.booking.data.remote.ApiErrors
import com.ropekanbaru.booking.data.remote.MrbsApi
import com.ropekanbaru.booking.data.remote.UserDto
import com.ropekanbaru.booking.data.remote.UserSaveRequest
import com.ropekanbaru.booking.ui.components.CheckboxRow
import com.ropekanbaru.booking.ui.components.ConfirmDialog
import com.ropekanbaru.booking.ui.components.EmptyState
import com.ropekanbaru.booking.ui.components.ErrorCard
import com.ropekanbaru.booking.ui.components.FieldLabel
import com.ropekanbaru.booking.ui.components.FormRow
import com.ropekanbaru.booking.ui.components.LabeledTextField
import com.ropekanbaru.booking.ui.components.Pill
import com.ropekanbaru.booking.ui.components.PrimaryButton
import com.ropekanbaru.booking.ui.components.SecondaryButton
import com.ropekanbaru.booking.ui.components.SelectField
import com.ropekanbaru.booking.ui.components.Tw
import com.ropekanbaru.booking.ui.components.WebCard
import com.ropekanbaru.booking.ui.components.WebModal
import com.ropekanbaru.booking.ui.components.rememberPagedLoader
import com.ropekanbaru.booking.ui.icons.DeleteOutline
import com.ropekanbaru.booking.ui.icons.Group
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Level user beserta labelnya, urut dari tertinggi. */
private val ROLES = listOf("system_admin" to "System Admin", "admin" to "Admin", "user" to "User", "viewer" to "View Only")
private fun roleLabel(role: String) = ROLES.firstOrNull { it.first == role }?.second ?: role

/** Warna badge level, sama dengan web (rose / purple / slate). */
@Composable
private fun RoleBadge(role: String, label: String) = when (role) {
    "system_admin" -> Pill(label, Color(0xFFFFF1F2), Color(0xFFBE123C))
    "admin" -> Pill(label, Color(0xFFFAF5FF), Color(0xFF7E22CE))
    "viewer" -> Pill(label, Color(0xFFF0FDFA), Color(0xFF0F766E))
    else -> Pill(label, Tw.Slate100, Tw.Slate600)
}

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

    PullToRefreshBox(isRefreshing = users.refreshing, onRefresh = { users.reload(refresh = true) }, modifier = modifier.fillMaxSize()) {
        BoxWithConstraints {
            val wide = maxWidth >= 600.dp
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(if (wide) 24.dp else 16.dp)) {
                WebCard(Modifier.fillMaxWidth()) {
                    // Filter & tombol tambah (bertumpuk di HP, sebaris di tablet)
                    val searchField: @Composable (Modifier) -> Unit = { m ->
                        LabeledTextField(
                            label = "Cari",
                            value = search,
                            onValueChange = { search = it },
                            placeholder = "Cari nama, username, email, atau department…",
                            trailingIcon = { Icon(Icons.Outlined.Search, null, tint = Tw.Slate400) },
                            modifier = m,
                        )
                    }
                    val roleField: @Composable (Modifier) -> Unit = { m ->
                        SelectField(
                            value = role?.let(::roleLabel) ?: "Semua level",
                            options = listOf<String?>(null) + ROLES.map { it.first },
                            label = { it?.let(::roleLabel) ?: "Semua level" },
                            onSelect = { role = it },
                            modifier = m,
                        )
                    }
                    if (wide) {
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(16.dp)) {
                            searchField(Modifier.weight(1f))
                            roleField(Modifier.width(180.dp))
                            PrimaryButton("Tambah User", onClick = { creating = true }, icon = Icons.Default.Add)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(16.dp)) {
                            searchField(Modifier.fillMaxWidth())
                            roleField(Modifier.fillMaxWidth())
                            PrimaryButton("Tambah User", onClick = { creating = true }, icon = Icons.Default.Add, modifier = Modifier.fillMaxWidth())
                        }
                    }
                    HorizontalDivider(color = Tw.Slate200)

                    error?.let { Box(Modifier.padding(16.dp)) { ErrorCard(it) } }
                    when {
                        users.loading -> Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        !users.loaded -> Box(Modifier.padding(16.dp)) { ErrorCard(users.error ?: "Gagal memuat user.", onRetry = { users.reload() }) }
                        users.items.isEmpty() -> EmptyState(Icons.Outlined.Group, "Tidak ada user")
                        else -> {
                            users.items.forEachIndexed { i, u ->
                                if (i > 0) HorizontalDivider(color = Tw.Slate100)
                                UserRow(
                                    user = u,
                                    isSelf = u.id == currentUser.id,
                                    canManage = canManage(u),
                                    onEdit = { editing = u },
                                    onDelete = { deleting = u },
                                )
                            }
                            users.error?.let { Box(Modifier.padding(16.dp)) { ErrorCard(it) } }
                            HorizontalDivider(color = Tw.Slate200)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                                Text("${users.items.size} dari ${users.total} user", style = MaterialTheme.typography.bodySmall, color = Tw.Slate500, modifier = Modifier.weight(1f))
                                if (users.hasMore) SecondaryButton(if (users.loadingMore) "Memuat…" else "Muat lebih banyak", onClick = users::loadMore, enabled = !users.loadingMore)
                            }
                        }
                    }
                }
            }
        }
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
private fun UserRow(user: UserDto, isSelf: Boolean, canManage: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(user.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Tw.Slate900, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                if (isSelf) {
                    Spacer(Modifier.width(6.dp))
                    Pill("Anda", Tw.Slate100, Tw.Slate600)
                }
            }
            Text("@${user.username}" + (user.email?.let { " · $it" } ?: ""), fontSize = 12.sp, color = Tw.Slate500, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RoleBadge(user.role, user.roleLabel.ifBlank { roleLabel(user.role) })
                if (user.isActive) Pill("Aktif", Tw.Emerald50, Tw.Emerald700) else Pill("Nonaktif", Tw.Red50, Tw.Red700)
                Text(
                    listOfNotNull(user.department, user.bookingsCount?.let { "$it booking" }).joinToString(" · "),
                    fontSize = 12.sp,
                    color = Tw.Slate500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (canManage) {
            IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, "Ubah ${user.name}", tint = Tw.Slate600) }
            if (!isSelf) IconButton(onClick = onDelete) { Icon(Icons.Outlined.DeleteOutline, "Hapus ${user.name}", tint = Tw.Red600) }
        }
    }
}

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

    // Admin biasa tidak boleh memberi level System Admin (kecuali akun itu memang System Admin).
    val roleOptions = listOf("user", "viewer", "admin") + if (currentUser.isSystemAdmin || user?.role == "system_admin") listOf("system_admin") else emptyList()

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

    WebModal(
        title = if (user == null) "Tambah User" else "Ubah User",
        onDismiss = onDismiss,
        footer = {
            SecondaryButton("Batal", onClick = onDismiss)
            PrimaryButton(if (saving) "Menyimpan…" else "Simpan", onClick = save, enabled = !saving)
        },
    ) {
        error?.let { ErrorCard(it) }
        LabeledTextField(label = "Nama", value = name, onValueChange = { name = it })
        LabeledTextField(
            label = "Username (untuk login)",
            value = username,
            onValueChange = { username = it.lowercase().filter { c -> c.isLetterOrDigit() || c in "._-" }.take(50) },
            placeholder = "mis. budi.santoso",
            hint = "Huruf kecil, angka, titik, garis bawah, atau strip; tanpa spasi.",
        )
        LabeledTextField(label = "Email", value = email, onValueChange = { email = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
        LabeledTextField(
            label = if (user == null) "Password" else "Password (kosongkan jika tidak diubah)",
            value = password,
            onValueChange = { password = it },
            hint = if (user == null) "Minimal 8 karakter." else null,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )
        FormRow(
            { m ->
                Column(m) {
                    FieldLabel("Level")
                    // Tidak boleh mengubah level akun sendiri: tampil sebagai pilihan nonaktif.
                    SelectField(roleLabel(role), if (isSelf) emptyList() else roleOptions, ::roleLabel, { role = it }, Modifier.fillMaxWidth())
                    if (isSelf) Text("Level akun sendiri tidak dapat diubah.", style = MaterialTheme.typography.bodySmall, color = Tw.Slate500, modifier = Modifier.padding(top = 4.dp))
                }
            },
            { m -> LabeledTextField(label = "Department", value = department, onValueChange = { department = it }, modifier = m) },
        )
        LabeledTextField(
            label = "No. telepon",
            value = phone,
            onValueChange = { phone = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
        CheckboxRow(
            label = "Akun aktif",
            checked = active,
            onCheckedChange = { active = it },
            description = if (isSelf) "Akun sendiri tidak dapat dinonaktifkan." else "Akun nonaktif tidak bisa login dan langsung dikeluarkan dari semua perangkat.",
            enabled = !isSelf,
        )
    }
}
