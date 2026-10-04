package com.ropekanbaru.booking.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ropekanbaru.booking.data.remote.ApiErrors
import com.ropekanbaru.booking.data.remote.DashboardDto
import com.ropekanbaru.booking.data.remote.MrbsApi
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val dashboard: DashboardDto? = null,
    val error: String? = null,
)

class HomeViewModel(private val api: MrbsApi) : ViewModel() {

    var state by mutableStateOf(HomeUiState())
        private set

    init {
        load()
    }

    fun refresh() = load(refreshing = true)

    private fun load(refreshing: Boolean = false) {
        state = state.copy(loading = state.dashboard == null, refreshing = refreshing, error = null)
        viewModelScope.launch {
            runCatching { api.dashboard() }
                .onSuccess { state = HomeUiState(loading = false, dashboard = it) }
                .onFailure { state = state.copy(loading = false, refreshing = false, error = ApiErrors.message(it)) }
        }
    }
}
