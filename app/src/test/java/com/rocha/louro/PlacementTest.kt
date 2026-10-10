package com.rocha.louro

import com.rocha.louro.content.Exercise
import com.rocha.louro.content.Level
import com.rocha.louro.content.LevelEntry
import com.rocha.louro.content.LevelType
import com.rocha.louro.data.Progress
import com.rocha.louro.notify.Reminder
import com.rocha.louro.session.PlacementTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class PlacementTest {
    private val today = 20_000L

    private fun level(index: Int, words: Int = 3) = LevelEntry(
        "level-%04d".format(index), "x",
        Level(
            type = LevelType.VOCABULARY,
            title = "L$index",
            glossary = (1..words).associate { "w$index-$it" to "g$index-$it" },
            exercises = listOf(Exercise.Card(id = "c", term = "t", translation = "tr")),
        ),
    )

    private val levels = (0..3).map { level(it) }

    /** Answers every question right while the word belongs to a level below [knownBelow]. */
    private fun run(test: PlacementTest, knownBelow: Int) {
        while (!test.finished) {
            val question = test.current ?: break
            val levelOfWord = question.item.removePrefix("w").substringBefore('-').toInt()
            test.answer(if (levelOfWord < knownBelow) question.answer else null)
        }
    }

    @Test
    fun stopsAtTheFirstUnknownLevel() {
        val test = PlacementTest(levels, random = Random(7))
        run(test, knownBelow = 2)
        assertEquals(2, test.passedLevels)
        assertEquals(8, test.answered)
    }

    @Test
    fun knowingEverythingSkipsEveryLevel() {
        val test = PlacementTest(levels, random = Random(7))
        run(test, knownBelow = 99)
        assertEquals(4, test.passedLevels)
        assertNull(test.current)
    }

    @Test
    fun oneSlipIsForgivenButTwoAreNot() {
        val oneSlip = PlacementTest(levels, random = Random(7))
        oneSlip.answer(null)
        repeat(2) { oneSlip.answer(oneSlip.current!!.answer) }
        assertEquals(1, oneSlip.passedLevels)

        val twoSlips = PlacementTest(levels, random = Random(7))
        twoSlips.answer(null)
        twoSlips.answer(null)
        assertNull(twoSlips.current)
        assertTrue(twoSlips.finished)
        assertEquals(0, twoSlips.passedLevels)
    }

    @Test
    fun applyingMarksLevelsDoneAndCountsTheirWords() {
        val progress = PlacementTest.apply(Progress(), "it", levels.take(2), today)
        val language = progress.language("it")

        assertEquals(setOf("level-0000", "level-0001"), language.completed)
        assertEquals(6, language.knownWords)
        assertEquals(today + 3, language.srs.getValue("w0-1").due)
        assertTrue(language.placementOffered)
        assertEquals(0, progress.streak)
    }

    @Test
    fun reminderOnlySpeaksWhenTodayIsStillOpen() {
        val playing = Progress(currentLanguage = "it", streak = 4, lastActiveDay = today - 1)

        assertNull(Reminder.text(Progress(), today))
        assertNull(Reminder.text(playing.copy(lastActiveDay = today), today))
        assertTrue(Reminder.text(playing, today)!!.title.contains("4 dias"))
        assertTrue(Reminder.text(playing, today + 5)!!.title.contains("esperando"))
    }
}
