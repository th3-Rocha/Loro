package com.rocha.louro

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rocha.louro.audio.Speaker
import com.rocha.louro.content.ContentRepository
import com.rocha.louro.content.Language
import com.rocha.louro.content.LevelEntry
import com.rocha.louro.data.Progress
import com.rocha.louro.data.ProgressStore
import com.rocha.louro.session.LevelSession
import com.rocha.louro.session.SessionItem
import com.rocha.louro.session.Srs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.util.Locale

data class Feedback(val correct: Boolean, val answer: String?, val hint: String?, val points: Int)

data class LevelUi(
    val title: String,
    val path: String,
    val item: SessionItem,
    val attempt: Int,
    val done: Int,
    val total: Int,
    val feedback: Feedback? = null,
)

data class Summary(val title: String, val points: Int, val streak: Int, val streakGrew: Boolean)

sealed interface Screen {
    data object Loading : Screen
    data object Languages : Screen
    data object Home : Screen
    data class Playing(val ui: LevelUi) : Screen
    data class Done(val summary: Summary) : Screen
}

data class AppState(
    val screen: Screen = Screen.Loading,
    val languages: List<Language> = emptyList(),
    val language: Language? = null,
    val levels: List<LevelEntry> = emptyList(),
    val progress: Progress = Progress(),
    val today: Long = 0,
) {
    val locale: Locale get() = Locale.forLanguageTag(language?.info?.locale ?: "en-US")
}

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val content = ContentRepository(app.assets)
    private val store = ProgressStore(File(app.filesDir, "progress.json"))
    val speaker = Speaker(app)

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private var session: LevelSession? = null
    private var sessionItems: List<SessionItem> = emptyList()
    private var sessionLevel: LevelEntry? = null

    init {
        viewModelScope.launch {
            val (languages, progress) = withContext(Dispatchers.IO) { content.languages() to store.load() }
            val language = languages.find { it.id == progress.currentLanguage }
            val levels = language?.let { loadLevels(it.id) }.orEmpty()
            _state.value = AppState(
                screen = if (language == null) Screen.Languages else Screen.Home,
                languages = languages,
                language = language,
                levels = levels,
                progress = progress,
                today = today(),
            )
        }
    }

    private fun today(): Long = LocalDate.now().toEpochDay()

    private suspend fun loadLevels(languageId: String): List<LevelEntry> =
        withContext(Dispatchers.IO) { content.levels(languageId) }

    private fun save(progress: Progress) {
        viewModelScope.launch(Dispatchers.IO) { store.save(progress) }
    }

    fun refreshDay() = _state.update { it.copy(today = today()) }

    fun openLanguages() = _state.update { it.copy(screen = Screen.Languages) }

    fun selectLanguage(language: Language) {
        viewModelScope.launch {
            val levels = loadLevels(language.id)
            val progress = _state.value.progress.copy(currentLanguage = language.id)
            save(progress)
            _state.update {
                it.copy(screen = Screen.Home, language = language, levels = levels, progress = progress)
            }
        }
    }

    fun startLevel(level: LevelEntry) {
        val state = _state.value
        val language = state.language ?: return
        val own = level.level.exercises.map { SessionItem(it.id, it) }
        val reviews = Srs.buildReviews(state.levels, state.progress.language(language.id), level, today())
        sessionItems = Srs.interleave(own, reviews)
        sessionLevel = level
        session = LevelSession(sessionItems)
        showCurrent()
    }

    private fun showCurrent() {
        val session = session ?: return
        val level = sessionLevel ?: return
        val item = session.current ?: return
        _state.update {
            it.copy(
                screen = Screen.Playing(
                    LevelUi(
                        title = level.level.title,
                        path = level.path,
                        item = item,
                        attempt = session.currentAttempt,
                        done = session.done,
                        total = session.total,
                    )
                )
            )
        }
    }

    /** Grades the current exercise and shows the feedback; [next] moves on. */
    fun submit(correct: Boolean, answer: String? = null, hint: String? = null) {
        val session = session ?: return
        val playing = _state.value.screen as? Screen.Playing ?: return
        if (playing.ui.feedback != null) return
        val gained = session.answer(correct)
        val feedback = Feedback(correct, answer, hint, gained)
        _state.update { it.copy(screen = Screen.Playing(playing.ui.copy(done = session.done, feedback = feedback))) }
    }

    fun next() {
        val session = session ?: return
        speaker.stop()
        if (session.finished) finish() else showCurrent()
    }

    fun lookUp(item: String, gloss: String) {
        session?.lookUp(item, gloss)
    }

    private fun finish() {
        val session = session ?: return
        val level = sessionLevel ?: return
        val state = _state.value
        val language = state.language ?: return
        val today = today()
        val before = state.progress.streakOn(today)
        val progress = Srs.complete(state.progress, language.id, level, sessionItems, session, today)
        save(progress)
        this.session = null
        _state.update {
            it.copy(
                screen = Screen.Done(
                    Summary(level.level.title, session.points, progress.streak, progress.streak > before)
                ),
                progress = progress,
                today = today,
            )
        }
    }

    fun backHome() {
        session = null
        speaker.stop()
        _state.update { it.copy(screen = Screen.Home) }
    }

    override fun onCleared() {
        speaker.release()
    }
}
