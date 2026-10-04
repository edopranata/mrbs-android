package com.ropekanbaru.booking.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
    @SerialName("device_name") val deviceName: String,
)

@Serializable
data class LoginResponse(val token: String, val user: UserDto)

/** Respons Laravel API Resource: `{ "data": ... }`. */
@Serializable
data class DataResponse<T>(val data: T)

@Serializable
data class MessageResponse(val message: String? = null)

@Serializable
data class UserDto(
    val id: Long,
    val name: String,
    val username: String,
    val email: String? = null,
    val role: String,
    @SerialName("role_label") val roleLabel: String = "",
    val department: String? = null,
    val phone: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("bookings_count") val bookingsCount: Int? = null,
) {
    val isAdmin: Boolean get() = role == "admin" || role == "system_admin"
    val isSystemAdmin: Boolean get() = role == "system_admin"
}

@Serializable
data class SettingsDto(
    @SerialName("app_name") val appName: String = "MRBS",
    @SerialName("app_subtitle") val appSubtitle: String? = "Booking Ruang Rapat",
    @SerialName("open_time") val openTime: String = "07:00",
    @SerialName("close_time") val closeTime: String = "20:00",
    @SerialName("slot_minutes") val slotMinutes: Int = 30,
    @SerialName("min_duration") val minDuration: Int = 30,
    @SerialName("max_duration") val maxDuration: Int = 480,
    @SerialName("max_advance_days") val maxAdvanceDays: Int = 60,
    @SerialName("max_repeat_weeks") val maxRepeatWeeks: Int = 8,
)

@Serializable
data class DashboardDto(
    val stats: DashboardStats = DashboardStats(),
    val ongoing: List<BookingDto> = emptyList(),
    val today: List<BookingDto> = emptyList(),
    @SerialName("my_upcoming") val myUpcoming: List<BookingDto> = emptyList(),
    /** Hanya untuk Admin / System Admin. */
    val admin: DashboardAdmin? = null,
)

@Serializable
data class DashboardAdmin(
    @SerialName("users_total") val usersTotal: Int = 0,
    @SerialName("users_active") val usersActive: Int = 0,
    @SerialName("bookings_this_month") val bookingsThisMonth: Int = 0,
    @SerialName("cancelled_this_month") val cancelledThisMonth: Int = 0,
    @SerialName("room_usage") val roomUsage: List<RoomUsage> = emptyList(),
)

@Serializable
data class RoomUsage(
    val id: Long,
    val name: String,
    val floor: Int,
    val color: String? = null,
    @SerialName("bookings_count") val bookingsCount: Int = 0,
)

@Serializable
data class DashboardStats(
    @SerialName("rooms_total") val roomsTotal: Int = 0,
    @SerialName("rooms_in_use") val roomsInUse: Int = 0,
    @SerialName("bookings_today") val bookingsToday: Int = 0,
    @SerialName("my_upcoming") val myUpcoming: Int = 0,
)

@Serializable
data class BookingDto(
    val id: Long,
    val title: String,
    val date: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    val status: String = "confirmed",
    val type: String = "internal",
    @SerialName("is_ongoing") val isOngoing: Boolean = false,
    @SerialName("is_recurring") val isRecurring: Boolean = false,
    @SerialName("has_ended") val hasEnded: Boolean = false,
    val description: String? = null,
    val participants: Int = 1,
    @SerialName("type_label") val typeLabel: String? = null,
    @SerialName("status_label") val statusLabel: String? = null,
    @SerialName("user_id") val userId: Long? = null,
    @SerialName("room_id") val roomId: Long? = null,
    @SerialName("series_id") val seriesId: String? = null,
    @SerialName("cancel_reason") val cancelReason: String? = null,
    @SerialName("cancelled_by") val cancelledBy: String? = null,
    val room: RoomRef? = null,
    val user: UserRef? = null,
    val can: BookingPermissions = BookingPermissions(),
) {
    val isCancelled: Boolean get() = status == "cancelled"
}

@Serializable
data class UserRef(val id: Long, val name: String, val department: String? = null)

@Serializable
data class BookingPermissions(val update: Boolean = false, val cancel: Boolean = false)

@Serializable
data class BookingDetailResponse(val data: BookingDto, val series: SeriesInfo? = null)

/** Posisi booking dalam seri berulang mingguan. */
@Serializable
data class SeriesInfo(
    val total: Int,
    val position: Int,
    @SerialName("following_cancellable") val followingCancellable: Int = 1,
)

@Serializable
data class CancelRequest(val reason: String? = null, val scope: String = "single")

@Serializable
data class CancelResponse(val data: BookingDto, @SerialName("cancelled_count") val cancelledCount: Int = 1)

