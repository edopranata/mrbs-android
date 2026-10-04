package com.ropekanbaru.booking.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ropekanbaru.booking.data.remote.ApiErrors
import com.ropekanbaru.booking.data.remote.DashboardDto
import com.ropekanbaru.booking.data.remote.MrbsApi
import com.ropekanbaru.booking.data.remote.RoomScheduleDto
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val dashboard: DashboardDto? = null,
    /** Jadwal hari ini per ruangan, untuk status ruangan saat ini. */
    val rooms: List<RoomScheduleDto> = emptyList(),
    val error: String? = null,
)

class HomeViewModel(private val api: MrbsApi, bookingChanges: StateFlow<Int>) : ViewModel() {

    var state by mutableStateOf(HomeUiState())
        private set

    init {
        load()
        // Muat ulang setelah booking dibuat/dibatalkan dari layar lain.
        viewModelScope.launch { bookingChanges.drop(1).collect { load() } }
    }

    fun refresh() = load(refreshing = true)

    private fun load(refreshing: Boolean = false) {
        state = state.copy(loading = state.dashboard == null, refreshing = refreshing, error = null)
        viewModelScope.launch {
            runCatching {
                // coroutineScope: bila salah satu gagal (mis. server tidak terjangkau), errornya
                // dilempar ke runCatching ini, tidak merambat ke viewModelScope dan membuat force close.
                coroutineScope {
                    val dashboard = async { api.dashboard() }
                    val schedule = async { api.schedule() }
                    dashboard.await() to schedule.await().rooms
                }
            }
                .onSuccess { (dashboard, rooms) -> state = HomeUiState(loading = false, dashboard = dashboard, rooms = rooms) }
                .onFailure { state = state.copy(loading = false, refreshing = false, error = ApiErrors.message(it)) }
        }
    }
}
