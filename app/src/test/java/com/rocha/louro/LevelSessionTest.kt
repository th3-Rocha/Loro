package com.rocha.louro

import com.rocha.louro.content.Exercise
import com.rocha.louro.session.LevelSession
import com.rocha.louro.session.SessionItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelSessionTest {
    private fun write(id: String) = SessionItem(id, Exercise.Write(id = id, prompt = "p", answers = listOf("a")))
    private fun card(id: String) = SessionItem(id, Exercise.Card(id = id, term = "t", translation = "tr"))

    @Test
    fun missedExerciseComesBackUntilCorrect() {
        val session = LevelSession(listOf(write("a"), write("b")))

        assertEquals(0, session.answer(false))
        assertEquals("b", session.current?.key)
        session.answer(true)
        assertFalse(session.finished)
        assertEquals("a", session.current?.key)
        assertEquals(1, session.currentAttempt)

        session.answer(true)
        assertTrue(session.finished)
        assertEquals(2, session.done)
    }

    @Test
    fun firstTryEarnsBonus() {
        val session = LevelSession(listOf(write("a"), write("b"), card("c")))
        assertEquals(15, session.answer(true))
        session.answer(false)
        assertEquals(5, session.answer(true))
        assertEquals(10, session.answer(true))
        assertEquals(30, session.points)
        assertEquals(mapOf("a" to true, "b" to false, "c" to true), session.firstTryResults)
    }
}
