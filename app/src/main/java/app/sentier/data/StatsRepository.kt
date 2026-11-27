/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** Période des statistiques. */
enum class StatsPeriod(val label: String) {
    WEEK("7 jours"),
    MONTH("30 jours"),
    YEAR("12 mois"),
}

/** Une barre d'un graphique : un jour (semaine, mois) ou un mois (année). */
data class StatsBucket(
    val label: String,
    val steps: Long = 0,
    val habits: Int = 0,
    val tasks: Int = 0,
    val xp: Int = 0,
)

/** Taux de réussite d'une habitude sur la période. */
data class HabitRate(val name: String, val color: Long, val done: Int, val days: Int) {
    val rate: Float get() = if (days <= 0) 0f else done.toFloat() / days
}

data class Stats(
    val period: StatsPeriod = StatsPeriod.WEEK,
    val buckets: List<StatsBucket> = emptyList(),
    val days: Int = 7,
    val totalSteps: Long = 0,
    /** Moyenne par jour avec des pas enregistrés. */
    val averageSteps: Long = 0,
    val bestDaySteps: Long = 0,
    val goalDays: Int = 0,
    val habitsDone: Int = 0,
    val tasksDone: Int = 0,
    val xp: Int = 0,
    val activeDays: Int = 0,
    val habitRates: List<HabitRate> = emptyList(),
    val loading: Boolean = true,
)

/** Statistiques calculées à partir de l'historique des pas, du journal du jeu et des habitudes. */
class StatsRepository(
    private val gameDao: GameDao,
    private val habitDao: HabitDao,
    private val habits: HabitRepository,
    private val clock: () -> LocalDate = { LocalDate.now() },
) {
    fun observe(period: StatsPeriod): Flow<Stats> {
        val today = clock()
        val from = firstDay(period, today).toEpochDay()
        return combine(
            habitDao.observeStepsSince(from),
            gameDao.observeEventsSince(from),
            habits.observeHabitsWithStats(),
        ) { steps, events, habitStats ->
            build(period, today, steps.associate { it.day to it.steps }, events, habitStats)
        }
    }

    companion object {
        fun firstDay(period: StatsPeriod, today: LocalDate): LocalDate = when (period) {
            StatsPeriod.WEEK -> today.minusDays(6)
            StatsPeriod.MONTH -> today.minusDays(29)
            StatsPeriod.YEAR -> today.withDayOfMonth(1).minusMonths(11)
        }

        fun build(
            period: StatsPeriod,
            today: LocalDate,
            steps: Map<Long, Long>,
            events: List<GameEvent>,
            habitStats: List<HabitWithStats>,
        ): Stats {
            val start = firstDay(period, today)
            val first = start.toEpochDay()
            val last = today.toEpochDay()
            val inRange = events.filter { it.day in first..last }
            val stepDays = steps.filterKeys { it in first..last }

            val buckets = when (period) {
                StatsPeriod.WEEK, StatsPeriod.MONTH -> (first..last).map { day ->
                    val date = LocalDate.ofEpochDay(day)
                    val label = if (period == StatsPeriod.WEEK) {
                        date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.FRANCE).uppercase(Locale.FRANCE)
                    } else {
                        date.dayOfMonth.toString()
                    }
                    bucket(label, stepDays.filterKeys { it == day }, inRange.filter { it.day == day })
                }
                StatsPeriod.YEAR -> (0L..11L).map { m ->
                    val month = start.plusMonths(m)
                    val range = month.toEpochDay()..month.plusMonths(1).minusDays(1).toEpochDay()
                    val label = month.month.getDisplayName(TextStyle.NARROW, Locale.FRANCE).uppercase(Locale.FRANCE)
                    bucket(label, stepDays.filterKeys { it in range }, inRange.filter { it.day in range })
                }
            }

            val walked = stepDays.values.filter { it > 0 }
            val rates = habitStats.filter { !it.habit.archived }.map { h ->
                val since = maxOf(first, h.habit.createdDay)
                val days = (last - since + 1).toInt().coerceAtLeast(1)
                HabitRate(h.habit.name, h.habit.color, (since..last).count { h.isDoneOn(it) }, days)
            }
            return Stats(
                period = period,
                buckets = buckets,
                days = (last - first + 1).toInt(),
                totalSteps = walked.sum(),
                averageSteps = if (walked.isEmpty()) 0 else walked.sum() / walked.size,
                bestDaySteps = walked.maxOrNull() ?: 0,
                goalDays = inRange.count { it.kind == GameEvent.STEPS },
                habitsDone = inRange.count { it.kind == GameEvent.HABIT },
                tasksDone = inRange.count { it.kind == GameEvent.TASK },
                xp = inRange.filter { it.kind != GameEvent.PURCHASE }.sumOf { it.xp },
                activeDays = inRange.filter { it.kind != GameEvent.PURCHASE && it.kind != GameEvent.CHALLENGE }.map { it.day }.distinct().size,
                habitRates = rates,
                loading = false,
            )
        }

        private fun bucket(label: String, steps: Map<Long, Long>, events: List<GameEvent>) = StatsBucket(
            label = label,
            steps = steps.values.sum(),
            habits = events.count { it.kind == GameEvent.HABIT },
            tasks = events.count { it.kind == GameEvent.TASK },
            xp = events.filter { it.kind != GameEvent.PURCHASE }.sumOf { it.xp },
        )
    }
}
