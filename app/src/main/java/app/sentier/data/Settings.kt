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
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "reglages")

/** D'où viennent les pas. */
enum class StepSource { NONE, HEALTH_CONNECT, SENSOR }

data class StepSettings(
    val goal: Int = 8000,
    /** Longueur d'un pas en centimètres. */
    val stepLengthCm: Int = 75,
    val source: StepSource = StepSource.NONE,
    /** L'objectif s'ajuste chaque jour aux pas des deux dernières semaines. */
    val adaptive: Boolean = false,
)

/** Thème de l'appli : celui du téléphone, ou toujours clair, ou toujours sombre. */
enum class ThemeMode(val label: String) { SYSTEM("Comme le téléphone"), LIGHT("Clair"), DARK("Sombre") }

/** Sauvegarde automatique chaque semaine dans un dossier choisi. */
data class AutoBackupSettings(
    val on: Boolean = false,
    /** Dossier choisi (adresse du sélecteur de dossiers d'Android). */
    val folder: String? = null,
    /** Date de la dernière sauvegarde réussie (millisecondes). */
    val lastAt: Long? = null,
    /** Dernière erreur, à montrer telle quelle. */
    val error: String? = null,
)

/** Présentation de l'onglet Tâches. */
enum class TaskView(val label: String) { BY_DATE("Par date"), MANUAL("Mon ordre"), CALENDAR("Calendrier") }

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
        val adaptive = booleanPreferencesKey("step_goal_adaptive")
        val adaptiveDay = longPreferencesKey("step_goal_adaptive_day")
        val theme = stringPreferencesKey("theme_mode")
        val celebratedLevel = intPreferencesKey("celebrated_level")
        val celebratedBadges = stringSetPreferencesKey("celebrated_badges")
        val onboarded = booleanPreferencesKey("onboarded")
        val taskView = stringPreferencesKey("task_view")
        val autoBackupOn = booleanPreferencesKey("auto_backup_on")
        val autoBackupFolder = stringPreferencesKey("auto_backup_folder")
        val autoBackupLast = longPreferencesKey("auto_backup_last")
        val autoBackupError = stringPreferencesKey("auto_backup_error")
    }

    /** Sauvegarde automatique : propre à ce téléphone (le dossier choisi n'a pas de sens ailleurs), donc jamais exportée. */
    val autoBackup: Flow<AutoBackupSettings> = context.dataStore.data.map { p ->
        AutoBackupSettings(
            on = p[Keys.autoBackupOn] ?: false,
            folder = p[Keys.autoBackupFolder],
            lastAt = p[Keys.autoBackupLast],
            error = p[Keys.autoBackupError],
        )
    }

    suspend fun currentAutoBackup(): AutoBackupSettings = autoBackup.first()

    suspend fun setAutoBackup(on: Boolean, folder: String?) = context.dataStore.edit {
        it[Keys.autoBackupOn] = on
        if (folder != null) it[Keys.autoBackupFolder] = folder
        it.remove(Keys.autoBackupError)
    }

    suspend fun setAutoBackupResult(at: Long?, error: String?) = context.dataStore.edit {
        if (at != null) it[Keys.autoBackupLast] = at
        if (error != null) it[Keys.autoBackupError] = error else it.remove(Keys.autoBackupError)
    }

    val taskView: Flow<TaskView> = context.dataStore.data.map { p ->
        p[Keys.taskView]?.let { v -> TaskView.entries.firstOrNull { it.name == v } } ?: TaskView.BY_DATE
    }

    suspend fun setTaskView(view: TaskView) = context.dataStore.edit { it[Keys.taskView] = view.name }

    /** Vrai une fois l'accueil du premier lancement terminé ou passé (null s'il n'a jamais été montré). */
    suspend fun onboarded(): Boolean? = context.dataStore.data.first()[Keys.onboarded]

    suspend fun setOnboarded() = context.dataStore.edit { it[Keys.onboarded] = true }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { p ->
        p[Keys.theme]?.let { v -> ThemeMode.entries.firstOrNull { it.name == v } } ?: ThemeMode.SYSTEM
    }

    suspend fun setThemeMode(mode: ThemeMode) = context.dataStore.edit { it[Keys.theme] = mode.name }

    /** Niveau et badges déjà fêtés à l'écran (null au premier lancement). */
    val celebrated: Flow<Pair<Int, Set<String>>?> = context.dataStore.data.map { p ->
        p[Keys.celebratedLevel]?.let { it to (p[Keys.celebratedBadges] ?: emptySet()) }
    }

    suspend fun setCelebrated(level: Int, badges: Set<String>) = context.dataStore.edit {
        it[Keys.celebratedLevel] = level
        it[Keys.celebratedBadges] = badges
    }

    suspend fun setAdaptive(on: Boolean) = context.dataStore.edit {
        it[Keys.adaptive] = on
        it.remove(Keys.adaptiveDay)
    }

    /** Jour du dernier ajustement de l'objectif adaptatif. */
    suspend fun adaptiveDay(): Long? = context.dataStore.data.first()[Keys.adaptiveDay]

    suspend fun setAdaptedGoal(goal: Int, day: Long) = context.dataStore.edit {
        it[Keys.goal] = goal.coerceIn(500, 50_000)
        it[Keys.adaptiveDay] = day
    }

    /** Réglages copiés dans une sauvegarde (sans l'état du capteur, propre au téléphone). */
    suspend fun exportJson(): JSONObject {
        val p = context.dataStore.data.first()
        return JSONObject().apply {
            p[Keys.goal]?.let { put("step_goal", it) }
            p[Keys.stepLength]?.let { put("step_length_cm", it) }
            p[Keys.source]?.let { put("step_source", it) }
            p[Keys.morningOn]?.let { put("reminder_morning_on", it) }
            p[Keys.morningMinute]?.let { put("reminder_morning_minute", it) }
            p[Keys.eveningOn]?.let { put("reminder_evening_on", it) }
            p[Keys.eveningMinute]?.let { put("reminder_evening_minute", it) }
            p[Keys.journeySeen]?.let { put("journey_seen_stops", it) }
            p[Keys.treasuresSeen]?.let { put("treasures_seen", JSONArray(it.toList())) }
            p[Keys.adaptive]?.let { put("step_goal_adaptive", it) }
            p[Keys.theme]?.let { put("theme_mode", it) }
            p[Keys.taskView]?.let { put("task_view", it) }
            p[Keys.celebratedLevel]?.let { put("celebrated_level", it) }
            p[Keys.celebratedBadges]?.let { put("celebrated_badges", JSONArray(it.toList())) }
        }
    }

    /** Remet les réglages d'une sauvegarde. Les valeurs inconnues ou invalides sont ignorées. */
    suspend fun importJson(o: JSONObject) = context.dataStore.edit { p ->
        fun int(name: String, range: IntRange): Int? = if (o.has(name)) o.optInt(name, Int.MIN_VALUE).takeIf { it in range } else null
        fun bool(name: String): Boolean? = if (o.opt(name) is Boolean) o.getBoolean(name) else null
        int("step_goal", 500..50_000)?.let { p[Keys.goal] = it }
        int("step_length_cm", 30..150)?.let { p[Keys.stepLength] = it }
        o.optString("step_source").takeIf { v -> StepSource.entries.any { it.name == v } }?.let { p[Keys.source] = it }
        bool("reminder_morning_on")?.let { p[Keys.morningOn] = it }
        int("reminder_morning_minute", 0 until 24 * 60)?.let { p[Keys.morningMinute] = it }
        bool("reminder_evening_on")?.let { p[Keys.eveningOn] = it }
        int("reminder_evening_minute", 0 until 24 * 60)?.let { p[Keys.eveningMinute] = it }
        int("journey_seen_stops", 0..1000)?.let { p[Keys.journeySeen] = it }
        o.optJSONArray("treasures_seen")?.let { a ->
            p[Keys.treasuresSeen] = (0 until a.length()).mapNotNull { a.opt(it) as? String }.filter { it.length <= 40 }.toSet()
        }
        bool("step_goal_adaptive")?.let { p[Keys.adaptive] = it }
        o.optString("theme_mode").takeIf { v -> ThemeMode.entries.any { it.name == v } }?.let { p[Keys.theme] = it }
        o.optString("task_view").takeIf { v -> TaskView.entries.any { it.name == v } }?.let { p[Keys.taskView] = it }
        int("celebrated_level", 1..10_000)?.let { p[Keys.celebratedLevel] = it }
        o.optJSONArray("celebrated_badges")?.let { a ->
            p[Keys.celebratedBadges] = (0 until a.length()).mapNotNull { a.opt(it) as? String }.filter { it.length <= 40 }.toSet()
        }
        p.remove(Keys.adaptiveDay)
        // Le capteur repart de zéro : son compteur est propre à chaque téléphone.
        p.remove(Keys.sensorLastCounter)
        p.remove(Keys.sensorDay)
        p.remove(Keys.sensorToday)
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
            adaptive = p[Keys.adaptive] ?: false,
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
