/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.domain

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
)

/**
 * Série de jours avec des jours de repos, pour qu'un jour raté n'efface pas des semaines d'efforts.
 * On commence avec 1 jour de repos, on en gagne 1 tous les 7 jours actifs (2 au plus).
 * Un jour sans action consomme un jour de repos ; sans réserve, la série repart de zéro.
 * Aujourd'hui ne compte pas comme raté tant que la journée n'est pas finie.
 */
object Streaks {
    const val MAX_REST_DAYS = 2
    const val DAYS_PER_REST_DAY = 7

    fun compute(activeDays: Set<Long>, today: Long): StreakInfo {
        val first = activeDays.filter { it <= today }.minOrNull() ?: return StreakInfo(restDays = 1)
        var current = 0
        var best = 0
        var rest = 1
        var sinceEarned = 0
        for (day in first..today) {
            if (day in activeDays) {
                current++
                sinceEarned++
                if (sinceEarned == DAYS_PER_REST_DAY) {
                    rest = (rest + 1).coerceAtMost(MAX_REST_DAYS)
                    sinceEarned = 0
                }
            } else if (day != today) {
                if (current > 0 && rest > 0) {
                    rest--
                } else {
                    current = 0
                    sinceEarned = 0
                }
            }
            best = maxOf(best, current)
        }
        return StreakInfo(current, best, rest, activeToday = today in activeDays)
    }
}

/** Compteurs utilisés pour les badges. */
data class GameStats(
    val tasksDone: Int = 0,
    val habitsDone: Int = 0,
    val stepGoals: Int = 0,
    val rewardsTaken: Int = 0,
    val level: Int = 1,
    val bestStreak: Int = 0,
    /** Escales du tour du monde atteintes. */
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
) {
    val unlocked: Boolean get() = current >= goal
}

object Badges {
    fun of(s: GameStats): List<Badge> = listOf(
        Badge("first_task", "Premier pas", "Finir une première tâche", "flag", s.tasksDone, 1),
        Badge("tasks_10", "Bien lancé", "Finir 10 tâches", "task", s.tasksDone, 10),
        Badge("tasks_100", "Infatigable", "Finir 100 tâches", "rocket", s.tasksDone, 100),
        Badge("habits_30", "Rituel", "Réussir 30 fois une habitude", "spa", s.habitsDone, 30),
        Badge("steps_7", "Marcheur", "Atteindre 7 fois l'objectif de pas", "walk", s.stepGoals, 7),
        Badge("streak_7", "Une semaine", "Tenir une série de 7 jours", "fire", s.bestStreak, 7),
        Badge("streak_30", "Un mois", "Tenir une série de 30 jours", "calendar", s.bestStreak, 30),
        Badge("level_5", "Niveau 5", "Atteindre le niveau 5", "star", s.level, 5),
        Badge("level_10", "Niveau 10", "Atteindre le niveau 10", "trophy", s.level, 10),
        Badge("reward_1", "Je me fais plaisir", "Prendre une récompense dans la boutique", "gift", s.rewardsTaken, 1),
        Badge("stops_5", "En route", "Atteindre 5 escales du tour du monde", "map", s.stops, 5),
        Badge("stops_25", "Tour du monde", "Boucler le tour du monde (25 escales)", "globe", s.stops, 25),
        Badge("challenge_1", "Défi relevé", "Réussir un défi de la semaine", "bolt", s.challenges, 1),
        Badge("challenge_10", "Aventurier", "Réussir 10 défis de la semaine", "explore", s.challenges, 10),
    )
}
