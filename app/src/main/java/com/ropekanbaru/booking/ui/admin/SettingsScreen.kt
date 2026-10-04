package com.ropekanbaru.booking.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ropekanbaru.booking.data.SettingsRules
import com.ropekanbaru.booking.data.remote.ApiErrors
import com.ropekanbaru.booking.data.remote.ManageSettingsDto
import com.ropekanbaru.booking.data.remote.MrbsApi
import com.ropekanbaru.booking.data.remote.SettingsDto
import com.ropekanbaru.booking.ui.components.ConfirmDialog
import com.ropekanbaru.booking.ui.components.ErrorCard
import com.ropekanbaru.booking.ui.components.LabeledDropdown
import com.ropekanbaru.booking.ui.components.OptionDropdown
import com.ropekanbaru.booking.ui.components.formatDuration
import com.ropekanbaru.booking.ui.components.rememberLoader
import kotlinx.coroutines.launch

/** Pengaturan aplikasi (khusus System Admin), sama dengan halaman Pengaturan versi web. */
@Composable
fun SettingsContent(
    api: MrbsApi,
    onApplied: (SettingsDto) -> Unit,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val manage = rememberLoader { api.manageSettings() }
    when (val data = manage.data) {
        null -> Box(modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            if (manage.loading) CircularProgressIndicator()
            else ErrorCard(manage.error ?: "Gagal memuat pengaturan.", onRetry = { manage.load() })
        }
        // Form dibuat ulang setiap data tersimpan berubah (setelah simpan / kembalikan default).
        else -> SettingsForm(api, data, onSaved = {
            manage.set(it)
            onApplied(it.values)
        }, onMessage = onMessage, modifier = modifier)
    }
}

