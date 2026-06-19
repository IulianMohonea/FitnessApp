package com.fitnessapp.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fitnessapp.data.local.model.ExerciseHistoryRecord
import com.fitnessapp.data.remote.model.ExerciseDifficulty
import com.fitnessapp.ui.theme.FitnessAppTheme

@Composable
fun StatisticsScreen(
    historyRecords: List<ExerciseHistoryRecord>,
    isLoading: Boolean,
    weeklyGoal: Int,
    selectedDifficulty: ExerciseDifficulty,
    availableDifficulties: List<ExerciseDifficulty>,
    onDifficultySelected: (ExerciseDifficulty) -> Unit,
    modifier: Modifier = Modifier
) {
    val stats = remember(historyRecords, weeklyGoal, selectedDifficulty) {
        WorkoutStats.from(
            historyRecords = historyRecords,
            weeklyGoal = weeklyGoal,
            selectedDifficulty = selectedDifficulty
        )
    }
    val weeklyStats = listOf(
        StatisticSummary(
            label = "Workouts",
            value = stats.weeklyWorkouts.toString(),
            note = "${stats.totalWorkouts} total",
            color = MaterialTheme.colorScheme.primary
        ),
        StatisticSummary(
            label = "Minutes",
            value = stats.weeklyMinutes.toString(),
            note = "${stats.totalMinutes} total",
            color = MaterialTheme.colorScheme.tertiary
        ),
        StatisticSummary(
            label = "Calories",
            value = stats.weeklyCalories.toString(),
            note = "${stats.totalCalories} total",
            color = MaterialTheme.colorScheme.secondary
        )
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StatisticsHeader()
            }

            item {
                DifficultySelector(
                    selectedDifficulty = selectedDifficulty,
                    availableDifficulties = availableDifficulties,
                    onDifficultySelected = onDifficultySelected
                )
            }

            if (isLoading) {
                item {
                    StatisticsStatusCard(message = "Loading workout statistics...")
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(weeklyStats) { stat ->
                        StatisticCard(stat = stat)
                    }
                }
            }

            item {
                WeeklyGoalCard(
                    completedWorkouts = stats.weeklyWorkouts,
                    weeklyGoal = weeklyGoal
                )
            }

            item {
                GraduationProgressCard(
                    selectedDifficulty = selectedDifficulty,
                    completedExercises = stats.completedForDifficulty
                )
            }

            item {
                Text(
                    text = "Focus progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (stats.focusProgress.isEmpty() && !isLoading) {
                item {
                    StatisticsStatusCard(
                        message = "No workout categories yet. Completed workouts will appear here."
                    )
                }
            } else {
                items(stats.focusProgress) { stat ->
                    FocusProgressRow(progress = stat)
                }
            }
        }
    }
}

@Composable
private fun DifficultySelector(
    selectedDifficulty: ExerciseDifficulty,
    availableDifficulties: List<ExerciseDifficulty>,
    onDifficultySelected: (ExerciseDifficulty) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Training level",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(availableDifficulties) { difficulty ->
                FilterChip(
                    selected = selectedDifficulty == difficulty,
                    onClick = { onDifficultySelected(difficulty) },
                    label = { Text(text = difficulty.displayName) }
                )
            }
        }
    }
}

