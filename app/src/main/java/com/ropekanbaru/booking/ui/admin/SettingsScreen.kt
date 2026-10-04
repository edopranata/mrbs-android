package com.ropekanbaru.booking.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ropekanbaru.booking.data.SettingsRules
import com.ropekanbaru.booking.data.remote.ApiErrors
import com.ropekanbaru.booking.data.remote.ManageSettingsDto
import com.ropekanbaru.booking.data.remote.MrbsApi
import com.ropekanbaru.booking.data.remote.SettingsDto
import com.ropekanbaru.booking.ui.components.ConfirmDialog
import com.ropekanbaru.booking.ui.components.ErrorCard
import com.ropekanbaru.booking.ui.components.FieldLabel
import com.ropekanbaru.booking.ui.components.IconBox
import com.ropekanbaru.booking.ui.components.LabeledTextField
import com.ropekanbaru.booking.ui.components.PrimaryButton
import com.ropekanbaru.booking.ui.components.SecondaryButton
import com.ropekanbaru.booking.ui.components.Segmented
import com.ropekanbaru.booking.ui.components.SelectField
import com.ropekanbaru.booking.ui.components.Tw
import com.ropekanbaru.booking.ui.components.WebCard
import com.ropekanbaru.booking.ui.components.formatDuration
import com.ropekanbaru.booking.ui.components.rememberLoader
import com.ropekanbaru.booking.ui.icons.CalendarMonth
import com.ropekanbaru.booking.ui.icons.Restore
import com.ropekanbaru.booking.ui.icons.Schedule
import com.ropekanbaru.booking.ui.icons.TextFields
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
    val defaults = data.defaults.copy(appSubtitle = data.defaults.appSubtitle.orEmpty())
    var form by remember(data) { mutableStateOf(saved) }
    var advance by remember(data) { mutableStateOf(saved.maxAdvanceDays.toString()) }
    var repeat by remember(data) { mutableStateOf(saved.maxRepeatWeeks.toString()) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var confirmReset by remember { mutableStateOf(false) }

    val current = form.copy(maxAdvanceDays = advance.toIntOrNull() ?: 0, maxRepeatWeeks = repeat.toIntOrNull() ?: 0)
    val changes = SettingsRules.changes(saved, current)
    val discard = {
        form = saved
        advance = saved.maxAdvanceDays.toString()
        repeat = saved.maxRepeatWeeks.toString()
        error = null
    }
    /** Tautan "↺ default" di sebelah label, hanya bila nilainya berbeda dari default server. */
    val defaultLink: (Boolean, () -> Unit) -> (@Composable () -> Unit)? = { differs, reset ->
        if (!differs) null else {
            {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clip(RoundedCornerShape(4.dp)).clickable(onClick = reset).padding(horizontal = 4.dp, vertical = 2.dp),
                ) {
                    Icon(Icons.Outlined.Restore, null, tint = Tw.Indigo600, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("default", fontSize = 12.sp, color = Tw.Indigo600)
                }
            }
        }
    }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val gutter = if (maxWidth < 600.dp) 16.dp else 24.dp
        Column(Modifier.fillMaxSize()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    // Tablet: isi tidak melebar penuh, berada di tengah.
                    .wrapContentWidth()
                    .widthIn(max = 760.dp)
                    .padding(gutter),
            ) {
                Text(
                    "Hanya System Admin yang dapat mengubah pengaturan ini. Perubahan berlaku untuk semua pengguna.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Tw.Slate500,
                )
                SecondaryButton("Kembalikan semua ke default", onClick = { confirmReset = true }, icon = Icons.Outlined.Restore)

                Surface(shape = RoundedCornerShape(8.dp), color = Tw.Indigo50, border = BorderStroke(1.dp, Tw.Indigo100), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp)) {
                        Icon(Icons.Outlined.Info, null, tint = Tw.Indigo600, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(SettingsRules.summary(current, ::formatDuration), style = MaterialTheme.typography.bodyMedium, color = Color(0xFF3730A3))
                    }
                }
                error?.let { ErrorCard(it) }

                Section(Icons.Outlined.TextFields, "Identitas Aplikasi", "Tampil di sidebar, halaman login, dan judul tab browser.") {
                    LabeledTextField(
                        label = "Nama aplikasi",
                        value = form.appName,
                        onValueChange = { form = form.copy(appName = it.take(50)) },
                        hint = "Default: ${defaults.appName}",
                        labelTrailing = defaultLink(form.appName != defaults.appName) { form = form.copy(appName = defaults.appName) },
                    )
                    LabeledTextField(
                        label = "Subjudul",
                        value = form.appSubtitle.orEmpty(),
                        onValueChange = { form = form.copy(appSubtitle = it.take(100)) },
                        hint = "Default: ${defaults.appSubtitle.orEmpty().ifEmpty { "(kosong)" }}",
                        labelTrailing = defaultLink(form.appSubtitle != defaults.appSubtitle) { form = form.copy(appSubtitle = defaults.appSubtitle) },
                    )
                    // Pratinjau sidebar
                    Surface(shape = RoundedCornerShape(8.dp), color = Color.White, border = BorderStroke(1.dp, Tw.Slate300), modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Tw.Indigo600)) {
                                Text(form.appName.take(1).uppercase().ifEmpty { "—" }, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(form.appName.ifEmpty { "—" }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Tw.Slate900, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(form.appSubtitle.orEmpty(), fontSize = 12.sp, color = Tw.Slate500, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Text("Pratinjau sidebar", fontSize = 11.sp, color = Tw.Slate400)
                        }
                    }
                }

                Section(Icons.Outlined.Schedule, "Jam Operasional", "Rentang jam yang tampil di grid jadwal dan bisa dipesan.") {
                    Column {
                        FieldLabel("Interval slot")
                        Segmented(
                            options = SettingsRules.slotChoices.map { "$it menit" },
                            selected = SettingsRules.slotChoices.indexOf(form.slotMinutes),
                            onSelect = { form = SettingsRules.withSlot(form, SettingsRules.slotChoices[it]) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    val times = SettingsRules.dayTimes(form.slotMinutes)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SettingSelect("Jam buka", form.openTime, times, { it }, "Default: ${defaults.openTime}", defaultLink(form.openTime != defaults.openTime) { form = form.copy(openTime = defaults.openTime) }, Modifier.weight(1f)) {
                            form = form.copy(openTime = it)
                        }
                        SettingSelect("Jam tutup", form.closeTime, times, { it }, "Default: ${defaults.closeTime}", defaultLink(form.closeTime != defaults.closeTime) { form = form.copy(closeTime = defaults.closeTime) }, Modifier.weight(1f)) {
                            form = form.copy(closeTime = it)
                        }
                    }
                }

                Section(Icons.Outlined.CalendarMonth, "Aturan Booking", "Batasan durasi dan seberapa jauh ke depan user bisa memesan.") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SettingSelect(
                            "Durasi minimal", form.minDuration, SettingsRules.minDurationOptions(form), ::formatDuration,
                            "Default: ${formatDuration(defaults.minDuration)}",
                            defaultLink(form.minDuration != defaults.minDuration) { form = form.copy(minDuration = defaults.minDuration) },
                            Modifier.weight(1f),
                        ) { form = form.copy(minDuration = it, maxDuration = maxOf(it, form.maxDuration)) }
                        SettingSelect(
                            "Durasi maksimal", form.maxDuration, SettingsRules.maxDurationOptions(form), ::formatDuration,
                            "Default: ${formatDuration(defaults.maxDuration)}",
                            defaultLink(form.maxDuration != defaults.maxDuration) { form = form.copy(maxDuration = defaults.maxDuration) },
                            Modifier.weight(1f),
                        ) { form = form.copy(maxDuration = it) }
                    }
                    LabeledTextField(
                        label = "Batas pemesanan ke depan (user biasa)",
                        value = advance,
                        onValueChange = { advance = it.filter(Char::isDigit).take(3) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = { Text("hari", color = Tw.Slate400, modifier = Modifier.padding(end = 12.dp)) },
                        hint = "1–365 hari. Admin tidak dibatasi. Default: ${defaults.maxAdvanceDays} hari",
                    )
                    LabeledTextField(
                        label = "Maksimal booking berulang",
                        value = repeat,
                        onValueChange = { repeat = it.filter(Char::isDigit).take(2) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = { Text("minggu", color = Tw.Slate400, modifier = Modifier.padding(end = 12.dp)) },
                        hint = "Isi 1 untuk menonaktifkan booking berulang. Default: ${defaults.maxRepeatWeeks} minggu",
                    )
                }
            }

            // Bar simpan: muncul saat ada perubahan
            if (changes.isNotEmpty()) {
                Surface(color = Color.White, shadowElevation = 8.dp) {
                    Column {
                        HorizontalDivider(color = Tw.Slate200)
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Icon(Icons.Outlined.Info, null, tint = Tw.Amber600, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("${changes.size} pengaturan belum disimpan", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFB45309), modifier = Modifier.weight(1f))
                            SecondaryButton("Batal", onClick = discard, enabled = !saving)
                            Spacer(Modifier.width(8.dp))
                            PrimaryButton(
                                if (saving) "Menyimpan…" else "Simpan",
                                enabled = !saving,
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
                            )
                        }
                    }
                }
            }
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

@Composable
private fun <T> SettingSelect(
    label: String,
    value: T,
    options: List<T>,
    display: (T) -> String,
    hint: String,
    labelTrailing: (@Composable () -> Unit)?,
    modifier: Modifier,
    onSelect: (T) -> Unit,
) {
    Column(modifier) {
        FieldLabel(label, trailing = labelTrailing)
        SelectField(display(value), options, display, onSelect, Modifier.fillMaxWidth())
        Text(hint, style = MaterialTheme.typography.bodySmall, color = Tw.Slate400, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun Section(icon: ImageVector, title: String, description: String, content: @Composable () -> Unit) {
    WebCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(20.dp)) {
            IconBox(icon, Tw.Indigo50, Tw.Indigo600)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Tw.Slate900)
                Text(description, fontSize = 12.sp, color = Tw.Slate500)
            }
        }
        HorizontalDivider(color = Tw.Slate200)
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { content() }
    }
}
