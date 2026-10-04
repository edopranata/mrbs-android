package com.ropekanbaru.booking.data.remote

import kotlinx.serialization.json.JsonObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Endpoint REST backend MRBS (lihat README repository mrbs-backend). */
interface MrbsApi {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("auth/me")
    suspend fun me(): DataResponse<UserDto>

    @POST("auth/logout")
    suspend fun logout(): MessageResponse

    @GET("settings")
    suspend fun settings(): SettingsDto

    @GET("dashboard")
    suspend fun dashboard(): DashboardDto

    @GET("schedule")
    suspend fun schedule(@Query("date") date: String): ScheduleDto

    @GET("rooms/availability")
    suspend fun availability(
        @Query("start_at") startAt: String,
        @Query("end_at") endAt: String,
        @Query("participants") participants: Int,
        @Query("repeat_weeks") repeatWeeks: Int? = null,
    ): DataResponse<List<AvailabilityRoomDto>>

    /** Satu booking: `data` berupa objek; berulang mingguan: `data` berupa daftar + `skipped`. */
    @POST("bookings")
    suspend fun createBooking(@Body body: CreateBookingRequest): JsonObject

    @GET("bookings/{id}")
    suspend fun booking(@Path("id") id: Long): BookingDetailResponse

    @POST("bookings/{id}/cancel")
    suspend fun cancelBooking(@Path("id") id: Long, @Body body: CancelRequest): CancelResponse
}
