package com.rocha.louro.session

import com.rocha.louro.content.Exercise
import com.rocha.louro.content.LevelEntry
import com.rocha.louro.data.LanguageProgress
import com.rocha.louro.data.Progress
import com.rocha.louro.data.SrsItem
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

/** The invisible Anki: decides when each word or concept comes back. */
object Srs {
    const val MAX_REVIEWS_PER_LEVEL = 3
    private const val MIN_EASE = 1.3

    fun grade(item: SrsItem?, correct: Boolean, today: Long, gloss: String? = null): SrsItem {
        val base = item ?: SrsItem(due = today)
        val known = gloss ?: base.gloss
        if (!correct) {
            return base.copy(
                gloss = known,
                interval = 0,
                ease = max(MIN_EASE, base.ease - 0.2),
                due = today,
                reps = 0,
                lapses = base.lapses + 1,
            )
        }
        val interval = when (base.reps) {
            0 -> 1
            1 -> 3
            else -> max(base.interval + 1, (base.interval * base.ease).roundToInt())
        }
        return base.copy(gloss = known, interval = interval, due = today + interval, reps = base.reps + 1)
    }

    fun dueItems(srs: Map<String, SrsItem>, today: Long): List<String> =
        srs.filterValues { it.due <= today }.entries
            .sortedWith(compareBy({ it.value.due }, { it.key }))
            .map { it.key }

    /**
     * Picks due items and turns each into an exercise: either one the learner
     * already met in a completed level, or a "what does it mean" quiz built
     * from the glossary.
     */
    fun buildReviews(
        levels: List<LevelEntry>,
        progress: LanguageProgress,
        current: LevelEntry,
        today: Long,
        random: Random = Random.Default,
    ): List<SessionItem> {
        val ownItems = current.level.exercises.flatMap { it.items }.toSet()
        val seen = levels.filter { it.id in progress.completed && it.id != current.id }
        val glosses = progress.srs.mapNotNull { (item, state) -> state.gloss?.let { item to it } }.toMap()

        return dueItems(progress.srs, today)
            .filter { it !in ownItems }
            .mapNotNull { item ->
                val candidates = seen.flatMap { it.level.exercises }
                    .filter { it.graded && it !is Exercise.Speak && item in it.items }
                    .toMutableList<Exercise>()
                glossQuiz(item, glosses, random)?.let(candidates::add)
                candidates.randomOrNull(random)?.let { SessionItem("review:$item", it, reviewItem = item) }
            }
            .take(MAX_REVIEWS_PER_LEVEL)
    }

    private fun glossQuiz(item: String, glosses: Map<String, String>, random: Random): Exercise.Choice? {
        val answer = glosses[item] ?: return null
        val distractors = glosses.filterKeys { it != item }.values.distinct().filter { it != answer }
        if (distractors.size < 2) return null
        val options = (distractors.shuffled(random).take(3) + answer).shuffled(random)
        return Exercise.Choice(
            id = "quiz:$item",
            review = listOf(item),
            prompt = "O que significa?",
            speak = item,
            options = options,
            answer = options.indexOf(answer),
        )
    }

    /** Spreads the review exercises through the level instead of piling them up. */
    fun interleave(own: List<SessionItem>, reviews: List<SessionItem>): List<SessionItem> {
        if (reviews.isEmpty()) return own
        val result = own.toMutableList()
        val step = max(1, own.size / (reviews.size + 1))
        reviews.forEachIndexed { index, review ->
            result.add(minOf(result.size, (index + 1) * step + index), review)
        }
        return result
    }

    /** Applies a finished session: points, completion, streak and the review schedule. */
    fun complete(
        progress: Progress,
        languageId: String,
        level: LevelEntry,
        items: List<SessionItem>,
        session: LevelSession,
        today: Long,
    ): Progress {
        val language = progress.language(languageId)
        val srs = language.srs.toMutableMap()

        for (item in items) {
            val correct = session.firstTryResults[item.key] ?: continue
            val targets = item.reviewItem?.let(::listOf) ?: item.exercise.items
            for (target in targets) {
                srs[target] = grade(srs[target], correct, today, level.level.glossary[target])
            }
        }
        for ((word, gloss) in session.lookedUpWords) {
            if (word !in srs) srs[word] = SrsItem(gloss = gloss, due = today)
        }

        val streak = when (progress.lastActiveDay) {
            today -> progress.streak
            today - 1 -> progress.streak + 1
            else -> 1
        }
        return progress.copy(
            languages = progress.languages + (languageId to language.copy(
                completed = language.completed + level.id,
                points = language.points + session.points,
                srs = srs,
            )),
            streak = streak,
            lastActiveDay = today,
        )
    }
}
