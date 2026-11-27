/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.domain

import java.time.LocalDate

/**
 * Règles du jeu, inspirées de Habitica : chaque action rapporte de l'expérience (XP)
 * et de l'or, l'XP fait monter de niveau, l'or s'échange contre ses propres récompenses.
 * Rien n'est jamais perdu : un jour raté ne retire ni XP ni or.
 */
object GameRules {
    /** XP d'une tâche ou d'une habitude selon sa difficulté (1 facile, 2 moyen, 3 difficile). */
    fun xpFor(difficulty: Int): Int = when (difficulty) {
        3 -> 30
        2 -> 20
        else -> 10
    }

    /** L'or vaut un cinquième de l'XP gagnée. */
    fun goldFor(xp: Int): Int = xp / 5

    /** Objectif de pas atteint. */
    const val STEPS_XP = 20

    /** XP à gagner pour passer du niveau [level] au suivant : 50, 75, 100… */
    fun xpToNext(level: Int): Int = 50 + 25 * (level - 1)

    fun levelOf(totalXp: Int): LevelInfo {
        var level = 1
        var rest = totalXp.coerceAtLeast(0)
        while (rest >= xpToNext(level)) {
            rest -= xpToNext(level)
            level++
        }
        return LevelInfo(level, rest, xpToNext(level))
    }
}

data class LevelInfo(val level: Int, val xpInLevel: Int, val xpForNext: Int) {
    val progress: Float get() = xpInLevel.toFloat() / xpForNext
}

data class StreakInfo(
    /** Jours d'affilée avec au moins une action (les jours de repos ne cassent pas la série). */
    val current: Int = 0,
    val best: Int = 0,
    /** Jours de repos en réserve. */
    val restDays: Int = 0,
    /** Vrai si aujourd'hui compte déjà dans la série. */
    val activeToday: Boolean = false,
    /** Gels de série en réserve. */
    val freezes: Int = 0,
    /** Gels déjà utilisés pour sauver la série. */
    val freezesUsed: Int = 0,
)

object Streaks {
    const val MAX_REST_DAYS = 2
    const val DAYS_PER_REST_DAY = 7

    /** Gels gardés en réserve au plus. */
    const val MAX_FREEZES = 2

    /** Prix d'un gel dans la boutique. */
    const val FREEZE_COST = 40

    /**
     * Calcule la série. Un jour sans action ne la casse pas s'il reste un jour de repos,
     * sinon un gel de série est utilisé s'il y en a un. Les gels viennent des achats ([boughtFreezes],
     * jour de chaque achat) et, si [gifts] est vrai, d'un gel offert au départ puis au début de chaque
     * trimestre (1er janvier, avril, juillet et octobre). Un gel ne protège que les jours qui suivent son arrivée.
     */
    fun compute(activeDays: Set<Long>, today: Long, boughtFreezes: List<Long> = emptyList(), gifts: Boolean = false): StreakInfo {
        val first = activeDays.filter { it <= today }.minOrNull()
            ?: return StreakInfo(restDays = 1, freezes = if (gifts) 1 else 0)
        val bought = boughtFreezes.groupingBy { it }.eachCount()
        var current = 0
        var best = 0
        var rest = 1
        var sinceEarned = 0
        var freezes = 0
        var used = 0
        for (day in first..today) {
            if (gifts && (day == first || isQuarterStart(day))) freezes = (freezes + 1).coerceAtMost(MAX_FREEZES)
            bought[day]?.let { freezes = (freezes + it).coerceAtMost(MAX_FREEZES) }
            if (day in activeDays) {
                current++
                sinceEarned++
                if (sinceEarned == DAYS_PER_REST_DAY) {
                    rest = (rest + 1).coerceAtMost(MAX_REST_DAYS)
                    sinceEarned = 0
                }
            } else if (day != today) {
                when {
                    current > 0 && rest > 0 -> rest--
                    current > 0 && freezes > 0 -> {
                        freezes--
                        used++
                    }
                    else -> {
                        current = 0
                        sinceEarned = 0
                    }
                }
            }
            best = maxOf(best, current)
        }
        return StreakInfo(current, best, rest, activeToday = today in activeDays, freezes = freezes, freezesUsed = used)
    }

    private fun isQuarterStart(day: Long): Boolean {
        val d = LocalDate.ofEpochDay(day)
        return d.dayOfMonth == 1 && d.monthValue % 3 == 1
    }
}

data class GameStats(
    val tasksDone: Int = 0,
    val habitsDone: Int = 0,
    val stepGoals: Int = 0,
    val rewardsTaken: Int = 0,
    val level: Int = 1,
    val bestStreak: Int = 0,
    /** Escales atteintes, tous voyages confondus. */
    val stops: Int = 0,
    /** Défis de la semaine relevés. */
    val challenges: Int = 0,
)

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    /** Nom de l'icône (voir BadgeIcons dans l'interface). */
    val icon: String,
    val current: Int,
    val goal: Int,
    /** Badge rare d'un défi saisonnier : [current] est le nombre de fois où il a été gagné. */
    val rare: Boolean = false,
) {
    val unlocked: Boolean get() = current >= goal
}

object Badges {
    fun of(s: GameStats): List<Badge> = listOf(
        Badge("first_task", tr("Premier pas", "First step"), tr("Finir une première tâche", "Finish a first task"), "flag", s.tasksDone, 1),
        Badge("tasks_10", tr("Bien lancé", "Off to a good start"), tr("Finir 10 tâches", "Finish 10 tasks"), "task", s.tasksDone, 10),
        Badge("tasks_100", tr("Infatigable", "Tireless"), tr("Finir 100 tâches", "Finish 100 tasks"), "rocket", s.tasksDone, 100),
        Badge("habits_30", tr("Rituel", "Ritual"), tr("Réussir 30 fois une habitude", "Complete a habit 30 times"), "spa", s.habitsDone, 30),
        Badge("steps_7", tr("Marcheur", "Walker"), tr("Atteindre 7 fois l'objectif de pas", "Reach the step goal 7 times"), "walk", s.stepGoals, 7),
        Badge("streak_7", tr("Une semaine", "One week"), tr("Tenir une série de 7 jours", "Keep a 7-day streak"), "fire", s.bestStreak, 7),
        Badge("streak_30", tr("Un mois", "One month"), tr("Tenir une série de 30 jours", "Keep a 30-day streak"), "calendar", s.bestStreak, 30),
        Badge("level_5", tr("Niveau 5", "Level 5"), tr("Atteindre le niveau 5", "Reach level 5"), "star", s.level, 5),
        Badge("level_10", tr("Niveau 10", "Level 10"), tr("Atteindre le niveau 10", "Reach level 10"), "trophy", s.level, 10),
        Badge("reward_1", tr("Je me fais plaisir", "Treat yourself"), tr("Prendre une récompense dans la boutique", "Take a reward from the shop"), "gift", s.rewardsTaken, 1),
        Badge("stops_5", tr("En route", "On the way"), tr("Atteindre 5 escales du tour du monde", "Reach 5 stops of the world tour"), "map", s.stops, 5),
        Badge("stops_25", tr("Tour du monde", "World tour"), tr("Boucler le tour du monde (25 escales)", "Complete the world tour (25 stops)"), "globe", s.stops, 25),
        Badge("challenge_1", tr("Défi relevé", "Challenge completed"), tr("Réussir un défi de la semaine", "Complete a weekly challenge"), "bolt", s.challenges, 1),
        Badge("challenge_10", tr("Aventurier", "Adventurer"), tr("Réussir 10 défis de la semaine", "Complete 10 weekly challenges"), "explore", s.challenges, 10),
    )
}
