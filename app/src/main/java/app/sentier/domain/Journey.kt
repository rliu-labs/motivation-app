/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.domain

/** Une étape d'un voyage, à [km] kilomètres du départ (distances arrondies). */
data class Stop(
    val name: String,
    val km: Double,
    val lat: Double,
    val lon: Double,
    val fact: String,
)

data class Journey(
    val id: Int,
    val title: String,
    val region: String,
    val stops: List<Stop>,
) {
    val lengthKm: Double get() = stops.last().km
    val from: String get() = stops.first().name
    val to: String get() = stops.last().name
}

/** Où l'on en est dans l'enchaînement des voyages, pour une distance totale parcourue. */
data class JourneyProgress(
    val totalKm: Double,
    val journeyIndex: Int,
    val journey: Journey,
    /** Kilomètres parcourus dans le voyage en cours. */
    val km: Double,
    /** Vrai quand tous les voyages sont terminés. */
    val allDone: Boolean,
) {
    /** Étapes atteintes dans le voyage en cours (le départ compte). */
    val reachedCount: Int get() = journey.stops.count { it.km <= km }
    val nextStop: Stop? get() = journey.stops.firstOrNull { it.km > km }
    val kmToNext: Double get() = nextStop?.let { it.km - km } ?: 0.0
    val fraction: Float get() = (km / journey.lengthKm).toFloat().coerceIn(0f, 1f)
    val finishedJourneys: List<Journey> get() = Journeys.all.take(journeyIndex + if (allDone) 1 else 0)
}

/** Une étape atteinte, avec sa clé unique pour la récompense. */
data class ReachedStop(val journey: Journey, val index: Int, val stop: Stop) {
    val key: Long get() = Journeys.keyOf(journey, index)
}

/**
 * Les voyages, parcourus l'un après l'autre avec ses pas. Ce sont de vrais itinéraires ;
 * les distances sont arrondies et suivent à peu près le chemin à pied.
 */
object Journeys {
    /** XP d'une étape atteinte ; l'or suit la règle habituelle (un cinquième). */
    const val STOP_XP = 15

