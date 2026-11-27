/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.domain

/** Deuxième lot de pays d'Europe : Pays-Bas, Belgique, Suisse, Autriche et Grèce. */
object EuropeTours2 {

    val paysBas = Journey(
        id = "pays-bas",
        title = "Pays-Bas",
        kind = JourneyKind.COUNTRY,
        tagline = "Des canaux d'Amsterdam au village sans routes de Giethoorn, par les tulipes et les moulins.",
        keyBase = 8_000L,
        area = MapArea(latTop = 53.6, latBottom = 50.7, lonLeft = 3.3, lonRight = 7.3),
        requires = "france",
        stops = listOf(
            Stop(
                "Amsterdam", "Pays-Bas", 0.0, 52.37, 4.89,
                "Des maisons étroites à pignons bordent les canaux du XVIIe siècle, inscrits à l'UNESCO ; beaucoup penchent légèrement vers l'avant.",
                "Le vélo est roi : la ville compte plus de vélos que d'habitants.",
                "Sous le toit des maisons, un crochet sert à hisser les meubles par les fenêtres, car les escaliers sont trop raides.",
                Souvenir("Sabots", "Chaussures de bois peintes, autrefois portées dans les champs humides.", "shoe"),
            ),
            Stop(
                "Zaanse Schans", "Pays-Bas", 4.0, 52.47, 4.82,
                "Des moulins à vent en bois et des maisons vertes, témoins d'une des premières régions industrielles d'Europe.",
                "Les moulins y broyaient les épices, pressaient l'huile et moulaient les pigments des peintres.",
                "La région de la Zaan a compté jusqu'à 600 moulins à vent.",
                Souvenir("Moulin miniature", "Un petit moulin de bois dont les ailes tournent au moindre souffle.", "art"),
            ),
            Stop(
                "Keukenhof", "Pays-Bas", 12.0, 52.27, 4.55,
                "Un parc de 32 hectares où fleurissent chaque printemps environ sept millions de bulbes.",
                "La tulipe, venue de l'Empire ottoman, est devenue l'emblème du pays.",
                "Vers 1637, un bulbe de tulipe rare pouvait valoir le prix d'une maison : c'est la « tulipomanie ».",
                Souvenir("Bulbe de tulipe", "À planter à l'automne pour une fleur au printemps.", "spice"),
            ),
            Stop(
                "Delft", "Pays-Bas", 20.0, 52.01, 4.36,
                "La Nouvelle Église et son clocher de 108 mètres dominent la grand-place et l'hôtel de ville.",
                "La faïence bleue de Delft, inspirée de la porcelaine chinoise, y est peinte à la main depuis le XVIIe siècle.",
                "Le peintre Vermeer y est né et y a peint « La Jeune Fille à la perle ».",
                Souvenir("Carreau de Delft", "Un carreau de faïence bleu et blanc peint d'un moulin.", "pottery"),
            ),
            Stop(
                "Rotterdam", "Pays-Bas", 26.0, 51.92, 4.48,
                "Les maisons cubes, inclinées à 45 degrés sur des piliers, et le pont Érasme, surnommé « le Cygne ».",
                "Son port est le plus grand d'Europe.",
                "Bombardé en 1940, le centre a été reconstruit en véritable vitrine de l'architecture moderne.",
                Souvenir("Maison cube", "Une maquette de maison cube posée sur sa pointe.", "art"),
            ),
            Stop(
                "Kinderdijk", "Pays-Bas", 30.0, 51.88, 4.64,
                "Dix-neuf moulins du XVIIIe siècle alignés le long des canaux pour pomper l'eau des polders.",
                "Un quart du pays se trouve sous le niveau de la mer : digues et pompes le gardent au sec.",
                "Kinderdijk veut dire « digue de l'enfant » : selon la légende, un berceau y aurait échoué après une inondation en 1421, un chat sur son bord.",
                Souvenir("Meule de gouda", "Le fromage de la ville voisine de Gouda.", "food"),
            ),
            Stop(
                "Utrecht", "Pays-Bas", 40.0, 52.09, 5.12,
                "La tour du Dom, haute de 112 mètres, la plus haute tour d'église des Pays-Bas.",
                "Miffy, la petite lapine aux yeux ronds et à la bouche en croix, est née ici sous le crayon de Dick Bruna.",
                "Une tornade a abattu la nef de la cathédrale en 1674 : depuis, la tour est séparée du reste de l'église.",
                Souvenir("Peluche Miffy", "La petite lapine blanche d'Utrecht.", "toy"),
            ),
            Stop(
                "Hoge Veluwe", "Pays-Bas", 56.0, 52.09, 5.82,
                "Au milieu du parc, le musée Kröller-Müller abrite près de 90 tableaux de Van Gogh.",
                "On traverse le parc sur des vélos blancs prêtés gratuitement.",
                "Landes, dunes de sable et forêts : on peut y croiser des cerfs et des sangliers.",
                Souvenir("Reproduction de Van Gogh", "Des tournesols aux couleurs éclatantes.", "art"),
            ),
            Stop(
                "Giethoorn", "Pays-Bas", 72.0, 52.74, 6.08,
                "Des fermes aux toits de chaume reliées par plus de 170 petits ponts de bois.",
                "Dans la vieille ville, il n'y a pas de routes : on circule en barque ou en canoë.",
                "Son nom viendrait des cornes de chèvres sauvages (« geitenhoorns ») trouvées par les premiers habitants.",
                Souvenir("Barque de Giethoorn", "Le trophée de ceux qui ont parcouru les Pays-Bas.", "boat"),
            ),
        ),
    )

