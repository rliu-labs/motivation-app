/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.domain

/** Rareté d'un trésor : plus il est rare, plus il est long à obtenir. */
enum class Rarity(val label: String) {
    RARE("Rare"),
    EPIC("Épique"),
    LEGENDARY("Légendaire"),
}

/** Ce qui fait gagner un trésor. */
enum class TreasureSource { LEVEL, STREAK, BADGES, CHALLENGES }

/**
 * Un objet rare d'une culture du monde, gagné avec les niveaux, les séries, les badges ou les défis.
 * Il rejoint les souvenirs du voyage dans le carnet. [icon] est un nom d'icône (voir l'interface).
 */
data class Treasure(
    val id: String,
    val name: String,
    /** Pays ou civilisation d'origine. */
    val origin: String,
    val description: String,
    val anecdote: String,
    val icon: String,
    val rarity: Rarity,
    val source: TreasureSource,
    val goal: Int,
    val current: Int = 0,
) {
    val owned: Boolean get() = current >= goal

    /** Comment l'obtenir, en une phrase. */
    val howTo: String
        get() = when (source) {
            TreasureSource.LEVEL -> "Atteindre le niveau $goal"
            TreasureSource.STREAK -> "Tenir une série de $goal jours"
            TreasureSource.BADGES -> if (goal >= Badges.of(GameStats()).size) "Obtenir tous les badges" else "Obtenir $goal badges"
            TreasureSource.CHALLENGES -> if (goal == 1) "Réussir un premier défi" else "Réussir $goal défis"
        }
}

