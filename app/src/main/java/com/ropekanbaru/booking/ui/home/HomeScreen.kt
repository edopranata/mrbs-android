package com.ropekanbaru.booking.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ropekanbaru.booking.data.AuthRepository
import com.ropekanbaru.booking.data.remote.BookingDto
import com.ropekanbaru.booking.data.remote.MrbsApi
import com.ropekanbaru.booking.data.remote.SettingsDto
import com.ropekanbaru.booking.data.remote.UserDto
import com.ropekanbaru.booking.ui.components.friendlyDate
import com.ropekanbaru.booking.ui.components.longDate
import com.ropekanbaru.booking.ui.components.roomColor
import com.ropekanbaru.booking.ui.theme.Emerald600
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(user: UserDto, settings: SettingsDto, api: MrbsApi, auth: AuthRepository) {
    val vm: HomeViewModel = viewModel(factory = viewModelFactory { initializer { HomeViewModel(api) } })
    val state = vm.state
    val scope = rememberCoroutineScope()
    var confirmLogout by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(settings.appName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        settings.appSubtitle?.let {
                            Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                actions = { TextButton(onClick = { confirmLogout = true }) { Text("Keluar") } },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = vm::refresh,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item {
                    Column {
                        Text("Halo, ${user.name}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            listOfNotNull(user.roleLabel.ifBlank { null }, user.department).joinToString(" · ") +
                                " — ${longDate()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                when {
                    state.loading -> item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    state.dashboard == null -> item {
                        ErrorCard(state.error ?: "Gagal memuat data.", onRetry = vm::refresh)
                    }

                    else -> {
                        val dashboard = state.dashboard
                        val stats = dashboard.stats
                        state.error?.let { item { ErrorCard(it, onRetry = vm::refresh) } }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatCard("${stats.roomsInUse} / ${stats.roomsTotal}", "Ruangan dipakai", Modifier.weight(1f))
                                StatCard("${stats.bookingsToday}", "Booking hari ini", Modifier.weight(1f))
                                StatCard("${stats.myUpcoming}", "Booking saya", Modifier.weight(1f))
                            }
                        }
                        item {
                            Text(
                                "Booking Saya Berikutnya",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                        if (dashboard.myUpcoming.isEmpty()) {
                            item {
                                Text(
                                    "Belum ada booking mendatang.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                )
                            }
                        }
                        items(dashboard.myUpcoming, key = { it.id }) { BookingCard(it) }
                    }
                }

                item {
                    Text(
                        "Jadwal ruangan dan pembuatan booking akan hadir di versi berikutnya. Tarik ke bawah untuk memperbarui.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("Keluar") },
            text = { Text("Yakin ingin keluar dari aplikasi?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmLogout = false
                    scope.launch { auth.logout() }
                }) { Text("Keluar") }
            },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text("Batal") } },
        )
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BookingCard(booking: BookingDto) {
    val accent = roomColor(booking.room?.color, MaterialTheme.colorScheme.primary)
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.height(androidx.compose.foundation.layout.IntrinsicSize.Min)) {
            Box(Modifier.width(5.dp).fillMaxHeight().background(accent))
            Column(Modifier.padding(12.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        booking.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (booking.isOngoing) Badge("Berlangsung", Emerald600)
                }
                Text(
                    "${friendlyDate(booking.date)}, ${booking.startTime}–${booking.endTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                booking.room?.let {
                    Text(
                        "${it.name} · Lt ${it.floor}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun Badge(text: String, color: Color) {
    Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(50)) {
        Text(
            text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(message, color = MaterialTheme.colorScheme.onErrorContainer)
            OutlinedButton(onClick = onRetry) { Text("Coba lagi") }
        }
    }
}
