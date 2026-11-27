/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.domain

/** Le deuxième lot de tours d'Europe en anglais (voir [EuropeTours2]). */
object EuropeTours2En {
    val all: Map<String, JourneyEn> by lazy {
        mapOf("pays-bas" to netherlands, "belgique" to belgium, "suisse" to switzerland, "autriche" to austria, "grece" to greece)
    }

    private val netherlands = JourneyEn(
        "Netherlands",
        "From the canals of Amsterdam to Giethoorn, the village without roads, by way of tulips and windmills.",
        listOf(
            StopEn(
                "Amsterdam", "Netherlands",
                "Narrow gabled houses line the 17th-century canals, listed by UNESCO; many lean slightly forward.",
                "The bicycle is king: the city has more bikes than inhabitants.",
                "Under the roof of the houses, a hook is used to hoist furniture through the windows, because the stairs are too steep.",
                "Clogs", "Painted wooden shoes, once worn in the wet fields.",
            ),
            StopEn(
                "Zaanse Schans", "Netherlands",
                "Wooden windmills and green houses, witnesses to one of the first industrial regions in Europe.",
                "The mills ground spices, pressed oil and milled pigments for painters.",
                "The Zaan region once had as many as 600 windmills.",
                "Miniature windmill", "A small wooden windmill whose sails turn at the slightest breath.",
            ),
            StopEn(
                "Keukenhof", "Netherlands",
                "A 32-hectare park where about seven million bulbs bloom every spring.",
                "The tulip, brought from the Ottoman Empire, became the emblem of the country.",
                "Around 1637, a rare tulip bulb could be worth the price of a house: this was “tulip mania”.",
                "Tulip bulb", "Plant it in autumn for a flower in spring.",
            ),
            StopEn(
                "Delft", "Netherlands",
                "The New Church and its 108-metre bell tower overlook the market square and the town hall.",
                "Delft blue earthenware, inspired by Chinese porcelain, has been painted by hand there since the 17th century.",
                "The painter Vermeer was born there and painted “Girl with a Pearl Earring” there.",
                "Delft tile", "A blue and white earthenware tile painted with a windmill.",
            ),
            StopEn(
                "Rotterdam", "Netherlands",
                "The cube houses, tilted at 45 degrees on pillars, and the Erasmus Bridge, nicknamed “the Swan”.",
                "Its port is the largest in Europe.",
                "Bombed in 1940, the centre was rebuilt as a true showcase of modern architecture.",
                "Cube house", "A model of a cube house standing on its tip.",
            ),
            StopEn(
                "Kinderdijk", "Netherlands",
                "Nineteen 18th-century windmills lined up along the canals to pump water out of the polders.",
                "A quarter of the country lies below sea level: dykes and pumps keep it dry.",
                "Kinderdijk means “child's dyke”: legend says a cradle washed up there after a flood in 1421, with a cat on its edge.",
                "Wheel of Gouda", "The cheese from the nearby town of Gouda.",
            ),
            StopEn(
                "Utrecht", "Netherlands",
                "The Dom Tower, 112 metres tall, the tallest church tower in the Netherlands.",
                "Miffy, the little rabbit with round eyes and a cross-shaped mouth, was born here from Dick Bruna's pencil.",
                "A tornado brought down the cathedral's nave in 1674: since then, the tower has stood apart from the rest of the church.",
                "Miffy plush", "Utrecht's little white rabbit.",
            ),
            StopEn(
                "Hoge Veluwe", "Netherlands",
                "In the middle of the park, the Kröller-Müller Museum holds nearly 90 paintings by Van Gogh.",
                "You cross the park on white bicycles lent for free.",
                "Heathland, sand dunes and forests: you may come across deer and wild boar.",
                "Van Gogh print", "Sunflowers in brilliant colours.",
            ),
            StopEn(
                "Giethoorn", "Netherlands",
                "Thatched farmhouses linked by more than 170 small wooden bridges.",
                "In the old village there are no roads: people get around by boat or canoe.",
                "Its name is said to come from the horns of wild goats (“geitenhoorns”) found by the first inhabitants.",
                "Giethoorn boat", "The trophy of those who have travelled across the Netherlands.",
            ),
        ),
    )

