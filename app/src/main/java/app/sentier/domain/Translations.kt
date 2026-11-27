/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.domain

/** Textes anglais d'une escale, dans l'ordre des champs français de [Stop] et de son [Souvenir]. */
data class StopEn(
    val city: String,
    val country: String,
    val architecture: String,
    val culture: String,
    val anecdote: String,
    val souvenirName: String,
    val souvenirDescription: String,
)

/** Textes anglais d'un voyage : [stops] suit exactement l'ordre des escales françaises. */
data class JourneyEn(val title: String, val tagline: String, val stops: List<StopEn>)

/**
 * Les voyages en anglais, par identifiant. Les données des voyages restent écrites en français ;
 * chaque nouveau voyage doit avoir ici sa traduction (un test le vérifie).
 */
object JourneysEn {
    val all: Map<String, JourneyEn> by lazy {
        mapOf("monde" to WorldTourEn.journey, "france" to TourDeFranceEn.journey) + EuropeToursEn.all + EuropeTours2En.all
    }
}

/** Le voyage dans la langue de l'appli : lui-même en français, ses textes anglais sinon (le français si une traduction manque). */
fun Journey.localized(english: Boolean = Lang.english): Journey {
    if (!english) return this
    val en = JourneysEn.all[id] ?: return this
    return copy(
        title = en.title,
        tagline = en.tagline,
        stops = stops.mapIndexed { i, s ->
            val t = en.stops.getOrNull(i) ?: return@mapIndexed s
            s.copy(
                city = t.city,
                country = t.country,
                architecture = t.architecture,
                culture = t.culture,
                anecdote = t.anecdote,
                souvenir = s.souvenir.copy(name = t.souvenirName, description = t.souvenirDescription),
            )
        },
    )
}

