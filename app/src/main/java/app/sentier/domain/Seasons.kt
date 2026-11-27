/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.domain

import java.time.LocalDate
import java.time.MonthDay
import java.time.temporal.ChronoUnit

/** Ce que mesure un défi saisonnier. */
enum class SeasonMetric { STEPS, ACTIVE_DAYS, HABITS, TASKS, SPORT_DAYS }

/**
 * Un défi saisonnier : il revient chaque année entre [start] et [end] (inclus, dans la même année).
 * Le réussir rapporte un bonus et un badge rare. [code] entre dans la clé de la récompense : il ne doit jamais changer.
 */
data class Season(
    val code: Int,
    val id: String,
    val title: String,
    val goalText: String,
    /** Clin d'œil culturel. */
    val flavor: String,
    val icon: String,
    val start: MonthDay,
    val end: MonthDay,
    val metric: SeasonMetric,
    val goal: Long,
    val xp: Int,
    val badgeTitle: String,
) {
    val gold: Int get() = GameRules.goldFor(xp)

    fun startIn(year: Int): LocalDate = start.atYear(year)
    fun endIn(year: Int): LocalDate = end.atYear(year)
    fun contains(day: LocalDate): Boolean = day in startIn(day.year)..endIn(day.year)
}

/** Ce qui a été fait pendant la période d'un défi saisonnier, jusqu'à aujourd'hui. */
data class SeasonActivity(
    val steps: Long = 0,
    val activeDays: Int = 0,
    val habits: Int = 0,
    val tasks: Int = 0,
    /** Jours avec une séance de sport cochée. */
    val sportDays: Int = 0,
    val hasSport: Boolean = false,
)

/** Le défi saisonnier en cours, avec sa progression. */
data class SeasonalChallenge(
    val season: Season,
    val year: Int,
    val current: Long,
    /** Jours restants après aujourd'hui (0 le dernier jour). */
    val daysLeft: Int,
    /** Bonus déjà versé. */
    val rewarded: Boolean = false,
    /** Conseil quand le défi ne peut pas encore avancer. */
    val hint: String? = null,
) {
    val done: Boolean get() = current >= season.goal
    val progress: Float get() = (current.toFloat() / season.goal).coerceIn(0f, 1f)
    val key: Long get() = Seasons.keyOf(year, season.code)
}

data class SeasonsState(
    val active: SeasonalChallenge? = null,
    /** Prochain défi saisonnier et sa date de début, quand aucun n'est en cours. */
    val next: Season? = null,
    val nextStart: LocalDate? = null,
    /** Nombre de fois où chaque défi a été réussi, par identifiant. */
    val wins: Map<String, Int> = emptyMap(),
)

/**
 * Les défis saisonniers, au fil de l'année : Nouvel An, Saint-Valentin, printemps, été, rentrée,
 * Halloween, novembre, l'Avent et les fêtes de fin d'année. Ils ne se chevauchent pas.
 */
