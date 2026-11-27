/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.data

import app.sentier.domain.SeasonActivity
import app.sentier.domain.SeasonalChallenge
import app.sentier.domain.Seasons
import app.sentier.domain.SeasonsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Les défis saisonniers : celui en cours avance avec ce qui est fait depuis son début,
 * et son bonus (avec un badge rare) n'est versé qu'une fois par an.
 */
class SeasonRepository(
    private val gameDao: GameDao,
    private val habitDao: HabitDao,
    private val game: GameRepository,
    private val clock: () -> LocalDate = { LocalDate.now() },
) {
    fun observe(): Flow<SeasonsState> {
        val today = clock()
        val todayDay = today.toEpochDay()
        val season = Seasons.active(today)
        // Sans défi en cours, on ne lit que le jour même : rien ne compte.
        val from = season?.startIn(today.year)?.toEpochDay() ?: todayDay
        return combine(
            gameDao.observeEventsSince(from),
            habitDao.observeStepsSince(from),
            habitDao.observeHabits(),
            gameDao.observeRefDays(GameEvent.SEASON),
        ) { events, steps, habits, won ->
            val wins = Seasons.winsOf(won.map { it.refId })
            if (season == null) {
                val (next, start) = Seasons.next(today)
                return@combine SeasonsState(next = next, nextStart = start, wins = wins)
            }
            val sportIds = habits.filter { isSport(it) }.map { it.id }.toSet()
            val inRange = events.filter { it.day in from..todayDay }
            val activity = SeasonActivity(
                steps = steps.filter { it.day <= todayDay }.sumOf { it.steps },
                activeDays = inRange.filter { GameEvent.isActivity(it.kind) }.map { it.day }.distinct().size,
                habits = inRange.count { it.kind == GameEvent.HABIT },
                tasks = inRange.count { it.kind == GameEvent.TASK },
                sportDays = inRange.filter { it.kind == GameEvent.HABIT && it.refId in sportIds }.map { it.day }.distinct().size,
                hasSport = sportIds.isNotEmpty(),
            )
            val key = Seasons.keyOf(today.year, season.code)
            SeasonsState(
                active = Seasons.challenge(season, today, activity, rewarded = won.any { it.refId == key }),
                wins = wins,
            )
        }
    }

    /** Verse le bonus du défi saisonnier s'il vient d'être réussi. Renvoie le défi récompensé, ou null. */
    suspend fun sync(): SeasonalChallenge? {
        val c = observe().first().active ?: return null
        if (!c.done || c.rewarded) return null
        return c.takeIf { game.onSeasonDone(c.key, c.season.xp) != null }
    }

    private fun isSport(h: Habit): Boolean = h.icon == "sport" || h.name.contains("sport", ignoreCase = true)
}
