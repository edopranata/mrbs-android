package com.ropekanbaru.booking

import com.ropekanbaru.booking.data.remote.MrbsApi
import com.ropekanbaru.booking.ui.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Proxy

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val uncaught = mutableListOf<Throwable>()
    private val previousHandler = Thread.getDefaultUncaughtExceptionHandler()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        Thread.setDefaultUncaughtExceptionHandler { _, e -> uncaught += e }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        Thread.setDefaultUncaughtExceptionHandler(previousHandler)
    }

    /**
     * API palsu: setiap panggilan gagal seperti server yang tidak terjangkau. (Proxy Java membungkus
     * exception checked seperti ConnectException, jadi dipakai exception runtime.)
     */
    private val offlineApi = Proxy.newProxyInstance(MrbsApi::class.java.classLoader, arrayOf(MrbsApi::class.java)) { _, _, _ ->
        throw IllegalStateException("Failed to connect to /127.0.0.1:8000")
    } as MrbsApi

    @Test
    fun `server tidak terjangkau tidak membuat aplikasi force close`() = runTest(dispatcher) {
        val vm = HomeViewModel(offlineApi, MutableStateFlow(0))
        advanceUntilIdle()

        assertEquals(emptyList<Throwable>(), uncaught)
        assertFalse(vm.state.loading)
        assertTrue(vm.state.error != null)
    }
}
