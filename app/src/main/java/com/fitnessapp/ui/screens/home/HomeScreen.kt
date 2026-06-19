package com.fitnessapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fitnessapp.data.local.model.ExerciseHistoryRecord
import com.fitnessapp.data.remote.model.ExerciseDifficulty
import com.fitnessapp.data.remote.model.RemoteExerciseIdea
import com.fitnessapp.ui.theme.FitnessAppTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class HomeChallenge(
    val name: String,
    val category: String,
    val level: String,
    val summary: String,
    val description: String,
    val durationMinutes: Int,
    val caloriesBurned: Int
)

@Composable
fun HomeScreen(
    username: String = "guest",
    historyRecords: List<ExerciseHistoryRecord> = emptyList(),
    isHistoryLoading: Boolean = false,
    selectedDifficulty: ExerciseDifficulty = ExerciseDifficulty.default,
    remoteExercises: List<RemoteExerciseIdea> = emptyList(),
    isRemoteLoading: Boolean = false,
    remoteErrorMessage: String? = null,
    onChallengeCompleted: (HomeChallenge) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFocus by remember { mutableStateOf("All") }
    var selectedChallenge by remember { mutableStateOf<HomeChallenge?>(null) }
    var selectedApiExercise by remember { mutableStateOf<RemoteExerciseIdea?>(null) }
    val todaysChallenges = remember(remoteExercises) {
        remoteExercises.map { exercise ->
            exercise.toHomeChallenge()
        }
    }
    val focusOptions = remember(todaysChallenges) {
        listOf("All") + todaysChallenges.map { challenge ->
            challenge.category
        }.distinct().sorted()
    }
    val activeFocus = if (selectedFocus in focusOptions) selectedFocus else "All"
    val visibleChallenges = remember(todaysChallenges, activeFocus) {
        if (activeFocus == "All") {
            todaysChallenges
        } else {
            todaysChallenges.filter { challenge -> challenge.category == activeFocus }
        }
    }
    val completedTodayKeys = remember(historyRecords) {
        historyRecords
            .filter { record -> record.isCompletedToday() }
            .map { record -> record.completionKey }
            .toSet()
    }

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
                HomeHeader(username = username)
            }

            item {
                FocusSelector(
                    options = focusOptions,
                    selectedFocus = activeFocus,
                    onFocusSelected = { selectedFocus = it }
                )
            }

            item {
                ProgressStats(historyRecords = historyRecords)
            }

            item {
                Text(
                    text = "Today's ${selectedDifficulty.displayName} challenges",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isRemoteLoading) {
                item {
                    HomeStatusCard(message = "Loading today's API challenges...")
                }
            } else if (remoteErrorMessage != null) {
                item {
                    HomeStatusCard(message = "Could not load today's challenges: $remoteErrorMessage")
                }
            } else if (visibleChallenges.isEmpty()) {
                item {
                    HomeStatusCard(
                        message = "No ${selectedDifficulty.displayName.lowercase()} API challenges available yet."
                    )
                }
            } else {
                items(visibleChallenges) { challenge ->
                    ChallengeRow(
                        challenge = challenge,
                        isCompletedToday = challenge.completionKey in completedTodayKeys,
                        isSaving = isHistoryLoading,
                        onDetailsClick = { selectedChallenge = challenge },
                        onCompleteClick = { onChallengeCompleted(challenge) }
                    )
                }
            }

            item {
                RemoteExerciseSection(
                    selectedDifficulty = selectedDifficulty,
                    remoteExercises = remoteExercises,
                    isLoading = isRemoteLoading,
                    errorMessage = remoteErrorMessage,
                    onExerciseClick = { selectedApiExercise = it }
                )
            }
        }
    }

    selectedChallenge?.let { challenge ->
        ExerciseDescriptionDialog(
            title = challenge.name,
            subtitle = "${challenge.category} - ${challenge.level}",
            description = challenge.description,
            onClose = { selectedChallenge = null },
            completeButtonText = if (challenge.completionKey in completedTodayKeys) {
                null
            } else {
                "Complete"
            },
            onCompleteClick = {
                onChallengeCompleted(challenge)
                selectedChallenge = null
            }
        )
    }

    selectedApiExercise?.let { exercise ->
        ExerciseDescriptionDialog(
            title = exercise.name,
            subtitle = "${exercise.category} - ${exercise.level} - ${exercise.equipment}",
            description = exercise.description,
            onClose = { selectedApiExercise = null }
        )
    }
}

@Composable
private fun HomeHeader(username: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Welcome back, $username",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Ready for a light session?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Small steps count. Pick a focus and keep the streak alive.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ExerciseOfTheDayCard(
    challenge: HomeChallenge,
    isCompletedToday: Boolean,
    isSaving: Boolean,
    onCompleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Exercise of the day",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f)
            )
            Text(
                text = challenge.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = "${challenge.summary}. Tap Complete to save it to history.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
            )
            Button(
                onClick = onCompleteClick,
                enabled = !isSaving && !isCompletedToday,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(text = if (isCompletedToday) "Completed" else "Complete")
            }
        }
    }
}

