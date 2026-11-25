/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 *
 * Ce programme est un logiciel libre : vous pouvez le redistribuer et/ou le
 * modifier selon les termes de la GNU General Public License version 3
 * publiée par la Free Software Foundation, ou (à votre choix) toute version
 * ultérieure. Voir le fichier LICENSE.
 *
 * Le principe (règle de répétition au format RRULE, option « à partir de la
 * date de fin ») est inspiré de Tasks.org (https://github.com/tasks/tasks,
 * GPL-3.0). Code réécrit et simplifié pour Sentier.
 */
package app.sentier.domain

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Règle de répétition d'une tâche.
 *
 * Stockée sous forme d'une chaîne RRULE simplifiée (RFC 5545), par exemple
 * `FREQ=DAILY;INTERVAL=2` ou `FREQ=WEEKLY;BYDAY=MO,WE,FR`.
 */
data class Recurrence(
    val frequency: Frequency,
    val interval: Int = 1,
    val days: Set<DayOfWeek> = emptySet(),
) {
    enum class Frequency { DAILY, WEEKLY, MONTHLY }

    init {
        require(interval >= 1) { "interval doit être ≥ 1" }
    }

    fun toRRule(): String = buildString {
        append("FREQ=").append(frequency.name)
        if (interval > 1) append(";INTERVAL=").append(interval)
        if (frequency == Frequency.WEEKLY && days.isNotEmpty()) {
            append(";BYDAY=")
            append(days.sorted().joinToString(",") { it.rruleCode() })
        }
    }

    /** Prochaine occurrence strictement après [from]. */
    fun nextAfter(from: LocalDate): LocalDate = when (frequency) {
        Frequency.DAILY -> from.plusDays(interval.toLong())
        Frequency.MONTHLY -> from.plusMonths(interval.toLong())
        Frequency.WEEKLY -> if (days.isEmpty()) {
            from.plusWeeks(interval.toLong())
        } else {
            nextWeekday(from)
        }
    }

    private fun nextWeekday(from: LocalDate): LocalDate {
        var candidate = from
        while (true) {
            candidate = candidate.plusDays(1)
            // En passant à une nouvelle semaine, sauter les semaines non concernées.
            if (candidate.dayOfWeek == DayOfWeek.MONDAY && interval > 1) {
                candidate = candidate.plusWeeks((interval - 1).toLong())
            }
            if (candidate.dayOfWeek in days) return candidate
        }
    }

    /**
     * Calcule la prochaine échéance quand on valide une tâche.
     *
     * @param dueDate échéance actuelle (ou null)
     * @param today date du jour
     * @param fromCompletion true pour « répéter à partir de la date de fin »
     *
     * Contrairement à Tasks.org, on ne laisse jamais la nouvelle échéance
     * dans le passé : après plusieurs jours de retard, la tâche revient à la
     * prochaine occurrence à venir, pour ne pas accumuler de culpabilité.
     */
    fun nextDueDate(dueDate: LocalDate?, today: LocalDate, fromCompletion: Boolean): LocalDate {
        val base = if (fromCompletion || dueDate == null) today else dueDate
        var next = nextAfter(base)
        while (!next.isAfter(today)) next = nextAfter(next)
        return next
    }

    /** Description en français, par ex. « Tous les 2 jours ». */
    fun describe(): String = when (frequency) {
        Frequency.DAILY -> if (interval == 1) "Chaque jour" else "Tous les $interval jours"
        Frequency.MONTHLY -> if (interval == 1) "Chaque mois" else "Tous les $interval mois"
        Frequency.WEEKLY -> {
            val prefix = if (interval == 1) "Chaque semaine" else "Toutes les $interval semaines"
            when {
                days.isEmpty() -> prefix
                days == WEEKDAYS -> if (interval == 1) "En semaine (lun.–ven.)" else "$prefix, du lundi au vendredi"
                days.size == 7 && interval == 1 -> "Chaque jour"
                else -> "$prefix : " + days.sorted().joinToString(", ") { it.shortFr() }
            }
        }
    }

    companion object {
        val WEEKDAYS: Set<DayOfWeek> = setOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
        )

        /** Lit une RRULE ; renvoie null si vide ou illisible. */
        fun parse(rrule: String?): Recurrence? {
            if (rrule.isNullOrBlank()) return null
            val parts = rrule.removePrefix("RRULE:").split(';')
                .mapNotNull { p -> p.split('=', limit = 2).takeIf { it.size == 2 }?.let { it[0].uppercase() to it[1] } }
                .toMap()
            val freq = when (parts["FREQ"]?.uppercase()) {
                "DAILY" -> Frequency.DAILY
                "WEEKLY" -> Frequency.WEEKLY
                "MONTHLY" -> Frequency.MONTHLY
                else -> return null
            }
            val interval = parts["INTERVAL"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1
            val days = parts["BYDAY"]?.split(',')?.mapNotNull { dayFromCode(it.trim()) }?.toSet() ?: emptySet()
            return Recurrence(freq, interval, if (freq == Frequency.WEEKLY) days else emptySet())
        }

        private fun dayFromCode(code: String): DayOfWeek? = when (code.takeLast(2).uppercase()) {
            "MO" -> DayOfWeek.MONDAY
            "TU" -> DayOfWeek.TUESDAY
            "WE" -> DayOfWeek.WEDNESDAY
            "TH" -> DayOfWeek.THURSDAY
            "FR" -> DayOfWeek.FRIDAY
            "SA" -> DayOfWeek.SATURDAY
            "SU" -> DayOfWeek.SUNDAY
            else -> null
        }
    }
}

fun DayOfWeek.rruleCode(): String = name.take(2)

fun DayOfWeek.shortFr(): String = when (this) {
    DayOfWeek.MONDAY -> "lun."
    DayOfWeek.TUESDAY -> "mar."
    DayOfWeek.WEDNESDAY -> "mer."
    DayOfWeek.THURSDAY -> "jeu."
    DayOfWeek.FRIDAY -> "ven."
    DayOfWeek.SATURDAY -> "sam."
    DayOfWeek.SUNDAY -> "dim."
}

fun DayOfWeek.letterFr(): String = when (this) {
    DayOfWeek.MONDAY -> "L"
    DayOfWeek.TUESDAY -> "M"
    DayOfWeek.WEDNESDAY -> "M"
    DayOfWeek.THURSDAY -> "J"
    DayOfWeek.FRIDAY -> "V"
    DayOfWeek.SATURDAY -> "S"
    DayOfWeek.SUNDAY -> "D"
}
