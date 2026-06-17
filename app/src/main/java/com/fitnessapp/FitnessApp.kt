package com.fitnessapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.fitnessapp.data.local.model.User
import com.fitnessapp.data.repository.AuthRepository
import com.fitnessapp.data.repository.AuthResult
import com.fitnessapp.ui.screens.auth.LoginScreen
import com.fitnessapp.ui.screens.auth.SignUpScreen
import com.fitnessapp.ui.screens.home.HomeScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FitnessApp() {
    val context = LocalContext.current
    val authRepository = remember {
        AuthRepository(context.applicationContext)
    }
    val scope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf(AppScreen.Login) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var authMessage by remember { mutableStateOf<String?>(null) }
    var isAuthLoading by remember { mutableStateOf(false) }

    when (currentScreen) {
        AppScreen.Login -> LoginScreen(
            authMessage = authMessage,
            isLoading = isAuthLoading,
            onLoginClick = { username, password ->
                authMessage = null
                isAuthLoading = true
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        authRepository.login(
                            username = username,
                            password = password
                        )
                    }

                    isAuthLoading = false
                    when (result) {
                        is AuthResult.Success -> {
                            currentUser = result.user
                            currentScreen = AppScreen.Home
                        }

                        is AuthResult.Error -> authMessage = result.message
                    }
                }
            },
            onSignUpClick = {
                authMessage = null
                currentScreen = AppScreen.SignUp
            }
        )

        AppScreen.SignUp -> SignUpScreen(
            authMessage = authMessage,
            isLoading = isAuthLoading,
            onSignUpClick = { name, username, email, password ->
                authMessage = null
                isAuthLoading = true
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        authRepository.signUp(
                            name = name,
                            username = username,
                            email = email,
                            password = password
                        )
                    }

                    isAuthLoading = false
                    when (result) {
                        is AuthResult.Success -> {
                            currentUser = result.user
                            currentScreen = AppScreen.Home
                        }

                        is AuthResult.Error -> authMessage = result.message
                    }
                }
            },
            onBackToLoginClick = {
                authMessage = null
                currentScreen = AppScreen.Login
            }
        )

        AppScreen.Home -> HomeScreen(
            username = currentUser?.username ?: "guest"
        )
    }
}

private enum class AppScreen {
    Login,
    SignUp,
    Home
}
