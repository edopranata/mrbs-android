package com.ropekanbaru.booking.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ropekanbaru.booking.data.AuthRepository
import com.ropekanbaru.booking.data.remote.ApiErrors
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
)

class LoginViewModel(private val auth: AuthRepository) : ViewModel() {

    var state by mutableStateOf(LoginUiState())
        private set

    fun onUsernameChange(value: String) {
        state = state.copy(username = value, error = null)
    }

    fun onPasswordChange(value: String) {
        state = state.copy(password = value, error = null)
    }

    fun submit() {
        if (state.loading) return
        if (state.username.isBlank() || state.password.isEmpty()) {
            state = state.copy(error = "Isi username dan password.")
            return
        }
        state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            auth.login(state.username, state.password)
                .onSuccess { state = LoginUiState() }
                .onFailure { state = state.copy(loading = false, error = ApiErrors.message(it)) }
        }
    }
}
