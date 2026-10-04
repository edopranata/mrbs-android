package com.ropekanbaru.booking.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import com.ropekanbaru.booking.ui.components.LabeledTextField
import com.ropekanbaru.booking.ui.components.PrimaryButton
import com.ropekanbaru.booking.ui.components.Tw
import com.ropekanbaru.booking.ui.components.WebCard
import kotlinx.coroutines.launch

/** Ubah profil & password akun sendiri, sama dengan halaman Profil versi web (dua kolom di tablet lebar). */
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

    val profileCard: @Composable (Modifier) -> Unit = { m ->
        WebCard(m) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("Informasi Profil", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Tw.Slate900)
                    Text("Level akun: ${user.roleLabel}", style = MaterialTheme.typography.bodyMedium, color = Tw.Slate500)
                }
                LabeledTextField(
                    label = "Username",
                    value = user.username,
                    onValueChange = {},
                    enabled = false,
                    hint = "Dipakai untuk login. Hubungi admin untuk mengubahnya.",
                )
                user.email?.let { LabeledTextField(label = "Email", value = it, onValueChange = {}, enabled = false) }
                LabeledTextField(
                    label = "Nama",
                    value = name,
                    onValueChange = { name = it },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                )
                LabeledTextField(label = "Department", value = department, onValueChange = { department = it })
                LabeledTextField(
                    label = "No. telepon",
                    value = phone,
                    onValueChange = { phone = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                )
                profileError?.let { ErrorCard(it) }
                PrimaryButton(
                    if (savingProfile) "Menyimpan…" else "Simpan Profil",
                    enabled = !savingProfile,
                    onClick = {
                        if (name.isBlank()) {
                            profileError = "Nama wajib diisi."
                        } else {
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
                        }
                    },
                )
            }
        }
    }

    val passwordCard: @Composable (Modifier) -> Unit = { m ->
        WebCard(m) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Ubah Password", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Tw.Slate900)
                PasswordField("Password saat ini", current) { current = it }
                PasswordField("Password baru", password, hint = "Minimal 8 karakter.") { password = it }
                PasswordField("Ulangi password baru", confirm) { confirm = it }
                passwordError?.let { ErrorCard(it) }
                PrimaryButton(
                    if (savingPassword) "Menyimpan…" else "Ubah Password",
                    enabled = !savingPassword,
                    onClick = {
                        passwordError = when {
                            current.isEmpty() || password.isEmpty() -> "Isi password saat ini dan password baru."
                            password.length < 8 -> "Password baru minimal 8 karakter."
                            password != confirm -> "Ulangi password baru belum sama."
                            else -> null
                        }
                        if (passwordError == null) {
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
                        }
                    },
                )
                Text(
                    "Setelah password diubah, sesi login di perangkat lain akan dikeluarkan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Tw.Slate500,
                )
            }
        }
    }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val wide = maxWidth >= 840.dp
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(if (maxWidth < 600.dp) 16.dp else 24.dp),
        ) {
            if (wide) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    profileCard(Modifier.weight(1f))
                    passwordCard(Modifier.weight(1f))
                }
            } else {
                profileCard(Modifier.fillMaxWidth())
                passwordCard(Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun PasswordField(label: String, value: String, hint: String? = null, onChange: (String) -> Unit) {
    LabeledTextField(
        label = label,
        value = value,
        onValueChange = onChange,
        hint = hint,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    )
}
