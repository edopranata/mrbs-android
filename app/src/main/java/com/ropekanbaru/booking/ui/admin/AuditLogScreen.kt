package com.ropekanbaru.booking.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ropekanbaru.booking.data.remote.AuditLogDto
import com.ropekanbaru.booking.data.remote.MrbsApi
import com.ropekanbaru.booking.ui.components.DatePickerModal
import com.ropekanbaru.booking.ui.components.EmptyState
import com.ropekanbaru.booking.ui.components.ErrorCard
import com.ropekanbaru.booking.ui.components.FieldLabel
import com.ropekanbaru.booking.ui.components.LabeledTextField
import com.ropekanbaru.booking.ui.components.Pill
import com.ropekanbaru.booking.ui.components.SecondaryButton
import com.ropekanbaru.booking.ui.components.SelectField
import com.ropekanbaru.booking.ui.components.Tw
import com.ropekanbaru.booking.ui.components.WebCard
import com.ropekanbaru.booking.ui.components.WebModal
import com.ropekanbaru.booking.ui.components.longDate
import com.ropekanbaru.booking.ui.components.rememberPagedLoader
import com.ropekanbaru.booking.ui.icons.CalendarMonth
import com.ropekanbaru.booking.ui.icons.Restore
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/** Kelompok aktivitas (sama dengan backend AuditLog::CATEGORIES). */
private val CATEGORIES = listOf(
    "auth" to "Akun & Login",
    "booking" to "Booking",
    "room" to "Ruangan",
    "user" to "User",
    "settings" to "Pengaturan",
    "system" to "Sistem",
)

private val categoryColors = mapOf(
    "auth" to (Tw.Sky50 to Tw.Sky600),
    "booking" to (Tw.Indigo50 to Tw.Indigo700),
    "room" to (Tw.Emerald50 to Tw.Emerald700),
    "user" to (Color(0xFFFAF5FF) to Color(0xFF7E22CE)),
    "settings" to (Tw.Amber50 to Tw.Amber600),
)

private val localeId = Locale.forLanguageTag("id-ID")
private val dayMonth = DateTimeFormatter.ofPattern("d MMM", localeId)
private val clock = DateTimeFormatter.ofPattern("HH:mm:ss")

@Composable
private fun ActionBadge(log: AuditLogDto, text: String = log.actionLabel) {
    val (bg, fg) = if (log.isFailure) Tw.Red50 to Tw.Red700 else categoryColors[log.category] ?: (Tw.Slate100 to Tw.Slate600)
    Pill(text, bg, fg)
}

private fun JsonElement?.display(): String = when (this) {
    null, JsonNull -> "—"
    is JsonPrimitive -> content.ifBlank { "—" }
    else -> toString()
}

private fun parseTime(iso: String): LocalDateTime? = runCatching { LocalDateTime.parse(iso) }.getOrNull()

