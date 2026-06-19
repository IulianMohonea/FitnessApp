package com.fitnessapp.data.local.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FitnessDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        createUsersTable(db)
        createExerciseHistoryTable(db)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        if (oldVersion < 2) {
            createExerciseHistoryTable(db)
            return
        }

        if (oldVersion < 3) {
            db.execSQL(
                """
                ALTER TABLE $EXERCISE_HISTORY_TABLE
                ADD COLUMN $EXERCISE_HISTORY_LEVEL TEXT NOT NULL DEFAULT 'Beginner'
                """.trimIndent()
            )
        }

        if (oldVersion > 3) {
            db.execSQL("DROP TABLE IF EXISTS $EXERCISE_HISTORY_TABLE")
            db.execSQL("DROP TABLE IF EXISTS $USERS_TABLE")
            onCreate(db)
        }
    }

    private fun createUsersTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $USERS_TABLE (
                $USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $USER_NAME TEXT NOT NULL,
                $USER_USERNAME TEXT NOT NULL UNIQUE,
                $USER_EMAIL TEXT NOT NULL UNIQUE,
                $USER_PASSWORD_HASH TEXT NOT NULL,
                $USER_CREATED_AT INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    private fun createExerciseHistoryTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $EXERCISE_HISTORY_TABLE (
                $EXERCISE_HISTORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $EXERCISE_HISTORY_USER_ID INTEGER NOT NULL,
                $EXERCISE_HISTORY_NAME TEXT NOT NULL,
                $EXERCISE_HISTORY_CATEGORY TEXT NOT NULL,
                $EXERCISE_HISTORY_LEVEL TEXT NOT NULL,
                $EXERCISE_HISTORY_SUMMARY TEXT NOT NULL,
                $EXERCISE_HISTORY_DURATION_MINUTES INTEGER NOT NULL,
                $EXERCISE_HISTORY_CALORIES_BURNED INTEGER NOT NULL,
                $EXERCISE_HISTORY_COMPLETED_AT INTEGER NOT NULL,
                FOREIGN KEY($EXERCISE_HISTORY_USER_ID)
                    REFERENCES $USERS_TABLE($USER_ID)
                    ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS ${EXERCISE_HISTORY_TABLE}_user_date_index
            ON $EXERCISE_HISTORY_TABLE (
                $EXERCISE_HISTORY_USER_ID,
                $EXERCISE_HISTORY_COMPLETED_AT
            )
            """.trimIndent()
        )
    }

    companion object {
        private const val DATABASE_NAME = "fitness_app.db"
        private const val DATABASE_VERSION = 3

        const val USERS_TABLE = "users"
        const val USER_ID = "id"
        const val USER_NAME = "name"
        const val USER_USERNAME = "username"
        const val USER_EMAIL = "email"
        const val USER_PASSWORD_HASH = "password_hash"
        const val USER_CREATED_AT = "created_at"

        const val EXERCISE_HISTORY_TABLE = "exercise_history"
        const val EXERCISE_HISTORY_ID = "id"
        const val EXERCISE_HISTORY_USER_ID = "user_id"
        const val EXERCISE_HISTORY_NAME = "name"
        const val EXERCISE_HISTORY_CATEGORY = "category"
        const val EXERCISE_HISTORY_LEVEL = "level"
        const val EXERCISE_HISTORY_SUMMARY = "summary"
        const val EXERCISE_HISTORY_DURATION_MINUTES = "duration_minutes"
        const val EXERCISE_HISTORY_CALORIES_BURNED = "calories_burned"
        const val EXERCISE_HISTORY_COMPLETED_AT = "completed_at"
    }
}
