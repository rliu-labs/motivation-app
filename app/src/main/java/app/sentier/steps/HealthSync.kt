/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.steps

import app.sentier.data.Habit
import app.sentier.data.HabitDao
import app.sentier.data.HabitRepository
import app.sentier.data.Settings
import app.sentier.domain.HealthDay
import app.sentier.domain.HealthLink
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.ZoneId

/** Ce que la carte « Sommeil et sport » affiche. */
data class HealthState(
    /** Réglage activé. */
    val on: Boolean = false,
    /** Autorisations du sommeil et du sport accordées dans Health Connect. */
    val granted: Boolean = false,
    /** Sommeil de la dernière nuit (réveil aujourd'hui), en minutes, s'il est connu. */
    val lastNightMinutes: Long? = null,
    val sportTodayMinutes: Long = 0,
    val sportTodaySessions: Int = 0,
    /** Habitudes reliées (null : aucune habitude de ce genre). */
    val sleepHabit: Habit? = null,
    val sportHabit: Habit? = null,
    /** Une lecture a déjà eu lieu. */
    val read: Boolean = false,
)

/**
 * Lit le sommeil et le sport dans Health Connect et coche les habitudes correspondantes,
 * sur les 7 derniers jours. Ne fait rien tant que le réglage est coupé.
 */
class HealthSync(
    private val dao: HabitDao,
    private val habits: HabitRepository,
    private val settings: Settings,
    private val reader: HealthReader,
    private val clock: () -> LocalDate = { LocalDate.now() },
    private val zone: () -> ZoneId = { ZoneId.systemDefault() },
) {
    private data class Reading(val granted: Boolean = false, val days: Map<LocalDate, HealthDay> = emptyMap(), val read: Boolean = false)

    private val reading = MutableStateFlow(Reading())
    private val mutex = Mutex()

    fun observe(): Flow<HealthState> = combine(settings.healthAuto, reading, dao.observeHabits()) { on, r, list ->
        val today = clock()
        val day = r.days[today]
        HealthState(
            on = on,
            granted = r.granted,
            lastNightMinutes = day?.sleepMinutes?.takeIf { it > 0 },
            sportTodayMinutes = day?.sportMinutes ?: 0,
            sportTodaySessions = day?.sportSessions ?: 0,
            sleepHabit = list.firstOrNull { HealthLink.isSleep(it) },
            sportHabit = list.firstOrNull { HealthLink.isSport(it) },
            read = r.read,
        )
    }

    suspend fun setOn(on: Boolean) {
        settings.setHealthAuto(on)
        if (on) sync() else reading.value = Reading()
    }

    /** Relit Health Connect et coche les habitudes. Sans effet si le réglage est coupé. */
    suspend fun sync() = mutex.withLock {
        if (!settings.healthAuto.first()) return@withLock
        val granted = runCatching { reader.hasHealthPermission() }.getOrDefault(false)
        if (!granted) {
            reading.value = Reading(granted = false, read = true)
            return@withLock
        }
        val today = clock()
        val z = zone()
        val first = today.minusDays(DAYS - 1)
        val to = today.plusDays(1).atStartOfDay(z).toInstant()
        // La nuit d'avant le premier jour commence la veille au soir.
        val sleep = runCatching { reader.readSleep(first.minusDays(1).atStartOfDay(z).toInstant(), to) }.getOrDefault(emptyList())
        val sport = runCatching { reader.readExercise(first.atStartOfDay(z).toInstant(), to) }.getOrDefault(emptyList())
        val sleepByDay = HealthLink.sleepByDay(sleep, z)
        val sportByDay = HealthLink.sportByDay(sport, z)
        val days = (0 until DAYS).associate { back ->
            val d = today.minusDays(back)
            val (min, n) = sportByDay[d] ?: (0L to 0)
            d to HealthDay(sleepMinutes = sleepByDay[d] ?: 0, sportMinutes = min, sportSessions = n)
        }
        reading.value = Reading(granted = true, days = days, read = true)
        apply(days)
    }

    /** Enregistre les valeurs lues, sans jamais baisser une valeur déjà saisie. */
    private suspend fun apply(days: Map<LocalDate, HealthDay>) {
        val linked = dao.observeHabits().first().filter { HealthLink.isSleep(it) || HealthLink.isSport(it) }
        for (habit in linked) {
            for ((date, day) in days) {
                val value = HealthLink.valueFor(habit, day) ?: continue
                val current = dao.getLog(habit.id, date.toEpochDay())?.value ?: 0.0
                if (value > current) habits.setValue(habit, date, value)
            }
        }
    }

    companion object {
        const val DAYS = 7L
    }
}
