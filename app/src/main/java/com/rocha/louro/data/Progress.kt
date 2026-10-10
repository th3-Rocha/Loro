package com.rocha.louro.data

import kotlinx.serialization.Serializable

/** Spaced-repetition state of one word or concept. Days are epoch days. */
@Serializable
data class SrsItem(
    val gloss: String? = null,
    val interval: Int = 0,
    val ease: Double = 2.5,
    val due: Long = 0,
    val reps: Int = 0,
    val lapses: Int = 0,
)

@Serializable
data class LanguageProgress(
    val completed: Set<String> = emptySet(),
    val points: Int = 0,
    val srs: Map<String, SrsItem> = emptyMap(),
)

@Serializable
data class Progress(
    val currentLanguage: String? = null,
    val languages: Map<String, LanguageProgress> = emptyMap(),
    val streak: Int = 0,
    val lastActiveDay: Long? = null,
) {
    fun language(id: String): LanguageProgress = languages[id] ?: LanguageProgress()

    /** The streak as it stands on [today]: it is lost after a full day without a level. */
    fun streakOn(today: Long): Int {
        val last = lastActiveDay ?: return 0
        return if (today - last <= 1) streak else 0
    }
}
