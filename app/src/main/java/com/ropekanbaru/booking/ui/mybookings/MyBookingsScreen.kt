package com.ropekanbaru.booking.ui.mybookings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ropekanbaru.booking.data.remote.MrbsApi
import com.ropekanbaru.booking.ui.components.BookingListRow
import com.ropekanbaru.booking.ui.components.EmptyState
import com.ropekanbaru.booking.ui.components.ErrorCard
import com.ropekanbaru.booking.ui.components.SecondaryButton
import com.ropekanbaru.booking.ui.components.Tw
import com.ropekanbaru.booking.ui.components.WebCard
import com.ropekanbaru.booking.ui.components.rememberPagedLoader

private enum class MyTab(val label: String, val period: String?, val status: String, val empty: String) {
    Upcoming("Mendatang", "upcoming", "confirmed", "Tidak ada booking mendatang"),
    Past("Riwayat", "past", "confirmed", "Tidak ada data"),
    Cancelled("Dibatalkan", null, "cancelled", "Tidak ada data"),
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

    PullToRefreshBox(isRefreshing = list.refreshing, onRefresh = { list.reload(refresh = true) }, modifier = modifier.fillMaxSize()) {
        BoxWithConstraints {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(if (maxWidth < 600.dp) 16.dp else 24.dp)) {
                WebCard(Modifier.fillMaxWidth()) {
                    // Tab bergaris bawah seperti web
                    Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp)) {
                        MyTab.entries.forEachIndexed { i, t ->
                            val active = i == tabIndex
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { tabIndex = i }
                                    .semantics {
                                        role = Role.Tab
                                        selected = active
                                    }
                                    .padding(top = 12.dp),
                            ) {
                                Text(
                                    t.label,
                                    color = if (active) Tw.Indigo600 else Tw.Slate500,
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                )
                                Box(Modifier.padding(top = 10.dp).height(2.dp).width(if (active) 84.dp else 0.dp).background(Tw.Indigo600))
                            }
                        }
                    }
                    HorizontalDivider(color = Tw.Slate200)

                    when {
                        list.loading -> Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        !list.loaded -> Box(Modifier.padding(16.dp)) { ErrorCard(list.error ?: "Gagal memuat booking.", onRetry = { list.reload() }) }
                        list.items.isEmpty() -> EmptyState(Icons.Outlined.DateRange, tab.empty)
                        else -> {
                            list.items.forEachIndexed { i, b ->
                                if (i > 0) HorizontalDivider(color = Tw.Slate100)
                                BookingListRow(b, onClick = { onOpenBooking(b.id) })
                            }
                            list.error?.let { Box(Modifier.padding(16.dp)) { ErrorCard(it) } }
                            HorizontalDivider(color = Tw.Slate200)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                                Text(
                                    "${list.items.size} dari ${list.total} booking",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Tw.Slate500,
                                    modifier = Modifier.weight(1f),
                                )
                                if (list.hasMore) {
                                    SecondaryButton(if (list.loadingMore) "Memuat…" else "Muat lebih banyak", onClick = list::loadMore, enabled = !list.loadingMore)
                                }
                            }
                        }
                    }
                }
                Box(Modifier.height(16.dp))
            }
        }
    }
}