/** Audit log (Admin & System Admin): siapa melakukan apa, kapan, dan dari mana. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogContent(api: MrbsApi, modifier: Modifier = Modifier) {
    var search by rememberSaveable { mutableStateOf("") }
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    var dateText by rememberSaveable { mutableStateOf<String?>(null) }
    var pickDate by remember { mutableStateOf(false) }
    var detail by remember { mutableStateOf<AuditLogDto?>(null) }
    val date = dateText?.let(LocalDate::parse)

    // Cari setelah berhenti mengetik sebentar.
    LaunchedEffect(search) {
        delay(350)
        query = search.trim()
    }
    val logs = rememberPagedLoader(query, category, dateText) { page ->
        api.auditLogs(search = query.ifEmpty { null }, category = category, from = dateText, to = dateText, page = page)
    }

    PullToRefreshBox(isRefreshing = logs.refreshing, onRefresh = { logs.reload(refresh = true) }, modifier = modifier.fillMaxSize()) {
        BoxWithConstraints {
            val wide = maxWidth >= 600.dp
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(if (wide) 24.dp else 16.dp)) {
                WebCard(Modifier.fillMaxWidth()) {
                    val searchField: @Composable (Modifier) -> Unit = { m ->
                        LabeledTextField(
                            label = "Cari",
                            value = search,
                            onValueChange = { search = it },
                            placeholder = "Aktivitas, nama pengguna, atau IP…",
                            trailingIcon = { Icon(Icons.Outlined.Search, null, tint = Tw.Slate400) },
                            modifier = m,
                        )
                    }
                    val categoryField: @Composable (Modifier) -> Unit = { m ->
                        Column(m) {
                            FieldLabel("Aktivitas")
                            SelectField(
                                value = CATEGORIES.firstOrNull { it.first == category }?.second ?: "Semua aktivitas",
                                options = listOf<String?>(null) + CATEGORIES.map { it.first },
                                label = { key -> CATEGORIES.firstOrNull { it.first == key }?.second ?: "Semua aktivitas" },
                                onSelect = { category = it },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    val dateField: @Composable (Modifier) -> Unit = { m ->
                        Column(m) {
                            FieldLabel("Tanggal")
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SecondaryButton(
                                    date?.let(::longDate) ?: "Semua tanggal",
                                    onClick = { pickDate = true },
                                    icon = Icons.Outlined.CalendarMonth,
                                    modifier = Modifier.weight(1f),
                                )
                                if (date != null) SecondaryButton("Hapus", onClick = { dateText = null })
                            }
                        }
                    }
                    if (wide) {
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(16.dp)) {
                            searchField(Modifier.weight(1f))
                            categoryField(Modifier.width(190.dp))
                            dateField(Modifier.width(280.dp))
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(16.dp)) {
                            searchField(Modifier.fillMaxWidth())
                            categoryField(Modifier.fillMaxWidth())
                            dateField(Modifier.fillMaxWidth())
                        }
                    }
                    HorizontalDivider(color = Tw.Slate200)

                    when {
                        logs.loading -> Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        !logs.loaded -> Box(Modifier.padding(16.dp)) { ErrorCard(logs.error ?: "Gagal memuat audit log.", onRetry = { logs.reload() }) }
                        logs.items.isEmpty() -> EmptyState(
                            Icons.Outlined.Restore,
                            "Belum ada catatan",
                            if (query.isNotEmpty() || category != null || date != null) "Tidak ada aktivitas yang cocok dengan filter." else "Aktivitas akan tercatat di sini.",
                        )
                        else -> {
                            logs.items.forEachIndexed { i, log ->
                                if (i > 0) HorizontalDivider(color = Tw.Slate100)
                                AuditRow(log, showIp = wide, onClick = { detail = log })
                            }
                            logs.error?.let { Box(Modifier.padding(16.dp)) { ErrorCard(it) } }
                            HorizontalDivider(color = Tw.Slate200)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                                Text("${logs.items.size} dari ${logs.total} catatan", style = MaterialTheme.typography.bodySmall, color = Tw.Slate500, modifier = Modifier.weight(1f))
                                if (logs.hasMore) SecondaryButton(if (logs.loadingMore) "Memuat…" else "Muat lebih banyak", onClick = logs::loadMore, enabled = !logs.loadingMore)
                            }
                        }
                    }
                }
            }
        }
    }

    if (pickDate) {
        DatePickerModal(
            selected = date ?: LocalDate.now(),
            maxDate = LocalDate.now(),
            onDismiss = { pickDate = false },
            onConfirm = { dateText = it.toString() },
        )
    }

    detail?.let { AuditDetailDialog(it, onDismiss = { detail = null }) }
}

@Composable
private fun AuditRow(log: AuditLogDto, showIp: Boolean, onClick: () -> Unit) {
    val time = parseTime(log.createdAt)
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp)) {
        Column(Modifier.width(64.dp)) {
            Text(time?.format(dayMonth).orEmpty(), fontSize = 12.sp, color = Tw.Slate500)
            Text(time?.format(clock).orEmpty(), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Tw.Slate700)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ActionBadge(log)
                Spacer(Modifier.width(8.dp))
                Text(log.userName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Tw.Slate900, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(log.description, style = MaterialTheme.typography.bodyMedium, color = Tw.Slate600, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (log.changes.isNotEmpty()) {
                Text("Diubah: " + log.changes.joinToString { it.label }, fontSize = 12.sp, color = Tw.Slate500, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (showIp && log.ipAddress != null) {
            Spacer(Modifier.width(12.dp))
            Text(log.ipAddress, fontSize = 12.sp, color = Tw.Slate400)
        }
    }
}

@Composable
private fun AuditDetailDialog(log: AuditLogDto, onDismiss: () -> Unit) {
    val time = parseTime(log.createdAt)
    WebModal(title = "Detail Aktivitas", onDismiss = onDismiss, maxWidth = 640.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionBadge(log, "${log.categoryLabel} · ${log.actionLabel}")
            Text(log.description, style = MaterialTheme.typography.bodyLarge, color = Tw.Slate900)
        }
        DetailItem("Waktu", time?.let { "${longDate(it.toLocalDate())}, ${it.format(clock)}" } ?: log.createdAt)
        DetailItem("Pengguna", log.userName + (log.user?.username?.let { " (@$it)" } ?: ""))
        DetailItem("Alamat IP", log.ipAddress ?: "—")
        log.meta.forEach { (key, value) -> DetailItem(key, value.display()) }
        DetailItem("Perangkat / browser", log.userAgent ?: "—", small = true)

        if (log.changes.isNotEmpty()) {
            Text("Perubahan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Tw.Slate900)
            Surface(shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Tw.Slate200), color = Color.White) {
                Column {
                    log.changes.forEachIndexed { i, change ->
                        if (i > 0) HorizontalDivider(color = Tw.Slate100)
                        val replaced = change.old.display() != "—" && change.new.display() != "—"
                        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(change.label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Tw.Slate500)
                            Text(
                                change.old.display(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Tw.Slate500,
                                textDecoration = if (replaced) TextDecoration.LineThrough else null,
                            )
                            Text("→ " + change.new.display(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Tw.Slate900)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String, small: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, fontSize = 12.sp, color = Tw.Slate500)
        Text(value, style = if (small) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium, color = if (small) Tw.Slate600 else Tw.Slate900)
    }
}