    val belgique = Journey(
        id = "belgique",
        title = "Belgique",
        kind = JourneyKind.COUNTRY,
        tagline = "De l'Atomium aux forêts des Ardennes, par Bruges, Gand et la Meuse.",
        keyBase = 9_000L,
        area = MapArea(latTop = 51.6, latBottom = 49.4, lonLeft = 2.4, lonRight = 6.5),
        requires = "france",
        stops = listOf(
            Stop(
                "Bruxelles", "Belgique", 0.0, 50.85, 4.35,
                "La Grand-Place et ses maisons des corporations dorées, et l'Atomium, molécule géante d'acier construite pour l'Exposition universelle de 1958.",
                "La bande dessinée est partout : Tintin, Spirou et les Schtroumpfs sont nés en Belgique, et des fresques en couvrent les murs.",
                "Le Manneken-Pis possède plus de 1 000 costumes, qu'on lui enfile les jours de fête.",
                Souvenir("Album de Tintin", "Les aventures du reporter à la houppe et de son chien Milou.", "book"),
            ),
            Stop(
                "Anvers", "Belgique", 5.0, 51.22, 4.42,
                "La gare centrale, surnommée la « cathédrale ferroviaire », sous une immense coupole de fer et de verre.",
                "La ville est la capitale mondiale du diamant : la plupart des diamants bruts du monde passent par elle.",
                "Le peintre Rubens y a vécu ; sa maison-atelier se visite toujours.",
                Souvenir("Diamant", "Une pierre taillée dans les ateliers d'Anvers.", "jewel"),
            ),
            Stop(
                "Gand", "Belgique", 14.0, 51.05, 3.72,
                "Le château des Comtes, forteresse du XIIe siècle au cœur de la ville, et les maisons à pignons du quai aux Herbes.",
                "L'Agneau mystique des frères Van Eyck, chef-d'œuvre de la peinture flamande, est exposé dans la cathédrale Saint-Bavon.",
                "L'un des panneaux de l'Agneau mystique, volé en 1934, n'a jamais été retrouvé.",
                Souvenir("Cuberdons", "Des bonbons violets en forme de cône, au cœur de sirop de framboise.", "food"),
            ),
            Stop(
                "Bruges", "Belgique", 22.0, 51.21, 3.22,
                "Le beffroi de 83 mètres domine la grand-place et des canaux bordés de maisons médiévales.",
                "La dentelle aux fuseaux y est travaillée à la main depuis le XVe siècle.",
                "Il faut gravir 366 marches pour atteindre le sommet du beffroi.",
                Souvenir("Napperon de dentelle", "Une dentelle fine faite aux fuseaux.", "textile"),
            ),
            Stop(
                "Ypres", "Belgique", 32.0, 50.85, 2.88,
                "La halle aux draps gothique, reconstruite à l'identique après sa destruction pendant la Première Guerre mondiale.",
                "Chaque soir à 20 heures, sous la porte de Menin, des clairons sonnent le Last Post en mémoire des soldats, depuis 1928.",
                "Tous les trois ans, la fête des chats rappelle qu'on jetait autrefois des chats du haut du beffroi ; aujourd'hui, ce sont des peluches.",
                Souvenir("Coquelicot", "La fleur du souvenir des soldats de la Grande Guerre.", "spice"),
            ),
            Stop(
                "Waterloo", "Belgique", 46.0, 50.68, 4.41,
                "La butte du Lion, colline artificielle de plus de 40 mètres coiffée d'un lion de fonte, domine le champ de bataille.",
                "Des milliers de figurants en costume y rejouent régulièrement la bataille de 1815.",
                "La butte a demandé environ 300 000 mètres cubes de terre, apportés dans des hottes par des femmes, les « hotteuses ».",
                Souvenir("Soldat de plomb", "Un grenadier de 1815 peint à la main.", "toy"),
            ),
            Stop(
                "Dinant", "Belgique", 62.0, 50.26, 4.91,
                "La collégiale au clocher en bulbe, adossée à la falaise sous la citadelle, au bord de la Meuse.",
                "Adolphe Sax, l'inventeur du saxophone, y est né en 1814.",
                "La couque de Dinant, biscuit au miel très dur, cuit dans des moules de bois sculpté.",
                Souvenir("Saxophone", "L'instrument inventé par l'enfant du pays.", "music"),
            ),
            Stop(
                "Liège", "Belgique", 76.0, 50.62, 5.57,
                "La gare des Guillemins, voûte blanche de verre et d'acier, et la montagne de Bueren, escalier de 374 marches.",
                "La gaufre de Liège, au sucre perlé, se mange chaude dans la rue.",
                "Le 15 août, le quartier d'Outremeuse fête sa « République libre » autour de Tchantchès, sa marionnette.",
                Souvenir("Tchantchès", "La marionnette au nez cabossé, héros populaire de Liège.", "toy"),
            ),
            Stop(
                "Spa", "Belgique", 84.0, 50.49, 5.86,
                "Des thermes et des fontaines, les pouhons, où l'on goûte l'eau ferrugineuse des sources, au milieu des forêts des Ardennes.",
                "Le circuit de Spa-Francorchamps, tout proche, accueille le Grand Prix de Belgique de Formule 1.",
                "Le mot « spa », employé dans de nombreuses langues pour les bains, vient du nom de la ville.",
                Souvenir("Boîte de pralines", "Le trophée de ceux qui ont parcouru la Belgique : des chocolats fourrés inventés à Bruxelles en 1912.", "food"),
            ),
        ),
    )

