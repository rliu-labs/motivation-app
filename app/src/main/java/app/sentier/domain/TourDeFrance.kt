/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.domain

/** Le tour de France, de Notre-Dame à l'arc de triomphe, après le tour du monde. */
object TourDeFrance {
    val stops: List<Stop> = listOf(
        Stop(
            "Paris", "France", 0.0, 48.85, 2.35,
            "Notre-Dame, cathédrale gothique commencée en 1163, avec ses arcs-boutants, ses deux tours et ses rosaces. Restaurée après l'incendie de 2019, elle a rouvert en décembre 2024.",
            "Les bouquinistes vendent livres anciens et gravures dans leurs boîtes vertes accrochées aux quais de la Seine.",
            "Sur le parvis de Notre-Dame, une plaque marque le point zéro des routes de France : les distances vers Paris se mesurent depuis là.",
            Souvenir("Chimère de Notre-Dame", "Réplique d'une des créatures de pierre ajoutées par Viollet-le-Duc sur la galerie de la cathédrale.", "art"),
        ),
        Stop(
            "Reims", "France", 6.0, 49.25, 4.03,
            "La cathédrale Notre-Dame, où furent sacrés la plupart des rois de France, et l'Ange au sourire sculpté sur sa façade.",
            "Le champagne vieillit dans des crayères, anciennes carrières de craie creusées sous la ville, inscrites à l'UNESCO avec les coteaux.",
            "Une bouteille de champagne est sous une pression d'environ 6 bars, à peu près trois fois celle d'un pneu de voiture.",
            Souvenir("Muselet", "Le petit panier de fil de fer qui retient le bouchon ; certains en font collection.", "wine"),
        ),
        Stop(
            "Strasbourg", "France", 22.0, 48.58, 7.75,
            "La cathédrale de grès rose et sa flèche unique de 142 mètres, puis les maisons à colombages de la Petite France, au bord de l'Ill.",
            "Le marché de Noël, l'un des plus anciens d'Europe, s'y tient depuis 1570.",
            "Grâce à sa flèche, la cathédrale a été le plus haut édifice du monde pendant plus de deux siècles, de 1647 à 1874.",
            Souvenir("Moule à kougelhopf", "Moule en terre cuite vernissée, en couronne, pour la brioche alsacienne aux raisins.", "pottery"),
        ),
        Stop(
            "Chamonix", "France", 40.0, 45.92, 6.87,
            "Des chalets de bois et de pierre aux grands toits, où la neige reste l'hiver comme une couche isolante.",
            "Les tout premiers Jeux olympiques d'hiver s'y sont déroulés en 1924.",
            "Le mont Blanc, plus haut sommet des Alpes, a été gravi pour la première fois en 1786 par Jacques Balmat et Michel Paccard.",
            Souvenir("Cloche d'alpage", "La cloche des troupeaux qu'on entend tinter tout l'été dans les pâturages.", "music"),
        ),
        Stop(
            "Lyon", "France", 50.0, 45.76, 4.82,
            "La basilique de Fourvière domine la ville depuis sa colline ; en bas, les traboules, passages couverts, traversent les immeubles d'une rue à l'autre.",
            "Chaque 8 décembre, la fête des Lumières illumine la ville et les Lyonnais posent des lumignons sur leurs fenêtres.",
            "Les frères Lumière y ont tourné en 1895 l'un des premiers films de l'histoire : la sortie des ouvriers de leur usine.",
            Souvenir("Guignol", "La marionnette à gaine créée à Lyon vers 1808 par Laurent Mourguet.", "toy"),
        ),
        Stop(
            "Avignon", "France", 63.0, 43.95, 4.81,
            "Le pont Saint-Bénezet, dont il ne reste que quatre arches, et le palais des Papes, le plus grand palais gothique du monde.",
            "Chaque juillet, le festival d'Avignon, créé en 1947 par Jean Vilar, fait de toute la ville une scène de théâtre.",
            "Au XIVe siècle, les papes ont quitté Rome pour Avignon : sept papes s'y sont succédé.",
            Souvenir("Cigale en faïence", "L'emblème de la Provence, porte-bonheur accroché aux murs des maisons.", "pottery"),
        ),
        Stop(
            "Marseille", "France", 72.0, 43.30, 5.37,
            "Notre-Dame-de-la-Garde, la « Bonne Mère », veille sur le Vieux-Port depuis sa colline, coiffée d'une Vierge dorée.",
            "La pétanque est née tout près, à La Ciotat, en 1907 : un joueur aux jambes douloureuses a lancé ses boules « pieds tanqués », sans élan.",
            "Fondée par des marins grecs vers 600 avant notre ère, Marseille est la plus ancienne ville de France.",
            Souvenir("Savon de Marseille", "Cube de savon à l'huile d'olive, cuit en chaudron, avec son tampon sur chaque face.", "soap"),
        ),
        Stop(
            "Carcassonne", "France", 90.0, 43.21, 2.36,
            "La Cité, ville fortifiée médiévale à double enceinte et 52 tours, restaurée au XIXe siècle par Viollet-le-Duc.",
            "Le cassoulet, plat mijoté de haricots blancs et de viandes, que se disputent Carcassonne, Castelnaudary et Toulouse.",
            "La légende dit que dame Carcas sauva la ville d'un siège en jetant un cochon gavé de blé par-dessus les remparts, pour faire croire que la ville ne manquait de rien.",
            Souvenir("Épée de chevalier", "Une petite épée de bois, comme celles des enfants qui jouent sur les remparts.", "toy"),
        ),
        Stop(
            "Biarritz", "France", 108.0, 43.49, -1.56,
            "Le phare domine l'océan depuis 1834, et le rocher de la Vierge s'avance dans les vagues au bout d'une passerelle de fer.",
            "La pelote basque se joue à main nue ou avec un chistera contre le mur du fronton, au cœur de chaque village.",
            "Le surf est arrivé en Europe par Biarritz, en 1957.",
            Souvenir("Espadrilles", "Chaussures de toile à semelle de corde, fabriquées notamment à Mauléon, au Pays basque.", "shoe"),
        ),
        Stop(
            "Bordeaux", "France", 120.0, 44.84, -0.57,
            "La place de la Bourse, du XVIIIe siècle, se reflète dans le miroir d'eau, l'un des plus grands du monde.",
            "La ville est entourée de vignobles réputés : Saint-Émilion, Médoc, Pessac, Sauternes…",
            "On l'appelle le port de la Lune, à cause du croissant que dessine la Garonne en traversant la ville.",
            Souvenir("Moule à canelé", "Petit moule de cuivre cannelé où cuit le gâteau bordelais, croustillant dehors et moelleux dedans.", "food"),
        ),
        Stop(
            "Chambord", "France", 142.0, 47.62, 1.52,
            "Le château voulu par François Ier, avec ses 282 cheminées et son escalier à double révolution, peut-être imaginé par Léonard de Vinci.",
            "Le Val de Loire, ses châteaux et ses jardins sont inscrits au patrimoine mondial de l'UNESCO.",
            "Dans l'escalier à double révolution, deux personnes peuvent monter et descendre en même temps sans jamais se croiser.",
            Souvenir("Clé de château", "Une grosse clé en fer forgé, comme celles des portes du château.", "key"),
        ),
        Stop(
            "Carnac", "France", 162.0, 47.58, -3.08,
            "Près de 3 000 menhirs dressés en longues files il y a environ 6 000 ans.",
            "Le fest-noz, fête de nuit bretonne où l'on danse en chaîne au son des chants et des bombardes, est inscrit au patrimoine immatériel de l'UNESCO.",
            "Une légende raconte que ces menhirs sont des soldats romains changés en pierre par saint Cornély.",
            Souvenir("Marinière", "Le tricot rayé bleu et blanc des marins bretons, adopté par la Marine en 1858.", "textile"),
        ),
        Stop(
            "Mont-Saint-Michel", "France", 176.0, 48.64, -1.51,
            "L'abbaye bénédictine se dresse au sommet d'un îlot rocheux, coiffée d'une statue dorée de l'archange saint Michel.",
            "L'omelette de la Mère Poulard, battue en rythme et cuite au feu de bois, y est servie depuis 1888.",
            "Les marées y sont parmi les plus fortes d'Europe : la mer monte, dit-on, « à la vitesse d'un cheval au galop ».",
            Souvenir("Enseigne de pèlerin", "Petite médaille que les pèlerins du Moyen Âge cousaient à leur chapeau.", "jewel"),
        ),
        Stop(
            "Lille", "France", 200.0, 50.63, 3.06,
            "Le beffroi de l'hôtel de ville, haut de 104 mètres, inscrit à l'UNESCO avec les autres beffrois du Nord et de Belgique.",
            "La braderie, le premier week-end de septembre, est l'un des plus grands marchés aux puces d'Europe ; on y mange des moules-frites.",
            "Dans les fêtes du Nord défilent des géants, figures de plusieurs mètres portées par des hommes cachés dessous.",
            Souvenir("Figurine de géant", "Lydéric ou Phinaert, les géants de la légende de la fondation de Lille.", "toy"),
        ),
        Stop(
            "Paris", "France", 212.0, 48.87, 2.30,
            "L'arc de triomphe de l'Étoile, en haut des Champs-Élysées, où arrive traditionnellement le Tour de France cycliste.",
            "Le Tour de France est né en 1903 ; le maillot jaune, apparu en 1919, a la couleur des pages du journal L'Auto qui l'organisait.",
            "Sous l'arc brûle depuis 1923 la flamme du Soldat inconnu, ravivée chaque soir.",
            Souvenir("Maillot jaune", "Le trophée de ceux qui ont bouclé le tour de France.", "bike"),
        ),
    )

    val journey: Journey = Journey(
        id = "france",
        title = "Tour de France",
        kind = JourneyKind.COUNTRY,
        tagline = "De Notre-Dame à l'arc de triomphe, en 14 escales des Alpes à la Bretagne.",
        keyBase = 2_000L,
        area = MapArea(latTop = 51.4, latBottom = 42.2, lonLeft = -5.3, lonRight = 8.6),
        stops = stops,
        requires = "monde",
    )
}
