package com.ropekanbaru.booking.ui.schedule

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ropekanbaru.booking.data.CalendarRange
import com.ropekanbaru.booking.data.ScheduleMode
import com.ropekanbaru.booking.data.remote.ApiErrors
import com.ropekanbaru.booking.data.remote.MrbsApi
import com.ropekanbaru.booking.data.remote.ScheduleDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ScheduleUiState(
    val mode: ScheduleMode = ScheduleMode.Day,
    val date: LocalDate = LocalDate.now(),
    /** Ruangan untuk tampilan minggu (wajib) & bulan (null = semua ruangan). */
    val roomId: Long? = null,
    /** "Sorot booking saya": booking orang lain dipudarkan. */
    val highlightMine: Boolean = false,
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val schedule: ScheduleDto? = null,
    val error: String? = null,
) {
    val range: ClosedRange<LocalDate> get() = CalendarRange.range(mode, date)
}

class ScheduleViewModel(private val api: MrbsApi, bookingChanges: StateFlow<Int>) : ViewModel() {

    var state by mutableStateOf(ScheduleUiState())
        private set

    private var job: Job? = null

    init {
        // Data dimuat saat halaman dibuka (lihat enter), bukan saat aplikasi mulai.
        viewModelScope.launch { bookingChanges.drop(1).collect { load() } }
    }

    /** Dipanggil setiap kali halaman Jadwal dibuka: selalu ambil data terbaru dari server. */
    fun enter() = load(refreshing = state.schedule != null)

    fun setDate(date: LocalDate) {
        if (date == state.date) return
        val before = state.range
        state = state.copy(date = date)
        // Pindah tanggal di dalam minggu/bulan yang sama tidak perlu memuat ulang.
        if (state.range != before) load()
    }

    fun shift(steps: Long) = setDate(CalendarRange.shift(state.mode, state.date, steps))

    fun setMode(mode: ScheduleMode) {
        if (mode == state.mode) return
        state = state.copy(mode = mode)
        load()
    }

    /** Ganti ruangan (tanpa memuat ulang: data jadwal sudah memuat semua ruangan). */
    fun setRoom(roomId: Long?) {
        state = state.copy(roomId = roomId)
    }

    /** Buka jadwal mingguan satu ruangan (dari halaman Ruangan). */
    fun showRoomWeek(roomId: Long) {
        // Data dimuat oleh enter() saat halaman Jadwal dibuka sesudahnya.
        state = state.copy(mode = ScheduleMode.Week, roomId = roomId)
    }

    fun toggleHighlight() {
        state = state.copy(highlightMine = !state.highlightMine)
    }

    fun refresh() = load(refreshing = true)

    private fun load(refreshing: Boolean = false) {
        val range = state.range
        val key = range.start.toString() to range.endInclusive.toString()
        // Data rentang sebelumnya tidak ditampilkan untuk rentang baru.
        val sameRange = state.schedule?.let { (it.from ?: it.date) to (it.to ?: it.date) } == key
        state = state.copy(loading = !sameRange, refreshing = refreshing, error = null, schedule = state.schedule.takeIf { sameRange })
        job?.cancel()
        job = viewModelScope.launch {
            runCatching { api.schedule(from = key.first, to = key.second) }
                .onSuccess { if (state.range == range) state = state.copy(loading = false, refreshing = false, schedule = it) }
                .onFailure { if (it !is CancellationException && state.range == range) state = state.copy(loading = false, refreshing = false, error = ApiErrors.message(it)) }
        }
    }
}
