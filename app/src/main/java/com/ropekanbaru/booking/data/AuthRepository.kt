package com.ropekanbaru.booking.data

import android.os.Build
import com.ropekanbaru.booking.data.remote.LoginRequest
import com.ropekanbaru.booking.data.remote.MrbsApi
import com.ropekanbaru.booking.data.remote.UserDto
import com.ropekanbaru.booking.data.session.SessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface SessionState {
    /** Sesi tersimpan sedang dibaca (splash screen tetap tampil). */
    data object Loading : SessionState
    data object LoggedOut : SessionState
    data class LoggedIn(val user: UserDto) : SessionState
}

class AuthRepository(
    private val api: MrbsApi,
    private val store: SessionStore,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow<SessionState>(SessionState.Loading)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    /** Dipanggil sekali saat aplikasi dibuka. */
    suspend fun restore() {
        val session = store.load()
        if (session == null) {
            _state.value = SessionState.LoggedOut
            return
        }
        // Tampilkan aplikasi segera dengan data tersimpan, lalu perbarui dari server.
        _state.value = SessionState.LoggedIn(session.user)
        try {
            val user = api.me().data
            store.updateUser(user)
            _state.value = SessionState.LoggedIn(user)
        } catch (e: HttpException) {
            // Hanya keluar bila server menolak token; error lain tidak me-logout.
            if (e.code() == 401 || e.code() == 403) signOutLocally()
        } catch (_: IOException) {
            // Offline / server tidak terjangkau: tetap login dengan data tersimpan.
        }
    }

    suspend fun login(username: String, password: String): Result<UserDto> = runCatching {
        val response = api.login(LoginRequest(username.trim(), password, deviceName()))
        store.save(response.token, response.user)
        _state.value = SessionState.LoggedIn(response.user)
        response.user
    }

    suspend fun logout() {
        runCatching { api.logout() } // tetap keluar walau server tidak terjangkau
        signOutLocally()
    }

    /** Dipanggil interceptor API saat token ditolak server. */
    fun onUnauthorized() {
        scope.launch { signOutLocally() }
    }

    private suspend fun signOutLocally() {
        store.clear()
        _state.value = SessionState.LoggedOut
    }

    private fun deviceName() = "Android ${Build.MANUFACTURER} ${Build.MODEL}".take(100)
}
