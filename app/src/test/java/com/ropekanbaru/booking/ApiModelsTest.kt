package com.ropekanbaru.booking

import com.ropekanbaru.booking.data.remote.ApiJson
import com.ropekanbaru.booking.data.remote.AuditLogDto
import com.ropekanbaru.booking.data.remote.DashboardDto
import com.ropekanbaru.booking.data.remote.LoginResponse
import com.ropekanbaru.booking.data.remote.PageResponse
import kotlinx.serialization.json.JsonPrimitive
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

    @Test
    fun `respons audit log terbaca termasuk meta kosong dan nilai angka`() {
        val json = """
            {"data":[
              {"id":3,"created_at":"2026-10-21T10:59:57","action":"room.updated","action_label":"Ubah ruangan","category":"room",
               "category_label":"Ruangan","description":"Mengubah ruangan BRILIAN ROOM","user_name":"Ani","user":{"id":1,"name":"Ani","username":"ani"},
               "subject_type":"room","subject_id":2,"changes":[{"field":"capacity","label":"Kapasitas","old":10,"new":12}],
               "meta":{},"ip_address":"127.0.0.1","user_agent":"curl"},
              {"id":2,"created_at":"2026-10-21T10:00:00","action":"auth.login_failed","action_label":"Login gagal","category":"auth",
               "category_label":"Akun & Login","description":"Login gagal: password salah","user_name":"budi","user":null,
               "changes":[],"meta":{"Username dicoba":"budi"},"ip_address":null,"user_agent":null}
            ],"meta":{"current_page":1,"last_page":1,"total":2},"categories":{"room":"Ruangan"},"retention_days":365}
        """.trimIndent()

        val page = ApiJson.decodeFromString<PageResponse<AuditLogDto>>(json)
        assertEquals(2, page.meta.total)
        assertEquals("12", (page.data[0].changes.single().new as JsonPrimitive).content)
        assertTrue(page.data[0].meta.isEmpty())
        assertTrue(page.data[1].isFailure)
        assertEquals("budi", (page.data[1].meta.getValue("Username dicoba") as JsonPrimitive).content)
    }
}
