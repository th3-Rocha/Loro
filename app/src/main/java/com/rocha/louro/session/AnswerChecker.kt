package com.rocha.louro.session

import java.text.Normalizer

enum class WriteResult { CORRECT, ACCENT, WRONG }

object AnswerChecker {
    private val punctuation = Regex("[.,!?;:¿¡\"]")
    private val spaces = Regex("\\s+")
    private val marks = Regex("\\p{Mn}+")

    /** Case, spacing and punctuation are forgiven; accents and spelling are not. */
    fun normalize(text: String): String =
        text.replace('’', '\'').replace(punctuation, " ").replace(spaces, " ").trim().lowercase()

    private fun stripAccents(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD).replace(marks, "")

    fun check(input: String, answers: List<String>): WriteResult {
        val given = normalize(input)
        val accepted = answers.map(::normalize)
        return when {
            given in accepted -> WriteResult.CORRECT
            stripAccents(given) in accepted.map(::stripAccents) -> WriteResult.ACCENT
            else -> WriteResult.WRONG
        }
    }
}
