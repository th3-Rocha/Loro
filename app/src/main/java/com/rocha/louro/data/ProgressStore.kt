package com.rocha.louro.data

import kotlinx.serialization.json.Json
import java.io.File

/** Progress lives in a single JSON file on the device; nothing ever leaves it. */
class ProgressStore(private val file: File) {
    private val json = Json { ignoreUnknownKeys = true }

    fun load(): Progress =
        runCatching { json.decodeFromString<Progress>(file.readText()) }.getOrDefault(Progress())

    fun save(progress: Progress) {
        val tmp = File(file.parentFile, file.name + ".tmp")
        tmp.writeText(json.encodeToString(Progress.serializer(), progress))
        tmp.renameTo(file)
    }
}
