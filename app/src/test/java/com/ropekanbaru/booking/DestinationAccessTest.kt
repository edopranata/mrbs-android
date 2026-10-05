package com.ropekanbaru.booking

import com.ropekanbaru.booking.data.remote.UserDto
import com.ropekanbaru.booking.ui.Destination
import org.junit.Assert.assertEquals
import org.junit.Test

class DestinationAccessTest {
    private fun allowed(role: String) = Destination.entries.filter { it.allowedFor(UserDto(1, "Uji", "uji", role = role)) }.toSet()

    @Test
    fun `view only hanya dashboard, jadwal, semua booking, dan profil`() {
        assertEquals(
            setOf(Destination.Dashboard, Destination.Schedule, Destination.TodayBookings, Destination.Profile),
            allowed("viewer"),
        )
    }

    @Test
    fun `user tidak membuka menu admin`() {
        assertEquals(
            setOf(Destination.Dashboard, Destination.Schedule, Destination.MyBookings, Destination.Rooms, Destination.Profile),
            allowed("user"),
        )
    }

    @Test
    fun `pengaturan khusus system admin`() {
        assertEquals(Destination.entries.toSet() - Destination.Settings, allowed("admin"))
        assertEquals(Destination.entries.toSet(), allowed("system_admin"))
    }
}
