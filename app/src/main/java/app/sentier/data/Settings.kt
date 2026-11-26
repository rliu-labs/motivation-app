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
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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

/** Rappels du matin et du soir. Les heures sont en minutes depuis minuit. */
data class ReminderSettings(
    val morningOn: Boolean = false,
    val morningMinute: Int = DEFAULT_MORNING,
    val eveningOn: Boolean = false,
    val eveningMinute: Int = DEFAULT_EVENING,
) {
    val anyOn: Boolean get() = morningOn || eveningOn

    companion object {
        const val DEFAULT_MORNING = 8 * 60
        const val DEFAULT_EVENING = 20 * 60 + 30
    }
}

/** Réglages de l'appli (DataStore). */
class Settings(private val context: Context) {
    private object Keys {
        val goal = intPreferencesKey("step_goal")
        val stepLength = intPreferencesKey("step_length_cm")
        val source = stringPreferencesKey("step_source")
        val sensorLastCounter = floatPreferencesKey("sensor_last_counter")
        val sensorDay = longPreferencesKey("sensor_day")
        val sensorToday = longPreferencesKey("sensor_today_steps")
        val morningOn = booleanPreferencesKey("reminder_morning_on")
        val morningMinute = intPreferencesKey("reminder_morning_minute")
        val eveningOn = booleanPreferencesKey("reminder_evening_on")
        val eveningMinute = intPreferencesKey("reminder_evening_minute")
        val journeySeen = intPreferencesKey("journey_seen_stops")
        val treasuresSeen = stringSetPreferencesKey("treasures_seen")
    }

    /** Trésors déjà annoncés à l'écran. */
    val treasuresSeen: Flow<Set<String>> = context.dataStore.data.map { it[Keys.treasuresSeen] ?: emptySet() }

    suspend fun addTreasuresSeen(ids: Collection<String>) = context.dataStore.edit {
        it[Keys.treasuresSeen] = (it[Keys.treasuresSeen] ?: emptySet()) + ids
    }

    /** Nombre d'étapes du voyage déjà annoncées à l'écran. */
    val journeySeen: Flow<Int> = context.dataStore.data.map { it[Keys.journeySeen] ?: 0 }

    suspend fun setJourneySeen(count: Int) = context.dataStore.edit { it[Keys.journeySeen] = count }

    val reminders: Flow<ReminderSettings> = context.dataStore.data.map { p ->
        ReminderSettings(
            morningOn = p[Keys.morningOn] ?: false,
            morningMinute = p[Keys.morningMinute] ?: ReminderSettings.DEFAULT_MORNING,
            eveningOn = p[Keys.eveningOn] ?: false,
            eveningMinute = p[Keys.eveningMinute] ?: ReminderSettings.DEFAULT_EVENING,
        )
    }

    suspend fun currentReminders(): ReminderSettings = reminders.first()

    suspend fun setReminders(r: ReminderSettings) = context.dataStore.edit {
        it[Keys.morningOn] = r.morningOn
        it[Keys.morningMinute] = r.morningMinute.coerceIn(0, 24 * 60 - 1)
        it[Keys.eveningOn] = r.eveningOn
        it[Keys.eveningMinute] = r.eveningMinute.coerceIn(0, 24 * 60 - 1)
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
