/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.domain

import app.sentier.data.Habit
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.floor

/** Une période lue dans Health Connect (une nuit de sommeil ou une séance de sport). */
data class HealthSession(val start: Instant, val end: Instant, val awakeMinutes: Long = 0) {
    val minutes: Long get() = ((end.epochSecond - start.epochSecond) / 60 - awakeMinutes).coerceAtLeast(0)
}

/** Ce que Health Connect donne pour un jour : sommeil de la nuit qui finit ce jour-là, et sport du jour. */
data class HealthDay(val sleepMinutes: Long = 0, val sportMinutes: Long = 0, val sportSessions: Int = 0)

/**
 * Relie le sommeil et le sport lus dans Health Connect aux habitudes : l'habitude de sommeil (icône « sleep »)
 * reçoit les heures dormies, l'habitude de sport (icône « sport ») est cochée les jours de séance.
 * Une valeur saisie à la main n'est jamais baissée.
 */
object HealthLink {
    /** Pour une habitude oui/non de sommeil : une nuit compte à partir de 7 heures. */
    const val GOOD_NIGHT_MINUTES = 7 * 60L

    /** Sommeil par jour : chaque nuit compte pour le jour du réveil. */
    fun sleepByDay(sessions: List<HealthSession>, zone: ZoneId): Map<LocalDate, Long> =
        sessions.groupBy { it.end.atZone(zone).toLocalDate() }.mapValues { (_, s) -> s.sumOf { it.minutes } }

    /** Sport par jour : minutes et nombre de séances, au jour du début de chaque séance. */
    fun sportByDay(sessions: List<HealthSession>, zone: ZoneId): Map<LocalDate, Pair<Long, Int>> =
        sessions.groupBy { it.start.atZone(zone).toLocalDate() }.mapValues { (_, s) -> s.sumOf { it.minutes } to s.size }

    fun isSleep(h: Habit): Boolean = h.icon == "sleep"
    fun isSport(h: Habit): Boolean = h.icon == "sport" || h.name.contains("sport", ignoreCase = true)

    /**
     * Valeur à enregistrer pour [habit] d'après [day], ou null si Health Connect n'apporte rien.
     * Habitude chiffrée : heures (ou minutes si l'unité est en minutes) pour le sommeil,
     * minutes ou nombre de séances pour le sport ; habitude oui/non : 1 si c'est fait.
     */
    fun valueFor(habit: Habit, day: HealthDay): Double? = when {
        isSleep(habit) -> {
            val m = day.sleepMinutes
            when {
                m <= 0 -> null
                !habit.isNumeric -> if (m >= GOOD_NIGHT_MINUTES) 1.0 else null
                isMinutes(habit.unit) -> m.toDouble()
                // Heures arrondies au dixième en dessous : 7 h 25 donne 7,4.
                else -> floor(m / 6.0) / 10.0
            }
        }
        isSport(habit) -> when {
            day.sportSessions <= 0 -> null
            !habit.isNumeric -> 1.0
            isMinutes(habit.unit) -> day.sportMinutes.toDouble()
            isHours(habit.unit) -> floor(day.sportMinutes / 6.0) / 10.0
            else -> day.sportSessions.toDouble()
        }
        else -> null
    }

    private fun isMinutes(unit: String): Boolean = unit.trim().lowercase().let { it.startsWith("min") || it == "mn" }
    private fun isHours(unit: String): Boolean = unit.trim().lowercase().let { it.startsWith("heure") || it == "h" }
}
