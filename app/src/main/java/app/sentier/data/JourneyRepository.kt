/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.data

import app.sentier.domain.ReachedStop
import app.sentier.domain.TourProgress
import app.sentier.domain.WorldTour
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

data class JourneyState(
    val progress: TourProgress = WorldTour.progress(0.0),
    /** Escales atteintes pas encore annoncées à l'écran (la plus récente en dernier). */
    val unseen: List<ReachedStop> = emptyList(),
    val stepLengthCm: Int = 75,
    val loading: Boolean = true,
)

/** Le tour du monde : tous les pas enregistrés font avancer d'escale en escale. */
class JourneyRepository(
    private val dao: HabitDao,
    private val settings: Settings,
    private val game: GameRepository? = null,
) {
    fun observe(): Flow<JourneyState> =
        combine(dao.observeTotalSteps(), settings.steps, settings.journeySeen) { total, s, seen ->
            val km = WorldTour.kmFor(total, s.stepLengthCm)
            val reached = WorldTour.reachedStops(km)
            JourneyState(WorldTour.progress(km), reached.drop(seen.coerceAtMost(reached.size)), s.stepLengthCm, loading = false)
        }

    /** Récompense les escales atteintes (une seule fois chacune). */
    suspend fun sync() {
        val km = WorldTour.kmFor(dao.observeTotalSteps().first(), settings.currentSteps().stepLengthCm)
        for (stop in WorldTour.reachedStops(km)) game?.onStopReached(stop.key)
    }

    /** Les escales atteintes ont été annoncées. */
    suspend fun markSeen() {
        val km = WorldTour.kmFor(dao.observeTotalSteps().first(), settings.currentSteps().stepLengthCm)
        settings.setJourneySeen(WorldTour.reachedStops(km).size)
    }
}