object Seasons {
    val all: List<Season> = listOf(
        Season(
            0, "nouvel_an", "Bonnes résolutions", "Être actif 7 jours entre le 1er et le 10 janvier",
            "En Espagne, on avale douze grains de raisin aux douze coups de minuit, un pour chaque mois à venir.",
            "celebration", MonthDay.of(1, 1), MonthDay.of(1, 10), SeasonMetric.ACTIVE_DAYS, 7, xp = 100,
            badgeTitle = "Résolution tenue",
        ),
        Season(
            1, "saint_valentin", "Prendre soin de soi", "Réussir 15 habitudes du 7 au 14 février",
            "Au Japon, on offre du chocolat à la Saint-Valentin, et on le rend un mois plus tard, au White Day.",
            "heart", MonthDay.of(2, 7), MonthDay.of(2, 14), SeasonMetric.HABITS, 15, xp = 80,
            badgeTitle = "Cœur fidèle",
        ),
        Season(
            2, "printemps", "Éveil du printemps", "Marcher 100 000 pas du 20 mars au 5 avril",
            "Au Japon, le hanami réunit familles et amis pour pique-niquer sous les cerisiers en fleurs.",
            "flower", MonthDay.of(3, 20), MonthDay.of(4, 5), SeasonMetric.STEPS, 100_000, xp = 120,
            badgeTitle = "Fleur de cerisier",
        ),
        Season(
            3, "ete", "Le grand été", "Marcher 200 000 pas du 21 juin au 21 juillet",
            "Au solstice d'été, le soleil ne se couche pas au nord de la Norvège : c'est le soleil de minuit.",
            "sun", MonthDay.of(6, 21), MonthDay.of(7, 21), SeasonMetric.STEPS, 200_000, xp = 150,
            badgeTitle = "Soleil de minuit",
        ),
        Season(
            4, "vacances", "Vacances actives", "Faire du sport 10 jours en août",
            "Dans la Grèce antique, les Jeux olympiques avaient lieu en plein été, tous les quatre ans.",
            "beach", MonthDay.of(8, 1), MonthDay.of(8, 31), SeasonMetric.SPORT_DAYS, 10, xp = 120,
            badgeTitle = "Esprit d'été",
        ),
        Season(
            5, "automne", "Feuilles d'automne", "Terminer 15 tâches du 22 septembre au 6 octobre",
            "Au Québec, l'automne teinte les érables de rouge et d'or : c'est la saison des couleurs.",
            "leaf", MonthDay.of(9, 22), MonthDay.of(10, 6), SeasonMetric.TASKS, 15, xp = 100,
            badgeTitle = "Feuille d'érable",
        ),
        Season(
            6, "halloween", "La nuit des citrouilles", "Marcher 50 000 pas du 25 au 31 octobre",
            "Halloween vient de Samhain, une fête celte qui marquait le passage à la saison sombre.",
            "moon", MonthDay.of(10, 25), MonthDay.of(10, 31), SeasonMetric.STEPS, 50_000, xp = 90,
            badgeTitle = "Citrouille dorée",
        ),
        Season(
            7, "novembre", "Novembre en mouvement", "Faire du sport 8 jours en novembre",
            "En novembre, des hommes du monde entier se laissent pousser la moustache pour la santé : c'est Movember.",
            "sport", MonthDay.of(11, 1), MonthDay.of(11, 30), SeasonMetric.SPORT_DAYS, 8, xp = 100,
            badgeTitle = "Moustache d'or",
        ),
        Season(
            8, "avent", "Calendrier de l'Avent", "Être actif 20 jours du 1er au 24 décembre",
            "Le premier calendrier de l'Avent imprimé est né en Allemagne au début du XXe siècle.",
            "tree", MonthDay.of(12, 1), MonthDay.of(12, 24), SeasonMetric.ACTIVE_DAYS, 20, xp = 150,
            badgeTitle = "Étoile du sapin",
        ),
        Season(
            9, "fetes", "La trêve des confiseurs", "Marcher 40 000 pas du 25 au 31 décembre",
            "Entre Noël et le jour de l'An, la vie politique française fait une pause : c'est la trêve des confiseurs.",
            "snow", MonthDay.of(12, 25), MonthDay.of(12, 31), SeasonMetric.STEPS, 40_000, xp = 90,
            badgeTitle = "Flocon d'hiver",
        ),
    )

    /** Clé unique d'un défi saisonnier d'une année, pour ne verser le bonus qu'une fois. */
    fun keyOf(year: Int, code: Int): Long = year * 100L + code

    /** Défi d'une clé de récompense (null si elle ne correspond à aucun). */
    fun ofKey(key: Long): Season? = all.firstOrNull { it.code.toLong() == key % 100 }

    fun active(today: LocalDate): Season? = all.firstOrNull { it.contains(today) }

    /** Prochain défi qui commence après [today], éventuellement l'année suivante. */
    fun next(today: LocalDate): Pair<Season, LocalDate> =
        (all.map { it to it.startIn(today.year) } + all.map { it to it.startIn(today.year + 1) })
            .filter { it.second.isAfter(today) }
            .minBy { it.second }

    /** Le défi en cours avec sa progression, d'après ce qui a été fait depuis son début. */
    fun challenge(season: Season, today: LocalDate, a: SeasonActivity, rewarded: Boolean): SeasonalChallenge {
        val current = when (season.metric) {
            SeasonMetric.STEPS -> a.steps
            SeasonMetric.ACTIVE_DAYS -> a.activeDays.toLong()
            SeasonMetric.HABITS -> a.habits.toLong()
            SeasonMetric.TASKS -> a.tasks.toLong()
            SeasonMetric.SPORT_DAYS -> a.sportDays.toLong()
        }
        val hint = when {
            season.metric == SeasonMetric.STEPS && a.steps == 0L -> "Active le comptage des pas dans l'onglet Habitudes."
            season.metric == SeasonMetric.SPORT_DAYS && !a.hasSport -> "Ajoute l'habitude « Faire du sport » pour relever ce défi."
            else -> null
        }
        return SeasonalChallenge(
            season = season,
            year = today.year,
            current = current.coerceAtMost(season.goal),
            daysLeft = ChronoUnit.DAYS.between(today, season.endIn(today.year)).toInt(),
            rewarded = rewarded,
            hint = hint,
        )
    }

    /** Nombre de victoires par défi, d'après les clés des bonus versés. */
    fun winsOf(keys: List<Long>): Map<String, Int> =
        keys.mapNotNull { ofKey(it) }.groupingBy { it.id }.eachCount()
}

/** Les badges rares, un par défi saisonnier, gagnés en le réussissant (une fois par an au plus). */
object RareBadges {
    fun of(wins: Map<String, Int>): List<Badge> = Seasons.all.map { s ->
        Badge(
            id = "saison_${s.id}",
            title = s.badgeTitle,
            description = "Réussir le défi « ${s.title} »",
            icon = s.icon,
            current = wins[s.id] ?: 0,
            goal = 1,
            rare = true,
        )
    }
}
