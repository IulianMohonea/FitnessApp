package com.fitnessapp.data.local.model

data class ExerciseHistoryRecord(
    val id: Long,
    val userId: Long,
    val name: String,
    val category: String,
    val level: String,
    val summary: String,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val completedAt: Long
)
