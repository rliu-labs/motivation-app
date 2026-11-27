/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.domain

enum class JourneyKind { WORLD, COUNTRY, THEME }

/** Cadre de la carte d'un voyage, en degrés. */
data class MapArea(val latTop: Double, val latBottom: Double, val lonLeft: Double, val lonRight: Double) {
    fun contains(lat: Double, lon: Double): Boolean = lat in latBottom..latTop && lon in lonLeft..lonRight
}

/**
 * Un voyage : une suite d'escales que les pas font atteindre une à une.
 * [keyBase] + n° d'escale donne la clé unique de chaque escale pour sa récompense : elle ne doit jamais changer.
 * [area] est le cadre de la carte (null : le monde entier). [requires] est le voyage à finir avant celui-ci.
 */
data class Journey(
    val id: String,
    val title: String,
    val kind: JourneyKind,
    val tagline: String,
    val keyBase: Long,
    val area: MapArea?,
    val stops: List<Stop>,
    val requires: String? = null,
) {
    val lengthKm: Double get() = stops.last().km

    fun keyOf(index: Int): Long = keyBase + index

    fun progress(km: Double): TourProgress = TourProgress(this, km.coerceAtLeast(0.0))

    /** Escales atteintes (hors départ), dans l'ordre. */
    fun reachedStops(km: Double): List<ReachedStop> =
        stops.mapIndexedNotNull { i, s -> if (i > 0 && s.km <= km) ReachedStop(this, i, s) else null }
}

/** Où l'on en est d'un voyage. */
data class TourProgress(val journey: Journey, val km: Double) {
    val stops: List<Stop> get() = journey.stops
    /** Index de la dernière escale atteinte (0 = départ). */
    val currentIndex: Int get() = stops.indexOfLast { it.km <= km }.coerceAtLeast(0)
    val current: Stop get() = stops[currentIndex]
    val nextStop: Stop? get() = stops.getOrNull(currentIndex + 1)
    val kmToNext: Double get() = nextStop?.let { it.km - km } ?: 0.0
    val done: Boolean get() = km >= journey.lengthKm
    val fraction: Float get() = (km / journey.lengthKm).toFloat().coerceIn(0f, 1f)
    /** Escales atteintes, départ compris. */
    val reached: List<Stop> get() = stops.take(currentIndex + 1)
}

/** Une escale atteinte (hors départ), avec sa clé unique pour la récompense. */
data class ReachedStop(val journey: Journey, val index: Int, val stop: Stop) {
    val key: Long get() = journey.keyOf(index)
}

/**
 * Tous les voyages, dans l'ordre où ils se débloquent : le tour du monde, puis le tour de France,
 * puis les pays d'Europe, à faire dans l'ordre qu'on veut.
 */
object Journeys {
    val all: List<Journey> = listOf(WorldTour.journey, TourDeFrance.journey) + EuropeTours.all + EuropeTours2.all + EuropeTours3.all

    fun byId(id: String?): Journey? = all.firstOrNull { it.id == id }

    /** Voyages qu'on peut commencer quand ceux de [done] sont finis (sans ceux déjà faits). */
    fun available(done: Set<String>): List<Journey> =
        all.filter { it.id !in done && it.requires != null && it.requires in done }

    /** Voyage à finir avant [journey], pour l'afficher sur un voyage encore verrouillé. */
    fun prerequisite(journey: Journey): Journey? = byId(journey.requires)
}
