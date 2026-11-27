/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.domain

import app.sentier.data.GameEvent
import app.sentier.data.HabitWithStats
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/** Les chiffres d'une semaine (ou du début de la semaine en cours). */
data class WeekNumbers(
    val activeDays: Int = 0,
    val habits: Int = 0,
    val tasks: Int = 0,
    val steps: Long = 0,
    val stepGoals: Int = 0,
    val xp: Int = 0,
) {
    val actions: Int get() = habits + tasks + stepGoals
}

/** Un moment fort de la semaine, montré dans le bilan. */
data class WeekHighlight(val icon: String, val title: String, val detail: String)

/** Objectif proposé pour la semaine suivante. */
data class WeeklyGoal(val kind: Kind, val target: Int) {
    enum class Kind { ACTIVE_DAYS, HABITS }

    val text: String get() = when (kind) {
        Kind.ACTIVE_DAYS -> if (target >= 7) tr("Être actif les 7 jours", "Be active all 7 days") else tr("Être actif $target jours", "Be active $target days")
        Kind.HABITS -> tr("Réussir $target habitudes", "Complete $target habits")
    }

    fun progressOf(n: WeekNumbers): Int = when (kind) {
        Kind.ACTIVE_DAYS -> n.activeDays
        Kind.HABITS -> n.habits
    }
}

/** Bilan d'une semaine du lundi au dimanche, avec ses moments forts et l'objectif de la suivante. */
data class WeeklyReview(
    val start: LocalDate,
    val today: LocalDate,
    val week: WeekNumbers,
    val previous: WeekNumbers,
    val highlights: List<WeekHighlight>,
    val goal: WeeklyGoal,
    /** Où en est l'objectif cette semaine, si le bilan porte sur la semaine passée. */
    val ongoing: WeekNumbers?,
) {
    val end: LocalDate get() = start.plusDays(6)

    val headline: String get() = when {
        week.activeDays >= 7 -> tr("Semaine parfaite !", "Perfect week!")
        week.activeDays >= 5 -> tr("Superbe semaine !", "Superb week!")
        week.activeDays >= 3 -> tr("Belle semaine", "Good week")
        week.activeDays >= 1 -> tr("Chaque pas compte", "Every step counts")
        else -> tr("Une semaine pour repartir", "A week to start again")
    }

    val message: String get() {
        val days = plural(week.activeDays, tr("jour", "day"))
        val diff = week.activeDays - previous.activeDays
        return when {
            week.activeDays == 0 -> tr("Pas d'activité cette semaine-là. Ce n'est pas grave : une petite habitude suffit pour relancer la machine.", "No activity that week. No big deal: one small habit is enough to get going again.")
            diff > 0 -> tr("Tu as été actif $days, ${plural(diff, "jour")} de plus que la semaine d'avant. Continue comme ça !", "You were active $days, ${plural(diff, "day")} more than the week before. Keep it up!")
            diff == 0 -> tr("Tu as été actif $days, aussi régulier que la semaine d'avant. La constance, c'est la clé.", "You were active $days, as steady as the week before. Consistency is key.")
            else -> tr("Tu as été actif $days. Un peu moins que la semaine d'avant, mais chaque jour compte.", "You were active $days. A little less than the week before, but every day counts.")
        }
    }

    /** Texte court de la notification du dimanche. */
    val summary: String get() =
        if (Lang.english) {
            "${plural(week.activeDays, "active day")}, ${plural(week.habits, "habit")}, ${plural(week.tasks, "task")}. " +
                "Goal: ${goal.text.replaceFirstChar { it.lowercase() }}."
        } else {
            "${plural(week.activeDays, "jour")} actif${if (week.activeDays > 1) "s" else ""}, " +
                "${plural(week.habits, "habitude")}, ${plural(week.tasks, "tâche")}. Objectif : ${goal.text.lowercase(Locale.FRANCE)}."
        }
}

internal fun plural(n: Int, word: String): String =
    if (n == 1 || (n == 0 && !Lang.english)) "$n $word" else "$n ${word}s"

object WeeklyReviews {
    /** Nombre de semaines regardées pour repérer un record. */
    const val HISTORY_WEEKS = 12

    /** Lundi de la semaine en bilan : celle qui se termine aujourd'hui le dimanche, sinon la précédente. */
    fun reviewedStart(today: LocalDate): LocalDate {
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return if (today.dayOfWeek == DayOfWeek.SUNDAY) monday else monday.minusWeeks(1)
    }

    /** Premier jour dont le bilan a besoin (pour les comparaisons). */
    fun firstNeededDay(today: LocalDate): LocalDate = reviewedStart(today).minusWeeks((HISTORY_WEEKS - 1).toLong())

    fun numbers(from: LocalDate, to: LocalDate, events: List<GameEvent>, steps: Map<Long, Long>): WeekNumbers {
        val range = from.toEpochDay()..to.toEpochDay()
        val inRange = events.filter { it.day in range }
        return WeekNumbers(
            activeDays = inRange.filter { GameEvent.isActivity(it.kind) }.map { it.day }.distinct().size,
            habits = inRange.count { it.kind == GameEvent.HABIT },
            tasks = inRange.count { it.kind == GameEvent.TASK },
            steps = steps.filterKeys { it in range }.values.sum(),
            stepGoals = inRange.count { it.kind == GameEvent.STEPS },
            xp = inRange.filter { it.kind != GameEvent.PURCHASE }.sumOf { it.xp },
        )
    }

