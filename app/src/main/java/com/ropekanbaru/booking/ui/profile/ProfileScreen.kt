package com.ropekanbaru.booking.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.ropekanbaru.booking.data.remote.ApiErrors
import com.ropekanbaru.booking.data.remote.MrbsApi
import com.ropekanbaru.booking.data.remote.PasswordRequest
import com.ropekanbaru.booking.data.remote.ProfileRequest
import com.ropekanbaru.booking.data.remote.UserDto
import com.ropekanbaru.booking.ui.components.ErrorCard
import kotlinx.coroutines.launch

/** Ubah profil & password akun sendiri (sama dengan halaman Profil versi web). */
@Composable
fun ProfileContent(
    api: MrbsApi,
    user: UserDto,
    onUserUpdated: suspend (UserDto) -> Unit,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    var name by rememberSaveable { mutableStateOf(user.name) }
    var department by rememberSaveable { mutableStateOf(user.department.orEmpty()) }
    var phone by rememberSaveable { mutableStateOf(user.phone.orEmpty()) }
    var profileError by remember { mutableStateOf<String?>(null) }
    var savingProfile by remember { mutableStateOf(false) }

    var current by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var savingPassword by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            // Tablet: isi tidak melebar penuh, berada di tengah.
            .wrapContentWidth()
            .widthIn(max = 720.dp)
            .padding(16.dp),
    ) {
        Section("Informasi Profil") {
            OutlinedTextField(
                value = user.username,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text("Username") },
                supportingText = { Text("Dipakai untuk login. Hubungi admin untuk mengubahnya.") },
                modifier = Modifier.fillMaxWidth(),
            )
            user.email?.let {
                OutlinedTextField(value = it, onValueChange = {}, enabled = false, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            }
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = department,
                onValueChange = { department = it },
                label = { Text("Divisi") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("No. telepon") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
            )
            profileError?.let { ErrorCard(it) }
            Button(
                onClick = {
                    if (name.isBlank()) {
                        profileError = "Nama wajib diisi."
                        return@Button
                    }
                    savingProfile = true
                    profileError = null
                    scope.launch {
                        runCatching { api.updateProfile(ProfileRequest(name.trim(), department.trim(), phone.trim())).data }
                            .onSuccess {
                                onUserUpdated(it)
                                onMessage("Profil berhasil disimpan.")
                            }
                            .onFailure { profileError = ApiErrors.message(it) }
                        savingProfile = false
                    }
                },
                enabled = !savingProfile,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (savingProfile) "Menyimpan…" else "Simpan Profil") }
        }

        Section("Ubah Password") {
            PasswordField("Password saat ini", current) { current = it }
            PasswordField("Password baru", password, supporting = "Minimal 8 karakter.") { password = it }
            PasswordField("Ulangi password baru", confirm) { confirm = it }
            passwordError?.let { ErrorCard(it) }
            Button(
                onClick = {
                    passwordError = when {
                        current.isEmpty() || password.isEmpty() -> "Isi password saat ini dan password baru."
                        password.length < 8 -> "Password baru minimal 8 karakter."
                        password != confirm -> "Ulangi password baru belum sama."
                        else -> null
                    }
                    if (passwordError != null) return@Button
                    savingPassword = true
                    scope.launch {
                        runCatching { api.changePassword(PasswordRequest(current, password, confirm)) }
                            .onSuccess {
                                current = ""
                                password = ""
                                confirm = ""
                                onMessage(it.message ?: "Password berhasil diubah.")
                            }
                            .onFailure { passwordError = ApiErrors.message(it) }
                        savingPassword = false
                    }
                },
                enabled = !savingPassword,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (savingPassword) "Menyimpan…" else "Ubah Password") }
            Text(
                "Setelah password diubah, sesi login di perangkat lain akan dikeluarkan.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun PasswordField(label: String, value: String, supporting: String? = null, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        supportingText = supporting?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth(),
    )
}
