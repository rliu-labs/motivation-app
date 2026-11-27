/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.domain

/** Rareté d'un trésor : plus il est rare, plus il est long à obtenir. */
enum class Rarity(val label: String) {
    RARE("Rare"),
    EPIC(tr("Épique", "Epic")),
    LEGENDARY(tr("Légendaire", "Legendary")),
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
            TreasureSource.LEVEL -> tr("Atteindre le niveau $goal", "Reach level $goal")
            TreasureSource.STREAK -> tr("Tenir une série de $goal jours", "Keep a $goal-day streak")
            TreasureSource.BADGES -> if (goal >= Badges.of(GameStats()).size) tr("Obtenir tous les badges", "Earn every badge") else tr("Obtenir $goal badges", "Earn $goal badges")
            TreasureSource.CHALLENGES -> if (goal == 1) tr("Réussir un premier défi", "Complete a first challenge") else tr("Réussir $goal défis", "Complete $goal challenges")
        }
}

object Treasures {
    private val all: List<Treasure> = listOf(
        // Les niveaux.
        Treasure(
            "compass", tr("Boussole de marin", "Sailor's compass"), "Portugal",
            tr("Elle guidait les caravelles des Grandes Découvertes le long des côtes d'Afrique, puis jusqu'aux Indes.", "It guided the caravels of the Age of Discovery along the coasts of Africa, then all the way to the Indies."),
            tr("La boussole est née en Chine, où elle servait d'abord à orienter les maisons et les tombeaux, bien avant les bateaux.", "The compass was born in China, where it was first used to orient houses and tombs, long before ships."),
            "compass", Rarity.RARE, TreasureSource.LEVEL, 3,
        ),
        Treasure(
            "astrolabe", "Astrolabe", tr("Al-Andalus (Espagne)", "Al-Andalus (Spain)"),
            tr("Disque de laiton gravé qui donne l'heure et la position des étoiles. Cordoue et Tolède en fabriquaient de superbes.", "An engraved brass disc that tells the time and the position of the stars. Córdoba and Toledo made superb ones."),
            tr("Il servait aussi à trouver la direction de La Mecque et à fixer les heures de prière.", "It was also used to find the direction of Mecca and set the hours of prayer."),
            "stars", Rarity.RARE, TreasureSource.LEVEL, 5,
        ),
        Treasure(
            "jade_seal", tr("Sceau de jade", "Jade seal"), tr("Chine", "China"),
            tr("Le jade, « pierre de l'immortalité », était réservé aux sceaux des empereurs.", "Jade, the “stone of immortality”, was reserved for the emperors' seals."),
            tr("Le sceau impérial de l'Héritage du royaume aurait été taillé dans un jade légendaire. Il a disparu il y a plus de mille ans.", "The Heirloom Seal of the Realm was said to be carved from a legendary jade. It vanished more than a thousand years ago."),
            "seal", Rarity.EPIC, TreasureSource.LEVEL, 10,
        ),
        Treasure(
            "sun_stone", tr("Pierre du Soleil", "Sun Stone"), tr("Empire aztèque (Mexique)", "Aztec Empire (Mexico)"),
            tr("Disque de basalte de 3,60 m et 24 tonnes, sculpté vers 1500, avec le dieu soleil en son centre.", "A basalt disc 3.6 m wide and weighing 24 tonnes, carved around 1500, with the sun god at its centre."),
            tr("Enterrée après la conquête espagnole, elle a été retrouvée en 1790 pendant des travaux sur la grande place de Mexico.", "Buried after the Spanish conquest, it was found in 1790 during works on Mexico City's main square."),
            "sun", Rarity.EPIC, TreasureSource.LEVEL, 15,
        ),
        Treasure(
            "laurel", tr("Couronne de laurier", "Laurel wreath"), tr("Grèce antique", "Ancient Greece"),
            tr("Aux Jeux pythiques de Delphes, les vainqueurs recevaient une couronne de laurier, l'arbre d'Apollon.", "At the Pythian Games of Delphi, winners received a wreath of laurel, Apollo's tree."),
            tr("À Olympie, les champions n'avaient pas de laurier : on les couronnait d'olivier sauvage.", "At Olympia, champions got no laurel: they were crowned with wild olive."),
            "laurel", Rarity.LEGENDARY, TreasureSource.LEVEL, 20,
        ),
        // Les séries.
        Treasure(
            "raku", tr("Bol raku", "Raku bowl"), tr("Japon", "Japan"),
            tr("Façonné à la main et cuit très vite, chaque bol est unique : il célèbre la beauté de l'imperfection, le wabi-sabi.", "Shaped by hand and fired very quickly, each bowl is unique: it celebrates the beauty of imperfection, wabi-sabi."),
            tr("À Kyoto, la famille Raku fabrique ces bols pour la cérémonie du thé depuis le XVIe siècle.", "In Kyoto, the Raku family has made these bowls for the tea ceremony since the 16th century."),
            "bowl", Rarity.RARE, TreasureSource.STREAK, 3,
        ),
        Treasure(
            "matryoshka", tr("Matriochka", "Matryoshka"), tr("Russie", "Russia"),
            tr("Poupées de bois peintes qui s'emboîtent les unes dans les autres, de la plus grande à la plus petite.", "Painted wooden dolls that nest inside one another, from the largest to the smallest."),
            tr("La première matriochka date des années 1890. Elle s'inspirait d'une poupée gigogne japonaise.", "The first matryoshka dates from the 1890s. It was inspired by a Japanese nesting doll."),
            "doll", Rarity.RARE, TreasureSource.STREAK, 7,
        ),
        Treasure(
            "murano", tr("Verre de Murano", "Murano glass"), tr("Venise (Italie)", "Venice (Italy)"),
            tr("Les verriers soufflent le verre sur l'île de Murano depuis 1291, quand Venise les y a installés.", "Glassmakers have blown glass on the island of Murano since 1291, when Venice moved them there."),
            tr("Pour garder leurs secrets, les maîtres verriers n'avaient pas le droit de quitter la République de Venise.", "To keep their secrets, master glassmakers were not allowed to leave the Republic of Venice."),
            "glass", Rarity.EPIC, TreasureSource.STREAK, 14,
        ),
        Treasure(
            "quetzal", tr("Plume de quetzal", "Quetzal feather"), tr("Civilisation maya", "Maya civilization"),
            tr("Ses longues plumes vertes ornaient les coiffes des souverains mayas et aztèques. Elles valaient plus que l'or.", "Its long green feathers adorned the headdresses of Maya and Aztec rulers. They were worth more than gold."),
            tr("Le quetzal est l'oiseau national du Guatemala, et la monnaie du pays porte son nom.", "The quetzal is Guatemala's national bird, and the country's currency bears its name."),
            "bird", Rarity.EPIC, TreasureSource.STREAK, 30,
        ),
        Treasure(
            "rosetta", tr("Pierre de Rosette", "Rosetta Stone"), tr("Égypte ancienne", "Ancient Egypt"),
            tr("Le même décret gravé en hiéroglyphes, en démotique et en grec : la clé pour relire l'Égypte des pharaons.", "The same decree carved in hieroglyphs, Demotic and Greek: the key to reading the Egypt of the pharaohs again."),
            tr("Champollion l'a déchiffrée en 1822. On raconte qu'il a couru voir son frère en criant « Je tiens l'affaire ! ».", "Champollion deciphered it in 1822. He is said to have run to his brother shouting “I've got it!”."),
            "scroll", Rarity.LEGENDARY, TreasureSource.STREAK, 100,
        ),
        // Les badges.
        Treasure(
            "mercator", tr("Mappemonde de Mercator", "Mercator's world map"), tr("Flandre", "Flanders"),
            tr("En 1569, Mercator dessine une carte où les caps restent des lignes droites : le rêve des navigateurs.", "In 1569, Mercator drew a map where compass bearings stay straight lines: a navigator's dream."),
            tr("Sur sa carte, le Groenland paraît aussi grand que l'Afrique, qui est pourtant 14 fois plus vaste.", "On his map, Greenland looks as big as Africa, which is in fact 14 times larger."),
            "map", Rarity.RARE, TreasureSource.BADGES, 3,
        ),
        Treasure(
            "djembe", tr("Djembé", "Djembe"), tr("Afrique de l'Ouest", "West Africa"),
            tr("Taillé dans un seul tronc et tendu d'une peau de chèvre, il rythme les fêtes et les récits des griots.", "Carved from a single trunk and covered with goatskin, it sets the rhythm of celebrations and griots' tales."),
            tr("Son nom viendrait d'une expression bambara qui invite tout le monde à se rassembler dans la paix.", "Its name is said to come from a Bambara phrase inviting everyone to gather in peace."),
            "drum", Rarity.EPIC, TreasureSource.BADGES, 6,
        ),
        Treasure(
            "faberge", tr("Œuf de Fabergé", "Fabergé egg"), tr("Russie impériale", "Imperial Russia"),
            tr("Joyau d'or et d'émail offert à Pâques par le tsar, avec une surprise cachée à l'intérieur.", "A jewel of gold and enamel given by the tsar at Easter, with a surprise hidden inside."),
            tr("Un œuf impérial qu'on croyait perdu a été retrouvé en 2014 chez un ferrailleur américain, qui voulait le fondre.", "An imperial egg thought lost was found in 2014 at an American scrap dealer's, who wanted to melt it down."),
            "egg", Rarity.LEGENDARY, TreasureSource.BADGES, 10,
        ),
        Treasure(
            "lamp", tr("Lampe merveilleuse", "Magic lamp"), tr("Les Mille et Une Nuits", "One Thousand and One Nights"),
            tr("La lampe à huile de cuivre des contes, qui exauce les vœux de celui qui la frotte.", "The copper oil lamp of the tales, which grants the wishes of whoever rubs it."),
            tr("Aladdin n'est dans aucun manuscrit arabe ancien : Antoine Galland l'a ajouté vers 1710, d'après le récit d'un conteur d'Alep.", "Aladdin is in no old Arabic manuscript: Antoine Galland added it around 1710, from the tale of a storyteller from Aleppo."),
            "lamp", Rarity.LEGENDARY, TreasureSource.BADGES, Badges.of(GameStats()).size,
        ),
        // Les défis.
        Treasure(
            "panama", tr("Chapeau panama", "Panama hat"), tr("Équateur", "Ecuador"),
            tr("Tressé à la main en paille de toquilla. Les plus fins demandent des mois de travail.", "Hand-woven from toquilla straw. The finest take months of work."),
            tr("Il est équatorien, mais il doit son nom aux ouvriers du canal de Panama, qui le portaient.", "It is Ecuadorian, but owes its name to the Panama Canal workers who wore it."),
            "hat", Rarity.RARE, TreasureSource.CHALLENGES, 1,
        ),
        Treasure(
            "rainstick", tr("Bâton de pluie", "Rain stick"), tr("Chili", "Chile"),
            tr("Un cactus séché, hérissé de ses épines et rempli de petits cailloux : retourné, il imite le bruit de la pluie.", "A dried cactus, studded with its spines and filled with pebbles: turned over, it mimics the sound of rain."),
            tr("On l'associe aux peuples du désert d'Atacama, l'un des endroits les plus secs de la planète.", "It is linked to the peoples of the Atacama Desert, one of the driest places on Earth."),
            "rain", Rarity.EPIC, TreasureSource.CHALLENGES, 5,
        ),
        Treasure(
            "moai", tr("Moaï", "Moai"), tr("Île de Pâques", "Easter Island"),
            tr("Les géants de pierre de Rapa Nui représentent les ancêtres qui veillent sur les villages.", "The stone giants of Rapa Nui represent the ancestors who watch over the villages."),
            tr("Presque tous tournent le dos à l'océan : ils regardent vers l'intérieur de l'île, vers ceux qu'ils protègent.", "Almost all turn their backs to the ocean: they look inland, towards those they protect."),
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
