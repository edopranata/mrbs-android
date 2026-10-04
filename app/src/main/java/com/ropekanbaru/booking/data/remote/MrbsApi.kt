package com.ropekanbaru.booking.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

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
}
