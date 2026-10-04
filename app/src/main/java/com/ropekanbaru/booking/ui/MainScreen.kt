package com.ropekanbaru.booking.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp
import com.ropekanbaru.booking.ui.components.isWideLayout
import com.ropekanbaru.booking.ui.schedule.ScheduleWideContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.ropekanbaru.booking.ui.admin.SettingsContent
import com.ropekanbaru.booking.ui.admin.TodayBookingsContent
import com.ropekanbaru.booking.ui.admin.UsersContent
import com.ropekanbaru.booking.ui.booking.BookingDefaults
import com.ropekanbaru.booking.ui.booking.BookingDetailSheet
import com.ropekanbaru.booking.ui.booking.BookingFormDialog
import com.ropekanbaru.booking.ui.components.ConfirmDialog
import com.ropekanbaru.booking.ui.home.HomeContent
import com.ropekanbaru.booking.ui.home.HomeViewModel
import com.ropekanbaru.booking.ui.more.MoreContent
import com.ropekanbaru.booking.ui.more.Page
import com.ropekanbaru.booking.ui.mybookings.MyBookingsContent
import com.ropekanbaru.booking.ui.profile.ProfileContent
import com.ropekanbaru.booking.ui.rooms.RoomsContent
import com.ropekanbaru.booking.ui.schedule.ScheduleContent
import com.ropekanbaru.booking.ui.schedule.ScheduleViewModel
import kotlinx.coroutines.launch

private fun Page.toDestination(): Destination = when (this) {
    Page.Profile -> Destination.Profile
    Page.Rooms -> Destination.Rooms
    Page.TodayBookings -> Destination.TodayBookings
    Page.Users -> Destination.Users
    Page.Settings -> Destination.Settings
}

private fun Destination.toPage(): Page? = when (this) {
    Destination.Profile -> Page.Profile
    Destination.Rooms -> Page.Rooms
    Destination.TodayBookings -> Page.TodayBookings
    Destination.Users -> Page.Users
    Destination.Settings -> Page.Settings
    else -> null
}

private enum class Tab(val label: String, val icon: ImageVector, val fab: Boolean) {
    Home("Beranda", Icons.Default.Home, fab = true),
    Schedule("Jadwal", Icons.Default.DateRange, fab = true),
    MyBookings("Booking Saya", Icons.AutoMirrored.Filled.List, fab = true),
    More("Lainnya", Icons.Default.Menu, fab = false),
}

