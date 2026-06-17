package com.fitnessapp.data.local.security

import java.security.MessageDigest

object PasswordHasher {
    fun hash(
        username: String,
        password: String
    ): String {
        val saltedPassword = "${username.trim().lowercase()}:$password"
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(saltedPassword.toByteArray(Charsets.UTF_8))

        return digest.joinToString(separator = "") { byte ->
            "%02x".format(byte)
        }
    }
}