    val suisse = Journey(
        id = "suisse",
        title = "Suisse",
        kind = JourneyKind.COUNTRY,
        tagline = "Du jet d'eau de Genève aux façades peintes d'Appenzell, par le Cervin et la Jungfrau.",
        keyBase = 10_000L,
        area = MapArea(latTop = 47.9, latBottom = 45.7, lonLeft = 5.8, lonRight = 10.6),
        requires = "france",
        stops = listOf(
            Stop(
                "Genève", "Suisse", 0.0, 46.20, 6.15,
                "Le jet d'eau du lac Léman projette l'eau jusqu'à 140 mètres de haut.",
                "La ville accueille le siège européen des Nations unies et la Croix-Rouge, fondée ici en 1863.",
                "L'horloge fleurie du Jardin anglais est faite de milliers de plantes, renouvelées au fil des saisons.",
                Souvenir("Montre suisse", "Une montre mécanique, fierté des horlogers genevois.", "clock"),
            ),
            Stop(
                "Lavaux", "Suisse", 6.0, 46.49, 6.75,
                "Des vignes en terrasses descendent jusqu'au lac Léman, retenues par des murs de pierre et inscrites à l'UNESCO.",
                "Le chasselas, cépage blanc, y donne un vin léger qu'on aime boire avec la fondue.",
                "On dit que les vignes profitent de « trois soleils » : celui du ciel, son reflet sur le lac et la chaleur des murs.",
                Souvenir("Caquelon à fondue", "Le poêlon de terre où fond le fromage.", "pottery"),
            ),
            Stop(
                "Montreux", "Suisse", 10.0, 46.41, 6.93,
                "Le château de Chillon, posé sur un rocher au bord du lac, l'un des monuments les plus visités de Suisse.",
                "Le festival de jazz de Montreux, créé en 1967, accueille chaque été les plus grands musiciens.",
                "Le poète Lord Byron a gravé son nom sur un pilier des souterrains du château.",
                Souvenir("Disque de jazz", "Un concert enregistré au festival.", "music"),
            ),
            Stop(
                "Zermatt", "Suisse", 26.0, 46.02, 7.75,
                "Le Cervin, pyramide de 4 478 mètres, domine des chalets de mélèze noircis par le temps.",
                "Le village est sans voitures : on y circule en taxis électriques et en calèches.",
                "Le Cervin a été gravi pour la première fois en 1865 ; quatre des sept alpinistes sont morts à la descente.",
                Souvenir("Couteau suisse", "Le couteau rouge aux mille outils.", "key"),
            ),
            Stop(
                "Jungfraujoch", "Suisse", 40.0, 46.55, 7.98,
                "La plus haute gare d'Europe, à 3 454 mètres, atteinte par un train à crémaillère qui traverse la montagne.",
                "Le cor des Alpes, long tube de bois, servait autrefois à appeler les troupeaux.",
                "Du sommet, on voit le glacier d'Aletsch, le plus long des Alpes avec environ 20 km.",
                Souvenir("Cor des Alpes", "Une miniature du long cor de bois des bergers.", "music"),
            ),
            Stop(
                "Berne", "Suisse", 48.0, 46.95, 7.45,
                "La tour de l'Horloge, le Zytglogge, et six kilomètres d'arcades couvertes dans la vieille ville.",
                "L'été, les Bernois se baignent dans l'Aar en se laissant porter par le courant.",
                "Albert Einstein y a imaginé la relativité restreinte en 1905, quand il travaillait à l'office des brevets.",
                Souvenir("Ours en peluche", "L'ours, emblème de la ville.", "toy"),
            ),
            Stop(
                "Lucerne", "Suisse", 58.0, 47.05, 8.31,
                "Le pont de la Chapelle, pont couvert en bois du XIVe siècle, et sa tour octogonale au milieu de la Reuss.",
                "Le carnaval de Lucerne et ses fanfares déchaînées, les Guggenmusik.",
                "En grande partie détruit par un incendie en 1993, le pont a été reconstruit en un an.",
                Souvenir("Panneau peint", "Une copie des peintures triangulaires accrochées sous le toit du pont.", "art"),
            ),
            Stop(
                "Zurich", "Suisse", 66.0, 47.37, 8.54,
                "Les deux tours du Grossmünster se dressent au bord de la Limmat.",
                "Les grands chocolatiers suisses, comme Sprüngli, y ont leurs racines.",
                "Le mouvement Dada est né à Zurich en 1916, au Cabaret Voltaire.",
                Souvenir("Tablette de chocolat", "Du chocolat au lait suisse.", "food"),
            ),
            Stop(
                "Appenzell", "Suisse", 76.0, 47.33, 9.41,
                "Des maisons aux façades de bois peintes de motifs colorés, au pied du massif de l'Alpstein.",
                "Chaque printemps, à la Landsgemeinde, les citoyens votent à main levée sur la place.",
                "Le canton d'Appenzell Rhodes-Intérieures a été le dernier de Suisse à accorder le droit de vote aux femmes, en 1990.",
                Souvenir("Fromage d'Appenzell", "Le trophée de ceux qui ont parcouru la Suisse : une meule frottée d'une saumure aux herbes à la recette secrète.", "food"),
            ),
        ),
    )