/**
 * Kerangka setelah login: navigasi bawah, tombol Buat Booking, halaman dari menu Lainnya,
 * serta form & detail booking yang bisa dibuka dari mana saja.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(user: UserDto, settings: SettingsDto, container: AppContainer) {
    val homeVm: HomeViewModel = viewModel(factory = viewModelFactory { initializer { HomeViewModel(container.api, container.bookingChanges) } })
    val scheduleVm: ScheduleViewModel = viewModel(factory = viewModelFactory { initializer { ScheduleViewModel(container.api, container.bookingChanges) } })
    val bookingChanges by container.bookingChanges.collectAsState()

    var tab by rememberSaveable { mutableStateOf(Tab.Home) }
    var page by rememberSaveable { mutableStateOf<Page?>(null) }
    var form by remember { mutableStateOf<BookingDefaults?>(null) }
    var detailId by remember { mutableStateOf<Long?>(null) }
    var confirmLogout by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val notify: (String) -> Unit = { message -> scope.launch { snackbar.showSnackbar(message) } }
    val bookingsChanged: (String) -> Unit = { message ->
        container.notifyBookingsChanged()
        notify(message)
    }
    val openSchedule: () -> Unit = {
        page = null
        tab = Tab.Schedule
    }

    // Level akun berubah (mis. diturunkan admin lain): tutup halaman yang tidak boleh diakses lagi.
    LaunchedEffect(user.role) {
        val allowed = when (page) {
            Page.TodayBookings, Page.Users -> user.isAdmin
            Page.Settings -> user.isSystemAdmin
            else -> true
        }
        if (!allowed) page = null
    }

    BackHandler(enabled = page != null) { page = null }
    BackHandler(enabled = page == null && tab != Tab.Home) { tab = Tab.Home }

    val wide = isWideLayout()

    /** Isi halaman dari menu Lainnya (HP) / sidebar (tablet). */
    val pageContent: @Composable (Page, Modifier) -> Unit = { current, modifier ->
        when (current) {
            Page.Profile -> ProfileContent(
                api = container.api,
                user = user,
                onUserUpdated = { container.authRepository.updateUser(it) },
                onMessage = notify,
                modifier = modifier,
            )
            Page.Rooms -> RoomsContent(
                api = container.api,
                isAdmin = user.isAdmin,
                onOpenSchedule = { roomId ->
                    scheduleVm.showRoomWeek(roomId)
                    openSchedule()
                },
                onBook = { roomId -> form = BookingDefaults(roomId = roomId) },
                onMessage = bookingsChanged, // ruangan berubah: jadwal & beranda ikut dimuat ulang
                modifier = modifier,
            )
            Page.TodayBookings -> TodayBookingsContent(
                api = container.api,
                bookingChanges = bookingChanges,
                currentUserId = user.id,
                onOpenBooking = { detailId = it },
                modifier = modifier,
            )
            Page.Users -> UsersContent(api = container.api, currentUser = user, onMessage = notify, modifier = modifier)
            Page.Settings -> SettingsContent(
                api = container.api,
                onApplied = { container.updateSettings(it) },
                onMessage = bookingsChanged,
                modifier = modifier,
            )
        }
    }

    /** Isi tab utama. Di tablet, jadwal memakai grid waktu seperti versi web. */
    val tabContent: @Composable (Tab, Modifier) -> Unit = { current, modifier ->
        when (current) {
            Tab.Home -> HomeContent(
                user = user,
                vm = homeVm,
                onOpenBooking = { detailId = it },
                onOpenSchedule = { tab = Tab.Schedule },
                onOpenMyBookings = { tab = Tab.MyBookings },
                onBookRoom = { roomId -> form = BookingDefaults(roomId = roomId) },
                modifier = modifier,
            )
            Tab.Schedule -> if (wide) {
                ScheduleWideContent(
                    vm = scheduleVm,
                    currentUserId = user.id,
                    slotMinutes = settings.slotMinutes,
                    onOpenBooking = { detailId = it },
                    onSelectSlot = { roomId, date, start, end -> form = BookingDefaults(roomId = roomId, date = date, start = start, end = end) },
                    modifier = modifier,
                )
            } else {
                ScheduleContent(
                    vm = scheduleVm,
                    currentUserId = user.id,
                    onOpenBooking = { detailId = it },
                    onBookRoom = { roomId, date -> form = BookingDefaults(roomId = roomId, date = date) },
                    modifier = modifier,
                )
            }
            Tab.MyBookings -> MyBookingsContent(
                api = container.api,
                bookingChanges = bookingChanges,
                onOpenBooking = { detailId = it },
                modifier = modifier,
            )
            Tab.More -> MoreContent(
                user = user,
                onOpen = { page = it },
                onLogout = { confirmLogout = true },
                modifier = modifier,
            )
        }
    }

    val newBooking: () -> Unit = {
        form = BookingDefaults(date = if (tab == Tab.Schedule && page == null) scheduleVm.state.date else null)
    }

    val current = page
    if (wide) {
        // Tablet: sidebar + header seperti versi web. Tab "Lainnya" tidak dipakai (menunya ada di sidebar).
        val destination = current?.toDestination() ?: when (tab) {
            Tab.Schedule -> Destination.Schedule
            Tab.MyBookings -> Destination.MyBookings
            else -> Destination.Dashboard
        }
        Row(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Sidebar(
                user = user,
                settings = settings,
                current = destination,
                onSelect = { dest ->
                    when (dest) {
                        Destination.Dashboard -> { page = null; tab = Tab.Home }
                        Destination.Schedule -> { page = null; tab = Tab.Schedule }
                        Destination.MyBookings -> { page = null; tab = Tab.MyBookings }
                        else -> { page = dest.toPage(); tab = Tab.More }
                    }
                },
                onLogout = { confirmLogout = true },
            )
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(destination.title, fontWeight = FontWeight.SemiBold) },
                        actions = {
                            Button(onClick = newBooking, modifier = Modifier.padding(end = 12.dp)) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Text("Buat Booking", modifier = Modifier.padding(start = 6.dp))
                            }
                        },
                    )
                },
                snackbarHost = { SnackbarHost(snackbar) },
                containerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.weight(1f),
            ) { padding ->
                val modifier = Modifier.padding(padding)
                if (current != null) pageContent(current, modifier) else tabContent(if (tab == Tab.More) Tab.Home else tab, modifier)
            }
        }
    } else if (current != null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(current.title, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = { IconButton(onClick = { page = null }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali") } },
                )
            },
            snackbarHost = { SnackbarHost(snackbar) },
            containerColor = MaterialTheme.colorScheme.background,
        ) { padding -> pageContent(current, Modifier.padding(padding)) }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(settings.appName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            settings.appSubtitle?.takeIf { it.isNotBlank() }?.let {
                                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
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
                if (tab.fab) {
                    ExtendedFloatingActionButton(
                        onClick = newBooking,
                        // Material3 menyembunyikan teks FAB dari aksesibilitas; labelnya diambil dari ikon.
                        icon = { Icon(Icons.Default.Add, contentDescription = "Buat Booking") },
                        text = { Text("Buat Booking") },
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbar) },
            containerColor = MaterialTheme.colorScheme.background,
        ) { padding -> tabContent(tab, Modifier.padding(padding)) }
    }

    form?.let { defaults ->
        BookingFormDialog(
            api = container.api,
            settings = settings,
            isAdmin = user.isAdmin,
            defaults = defaults,
            onDismiss = { form = null },
            onSaved = { message ->
                form = null
                bookingsChanged(message)
            },
        )
    }

    detailId?.let { id ->
        BookingDetailSheet(
            api = container.api,
            bookingId = id,
            currentUserId = user.id,
            isAdmin = user.isAdmin,
            onDismiss = { detailId = null },
            onEdit = { booking ->
                detailId = null
                form = BookingDefaults(editing = booking)
            },
            onChanged = { message ->
                detailId = null
                bookingsChanged(message)
            },
        )
    }

    if (confirmLogout) {
        ConfirmDialog(
            title = "Keluar",
            message = "Yakin ingin keluar dari aplikasi?",
            confirmText = "Keluar",
            onDismiss = { confirmLogout = false },
            onConfirm = { scope.launch { container.authRepository.logout() } },
        )
    }
}