    /** Objectif de la semaine suivante : un peu plus que la semaine écoulée, jamais démesuré. */
    fun nextGoal(week: WeekNumbers): WeeklyGoal = when {
        week.activeDays <= 0 -> WeeklyGoal(WeeklyGoal.Kind.ACTIVE_DAYS, 3)
        week.activeDays < 7 -> WeeklyGoal(WeeklyGoal.Kind.ACTIVE_DAYS, week.activeDays + 1)
        else -> WeeklyGoal(WeeklyGoal.Kind.HABITS, maxOf(week.habits + 1, (week.habits * 1.1).toInt() + 1))
    }

    fun build(
        today: LocalDate,
        events: List<GameEvent>,
        steps: Map<Long, Long>,
        habits: List<HabitWithStats>,
    ): WeeklyReview {
        val start = reviewedStart(today)
        val end = start.plusDays(6)
        val week = numbers(start, end, events, steps)
        val previous = numbers(start.minusWeeks(1), end.minusWeeks(1), events, steps)
        val earlier = (1 until HISTORY_WEEKS).map { w ->
            numbers(start.minusWeeks(w.toLong()), end.minusWeeks(w.toLong()), events, steps)
        }
        val ongoing = if (today > end) numbers(end.plusDays(1), today, events, steps) else null
        return WeeklyReview(
            start = start,
            today = today,
            week = week,
            previous = previous,
            highlights = highlights(start, week, previous, earlier, events, steps, habits),
            goal = nextGoal(week),
            ongoing = ongoing,
        )
    }

    private fun dayName(date: LocalDate): String = date.dayOfWeek.getDisplayName(TextStyle.FULL, Lang.locale)

    private fun highlights(
        start: LocalDate,
        week: WeekNumbers,
        previous: WeekNumbers,
        earlier: List<WeekNumbers>,
        events: List<GameEvent>,
        steps: Map<Long, Long>,
        habits: List<HabitWithStats>,
    ): List<WeekHighlight> {
        val out = mutableListOf<WeekHighlight>()
        val days = (0L..6L).map { start.plusDays(it) }
        if (week.activeDays >= 7) {
            out += WeekHighlight("star", tr("Semaine parfaite", "Perfect week"), tr("Actif les 7 jours, sans exception.", "Active all 7 days, no exceptions."))
        } else if (week.actions > 0 && earlier.isNotEmpty() && week.actions > earlier.maxOf { it.actions }) {
            out += WeekHighlight(
                "trophy", tr("Record sur 3 mois", "3-month record"),
                tr("${plural(week.actions, "action")}, ta semaine la plus remplie depuis ${HISTORY_WEEKS} semaines.", "${plural(week.actions, "action")}, your fullest week in ${HISTORY_WEEKS} weeks."),
            )
        }
        val perDay = days.associateWith { d ->
            events.count { it.day == d.toEpochDay() && GameEvent.isActivity(it.kind) }
        }
        perDay.maxByOrNull { it.value }?.takeIf { it.value > 0 }?.let { (d, n) ->
            out += WeekHighlight(
                "sun", tr("Meilleure journée", "Best day"),
                "${dayName(d).replaceFirstChar { it.titlecase(Lang.locale) }}" + tr(", avec ", ", with ") + "${plural(n, "action")}.",
            )
        }
        val range = days.first().toEpochDay()..days.last().toEpochDay()
        habits.filter { !it.habit.archived }
            .map { h -> h to range.count { day -> day >= h.habit.createdDay && h.isDoneOn(day) } }
            .filter { it.second > 0 }
            .maxByOrNull { it.second }
            ?.let { (h, n) ->
                out += WeekHighlight(
                    "habit", tr("Habitude phare", "Standout habit"),
                    "${h.habit.name}" + if (n >= 7) tr(", réussie tous les jours.", ", done every day.") else tr(", réussie $n jours sur 7.", ", done $n days out of 7."),
                )
            }
        if (week.habits > previous.habits && previous.habits > 0) {
            out += WeekHighlight(
                "up", tr("En progrès", "Improving"),
                plural(week.habits - previous.habits, tr("habitude", "habit")) + tr(" de plus que la semaine d'avant.", " more than the week before."),
            )
        }
        val bestSteps = days.maxByOrNull { steps[it.toEpochDay()] ?: 0L }
        val bestCount = bestSteps?.let { steps[it.toEpochDay()] } ?: 0L
        if (bestSteps != null && bestCount > 0) {
            out += WeekHighlight(
                "walk", tr("Record de pas", "Step record"),
                tr("${formatSteps(bestCount)} pas ${dayName(bestSteps)}, ${formatSteps(week.steps)} sur la semaine.", "${formatSteps(bestCount)} steps on ${dayName(bestSteps)}, ${formatSteps(week.steps)} over the week."),
            )
        }
        if (week.tasks > 0) {
            out += WeekHighlight(
                "task", tr("Tâches bouclées", "Tasks done"),
                if (Lang.english) "${plural(week.tasks, "task")} finished." else "${plural(week.tasks, "tâche")} terminée${if (week.tasks > 1) "s" else ""}.",
            )
        }
        return out.take(4)
    }

    private fun formatSteps(n: Long): String = java.text.NumberFormat.getIntegerInstance(Lang.locale).format(n)
}
