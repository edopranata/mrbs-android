package com.ropekanbaru.booking.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ropekanbaru.booking.AppContainer
import com.ropekanbaru.booking.data.remote.SettingsDto
import com.ropekanbaru.booking.data.remote.UserDto
import com.ropekanbaru.booking.ui.booking.BookingDefaults
import com.ropekanbaru.booking.ui.booking.BookingDetailSheet
import com.ropekanbaru.booking.ui.booking.BookingFormDialog
import com.ropekanbaru.booking.ui.home.HomeContent
import com.ropekanbaru.booking.ui.home.HomeViewModel
import com.ropekanbaru.booking.ui.schedule.ScheduleContent
import com.ropekanbaru.booking.ui.schedule.ScheduleViewModel
import kotlinx.coroutines.launch

private enum class Tab(val label: String, val icon: ImageVector) {
    Home("Beranda", Icons.Default.Home),
    Schedule("Jadwal", Icons.Default.DateRange),
}

/** Kerangka setelah login: navigasi bawah, tombol Buat Booking, form & detail booking. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(user: UserDto, settings: SettingsDto, container: AppContainer) {
    val homeVm: HomeViewModel = viewModel(factory = viewModelFactory { initializer { HomeViewModel(container.api, container.bookingChanges) } })
    val scheduleVm: ScheduleViewModel = viewModel(factory = viewModelFactory { initializer { ScheduleViewModel(container.api, container.bookingChanges) } })

    var tab by rememberSaveable { mutableStateOf(Tab.Home) }
    var form by remember { mutableStateOf<BookingDefaults?>(null) }
    var detailId by remember { mutableStateOf<Long?>(null) }
    var confirmLogout by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val showMessage: (String) -> Unit = { message ->
        container.notifyBookingsChanged()
        scope.launch { snackbar.showSnackbar(message) }
    }

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
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { form = BookingDefaults(date = if (tab == Tab.Schedule) scheduleVm.state.date else null) },
                // Material3 menyembunyikan teks FAB dari aksesibilitas; labelnya diambil dari ikon.
                icon = { Icon(Icons.Default.Add, contentDescription = "Buat Booking") },
                text = { Text("Buat Booking") },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (tab) {
            Tab.Home -> HomeContent(
                user = user,
                vm = homeVm,
                onOpenBooking = { detailId = it },
                onOpenSchedule = { tab = Tab.Schedule },
                modifier = modifier,
            )
            Tab.Schedule -> ScheduleContent(
                vm = scheduleVm,
                currentUserId = user.id,
                onOpenBooking = { detailId = it },
                onBookRoom = { roomId, date -> form = BookingDefaults(roomId = roomId, date = date) },
                modifier = modifier,
            )
        }
    }

    form?.let { defaults ->
        BookingFormDialog(
            api = container.api,
            settings = settings,
            isAdmin = user.isAdmin,
            defaults = defaults,
            onDismiss = { form = null },
            onCreated = { message ->
                form = null
                showMessage(message)
            },
        )
    }

    detailId?.let { id ->
        BookingDetailSheet(
            api = container.api,
            bookingId = id,
            currentUserId = user.id,
            onDismiss = { detailId = null },
            onChanged = { message ->
                detailId = null
                showMessage(message)
            },
        )
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("Keluar") },
            text = { Text("Yakin ingin keluar dari aplikasi?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmLogout = false
                    scope.launch { container.authRepository.logout() }
                }) { Text("Keluar") }
            },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text("Batal") } },
        )
    }
}