    val autriche = Journey(
        id = "autriche",
        title = "Autriche",
        kind = JourneyKind.COUNTRY,
        tagline = "De Schönbrunn au lac de Neusiedl, par le Danube, Salzbourg et les Alpes du Tyrol.",
        keyBase = 11_000L,
        area = MapArea(latTop = 49.1, latBottom = 46.3, lonLeft = 9.4, lonRight = 17.2),
        requires = "france",
        stops = listOf(
            Stop(
                "Vienne", "Autriche", 0.0, 48.19, 16.31,
                "Le château de Schönbrunn, résidence d'été des Habsbourg, et ses 1 441 pièces.",
                "Les cafés viennois sont une institution : on peut y rester des heures avec un café et un verre d'eau.",
                "Le zoo de Schönbrunn, ouvert en 1752, est le plus ancien zoo encore en activité au monde.",
                Souvenir("Boîte de Sachertorte", "La boîte de bois du gâteau au chocolat créé en 1832.", "food"),
            ),
            Stop(
                "Melk", "Autriche", 6.0, 48.23, 15.33,
                "L'abbaye bénédictine baroque, jaune et blanche, perchée au-dessus du Danube.",
                "Dans la vallée de la Wachau, on cultive l'abricot et le grüner veltliner, vin blanc du pays.",
                "Umberto Eco a nommé « Adso de Melk » le narrateur du « Nom de la rose ».",
                Souvenir("Livre ancien", "Comme ceux de la bibliothèque baroque de l'abbaye.", "book"),
            ),
            Stop(
                "Linz", "Autriche", 14.0, 48.31, 14.29,
                "L'Ars Electronica Center, musée du futur dont la façade de verre s'illumine de couleurs la nuit.",
                "La Linzer Torte, à la confiture et à la pâte aux amandes, passe pour le plus ancien gâteau dont on ait gardé la recette.",
                "L'astronome Johannes Kepler y a vécu et y a publié une de ses lois du mouvement des planètes.",
                Souvenir("Linzer Torte", "La tarte quadrillée à la confiture de groseille.", "food"),
            ),
            Stop(
                "Hallstatt", "Autriche", 26.0, 47.56, 13.65,
                "Des maisons de bois serrées entre le lac et la montagne, autour d'un clocher pointu.",
                "Le sel y est extrait depuis plus de 7 000 ans : on la dit la plus ancienne mine de sel du monde.",
                "Le cimetière manquant de place, plus de 600 crânes ont été peints de fleurs et de noms dans l'ossuaire.",
                Souvenir("Sel de Hallstatt", "Un sachet de sel de la montagne.", "spice"),
            ),
            Stop(
                "Salzbourg", "Autriche", 32.0, 47.80, 13.04,
                "La forteresse de Hohensalzburg, l'une des plus grandes forteresses médiévales d'Europe, au-dessus des clochers baroques.",
                "Mozart y est né en 1756 ; le festival de Salzbourg fait vivre sa musique chaque été.",
                "Le film « La Mélodie du bonheur » a été tourné dans la ville et ses environs en 1964.",
                Souvenir("Partition de Mozart", "Une page de « La Flûte enchantée ».", "music"),
            ),
            Stop(
                "Innsbruck", "Autriche", 48.0, 47.27, 11.39,
                "Le Petit Toit d'or, balcon couvert de 2 657 tuiles de cuivre doré, et le tremplin de saut de Bergisel, redessiné par Zaha Hadid.",
                "La ville a accueilli deux fois les Jeux olympiques d'hiver, en 1964 et 1976.",
                "Un funiculaire mène du centre-ville aux sommets de la Nordkette en une vingtaine de minutes.",
                Souvenir("Cristal taillé", "Une petite figurine de cristal de la vallée de l'Inn.", "jewel"),
            ),
            Stop(
                "Grossglockner", "Autriche", 58.0, 47.07, 12.69,
                "La route alpine du Grossglockner, 48 kilomètres et 36 virages jusqu'à 2 504 mètres, au pied du plus haut sommet d'Autriche.",
                "Les marmottes sifflent pour prévenir leurs voisines d'un danger.",
                "Le glacier de la Pasterze, au pied du sommet, recule de plusieurs dizaines de mètres par an.",
                Souvenir("Edelweiss", "La fleur blanche et duveteuse des hauts alpages.", "spice"),
            ),
            Stop(
                "Graz", "Autriche", 76.0, 47.07, 15.44,
                "La tour de l'Horloge sur le Schlossberg, et la Kunsthaus, musée surnommé « l'extraterrestre amical ».",
                "L'huile de pépins de courge, vert sombre, assaisonne les salades et même les glaces.",
                "Sur l'horloge, la grande aiguille marque les heures et la petite les minutes, pour être lisible de loin.",
                Souvenir("Huile de courge", "Une bouteille d'huile vert sombre de Styrie.", "food"),
            ),
            Stop(
                "Lac de Neusiedl", "Autriche", 86.0, 47.85, 16.76,
                "Un lac des steppes peu profond, bordé de roseaux et partagé avec la Hongrie.",
                "Les cigognes nichent sur les cheminées des villages, comme à Rust.",
                "Le lac ne fait qu'environ 1,8 mètre de profondeur ; il s'est même entièrement asséché au XIXe siècle.",
                Souvenir("Cigogne de bois", "Le trophée de ceux qui ont parcouru l'Autriche.", "toy"),
            ),
        ),
    )

