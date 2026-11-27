/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** Ce qui a été fait depuis le lundi de la semaine en cours. */
data class WeekActivity(
    /** Lundi de la semaine (jour epoch). */
    val weekStart: Long,
    val today: Long,
    val steps: Long = 0,
    val tasks: Int = 0,
    /** Tâches difficiles terminées. */
    val hardTasks: Int = 0,
    val habits: Int = 0,
    /** Jours avec au moins une action. */
    val activeDays: Int = 0,
    /** Jours de sport d'affilée jusqu'à aujourd'hui (ou hier, si la séance du jour reste à faire). */
    val sportRun: Int = 0,
    /** Vrai si une habitude de sport existe. */
    val hasSport: Boolean = false,
)

/** Un défi de la semaine, avec un bonus d'XP et d'or s'il est relevé. */
data class Challenge(
    val index: Int,
    val title: String,
    val goalText: String,
    /** Clin d'œil culturel. */
    val flavor: String,
    val icon: String,
    val current: Long,
    val goal: Long,
    val xp: Int,
    /** Conseil quand le défi ne peut pas encore avancer. */
    val hint: String? = null,
) {
    val done: Boolean get() = current >= goal
    val gold: Int get() = GameRules.goldFor(xp)
    val progress: Float get() = (current.toFloat() / goal).coerceIn(0f, 1f)
}

data class WeekChallenges(
    val weekStart: Long = 0,
    /** Jours restants après aujourd'hui (0 le dimanche). */
    val daysLeft: Int = 6,
    val list: List<Challenge> = emptyList(),
    /** Index des défis dont le bonus est déjà versé. */
    val rewarded: Set<Int> = emptySet(),
) {
    val doneCount: Int get() = list.count { it.done }
}

/**
 * Les défis de la semaine, du lundi au dimanche. Chacun ne rapporte son bonus qu'une fois par semaine,
 * et rien n'est perdu si un défi n'est pas relevé.
 */
object Challenges {
    const val WEEKLY_STEPS = 70_000L
    const val SPORT_DAYS = 7

    fun weekStart(day: LocalDate): LocalDate = day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    /** Clé unique d'un défi d'une semaine, pour ne verser le bonus qu'une fois. */
    fun keyOf(weekStart: Long, index: Int): Long = weekStart * 10 + index

    /** Jours d'affilée de [days] qui finissent aujourd'hui, ou hier si aujourd'hui n'en fait pas (encore) partie. */
    fun runEndingAt(days: Set<Long>, today: Long): Int {
        var day = if (today in days) today else today - 1
        var run = 0
        while (day in days) {
            run++
            day--
        }
        return run
    }

    fun of(a: WeekActivity): List<Challenge> = listOf(
        Challenge(
            0, tr("La route de la soie", "The Silk Road"), tr("Marcher 70 000 pas cette semaine", "Walk 70,000 steps this week"),
            tr("Les caravanes reliaient la Chine à la Méditerranée pas à pas, sur plus de 6 000 km.", "Caravans linked China to the Mediterranean step by step, over more than 6,000 km."),
            "walk", a.steps, WEEKLY_STEPS, xp = 60,
            hint = tr("Active le comptage des pas dans l'onglet Habitudes.", "Turn on step counting in the Habits tab.").takeIf { a.steps == 0L },
        ),
        Challenge(
            1, tr("Esprit olympique", "Olympic spirit"), tr("Faire du sport 7 jours d'affilée", "Work out 7 days in a row"),
            tr("À Olympie, chaque athlète jurait s'être entraîné dix mois avant les Jeux.", "At Olympia, every athlete swore they had trained for ten months before the Games."),
            "sport", a.sportRun.toLong().coerceAtMost(SPORT_DAYS.toLong()), SPORT_DAYS.toLong(), xp = 80,
            hint = tr("Ajoute l'habitude « Faire du sport » pour relever ce défi.", "Add the habit “Work out” to take on this challenge.").takeIf { !a.hasSport },
        ),
        Challenge(
            2, tr("Kaizen", "Kaizen"), tr("Être actif 5 jours cette semaine", "Be active 5 days this week"),
            tr("Au Japon, le kaizen, c'est progresser un tout petit peu chaque jour.", "In Japan, kaizen means getting a tiny bit better every day."),
            "calendar", a.activeDays.toLong(), 5, xp = 30,
        ),
        Challenge(
            3, tr("Pierre après pierre", "Stone by stone"), tr("Terminer 10 tâches cette semaine", "Finish 10 tasks this week"),
            tr("Rome ne s'est pas faite en un jour, mais une pierre après l'autre.", "Rome wasn't built in a day, but one stone after another."),
            "task", a.tasks.toLong(), 10, xp = 30,
        ),
        Challenge(
            4, tr("Les travaux d'Hercule", "The labours of Hercules"), tr("Terminer 3 tâches difficiles cette semaine", "Finish 3 hard tasks this week"),
            tr("Hercule en a accompli douze. Trois, c'est déjà un bel exploit.", "Hercules completed twelve. Three is already quite a feat."),
            "rocket", a.hardTasks.toLong(), 3, xp = 50,
        ),
        Challenge(
            5, tr("Le rituel du maté", "The mate ritual"), tr("Réussir 15 habitudes cette semaine", "Complete 15 habits this week"),
            tr("En Argentine, le maté se prépare et se partage chaque jour, comme un rituel.", "In Argentina, mate is prepared and shared every day, like a ritual."),
            "spa", a.habits.toLong(), 15, xp = 30,
        ),
    )
}
