package com.rocha.louro.session

import com.rocha.louro.content.Exercise

/** One step of a session. [reviewItem] is set when the spaced-repetition engine injected it. */
data class SessionItem(val key: String, val exercise: Exercise, val reviewItem: String? = null)

/**
 * A level run: every exercise must end up correct, and a missed one goes back
 * to the end of the queue until it is. No timer anywhere.
 */
class LevelSession(items: List<SessionItem>) {
    private val queue = ArrayDeque(items)
    private val attempts = mutableMapOf<String, Int>()
    private val firstTry = mutableMapOf<String, Boolean>()
    private val lookedUp = mutableMapOf<String, String>()

    val total: Int = items.size
    var done: Int = 0
        private set
    var points: Int = 0
        private set

    val current: SessionItem? get() = queue.firstOrNull()
    val finished: Boolean get() = queue.isEmpty()

    /** How many times the current exercise has already been answered. */
    val currentAttempt: Int get() = current?.let { attempts[it.key] } ?: 0

    /** First-attempt result of each exercise seen so far. */
    val firstTryResults: Map<String, Boolean> get() = firstTry

    /** Words the learner tapped while reading, with their meaning. */
    val lookedUpWords: Map<String, String> get() = lookedUp

    /** Records the answer to the current exercise and returns the points earned. */
    fun answer(correct: Boolean): Int {
        val item = queue.removeFirst()
        val attempt = (attempts[item.key] ?: 0) + 1
        attempts[item.key] = attempt
        if (attempt == 1) firstTry[item.key] = correct
        if (!correct) {
            queue.addLast(item)
            return 0
        }
        done++
        val gained = when {
            !item.exercise.graded -> SEEN_POINTS
            attempt == 1 -> CORRECT_POINTS + FIRST_TRY_BONUS
            else -> CORRECT_POINTS
        }
        points += gained
        return gained
    }

    fun lookUp(item: String, gloss: String) {
        lookedUp[item] = gloss
    }

    companion object {
        const val SEEN_POINTS = 5
        const val CORRECT_POINTS = 10
        const val FIRST_TRY_BONUS = 5
    }
}
