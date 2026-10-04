package com.ropekanbaru.booking.ui.admin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ropekanbaru.booking.data.remote.BookingDto
import com.ropekanbaru.booking.data.remote.MrbsApi
import com.ropekanbaru.booking.ui.components.CardColumns
import com.ropekanbaru.booking.ui.components.FullSpan
import com.ropekanbaru.booking.ui.components.BookingCard
import com.ropekanbaru.booking.ui.components.EmptyText
import com.ropekanbaru.booking.ui.components.ErrorCard
import com.ropekanbaru.booking.ui.components.longDate
import com.ropekanbaru.booking.ui.components.rememberLoader
import kotlinx.coroutines.delay

/**
 * Pantauan admin: booking hari ini yang sedang berlangsung dan yang akan datang
 * (yang sudah selesai atau dibatalkan tidak ditampilkan), sama dengan menu Semua Booking di web.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayBookingsContent(
    api: MrbsApi,
    bookingChanges: Int,
    currentUserId: Long,
    onOpenBooking: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = rememberLoader { api.todayBookings() }
    var search by rememberSaveable { mutableStateOf("") }
    var roomId by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(bookingChanges) { if (today.data != null) today.load() }
    // Perbarui otomatis setiap menit agar booking yang selesai hilang & yang mulai pindah ke "berlangsung".
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            today.load()
        }
    }

    PullToRefreshBox(isRefreshing = today.refreshing, onRefresh = { today.load(refresh = true) }, modifier = modifier.fillMaxSize()) {
        val data = today.data
        when {
            today.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            data == null -> Column(Modifier.padding(16.dp)) { ErrorCard(today.error ?: "Gagal memuat booking.", onRetry = { today.load() }) }
            else -> {
                val all = data.ongoing + data.upcoming
                val rooms = all.mapNotNull { it.room }.distinctBy { it.id }.sortedWith(compareBy({ it.floor }, { it.name }))
                val term = search.trim()
                val matches = { b: BookingDto ->
                    (roomId == null || b.room?.id == roomId) && (
                        term.isEmpty() || listOfNotNull(b.title, b.user?.name, b.room?.name).any { it.contains(term, ignoreCase = true) }
                        )
                }
                val ongoing = data.ongoing.filter(matches)
                val upcoming = data.upcoming.filter(matches)

                LazyVerticalGrid(
                    columns = CardColumns,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item(span = FullSpan) {
                        Text(
                            "Booking Hari Ini · ${longDate()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    item(span = FullSpan) {
                        OutlinedTextField(
                            value = search,
                            onValueChange = { search = it },
                            placeholder = { Text("Cari rapat, pemesan, atau ruangan…") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (rooms.size > 1) {
                        item(span = FullSpan) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                FilterChip(selected = roomId == null, onClick = { roomId = null }, label = { Text("Semua ruangan") })
                                rooms.forEach { room ->
                                    FilterChip(selected = roomId == room.id, onClick = { roomId = room.id }, label = { Text(room.name) })
                                }
                            }
                        }
                    }
                    today.error?.let { item(span = FullSpan) { ErrorCard(it) } }

                    item(span = FullSpan) { Heading("Sedang Berlangsung (${ongoing.size})") }
                    if (ongoing.isEmpty()) item(span = FullSpan) { EmptyText("Tidak ada rapat yang sedang berlangsung.") }
                    items(ongoing, key = { "on-${it.id}" }) {
                        BookingCard(it, onClick = { onOpenBooking(it.id) }, showUser = true, mine = it.userId == currentUserId)
                    }

                    item(span = FullSpan) { Heading("Akan Datang (${upcoming.size})") }
                    if (upcoming.isEmpty()) item(span = FullSpan) { EmptyText("Tidak ada booking lagi hari ini.") }
                    items(upcoming, key = { "up-${it.id}" }) {
                        BookingCard(it, onClick = { onOpenBooking(it.id) }, showUser = true, mine = it.userId == currentUserId)
                    }
                }
            }
        }
    }
}

@Composable
private fun Heading(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
}
