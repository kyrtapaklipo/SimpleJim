package com.simplejim.tracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.simplejim.tracker.DraftExercise
import com.simplejim.tracker.SimpleJimTab
import com.simplejim.tracker.SimpleJimUiState
import com.simplejim.tracker.SimpleJimViewModel
import com.simplejim.tracker.data.WorkoutSession
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

@Composable
fun SimpleJimApp(viewModel: SimpleJimViewModel) {
    val state = viewModel.uiState

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            HeaderBanner(lastWorkout = state.history.firstOrNull())
            TabRow(selectedTabIndex = state.selectedTab.ordinal) {
                SimpleJimTab.entries.forEach { tab ->
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = { Text(text = tab.name) },
                    )
                }
            }

            when (state.selectedTab) {
                SimpleJimTab.Track -> TrackScreen(
                    modifier = Modifier.weight(1f),
                    state = state,
                    onNotesChange = viewModel::updateNotes,
                    onExerciseNameChange = viewModel::updateExerciseName,
                    onSetWeightChange = viewModel::updateSetWeight,
                    onSetRepsChange = viewModel::updateSetReps,
                    onAddExercise = viewModel::addExercise,
                    onRemoveExercise = viewModel::removeExercise,
                    onAddSet = viewModel::addSet,
                    onRemoveLastSet = viewModel::removeLastSet,
                    onResetDraft = viewModel::resetDraft,
                    onSaveWorkout = viewModel::saveWorkout,
                )

                SimpleJimTab.History -> HistoryScreen(
                    modifier = Modifier.weight(1f),
                    history = state.history,
                    lastSavedAt = state.lastSavedAt,
                )
            }
        }
    }
}

@Composable
private fun HeaderBanner(lastWorkout: WorkoutSession?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary,
                    ),
                ),
            ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "SimpleJim",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Text(
                text = "Bare-bones lifting logs for a phone-first routine.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Text(
                text = lastWorkout?.let { "Last session: ${formatTimestamp(it.performedAt)}" }
                    ?: "No sessions logged yet. Start with one exercise and one set.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
private fun TrackScreen(
    modifier: Modifier,
    state: SimpleJimUiState,
    onNotesChange: (String) -> Unit,
    onExerciseNameChange: (Long, String) -> Unit,
    onSetWeightChange: (Long, Int, String) -> Unit,
    onSetRepsChange: (Long, Int, String) -> Unit,
    onAddExercise: () -> Unit,
    onRemoveExercise: (Long) -> Unit,
    onAddSet: (Long) -> Unit,
    onRemoveLastSet: (Long) -> Unit,
    onResetDraft: () -> Unit,
    onSaveWorkout: () -> Unit,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .imePadding(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SummaryCard(
                title = "Today's Log",
                body = "Use pounds, tap in reps, and save when you finish the workout.",
                supporting = "${state.history.size} total logged sessions",
            )
        }

        state.validationMessage?.let { message ->
            item {
                StatusCard(
                    title = "Can't save yet",
                    body = message,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }

        item {
            OutlinedTextField(
                value = state.draftNotes,
                onValueChange = onNotesChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes") },
                placeholder = { Text("Optional: push day, low energy, PR attempt...") },
                minLines = 2,
                maxLines = 4,
            )
        }

        itemsIndexed(
            items = state.draftExercises,
            key = { _, exercise -> exercise.id },
        ) { index, exercise ->
            ExerciseEditorCard(
                index = index,
                exercise = exercise,
                canRemoveExercise = state.draftExercises.size > 1,
                onNameChange = { onExerciseNameChange(exercise.id, it) },
                onWeightChange = { setIndex, value -> onSetWeightChange(exercise.id, setIndex, value) },
                onRepsChange = { setIndex, value -> onSetRepsChange(exercise.id, setIndex, value) },
                onAddSet = { onAddSet(exercise.id) },
                onRemoveLastSet = { onRemoveLastSet(exercise.id) },
                onRemoveExercise = { onRemoveExercise(exercise.id) },
            )
        }

        item {
            OutlinedButton(
                onClick = onAddExercise,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Add Exercise")
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onSaveWorkout,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Save Workout")
                }
                OutlinedButton(
                    onClick = onResetDraft,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Start Fresh")
                }
            }
        }
    }
}

@Composable
private fun ExerciseEditorCard(
    index: Int,
    exercise: DraftExercise,
    canRemoveExercise: Boolean,
    onNameChange: (String) -> Unit,
    onWeightChange: (Int, String) -> Unit,
    onRepsChange: (Int, String) -> Unit,
    onAddSet: () -> Unit,
    onRemoveLastSet: () -> Unit,
    onRemoveExercise: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Exercise ${index + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (canRemoveExercise) {
                    TextButton(onClick = onRemoveExercise) {
                        Text("Remove")
                    }
                }
            }

            OutlinedTextField(
                value = exercise.name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Exercise name") },
                placeholder = { Text("Bench Press") },
                singleLine = true,
            )

            exercise.sets.forEachIndexed { setIndex, set ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Set ${setIndex + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedTextField(
                            value = set.weight,
                            onValueChange = { onWeightChange(setIndex, it) },
                            modifier = Modifier.weight(1f),
                            label = { Text("Weight (lb)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = set.reps,
                            onValueChange = { onRepsChange(setIndex, it) },
                            modifier = Modifier.weight(1f),
                            label = { Text("Reps") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onAddSet) {
                    Text("Add Set")
                }
                if (exercise.sets.size > 1) {
                    TextButton(onClick = onRemoveLastSet) {
                        Text("Remove Last Set")
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryScreen(
    modifier: Modifier,
    history: List<WorkoutSession>,
    lastSavedAt: Long?,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        lastSavedAt?.let { timestamp ->
            item {
                StatusCard(
                    title = "Workout Saved",
                    body = "Stored locally on this device at ${formatTimestamp(timestamp)}.",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }

        if (history.isEmpty()) {
            item {
                SummaryCard(
                    title = "No History Yet",
                    body = "Your saved workouts show up here. The app keeps everything local and offline.",
                    supporting = "There is no account, sync, or backend in this first cut.",
                )
            }
        } else {
            items(history, key = { it.id }) { session ->
                HistoryCard(session = session)
            }
        }
    }
}

@Composable
private fun HistoryCard(session: WorkoutSession) {
    val totalSets = session.exercises.sumOf { it.sets.size }
    val totalVolume = session.exercises.sumOf { exercise ->
        exercise.sets.sumOf { set -> set.weight * set.reps }
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = formatTimestamp(session.performedAt),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SessionChip("${session.exercises.size} exercises")
                SessionChip("$totalSets sets")
                SessionChip("${formatWeight(totalVolume)} lb volume")
            }

            if (session.notes.isNotBlank()) {
                Text(
                    text = session.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            session.exercises.forEach { exercise ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = exercise.sets.joinToString("   ") { set ->
                            "${formatWeight(set.weight)} x ${set.reps}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionChip(label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    body: String,
    supporting: String,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = supporting,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun StatusCard(
    title: String,
    body: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = DateTimeFormatter.ofPattern("EEE, MMM d  h:mm a", Locale.US)
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}

private fun formatWeight(weight: Double): String {
    return if (abs(weight - weight.toInt().toDouble()) < 0.001) {
        weight.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", weight)
    }
}
