/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.domain

/** Un objet typique rapporté d'une escale. [icon] est un nom d'icône (voir l'interface). */
data class Souvenir(val name: String, val description: String, val icon: String)

/**
 * Une escale du tour du monde, atteinte après [km] kilomètres de marche depuis le départ.
 * Les kilomètres sont ceux de la marche, pas ceux de la carte : le tour fait environ 700 km à pied.
 */
data class Stop(
    val city: String,
    val country: String,
    val km: Double,
    val lat: Double,
    val lon: Double,
    val architecture: String,
    val culture: String,
    val anecdote: String,
    val souvenir: Souvenir,
)

/** Où l'on en est du tour du monde. */
data class TourProgress(val km: Double) {
    val stops: List<Stop> get() = WorldTour.stops
    /** Index de la dernière escale atteinte (0 = départ). */
    val currentIndex: Int get() = stops.indexOfLast { it.km <= km }.coerceAtLeast(0)
    val current: Stop get() = stops[currentIndex]
    val nextStop: Stop? get() = stops.getOrNull(currentIndex + 1)
    val kmToNext: Double get() = nextStop?.let { it.km - km } ?: 0.0
    val done: Boolean get() = km >= WorldTour.lengthKm
    val fraction: Float get() = (km / WorldTour.lengthKm).toFloat().coerceIn(0f, 1f)
    /** Escales atteintes, départ compris. */
    val reached: List<Stop> get() = stops.take(currentIndex + 1)
}

/** Une escale atteinte (hors départ), avec sa clé unique pour la récompense. */
data class ReachedStop(val index: Int, val stop: Stop) {
    val key: Long get() = WorldTour.keyOf(index)
}

/** Le tour du monde, vers l'est, de Paris à Paris. */
object WorldTour {
    /** XP d'une escale atteinte ; l'or suit la règle habituelle (un cinquième). */
    const val STOP_XP = 15