    private val belgium = JourneyEn(
        "Belgium",
        "From the Atomium to the forests of the Ardennes, through Bruges, Ghent and the Meuse.",
        listOf(
            StopEn(
                "Brussels", "Belgium",
                "The Grand-Place and its gilded guild houses, and the Atomium, a giant steel molecule built for the 1958 World's Fair.",
                "Comics are everywhere: Tintin, Spirou and the Smurfs were born in Belgium, and murals of them cover the walls.",
                "Manneken Pis owns more than 1,000 costumes, which he is dressed in on festival days.",
                "Tintin album", "The adventures of the quiffed reporter and his dog Snowy.",
            ),
            StopEn(
                "Antwerp", "Belgium",
                "The central station, nicknamed the “railway cathedral”, under a vast dome of iron and glass.",
                "The city is the world capital of diamonds: most of the world's rough diamonds pass through it.",
                "The painter Rubens lived there; his house and studio can still be visited.",
                "Diamond", "A stone cut in Antwerp's workshops.",
            ),
            StopEn(
                "Ghent", "Belgium",
                "The Castle of the Counts, a 12th-century fortress in the heart of the city, and the gabled houses of the Graslei.",
                "The Ghent Altarpiece by the Van Eyck brothers, a masterpiece of Flemish painting, is on display in St Bavo's Cathedral.",
                "One of the panels of the Ghent Altarpiece, stolen in 1934, has never been found.",
                "Cuberdons", "Purple cone-shaped sweets with a raspberry syrup centre.",
            ),
            StopEn(
                "Bruges", "Belgium",
                "The 83-metre belfry overlooks the market square and canals lined with medieval houses.",
                "Bobbin lace has been made by hand there since the 15th century.",
                "You must climb 366 steps to reach the top of the belfry.",
                "Lace doily", "Fine lace made with bobbins.",
            ),
            StopEn(
                "Ypres", "Belgium",
                "The Gothic Cloth Hall, rebuilt exactly as it was after its destruction in the First World War.",
                "Every evening at 8 pm, under the Menin Gate, buglers sound the Last Post in memory of the soldiers, as they have since 1928.",
                "Every three years, the Cat Parade recalls that cats were once thrown from the top of the belfry; today, they are stuffed toys.",
                "Poppy", "The flower of remembrance for the soldiers of the Great War.",
            ),
            StopEn(
                "Waterloo", "Belgium",
                "The Lion's Mound, an artificial hill over 40 metres high topped with a cast-iron lion, overlooks the battlefield.",
                "Thousands of costumed extras regularly re-enact the battle of 1815 there.",
                "The mound took about 300,000 cubic metres of earth, carried in baskets on the backs of women, the “hotteuses”.",
                "Tin soldier", "A hand-painted grenadier from 1815.",
            ),
            StopEn(
                "Dinant", "Belgium",
                "The collegiate church with its onion-domed bell tower, set against the cliff below the citadel, on the banks of the Meuse.",
                "Adolphe Sax, the inventor of the saxophone, was born there in 1814.",
                "The couque de Dinant, a very hard honey biscuit, is baked in carved wooden moulds.",
                "Saxophone", "The instrument invented by the town's own son.",
            ),
            StopEn(
                "Liège", "Belgium",
                "Guillemins station, a white vault of glass and steel, and the Montagne de Bueren, a staircase of 374 steps.",
                "The Liège waffle, with pearl sugar, is eaten hot in the street.",
                "On 15 August, the Outremeuse district celebrates its “Free Republic” around Tchantchès, its puppet.",
                "Tchantchès", "The puppet with the battered nose, Liège's folk hero.",
            ),
            StopEn(
                "Spa", "Belgium",
                "Baths and fountains, the pouhons, where you taste the iron-rich spring water, amid the forests of the Ardennes.",
                "The nearby Spa-Francorchamps circuit hosts the Belgian Formula 1 Grand Prix.",
                "The word “spa”, used in many languages for baths, comes from the name of the town.",
                "Box of pralines", "The trophy of those who have travelled across Belgium: filled chocolates invented in Brussels in 1912.",
            ),
        ),
    )

