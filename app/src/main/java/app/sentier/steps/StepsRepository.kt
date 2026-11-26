/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.steps

import app.sentier.data.DailySteps
import app.sentier.data.GameRepository
import app.sentier.data.HabitDao
import app.sentier.data.Settings
import app.sentier.data.StepSettings
import app.sentier.data.StepSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate

/** Ce que l'écran doit proposer pour obtenir les pas. */
data class StepAvailability(
    val healthConnect: HealthConnectSource.Status = HealthConnectSource.Status.UNAVAILABLE,
    val healthConnectGranted: Boolean = false,
    val sensorAvailable: Boolean = false,
    val checked: Boolean = false,
)

data class StepsState(
    val today: LocalDate,
    val settings: StepSettings = StepSettings(),
    /** Pas des 7 derniers jours, du plus ancien à aujourd'hui (0 si inconnu). */
    val week: List<Pair<LocalDate, Long>> = emptyList(),
    val totalSteps: Long = 0,
    val availability: StepAvailability = StepAvailability(),
) {
    val todaySteps: Long get() = week.lastOrNull()?.second ?: 0L
    val progress: Float get() = if (settings.goal <= 0) 0f else (todaySteps.toFloat() / settings.goal).coerceIn(0f, 1f)
    val goalReached: Boolean get() = todaySteps >= settings.goal
    val todayKm: Double get() = todaySteps * settings.stepLengthCm / 100_000.0
    val totalKm: Double get() = totalSteps * settings.stepLengthCm / 100_000.0
    val connected: Boolean get() = when (settings.source) {
        StepSource.HEALTH_CONNECT -> availability.healthConnectGranted
        StepSource.SENSOR -> true
        StepSource.NONE -> false
    }
}

class StepsRepository(
    private val dao: HabitDao,
    private val settings: Settings,
    private val healthConnect: HealthConnectSource,
    private val sensor: SensorStepSource,
    private val game: GameRepository? = null,
    private val clock: () -> LocalDate = { LocalDate.now() },
) {
    private val availability = MutableStateFlow(StepAvailability())
    private val mutex = Mutex()

    fun observe(): Flow<StepsState> {
        val today = clock()
        return combine(
            settings.steps,
            dao.observeStepsSince(today.minusDays(6).toEpochDay()),
            dao.observeTotalSteps(),
            availability,
        ) { s, rows, total, avail ->
            val byDay = rows.associate { it.day to it.steps }
            val week = (6 downTo 0).map { back ->
                val d = today.minusDays(back.toLong())
                d to (byDay[d.toEpochDay()] ?: 0L)
            }
            StepsState(today, s, week, total, avail)
        }
    }

    val availabilityState: StateFlow<StepAvailability> get() = availability

    /** Met à jour la disponibilité et relit les pas. À appeler à l'ouverture de l'écran. */
    suspend fun refresh() = mutex.withLock {
        val hcStatus = healthConnect.status()
        val granted = hcStatus == HealthConnectSource.Status.AVAILABLE && healthConnect.hasPermission()
        availability.value = StepAvailability(hcStatus, granted, sensor.isAvailable, checked = true)

        val s = settings.currentSteps()
        val today = clock()
        when (s.source) {
            StepSource.HEALTH_CONNECT -> if (granted) {
                runCatching { healthConnect.readDailySteps(today.minusDays(6), today) }
                    .onSuccess { days ->
                        days.forEach { (d, n) ->
                            dao.putSteps(DailySteps(d.toEpochDay(), n))
                            if (n >= s.goal) game?.onStepsGoal(d.toEpochDay())
                        }
                    }
            }
            StepSource.SENSOR -> sampleSensorLocked(today)
            StepSource.NONE -> Unit
        }
    }

    /** Lecture du capteur (appelée aussi en arrière-plan toutes les 15 minutes). */
    suspend fun sampleSensor() = mutex.withLock { sampleSensorLocked(clock()) }

    private suspend fun sampleSensorLocked(today: LocalDate) {
        val counter = sensor.readCounter() ?: return
        val (last, lastDay, soFar) = settings.sensorState()
        val epoch = today.toEpochDay()
        val steps = SensorStepSource.accumulate(last, lastDay, soFar, counter, epoch)
        settings.saveSensorState(counter, epoch, steps)
        dao.putSteps(DailySteps(epoch, steps))
        if (steps >= settings.currentSteps().goal) game?.onStepsGoal(epoch)
    }

    suspend fun useSource(source: StepSource) {
        settings.setSource(source)
        refresh()
    }

    suspend fun setGoal(goal: Int) = settings.setGoal(goal)
    suspend fun setStepLength(cm: Int) = settings.setStepLength(cm)
}
