package com.ropekanbaru.booking.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import com.ropekanbaru.booking.ui.components.PrimaryButton
import com.ropekanbaru.booking.ui.components.Tw
import com.ropekanbaru.booking.ui.components.isWideLayout
import com.ropekanbaru.booking.ui.home.HomeContent
import com.ropekanbaru.booking.ui.home.HomeViewModel
import com.ropekanbaru.booking.ui.icons.EditCalendar
import com.ropekanbaru.booking.ui.mybookings.MyBookingsContent
import com.ropekanbaru.booking.ui.profile.ProfileContent
import com.ropekanbaru.booking.ui.rooms.RoomsContent
import com.ropekanbaru.booking.ui.schedule.ScheduleContent
import com.ropekanbaru.booking.ui.schedule.ScheduleViewModel
import kotlinx.coroutines.launch

/**
 * Kerangka setelah login, sama dengan versi web: sidebar menu (tetap di tablet, laci ☰ di HP),
 * header berjudul halaman dengan tombol Buat Booking, serta form & detail booking.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(user: UserDto, settings: SettingsDto, container: AppContainer) {
    val homeVm: HomeViewModel = viewModel(factory = viewModelFactory { initializer { HomeViewModel(container.api, container.bookingChanges) } })
    val scheduleVm: ScheduleViewModel = viewModel(factory = viewModelFactory { initializer { ScheduleViewModel(container.api, container.bookingChanges) } })
    val bookingChanges by container.bookingChanges.collectAsState()

    var destination by rememberSaveable { mutableStateOf(Destination.Dashboard) }
    // Bertambah setiap kali menu dibuka (termasuk membuka ulang menu yang sama), agar halaman
    // selalu meminta data terbaru ke server alih-alih menampilkan data lama di perangkat.
    var visit by rememberSaveable { mutableIntStateOf(0) }
    var form by remember { mutableStateOf<BookingDefaults?>(null) }
    var detailId by remember { mutableStateOf<Long?>(null) }
    var confirmLogout by remember { mutableStateOf(false) }
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val wide = isWideLayout()

    val notify: (String) -> Unit = { message -> scope.launch { snackbar.showSnackbar(message) } }
    val bookingsChanged: (String) -> Unit = { message ->
        container.notifyBookingsChanged()
        notify(message)
    }
    val navigate: (Destination) -> Unit = {
        destination = it
        visit++
        scope.launch { drawer.close() }
    }
    val newBooking: () -> Unit = {
        form = BookingDefaults(date = if (destination == Destination.Schedule) scheduleVm.state.date else null)
    }

    // Level akun berubah (mis. diturunkan admin lain): tutup halaman yang tidak boleh diakses lagi.
    LaunchedEffect(user.role) {
        if ((destination.admin && !user.isAdmin) || (destination.systemAdmin && !user.isSystemAdmin)) navigate(Destination.Dashboard)
    }

    BackHandler(enabled = drawer.isOpen) { scope.launch { drawer.close() } }
    BackHandler(enabled = !drawer.isOpen && destination != Destination.Dashboard) { navigate(Destination.Dashboard) }

    // Dashboard & Jadwal menyimpan datanya di ViewModel: minta data baru setiap kali dibuka.
    LaunchedEffect(destination, visit) {
        when (destination) {
            Destination.Dashboard -> homeVm.enter()
            Destination.Schedule -> scheduleVm.enter()
            else -> Unit
        }
    }

    val sidebar: @Composable (onClose: (() -> Unit)?) -> Unit = { onClose ->
        Sidebar(
            user = user,
            settings = settings,
            current = destination,
            onSelect = navigate,
            onLogout = {
                scope.launch { drawer.close() }
                confirmLogout = true
            },
            onClose = onClose,
        )
    }

    val page: @Composable () -> Unit = {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = { Text(destination.title, fontWeight = FontWeight.SemiBold) },
                        navigationIcon = {
                            if (!wide) IconButton(onClick = { scope.launch { drawer.open() } }) { Icon(Icons.Default.Menu, "Buka menu") }
                        },
                        actions = {
                            if (wide) {
                                PrimaryButton("Buat Booking", onClick = newBooking, icon = Icons.Outlined.EditCalendar, modifier = Modifier.padding(end = 12.dp))
                            } else {
                                FilledIconButton(
                                    onClick = newBooking,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Tw.Indigo600),
                                    modifier = Modifier.padding(end = 8.dp),
                                ) { Icon(Icons.Outlined.EditCalendar, "Buat Booking", Modifier.size(20.dp)) }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                    )
                    HorizontalDivider(color = Tw.Slate200)
                }
            },
            snackbarHost = { SnackbarHost(snackbar) },
            containerColor = Tw.Slate50,
        ) { padding ->
            val modifier = Modifier.padding(padding)
            // key: halaman dibuat ulang setiap kali dibuka, sehingga datanya dimuat ulang dari server.
            key(destination, visit) {
            when (destination) {
                Destination.Dashboard -> HomeContent(
                    user = user,
                    vm = homeVm,
                    onOpenBooking = { detailId = it },
                    onOpenSchedule = { navigate(Destination.Schedule) },
                    onOpenMyBookings = { navigate(Destination.MyBookings) },
                    onBookRoom = { roomId -> form = BookingDefaults(roomId = roomId) },
                    modifier = modifier,
                )
                Destination.Schedule -> ScheduleContent(
                    vm = scheduleVm,
                    currentUserId = user.id,
                    slotMinutes = settings.slotMinutes,
                    onOpenBooking = { detailId = it },
                    onSelectSlot = { roomId, date, start, end -> form = BookingDefaults(roomId = roomId, date = date, start = start, end = end) },
                    modifier = modifier,
                )
                Destination.MyBookings -> MyBookingsContent(
                    api = container.api,
                    bookingChanges = bookingChanges,
                    onOpenBooking = { detailId = it },
                    modifier = modifier,
                )
                Destination.Rooms -> RoomsContent(
                    api = container.api,
                    isAdmin = user.isAdmin,
                    onOpenSchedule = { roomId ->
                        scheduleVm.showRoomWeek(roomId)
                        navigate(Destination.Schedule)
                    },
                    onBook = { roomId -> form = BookingDefaults(roomId = roomId) },
                    onMessage = bookingsChanged, // ruangan berubah: jadwal & dashboard ikut dimuat ulang
                    modifier = modifier,
                )
                Destination.TodayBookings -> TodayBookingsContent(
                    api = container.api,
                    bookingChanges = bookingChanges,
                    currentUserId = user.id,
                    onOpenBooking = { detailId = it },
                    modifier = modifier,
                )
                Destination.Users -> UsersContent(api = container.api, currentUser = user, onMessage = notify, modifier = modifier)
                Destination.Settings -> SettingsContent(
                    api = container.api,
                    onApplied = { container.updateSettings(it) },
                    onMessage = bookingsChanged,
                    modifier = modifier,
                )
                Destination.Profile -> ProfileContent(
                    api = container.api,
                    user = user,
                    onUserUpdated = { container.authRepository.updateUser(it) },
                    onMessage = notify,
                    modifier = modifier,
                )
            }
            }
        }
    }

    if (wide) {
        Row(Modifier.fillMaxSize().background(Tw.Slate50)) {
            sidebar(null)
            Box(Modifier.weight(1f)) { page() }
        }
    } else {
        ModalNavigationDrawer(drawerState = drawer, drawerContent = { sidebar { scope.launch { drawer.close() } } }) {
            page()
        }
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
