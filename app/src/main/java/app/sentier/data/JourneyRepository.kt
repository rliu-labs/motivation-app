/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.data

import app.sentier.domain.JourneyProgress
import app.sentier.domain.Journeys
import app.sentier.domain.ReachedStop
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

data class JourneyState(
    val progress: JourneyProgress = Journeys.progress(0.0),
    /** Étapes atteintes pas encore annoncées à l'écran (la plus récente en dernier). */
    val unseen: List<ReachedStop> = emptyList(),
    val stepLengthCm: Int = 75,
    val loading: Boolean = true,
)

/** Le voyage virtuel : tous les pas enregistrés font avancer d'étape en étape. */
class JourneyRepository(
    private val dao: HabitDao,
    private val settings: Settings,
    private val game: GameRepository? = null,
) {
    fun observe(): Flow<JourneyState> =
        combine(dao.observeTotalSteps(), settings.steps, settings.journeySeen) { total, s, seen ->
            val km = Journeys.kmFor(total, s.stepLengthCm)
            val reached = Journeys.reachedStops(km)
            JourneyState(Journeys.progress(km), reached.drop(seen.coerceAtMost(reached.size)), s.stepLengthCm, loading = false)
        }

    /** Récompense les étapes atteintes (une seule fois chacune). */
    suspend fun sync() {
        val km = Journeys.kmFor(dao.observeTotalSteps().first(), settings.currentSteps().stepLengthCm)
        for (stop in Journeys.reachedStops(km)) game?.onStopReached(stop.key)
    }

    /** Les étapes atteintes ont été annoncées. */
    suspend fun markSeen() {
        val km = Journeys.kmFor(dao.observeTotalSteps().first(), settings.currentSteps().stepLengthCm)
        settings.setJourneySeen(Journeys.reachedStops(km).size)
    }
}
