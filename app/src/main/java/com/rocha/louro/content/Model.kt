package com.rocha.louro.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Shared parser for every content file. The exercise type is chosen by the "kind" field. */
val ContentJson = Json {
    ignoreUnknownKeys = true
    classDiscriminator = "kind"
}

/** `language.json` at the root of each language folder. */
@Serializable
data class LanguageInfo(
    val name: String,
    /** BCP-47 tag used for text-to-speech, e.g. "it-IT". */
    val locale: String,
    val flag: String = "",
)

data class Language(val id: String, val info: LanguageInfo)

@Serializable
enum class LevelType {
    @SerialName("vocabulary") VOCABULARY,
    @SerialName("listen_speak") LISTEN_SPEAK,
    @SerialName("writing") WRITING,
    @SerialName("reading") READING,
    @SerialName("grammar") GRAMMAR,
}

/** `level.json` inside each `level-NNNN` folder. */
@Serializable
data class Level(
    val schema: Int = 1,
    val type: LevelType,
    val title: String,
    val theme: String = "",
    /** Meaning (in Portuguese) of each item this level introduces. */
    val glossary: Map<String, String> = emptyMap(),
    val exercises: List<Exercise>,
)

data class LevelEntry(val id: String, val path: String, val level: Level)

@Serializable
sealed class Exercise {
    abstract val id: String

    /** Words/concepts this exercise introduces. */
    abstract val new: List<String>

    /** Words/concepts this exercise revisits in passing. */
    abstract val review: List<String>

    /** Whether the learner can get it wrong. */
    open val graded: Boolean get() = true

    val items: List<String> get() = new + review

    @Serializable
    @SerialName("card")
    data class Card(
        override val id: String,
        override val new: List<String> = emptyList(),
        override val review: List<String> = emptyList(),
        val term: String,
        val translation: String,
        val example: String? = null,
        val exampleTranslation: String? = null,
        val emoji: String? = null,
        val image: String? = null,
        val audio: String? = null,
    ) : Exercise() {
        override val graded: Boolean get() = false
    }

    @Serializable
    @SerialName("note")
    data class Note(
        override val id: String,
        override val new: List<String> = emptyList(),
        override val review: List<String> = emptyList(),
        val title: String,
        val text: String,
        val examples: List<NoteExample> = emptyList(),
    ) : Exercise() {
        override val graded: Boolean get() = false
    }

    @Serializable
    @SerialName("choice")
    data class Choice(
        override val id: String,
        override val new: List<String> = emptyList(),
        override val review: List<String> = emptyList(),
        val prompt: String,
        /** Optional target-language text shown under the prompt and read aloud. */
        val speak: String? = null,
        val options: List<String>,
        val answer: Int,
    ) : Exercise()

    @Serializable
    @SerialName("write")
    data class Write(
        override val id: String,
        override val new: List<String> = emptyList(),
        override val review: List<String> = emptyList(),
        /** Sentence in Portuguese to be written in the target language. */
        val prompt: String,
        val answers: List<String>,
    ) : Exercise()

    @Serializable
    @SerialName("speak")
    data class Speak(
        override val id: String,
        override val new: List<String> = emptyList(),
        override val review: List<String> = emptyList(),
        val text: String,
        val translation: String? = null,
        val audio: String? = null,
    ) : Exercise()

    @Serializable
    @SerialName("read")
    data class Read(
        override val id: String,
        override val new: List<String> = emptyList(),
        override val review: List<String> = emptyList(),
        val title: String? = null,
        val tokens: List<Token>,
        val translation: String? = null,
    ) : Exercise() {
        override val graded: Boolean get() = false
    }
}

@Serializable
data class NoteExample(val text: String, val translation: String)

@Serializable
data class Token(
    /** Text exactly as it appears, punctuation included. */
    val t: String,
    /** Meaning in Portuguese; tokens without it are not tappable. */
    val g: String? = null,
    /** Item key for the review queue; defaults to the lowercased text without punctuation. */
    val w: String? = null,
) {
    val item: String get() = w ?: t.lowercase().trim { !it.isLetterOrDigit() }
}
