package com.fitnessapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.fitnessapp.data.local.model.User
import com.fitnessapp.data.repository.AppPreferencesRepository
import com.fitnessapp.data.repository.AuthRepository
import com.fitnessapp.data.repository.AuthResult
import com.fitnessapp.ui.screens.auth.LoginScreen
import com.fitnessapp.ui.screens.auth.SignUpScreen
import com.fitnessapp.ui.screens.main.MainScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FitnessApp() {
    val context = LocalContext.current
    val authRepository = remember {
        AuthRepository(context.applicationContext)
    }
    val preferencesRepository = remember {
        AppPreferencesRepository(context.applicationContext)
    }
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    var currentUser by remember { mutableStateOf<User?>(null) }
    var authMessage by remember { mutableStateOf<String?>(null) }
    var isAuthLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val savedUserId = preferencesRepository.getLoggedInUserId()
        val savedUser = withContext(Dispatchers.IO) {
            authRepository.findUserById(savedUserId)
        }

        if (savedUser == null) {
            preferencesRepository.clearLoggedInUserId()
            navController.navigate(AppRoute.Login) {
                popUpTo(AppRoute.Loading) { inclusive = true }
            }
        } else {
            currentUser = savedUser
            navController.navigate(AppRoute.Main) {
                popUpTo(AppRoute.Loading) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoute.Loading
    ) {
        composable(AppRoute.Loading) {
            SessionLoadingScreen()
        }

        composable(AppRoute.Login) {
            LoginScreen(
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
                                preferencesRepository.saveLastUsername(result.user.username)
                                preferencesRepository.saveLoggedInUserId(result.user.id)
                                navController.navigate(AppRoute.Main) {
                                    popUpTo(AppRoute.Login) { inclusive = true }
                                }
                            }

                            is AuthResult.Error -> authMessage = result.message
                        }
                    }
                },
                onSignUpClick = {
                    authMessage = null
                    navController.navigate(AppRoute.SignUp)
                }
            )
        }

        composable(AppRoute.SignUp) {
            SignUpScreen(
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
                                preferencesRepository.saveLastUsername(result.user.username)
                                preferencesRepository.saveLoggedInUserId(result.user.id)
                                navController.navigate(AppRoute.Main) {
                                    popUpTo(AppRoute.SignUp) { inclusive = true }
                                }
                            }

                            is AuthResult.Error -> authMessage = result.message
                        }
                    }
                },
                onBackToLoginClick = {
                    authMessage = null
                    navController.navigate(AppRoute.Login) {
                        popUpTo(AppRoute.SignUp) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoute.Main) {
            MainScreen(
                username = currentUser?.username ?: "guest",
                userId = currentUser?.id ?: 0L,
                onLogoutClick = {
                    currentUser = null
                    authMessage = null
                    preferencesRepository.clearLoggedInUserId()
                    navController.navigate(AppRoute.Login) {
                        popUpTo(AppRoute.Main) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
private fun SessionLoadingScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Loading your session...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private object AppRoute {
    const val Loading = "loading"
    const val Login = "login"
    const val SignUp = "sign_up"
    const val Main = "main"
}