@Composable
private fun RemoteExerciseSection(
    selectedDifficulty: ExerciseDifficulty,
    remoteExercises: List<RemoteExerciseIdea>,
    isLoading: Boolean,
    errorMessage: String?,
    onExerciseClick: (RemoteExerciseIdea) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "${selectedDifficulty.displayName} exercise ideas from API",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text(
                text = "Loading remote exercise data...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return
        }

        if (errorMessage != null) {
            Text(
                text = "Could not load API exercises: $errorMessage",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            return
        }

        if (remoteExercises.isEmpty()) {
            Text(
                text = "No ${selectedDifficulty.displayName.lowercase()} remote exercises available yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return
        }

        remoteExercises.forEach { exercise ->
            RemoteExerciseRow(
                exercise = exercise,
                onClick = { onExerciseClick(exercise) }
            )
        }
    }
}

@Composable
private fun RemoteExerciseRow(
    exercise: RemoteExerciseIdea,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${exercise.category} - ${exercise.level} - ${exercise.equipment}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FocusSelector(
    options: List<String>,
    selectedFocus: String,
    onFocusSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Focus",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(options) { option ->
                FilterChip(
                    selected = selectedFocus == option,
                    onClick = { onFocusSelected(option) },
                    label = { Text(text = option) }
                )
            }
        }
    }
}

@Composable
private fun ProgressStats(historyRecords: List<ExerciseHistoryRecord>) {
    val metrics = remember(historyRecords) {
        HomeMetrics.from(historyRecords)
    }
    val stats = listOf(
        HomeStat("Streak", formatDays(metrics.streakDays), MaterialTheme.colorScheme.primary),
        HomeStat("Workouts", metrics.totalWorkouts.toString(), MaterialTheme.colorScheme.secondary),
        HomeStat("Minutes", metrics.totalMinutes.toString(), MaterialTheme.colorScheme.tertiary)
    )

    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(stats) { stat ->
            StatCard(stat = stat)
        }
    }
}

@Composable
private fun StatCard(stat: HomeStat) {
    Card(
        modifier = Modifier.width(132.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(stat.color)
            )
            Text(
                text = stat.value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stat.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HomeStatusCard(message: String) {
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
private fun ChallengeRow(
    challenge: HomeChallenge,
    isCompletedToday: Boolean,
    isSaving: Boolean,
    onDetailsClick: () -> Unit,
    onCompleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDetailsClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = challenge.name.first().toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = challenge.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${challenge.category} - ${challenge.level} - ${challenge.summary}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${challenge.durationMinutes} min - ${challenge.caloriesBurned} calories",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onCompleteClick,
                    enabled = !isSaving && !isCompletedToday
                ) {
                    Text(text = if (isCompletedToday) "Completed" else "Complete")
                }
            }
        }
    }
}

@Composable
private fun ExerciseDescriptionDialog(
    title: String,
    subtitle: String,
    description: String,
    onClose: () -> Unit,
    completeButtonText: String? = null,
    onCompleteClick: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onClose) {
                Text(text = "Close")
            }
        },
        dismissButton = {
            if (completeButtonText != null) {
                Button(
                    onClick = onCompleteClick
                ) {
                    Text(text = completeButtonText)
                }
            }
        }
    )
}

private data class HomeStat(
    val label: String,
    val value: String,
    val color: Color
)

private data class HomeMetrics(
    val streakDays: Int,
    val totalWorkouts: Int,
    val totalMinutes: Int
) {
    companion object {
        fun from(historyRecords: List<ExerciseHistoryRecord>): HomeMetrics {
            val completedDates = historyRecords
                .map { record -> record.completedLocalDate }
                .toSet()
            val today = LocalDate.now()
            val streakStart = when {
                today in completedDates -> today
                today.minusDays(1) in completedDates -> today.minusDays(1)
                else -> null
            }
            var streakDays = 0
            var cursor = streakStart

            while (cursor != null && cursor in completedDates) {
                streakDays += 1
                cursor = cursor.minusDays(1)
            }

            return HomeMetrics(
                streakDays = streakDays,
                totalWorkouts = historyRecords.size,
                totalMinutes = historyRecords.sumOf { record -> record.durationMinutes }
            )
        }
    }
}

private fun RemoteExerciseIdea.toHomeChallenge(): HomeChallenge {
    val durationMinutes = when (level) {
        "Intermediate" -> 20
        "Expert" -> 25
        else -> 15
    }
    val caloriesBurned = when (level) {
        "Intermediate" -> 120
        "Expert" -> 160
        else -> 80
    }

    return HomeChallenge(
        name = name,
        category = category,
        level = level,
        summary = "Equipment: $equipment",
        description = description,
        durationMinutes = durationMinutes,
        caloriesBurned = caloriesBurned
    )
}

private val HomeChallenge.completionKey: String
    get() = "$name|$category|$level"

private val ExerciseHistoryRecord.completionKey: String
    get() = "$name|$category|$level"

private val ExerciseHistoryRecord.completedLocalDate: LocalDate
    get() = Instant.ofEpochMilli(completedAt)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

private fun ExerciseHistoryRecord.isCompletedToday(): Boolean {
    return completedLocalDate == LocalDate.now()
}

private fun formatDays(days: Int): String {
    return if (days == 1) {
        "1 day"
    } else {
        "$days days"
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    FitnessAppTheme {
        HomeScreen(
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
            )
        )
    }
}
