package com.ropekanbaru.booking.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ropekanbaru.booking.data.remote.SettingsDto
import com.ropekanbaru.booking.data.remote.UserDto
import com.ropekanbaru.booking.ui.components.Tw
import com.ropekanbaru.booking.ui.icons.Apartment
import com.ropekanbaru.booking.ui.icons.Assignment
import com.ropekanbaru.booking.ui.icons.CalendarMonth
import com.ropekanbaru.booking.ui.icons.Dashboard
import com.ropekanbaru.booking.ui.icons.FactCheck
import com.ropekanbaru.booking.ui.icons.Group
import com.ropekanbaru.booking.ui.icons.Logout

/** Tujuan menu sidebar (sama dengan menu versi web). */
enum class Destination(val title: String, val icon: ImageVector, val admin: Boolean = false, val systemAdmin: Boolean = false) {
    Dashboard("Dashboard", Icons.Outlined.Dashboard),
    Schedule("Jadwal Ruangan", Icons.Outlined.CalendarMonth),
    MyBookings("Booking Saya", Icons.AutoMirrored.Outlined.Assignment),
    Rooms("Ruangan", Icons.Outlined.Apartment),
    TodayBookings("Semua Booking", Icons.AutoMirrored.Outlined.FactCheck, admin = true),
    Users("Manajemen User", Icons.Outlined.Group, admin = true),
    Settings("Pengaturan", Icons.Outlined.Settings, admin = true, systemAdmin = true),
    Profile("Profil", Icons.Outlined.AccountCircle),
    ;

    /** Boleh dibuka akun ini? View Only hanya melihat Dashboard, Jadwal Ruangan, Semua Booking (+ Profil). */
    fun allowedFor(user: UserDto): Boolean = when {
        user.isViewer -> this in VIEWER_MENU || this == Profile
        systemAdmin -> user.isSystemAdmin
        admin -> user.isAdmin
        else -> true
    }
}

private val MAIN_MENU = listOf(Destination.Dashboard, Destination.Schedule, Destination.MyBookings, Destination.Rooms)
private val VIEWER_MENU = listOf(Destination.Dashboard, Destination.Schedule, Destination.TodayBookings)

private val Slate200 = Color(0xFFE2E8F0)

@Composable
fun Sidebar(
    user: UserDto,
    settings: SettingsDto,
    current: Destination,
    onSelect: (Destination) -> Unit,
    onLogout: () -> Unit,
    onClose: (() -> Unit)? = null,
) {
    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.width(256.dp).fillMaxHeight()) {
        // Aplikasi tampil edge-to-edge: jangan tertutup status bar / navigasi sistem.
        Row(Modifier.systemBarsPadding()) {
            Column(Modifier.weight(1f)) {
                // Identitas aplikasi
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(64.dp).padding(start = 20.dp, end = 8.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primary),
                    ) { Icon(Icons.Outlined.CalendarMonth, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(settings.appName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        settings.appSubtitle?.takeIf { it.isNotBlank() }?.let {
                            Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    // HP: sidebar tampil sebagai laci; tombol tutup seperti versi web mobile.
                    onClose?.let { IconButton(onClick = it) { Icon(Icons.Default.Close, "Tutup menu", tint = Tw.Slate500) } }
                }
                HorizontalDivider(color = Slate200)

                // Menu
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(12.dp),
                ) {
                    (if (user.isViewer) VIEWER_MENU else MAIN_MENU).forEach {
                        MenuRow(it, current == it) { onSelect(it) }
                    }
                    if (user.isAdmin) {
                        Text(
                            "ADMIN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 12.dp, top = 20.dp, bottom = 4.dp),
                        )
                        Destination.entries.filter { it.admin && it.allowedFor(user) }.forEach {
                            MenuRow(it, current == it) { onSelect(it) }
                        }
                    }
                }

                // Akun
                HorizontalDivider(color = Slate200)
                Column(Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (current == Destination.Profile) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .clickable { onSelect(Destination.Profile) }
                            .padding(8.dp),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                        ) {
                            Text(initials(user.name), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(user.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                "${user.roleLabel} · ${user.department ?: "-"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onLogout)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.Logout, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Keluar", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Box(Modifier.width(1.dp).fillMaxHeight().background(Slate200))
        }
    }
}

@Composable
private fun MenuRow(destination: Destination, selected: Boolean, onClick: () -> Unit) {
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Tab
                this.selected = selected
            }
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Icon(destination.icon, null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(destination.title, color = color, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

private fun initials(name: String): String =
    name.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