    private val switzerland = JourneyEn(
        "Switzerland",
        "From the Geneva water jet to the painted façades of Appenzell, by way of the Matterhorn and the Jungfrau.",
        listOf(
            StopEn(
                "Geneva", "Switzerland",
                "The water jet on Lake Geneva shoots water up to 140 metres high.",
                "The city hosts the European headquarters of the United Nations and the Red Cross, founded here in 1863.",
                "The flower clock in the English Garden is made of thousands of plants, renewed through the seasons.",
                "Swiss watch", "A mechanical watch, the pride of Geneva's watchmakers.",
            ),
            StopEn(
                "Lavaux", "Switzerland",
                "Terraced vineyards run down to Lake Geneva, held up by stone walls and listed by UNESCO.",
                "Chasselas, a white grape, makes a light wine that people like to drink with fondue.",
                "It is said the vines enjoy “three suns”: the one in the sky, its reflection on the lake and the warmth of the walls.",
                "Fondue pot", "The earthenware pot where the cheese melts.",
            ),
            StopEn(
                "Montreux", "Switzerland",
                "Chillon Castle, set on a rock at the edge of the lake, one of the most visited monuments in Switzerland.",
                "The Montreux Jazz Festival, founded in 1967, welcomes the greatest musicians every summer.",
                "The poet Lord Byron carved his name on a pillar in the castle's dungeons.",
                "Jazz record", "A concert recorded at the festival.",
            ),
            StopEn(
                "Zermatt", "Switzerland",
                "The Matterhorn, a 4,478-metre pyramid, towers over larch chalets darkened by time.",
                "The village is car-free: people get around by electric taxi and horse-drawn carriage.",
                "The Matterhorn was first climbed in 1865; four of the seven climbers died on the way down.",
                "Swiss Army knife", "The red knife with a thousand tools.",
            ),
            StopEn(
                "Jungfraujoch", "Switzerland",
                "The highest railway station in Europe, at 3,454 metres, reached by a rack railway that runs through the mountain.",
                "The alphorn, a long wooden tube, was once used to call the herds.",
                "From the top, you can see the Aletsch Glacier, the longest in the Alps at about 20 km.",
                "Alphorn", "A miniature of the shepherds' long wooden horn.",
            ),
            StopEn(
                "Bern", "Switzerland",
                "The clock tower, the Zytglogge, and six kilometres of covered arcades in the old town.",
                "In summer, the people of Bern swim in the Aare, letting the current carry them along.",
                "Albert Einstein came up with special relativity there in 1905, while working at the patent office.",
                "Teddy bear", "The bear, the city's emblem.",
            ),
            StopEn(
                "Lucerne", "Switzerland",
                "The Chapel Bridge, a 14th-century covered wooden bridge, and its octagonal tower in the middle of the Reuss.",
                "Lucerne's carnival and its wild brass bands, the Guggenmusik.",
                "Largely destroyed by fire in 1993, the bridge was rebuilt in a year.",
                "Painted panel", "A copy of the triangular paintings hung under the bridge's roof.",
            ),
            StopEn(
                "Zurich", "Switzerland",
                "The two towers of the Grossmünster rise on the banks of the Limmat.",
                "The great Swiss chocolate makers, such as Sprüngli, have their roots there.",
                "The Dada movement was born in Zurich in 1916, at the Cabaret Voltaire.",
                "Chocolate bar", "Swiss milk chocolate.",
            ),
            StopEn(
                "Appenzell", "Switzerland",
                "Houses with wooden façades painted in colourful patterns, at the foot of the Alpstein massif.",
                "Every spring, at the Landsgemeinde, citizens vote by show of hands in the square.",
                "The canton of Appenzell Innerrhoden was the last in Switzerland to give women the vote, in 1990.",
                "Appenzeller cheese", "The trophy of those who have travelled across Switzerland: a wheel rubbed with a herbal brine to a secret recipe.",
            ),
        ),
    )

