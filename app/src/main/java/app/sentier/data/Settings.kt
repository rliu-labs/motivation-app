/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "reglages")

/** D'où viennent les pas. */
enum class StepSource { NONE, HEALTH_CONNECT, SENSOR }

data class StepSettings(
    val goal: Int = 8000,
    /** Longueur d'un pas en centimètres. */
    val stepLengthCm: Int = 75,
    val source: StepSource = StepSource.NONE,
)

/** Réglages de l'appli (DataStore). */
class Settings(private val context: Context) {
    private object Keys {
        val goal = intPreferencesKey("step_goal")
        val stepLength = intPreferencesKey("step_length_cm")
        val source = stringPreferencesKey("step_source")
        val sensorLastCounter = floatPreferencesKey("sensor_last_counter")
        val sensorDay = longPreferencesKey("sensor_day")
        val sensorToday = longPreferencesKey("sensor_today_steps")
    }

    val steps: Flow<StepSettings> = context.dataStore.data.map { p ->
        StepSettings(
            goal = p[Keys.goal] ?: 8000,
            stepLengthCm = p[Keys.stepLength] ?: 75,
            source = p[Keys.source]?.let { runCatching { StepSource.valueOf(it) }.getOrNull() } ?: StepSource.NONE,
        )
    }

    suspend fun currentSteps(): StepSettings = steps.first()

    suspend fun setGoal(goal: Int) = context.dataStore.edit { it[Keys.goal] = goal.coerceIn(500, 50_000) }
    suspend fun setStepLength(cm: Int) = context.dataStore.edit { it[Keys.stepLength] = cm.coerceIn(30, 150) }
    suspend fun setSource(source: StepSource) = context.dataStore.edit { it[Keys.source] = source.name }

    /** État du compteur du capteur : (dernière valeur lue, jour, pas du jour). */
    suspend fun sensorState(): Triple<Float?, Long?, Long> {
        val p = context.dataStore.data.first()
        return Triple(p[Keys.sensorLastCounter], p[Keys.sensorDay], p[Keys.sensorToday] ?: 0L)
    }

    suspend fun saveSensorState(counter: Float, day: Long, todaySteps: Long) = context.dataStore.edit {
        it[Keys.sensorLastCounter] = counter
        it[Keys.sensorDay] = day
        it[Keys.sensorToday] = todaySteps
    }
}
