package com.ropekanbaru.booking

import android.content.Context
import com.ropekanbaru.booking.data.AuthRepository
import com.ropekanbaru.booking.data.remote.ApiClient
import com.ropekanbaru.booking.data.remote.MrbsApi
import com.ropekanbaru.booking.data.remote.SettingsDto
import com.ropekanbaru.booking.data.session.SessionStore
import com.ropekanbaru.booking.data.session.TokenCipher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Dependency sederhana untuk seluruh aplikasi (tanpa framework DI).
 */
class AppContainer(context: Context) {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val sessionStore = SessionStore(context.applicationContext, TokenCipher())

    // authRepository dipakai oleh interceptor API (saat 401) dan sebaliknya; diisi di blok init.
    val authRepository: AuthRepository

    val api: MrbsApi = ApiClient.create(
        baseUrl = BuildConfig.API_BASE_URL,
        tokenProvider = { sessionStore.token },
        onUnauthorized = { authRepository.onUnauthorized() },
        debug = BuildConfig.DEBUG,
    )

    private val _settings = MutableStateFlow(SettingsDto())

    /** Nama aplikasi & aturan booking dari menu Pengaturan backend (publik). */
    val settings: StateFlow<SettingsDto> = _settings.asStateFlow()

    private val _bookingChanges = MutableStateFlow(0)

    /** Bertambah setiap ada booking dibuat/dibatalkan, agar layar lain memuat ulang datanya. */
    val bookingChanges: StateFlow<Int> = _bookingChanges.asStateFlow()

    fun notifyBookingsChanged() {
        _bookingChanges.value++
    }

    /** Dipanggil setelah System Admin menyimpan Pengaturan. */
    fun updateSettings(settings: SettingsDto) {
        _settings.value = settings
    }

    init {
        authRepository = AuthRepository(api, sessionStore, appScope)
    }

    fun start() {
        appScope.launch { authRepository.restore() }
        appScope.launch { runCatching { api.settings() }.onSuccess { _settings.value = it } }
    }
}
