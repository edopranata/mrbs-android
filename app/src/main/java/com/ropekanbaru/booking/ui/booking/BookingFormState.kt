package com.ropekanbaru.booking.ui.booking

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ropekanbaru.booking.data.CreateBookingResult
import com.ropekanbaru.booking.data.TimeSlots
import com.ropekanbaru.booking.data.isSelectable
import com.ropekanbaru.booking.data.remote.ApiErrors
import com.ropekanbaru.booking.data.remote.AvailabilityRoomDto
import com.ropekanbaru.booking.data.remote.BookingDto
import com.ropekanbaru.booking.data.remote.CreateBookingRequest
import com.ropekanbaru.booking.data.remote.MrbsApi
import com.ropekanbaru.booking.data.remote.OccurrenceDto
import com.ropekanbaru.booking.data.remote.SettingsDto
import com.ropekanbaru.booking.data.remote.UpdateBookingRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

/**
 * Nilai awal form, mis. dari tombol "Pesan" di jadwal ruangan atau slot yang dipilih di grid jadwal
 * ([start]/[end] "HH:mm"; tanpa [end] memakai durasi bawaan).
 * Dengan [editing], form mengubah booking tersebut (tanpa pengulangan mingguan).
 */
data class BookingDefaults(
    val roomId: Long? = null,
    val date: LocalDate? = null,
    val start: String? = null,
    val end: String? = null,
    val editing: BookingDto? = null,
)

/**
 * State & logika form Buat/Ubah Booking. Dibuat ulang setiap form dibuka (terikat ke dialog).
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
    val editing: BookingDto? = defaults.editing
    val isEditing: Boolean get() = editing != null

    var title by mutableStateOf(editing?.title.orEmpty())
    var description by mutableStateOf(editing?.description.orEmpty())
    var participants by mutableStateOf(editing?.participants?.toString() ?: "2")
    var type by mutableStateOf(editing?.type ?: "internal")
    var date by mutableStateOf(
        editing?.let { LocalDate.parse(it.date) } ?: defaults.date?.takeIf { !it.isBefore(today) } ?: today,
    )
        private set
    var start by mutableStateOf<String?>(null)
        private set
    var end by mutableStateOf<String?>(null)
        private set
    var roomId by mutableStateOf(editing?.roomId ?: editing?.room?.id ?: defaults.roomId)
        private set
    var repeat by mutableStateOf(false)
        private set
    var repeatWeeks by mutableStateOf(minOf(4, settings.maxRepeatWeeks))
        private set
    var skipConflicts by mutableStateOf(false)

    var rooms by mutableStateOf<List<AvailabilityRoomDto>>(emptyList())
        private set

    /** Pratinjau tiap tanggal booking mingguan untuk ruangan terpilih. */
    var occurrences by mutableStateOf<List<OccurrenceDto>>(emptyList())
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
        if (editing != null) {
            start = editing.startTime
            end = editing.endTime
        } else {
            // Hari ini sudah lewat jam operasional: tawarkan besok.
            if (startOptions.isEmpty() && date == today) date = today.plusDays(1)
            val preferred = defaults.start?.takeIf { it in startOptions }
                ?: if (date == today) startOptions.firstOrNull() else "09:00".takeIf { it in startOptions } ?: startOptions.firstOrNull()
            start = preferred
            end = defaults.end?.takeIf { preferred != null && it in TimeSlots.endOptions(settings, preferred) }
                ?: preferred?.let { TimeSlots.defaultEnd(settings, it) }
        }
        checkAvailability()
    }

    fun selectRoom(id: Long) {
        roomId = id
        if (effectiveWeeks > 1) checkAvailability() // perbarui pratinjau tanggal untuk ruangan ini
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
            val people = participants.toIntOrNull()?.coerceAtLeast(1) ?: 1
            runCatching {
                api.availability(
                    startAt = "$date $s",
                    endAt = "$date $e",
                    participants = people,
                    repeatWeeks = effectiveWeeks.takeIf { it > 1 },
                    ignoreBookingId = editing?.id,
                ).data
            }.onSuccess { rooms = it }
                .onFailure { error = ApiErrors.message(it) }
            val room = roomId
            occurrences = if (effectiveWeeks > 1 && room != null) {
                runCatching { api.occurrences(room, "$date $s", "$date $e", effectiveWeeks, people).data }.getOrDefault(emptyList())
            } else emptyList()
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
            if (editing != null) {
                api.updateBooking(
                    editing.id,
                    UpdateBookingRequest(
                        roomId = roomId!!,
                        title = title.trim(),
                        description = description.trim(),
                        startAt = "$date $start",
                        endAt = "$date $end",
                        participants = participants.toInt(),
                        type = type,
                    ),
                )
                return@runCatching "Booking berhasil diperbarui."
            }
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
