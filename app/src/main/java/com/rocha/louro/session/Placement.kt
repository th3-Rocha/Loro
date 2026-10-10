package com.rocha.louro.session

import com.rocha.louro.content.LevelEntry
import com.rocha.louro.data.Progress
import com.rocha.louro.data.SrsItem
import kotlin.random.Random

data class PlacementQuestion(val item: String, val options: List<String>, val answer: Int)

/**
 * Vocabulary placement: quizzes a few glossary words of each level, in order,
 * and stops at the first level the learner does not know. Everything before
 * it can be skipped.
 */
class PlacementTest(
    private val levels: List<LevelEntry>,
    private val startIndex: Int = 0,
    private val random: Random = Random.Default,
) {
    private val glosses: List<String> = levels.flatMap { it.level.glossary.values }.distinct()
    private var levelIndex = startIndex
    private var pending = ArrayDeque<PlacementQuestion>()
    private var asked = 0
    private var missed = 0

    var finished = false
        private set

    /** How many levels, counted from [startIndex], the learner already knows. */
    var passedLevels = 0
        private set
    var answered = 0
        private set

    init {
        loadLevel()
    }

    val current: PlacementQuestion? get() = if (finished) null else pending.firstOrNull()

    /** [choice] is the picked option, or null for "I don't know". */
    fun answer(choice: Int?) {
        val question = pending.removeFirstOrNull() ?: return
        answered++
        if (choice != question.answer) missed++
        if (missed > allowedMisses(asked)) {
            finished = true
        } else if (pending.isEmpty()) {
            passedLevels++
            levelIndex++
            loadLevel()
        }
    }

    private fun loadLevel() {
        val level = levels.getOrNull(levelIndex)
        val questions = level?.level?.glossary.orEmpty().entries
            .shuffled(random)
            .take(QUESTIONS_PER_LEVEL)
            .mapNotNull { (item, gloss) -> question(item, gloss) }
        if (questions.isEmpty()) {
            finished = true
            return
        }
        pending = ArrayDeque(questions)
        asked = questions.size
        missed = 0
    }

    private fun question(item: String, gloss: String): PlacementQuestion? {
        val distractors = glosses.filter { it != gloss }.shuffled(random).take(3)
        if (distractors.size < 2) return null
        val options = (distractors + gloss).shuffled(random)
        return PlacementQuestion(item, options, options.indexOf(gloss))
    }

    companion object {
        const val QUESTIONS_PER_LEVEL = 3

        /** One slip is forgiven only when the level was sampled in full. */
        fun allowedMisses(asked: Int): Int = if (asked >= QUESTIONS_PER_LEVEL) 1 else 0

        /** Marks [skipped] as done and schedules their words for a check in a few days. */
        fun apply(progress: Progress, languageId: String, skipped: List<LevelEntry>, today: Long): Progress {
            val language = progress.language(languageId)
            val srs = language.srs.toMutableMap()
            for (level in skipped) {
                for ((item, gloss) in level.level.glossary) {
                    if (item !in srs) srs[item] = SrsItem(gloss = gloss, interval = 3, due = today + 3, reps = 2)
                }
            }
            return progress.copy(
                languages = progress.languages + (languageId to language.copy(
                    completed = language.completed + skipped.map { it.id },
                    srs = srs,
                    placementOffered = true,
                )),
            )
        }
    }
}
