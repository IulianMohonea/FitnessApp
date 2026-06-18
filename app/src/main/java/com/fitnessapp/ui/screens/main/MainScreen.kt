package com.fitnessapp.ui.screens.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.fitnessapp.ui.navigation.FitnessBottomNavigationBar
import com.fitnessapp.ui.navigation.MainTab
import com.fitnessapp.ui.screens.history.ExerciseHistoryScreen
import com.fitnessapp.ui.screens.home.HomeScreen
import com.fitnessapp.ui.screens.statistics.StatisticsScreen
import com.fitnessapp.ui.theme.FitnessAppTheme

@Composable
fun MainScreen(
    username: String,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(MainTab.Home) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            FitnessBottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)

        when (selectedTab) {
            MainTab.Statistics -> StatisticsScreen(modifier = contentModifier)
            MainTab.Home -> HomeScreen(
                username = username,
                modifier = contentModifier
            )
            MainTab.History -> ExerciseHistoryScreen(modifier = contentModifier)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MainScreenPreview() {
    FitnessAppTheme {
        MainScreen(username = "alex")
    }
}
