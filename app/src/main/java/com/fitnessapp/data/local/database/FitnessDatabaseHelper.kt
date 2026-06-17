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
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $USERS_TABLE (
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

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        db.execSQL("DROP TABLE IF EXISTS $USERS_TABLE")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "fitness_app.db"
        private const val DATABASE_VERSION = 1

        const val USERS_TABLE = "users"
        const val USER_ID = "id"
        const val USER_NAME = "name"
        const val USER_USERNAME = "username"
        const val USER_EMAIL = "email"
        const val USER_PASSWORD_HASH = "password_hash"
        const val USER_CREATED_AT = "created_at"
    }
}
