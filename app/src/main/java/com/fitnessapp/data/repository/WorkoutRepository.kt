package com.fitnessapp.data.repository

import android.content.ContentValues
import android.content.Context
import com.fitnessapp.data.local.database.FitnessDatabaseHelper
import com.fitnessapp.data.local.database.FitnessDatabaseHelper.Companion.EXERCISE_HISTORY_CALORIES_BURNED
import com.fitnessapp.data.local.database.FitnessDatabaseHelper.Companion.EXERCISE_HISTORY_CATEGORY
import com.fitnessapp.data.local.database.FitnessDatabaseHelper.Companion.EXERCISE_HISTORY_COMPLETED_AT
import com.fitnessapp.data.local.database.FitnessDatabaseHelper.Companion.EXERCISE_HISTORY_DURATION_MINUTES
import com.fitnessapp.data.local.database.FitnessDatabaseHelper.Companion.EXERCISE_HISTORY_ID
import com.fitnessapp.data.local.database.FitnessDatabaseHelper.Companion.EXERCISE_HISTORY_LEVEL
import com.fitnessapp.data.local.database.FitnessDatabaseHelper.Companion.EXERCISE_HISTORY_NAME
import com.fitnessapp.data.local.database.FitnessDatabaseHelper.Companion.EXERCISE_HISTORY_SUMMARY
import com.fitnessapp.data.local.database.FitnessDatabaseHelper.Companion.EXERCISE_HISTORY_TABLE
import com.fitnessapp.data.local.database.FitnessDatabaseHelper.Companion.EXERCISE_HISTORY_USER_ID
import com.fitnessapp.data.local.model.ExerciseHistoryRecord

class WorkoutRepository(context: Context) {
    private val databaseHelper = FitnessDatabaseHelper(context.applicationContext)

    fun addWorkout(
        userId: Long,
        name: String,
        category: String,
        level: String,
        summary: String,
        durationMinutes: Int,
        caloriesBurned: Int,
        completedAt: Long = System.currentTimeMillis()
    ): Long {
        val values = ContentValues().apply {
            put(EXERCISE_HISTORY_USER_ID, userId)
            put(EXERCISE_HISTORY_NAME, name.trim())
            put(EXERCISE_HISTORY_CATEGORY, category.trim())
            put(EXERCISE_HISTORY_LEVEL, level.trim())
            put(EXERCISE_HISTORY_SUMMARY, summary.trim())
            put(EXERCISE_HISTORY_DURATION_MINUTES, durationMinutes)
            put(EXERCISE_HISTORY_CALORIES_BURNED, caloriesBurned)
            put(EXERCISE_HISTORY_COMPLETED_AT, completedAt)
        }

        return databaseHelper.writableDatabase.insert(
            EXERCISE_HISTORY_TABLE,
            null,
            values
        )
    }

    fun getHistory(userId: Long): List<ExerciseHistoryRecord> {
        if (userId <= 0L) return emptyList()

        val cursor = databaseHelper.readableDatabase.query(
            EXERCISE_HISTORY_TABLE,
            arrayOf(
                EXERCISE_HISTORY_ID,
                EXERCISE_HISTORY_USER_ID,
                EXERCISE_HISTORY_NAME,
                EXERCISE_HISTORY_CATEGORY,
                EXERCISE_HISTORY_LEVEL,
                EXERCISE_HISTORY_SUMMARY,
                EXERCISE_HISTORY_DURATION_MINUTES,
                EXERCISE_HISTORY_CALORIES_BURNED,
                EXERCISE_HISTORY_COMPLETED_AT
            ),
            "$EXERCISE_HISTORY_USER_ID = ?",
            arrayOf(userId.toString()),
            null,
            null,
            "$EXERCISE_HISTORY_COMPLETED_AT DESC"
        )

        cursor.use {
            val history = mutableListOf<ExerciseHistoryRecord>()

            while (it.moveToNext()) {
                history += ExerciseHistoryRecord(
                    id = it.getLong(it.getColumnIndexOrThrow(EXERCISE_HISTORY_ID)),
                    userId = it.getLong(it.getColumnIndexOrThrow(EXERCISE_HISTORY_USER_ID)),
                    name = it.getString(it.getColumnIndexOrThrow(EXERCISE_HISTORY_NAME)),
                    category = it.getString(it.getColumnIndexOrThrow(EXERCISE_HISTORY_CATEGORY)),
                    level = it.getString(it.getColumnIndexOrThrow(EXERCISE_HISTORY_LEVEL)),
                    summary = it.getString(it.getColumnIndexOrThrow(EXERCISE_HISTORY_SUMMARY)),
                    durationMinutes = it.getInt(
                        it.getColumnIndexOrThrow(EXERCISE_HISTORY_DURATION_MINUTES)
                    ),
                    caloriesBurned = it.getInt(
                        it.getColumnIndexOrThrow(EXERCISE_HISTORY_CALORIES_BURNED)
                    ),
                    completedAt = it.getLong(
                        it.getColumnIndexOrThrow(EXERCISE_HISTORY_COMPLETED_AT)
                    )
                )
            }

            return history
        }
    }
}
