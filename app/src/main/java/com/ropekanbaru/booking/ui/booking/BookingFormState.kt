package com.ropekanbaru.booking.ui.booking

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ropekanbaru.booking.data.CreateBookingResult
import com.ropekanbaru.booking.data.TimeSlots
import com.ropekanbaru.booking.data.isSelectable
import com.ropekanbaru.booking.data.remote.ApiErrors
import com.ropekanbaru.booking.data.remote.AvailabilityRoomDto
import com.ropekanbaru.booking.data.remote.CreateBookingRequest
import com.ropekanbaru.booking.data.remote.MrbsApi
import com.ropekanbaru.booking.data.remote.SettingsDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

/** Nilai awal form, mis. dari tombol "Pesan" di jadwal ruangan. */
data class BookingDefaults(val roomId: Long? = null, val date: LocalDate? = null)

/**
 * State & logika form Buat Booking. Dibuat ulang setiap form dibuka (terikat ke dialog).
 */
class BookingFormState(
    private val api: MrbsApi,
    private val scope: CoroutineScope,
    val settings: SettingsDto,
    val isAdmin: Boolean,
    defaults: BookingDefaults,
    private val today: LocalDate = LocalDate.now(),
    private val now: () -> LocalTime = { LocalTime.now() },
) {
    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var participants by mutableStateOf("2")
    var type by mutableStateOf("internal")
    var date by mutableStateOf(defaults.date?.takeIf { !it.isBefore(today) } ?: today)
        private set
    var start by mutableStateOf<String?>(null)
        private set
    var end by mutableStateOf<String?>(null)
        private set
    var roomId by mutableStateOf(defaults.roomId)
    var repeat by mutableStateOf(false)
        private set
    var repeatWeeks by mutableStateOf(minOf(4, settings.maxRepeatWeeks))
        private set
    var skipConflicts by mutableStateOf(false)

    var rooms by mutableStateOf<List<AvailabilityRoomDto>>(emptyList())
        private set
    var checking by mutableStateOf(false)
        private set
    var saving by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    val maxDate: LocalDate? get() = if (isAdmin) null else today.plusDays(settings.maxAdvanceDays.toLong())
    val startOptions: List<String> get() = TimeSlots.startOptions(settings, date, today, now())
    val endOptions: List<String> get() = start?.let { TimeSlots.endOptions(settings, it) } ?: emptyList()
    val weekOptions: List<Int> get() = (2..settings.maxRepeatWeeks).toList()
    val effectiveWeeks: Int get() = if (repeat) repeatWeeks else 1
    val lastDate: LocalDate get() = date.plusWeeks((effectiveWeeks - 1).toLong())
    val selectedRoom: AvailabilityRoomDto? get() = rooms.firstOrNull { it.id == roomId }

    private var availabilityJob: Job? = null

    init {
        // Hari ini sudah lewat jam operasional: tawarkan besok.
        if (startOptions.isEmpty() && date == today) date = today.plusDays(1)
        val preferred = if (date == today) startOptions.firstOrNull() else "09:00".takeIf { it in startOptions } ?: startOptions.firstOrNull()
        start = preferred
        end = preferred?.let { TimeSlots.defaultEnd(settings, it) }
        checkAvailability()
    }

    fun updateDate(value: LocalDate) {
        date = value
        if (start !in startOptions) updateStart(startOptions.firstOrNull()) else checkAvailability()
    }

    fun updateStart(value: String?) {
        start = value
        if (end !in endOptions) end = value?.let { TimeSlots.defaultEnd(settings, it) }
        checkAvailability()
    }

    fun updateEnd(value: String) {
        end = value
        checkAvailability()
    }

    fun updateParticipants(value: String) {
        participants = value.filter(Char::isDigit).take(4)
        checkAvailability()
    }

    fun updateRepeat(enabled: Boolean) {
        repeat = enabled
        checkAvailability()
    }

    fun updateRepeatWeeks(weeks: Int) {
        repeatWeeks = weeks
        checkAvailability()
    }

    /** Ketersediaan semua ruangan; dijeda 300 ms agar perubahan beruntun hanya memicu satu request. */
    fun checkAvailability() {
        val s = start ?: return
        val e = end ?: return
        availabilityJob?.cancel()
        availabilityJob = scope.launch {
            delay(300)
            checking = true
            runCatching {
                api.availability(
                    startAt = "$date $s",
                    endAt = "$date $e",
                    participants = participants.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                    repeatWeeks = effectiveWeeks.takeIf { it > 1 },
                ).data
            }.onSuccess { rooms = it }
                .onFailure { error = ApiErrors.message(it) }
            checking = false
        }
    }

    /** Mengembalikan pesan sukses, atau null bila gagal (lihat [error]). */
    suspend fun submit(): String? {
        error = when {
            title.isBlank() -> "Isi judul rapat."
            start == null || end == null -> "Pilih jam mulai dan selesai."
            roomId == null -> "Pilih ruangan."
            (participants.toIntOrNull() ?: 0) < 1 -> "Jumlah peserta minimal 1."
            else -> null
        }
        if (error != null) return null

        saving = true
        return runCatching {
            val response = api.createBooking(
                CreateBookingRequest(
                    roomId = roomId!!,
                    title = title.trim(),
                    description = description.trim().ifBlank { null },
                    startAt = "$date $start",
                    endAt = "$date $end",
                    participants = participants.toInt(),
                    type = type,
                    repeatWeeks = effectiveWeeks.takeIf { it > 1 },
                    skipConflicts = if (effectiveWeeks > 1) skipConflicts else null,
                ),
            )
            CreateBookingResult.from(response).message
        }.onFailure {
            error = ApiErrors.message(it)
            checkAvailability() // jadwal mungkin sudah berubah (mis. bentrok dengan booking baru orang lain)
        }.also { saving = false }.getOrNull()
    }

    fun isSelectable(room: AvailabilityRoomDto) = room.isSelectable(effectiveWeeks)
}
