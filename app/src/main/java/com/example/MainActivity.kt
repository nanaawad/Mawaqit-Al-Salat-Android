package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.OrientationMode
import com.example.ui.screens.MosqueDisplayScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MosqueDeepEmerald
import com.example.ui.theme.MosqueGoldAccent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MosqueViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MosqueViewModel by viewModels()
    private val openSettingsSignal = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        configureImmersiveSystemBars()

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            var showSettings by rememberSaveable { mutableStateOf(false) }
            var settingsTab by rememberSaveable { mutableIntStateOf(0) }

            // Respond to TV remote Menu/Settings hardware key
            LaunchedEffect(openSettingsSignal.value) {
                if (openSettingsSignal.value) {
                    showSettings = true
                    openSettingsSignal.value = false
                }
            }

            // Keep screen on dynamically
            LaunchedEffect(state.settings.keepScreenOn) {
                if (state.settings.keepScreenOn) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            // Orientation control dynamically
            LaunchedEffect(state.settings.orientationModeName) {
                val mode = runCatching { OrientationMode.valueOf(state.settings.orientationModeName) }
                    .getOrDefault(OrientationMode.AUTO)
                requestedOrientation = when (mode) {
                    OrientationMode.AUTO -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    OrientationMode.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    OrientationMode.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                }
            }

            DisposableEffect(showSettings) {
                configureImmersiveSystemBars()
                onDispose { }
            }

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MyApplicationTheme(fontFamilyKey = state.settings.fontFamilyKey) {
                    if (!state.isReady) {
                        LoadingSplash()
                    } else if (showSettings) {
                        SettingsScreen(
                            state = state,
                            viewModel = viewModel,
                            initialTab = settingsTab,
                            onClose = { showSettings = false }
                        )
                    } else {
                        MosqueDisplayScreen(
                            state = state,
                            onOpenSettings = {
                                settingsTab = 0
                                showSettings = true
                            },
                            onOpenCitySelector = {
                                settingsTab = 0
                                showSettings = true
                            },
                            onSyncPrayerTimesNow = { viewModel.syncPrayerTimesNow() },
                            onToggleAdhkar = { viewModel.toggleManualAdhkarDisplay() },
                            onTriggerTestCycle = { viewModel.startFullCycleSimulation() },
                            onStopAudio = { viewModel.stopAudio() },
                            onDismissPhase = { viewModel.dismissCurrentPhase() },
                            onToggleAdhkarPause = { viewModel.toggleAdhkarPause() },
                            onNextAdhkar = { viewModel.nextAdhkarItem() },
                            onAdjustPrayerOffset = { prayer, delta -> viewModel.adjustPrayerOffset(prayer, delta) },
                            onResetPrayerOffset = { prayer -> viewModel.resetSinglePrayerOffset(prayer) },
                            onSetIqamaMinutes = { prayer, mins -> viewModel.setIqamaMinutes(prayer, mins) }
                        )
                    }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_SETTINGS) {
            openSettingsSignal.value = true
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            configureImmersiveSystemBars()
        }
    }

    private fun configureImmersiveSystemBars() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }
}

@Composable
private fun LoadingSplash() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MosqueDeepEmerald),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MosqueGoldAccent)
    }
}
