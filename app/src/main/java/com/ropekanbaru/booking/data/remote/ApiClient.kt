package com.ropekanbaru.booking.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    fun create(
        baseUrl: String,
        tokenProvider: () -> String?,
        onUnauthorized: () -> Unit,
        debug: Boolean,
    ): MrbsApi {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val token = tokenProvider()
                val request = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .apply { if (token != null) header("Authorization", "Bearer $token") }
                    .build()
                val response = chain.proceed(request)

                // Token kedaluwarsa/dicabut, atau akun dinonaktifkan admin: keluarkan pengguna.
                val deactivated = response.code == 403 &&
                    response.peekBody(4096).string().contains("dinonaktifkan", ignoreCase = true)
                if (token != null && (response.code == 401 || deactivated)) onUnauthorized()

                response
            }
            .apply {
                // BASIC: hanya method, URL, status, dan durasi (token tidak ikut tercatat di log).
                if (debug) addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(ApiJson.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(MrbsApi::class.java)
    }
}