private object WorldTourEn {
    val journey = JourneyEn(
        "World tour",
        "From Paris to Paris, heading east, in 25 stops across five continents.",
        listOf(
            StopEn(
                "Paris", "France",
                "Haussmann-style buildings in dressed stone and the Eiffel Tower, built for the 1889 World's Fair.",
                "Museums, cafés and fine food: the French gastronomic meal is on UNESCO's intangible heritage list.",
                "The Eiffel Tower was meant to be taken down after 20 years. Its radio antennas saved it.",
                "Traveller's passport", "The booklet that will receive the stamp of every stop.",
            ),
            StopEn(
                "Marrakesh", "Morocco",
                "The 12th-century Koutoubia minaret and zellige, mosaics of small glazed clay tiles cut by hand.",
                "On Jemaa el-Fna square, storytellers and musicians keep alive a tradition on UNESCO's intangible heritage list.",
                "Mint tea is poured from high up to make a light foam in the glass.",
                "Babouches", "Pointed leather slippers, stitched in the souks.",
            ),
            StopEn(
                "Rome", "Italy",
                "The Pantheon and its dome, still the largest unreinforced concrete dome in the world, nearly 1,900 years after it was built.",
                "Espresso is drunk at the counter, standing up, in a few sips.",
                "The coins thrown into the Trevi Fountain, over a million euros a year, are given to charity.",
                "Moka pot", "The eight-sided coffee maker invented in Italy in 1933.",
            ),
            StopEn(
                "Athens", "Greece",
                "The Parthenon, a Doric marble temple completed on the Acropolis in the 5th century BC.",
                "Theatre was born here: tragedies and comedies were staged in the open air before thousands of spectators.",
                "The Parthenon's columns bulge slightly so that they look perfectly straight to the eye.",
                "Komboloi", "A string of beads you slide between your fingers to relax.",
            ),
            StopEn(
                "Istanbul", "Turkey",
                "Hagia Sophia, built in the 6th century, whose dome seems to float above a ring of windows.",
                "The Grand Bazaar, one of the oldest covered markets in the world, holds thousands of shops.",
                "The city straddles two continents: the Bosphorus separates its European shore from its Asian shore.",
                "Nazar eye", "A blue glass bead said to protect against the evil eye.",
            ),
            StopEn(
                "Petra", "Jordan",
                "Al-Khazneh, a monumental façade carved straight into the pink sandstone cliff by the Nabataeans.",
                "The Bedouin welcome travellers with coffee scented with cardamom.",
                "You enter the city through the Siq, a narrow canyon about 1.2 km long.",
                "Sand bottle", "Dunes and camels drawn grain by grain with coloured sand.",
            ),
            StopEn(
                "Cairo", "Egypt",
                "The Great Pyramid of Giza, the only one of the Seven Wonders of the Ancient World still standing.",
                "The Nile has set the rhythm of life since antiquity; feluccas, traditional sailing boats, still sail on it.",
                "The Great Pyramid remained the tallest structure in the world for nearly 4,000 years.",
                "Scarab", "A lucky amulet, the ancient Egyptians' symbol of the rising sun.",
            ),
            StopEn(
                "Lalibela", "Ethiopia",
                "Eleven churches carved into the rock, including the Church of Saint George, cut from a single cross-shaped block.",
                "The coffee ceremony, served in three rounds, is a great moment of hospitality.",
                "Ethiopia follows its own calendar, which has thirteen months.",
                "Jebena", "A long-necked clay coffee pot used for the coffee ceremony.",
            ),
            StopEn(
                "Isfahan", "Iran",
                "Naqsh-e Jahan Square, lined with mosques covered in blue and turquoise tiles.",
                "Poetry is part of everyday life: people readily quote Hafez and Saadi.",
                "In the Sheikh Lotfollah Mosque, the dome changes colour with the daylight.",
                "Persian rug", "Knotted by hand, it can have over a million knots per square metre.",
            ),
            StopEn(
                "Agra", "India",
                "The Taj Mahal, a white marble mausoleum inlaid with stones, built in the 17th century by Emperor Shah Jahan for his wife.",
                "Chai, spiced milky tea, is drunk at any hour in small cups.",
                "The Taj Mahal's marble changes shade through the day, from the pink of dawn to the gold of evening.",
                "Spice box", "The masala dabba, a round box that keeps everyday spices close at hand.",
            ),
            StopEn(
                "Kathmandu", "Nepal",
                "The Boudhanath stupa, a huge white dome topped by the eyes of the Buddha facing the four directions.",
                "Prayer flags flutter everywhere: it is said the wind carries the prayers away.",
                "Nepal's flag is the only national flag that is not rectangular.",
                "Prayer flags", "Five colours for five elements: sky, air, fire, water and earth.",
            ),
            StopEn(
                "Beijing", "China",
                "The Forbidden City, the imperial palace with yellow tiled roofs, a colour reserved for the emperor.",
                "Calligraphy, the art of brush and ink, and tea tasting in small cups.",
                "Contrary to popular belief, the Great Wall cannot be seen with the naked eye from space.",
                "Yixing teapot", "A small unglazed clay teapot that keeps the taste of teas from one brew to the next.",
            ),
            StopEn(
                "Seoul", "South Korea",
                "Hanok, wooden houses with curved roofs, heated through the floor by ondol.",
                "Hangul, the Korean alphabet, was created in the 15th century by King Sejong so that everyone could learn to read.",
                "Making kimchi with the family in autumn, kimjang, is on UNESCO's intangible heritage list.",
                "Hahoe mask", "A smiling wooden mask from traditional Korean theatre.",
            ),
            StopEn(
                "Kyoto", "Japan",
                "The Fushimi Inari shrine and its thousands of vermilion torii forming tunnels up the mountain.",
                "The tea ceremony, the art of preparing matcha with slow, precise gestures.",
                "Kyoto has about 1,600 Buddhist temples.",
                "Daruma", "A lucky figure: you paint one eye when you set a goal, the other when you reach it.",
            ),
            StopEn(
                "Hội An", "Vietnam",
                "The old merchant town and its Japanese covered bridge, built in the late 16th century.",
                "On full-moon evenings, the town turns off its lights and glows with lanterns.",
                "The market tailors make made-to-measure clothes in a day.",
                "Silk lantern", "A colourful lantern of silk stretched over a bamboo frame.",
            ),
            StopEn(
                "Angkor", "Cambodia",
                "Angkor Wat, a 12th-century temple, is the largest religious monument in the world.",
                "The Khmer royal ballet, with its highly codified hand gestures, is on UNESCO's intangible heritage list.",
                "Angkor Wat appears on the flag of Cambodia.",
                "Krama", "A checked cotton scarf worn by almost every Cambodian.",
            ),
            StopEn(
                "Bali", "Indonesia",
                "Temples with split gates and terraced rice fields watered by subak, a system listed by UNESCO.",
                "Every morning, small offerings of flowers and rice, canang sari, are placed in front of houses.",
                "On Nyepi, the Balinese New Year, the whole island falls silent: no lights, no work, even the airport closes.",
                "Balinese mask", "A carved and painted mask from the sacred dances, such as the Barong.",
            ),
            StopEn(
                "Sydney", "Australia",
                "The Sydney Opera House, designed by Jørn Utzon, and its sail-shaped roofs.",
                "Aboriginal peoples have lived here for more than 50,000 years; their dot paintings tell stories and paths.",
                "The Opera House roofs are covered with more than a million tiles.",
                "Boomerang", "An Aboriginal hunting tool; some kinds come back to the thrower.",
            ),
            StopEn(
                "Rotorua", "New Zealand",
                "Wharenui, Māori meeting houses carved and painted to tell the tribe's history.",
                "The haka, a Māori ritual dance, is also performed by the All Blacks before their matches.",
                "The town sits on a geothermal area: geysers and bubbling mud pools.",
                "Pounamu pendant", "Carved green jade, often given as a sign of friendship.",
            ),
            StopEn(
                "Cusco", "Peru",
                "Inca walls, made of stone blocks fitted together without mortar with astonishing precision.",
                "Quechua, the language of the Incas, is still spoken by millions of people.",
                "Machu Picchu, a few hours away, was only revealed to the world in 1911.",
                "Alpaca poncho", "Hand-woven from the soft wool of Andean alpacas.",
            ),
            StopEn(
                "Rio de Janeiro", "Brazil",
                "Christ the Redeemer, a 30-metre Art Deco statue on top of Corcovado.",
                "Carnival and its samba schools, which prepare their parade all year long.",
                "The Copacabana pavement draws waves in a mosaic of black and white stones.",
                "Pandeiro", "A Brazilian tambourine, the rhythmic heart of samba.",
            ),
            StopEn(
                "Havana", "Cuba",
                "Old Havana, its pastel colonial façades and wrought-iron balconies.",
                "Cuban son, the ancestor of salsa, can be heard in the streets and patios.",
                "American cars from the 1950s still drive there, often as taxis.",
                "Maracas", "Two rattles filled with seeds that keep the beat.",
            ),
            StopEn(
                "Mexico City", "Mexico",
                "Nearby, the Pyramid of the Sun at Teotihuacan, one of the largest in the ancient Americas.",
                "The Day of the Dead, a colourful festival in memory of the departed, is on UNESCO's intangible heritage list.",
                "The city is built on a former lake; some districts sink several centimetres a year.",
                "Alebrije", "A figure of an imaginary animal, carved and painted in bright colours.",
            ),
            StopEn(
                "New York", "United States",
                "Art Deco skyscrapers such as the Chrysler Building, and the Statue of Liberty, a gift from France in 1886.",
                "Broadway musicals, and hundreds of languages spoken in the city.",
                "The Statue of Liberty was designed by Bartholdi, with an inner frame by Gustave Eiffel.",
                "Yellow taxi", "A miniature of the famous yellow cab.",
            ),
            StopEn(
                "Reykjavik", "Iceland",
                "Hallgrímskirkja church, whose façade is inspired by the basalt columns formed by lava.",
                "The sagas, medieval Icelandic tales, are still read at school.",
                "About nine homes in ten are heated by geothermal energy.",
                "Lopapeysa", "An Icelandic wool jumper with a circular pattern around the neck.",
            ),
            StopEn(
                "Paris", "France",
                "Back under the Arc de Triomphe de l'Étoile, where twelve avenues begin.",
                "The world tour is complete, just like in Jules Verne's novel.",
                "In “Around the World in Eighty Days”, Phileas Fogg wins his bet thanks to the day gained by travelling east.",
                "Globe", "The trophy of those who have gone around the world.",
            ),
        ),
    )
}

