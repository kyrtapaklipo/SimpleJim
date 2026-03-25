package com.simplejim.tracker

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simplejim.tracker.data.WorkoutExercise
import com.simplejim.tracker.data.WorkoutRepository
import com.simplejim.tracker.data.WorkoutSession
import com.simplejim.tracker.data.WorkoutSet

enum class SimpleJimTab {
    Track,
    History,
}

data class DraftSet(
    val weight: String = "",
    val reps: String = "",
)

data class DraftExercise(
    val id: Long,
    val name: String = "",
    val sets: List<DraftSet> = listOf(DraftSet()),
)

data class SimpleJimUiState(
    val selectedTab: SimpleJimTab = SimpleJimTab.Track,
    val draftNotes: String = "",
    val draftExercises: List<DraftExercise> = emptyList(),
    val history: List<WorkoutSession> = emptyList(),
    val validationMessage: String? = null,
    val lastSavedAt: Long? = null,
)

class SimpleJimViewModel(application: Application) : AndroidViewModel(application) {
    private var idCounter = 1L
    private val repository = WorkoutRepository(application)

    var uiState by mutableStateOf(
        SimpleJimUiState(
            draftExercises = listOf(newDraftExercise()),
            history = repository.loadWorkouts(),
        ),
    )
        private set

    fun selectTab(tab: SimpleJimTab) {
        uiState = uiState.copy(selectedTab = tab)
    }

    fun updateNotes(notes: String) {
        uiState = uiState.copy(
            draftNotes = notes,
            validationMessage = null,
        )
    }

    fun addExercise() {
        uiState = uiState.copy(
            draftExercises = uiState.draftExercises + newDraftExercise(),
            validationMessage = null,
        )
    }

    fun removeExercise(exerciseId: Long) {
        val updatedExercises = uiState.draftExercises.filterNot { it.id == exerciseId }
        uiState = uiState.copy(
            draftExercises = if (updatedExercises.isEmpty()) listOf(newDraftExercise()) else updatedExercises,
            validationMessage = null,
        )
    }

    fun updateExerciseName(exerciseId: Long, name: String) {
        updateExercise(exerciseId) { exercise ->
            exercise.copy(name = name)
        }
    }

    fun addSet(exerciseId: Long) {
        updateExercise(exerciseId) { exercise ->
            exercise.copy(sets = exercise.sets + DraftSet())
        }
    }

    fun removeLastSet(exerciseId: Long) {
        updateExercise(exerciseId) { exercise ->
            val trimmedSets = if (exercise.sets.size > 1) exercise.sets.dropLast(1) else exercise.sets
            exercise.copy(sets = trimmedSets)
        }
    }

    fun updateSetWeight(exerciseId: Long, setIndex: Int, weight: String) {
        updateExercise(exerciseId) { exercise ->
            exercise.copy(
                sets = exercise.sets.mapIndexed { index, draftSet ->
                    if (index == setIndex) {
                        draftSet.copy(weight = sanitizeDecimal(weight))
                    } else {
                        draftSet
                    }
                },
            )
        }
    }

    fun updateSetReps(exerciseId: Long, setIndex: Int, reps: String) {
        updateExercise(exerciseId) { exercise ->
            exercise.copy(
                sets = exercise.sets.mapIndexed { index, draftSet ->
                    if (index == setIndex) {
                        draftSet.copy(reps = reps.filter(Char::isDigit))
                    } else {
                        draftSet
                    }
                },
            )
        }
    }

    fun resetDraft() {
        uiState = uiState.copy(
            draftNotes = "",
            draftExercises = listOf(newDraftExercise()),
            validationMessage = null,
        )
    }

    fun saveWorkout() {
        val cleanedExercises = uiState.draftExercises.mapNotNull { draftExercise ->
            val name = draftExercise.name.trim()
            val sets = draftExercise.sets.mapNotNull { draftSet ->
                val weight = draftSet.weight.toDoubleOrNull()
                val reps = draftSet.reps.toIntOrNull()
                if (weight != null && reps != null && reps > 0 && weight >= 0) {
                    WorkoutSet(weight = weight, reps = reps)
                } else {
                    null
                }
            }

            if (name.isNotEmpty() && sets.isNotEmpty()) {
                WorkoutExercise(name = name, sets = sets)
            } else {
                null
            }
        }

        if (cleanedExercises.isEmpty()) {
            uiState = uiState.copy(
                validationMessage = "Add at least one named exercise with valid weight and reps before saving.",
            )
            return
        }

        val session = WorkoutSession(
            id = System.currentTimeMillis(),
            performedAt = System.currentTimeMillis(),
            notes = uiState.draftNotes.trim(),
            exercises = cleanedExercises,
        )
        val updatedHistory = listOf(session) + uiState.history

        repository.saveWorkouts(updatedHistory)
        uiState = uiState.copy(
            selectedTab = SimpleJimTab.History,
            draftNotes = "",
            draftExercises = listOf(newDraftExercise()),
            history = updatedHistory.sortedByDescending { it.performedAt },
            validationMessage = null,
            lastSavedAt = session.performedAt,
        )
    }

    private fun updateExercise(exerciseId: Long, transform: (DraftExercise) -> DraftExercise) {
        uiState = uiState.copy(
            draftExercises = uiState.draftExercises.map { exercise ->
                if (exercise.id == exerciseId) {
                    transform(exercise)
                } else {
                    exercise
                }
            },
            validationMessage = null,
        )
    }

    private fun newDraftExercise(): DraftExercise {
        return DraftExercise(id = nextId())
    }

    private fun nextId(): Long {
        val currentId = idCounter
        idCounter += 1
        return currentId
    }

    private fun sanitizeDecimal(input: String): String {
        val filtered = input.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } <= 1) {
            return filtered
        }

        var dotSeen = false
        return buildString {
            filtered.forEach { character ->
                if (character == '.') {
                    if (!dotSeen) {
                        dotSeen = true
                        append(character)
                    }
                } else {
                    append(character)
                }
            }
        }
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(SimpleJimViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return SimpleJimViewModel(application) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }
    }
}
