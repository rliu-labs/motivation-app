/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.domain

/** Un tampon du passeport : une escale (hors départ), datée si elle a été atteinte. */
data class Stamp(
    val journey: Journey,
    val index: Int,
    val stop: Stop,
    /** Jour (epoch) où l'escale a été atteinte, ou null si elle reste à atteindre. */
    val day: Long?,
) {
    val stamped: Boolean get() = day != null
}

/** Une page du passeport : les tampons d'un voyage. */
data class PassportPage(val journey: Journey, val stamps: List<Stamp>) {
    val count: Int get() = stamps.count { it.stamped }
    val complete: Boolean get() = stamps.isNotEmpty() && stamps.all { it.stamped }
}

data class PassportState(
    val pages: List<PassportPage> = emptyList(),
    val loading: Boolean = true,
) {
    val stamps: Int get() = pages.sumOf { it.count }
    /** Pays où au moins une escale a été tamponnée. */
    val countries: Int get() = pages.flatMap { p -> p.stamps.filter { it.stamped }.map { it.stop.country } }.distinct().size
    val journeysDone: Int get() = pages.count { it.complete }
    /** Premier tampon, pour la date de délivrance du passeport. */
    val firstDay: Long? get() = pages.flatMap { p -> p.stamps.mapNotNull { it.day } }.minOrNull()
}

/**
 * Le passeport de voyage : un tampon pour chaque escale atteinte, tous voyages confondus.
 * Les escales sont retrouvées grâce à la clé de leur récompense, qui garde aussi le jour où elles ont été atteintes.
 */
object Passport {
    /**
     * [reached] : jour où chaque escale a été atteinte, par clé de récompense.
     * Une page par voyage commencé (au moins un tampon) ou en cours ([currentId]), dans l'ordre des voyages.
     */
    fun of(reached: Map<Long, Long>, currentId: String?): PassportState {
        val pages = Journeys.all.mapNotNull { j ->
            val stamps = j.stops.mapIndexedNotNull { i, stop ->
                if (i == 0) null else Stamp(j, i, stop, reached[j.keyOf(i)])
            }
            PassportPage(j, stamps).takeIf { page -> page.count > 0 || j.id == currentId }
        }
        return PassportState(pages, loading = false)
    }
}
