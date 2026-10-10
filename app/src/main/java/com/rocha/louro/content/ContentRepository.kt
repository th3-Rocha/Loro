package com.rocha.louro.content

import android.content.res.AssetManager

/**
 * Reads the content tree from assets. The app knows nothing about any language:
 * one folder per language, one `level-NNNN` folder per level, in name order.
 */
class ContentRepository(private val assets: AssetManager) {

    fun languages(): List<Language> =
        assets.list(ROOT).orEmpty().sorted().mapNotNull { id ->
            runCatching {
                Language(id, ContentJson.decodeFromString<LanguageInfo>(read("$ROOT/$id/language.json")))
            }.getOrNull()
        }

    fun levels(languageId: String): List<LevelEntry> =
        assets.list("$ROOT/$languageId").orEmpty()
            .filter { it.startsWith("level-") }
            .sorted()
            .mapNotNull { id ->
                val path = "$ROOT/$languageId/$id"
                runCatching {
                    LevelEntry(id, path, ContentJson.decodeFromString<Level>(read("$path/level.json")))
                }.getOrNull()
            }

    private fun read(path: String): String = assets.open(path).bufferedReader().use { it.readText() }

    companion object {
        const val ROOT = "content"
    }
}