@Composable
private fun SettingsForm(
    api: MrbsApi,
    data: ManageSettingsDto,
    onSaved: (ManageSettingsDto) -> Unit,
    onMessage: (String) -> Unit,
    modifier: Modifier,
) {
    val scope = rememberCoroutineScope()
    val saved = data.values.copy(appSubtitle = data.values.appSubtitle.orEmpty())
    var form by remember(data) { mutableStateOf(saved) }
    var advance by remember(data) { mutableStateOf(saved.maxAdvanceDays.toString()) }
    var repeat by remember(data) { mutableStateOf(saved.maxRepeatWeeks.toString()) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var confirmReset by remember { mutableStateOf(false) }

    val current = form.copy(
        maxAdvanceDays = advance.toIntOrNull() ?: 0,
        maxRepeatWeeks = repeat.toIntOrNull() ?: 0,
    )
    val changes = SettingsRules.changes(saved, current)
    val isCustom = { key: String -> key in data.overridden }

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
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp)) {
            Text(
                SettingsRules.summary(current, ::formatDuration),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(12.dp),
            )
        }
        error?.let { ErrorCard(it) }

        Section("Identitas Aplikasi", "Tampil di bagian atas aplikasi, halaman login, dan judul tab browser.") {
            OutlinedTextField(
                value = form.appName,
                onValueChange = { form = form.copy(appName = it.take(50)) },
                label = { Text("Nama aplikasi") },
                singleLine = true,
                supportingText = defaultHint(isCustom("app_name"), data.defaults.appName) { form = form.copy(appName = data.defaults.appName) },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = form.appSubtitle.orEmpty(),
                onValueChange = { form = form.copy(appSubtitle = it.take(100)) },
                label = { Text("Subjudul") },
                singleLine = true,
                supportingText = defaultHint(isCustom("app_subtitle"), data.defaults.appSubtitle.orEmpty()) {
                    form = form.copy(appSubtitle = data.defaults.appSubtitle.orEmpty())
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Section("Jam Operasional", "Rentang jam yang tampil di jadwal dan bisa dipesan.") {
            Column {
                Text("Interval slot", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OptionDropdown(
                    value = "${form.slotMinutes} menit",
                    options = SettingsRules.slotChoices,
                    label = { "$it menit" },
                    onSelect = { form = SettingsRules.withSlot(form, it) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            val times = SettingsRules.dayTimes(form.slotMinutes)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LabeledDropdown("Jam buka", form.openTime, times, { form = form.copy(openTime = it) }, Modifier.weight(1f))
                LabeledDropdown("Jam tutup", form.closeTime, times, { form = form.copy(closeTime = it) }, Modifier.weight(1f))
            }
        }

        Section("Aturan Booking", "Batasan durasi dan seberapa jauh ke depan user bisa memesan.") {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Durasi minimal", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OptionDropdown(
                        value = formatDuration(form.minDuration),
                        options = SettingsRules.minDurationOptions(form),
                        label = ::formatDuration,
                        onSelect = { form = form.copy(minDuration = it, maxDuration = maxOf(it, form.maxDuration)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text("Durasi maksimal", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OptionDropdown(
                        value = formatDuration(form.maxDuration),
                        options = SettingsRules.maxDurationOptions(form),
                        label = ::formatDuration,
                        onSelect = { form = form.copy(maxDuration = it) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            OutlinedTextField(
                value = advance,
                onValueChange = { advance = it.filter(Char::isDigit).take(3) },
                label = { Text("Batas pemesanan ke depan (user biasa)") },
                suffix = { Text("hari") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = { Text("1–365 hari. Admin tidak dibatasi.") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = repeat,
                onValueChange = { repeat = it.filter(Char::isDigit).take(2) },
                label = { Text("Maksimal booking berulang") },
                suffix = { Text("minggu") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = { Text("1–52 minggu. Isi 1 untuk menonaktifkan booking berulang.") },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Button(
            onClick = {
                saving = true
                error = null
                scope.launch {
                    runCatching { api.updateSettings(changes) }
                        .onSuccess {
                            onSaved(it)
                            onMessage("Pengaturan berhasil disimpan.")
                        }
                        .onFailure { error = ApiErrors.message(it) }
                    saving = false
                }
            },
            enabled = changes.isNotEmpty() && !saving,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (saving) "Menyimpan…" else if (changes.isEmpty()) "Tidak ada perubahan" else "Simpan ${changes.size} perubahan") }
        if (changes.isNotEmpty()) {
            OutlinedButton(onClick = {
                form = saved
                advance = saved.maxAdvanceDays.toString()
                repeat = saved.maxRepeatWeeks.toString()
                error = null
            }, modifier = Modifier.fillMaxWidth()) { Text("Batalkan perubahan") }
        }
        TextButton(onClick = { confirmReset = true }, modifier = Modifier.fillMaxWidth()) {
            Text("↺ Kembalikan semua ke default", color = MaterialTheme.colorScheme.error)
        }
    }

    if (confirmReset) {
        ConfirmDialog(
            title = "Kembalikan ke default",
            message = "Semua pengaturan akan dikembalikan ke nilai default dari file .env server. Lanjutkan?",
            confirmText = "Kembalikan",
            danger = true,
            onDismiss = { confirmReset = false },
            onConfirm = {
                scope.launch {
                    runCatching { api.resetSettings() }
                        .onSuccess {
                            onSaved(it)
                            onMessage("Pengaturan dikembalikan ke default.")
                        }
                        .onFailure { error = ApiErrors.message(it) }
                }
            },
        )
    }
}

/** Keterangan di bawah kolom: tombol "↺ default" bila nilainya sudah diubah dari default server. */
private fun defaultHint(custom: Boolean, default: String, onUseDefault: () -> Unit): (@Composable () -> Unit)? =
    if (!custom) null else {
        {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Default: ${default.ifEmpty { "(kosong)" }}", modifier = Modifier.weight(1f))
                TextButton(onClick = onUseDefault) { Text("↺ default") }
            }
        }
    }

@Composable
private fun Section(title: String, description: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            content()
        }
    }
}
