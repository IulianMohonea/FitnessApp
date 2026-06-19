package com.fitnessapp.data.repository

import android.content.Context
import com.fitnessapp.data.remote.model.ExerciseDifficulty

class AppPreferencesRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    fun saveLastUsername(username: String) {
        preferences.edit()
            .putString(KEY_LAST_USERNAME, username)
            .apply()
    }

    fun getLastUsername(): String? = preferences.getString(KEY_LAST_USERNAME, null)

    fun saveLoggedInUserId(userId: Long) {
        preferences.edit()
            .putLong(KEY_LOGGED_IN_USER_ID, userId)
            .apply()
    }

    fun getLoggedInUserId(): Long {
        return preferences.getLong(KEY_LOGGED_IN_USER_ID, NO_LOGGED_IN_USER_ID)
    }

    fun clearLoggedInUserId() {
        preferences.edit()
            .remove(KEY_LOGGED_IN_USER_ID)
            .apply()
    }

    fun saveLastMainTab(tabName: String) {
        preferences.edit()
            .putString(KEY_LAST_MAIN_TAB, tabName)
            .apply()
    }

    fun getLastMainTab(): String? = preferences.getString(KEY_LAST_MAIN_TAB, null)

    fun getWeeklyGoal(): Int = preferences.getInt(KEY_WEEKLY_GOAL, DEFAULT_WEEKLY_GOAL)

    fun saveExerciseDifficulty(difficulty: ExerciseDifficulty) {
        preferences.edit()
            .putString(KEY_EXERCISE_DIFFICULTY, difficulty.name)
            .apply()
    }

    fun getExerciseDifficulty(): ExerciseDifficulty {
        return ExerciseDifficulty.fromPreference(
            preferences.getString(KEY_EXERCISE_DIFFICULTY, null)
        )
    }

    companion object {
        private const val PREFERENCES_NAME = "fitness_app_preferences"
        private const val KEY_LAST_USERNAME = "last_username"
        private const val KEY_LOGGED_IN_USER_ID = "logged_in_user_id"
        private const val KEY_LAST_MAIN_TAB = "last_main_tab"
        private const val KEY_WEEKLY_GOAL = "weekly_goal"
        private const val KEY_EXERCISE_DIFFICULTY = "exercise_difficulty"
        private const val DEFAULT_WEEKLY_GOAL = 7
        private const val NO_LOGGED_IN_USER_ID = 0L
    }
}