    val stops: List<Stop> = listOf(
        Stop(
            "Paris", "France", 0.0, 48.86, 2.35,
            "Les immeubles haussmanniens en pierre de taille et la tour Eiffel, construite pour l'Exposition universelle de 1889.",
            "Musées, cafés et gastronomie : le repas gastronomique des Français est inscrit au patrimoine immatériel de l'UNESCO.",
            "La tour Eiffel devait être démontée au bout de 20 ans. Ses antennes de radio l'ont sauvée.",
            Souvenir("Passeport de voyageur", "Le carnet qui recevra le tampon de chaque escale.", "passport"),
        ),
        Stop(
            "Marrakech", "Maroc", 8.0, 31.63, -7.99,
            "Le minaret de la Koutoubia, du XIIe siècle, et les zelliges, mosaïques de petits carreaux d'argile émaillée taillés à la main.",
            "Sur la place Jemaa el-Fna, conteurs et musiciens perpétuent une tradition inscrite au patrimoine immatériel de l'UNESCO.",
            "Le thé à la menthe se verse de haut pour former une mousse légère dans le verre.",
            Souvenir("Babouches", "Chaussons en cuir aux bouts pointus, cousus dans les souks.", "shoe"),
        ),
        Stop(
            "Rome", "Italie", 20.0, 41.90, 12.50,
            "Le Panthéon et sa coupole, toujours la plus grande coupole en béton non armé du monde, près de 1 900 ans après sa construction.",
            "L'espresso se boit au comptoir, debout, en quelques gorgées.",
            "Les pièces lancées dans la fontaine de Trevi, plus d'un million d'euros par an, sont reversées à une œuvre de charité.",
            Souvenir("Cafetière moka", "La cafetière à huit pans inventée en Italie en 1933.", "coffee"),
        ),
        Stop(
            "Athènes", "Grèce", 35.0, 37.97, 23.73,
            "Le Parthénon, temple de marbre de style dorique achevé au Ve siècle avant notre ère sur l'Acropole.",
            "Le théâtre y est né : tragédies et comédies se jouaient en plein air devant des milliers de spectateurs.",
            "Les colonnes du Parthénon sont légèrement bombées pour paraître parfaitement droites à l'œil.",
            Souvenir("Komboloï", "Chapelet de perles qu'on fait glisser entre ses doigts pour se détendre.", "jewel"),
        ),
        Stop(
            "Istanbul", "Turquie", 53.0, 41.01, 28.98,
            "Sainte-Sophie, bâtie au VIe siècle, dont la coupole semble flotter au-dessus d'une couronne de fenêtres.",
            "Le Grand Bazar, l'un des plus anciens marchés couverts du monde, abrite des milliers de boutiques.",
            "La ville est à cheval sur deux continents : le Bosphore sépare sa rive européenne de sa rive asiatique.",
            Souvenir("Œil nazar", "Perle de verre bleue censée protéger du mauvais œil.", "eye"),
        ),
        Stop(
            "Pétra", "Jordanie", 73.0, 30.33, 35.44,
            "La Khazneh, façade monumentale sculptée directement dans la falaise de grès rose par les Nabatéens.",
            "Les Bédouins accueillent les voyageurs avec un café parfumé à la cardamome.",
            "On entre dans la cité par le Siq, un canyon étroit d'environ 1,2 km.",
            Souvenir("Bouteille de sable", "Dunes et chameaux dessinés grain par grain avec du sable coloré.", "sand"),
        ),
        Stop(
            "Le Caire", "Égypte", 95.0, 29.98, 31.13,
            "La grande pyramide de Gizeh, seule des sept merveilles du monde antique encore debout.",
            "Le Nil rythme la vie depuis l'Antiquité ; les felouques, voiliers traditionnels, y naviguent toujours.",
            "La grande pyramide est restée la construction la plus haute du monde pendant près de 4 000 ans.",
            Souvenir("Scarabée", "Amulette porte-bonheur, symbole du soleil levant chez les anciens Égyptiens.", "jewel"),
        ),
        Stop(
            "Lalibela", "Éthiopie", 120.0, 12.03, 39.04,
            "Onze églises creusées dans la roche, dont l'église Saint-Georges, taillée d'un seul bloc en forme de croix.",
            "La cérémonie du café, servie en trois tournées, est un grand moment d'hospitalité.",
            "L'Éthiopie suit son propre calendrier, qui compte treize mois.",
            Souvenir("Jebena", "Cafetière en terre cuite au long col, utilisée pour la cérémonie du café.", "pottery"),
        ),
        Stop(
            "Ispahan", "Iran", 148.0, 32.66, 51.68,
            "La place Naghsh-e Jahan, bordée de mosquées couvertes de céramiques bleues et turquoise.",
            "La poésie fait partie du quotidien : on cite volontiers Hafez et Saadi.",
            "Dans la mosquée du cheikh Lotfollah, la coupole change de couleur avec la lumière du jour.",
            Souvenir("Tapis persan", "Noué à la main, il peut compter plus d'un million de nœuds au mètre carré.", "textile"),
        ),
        Stop(
            "Agra", "Inde", 173.0, 27.18, 78.04,
            "Le Taj Mahal, mausolée de marbre blanc incrusté de pierres, élevé au XVIIe siècle par l'empereur Shah Jahan pour son épouse.",
            "Le chai, thé au lait et aux épices, se boit à toute heure dans de petits gobelets.",
            "Le marbre du Taj Mahal change de teinte au fil de la journée, du rose de l'aube au doré du soir.",
            Souvenir("Boîte à épices", "Le masala dabba, boîte ronde qui garde les épices du quotidien à portée de main.", "spice"),
        ),
        Stop(
            "Katmandou", "Népal", 198.0, 27.72, 85.36,
            "Le stupa de Boudhanath, immense dôme blanc surmonté des yeux de Bouddha tournés vers les quatre directions.",
            "Les drapeaux de prière flottent partout : on dit que le vent emporte les prières.",
            "Le drapeau du Népal est le seul drapeau national qui n'est pas rectangulaire.",
            Souvenir("Drapeaux de prière", "Cinq couleurs pour cinq éléments : ciel, air, feu, eau et terre.", "flag"),
        ),
        Stop(
            "Pékin", "Chine", 226.0, 39.92, 116.39,
            "La Cité interdite, palais impérial aux toits de tuiles jaunes, couleur réservée à l'empereur.",
            "La calligraphie, art du pinceau et de l'encre, et la dégustation du thé en petites tasses.",
            "Contrairement à une idée reçue, la Grande Muraille ne se voit pas à l'œil nu depuis l'espace.",
            Souvenir("Théière de Yixing", "Petite théière en argile non émaillée qui garde le goût des thés au fil des infusions.", "tea"),
        ),
        Stop(
            "Séoul", "Corée du Sud", 254.0, 37.58, 126.98,
            "Les hanok, maisons de bois aux toits incurvés, chauffées par le sol grâce à l'ondol.",
            "Le hangeul, l'alphabet coréen, a été créé au XVe siècle par le roi Sejong pour que chacun puisse apprendre à lire.",
            "Préparer le kimchi en famille à l'automne, le kimjang, est inscrit au patrimoine immatériel de l'UNESCO.",
            Souvenir("Masque de Hahoe", "Masque de bois souriant du théâtre traditionnel coréen.", "mask"),
        ),
        Stop(
            "Kyoto", "Japon", 284.0, 34.97, 135.77,
            "Le sanctuaire Fushimi Inari et ses milliers de torii vermillon qui forment des tunnels sur la montagne.",
            "La cérémonie du thé, l'art de préparer le matcha avec des gestes précis et lents.",
            "Kyoto compte environ 1 600 temples bouddhistes.",
            Souvenir("Daruma", "Figurine porte-bonheur : on lui peint un œil en se fixant un but, l'autre quand on l'atteint.", "toy"),
        ),
        Stop(
            "Hội An", "Viêt Nam", 314.0, 15.88, 108.33,
            "La vieille ville marchande et son pont couvert japonais, construit à la fin du XVIe siècle.",
            "Les soirs de pleine lune, la ville éteint ses lumières et s'éclaire aux lanternes.",
            "Les tailleurs du marché confectionnent des vêtements sur mesure en une journée.",
            Souvenir("Lanterne de soie", "Lanterne colorée en soie tendue sur une armature de bambou.", "light"),
        ),
        Stop(
            "Angkor", "Cambodge", 344.0, 13.41, 103.87,
            "Angkor Vat, temple du XIIe siècle, est le plus grand monument religieux du monde.",
            "Le ballet royal khmer, aux gestes de mains très codifiés, est inscrit au patrimoine immatériel de l'UNESCO.",
            "Angkor Vat figure sur le drapeau du Cambodge.",
            Souvenir("Krama", "Foulard à carreaux en coton, porté par presque tous les Cambodgiens.", "textile"),
        ),
        Stop(
            "Bali", "Indonésie", 376.0, -8.51, 115.26,
            "Des temples aux portes fendues et des rizières en terrasses irriguées par le subak, un système inscrit à l'UNESCO.",
            "Chaque matin, de petites offrandes de fleurs et de riz, les canang sari, sont posées devant les maisons.",
            "Le jour de Nyepi, le Nouvel An balinais, toute l'île se tait : pas de lumière, pas de travail, même l'aéroport ferme.",
            Souvenir("Masque balinais", "Masque sculpté et peint des danses sacrées, comme celle du Barong.", "mask"),
        ),
        Stop(
            "Sydney", "Australie", 411.0, -33.86, 151.21,
            "L'Opéra de Sydney, dessiné par Jørn Utzon, et ses toits en forme de voiles.",
            "Les peuples aborigènes vivent ici depuis plus de 50 000 ans ; leur art du point raconte des histoires et des chemins.",
            "Les toits de l'Opéra sont couverts de plus d'un million de tuiles.",
            Souvenir("Boomerang", "Outil de chasse aborigène ; certains modèles reviennent vers le lanceur.", "toy"),
        ),
        Stop(
            "Rotorua", "Nouvelle-Zélande", 446.0, -38.14, 176.25,
            "Les wharenui, maisons de réunion maories, sculptées et peintes pour raconter l'histoire de la tribu.",
            "Le haka, danse rituelle maorie, est aussi exécuté par les All Blacks avant leurs matchs.",
            "La ville est posée sur une zone géothermique : geysers et mares de boue bouillonnante.",
            Souvenir("Pendentif en pounamu", "Jade vert sculpté, souvent offert en signe d'amitié.", "jewel"),
        ),
        Stop(
            "Cusco", "Pérou", 486.0, -13.53, -71.97,
            "Les murs incas, faits de blocs de pierre ajustés sans mortier avec une précision stupéfiante.",
            "Le quechua, la langue des Incas, est encore parlé par des millions de personnes.",
            "Le Machu Picchu, à quelques heures de là, n'a été révélé au monde qu'en 1911.",
            Souvenir("Poncho en alpaga", "Tissé à la main avec la laine douce des alpagas des Andes.", "textile"),
        ),
        Stop(
            "Rio de Janeiro", "Brésil", 521.0, -22.95, -43.21,
            "Le Christ Rédempteur, statue Art déco de 30 mètres au sommet du Corcovado.",
            "Le carnaval et ses écoles de samba, qui préparent leur défilé toute l'année.",
            "Le trottoir de Copacabana dessine des vagues en mosaïque de pierres noires et blanches.",
            Souvenir("Pandeiro", "Tambourin brésilien, cœur rythmique de la samba.", "music"),
        ),
        Stop(
            "La Havane", "Cuba", 556.0, 23.14, -82.36,
            "La vieille Havane, ses façades coloniales aux couleurs pastel et ses balcons en fer forgé.",
            "Le son cubain, ancêtre de la salsa, s'écoute dans les rues et les patios.",
            "Des voitures américaines des années 1950 y roulent encore, souvent comme taxis.",
            Souvenir("Maracas", "Deux hochets remplis de graines qui marquent le rythme.", "music"),
        ),
        Stop(
            "Mexico", "Mexique", 586.0, 19.43, -99.13,
            "Non loin, la pyramide du Soleil de Teotihuacan, l'une des plus grandes de l'Amérique ancienne.",
            "Le Jour des morts, fête colorée en mémoire des défunts, est inscrit au patrimoine immatériel de l'UNESCO.",
            "La ville est bâtie sur un ancien lac ; certains quartiers s'enfoncent de plusieurs centimètres par an.",
            Souvenir("Alebrije", "Figurine d'animal imaginaire, sculptée et peinte de couleurs vives.", "art"),
        ),
        Stop(
            "New York", "États-Unis", 621.0, 40.71, -74.01,
            "Les gratte-ciel Art déco comme le Chrysler Building, et la statue de la Liberté, offerte par la France en 1886.",
            "Les comédies musicales de Broadway, et des centaines de langues parlées dans la ville.",
            "La statue de la Liberté a été conçue par Bartholdi, avec une structure intérieure de Gustave Eiffel.",
            Souvenir("Taxi jaune", "Une miniature du célèbre yellow cab.", "car"),
        ),
        Stop(
            "Reykjavik", "Islande", 661.0, 64.15, -21.94,
            "L'église Hallgrímskirkja, dont la façade s'inspire des orgues de basalte formées par la lave.",
            "Les sagas, récits islandais du Moyen Âge, sont encore lues à l'école.",
            "Environ neuf maisons sur dix y sont chauffées par la géothermie.",
            Souvenir("Lopapeysa", "Pull en laine islandaise au motif circulaire autour du col.", "textile"),
        ),
        Stop(
            "Paris", "France", 700.0, 48.86, 2.35,
            "Retour sous l'arc de triomphe de l'Étoile, d'où partent douze avenues.",
            "Le tour du monde est bouclé, comme dans le roman de Jules Verne.",
            "Dans « Le Tour du monde en quatre-vingts jours », Phileas Fogg gagne son pari grâce au jour gagné en voyageant vers l'est.",
            Souvenir("Globe terrestre", "Le trophée de ceux qui ont fait le tour du monde.", "globe"),
        ),
    )

    val lengthKm: Double get() = stops.last().km

    /** Distance parcourue à partir d'un nombre de pas et de la longueur d'un pas. */
    fun kmFor(steps: Long, stepLengthCm: Int): Double = steps * stepLengthCm / 100_000.0

    fun progress(km: Double): TourProgress = TourProgress(km.coerceAtLeast(0.0))

    /** Escales atteintes (hors départ), dans l'ordre. */
    fun reachedStops(km: Double): List<ReachedStop> =
        stops.mapIndexedNotNull { i, s -> if (i > 0 && s.km <= km) ReachedStop(i, s) else null }

    fun keyOf(index: Int): Long = 1_000L + index
}