@Composable
private fun StatisticsHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Statistics",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "A quick overview calculated from your saved workout history.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatisticCard(stat: StatisticSummary) {
    Card(
        modifier = Modifier.width(152.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(10.dp)
                    .clip(CircleShape)
                    .background(stat.color)
            )
            Text(
                text = stat.value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stat.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stat.note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WeeklyGoalCard(
    completedWorkouts: Int,
    weeklyGoal: Int
) {
    val progress = if (weeklyGoal <= 0) {
        0f
    } else {
        completedWorkouts.toFloat() / weeklyGoal.toFloat()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Weekly goal",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f)
            )
            Text(
                text = "$completedWorkouts of $weeklyGoal workouts completed",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            ProgressBar(
                progress = progress,
                activeColor = MaterialTheme.colorScheme.onPrimary,
                trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.24f)
            )
            Text(
                text = if (completedWorkouts >= weeklyGoal) {
                    "Weekly goal completed. Nice consistency."
                } else {
                    "${weeklyGoal - completedWorkouts} more sessions to finish the week."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun GraduationProgressCard(
    selectedDifficulty: ExerciseDifficulty,
    completedExercises: Int
) {
    val progress = completedExercises.toFloat() / ExerciseDifficulty.GRADUATION_GOAL.toFloat()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "${selectedDifficulty.displayName} graduation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$completedExercises of ${ExerciseDifficulty.GRADUATION_GOAL} exercises completed",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ProgressBar(
                progress = progress,
                activeColor = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = if (completedExercises >= ExerciseDifficulty.GRADUATION_GOAL) {
                    "Level completed. You are ready to graduate."
                } else {
                    "${ExerciseDifficulty.GRADUATION_GOAL - completedExercises} more exercises to graduate."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FocusProgressRow(progress: FocusProgress) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = progress.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${progress.sessions} sessions - ${progress.totalMinutes} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            ProgressBar(
                progress = progress.progress,
                activeColor = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun StatisticsStatusCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProgressBar(
    progress: Float,
    activeColor: Color,
    trackColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(activeColor)
        )
    }
}

private data class WorkoutStats(
    val totalWorkouts: Int,
    val totalMinutes: Int,
    val totalCalories: Int,
    val weeklyWorkouts: Int,
    val weeklyMinutes: Int,
    val weeklyCalories: Int,
    val completedForDifficulty: Int,
    val focusProgress: List<FocusProgress>
) {
    companion object {
        fun from(
            historyRecords: List<ExerciseHistoryRecord>,
            weeklyGoal: Int,
            selectedDifficulty: ExerciseDifficulty
        ): WorkoutStats {
            val weekStart = System.currentTimeMillis() - WEEK_IN_MILLIS
            val weeklyRecords = historyRecords.filter { record ->
                record.completedAt >= weekStart
            }
            val groupedByCategory = historyRecords.groupBy { record ->
                record.category
            }
            val maxCategorySessions = groupedByCategory.values.maxOfOrNull { records ->
                records.size
            } ?: 0
            val normalizedGoal = weeklyGoal.coerceAtLeast(1)

            return WorkoutStats(
                totalWorkouts = historyRecords.size,
                totalMinutes = historyRecords.sumOf { record -> record.durationMinutes },
                totalCalories = historyRecords.sumOf { record -> record.caloriesBurned },
                weeklyWorkouts = weeklyRecords.size,
                weeklyMinutes = weeklyRecords.sumOf { record -> record.durationMinutes },
                weeklyCalories = weeklyRecords.sumOf { record -> record.caloriesBurned },
                completedForDifficulty = historyRecords.count { record ->
                    selectedDifficulty.matches(record.level)
                },
                focusProgress = groupedByCategory.map { (category, records) ->
                    FocusProgress(
                        name = category,
                        sessions = records.size,
                        totalMinutes = records.sumOf { record -> record.durationMinutes },
                        progress = records.size.toFloat() /
                            maxCategorySessions.coerceAtLeast(normalizedGoal).toFloat()
                    )
                }.sortedByDescending { progress -> progress.sessions }
            )
        }

        private const val WEEK_IN_MILLIS = 7L * 24L * 60L * 60L * 1000L
    }
}

private data class StatisticSummary(
    val label: String,
    val value: String,
    val note: String,
    val color: Color
)

private data class FocusProgress(
    val name: String,
    val sessions: Int,
    val totalMinutes: Int,
    val progress: Float
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StatisticsScreenPreview() {
    FitnessAppTheme {
        StatisticsScreen(
            historyRecords = listOf(
                ExerciseHistoryRecord(
                    id = 1L,
                    userId = 1L,
                    name = "Bodyweight Squats",
                    category = "Strength",
                    level = "Beginner",
                    summary = "3 sets x 12 reps",
                    durationMinutes = 18,
                    caloriesBurned = 120,
                    completedAt = System.currentTimeMillis()
                )
            ),
            isLoading = false,
            weeklyGoal = 7,
            selectedDifficulty = ExerciseDifficulty.Beginner,
            availableDifficulties = listOf(ExerciseDifficulty.Beginner),
            onDifficultySelected = {}
        )
    }
}