private object TourDeFranceEn {
    val journey = JourneyEn(
        "Tour de France",
        "From Notre-Dame to the Arc de Triomphe, in 14 stops from the Alps to Brittany.",
        listOf(
            StopEn(
                "Paris", "France",
                "Notre-Dame, a Gothic cathedral begun in 1163, with its flying buttresses, two towers and rose windows. Restored after the 2019 fire, it reopened in December 2024.",
                "The bouquinistes sell old books and prints from their green boxes fixed to the banks of the Seine.",
                "In front of Notre-Dame, a plaque marks point zero of France's roads: distances to Paris are measured from there.",
                "Notre-Dame chimera", "A replica of one of the stone creatures Viollet-le-Duc added to the cathedral's gallery.",
            ),
            StopEn(
                "Reims", "France",
                "Notre-Dame cathedral, where most kings of France were crowned, and the Smiling Angel carved on its façade.",
                "Champagne ages in crayères, old chalk quarries dug beneath the city, listed by UNESCO along with the hillsides.",
                "A bottle of champagne is under a pressure of about 6 bar, roughly three times that of a car tyre.",
                "Muselet", "The little wire cage that holds the cork in; some people collect them.",
            ),
            StopEn(
                "Strasbourg", "France",
                "The pink sandstone cathedral and its single 142-metre spire, then the half-timbered houses of La Petite France, on the banks of the Ill.",
                "The Christmas market, one of the oldest in Europe, has been held there since 1570.",
                "Thanks to its spire, the cathedral was the tallest building in the world for more than two centuries, from 1647 to 1874.",
                "Kugelhopf mould", "A ring-shaped glazed earthenware mould for the Alsatian raisin brioche.",
            ),
            StopEn(
                "Chamonix", "France",
                "Wood and stone chalets with wide roofs, where the snow stays all winter like an insulating layer.",
                "The very first Winter Olympics were held there in 1924.",
                "Mont Blanc, the highest peak in the Alps, was first climbed in 1786 by Jacques Balmat and Michel Paccard.",
                "Cowbell", "The herd bell you hear ringing all summer in the mountain pastures.",
            ),
            StopEn(
                "Lyon", "France",
                "The Fourvière basilica overlooks the city from its hill; below, the traboules, covered passages, cut through buildings from one street to the next.",
                "Every 8 December, the Festival of Lights lights up the city and Lyon's people set candles on their windowsills.",
                "The Lumière brothers shot one of the first films in history there in 1895: workers leaving their factory.",
                "Guignol", "The glove puppet created in Lyon around 1808 by Laurent Mourguet.",
            ),
            StopEn(
                "Avignon", "France",
                "The Saint-Bénézet bridge, of which only four arches remain, and the Palace of the Popes, the largest Gothic palace in the world.",
                "Every July, the Avignon Festival, founded in 1947 by Jean Vilar, turns the whole city into a theatre stage.",
                "In the 14th century, the popes left Rome for Avignon: seven popes reigned there in turn.",
                "Earthenware cicada", "The emblem of Provence, a lucky charm hung on the walls of houses.",
            ),
            StopEn(
                "Marseille", "France",
                "Notre-Dame-de-la-Garde, the “Good Mother”, watches over the Old Port from its hill, topped with a golden Virgin.",
                "Pétanque was born nearby, in La Ciotat, in 1907: a player with aching legs threw his balls “feet planted”, without a run-up.",
                "Founded by Greek sailors around 600 BC, Marseille is the oldest city in France.",
                "Marseille soap", "A cube of olive oil soap, cooked in a cauldron, with a stamp on each face.",
            ),
            StopEn(
                "Carcassonne", "France",
                "La Cité, a medieval fortified town with a double wall and 52 towers, restored in the 19th century by Viollet-le-Duc.",
                "Cassoulet, a slow-cooked dish of white beans and meats, claimed by Carcassonne, Castelnaudary and Toulouse alike.",
                "Legend says Dame Carcas saved the town from a siege by throwing a wheat-fed pig over the ramparts, to make it seem the town lacked nothing.",
                "Knight's sword", "A small wooden sword, like those of the children playing on the ramparts.",
            ),
            StopEn(
                "Biarritz", "France",
                "The lighthouse has overlooked the ocean since 1834, and the Rocher de la Vierge juts into the waves at the end of an iron footbridge.",
                "Basque pelota is played bare-handed or with a chistera against the fronton wall, at the heart of every village.",
                "Surfing arrived in Europe through Biarritz, in 1957.",
                "Espadrilles", "Canvas shoes with rope soles, made notably in Mauléon, in the Basque Country.",
            ),
            StopEn(
                "Bordeaux", "France",
                "The 18th-century Place de la Bourse is reflected in the water mirror, one of the largest in the world.",
                "The city is surrounded by famous vineyards: Saint-Émilion, Médoc, Pessac, Sauternes…",
                "It is called the Port of the Moon, after the crescent the Garonne draws as it crosses the city.",
                "Canelé mould", "A small fluted copper mould for the Bordeaux cake, crisp outside and soft inside.",
            ),
            StopEn(
                "Chambord", "France",
                "The château built for Francis I, with its 282 chimneys and double-helix staircase, perhaps designed by Leonardo da Vinci.",
                "The Loire Valley, its châteaux and gardens are on UNESCO's World Heritage list.",
                "On the double-helix staircase, two people can go up and down at the same time without ever meeting.",
                "Château key", "A big wrought-iron key, like those of the château's doors.",
            ),
            StopEn(
                "Carnac", "France",
                "Nearly 3,000 standing stones set up in long rows about 6,000 years ago.",
                "The fest-noz, a Breton night festival where people dance in a chain to songs and bombardes, is on UNESCO's intangible heritage list.",
                "A legend tells that these menhirs are Roman soldiers turned to stone by Saint Cornelius.",
                "Breton striped top", "The blue and white striped knit of Breton sailors, adopted by the Navy in 1858.",
            ),
            StopEn(
                "Mont-Saint-Michel", "France",
                "The Benedictine abbey rises on top of a rocky islet, crowned with a golden statue of the Archangel Michael.",
                "La Mère Poulard's omelette, whisked in rhythm and cooked over a wood fire, has been served there since 1888.",
                "The tides there are among the strongest in Europe: the sea rises, they say, “as fast as a galloping horse”.",
                "Pilgrim badge", "A small medal that medieval pilgrims sewed onto their hats.",
            ),
            StopEn(
                "Lille", "France",
                "The 104-metre belfry of the town hall, listed by UNESCO with the other belfries of northern France and Belgium.",
                "The Braderie, on the first weekend of September, is one of the largest flea markets in Europe; people eat mussels and chips.",
                "In the festivals of the North, giants parade: figures several metres tall carried by men hidden underneath.",
                "Giant figurine", "Lydéric or Phinaert, the giants of the legend of Lille's founding.",
            ),
            StopEn(
                "Paris", "France",
                "The Arc de Triomphe de l'Étoile, at the top of the Champs-Élysées, where the Tour de France cycle race traditionally finishes.",
                "The Tour de France began in 1903; the yellow jersey, introduced in 1919, has the colour of the pages of L'Auto, the newspaper that ran it.",
                "Under the arch, the flame of the Unknown Soldier has burned since 1923, rekindled every evening.",
                "Yellow jersey", "The trophy of those who have completed the tour of France.",
            ),
        ),
    )
}