    val all: List<Journey> = listOf(
        Journey(
            1, "Le tour du lac d'Annecy", "Haute-Savoie",
            listOf(
                Stop("Annecy", 0.0, 45.899, 6.129, "Point de départ au bord du lac, près du palais de l'Île, l'ancienne prison au milieu du Thiou."),
                Stop("Sévrier", 6.0, 45.864, 6.141, "Rive ouest, face aux montagnes des Bauges."),
                Stop("Duingt", 13.0, 45.827, 6.199, "Le lac se resserre ici, avec un château posé sur une presqu'île."),
                Stop("Bout du Lac", 19.0, 45.795, 6.215, "Réserve naturelle au sud du lac, refuge des oiseaux et des roselières."),
                Stop("Talloires", 26.0, 45.840, 6.214, "Petit port niché dans une baie, au pied des falaises."),
                Stop("Menthon-Saint-Bernard", 30.0, 45.860, 6.194, "Son château domine le lac depuis le Moyen Âge."),
                Stop("Veyrier-du-Lac", 34.0, 45.883, 6.171, "Dernier village avant le retour, sous le mont Veyrier."),
                Stop("Annecy", 40.0, 45.899, 6.129, "Le tour est bouclé : environ 40 km autour de l'un des lacs les plus purs d'Europe."),
            ),
        ),
        Journey(
            2, "Le chemin de Stevenson", "Velay et Cévennes",
            listOf(
                Stop("Le Monastier-sur-Gazeille", 0.0, 44.940, 3.994, "En septembre 1878, l'écrivain Robert Louis Stevenson part d'ici avec l'ânesse Modestine."),
                Stop("Le Bouchet-Saint-Nicolas", 22.0, 44.889, 3.787, "Premier soir du voyage, non loin du lac du Bouchet, un ancien cratère."),
                Stop("Pradelles", 45.0, 44.770, 3.884, "Village perché classé parmi les plus beaux de France."),
                Stop("Langogne", 55.0, 44.727, 3.856, "Entrée en Lozère, avec sa halle aux grains en granit."),
                Stop("Notre-Dame-des-Neiges", 85.0, 44.594, 3.932, "Abbaye trappiste où Stevenson fait halte quelques jours."),
                Stop("Le Bleymard", 115.0, 44.484, 3.733, "Au pied du mont Lozère, avant la grande montée."),
                Stop("Pic de Finiels", 125.0, 44.431, 3.751, "Point culminant du mont Lozère, à 1 699 mètres."),
                Stop("Le Pont-de-Montvert", 135.0, 44.367, 3.746, "Village au pont de pierre, au cœur des Cévennes protestantes."),
                Stop("Florac", 160.0, 44.324, 3.594, "Petite ville au pied des falaises, siège du parc national des Cévennes."),
                Stop("Saint-Jean-du-Gard", 220.0, 44.104, 3.885, "Fin du voyage : Stevenson y vend Modestine avant de repartir."),
            ),
        ),
        Journey(
            3, "Le canal du Midi", "Occitanie",
            listOf(
                Stop("Toulouse", 0.0, 43.604, 1.444, "La ville rose, départ du canal creusé au XVIIe siècle sous la direction de Pierre-Paul Riquet."),
                Stop("Seuil de Naurouze", 52.0, 43.355, 1.817, "Point le plus haut du canal, où l'eau se partage entre l'Atlantique et la Méditerranée."),
                Stop("Castelnaudary", 65.0, 43.318, 1.954, "Son Grand Bassin est le plus vaste plan d'eau du canal, et c'est la patrie du cassoulet."),
                Stop("Carcassonne", 105.0, 43.212, 2.353, "La Cité médiévale et le canal sont tous deux inscrits au patrimoine mondial de l'UNESCO."),
                Stop("Capestang", 190.0, 43.328, 3.045, "Village dominé par sa grande collégiale restée inachevée."),
                Stop("Béziers", 208.0, 43.344, 3.215, "Les neuf écluses de Fonseranes forment un escalier d'eau unique."),
                Stop("Agde", 228.0, 43.310, 3.475, "Son écluse ronde, unique sur le canal, dessert trois directions."),
                Stop("Sète", 245.0, 43.403, 3.697, "Port de pêche sur l'étang de Thau, et la Méditerranée en récompense."),
            ),
        ),
        Journey(
            4, "De Paris au Mont-Saint-Michel", "Île-de-France et Normandie",
            listOf(
                Stop("Paris", 0.0, 48.853, 2.349, "Départ du parvis de Notre-Dame, le point zéro des routes de France."),
                Stop("Versailles", 20.0, 48.805, 2.120, "Le château de Louis XIV et ses jardins à la française."),
                Stop("Chartres", 95.0, 48.448, 1.488, "La cathédrale et ses vitraux, parmi les mieux conservés au monde."),
                Stop("Nogent-le-Rotrou", 150.0, 48.322, 0.822, "Capitale du Perche, gardée par le château Saint-Jean."),
                Stop("Alençon", 210.0, 48.432, 0.091, "Ville du point d'Alençon, une dentelle reconnue par l'UNESCO."),
                Stop("Domfront", 275.0, 48.593, -0.646, "Cité médiévale perchée sur un éperon rocheux."),
                Stop("Avranches", 345.0, 48.684, -1.357, "Les manuscrits du Mont y sont conservés, face à la baie."),
                Stop("Mont-Saint-Michel", 365.0, 48.636, -1.511, "L'abbaye sur son rocher, au milieu de l'une des plus grandes marées d'Europe."),
            ),
        ),
    )

    val totalKm: Double get() = all.sumOf { it.lengthKm }

    /** Distance parcourue à partir d'un nombre de pas et de la longueur d'un pas. */
    fun kmFor(steps: Long, stepLengthCm: Int): Double = steps * stepLengthCm / 100_000.0

    fun progress(totalKm: Double): JourneyProgress {
        var rest = totalKm.coerceAtLeast(0.0)
        all.forEachIndexed { i, journey ->
            if (rest < journey.lengthKm) return JourneyProgress(totalKm, i, journey, rest, allDone = false)
            rest -= journey.lengthKm
        }
        val last = all.last()
        return JourneyProgress(totalKm, all.lastIndex, last, last.lengthKm, allDone = true)
    }

    /** Toutes les étapes atteintes (hors points de départ), dans l'ordre. */
    fun reachedStops(totalKm: Double): List<ReachedStop> {
        val result = mutableListOf<ReachedStop>()
        var offset = 0.0
        for (journey in all) {
            journey.stops.forEachIndexed { i, stop ->
                if (i > 0 && offset + stop.km <= totalKm) result += ReachedStop(journey, i, stop)
            }
            offset += journey.lengthKm
        }
        return result
    }

    fun keyOf(journey: Journey, index: Int): Long = journey.id * 100L + index
}
