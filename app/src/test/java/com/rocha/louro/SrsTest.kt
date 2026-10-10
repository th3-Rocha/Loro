package com.rocha.louro

import com.rocha.louro.content.Exercise
import com.rocha.louro.content.Level
import com.rocha.louro.content.LevelEntry
import com.rocha.louro.content.LevelType
import com.rocha.louro.data.LanguageProgress
import com.rocha.louro.data.Progress
import com.rocha.louro.data.SrsItem
import com.rocha.louro.session.LevelSession
import com.rocha.louro.session.SessionItem
import com.rocha.louro.session.Srs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SrsTest {
    private val today = 20_000L

    private fun level(id: String, vararg exercises: Exercise, glossary: Map<String, String> = emptyMap()) =
        LevelEntry(id, "content/x/$id", Level(type = LevelType.VOCABULARY, title = id, glossary = glossary, exercises = exercises.toList()))

    private fun choice(id: String, vararg review: String) =
        Exercise.Choice(id = id, review = review.toList(), prompt = "p", options = listOf("a", "b"), answer = 0)

    @Test
    fun correctAnswersSpaceOutAndMissesComeBackToday() {
        var item = Srs.grade(null, true, today)
        assertEquals(today + 1, item.due)
        item = Srs.grade(item, true, item.due)
        assertEquals(3, item.interval)
        item = Srs.grade(item, true, item.due)
        assertEquals(8, item.interval)

        val missed = Srs.grade(item, false, today + 30)
        assertEquals(today + 30, missed.due)
        assertEquals(0, missed.reps)
        assertEquals(1, missed.lapses)
        assertTrue(missed.ease < item.ease)
    }

    @Test
    fun streakGrowsOnConsecutiveDaysAndResetsAfterAGap() {
        val entry = level("level-0000", choice("q", "ciao"))
        val items = listOf(SessionItem("q", entry.level.exercises[0]))
        fun play(progress: Progress, day: Long): Progress {
            val session = LevelSession(items).apply { answer(true) }
            return Srs.complete(progress, "it", entry, items, session, day)
        }

        var progress = play(Progress(), today)
        assertEquals(1, progress.streak)
        progress = play(progress, today)
        assertEquals(1, progress.streak)
        progress = play(progress, today + 1)
        assertEquals(2, progress.streak)
        assertEquals(0, progress.streakOn(today + 3))
        progress = play(progress, today + 3)
        assertEquals(1, progress.streak)
        assertEquals(setOf("level-0000"), progress.language("it").completed)
    }

    @Test
    fun completingStoresGlossAndLookedUpWords() {
        val card = Exercise.Card(id = "c", new = listOf("ciao"), term = "ciao", translation = "oi")
        val entry = level("level-0000", card, glossary = mapOf("ciao" to "oi"))
        val items = listOf(SessionItem("c", card))
        val session = LevelSession(items).apply {
            lookUp("bar", "cafeteria")
            answer(true)
        }

        val srs = Srs.complete(Progress(), "it", entry, items, session, today).language("it").srs

        assertEquals("oi", srs.getValue("ciao").gloss)
        assertEquals(today + 1, srs.getValue("ciao").due)
        assertEquals(SrsItem(gloss = "cafeteria", due = today), srs.getValue("bar"))
    }

    @Test
    fun dueItemsBecomeReviewsFromCompletedLevels() {
        val done = level("level-0000", choice("q-ciao", "ciao"), choice("q-grazie", "grazie"))
        val current = level("level-0001", choice("q-new", "grazie"))
        val progress = LanguageProgress(
            completed = setOf("level-0000"),
            srs = mapOf(
                "ciao" to SrsItem(due = today - 1),
                "grazie" to SrsItem(due = today),
                "piacere" to SrsItem(due = today + 5),
            ),
        )

        val reviews = Srs.buildReviews(listOf(done, current), progress, current, today, Random(1))

        // "grazie" is already practised by the level itself and "piacere" is not due yet.
        assertEquals(listOf("ciao"), reviews.map { it.reviewItem })
        assertEquals("q-ciao", reviews.single().exercise.id)
    }

    @Test
    fun lookedUpWordIsReviewedWithAGlossQuiz() {
        val current = level("level-0001", choice("q", "x"))
        val progress = LanguageProgress(
            srs = mapOf(
                "bar" to SrsItem(gloss = "cafeteria", due = today),
                "ciao" to SrsItem(gloss = "oi", due = today + 9),
                "grazie" to SrsItem(gloss = "obrigado", due = today + 9),
            ),
        )

        val quiz = Srs.buildReviews(listOf(current), progress, current, today, Random(1)).single().exercise as Exercise.Choice

        assertEquals("bar", quiz.speak)
        assertEquals("cafeteria", quiz.options[quiz.answer])
        assertEquals(3, quiz.options.size)
        assertNull(Srs.buildReviews(listOf(current), LanguageProgress(srs = mapOf("bar" to SrsItem(gloss = "cafeteria", due = today))), current, today).firstOrNull())
    }

    @Test
    fun reviewsAreSpreadThroughTheLevel() {
        val own = (1..6).map { SessionItem("o$it", choice("o$it")) }
        val reviews = (1..2).map { SessionItem("r$it", choice("r$it"), reviewItem = "w$it") }
        assertEquals(
            listOf("o1", "o2", "r1", "o3", "o4", "r2", "o5", "o6"),
            Srs.interleave(own, reviews).map { it.key },
        )
    }
}
