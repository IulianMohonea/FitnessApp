package com.fitnessapp.data.remote.model

enum class ExerciseDifficulty(
    val apiLevel: String,
    val displayName: String
) {
    Beginner(
        apiLevel = "Beginner",
        displayName = "Beginner"
    ),
    Intermediate(
        apiLevel = "Intermediate",
        displayName = "Intermediate"
    ),
    Hard(
        apiLevel = "Expert",
        displayName = "Hard"
    );

    fun matches(level: String): Boolean {
        return level.equals(apiLevel, ignoreCase = true) ||
            level.equals(displayName, ignoreCase = true)
    }

    companion object {
        val default: ExerciseDifficulty = Beginner
        const val GRADUATION_GOAL = 10

        fun fromPreference(value: String?): ExerciseDifficulty {
            return entries.firstOrNull { difficulty ->
                difficulty.name == value ||
                    difficulty.apiLevel.equals(value, ignoreCase = true) ||
                    difficulty.displayName.equals(value, ignoreCase = true)
            } ?: default
        }
    }
}
