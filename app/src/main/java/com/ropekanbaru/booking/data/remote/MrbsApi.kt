package com.ropekanbaru.booking.data.remote

import kotlinx.serialization.json.JsonObject
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Endpoint REST backend MRBS (lihat README repository mrbs-backend). */
interface MrbsApi {

    // ---- Akun ----

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("auth/me")
    suspend fun me(): DataResponse<UserDto>

    @POST("auth/logout")
    suspend fun logout(): MessageResponse

    @PUT("auth/profile")
    suspend fun updateProfile(@Body body: ProfileRequest): DataResponse<UserDto>

    @PUT("auth/password")
    suspend fun changePassword(@Body body: PasswordRequest): MessageResponse

    // ---- Umum ----

    @GET("settings")
    suspend fun settings(): SettingsDto

    @GET("dashboard")
    suspend fun dashboard(): DashboardDto

    /** Satu hari (`date`) atau rentang (`from` + `to`, maks. 42 hari). */
    @GET("schedule")
    suspend fun schedule(
        @Query("date") date: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
    ): ScheduleDto

    // ---- Ruangan ----

    @GET("rooms")
    suspend fun rooms(): DataResponse<List<RoomDto>>

    @GET("rooms/availability")
    suspend fun availability(
        @Query("start_at") startAt: String,
        @Query("end_at") endAt: String,
        @Query("participants") participants: Int,
        @Query("repeat_weeks") repeatWeeks: Int? = null,
        @Query("ignore_booking_id") ignoreBookingId: Long? = null,
    ): DataResponse<List<AvailabilityRoomDto>>

    @POST("rooms")
    suspend fun createRoom(@Body body: RoomSaveRequest): DataResponse<RoomDto>

    @PUT("rooms/{id}")
    suspend fun updateRoom(@Path("id") id: Long, @Body body: RoomSaveRequest): DataResponse<RoomDto>

    @DELETE("rooms/{id}")
    suspend fun deleteRoom(@Path("id") id: Long): MessageResponse

    // ---- Booking ----

    /** `mine=1` untuk booking milik sendiri; `period` = upcoming | past; `status` = confirmed | cancelled. */
    @GET("bookings")
    suspend fun bookings(
        @Query("mine") mine: Int? = null,
        @Query("period") period: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
    ): PageResponse<BookingDto>

    @GET("bookings/today")
    suspend fun todayBookings(): TodayBookingsDto

    @GET("bookings/occurrences")
    suspend fun occurrences(
        @Query("room_id") roomId: Long,
        @Query("start_at") startAt: String,
        @Query("end_at") endAt: String,
        @Query("repeat_weeks") repeatWeeks: Int,
        @Query("participants") participants: Int,
    ): DataResponse<List<OccurrenceDto>>

    /** Satu booking: `data` berupa objek; berulang mingguan: `data` berupa daftar + `skipped`. */
    @POST("bookings")
    suspend fun createBooking(@Body body: CreateBookingRequest): JsonObject

    @GET("bookings/{id}")
    suspend fun booking(@Path("id") id: Long): BookingDetailResponse

    @PUT("bookings/{id}")
    suspend fun updateBooking(@Path("id") id: Long, @Body body: UpdateBookingRequest): DataResponse<BookingDto>

    @POST("bookings/{id}/cancel")
    suspend fun cancelBooking(@Path("id") id: Long, @Body body: CancelRequest): CancelResponse

    /** Hapus permanen (khusus admin). */
    @DELETE("bookings/{id}")
    suspend fun deleteBooking(@Path("id") id: Long): MessageResponse

    // ---- Manajemen user (admin) ----

    @GET("users")
    suspend fun users(
        @Query("search") search: String? = null,
        @Query("role") role: String? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
    ): PageResponse<UserDto>

    @POST("users")
    suspend fun createUser(@Body body: UserSaveRequest): DataResponse<UserDto>

    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: Long, @Body body: UserSaveRequest): DataResponse<UserDto>

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: Long): MessageResponse

    // ---- Pengaturan (System Admin) ----

    @GET("settings/manage")
    suspend fun manageSettings(): ManageSettingsDto

    /** Hanya kolom yang berubah (lihat SettingsRules.changes), agar nilai lain tetap mengikuti default. */
    @PUT("settings")
    suspend fun updateSettings(@Body body: JsonObject): ManageSettingsDto

    @DELETE("settings")
    suspend fun resetSettings(): ManageSettingsDto
}