    val grece = Journey(
        id = "grece",
        title = "Grèce",
        kind = JourneyKind.COUNTRY,
        tagline = "De l'Acropole à Santorin, par Olympie, Delphes et les Météores.",
        keyBase = 12_000L,
        area = MapArea(latTop = 41.8, latBottom = 35.8, lonLeft = 19.3, lonRight = 28.0),
        requires = "france",
        stops = listOf(
            Stop(
                "Athènes", "Grèce", 0.0, 37.97, 23.73,
                "La Plaka, le plus vieux quartier de la ville, et ses ruelles au pied de l'Acropole.",
                "La relève des evzones, gardes en jupe plissée de 400 plis, devant le Parlement.",
                "Les premiers Jeux olympiques modernes s'y sont tenus en 1896, dans le stade Panathénaïque tout en marbre.",
                Souvenir("Tsarouchi", "La chaussure à pompon des evzones.", "shoe"),
            ),
            Stop(
                "Corinthe", "Grèce", 6.0, 37.93, 22.99,
                "Le canal de Corinthe, taillé dans la roche en 1893 : plus de 6 km de long, mais seulement 25 mètres de large.",
                "Les raisins secs de Corinthe ont donné leur nom au mot anglais « currant ».",
                "L'empereur Néron avait déjà commencé à creuser le canal en l'an 67, avec une pelle d'or.",
                Souvenir("Raisins de Corinthe", "Un sachet de petits raisins secs noirs.", "food"),
            ),
            Stop(
                "Mycènes", "Grèce", 12.0, 37.73, 22.76,
                "La porte des Lionnes, sculptée il y a plus de 3 000 ans, et des murs faits de blocs énormes.",
                "Dans les poèmes d'Homère, Agamemnon est le roi de Mycènes.",
                "On appelle ces murs « cyclopéens » : les Grecs pensaient que seuls des géants, les Cyclopes, avaient pu les bâtir.",
                Souvenir("Masque d'Agamemnon", "La réplique du masque d'or trouvé dans une tombe royale.", "mask"),
            ),
            Stop(
                "Olympie", "Grèce", 26.0, 37.64, 21.63,
                "Les ruines du temple de Zeus et le stade où couraient les athlètes de l'Antiquité.",
                "La flamme olympique y est allumée avant chaque Jeux, avec les rayons du soleil et un miroir.",
                "Les vainqueurs ne recevaient pas de médaille, mais une couronne d'olivier.",
                Souvenir("Couronne d'olivier", "Le prix des champions de l'Antiquité.", "spice"),
            ),
            Stop(
                "Delphes", "Grèce", 42.0, 38.48, 22.50,
                "Le sanctuaire d'Apollon s'étage sur les pentes du Parnasse, avec son théâtre et la tholos, temple rond.",
                "La Pythie y rendait les oracles d'Apollon, consultés par les cités de tout le monde grec.",
                "Les Grecs voyaient Delphes comme le centre du monde, marqué par une pierre, l'omphalos, le « nombril ».",
                Souvenir("Omphalos", "Une réplique de la pierre du nombril du monde.", "art"),
            ),
            Stop(
                "Météores", "Grèce", 58.0, 39.71, 21.63,
                "Des monastères perchés au sommet de pitons rocheux hauts de plusieurs centaines de mètres.",
                "Des moines et des moniales vivent toujours dans six de ces monastères.",
                "Jusqu'aux années 1920, on y montait dans un filet hissé par une corde et un treuil.",
                Souvenir("Icône peinte", "Une petite image sainte peinte sur bois.", "art"),
            ),
            Stop(
                "Mont Olympe", "Grèce", 68.0, 40.09, 22.36,
                "Le plus haut sommet de Grèce, à 2 918 mètres, demeure des dieux dans la mythologie.",
                "Les douze dieux de l'Olympe, Zeus en tête, veillaient sur la vie des hommes.",
                "Son plus haut sommet, le Mytikas, n'a été gravi qu'en 1913.",
                Souvenir("Éclair de Zeus", "L'arme du roi des dieux, en bois doré.", "art"),
            ),
            Stop(
                "Thessalonique", "Grèce", 76.0, 40.63, 22.95,
                "La tour Blanche, sur le front de mer, symbole de la ville.",
                "La bougatsa, feuilleté à la crème ou au fromage, se mange au petit-déjeuner.",
                "Alexandre le Grand est né tout près, à Pella, en 356 avant notre ère.",
                Souvenir("Pièce d'Alexandre", "La réplique d'une drachme d'argent à l'effigie d'Alexandre.", "jewel"),
            ),
            Stop(
                "Santorin", "Grèce", 110.0, 36.46, 25.38,
                "Des maisons blanches et des coupoles bleues accrochées au bord d'une caldeira, cratère de volcan envahi par la mer.",
                "Les vignes y sont enroulées en couronnes au ras du sol pour se protéger du vent.",
                "Une gigantesque éruption, vers 1600 avant notre ère, aurait pu inspirer le mythe de l'Atlantide.",
                Souvenir("Coupole bleue", "Le trophée de ceux qui ont parcouru la Grèce.", "art"),
            ),
        ),
    )

    val all: List<Journey> = listOf(paysBas, belgique, suisse, autriche, grece)
}
