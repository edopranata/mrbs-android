package com.ropekanbaru.booking.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ropekanbaru.booking.BuildConfig
import com.ropekanbaru.booking.R
import com.ropekanbaru.booking.data.AuthRepository
import com.ropekanbaru.booking.data.remote.SettingsDto
import com.ropekanbaru.booking.ui.components.ErrorCard
import com.ropekanbaru.booking.ui.components.LabeledTextField
import com.ropekanbaru.booking.ui.components.PrimaryButton
import com.ropekanbaru.booking.ui.components.Tw
import com.ropekanbaru.booking.ui.icons.Visibility
import com.ropekanbaru.booking.ui.icons.VisibilityOff
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@Composable
fun LoginScreen(auth: AuthRepository, settings: SettingsDto) {
    val vm: LoginViewModel = viewModel(factory = viewModelFactory { initializer { LoginViewModel(auth) } })
    val state = vm.state
    val focus = LocalFocusManager.current
    var showPassword by rememberSaveable { mutableStateOf(false) }

    Surface(color = Tw.Slate50, modifier = Modifier.fillMaxSize()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        ) {
            // Seperti halaman login versi web: ikon, judul "Masuk", isian berlabel, tombol penuh.
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth(),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Tw.Indigo600),
                )
                Column {
                    Text("Masuk", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Tw.Slate900)
                    Text(
                        "Gunakan akun kantor Anda untuk melanjutkan.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Tw.Slate500,
                    )
                }
                Spacer(Modifier.height(8.dp))

                LabeledTextField(
                    label = "Username",
                    value = state.username,
                    onValueChange = vm::onUsernameChange,
                    enabled = !state.loading,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Next,
                    ),
                    textFieldModifier = Modifier.semantics { contentType = ContentType.Username },
                )
                LabeledTextField(
                    label = "Password",
                    value = state.password,
                    onValueChange = vm::onPasswordChange,
                    enabled = !state.loading,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (showPassword) "Sembunyikan password" else "Lihat password",
                                tint = Tw.Slate400,
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focus.clearFocus()
                        vm.submit()
                    }),
                    textFieldModifier = Modifier.semantics { contentType = ContentType.Password },
                )

                state.error?.let { ErrorCard(it) }

                PrimaryButton(
                    if (state.loading) "Memproses…" else "Masuk",
                    onClick = {
                        focus.clearFocus()
                        vm.submit()
                    },
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                )

                if (BuildConfig.DEBUG) {
                    Text(
                        "Server: ${BuildConfig.API_BASE_URL}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Tw.Slate400,
                    )
                }
            }
        }
    }
}
