/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 *
 * Le calcul du score de force reprend la formule de Loop Habit Tracker
 * (https://github.com/iSoron/uhabits), Copyright (C) 2016-2021 Álinson
 * Santos Xavier, sous licence GPL-3.0 : moyenne mobile exponentielle dont
 * le facteur dépend de la fréquence de l'habitude.
 * Modifications : réécriture simplifiée en Kotlin pour Sentier, valeurs
 * chiffrées ramenées à une fraction de la cible, jour en cours non pénalisant.
 */
package app.sentier.domain

import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

object HabitScore {

    /** Facteur de la moyenne mobile : plus l'habitude est fréquente, plus le score réagit vite. */
    fun multiplier(freqNum: Int, freqDen: Int): Double {
        val frequency = freqNum.toDouble() / freqDen.coerceAtLeast(1)
        return 0.5.pow(sqrt(frequency) / 13.0)
    }

    /**
     * Calcule le score de force (0 à 1) au jour [today].
     *
     * @param completion part réalisée chaque jour (0 à 1), indexée par jour (epoch day)
     * @param startDay premier jour pris en compte (création de l'habitude)
     */
    fun compute(
        completion: Map<Long, Double>,
        freqNum: Int,
        freqDen: Int,
        startDay: Long,
        today: Long,
    ): Double {
        if (today < startDay) return 0.0
        val num = freqNum.coerceAtLeast(1)
        val den = freqDen.coerceAtLeast(1)
        val m = multiplier(num, den)
        // Le jour en cours ne fait baisser le score que s'il est déjà réalisé (sinon il compte quand il est fini).
        val lastDay = if ((completion[today] ?: 0.0) > 0.0) today else today - 1
        var score = 0.0
        var window = 0.0
        var day = startDay
        while (day <= lastDay) {
            window += completion[day] ?: 0.0
            if (day - den >= startDay) window -= completion[day - den] ?: 0.0
            val done = min(1.0, window / num)
            score = score * m + done * (1 - m)
            day++
        }
        return score.coerceIn(0.0, 1.0)
    }

    /** Part réalisée d'une valeur saisie : 1 si oui, valeur/cible si chiffrée. */
    fun completionOf(value: Double, numeric: Boolean, target: Double): Double =
        if (!numeric) (if (value > 0) 1.0 else 0.0)
        else if (target <= 0) (if (value > 0) 1.0 else 0.0)
        else min(1.0, value / target)
}

/** Description en français de la fréquence. */
fun describeFrequency(num: Int, den: Int): String = when {
    num >= den -> tr("Chaque jour", "Every day")
    den == 7 -> if (num == 1) tr("1 fois par semaine", "Once a week") else tr("$num fois par semaine", "$num times a week")
    den == 30 || den == 31 -> if (num == 1) tr("1 fois par mois", "Once a month") else tr("$num fois par mois", "$num times a month")
    num == 1 -> tr("Tous les $den jours", "Every $den days")
    else -> tr("$num fois tous les $den jours", "$num times every $den days")
}
