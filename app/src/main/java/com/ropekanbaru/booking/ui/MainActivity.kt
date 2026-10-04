package com.ropekanbaru.booking.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ropekanbaru.booking.AppContainer
import com.ropekanbaru.booking.MrbsApp
import com.ropekanbaru.booking.data.SessionState
import com.ropekanbaru.booking.ui.login.LoginScreen
import com.ropekanbaru.booking.ui.theme.MrbsTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as MrbsApp).container
        // Splash tetap tampil sampai sesi login tersimpan selesai dibaca.
        splash.setKeepOnScreenCondition { container.authRepository.state.value is SessionState.Loading }

        setContent {
            MrbsTheme { MrbsRoot(container) }
        }
    }
}

@Composable
private fun MrbsRoot(container: AppContainer) {
    val session by container.authRepository.state.collectAsStateWithLifecycle()
    val settings by container.settings.collectAsStateWithLifecycle()

    // Animasi hanya saat berpindah Login <-> Beranda; pembaruan data user tidak memicu animasi.
    AnimatedContent(
        targetState = session,
        contentKey = { it::class },
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "session",
    ) { state ->
        when (state) {
            SessionState.Loading -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
            SessionState.LoggedOut -> LoginScreen(auth = container.authRepository, settings = settings)
            is SessionState.LoggedIn -> MainScreen(user = state.user, settings = settings, container = container)
        }
    }
}
