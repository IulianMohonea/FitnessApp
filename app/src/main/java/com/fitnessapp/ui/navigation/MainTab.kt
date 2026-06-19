package com.fitnessapp.ui.navigation

enum class MainTab(
    val label: String,
    val shortLabel: String,
    val route: String
) {
    Statistics(
        label = "Statistics",
        shortLabel = "Stats",
        route = "statistics"
    ),
    Home(
        label = "Home",
        shortLabel = "Home",
        route = "home"
    ),
    History(
        label = "History",
        shortLabel = "Hist",
        route = "history"
    )
}