    private val austria = JourneyEn(
        "Austria",
        "From Schönbrunn to Lake Neusiedl, along the Danube, through Salzburg and the Tyrolean Alps.",
        listOf(
            StopEn(
                "Vienna", "Austria",
                "Schönbrunn Palace, the Habsburgs' summer residence, and its 1,441 rooms.",
                "Viennese cafés are an institution: you can stay for hours with a coffee and a glass of water.",
                "Schönbrunn Zoo, opened in 1752, is the oldest zoo still operating in the world.",
                "Sachertorte box", "The wooden box of the chocolate cake created in 1832.",
            ),
            StopEn(
                "Melk", "Austria",
                "The yellow and white Baroque Benedictine abbey, perched above the Danube.",
                "In the Wachau valley, apricots and Grüner Veltliner, the local white wine, are grown.",
                "Umberto Eco named the narrator of “The Name of the Rose” “Adso of Melk”.",
                "Old book", "Like those in the abbey's Baroque library.",
            ),
            StopEn(
                "Linz", "Austria",
                "The Ars Electronica Center, a museum of the future whose glass façade lights up in colours at night.",
                "The Linzer Torte, with jam and almond pastry, is said to be the oldest cake whose recipe has been kept.",
                "The astronomer Johannes Kepler lived there and published one of his laws of planetary motion there.",
                "Linzer Torte", "The lattice tart with redcurrant jam.",
            ),
            StopEn(
                "Hallstatt", "Austria",
                "Wooden houses squeezed between the lake and the mountain, around a pointed bell tower.",
                "Salt has been mined there for more than 7,000 years: it is said to be the oldest salt mine in the world.",
                "As the cemetery was short of space, more than 600 skulls were painted with flowers and names in the ossuary.",
                "Hallstatt salt", "A little bag of salt from the mountain.",
            ),
            StopEn(
                "Salzburg", "Austria",
                "Hohensalzburg Fortress, one of the largest medieval fortresses in Europe, above the Baroque bell towers.",
                "Mozart was born there in 1756; the Salzburg Festival keeps his music alive every summer.",
                "The film “The Sound of Music” was shot in and around the city in 1964.",
                "Mozart score", "A page from “The Magic Flute”.",
            ),
            StopEn(
                "Innsbruck", "Austria",
                "The Golden Roof, a balcony covered with 2,657 gilded copper tiles, and the Bergisel ski jump, redesigned by Zaha Hadid.",
                "The city has hosted the Winter Olympics twice, in 1964 and 1976.",
                "A funicular takes you from the city centre to the peaks of the Nordkette in about twenty minutes.",
                "Cut crystal", "A small crystal figure from the Inn valley.",
            ),
            StopEn(
                "Grossglockner", "Austria",
                "The Grossglockner High Alpine Road, 48 kilometres and 36 hairpins up to 2,504 metres, at the foot of Austria's highest peak.",
                "Marmots whistle to warn their neighbours of danger.",
                "The Pasterze glacier, at the foot of the peak, retreats by several dozen metres a year.",
                "Edelweiss", "The white, downy flower of the high pastures.",
            ),
            StopEn(
                "Graz", "Austria",
                "The clock tower on the Schlossberg, and the Kunsthaus, a museum nicknamed “the friendly alien”.",
                "Dark green pumpkin seed oil dresses salads and even ice cream.",
                "On the clock, the big hand shows the hours and the small one the minutes, so it can be read from afar.",
                "Pumpkin seed oil", "A bottle of Styria's dark green oil.",
            ),
            StopEn(
                "Lake Neusiedl", "Austria",
                "A shallow steppe lake, fringed with reeds and shared with Hungary.",
                "Storks nest on the village chimneys, as in Rust.",
                "The lake is only about 1.8 metres deep; it even dried up completely in the 19th century.",
                "Wooden stork", "The trophy of those who have travelled across Austria.",
            ),
        ),
    )

