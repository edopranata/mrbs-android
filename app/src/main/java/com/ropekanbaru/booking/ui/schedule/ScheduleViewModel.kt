package com.ropekanbaru.booking.ui.schedule

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ropekanbaru.booking.data.remote.ApiErrors
import com.ropekanbaru.booking.data.remote.MrbsApi
import com.ropekanbaru.booking.data.remote.ScheduleDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ScheduleUiState(
    val date: LocalDate = LocalDate.now(),
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val schedule: ScheduleDto? = null,
    val error: String? = null,
)

class ScheduleViewModel(private val api: MrbsApi, bookingChanges: StateFlow<Int>) : ViewModel() {

    var state by mutableStateOf(ScheduleUiState())
        private set

    private var job: Job? = null

    init {
        load()
        viewModelScope.launch { bookingChanges.drop(1).collect { load() } }
    }

    fun setDate(date: LocalDate) {
        if (date == state.date) return
        state = state.copy(date = date)
        load()
    }

    fun shift(days: Long) = setDate(state.date.plusDays(days))

    fun refresh() = load(refreshing = true)

    private fun load(refreshing: Boolean = false) {
        val date = state.date
        // Data tanggal sebelumnya tidak ditampilkan untuk tanggal baru.
        val sameDate = state.schedule?.date == date.toString()
        state = state.copy(loading = !sameDate, refreshing = refreshing, error = null, schedule = state.schedule.takeIf { sameDate })
        job?.cancel()
        job = viewModelScope.launch {
            runCatching { api.schedule(date.toString()) }
                .onSuccess { if (state.date == date) state = state.copy(loading = false, refreshing = false, schedule = it) }
                .onFailure { if (state.date == date) state = state.copy(loading = false, refreshing = false, error = ApiErrors.message(it)) }
        }
    }
}
