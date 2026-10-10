package com.rocha.louro

import com.rocha.louro.content.ContentJson
import com.rocha.louro.content.Exercise
import com.rocha.louro.content.LanguageInfo
import com.rocha.louro.content.Level
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Guards the bundled content: every level must parse and only review what was already taught. */
class ContentTest {
    private val root = File("src/main/assets/content")

    @Test
    fun everyLevelIsValid() {
        val languages = root.listFiles { file -> file.isDirectory }.orEmpty().sortedBy { it.name }
        assertTrue("no languages found in ${root.absolutePath}", languages.isNotEmpty())

        for (language in languages) {
            ContentJson.decodeFromString<LanguageInfo>(File(language, "language.json").readText())
            val taught = mutableSetOf<String>()
            val levels = language.listFiles { file -> file.name.startsWith("level-") }.orEmpty().sortedBy { it.name }
            assertTrue("${language.name} has no levels", levels.isNotEmpty())

            levels.forEachIndexed { index, dir ->
                val where = "${language.name}/${dir.name}"
                assertEquals("$where breaks the numbering", "level-%04d".format(index), dir.name)
                val level = ContentJson.decodeFromString<Level>(File(dir, "level.json").readText())
                assertTrue("$where has no exercises", level.exercises.isNotEmpty())
                assertEquals("$where repeats an exercise id", level.exercises.size, level.exercises.map { it.id }.toSet().size)

                for (exercise in level.exercises) {
                    val at = "$where/${exercise.id}"
                    for (item in exercise.new) {
                        assertTrue("$at introduces '$item' without a glossary entry", item in level.glossary)
                    }
                    taught += exercise.new
                    for (item in exercise.review) {
                        assertTrue("$at reviews '$item' before it was taught", item in taught)
                    }
                    when (exercise) {
                        is Exercise.Choice -> {
                            assertTrue("$at needs at least two options", exercise.options.size >= 2)
                            assertTrue("$at answer out of range", exercise.answer in exercise.options.indices)
                        }
                        is Exercise.Write -> assertTrue("$at has no accepted answer", exercise.answers.isNotEmpty())
                        is Exercise.Read -> assertTrue("$at has no tokens", exercise.tokens.isNotEmpty())
                        else -> Unit
                    }
                }
            }
        }
    }
}
