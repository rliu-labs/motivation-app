/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.data

import app.sentier.domain.HabitScore
import app.sentier.domain.HabitTemplate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

/** Une habitude avec ses valeurs récentes et son score de force. */
data class HabitWithStats(
    val habit: Habit,
    /** Valeur saisie par jour (epoch day). */
    val values: Map<Long, Double>,
    /** Score de force, 0 à 1. */
    val strength: Double,
) {
    fun valueOn(day: Long): Double = values[day] ?: 0.0
    fun completionOn(day: Long): Double = HabitScore.completionOf(valueOn(day), habit.isNumeric, habit.target)
    fun isDoneOn(day: Long): Boolean = completionOn(day) >= 1.0
}

class HabitRepository(
    private val dao: HabitDao,
    private val clock: () -> LocalDate = { LocalDate.now() },
) {
    fun today(): LocalDate = clock()

    /** Habitudes actives avec leurs statistiques (valeurs sur la dernière année). */
    fun observeHabitsWithStats(): Flow<List<HabitWithStats>> {
        val today = today().toEpochDay()
        return combine(dao.observeHabits(), dao.observeLogsSince(today - HISTORY_DAYS)) { habits, logs ->
            val byHabit = logs.groupBy { it.habitId }
            habits.map { habit -> stats(habit, byHabit[habit.id].orEmpty(), today) }
        }
    }

    fun observeHabitWithStats(id: Long): Flow<HabitWithStats?> {
        val today = today().toEpochDay()
        return combine(dao.observeHabit(id), dao.observeLogs(id)) { habit, logs ->
            habit?.let { stats(it, logs, today) }
        }
    }

    private fun stats(habit: Habit, logs: List<HabitLog>, today: Long): HabitWithStats {
        val values = logs.associate { it.day to it.value }
        val completion = values.mapValues { (_, v) -> HabitScore.completionOf(v, habit.isNumeric, habit.target) }
        val start = minOf(habit.createdDay, logs.minOfOrNull { it.day } ?: habit.createdDay)
            .coerceAtLeast(today - HISTORY_DAYS)
        val strength = HabitScore.compute(completion, habit.freqNum, habit.freqDen, start, today)
        return HabitWithStats(habit, values, strength)
    }

    suspend fun getHabit(id: Long): Habit? = dao.getHabit(id)

    suspend fun addHabit(habit: Habit): Long =
        dao.insert(habit.copy(id = 0, createdDay = today().toEpochDay(), position = dao.maxPosition() + 1))

    suspend fun addFromTemplate(t: HabitTemplate): Long = addHabit(
        Habit(
            name = t.name, color = t.color, icon = t.icon, type = t.type, target = t.target, unit = t.unit,
            freqNum = t.freqNum, freqDen = t.freqDen, createdDay = 0,
        )
    )

    suspend fun update(habit: Habit) = dao.update(habit)
    suspend fun delete(habit: Habit) = dao.delete(habit)

    /** Coche ou décoche une habitude oui/non. Renvoie true si elle est maintenant faite. */
    suspend fun toggle(habit: Habit, day: LocalDate = today()): Boolean {
        val epoch = day.toEpochDay()
        val current = dao.getLog(habit.id, epoch)
        return if (current != null && current.value > 0) {
            dao.deleteLog(habit.id, epoch)
            false
        } else {
            dao.putLog(HabitLog(habit.id, epoch, 1.0))
            true
        }
    }

    /** Enregistre une valeur chiffrée (0 efface). */
    suspend fun setValue(habit: Habit, day: LocalDate, value: Double) {
        val epoch = day.toEpochDay()
        if (value <= 0) dao.deleteLog(habit.id, epoch) else dao.putLog(HabitLog(habit.id, epoch, value))
    }

    companion object {
        const val HISTORY_DAYS = 365L
    }
}
