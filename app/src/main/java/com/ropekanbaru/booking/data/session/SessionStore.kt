package com.ropekanbaru.booking.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ropekanbaru.booking.data.remote.ApiJson
import com.ropekanbaru.booking.data.remote.UserDto
import kotlinx.coroutines.flow.first

private val Context.sessionDataStore by preferencesDataStore(name = "session")

data class StoredSession(val token: String, val user: UserDto)

/**
 * Sesi login di perangkat: token (terenkripsi) dan data user terakhir.
 * Token juga disimpan di memori agar bisa dibaca cepat oleh setiap request API.
 */
class SessionStore(private val context: Context, private val cipher: TokenCipher) {

    @Volatile
    var token: String? = null
        private set

    suspend fun load(): StoredSession? {
        val prefs = context.sessionDataStore.data.first()
        val token = prefs[TOKEN]?.let(cipher::decrypt)
        val user = prefs[USER]?.let { runCatching { ApiJson.decodeFromString<UserDto>(it) }.getOrNull() }
        if (token == null || user == null) {
            clear()
            return null
        }
        this.token = token
        return StoredSession(token, user)
    }

    suspend fun save(token: String, user: UserDto) {
        context.sessionDataStore.edit {
            it[TOKEN] = cipher.encrypt(token)
            it[USER] = ApiJson.encodeToString(user)
        }
        this.token = token
    }

    suspend fun updateUser(user: UserDto) {
        context.sessionDataStore.edit { it[USER] = ApiJson.encodeToString(user) }
    }

    suspend fun clear() {
        token = null
        context.sessionDataStore.edit { it.clear() }
    }

    private companion object {
        val TOKEN = stringPreferencesKey("token")
        val USER = stringPreferencesKey("user")
    }
}