object Treasures {
    private val all: List<Treasure> = listOf(
        // Les niveaux.
        Treasure(
            "compass", "Boussole de marin", "Portugal",
            "Elle guidait les caravelles des Grandes Découvertes le long des côtes d'Afrique, puis jusqu'aux Indes.",
            "La boussole est née en Chine, où elle servait d'abord à orienter les maisons et les tombeaux, bien avant les bateaux.",
            "compass", Rarity.RARE, TreasureSource.LEVEL, 3,
        ),
        Treasure(
            "astrolabe", "Astrolabe", "Al-Andalus (Espagne)",
            "Disque de laiton gravé qui donne l'heure et la position des étoiles. Cordoue et Tolède en fabriquaient de superbes.",
            "Il servait aussi à trouver la direction de La Mecque et à fixer les heures de prière.",
            "stars", Rarity.RARE, TreasureSource.LEVEL, 5,
        ),
        Treasure(
            "jade_seal", "Sceau de jade", "Chine",
            "Le jade, « pierre de l'immortalité », était réservé aux sceaux des empereurs.",
            "Le sceau impérial de l'Héritage du royaume aurait été taillé dans un jade légendaire. Il a disparu il y a plus de mille ans.",
            "seal", Rarity.EPIC, TreasureSource.LEVEL, 10,
        ),
        Treasure(
            "sun_stone", "Pierre du Soleil", "Empire aztèque (Mexique)",
            "Disque de basalte de 3,60 m et 24 tonnes, sculpté vers 1500, avec le dieu soleil en son centre.",
            "Enterrée après la conquête espagnole, elle a été retrouvée en 1790 pendant des travaux sur la grande place de Mexico.",
            "sun", Rarity.EPIC, TreasureSource.LEVEL, 15,
        ),
        Treasure(
            "laurel", "Couronne de laurier", "Grèce antique",
            "Aux Jeux pythiques de Delphes, les vainqueurs recevaient une couronne de laurier, l'arbre d'Apollon.",
            "À Olympie, les champions n'avaient pas de laurier : on les couronnait d'olivier sauvage.",
            "laurel", Rarity.LEGENDARY, TreasureSource.LEVEL, 20,
        ),
        // Les séries.
        Treasure(
            "raku", "Bol raku", "Japon",
            "Façonné à la main et cuit très vite, chaque bol est unique : il célèbre la beauté de l'imperfection, le wabi-sabi.",
            "À Kyoto, la famille Raku fabrique ces bols pour la cérémonie du thé depuis le XVIe siècle.",
            "bowl", Rarity.RARE, TreasureSource.STREAK, 3,
        ),
        Treasure(
            "matryoshka", "Matriochka", "Russie",
            "Poupées de bois peintes qui s'emboîtent les unes dans les autres, de la plus grande à la plus petite.",
            "La première matriochka date des années 1890. Elle s'inspirait d'une poupée gigogne japonaise.",
            "doll", Rarity.RARE, TreasureSource.STREAK, 7,
        ),
        Treasure(
            "murano", "Verre de Murano", "Venise (Italie)",
            "Les verriers soufflent le verre sur l'île de Murano depuis 1291, quand Venise les y a installés.",
            "Pour garder leurs secrets, les maîtres verriers n'avaient pas le droit de quitter la République de Venise.",
            "glass", Rarity.EPIC, TreasureSource.STREAK, 14,
        ),
        Treasure(
            "quetzal", "Plume de quetzal", "Civilisation maya",
            "Ses longues plumes vertes ornaient les coiffes des souverains mayas et aztèques. Elles valaient plus que l'or.",
            "Le quetzal est l'oiseau national du Guatemala, et la monnaie du pays porte son nom.",
            "bird", Rarity.EPIC, TreasureSource.STREAK, 30,
        ),
        Treasure(
            "rosetta", "Pierre de Rosette", "Égypte ancienne",
            "Le même décret gravé en hiéroglyphes, en démotique et en grec : la clé pour relire l'Égypte des pharaons.",
            "Champollion l'a déchiffrée en 1822. On raconte qu'il a couru voir son frère en criant « Je tiens l'affaire ! ».",
            "scroll", Rarity.LEGENDARY, TreasureSource.STREAK, 100,
        ),
        // Les badges.
        Treasure(
            "mercator", "Mappemonde de Mercator", "Flandre",
            "En 1569, Mercator dessine une carte où les caps restent des lignes droites : le rêve des navigateurs.",
            "Sur sa carte, le Groenland paraît aussi grand que l'Afrique, qui est pourtant 14 fois plus vaste.",
            "map", Rarity.RARE, TreasureSource.BADGES, 3,
        ),
        Treasure(
            "djembe", "Djembé", "Afrique de l'Ouest",
            "Taillé dans un seul tronc et tendu d'une peau de chèvre, il rythme les fêtes et les récits des griots.",
            "Son nom viendrait d'une expression bambara qui invite tout le monde à se rassembler dans la paix.",
            "drum", Rarity.EPIC, TreasureSource.BADGES, 6,
        ),
        Treasure(
            "faberge", "Œuf de Fabergé", "Russie impériale",
            "Joyau d'or et d'émail offert à Pâques par le tsar, avec une surprise cachée à l'intérieur.",
            "Un œuf impérial qu'on croyait perdu a été retrouvé en 2014 chez un ferrailleur américain, qui voulait le fondre.",
            "egg", Rarity.LEGENDARY, TreasureSource.BADGES, 10,
        ),
        Treasure(
            "lamp", "Lampe merveilleuse", "Les Mille et Une Nuits",
            "La lampe à huile de cuivre des contes, qui exauce les vœux de celui qui la frotte.",
            "Aladdin n'est dans aucun manuscrit arabe ancien : Antoine Galland l'a ajouté vers 1710, d'après le récit d'un conteur d'Alep.",
            "lamp", Rarity.LEGENDARY, TreasureSource.BADGES, Badges.of(GameStats()).size,
        ),
        // Les défis.
        Treasure(
            "panama", "Chapeau panama", "Équateur",
            "Tressé à la main en paille de toquilla. Les plus fins demandent des mois de travail.",
            "Il est équatorien, mais il doit son nom aux ouvriers du canal de Panama, qui le portaient.",
            "hat", Rarity.RARE, TreasureSource.CHALLENGES, 1,
        ),
        Treasure(
            "rainstick", "Bâton de pluie", "Chili",
            "Un cactus séché, hérissé de ses épines et rempli de petits cailloux : retourné, il imite le bruit de la pluie.",
            "On l'associe aux peuples du désert d'Atacama, l'un des endroits les plus secs de la planète.",
            "rain", Rarity.EPIC, TreasureSource.CHALLENGES, 5,
        ),
        Treasure(
            "moai", "Moaï", "Île de Pâques",
            "Les géants de pierre de Rapa Nui représentent les ancêtres qui veillent sur les villages.",
            "Presque tous tournent le dos à l'océan : ils regardent vers l'intérieur de l'île, vers ceux qu'ils protègent.",
            "moai", Rarity.LEGENDARY, TreasureSource.CHALLENGES, 15,
        ),
    )

    val count: Int get() = all.size

    /** Tous les trésors, avec leur avancement. */
    fun of(stats: GameStats, badgesUnlocked: Int): List<Treasure> = all.map { t ->
        t.copy(
            current = when (t.source) {
                TreasureSource.LEVEL -> stats.level
                TreasureSource.STREAK -> stats.bestStreak
                TreasureSource.BADGES -> badgesUnlocked
                TreasureSource.CHALLENGES -> stats.challenges
            }
        )
    }
}
