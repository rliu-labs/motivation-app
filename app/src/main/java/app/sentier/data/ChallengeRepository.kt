/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.data

import app.sentier.domain.Challenge
import app.sentier.domain.Challenges
import app.sentier.domain.GameRules
import app.sentier.domain.WeekActivity
import app.sentier.domain.WeekChallenges
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Les défis de la semaine, calculés à partir du journal du jeu, des habitudes et des pas. */
class ChallengeRepository(
    private val gameDao: GameDao,
    private val habitDao: HabitDao,
    private val game: GameRepository,
    private val clock: () -> LocalDate = { LocalDate.now() },
) {
    fun observe(): Flow<WeekChallenges> {
        val today = clock()
        val monday = Challenges.weekStart(today)
        val todayDay = today.toEpochDay()
        val weekStart = monday.toEpochDay()
        // Deux semaines de journal : la série de sport peut avoir commencé la semaine d'avant.
        return combine(
            gameDao.observeEventsSince(todayDay - 13),
            habitDao.observeStepsSince(weekStart),
            habitDao.observeHabits(),
        ) { events, steps, habits ->
            val sportIds = habits.filter { isSport(it) }.map { it.id }.toSet()
            val week = events.filter { it.day in weekStart..todayDay }
            val activity = WeekActivity(
                weekStart = weekStart,
                today = todayDay,
                steps = steps.filter { it.day <= todayDay }.sumOf { it.steps },
                tasks = week.count { it.kind == GameEvent.TASK },
                hardTasks = week.count { it.kind == GameEvent.TASK && it.xp >= GameRules.xpFor(3) },
                habits = week.count { it.kind == GameEvent.HABIT },
                activeDays = week.filter { it.kind != GameEvent.PURCHASE && it.kind != GameEvent.CHALLENGE }.map { it.day }.distinct().size,
                sportRun = Challenges.runEndingAt(
                    events.filter { it.kind == GameEvent.HABIT && it.refId in sportIds }.map { it.day }.toSet(),
                    todayDay,
                ),
                hasSport = sportIds.isNotEmpty(),
            )
            val keys = (0 until 10).associateBy { Challenges.keyOf(weekStart, it) }
            val rewarded = events.filter { it.kind == GameEvent.CHALLENGE }.mapNotNull { keys[it.refId] }.toSet()
            WeekChallenges(
                weekStart = weekStart,
                daysLeft = ChronoUnit.DAYS.between(today, monday.plusDays(6)).toInt(),
                list = Challenges.of(activity),
                rewarded = rewarded,
            )
        }
    }

    /** Verse le bonus des défis relevés cette semaine. Renvoie ceux qui viennent d'être récompensés. */
    suspend fun sync(): List<Challenge> {
        val week = observe().first()
        return week.list.filter { it.done && it.index !in week.rewarded }.filter { c ->
            game.onChallengeDone(Challenges.keyOf(week.weekStart, c.index), c.xp) != null
        }
    }

    private fun isSport(h: Habit): Boolean = h.icon == "sport" || h.name.contains("sport", ignoreCase = true)
}
