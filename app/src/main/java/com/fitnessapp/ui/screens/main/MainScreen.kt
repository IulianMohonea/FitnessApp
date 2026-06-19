package com.fitnessapp.ui.screens.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fitnessapp.data.local.model.ExerciseHistoryRecord
import com.fitnessapp.data.remote.model.ExerciseDifficulty
import com.fitnessapp.data.remote.model.RemoteExerciseIdea
import com.fitnessapp.data.repository.AppPreferencesRepository
import com.fitnessapp.data.repository.RemoteExerciseRepository
import com.fitnessapp.data.repository.RemoteExerciseResult
import com.fitnessapp.data.repository.WorkoutRepository
import com.fitnessapp.ui.navigation.FitnessBottomNavigationBar
import com.fitnessapp.ui.navigation.MainTab
import com.fitnessapp.ui.screens.history.ExerciseHistoryScreen
import com.fitnessapp.ui.screens.home.HomeChallenge
import com.fitnessapp.ui.screens.home.HomeScreen
import com.fitnessapp.ui.screens.statistics.StatisticsScreen
import com.fitnessapp.ui.theme.FitnessAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MainScreen(
    username: String,
    userId: Long,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferencesRepository = remember {
        AppPreferencesRepository(context.applicationContext)
    }
    val workoutRepository = remember {
        WorkoutRepository(context.applicationContext)
    }
    val remoteExerciseRepository = remember {
        RemoteExerciseRepository()
    }
    val scope = rememberCoroutineScope()
    val weeklyGoal = remember { preferencesRepository.getWeeklyGoal() }
    val startTab = remember { preferencesRepository.getLastMainTab().toMainTab() }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedTab = navBackStackEntry?.destination?.route?.toMainTab() ?: startTab

    var selectedDifficulty by remember {
        mutableStateOf(preferencesRepository.getExerciseDifficulty())
    }
    var historyRecords by remember {
        mutableStateOf<List<ExerciseHistoryRecord>>(emptyList())
    }
    var isHistoryLoading by remember { mutableStateOf(true) }
    var remoteExercises by remember {
        mutableStateOf<List<RemoteExerciseIdea>>(emptyList())
    }
    var isRemoteLoading by remember { mutableStateOf(false) }
    var remoteErrorMessage by remember { mutableStateOf<String?>(null) }
    val availableDifficulties = remember(historyRecords) {
        historyRecords.availableDifficulties()
    }
    val activeDifficulty = if (selectedDifficulty in availableDifficulties) {
        selectedDifficulty
    } else {
        ExerciseDifficulty.default
    }

    LaunchedEffect(activeDifficulty, selectedDifficulty) {
        if (activeDifficulty != selectedDifficulty) {
            selectedDifficulty = activeDifficulty
            preferencesRepository.saveExerciseDifficulty(activeDifficulty)
        }
    }

    LaunchedEffect(userId) {
        isHistoryLoading = true
        historyRecords = withContext(Dispatchers.IO) {
            workoutRepository.getHistory(userId)
        }
        isHistoryLoading = false
    }

    LaunchedEffect(activeDifficulty) {
        isRemoteLoading = true
        remoteErrorMessage = null

        when (val result = withContext(Dispatchers.IO) {
            remoteExerciseRepository.fetchExerciseIdeas(
                difficulty = activeDifficulty
            )
        }) {
            is RemoteExerciseResult.Success -> {
                remoteExercises = result.exercises
            }

            is RemoteExerciseResult.Error -> {
                remoteErrorMessage = result.message
            }
        }

        isRemoteLoading = false
    }

    fun selectTab(tab: MainTab) {
        preferencesRepository.saveLastMainTab(tab.route)
        navController.navigate(tab.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun selectDifficulty(difficulty: ExerciseDifficulty) {
        selectedDifficulty = difficulty
        preferencesRepository.saveExerciseDifficulty(difficulty)
    }

    fun logCompletedChallenge(challenge: HomeChallenge) {
        if (userId <= 0L) return

        scope.launch {
            isHistoryLoading = true
            historyRecords = withContext(Dispatchers.IO) {
                workoutRepository.addWorkout(
                    userId = userId,
                    name = challenge.name,
                    category = challenge.category,
                    level = challenge.level,
                    summary = challenge.summary,
                    durationMinutes = challenge.durationMinutes,
                    caloriesBurned = challenge.caloriesBurned
                )
                workoutRepository.getHistory(userId)
            }
            isHistoryLoading = false
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            FitnessBottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectTab(it) }
            )
        }
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)

        NavHost(
            navController = navController,
            startDestination = startTab.route,
            modifier = contentModifier
        ) {
            composable(MainTab.Statistics.route) {
                StatisticsScreen(
                    historyRecords = historyRecords,
                    isLoading = isHistoryLoading,
                    weeklyGoal = weeklyGoal,
                    selectedDifficulty = activeDifficulty,
                    availableDifficulties = availableDifficulties,
                    onDifficultySelected = ::selectDifficulty
                )
            }

            composable(MainTab.Home.route) {
                HomeScreen(
                    username = username,
                    historyRecords = historyRecords,
                    isHistoryLoading = isHistoryLoading,
                    selectedDifficulty = activeDifficulty,
                    remoteExercises = remoteExercises,
                    isRemoteLoading = isRemoteLoading,
                    remoteErrorMessage = remoteErrorMessage,
                    onChallengeCompleted = ::logCompletedChallenge
                )
            }

            composable(MainTab.History.route) {
                ExerciseHistoryScreen(
                    historyItems = historyRecords,
                    isLoading = isHistoryLoading
                )
            }
        }
    }
}

private fun String?.toMainTab(): MainTab {
    return MainTab.entries.firstOrNull { tab ->
        tab.name == this || tab.route == this
    } ?: MainTab.Home
}

private fun List<ExerciseHistoryRecord>.availableDifficulties(): List<ExerciseDifficulty> {
    val beginnerCompleted = count { record ->
        ExerciseDifficulty.Beginner.matches(record.level)
    }
    val intermediateCompleted = count { record ->
        ExerciseDifficulty.Intermediate.matches(record.level)
    }
    val difficulties = mutableListOf(ExerciseDifficulty.Beginner)

    val isIntermediateUnlocked = beginnerCompleted >= ExerciseDifficulty.GRADUATION_GOAL
    if (isIntermediateUnlocked) {
        difficulties += ExerciseDifficulty.Intermediate
    }

    if (isIntermediateUnlocked && intermediateCompleted >= ExerciseDifficulty.GRADUATION_GOAL) {
        difficulties += ExerciseDifficulty.Hard
    }

    return difficulties
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MainScreenPreview() {
    FitnessAppTheme {
        MainScreen(
            username = "alex",
            userId = 1L
        )
    }
}
