package com.ropekanbaru.booking.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ropekanbaru.booking.data.remote.UserDto

/** Halaman tambahan yang dibuka dari menu Lainnya. */
enum class Page(val title: String) {
    Profile("Profil"),
    Rooms("Ruangan"),
    TodayBookings("Semua Booking"),
    Users("Manajemen User"),
    Settings("Pengaturan"),
}

/** Menu Lainnya: akun, ruangan, menu admin (sesuai level), dan keluar. */
@Composable
fun MoreContent(user: UserDto, onOpen: (Page) -> Unit, onLogout: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ListItem(
            headlineContent = { Text(user.name, fontWeight = FontWeight.SemiBold) },
            supportingContent = { Text(listOfNotNull(user.username, user.roleLabel.ifBlank { null }, user.department).joinToString(" · ")) },
            leadingContent = { Icon(Icons.Default.AccountCircle, null, tint = MaterialTheme.colorScheme.primary) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
            modifier = Modifier.clickable { onOpen(Page.Profile) }.padding(vertical = 4.dp),
        )
        HorizontalDivider()
        MenuItem("Ruangan", "Daftar ruangan, fasilitas, dan jadwalnya", Icons.Default.Place) { onOpen(Page.Rooms) }

        if (user.isAdmin) {
            SectionTitle("Admin")
            MenuItem("Semua Booking", "Booking hari ini yang berlangsung & akan datang", Icons.Default.CheckCircle) { onOpen(Page.TodayBookings) }
            MenuItem("Manajemen User", "Tambah, ubah, dan nonaktifkan akun", Icons.Default.Person) { onOpen(Page.Users) }
        }
        if (user.isSystemAdmin) {
            MenuItem("Pengaturan", "Nama aplikasi, jam operasional, dan aturan booking", Icons.Default.Settings) { onOpen(Page.Settings) }
        }

        HorizontalDivider(Modifier.padding(top = 8.dp))
        ListItem(
            headlineContent = { Text("Keluar", color = MaterialTheme.colorScheme.error) },
            leadingContent = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = MaterialTheme.colorScheme.error) },
            modifier = Modifier.clickable(onClick = onLogout),
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun MenuItem(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, null) },
        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
        modifier = Modifier.clickable(onClick = onClick),
    )
}
