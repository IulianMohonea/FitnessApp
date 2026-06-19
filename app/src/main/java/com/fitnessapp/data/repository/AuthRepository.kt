package com.fitnessapp.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import com.fitnessapp.data.local.database.FitnessDatabaseHelper
import com.fitnessapp.data.local.database.FitnessDatabaseHelper.Companion.USERS_TABLE
import com.fitnessapp.data.local.database.FitnessDatabaseHelper.Companion.USER_CREATED_AT
import com.fitnessapp.data.local.database.FitnessDatabaseHelper.Companion.USER_EMAIL
import com.fitnessapp.data.local.database.FitnessDatabaseHelper.Companion.USER_ID
import com.fitnessapp.data.local.database.FitnessDatabaseHelper.Companion.USER_NAME
import com.fitnessapp.data.local.database.FitnessDatabaseHelper.Companion.USER_PASSWORD_HASH
import com.fitnessapp.data.local.database.FitnessDatabaseHelper.Companion.USER_USERNAME
import com.fitnessapp.data.local.model.User
import com.fitnessapp.data.local.security.PasswordHasher

class AuthRepository(context: Context) {
    private val databaseHelper = FitnessDatabaseHelper(context.applicationContext)

    fun login(
        username: String,
        password: String
    ): AuthResult {
        val cleanUsername = username.trim()

        if (cleanUsername.isBlank() || password.isBlank()) {
            return AuthResult.Error("Enter your username and password.")
        }

        val user = findUserByUsername(cleanUsername)
            ?: return AuthResult.Error("Account not found. Sign up first.")

        val expectedHash = PasswordHasher.hash(cleanUsername, password)
        val storedHash = findPasswordHash(cleanUsername)

        return if (storedHash == expectedHash) {
            AuthResult.Success(user)
        } else {
            AuthResult.Error("Wrong password.")
        }
    }

    fun signUp(
        name: String,
        username: String,
        email: String,
        password: String
    ): AuthResult {
        val cleanName = name.trim()
        val cleanUsername = username.trim()
        val cleanEmail = email.trim().lowercase()

        if (
            cleanName.isBlank() ||
            cleanUsername.isBlank() ||
            cleanEmail.isBlank() ||
            password.isBlank()
        ) {
            return AuthResult.Error("Complete all sign up fields.")
        }

        if (!cleanEmail.contains("@")) {
            return AuthResult.Error("Enter a valid email.")
        }

        if (password.length < 4) {
            return AuthResult.Error("Password needs at least 4 characters.")
        }

        val values = ContentValues().apply {
            put(USER_NAME, cleanName)
            put(USER_USERNAME, cleanUsername)
            put(USER_EMAIL, cleanEmail)
            put(USER_PASSWORD_HASH, PasswordHasher.hash(cleanUsername, password))
            put(USER_CREATED_AT, System.currentTimeMillis())
        }

        return try {
            val userId = databaseHelper.writableDatabase.insertOrThrow(
                USERS_TABLE,
                null,
                values
            )

            AuthResult.Success(
                User(
                    id = userId,
                    name = cleanName,
                    username = cleanUsername,
                    email = cleanEmail
                )
            )
        } catch (_: SQLiteConstraintException) {
            AuthResult.Error("Username or email already exists.")
        }
    }

    fun findUserById(userId: Long): User? {
        if (userId <= 0L) return null

        val db = databaseHelper.readableDatabase
        val cursor = db.query(
            USERS_TABLE,
            arrayOf(USER_ID, USER_NAME, USER_USERNAME, USER_EMAIL),
            "$USER_ID = ?",
            arrayOf(userId.toString()),
            null,
            null,
            null,
            "1"
        )

        cursor.use {
            if (!it.moveToFirst()) return null

            return User(
                id = it.getLong(it.getColumnIndexOrThrow(USER_ID)),
                name = it.getString(it.getColumnIndexOrThrow(USER_NAME)),
                username = it.getString(it.getColumnIndexOrThrow(USER_USERNAME)),
                email = it.getString(it.getColumnIndexOrThrow(USER_EMAIL))
            )
        }
    }

    private fun findUserByUsername(username: String): User? {
        val db = databaseHelper.readableDatabase
        val cursor = db.query(
            USERS_TABLE,
            arrayOf(USER_ID, USER_NAME, USER_USERNAME, USER_EMAIL),
            "$USER_USERNAME = ?",
            arrayOf(username),
            null,
            null,
            null,
            "1"
        )

        cursor.use {
            if (!it.moveToFirst()) return null

            return User(
                id = it.getLong(it.getColumnIndexOrThrow(USER_ID)),
                name = it.getString(it.getColumnIndexOrThrow(USER_NAME)),
                username = it.getString(it.getColumnIndexOrThrow(USER_USERNAME)),
                email = it.getString(it.getColumnIndexOrThrow(USER_EMAIL))
            )
        }
    }

    private fun findPasswordHash(username: String): String? {
        val db = databaseHelper.readableDatabase
        val cursor = db.query(
            USERS_TABLE,
            arrayOf(USER_PASSWORD_HASH),
            "$USER_USERNAME = ?",
            arrayOf(username),
            null,
            null,
            null,
            "1"
        )

        cursor.use {
            if (!it.moveToFirst()) return null

            return it.getString(it.getColumnIndexOrThrow(USER_PASSWORD_HASH))
        }
    }
}

sealed interface AuthResult {
    data class Success(val user: User) : AuthResult
    data class Error(val message: String) : AuthResult
}
