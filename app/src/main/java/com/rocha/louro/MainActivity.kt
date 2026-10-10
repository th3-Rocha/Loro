package com.rocha.louro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rocha.louro.ui.home.HomeScreen
import com.rocha.louro.ui.home.LanguageScreen
import com.rocha.louro.ui.level.DoneScreen
import com.rocha.louro.ui.level.LevelScreen
import com.rocha.louro.ui.level.rememberExitRequest
import com.rocha.louro.ui.theme.LouroColors
import com.rocha.louro.ui.theme.LouroTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LouroTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                Box(Modifier.fillMaxSize().background(LouroColors.Eggplant).safeDrawingPadding()) {
                    when (val screen = state.screen) {
                        Screen.Loading -> Unit
                        Screen.Languages -> {
                            BackHandler(enabled = state.language != null, onBack = viewModel::backHome)
                            LanguageScreen(state.languages, state.language, viewModel::selectLanguage)
                        }
                        Screen.Home -> HomeScreen(state, viewModel::startLevel, viewModel::openLanguages)
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

    override fun onResume() {
        super.onResume()
        viewModel.refreshDay()
    }
}
