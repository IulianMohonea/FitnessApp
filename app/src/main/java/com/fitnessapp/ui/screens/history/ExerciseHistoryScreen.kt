package com.fitnessapp.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fitnessapp.data.local.model.ExerciseHistoryRecord
import com.fitnessapp.ui.theme.FitnessAppTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExerciseHistoryScreen(
    historyItems: List<ExerciseHistoryRecord>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = remember(historyItems) {
        listOf("All") + historyItems.map { it.category }.distinct().sorted()
    }
    val activeFilter = if (selectedFilter in filters) selectedFilter else "All"
    val visibleHistory = remember(historyItems, activeFilter) {
        if (activeFilter == "All") {
            historyItems
        } else {
            historyItems.filter { it.category == activeFilter }
        }
    }
    val groupedHistory = remember(visibleHistory) {
        visibleHistory.groupBy { historyItem ->
            historyItem.completedLocalDate
        }.toSortedMap(compareByDescending { date -> date })
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                HistoryHeader()
            }

            item {
                HistoryFilterRow(
                    filters = filters,
                    selectedFilter = activeFilter,
                    onFilterSelected = { selectedFilter = it }
                )
            }

            if (isLoading) {
                item {
                    HistoryStatusCard(message = "Loading workout history...")
                }
            } else if (visibleHistory.isEmpty()) {
                item {
                    HistoryStatusCard(
                        message = "No workouts in this category yet. Start the exercise of the day from Home to add one."
                    )
                }
            } else {
                groupedHistory.forEach { (date, dayItems) ->
                    item {
                        HistoryDayHeader(
                            date = date,
                            workoutCount = dayItems.size
                        )
                    }

                    items(dayItems) { historyItem ->
                        ExerciseHistoryRow(item = historyItem)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Exercise history",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Review your latest completed exercises and workout notes.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HistoryFilterRow(
    filters: List<String>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filters) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(text = filter.displayLabel) }
            )
        }
    }
}

@Composable
private fun HistoryDayHeader(
    date: LocalDate,
    workoutCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatHistoryDay(date),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (workoutCount == 1) "1 exercise" else "$workoutCount exercises",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ExerciseHistoryRow(item: ExerciseHistoryRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.name.first().toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${item.category} - ${item.level} - ${item.summary}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${item.durationMinutes} min - ${item.caloriesBurned} calories",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formatHistoryDate(item.completedAt),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HistoryStatusCard(message: String) {
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

private fun formatHistoryDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

private fun formatHistoryDay(date: LocalDate): String {
    val today = LocalDate.now()

    return when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> {
            val formatter = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
            val dateMillis = date.atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            formatter.format(Date(dateMillis))
        }
    }
}

private val ExerciseHistoryRecord.completedLocalDate: LocalDate
    get() = Instant.ofEpochMilli(completedAt)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

private val String.displayLabel: String
    get() = if (this == "Strength") "STR" else this

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ExerciseHistoryScreenPreview() {
    FitnessAppTheme {
        ExerciseHistoryScreen(
            historyItems = listOf(
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
            isLoading = false
        )
    }
}
