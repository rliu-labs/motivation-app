/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.data

import app.sentier.domain.WeeklyReview
import app.sentier.domain.WeeklyReviews
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/** Bilan de la semaine, calculé sur le téléphone à partir du journal du jeu, des pas et des habitudes. */
class WeeklyReviewRepository(
    private val gameDao: GameDao,
    private val habitDao: HabitDao,
    private val habits: HabitRepository,
    private val clock: () -> LocalDate = { LocalDate.now() },
) {
    fun observe(): Flow<WeeklyReview> {
        val today = clock()
        val from = WeeklyReviews.firstNeededDay(today).toEpochDay()
        return combine(
            gameDao.observeEventsSince(from),
            habitDao.observeStepsSince(from),
            habits.observeHabitsWithStats(),
        ) { events, steps, habitStats ->
            WeeklyReviews.build(today, events, steps.associate { it.day to it.steps }, habitStats)
        }
    }

    suspend fun current(): WeeklyReview = observe().first()
}
