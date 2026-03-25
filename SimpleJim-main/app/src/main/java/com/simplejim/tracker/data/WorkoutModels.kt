package com.simplejim.tracker.data

import org.json.JSONArray
import org.json.JSONObject

data class WorkoutSet(
    val weight: Double,
    val reps: Int,
)

data class WorkoutExercise(
    val name: String,
    val sets: List<WorkoutSet>,
)

data class WorkoutSession(
    val id: Long,
    val performedAt: Long,
    val notes: String,
    val exercises: List<WorkoutExercise>,
)

fun List<WorkoutSession>.toJsonString(): String {
    val sessions = JSONArray()
    forEach { session ->
        sessions.put(session.toJsonObject())
    }
    return sessions.toString()
}

fun workoutsFromJson(rawJson: String): List<WorkoutSession> {
    val sessions = JSONArray(rawJson)
    return buildList {
        for (sessionIndex in 0 until sessions.length()) {
            val sessionObject = sessions.optJSONObject(sessionIndex) ?: continue
            val exercisesArray = sessionObject.optJSONArray("exercises") ?: JSONArray()

            val exercises = buildList {
                for (exerciseIndex in 0 until exercisesArray.length()) {
                    val exerciseObject = exercisesArray.optJSONObject(exerciseIndex) ?: continue
                    val setsArray = exerciseObject.optJSONArray("sets") ?: JSONArray()

                    val sets = buildList {
                        for (setIndex in 0 until setsArray.length()) {
                            val setObject = setsArray.optJSONObject(setIndex) ?: continue
                            add(
                                WorkoutSet(
                                    weight = setObject.optDouble("weight"),
                                    reps = setObject.optInt("reps"),
                                ),
                            )
                        }
                    }

                    add(
                        WorkoutExercise(
                            name = exerciseObject.optString("name"),
                            sets = sets,
                        ),
                    )
                }
            }

            add(
                WorkoutSession(
                    id = sessionObject.optLong("id"),
                    performedAt = sessionObject.optLong("performedAt"),
                    notes = sessionObject.optString("notes"),
                    exercises = exercises,
                ),
            )
        }
    }
}

private fun WorkoutSession.toJsonObject(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("performedAt", performedAt)
        put("notes", notes)
        put(
            "exercises",
            JSONArray().apply {
                exercises.forEach { exercise ->
                    put(
                        JSONObject().apply {
                            put("name", exercise.name)
                            put(
                                "sets",
                                JSONArray().apply {
                                    exercise.sets.forEach { set ->
                                        put(
                                            JSONObject().apply {
                                                put("weight", set.weight)
                                                put("reps", set.reps)
                                            },
                                        )
                                    }
                                },
                            )
                        },
                    )
                }
            },
        )
    }
}
