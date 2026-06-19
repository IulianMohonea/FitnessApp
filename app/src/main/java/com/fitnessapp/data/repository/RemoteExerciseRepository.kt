package com.fitnessapp.data.repository

import com.fitnessapp.data.remote.model.ExerciseDifficulty
import com.fitnessapp.data.remote.model.RemoteExerciseIdea
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class RemoteExerciseRepository {
    fun fetchExerciseIdeas(
        exercisesPerCategory: Int = 5,
        difficulty: ExerciseDifficulty? = null
    ): RemoteExerciseResult {
        return try {
            val metadata = JSONObject(fetchText(EXERCISE_METADATA_URL))
            val downloadUrl = metadata.optString("download_url")
                .takeIf { it.isNotBlank() }
                ?: EXERCISE_RAW_URL

            val exerciseArray = JSONArray(fetchText(downloadUrl))
            val exercises = mutableListOf<RemoteExerciseIdea>()
            val categoryCounts = mutableMapOf<String, Int>()

            for (index in 0 until exerciseArray.length()) {
                val exercise = exerciseArray.getJSONObject(index)
                val name = exercise.optCleanString("name", fallback = "")
                val category = exercise.optCleanString("category", fallback = "Exercise")
                    .titleCase()
                val level = exercise.optCleanString("level", fallback = "Any level").titleCase()
                val currentCategoryCount = categoryCounts[category] ?: 0

                if (name.isBlank()) continue
                if (difficulty != null && !difficulty.matches(level)) continue
                if (currentCategoryCount >= exercisesPerCategory) continue

                exercises += RemoteExerciseIdea(
                    name = name,
                    category = category,
                    level = level,
                    equipment = exercise.optCleanString("equipment", fallback = "No equipment")
                        .titleCase(),
                    description = exercise.optInstructionDescription()
                )
                categoryCounts[category] = currentCategoryCount + 1
            }

            RemoteExerciseResult.Success(exercises)
        } catch (exception: Exception) {
            RemoteExerciseResult.Error(
                message = exception.message ?: "Could not load remote exercises."
            )
        }
    }

    private fun fetchText(urlString: String): String {
        val connection = URL(urlString).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/json")

        return try {
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                throw IOException("Request failed with HTTP $responseCode.")
            }

            connection.inputStream.bufferedReader().use { reader ->
                reader.readText()
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun JSONObject.optCleanString(
        key: String,
        fallback: String
    ): String {
        val value = opt(key)

        return if (value == null || value == JSONObject.NULL) {
            fallback
        } else {
            value.toString().ifBlank { fallback }
        }
    }

    private fun JSONObject.optInstructionDescription(): String {
        val instructions = optJSONArray("instructions") ?: return DEFAULT_DESCRIPTION
        val steps = mutableListOf<String>()

        for (index in 0 until instructions.length()) {
            val step = instructions.optString(index).trim()
            if (step.isNotBlank()) {
                steps += "${index + 1}. $step"
            }
        }

        return steps.joinToString(separator = "\n\n")
            .ifBlank { DEFAULT_DESCRIPTION }
    }

    private fun String.titleCase(): String {
        return replaceFirstChar { char ->
            if (char.isLowerCase()) {
                char.titlecase()
            } else {
                char.toString()
            }
        }
    }

    companion object {
        private const val EXERCISE_METADATA_URL =
            "https://api.github.com/repos/yuhonas/free-exercise-db/contents/dist/exercises.json"
        private const val EXERCISE_RAW_URL =
            "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/dist/exercises.json"
        private const val DEFAULT_DESCRIPTION =
            "No detailed instructions were provided by the API for this exercise."
    }
}

sealed interface RemoteExerciseResult {
    data class Success(val exercises: List<RemoteExerciseIdea>) : RemoteExerciseResult
    data class Error(val message: String) : RemoteExerciseResult
}
