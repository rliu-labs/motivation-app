/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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

/** Activité de l'année, jour par jour, pour le calendrier de chaleur. */
data class YearActivity(
    /** Premier jour affiché : un lundi, il y a 52 semaines. */
    val start: LocalDate,
    val today: LocalDate,
    /** Nombre d'actions (habitudes, tâches, objectif de pas…) par jour (epoch day). */
    val counts: Map<Long, Int> = emptyMap(),
) {
    val weeks: Int get() = ((today.toEpochDay() - start.toEpochDay()) / 7 + 1).toInt()
    val activeDays: Int get() = counts.count { it.value > 0 }
    val total: Int get() = counts.values.sum()
    private val max: Int get() = counts.values.maxOrNull() ?: 0

    fun countOn(day: Long): Int = counts[day] ?: 0

    /** Intensité de 0 (rien) à 4 (journée la plus remplie), relative à la meilleure journée. */
    fun levelOn(day: Long): Int {
        val c = countOn(day)
        if (c <= 0) return 0
        val m = max.coerceAtLeast(1)
        return ((c * 4 + m - 1) / m).coerceIn(1, 4)
    }

    /** Plus longue suite de jours actifs sur l'année. */
    val bestRun: Int get() {
        var best = 0
        var run = 0
        for (day in start.toEpochDay()..today.toEpochDay()) {
            run = if (countOn(day) > 0) run + 1 else 0
            best = maxOf(best, run)
        }
        return best
    }

    companion object {
        fun firstDay(today: LocalDate): LocalDate =
            today.minusWeeks(52).with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))

        fun of(today: LocalDate, events: List<GameEvent>): YearActivity {
            val start = firstDay(today)
            val range = start.toEpochDay()..today.toEpochDay()
            val counts = events.filter { GameEvent.isActivity(it.kind) && it.day in range }
                .groupingBy { it.day }.eachCount()
            return YearActivity(start, today, counts)
        }
    }
}

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

    /** L'année jour par jour, pour le calendrier de chaleur. */
    fun observeYear(): Flow<YearActivity> {
        val today = clock()
        return gameDao.observeEventsSince(YearActivity.firstDay(today).toEpochDay()).map { YearActivity.of(today, it) }
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
                xp = inRange.sumOf { it.xp },
                activeDays = inRange.filter { GameEvent.isActivity(it.kind) }.map { it.day }.distinct().size,
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