@Serializable
data class CreateBookingRequest(
    @SerialName("room_id") val roomId: Long,
    val title: String,
    val description: String? = null,
    @SerialName("start_at") val startAt: String,
    @SerialName("end_at") val endAt: String,
    val participants: Int,
    val type: String,
    @SerialName("repeat_weeks") val repeatWeeks: Int? = null,
    @SerialName("skip_conflicts") val skipConflicts: Boolean? = null,
)

/** Ubah booking (tanpa pengulangan; satu booking saja). */
@Serializable
data class UpdateBookingRequest(
    @SerialName("room_id") val roomId: Long,
    val title: String,
    val description: String,
    @SerialName("start_at") val startAt: String,
    @SerialName("end_at") val endAt: String,
    val participants: Int,
    val type: String,
)

/** Pratinjau satu tanggal booking mingguan. */
@Serializable
data class OccurrenceDto(
    val date: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    val available: Boolean,
    val reason: String? = null,
)

@Serializable
data class SkippedDate(val date: String, val reason: String? = null)

/** Jadwal ruangan aktif beserta booking pada satu tanggal atau rentang `from`–`to`. */
@Serializable
data class ScheduleDto(
    val date: String,
    val from: String? = null,
    val to: String? = null,
    @SerialName("open_time") val openTime: String = "07:00",
    @SerialName("close_time") val closeTime: String = "20:00",
    val rooms: List<RoomScheduleDto> = emptyList(),
)

@Serializable
data class RoomScheduleDto(
    val id: Long,
    val code: String? = null,
    val name: String,
    val floor: Int,
    val capacity: Int = 0,
    val color: String? = null,
    val bookings: List<BookingDto> = emptyList(),
)

/** Ketersediaan ruangan untuk rentang waktu tertentu (opsional berulang mingguan). */
@Serializable
data class AvailabilityRoomDto(
    val id: Long,
    val name: String,
    val floor: Int,
    val capacity: Int = 0,
    val color: String? = null,
    val available: Boolean = false,
    @SerialName("fits_capacity") val fitsCapacity: Boolean = true,
    @SerialName("conflict_dates") val conflictDates: List<String> = emptyList(),
    val conflicts: List<ConflictDto> = emptyList(),
)

@Serializable
data class ConflictDto(
    val title: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    val user: String? = null,
    val date: String? = null,
)

@Serializable
data class RoomRef(
    val id: Long,
    val name: String,
    val floor: Int,
    val code: String? = null,
    val color: String? = null,
)

/** Ruangan lengkap (halaman Ruangan & kelola ruangan). */
@Serializable
data class RoomDto(
    val id: Long,
    val code: String = "",
    val name: String,
    val floor: Int,
    val capacity: Int = 0,
    val facilities: List<String> = emptyList(),
    val description: String? = null,
    val color: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
)

/** Tambah/ubah ruangan. String kosong dikirim agar kolom bisa dikosongkan (Laravel → null). */
@Serializable
data class RoomSaveRequest(
    val code: String,
    val name: String,
    val floor: Int,
    val capacity: Int,
    val facilities: List<String>,
    val description: String,
    val color: String,
    @SerialName("is_active") val isActive: Boolean,
)

/** Respons daftar Laravel yang dipaginasi. */
@Serializable
data class PageResponse<T>(val data: List<T>, val meta: PageMeta = PageMeta())

@Serializable
data class PageMeta(
    @SerialName("current_page") val currentPage: Int = 1,
    @SerialName("last_page") val lastPage: Int = 1,
    val total: Int = 0,
)

/** Pantauan admin: booking hari ini yang sedang berlangsung & akan datang. */
@Serializable
data class TodayBookingsDto(
    val date: String,
    val ongoing: List<BookingDto> = emptyList(),
    val upcoming: List<BookingDto> = emptyList(),
)

@Serializable
data class ProfileRequest(val name: String, val department: String, val phone: String)

@Serializable
data class PasswordRequest(
    @SerialName("current_password") val currentPassword: String,
    val password: String,
    @SerialName("password_confirmation") val passwordConfirmation: String,
)

/** Tambah/ubah user. `password` null (tidak dikirim) = tidak diubah. */
@Serializable
data class UserSaveRequest(
    val name: String,
    val username: String,
    val email: String,
    val password: String?,
    val role: String,
    val department: String,
    val phone: String,
    @SerialName("is_active") val isActive: Boolean,
)

/** Halaman Pengaturan: nilai berlaku, nilai default (.env), dan kunci yang diubah. */
@Serializable
data class ManageSettingsDto(
    val values: SettingsDto,
    val defaults: SettingsDto,
    val overridden: List<String> = emptyList(),
)

/** Bentuk error Laravel: `{ "message": "...", "errors": { "field": ["pesan"] } }`. */
@Serializable
data class ApiErrorBody(
    val message: String? = null,
    val errors: Map<String, List<String>> = emptyMap(),
)
