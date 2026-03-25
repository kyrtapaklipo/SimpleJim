package com.simplejim.tracker.data

import android.content.Context

class WorkoutRepository(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun loadWorkouts(): List<WorkoutSession> {
        val rawJson = preferences.getString(KEY_WORKOUTS, null) ?: return emptyList()
        return runCatching { workoutsFromJson(rawJson).sortedByDescending { it.performedAt } }
            .getOrDefault(emptyList())
    }

    fun saveWorkouts(workouts: List<WorkoutSession>) {
        preferences.edit()
            .putString(KEY_WORKOUTS, workouts.sortedByDescending { it.performedAt }.toJsonString())
            .apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "simple_jim_store"
        const val KEY_WORKOUTS = "saved_workouts"
    }
}
