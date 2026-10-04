package com.ropekanbaru.booking

import com.ropekanbaru.booking.data.remote.ApiJson
import com.ropekanbaru.booking.data.remote.DashboardDto
import com.ropekanbaru.booking.data.remote.LoginResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Memastikan respons JSON backend (contoh nyata) terbaca, termasuk field yang tidak dikenal. */
class ApiModelsTest {

    @Test
    fun `respons login terbaca`() {
        val json = """
            {"token":"1|abc","user":{"id":7,"name":"System Administrator","username":"sysadmin",
             "email":"sysadmin@kantor.test","role":"system_admin","role_label":"System Admin",
             "department":"IT","phone":null,"is_active":true,"created_at":"2026-10-03 13:20:00"}}
        """.trimIndent()
        val res = ApiJson.decodeFromString<LoginResponse>(json)
        assertEquals("1|abc", res.token)
        assertEquals("sysadmin", res.user.username)
        assertTrue(res.user.isAdmin)
    }

    @Test
    fun `dashboard terbaca dan field tambahan diabaikan`() {
        val json = """
            {"stats":{"rooms_total":7,"rooms_in_use":2,"bookings_today":5,"my_upcoming":1},
             "ongoing":[],"today":[],
             "my_upcoming":[{"id":12,"title":"Rapat Mingguan","date":"2026-10-05","start_time":"09:00",
               "end_time":"10:00","status":"confirmed","type":"internal","is_ongoing":false,
               "is_recurring":true,"series_id":"x","room":{"id":1,"code":"R101","name":"Ruang Anggrek","floor":1,"color":"#4f46e5"},
               "can":{"update":true,"cancel":true}}],
             "admin":{"users_total":3}}
        """.trimIndent()
        val dashboard = ApiJson.decodeFromString<DashboardDto>(json)
        assertEquals(2, dashboard.stats.roomsInUse)
        assertEquals("Ruang Anggrek", dashboard.myUpcoming.single().room?.name)
        assertTrue(dashboard.myUpcoming.single().isRecurring)
    }
}
