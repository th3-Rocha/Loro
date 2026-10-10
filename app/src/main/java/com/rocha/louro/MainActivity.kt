package com.rocha.louro

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rocha.louro.notify.Reminder
import com.rocha.louro.ui.home.HomeScreen
import com.rocha.louro.ui.home.LanguageScreen
import com.rocha.louro.ui.home.PlacementDoneScreen
import com.rocha.louro.ui.home.PlacementIntroScreen
import com.rocha.louro.ui.home.PlacementQuestionScreen
import com.rocha.louro.ui.level.DoneScreen
import com.rocha.louro.ui.level.LevelScreen
import com.rocha.louro.ui.level.rememberExitRequest
import com.rocha.louro.ui.theme.LouroColors
import com.rocha.louro.ui.theme.LouroTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bars = SystemBarStyle.dark(Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
        Reminder.schedule(this)
        setContent {
            LouroTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                val askNotifications = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission(),
                ) {}
                // Asked once, on the first visit to the path, so the streak reminder can show up.
                LaunchedEffect(state.screen == Screen.Home) {
                    if (state.screen == Screen.Home && shouldAskForNotifications()) {
                        askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                Box(Modifier.fillMaxSize().background(LouroColors.Eggplant).safeDrawingPadding()) {
                    when (val screen = state.screen) {
                        Screen.Loading -> Unit
                        Screen.Languages -> {
                            BackHandler(enabled = state.language != null, onBack = viewModel::backHome)
                            LanguageScreen(state.languages, state.language, viewModel::selectLanguage)
                        }
                        Screen.Home -> HomeScreen(
                            state, viewModel::startLevel, viewModel::openLanguages, viewModel::openPlacement,
                        )
                        Screen.PlacementIntro -> {
                            BackHandler(onBack = viewModel::skipPlacement)
                            PlacementIntroScreen(
                                state.language?.info?.name.orEmpty(), viewModel::startPlacement, viewModel::skipPlacement,
                            )
                        }
                        is Screen.Placement -> {
                            BackHandler(onBack = viewModel::backHome)
                            PlacementQuestionScreen(
                                question = screen.question,
                                number = screen.number,
                                speak = { viewModel.speaker.speak(it, state.locale) },
                                onAnswer = viewModel::answerPlacement,
                            )
                        }
                        is Screen.PlacementDone -> {
                            BackHandler(onBack = viewModel::backHome)
                            PlacementDoneScreen(screen.result, viewModel::backHome)
                        }
                        is Screen.Playing -> {
                            val (exitRequested, onExitRequest) = rememberExitRequest()
                            BackHandler { onExitRequest(true) }
                            LevelScreen(
                                ui = screen.ui,
                                submit = viewModel::submit,
                                next = viewModel::next,
                                speak = { text, audio -> viewModel.speaker.speak(text, state.locale, audio) },
                                lookUp = viewModel::lookUp,
                                exitRequested = exitRequested,
                                onExitRequest = onExitRequest,
                                onExit = viewModel::backHome,
                            )
                        }
                        is Screen.Done -> {
                            BackHandler(onBack = viewModel::backHome)
                            DoneScreen(screen.summary, viewModel::backHome)
                        }
                    }
                }
            }
        }
    }

    private fun shouldAskForNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < 33) return false
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        val prefs = getSharedPreferences("louro", Context.MODE_PRIVATE)
        if (granted || prefs.getBoolean(ASKED_NOTIFICATIONS, false)) return false
        prefs.edit().putBoolean(ASKED_NOTIFICATIONS, true).apply()
        return true
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDay()
        Reminder.cancelShown(this)
    }

    private companion object {
        const val ASKED_NOTIFICATIONS = "asked_notifications"
    }
}
