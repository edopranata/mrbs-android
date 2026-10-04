package com.ropekanbaru.booking.ui.mybookings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ropekanbaru.booking.data.remote.MrbsApi
import com.ropekanbaru.booking.ui.components.CardColumns
import com.ropekanbaru.booking.ui.components.FullSpan
import com.ropekanbaru.booking.ui.components.BookingCard
import com.ropekanbaru.booking.ui.components.EmptyText
import com.ropekanbaru.booking.ui.components.ErrorCard
import com.ropekanbaru.booking.ui.components.rememberPagedLoader

private enum class MyTab(val label: String, val period: String?, val status: String, val empty: String) {
    Upcoming("Mendatang", "upcoming", "confirmed", "Tidak ada booking mendatang."),
    Past("Riwayat", "past", "confirmed", "Belum ada riwayat booking."),
    Cancelled("Dibatalkan", null, "cancelled", "Tidak ada booking yang dibatalkan."),
}

/** Booking milik user: mendatang, riwayat, dan yang dibatalkan (sama dengan menu web). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsContent(
    api: MrbsApi,
    bookingChanges: Int,
    onOpenBooking: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tab = MyTab.entries[tabIndex]
    val list = rememberPagedLoader(tab) { page ->
        api.bookings(mine = 1, period = tab.period, status = tab.status, page = page)
    }
    // Muat ulang setelah booking dibuat/diubah/dibatalkan.
    LaunchedEffect(bookingChanges) { if (list.loaded) list.reload() }

    Column(modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = tabIndex, containerColor = MaterialTheme.colorScheme.surface) {
            MyTab.entries.forEachIndexed { i, t ->
                Tab(selected = i == tabIndex, onClick = { tabIndex = i }, text = { Text(t.label) })
            }
        }
        PullToRefreshBox(isRefreshing = list.refreshing, onRefresh = { list.reload(refresh = true) }, modifier = Modifier.fillMaxSize()) {
            when {
                list.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                !list.loaded -> Column(Modifier.padding(16.dp)) {
                    ErrorCard(list.error ?: "Gagal memuat booking.", onRetry = { list.reload() })
                }
                else -> LazyVerticalGrid(
                    columns = CardColumns,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item(span = FullSpan) {
                        Text(
                            "${list.total} booking",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (list.items.isEmpty()) item(span = FullSpan) { EmptyText(tab.empty) }
                    items(list.items, key = { it.id }) { BookingCard(it, onClick = { onOpenBooking(it.id) }) }
                    list.error?.let { item(span = FullSpan) { ErrorCard(it) } }
                    if (list.hasMore) {
                        item(span = FullSpan) {
                            OutlinedButton(onClick = list::loadMore, enabled = !list.loadingMore, modifier = Modifier.fillMaxWidth()) {
                                Text(if (list.loadingMore) "Memuat…" else "Muat lebih banyak")
                            }
                        }
                    }
                }
            }
        }
    }
}
