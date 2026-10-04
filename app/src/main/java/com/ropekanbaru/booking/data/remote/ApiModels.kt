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
) {
    val isAdmin: Boolean get() = role == "admin" || role == "system_admin"
}

@Serializable
data class SettingsDto(
    @SerialName("app_name") val appName: String = "MRBS",
    @SerialName("app_subtitle") val appSubtitle: String? = "Booking Ruang Rapat",
    @SerialName("open_time") val openTime: String = "07:00",
    @SerialName("close_time") val closeTime: String = "20:00",
    @SerialName("slot_minutes") val slotMinutes: Int = 30,
)

@Serializable
data class DashboardDto(
    val stats: DashboardStats = DashboardStats(),
    val ongoing: List<BookingDto> = emptyList(),
    @SerialName("my_upcoming") val myUpcoming: List<BookingDto> = emptyList(),
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
    val room: RoomRef? = null,
)

@Serializable
data class RoomRef(
    val id: Long,
    val name: String,
    val floor: Int,
    val code: String? = null,
    val color: String? = null,
)

/** Bentuk error Laravel: `{ "message": "...", "errors": { "field": ["pesan"] } }`. */
@Serializable
data class ApiErrorBody(
    val message: String? = null,
    val errors: Map<String, List<String>> = emptyMap(),
)
