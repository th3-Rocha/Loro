package com.rocha.louro

import com.rocha.louro.session.AnswerChecker
import com.rocha.louro.session.WriteResult
import org.junit.Assert.assertEquals
import org.junit.Test

class AnswerCheckerTest {
    @Test
    fun forgivesCasePunctuationAndSpacing() {
        assertEquals(WriteResult.CORRECT, AnswerChecker.check("  ciao,  MI chiamo luca! ", listOf("Ciao, mi chiamo Luca")))
    }

    @Test
    fun acceptsAnyListedAnswer() {
        assertEquals(WriteResult.CORRECT, AnswerChecker.check("I’m Anna", listOf("I am Anna", "I'm Anna")))
    }

    @Test
    fun missingAccentIsNotCorrect() {
        assertEquals(WriteResult.ACCENT, AnswerChecker.check("Lui e di Roma", listOf("Lui è di Roma")))
        assertEquals(WriteResult.ACCENT, AnswerChecker.check("un caffe per favore", listOf("Un caffè, per favore")))
    }

    @Test
    fun wrongWordIsWrong() {
        assertEquals(WriteResult.WRONG, AnswerChecker.check("Lui sono di Roma", listOf("Lui è di Roma")))
    }
}
