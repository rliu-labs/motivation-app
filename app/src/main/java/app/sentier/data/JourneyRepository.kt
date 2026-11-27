/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.data

import app.sentier.domain.Journey
import app.sentier.domain.Journeys
import app.sentier.domain.ReachedStop
import app.sentier.domain.TourProgress
import app.sentier.domain.WorldTour
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlin.math.ceil

data class JourneyState(
    val journey: Journey = WorldTour.journey,
    val progress: TourProgress = WorldTour.progress(0.0),
    /** Escales atteintes pas encore annoncées à l'écran (la plus récente en dernier). */
    val unseen: List<ReachedStop> = emptyList(),
    val stepLengthCm: Int = 75,
    /** Voyages finis, sans celui en cours. */
    val done: Set<String> = emptySet(),
    /** Voyages qu'on peut choisir pour la suite, une fois celui en cours fini. */
    val next: List<Journey> = emptyList(),
    val loading: Boolean = true,
) {
    /** Le voyage en cours est-il fini (ou l'était-il déjà) ? */
    fun isDone(j: Journey): Boolean = j.id in done || (j.id == journey.id && progress.done)
}

/**
 * Les voyages : les pas faits depuis le départ du voyage en cours font avancer d'escale en escale.
 * Le premier est le tour du monde, qui part du tout premier pas enregistré.
 */
class JourneyRepository(
    private val dao: HabitDao,
    private val settings: Settings,
    private val game: GameRepository? = null,
) {
    fun observe(): Flow<JourneyState> =
        combine(dao.observeTotalSteps(), settings.steps, settings.journey) { total, s, j ->
            val journey = Journeys.byId(j.current) ?: WorldTour.journey
            val km = kmOf(total, j.startSteps, s.stepLengthCm)
            val progress = journey.progress(km)
            val reached = journey.reachedStops(km)
            val done = j.done - journey.id
            JourneyState(
                journey = journey,
                progress = progress,
                unseen = reached.drop(j.seen.coerceAtMost(reached.size)),
                stepLengthCm = s.stepLengthCm,
                done = done,
                next = if (progress.done) Journeys.available(done + journey.id) else emptyList(),
                loading = false,
            )
        }

    /** Récompense les escales atteintes du voyage en cours (une seule fois chacune). */
    suspend fun sync() {
        val (journey, km) = current()
        for (stop in journey.reachedStops(km)) game?.onStopReached(stop.key)
    }

    /** Les escales atteintes ont été annoncées. */
    suspend fun markSeen() {
        val (journey, km) = current()
        settings.setJourneySeen(journey.reachedStops(km).size)
    }

    /**
     * Part pour le voyage [id], si celui en cours est fini et que [id] est débloqué.
     * Les pas faits après la fin du voyage précédent comptent déjà pour le nouveau.
     * Renvoie false si le voyage ne peut pas encore commencer.
     */
    suspend fun start(id: String): Boolean {
        val j = settings.currentJourney()
        val journey = Journeys.byId(j.current) ?: WorldTour.journey
        val total = dao.observeTotalSteps().first()
        val stepLength = settings.currentSteps().stepLengthCm.coerceAtLeast(1)
        if (kmOf(total, j.startSteps, stepLength) < journey.lengthKm) return false
        val done = j.done + journey.id
        val next = Journeys.available(done).firstOrNull { it.id == id } ?: return false
        val finishedAt = j.startSteps + ceil(journey.lengthKm * 100_000 / stepLength).toLong()
        settings.startJourney(next.id, finishedAt.coerceIn(0L, total), done)
        sync()
        return true
    }

    private suspend fun current(): Pair<Journey, Double> {
        val j = settings.currentJourney()
        val journey = Journeys.byId(j.current) ?: WorldTour.journey
        return journey to kmOf(dao.observeTotalSteps().first(), j.startSteps, settings.currentSteps().stepLengthCm)
    }

    private fun kmOf(total: Long, startSteps: Long, stepLengthCm: Int): Double =
        WorldTour.kmFor((total - startSteps).coerceAtLeast(0L), stepLengthCm)
}
