package com.ropekanbaru.booking.data.remote

import kotlinx.serialization.json.Json

/** Konfigurasi JSON bersama: field baru dari server tidak membuat aplikasi lama error. */
val ApiJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    coerceInputValues = true
}