    private val greece = JourneyEn(
        "Greece",
        "From the Acropolis to Santorini, by way of Olympia, Delphi and the Meteora.",
        listOf(
            StopEn(
                "Athens", "Greece",
                "The Plaka, the oldest district of the city, and its lanes at the foot of the Acropolis.",
                "The changing of the evzones, guards in kilts with 400 pleats, in front of Parliament.",
                "The first modern Olympic Games were held there in 1896, in the all-marble Panathenaic Stadium.",
                "Tsarouchi", "The pompom shoe of the evzones.",
            ),
            StopEn(
                "Corinth", "Greece",
                "The Corinth Canal, cut through the rock in 1893: more than 6 km long, but only 25 metres wide.",
                "Corinth's raisins gave their name to the English word “currant”.",
                "Emperor Nero had already started digging the canal in the year 67, with a golden shovel.",
                "Corinth currants", "A little bag of small black raisins.",
            ),
            StopEn(
                "Mycenae", "Greece",
                "The Lion Gate, carved more than 3,000 years ago, and walls made of enormous blocks.",
                "In Homer's poems, Agamemnon is the king of Mycenae.",
                "These walls are called “Cyclopean”: the Greeks thought only giants, the Cyclopes, could have built them.",
                "Mask of Agamemnon", "A replica of the gold mask found in a royal tomb.",
            ),
            StopEn(
                "Olympia", "Greece",
                "The ruins of the Temple of Zeus and the stadium where the athletes of antiquity ran.",
                "The Olympic flame is lit there before every Games, with the sun's rays and a mirror.",
                "Winners did not receive a medal, but an olive wreath.",
                "Olive wreath", "The prize of the champions of antiquity.",
            ),
            StopEn(
                "Delphi", "Greece",
                "The sanctuary of Apollo rises in tiers on the slopes of Parnassus, with its theatre and the tholos, a round temple.",
                "The Pythia delivered Apollo's oracles there, consulted by cities from all over the Greek world.",
                "The Greeks saw Delphi as the centre of the world, marked by a stone, the omphalos, the “navel”.",
                "Omphalos", "A replica of the stone at the navel of the world.",
            ),
            StopEn(
                "Meteora", "Greece",
                "Monasteries perched on top of rock pillars several hundred metres high.",
                "Monks and nuns still live in six of these monasteries.",
                "Until the 1920s, people went up in a net hoisted by a rope and a winch.",
                "Painted icon", "A small holy image painted on wood.",
            ),
            StopEn(
                "Mount Olympus", "Greece",
                "The highest peak in Greece, at 2,918 metres, home of the gods in mythology.",
                "The twelve gods of Olympus, led by Zeus, watched over the lives of mortals.",
                "Its highest summit, Mytikas, was only climbed in 1913.",
                "Zeus's thunderbolt", "The weapon of the king of the gods, in gilded wood.",
            ),
            StopEn(
                "Thessaloniki", "Greece",
                "The White Tower, on the seafront, the symbol of the city.",
                "Bougatsa, a pastry filled with custard or cheese, is eaten for breakfast.",
                "Alexander the Great was born nearby, at Pella, in 356 BC.",
                "Alexander coin", "A replica of a silver drachma bearing Alexander's likeness.",
            ),
            StopEn(
                "Santorini", "Greece",
                "White houses and blue domes clinging to the edge of a caldera, a volcanic crater flooded by the sea.",
                "The vines there are coiled into baskets close to the ground to protect them from the wind.",
                "A gigantic eruption, around 1600 BC, may have inspired the myth of Atlantis.",
                "Blue dome", "The trophy of those who have travelled across Greece.",
            ),
        ),
    )
}
