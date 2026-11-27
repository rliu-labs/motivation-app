/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.domain

/**
 * Objectif de pas adaptatif : un peu au-dessus de ce que l'on marche vraiment,
 * pour qu'il reste atteignable tout en donnant envie d'aller un peu plus loin.
 */
object AdaptiveGoal {
    /** Jours regardés (hors aujourd'hui, qui n'est pas fini). */
    const val WINDOW_DAYS = 14
    /** Jours avec des pas qu'il faut au moins pour ajuster l'objectif. */
    const val MIN_DAYS = 5
    const val MIN_GOAL = 3_000
    const val MAX_GOAL = 20_000
    /** Écart maximal d'un jour à l'autre, pour que l'objectif bouge en douceur. */
    const val MAX_CHANGE = 1_000
    private const val STEP = 250

    /**
     * Nouvel objectif à partir des pas des jours passés : la médiane des jours marchés, plus 10 %,
     * arrondie à 250 pas. Renvoie [current] s'il n'y a pas encore assez de jours.
     */
    fun compute(pastDays: List<Long>, current: Int): Int {
        val walked = pastDays.filter { it > 0 }.sorted()
        if (walked.size < MIN_DAYS) return current
        val median = if (walked.size % 2 == 1) {
            walked[walked.size / 2].toDouble()
        } else {
            (walked[walked.size / 2 - 1] + walked[walked.size / 2]) / 2.0
        }
        val target = (Math.round(median * 1.1 / STEP) * STEP).toInt().coerceIn(MIN_GOAL, MAX_GOAL)
        return target.coerceIn(current - MAX_CHANGE, current + MAX_CHANGE)
    }
}
