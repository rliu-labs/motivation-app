/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.domain

/** Les premiers tours d'Europe en anglais (voir [EuropeTours]). */
object EuropeToursEn {
    val all: Map<String, JourneyEn> by lazy {
        mapOf("italie" to italy, "espagne" to spain, "portugal" to portugal, "royaume-uni" to uk, "allemagne" to germany)
    }

    private val italy = JourneyEn(
        "Italy",
        "From Venice to the trulli of Apulia, by way of Florence, Rome and Pompeii.",
        listOf(
            StopEn(
                "Venice", "Italy",
                "More than 400 bridges span some 150 canals; the city rests on millions of wooden piles driven into the lagoon.",
                "Carnival and its masks, and the gondoliers who have passed on their trade for centuries.",
                "A gondola is deliberately lopsided: one side is wider than the other to balance the weight of the gondolier, who rows on one side only.",
                "Venetian mask", "A gilded papier-mâché mask worn during Carnival.",
            ),
            StopEn(
                "Milan", "Italy",
                "The Duomo, a white marble Gothic cathedral bristling with 135 spires and more than 3,000 statues.",
                "La Scala, one of the most famous opera houses in the world, opened in 1778.",
                "Leonardo da Vinci's Last Supper is painted on the refectory wall of a convent in the city; you must book well ahead to see it.",
                "Opera score", "An aria by Verdi, several of whose operas premiered at La Scala.",
            ),
            StopEn(
                "Cinque Terre", "Italy",
                "Five villages of colourful houses clinging to the cliffs, linked by clifftop paths above the sea.",
                "Vines grow on terraces held up by dry-stone walls, built by hand over the centuries.",
                "It is said the houses were painted bright colours so that fishermen could spot their own from the sea.",
                "Pesto mortar", "A marble mortar and wooden pestle for pounding Ligurian basil.",
            ),
            StopEn(
                "Pisa", "Italy",
                "The Leaning Tower, the cathedral's bell tower begun in 1173; it started tilting from the first floors, on ground that was too soft.",
                "The Square of Miracles brings together a cathedral, baptistery, tower and cemetery of white marble on a lawn.",
                "Work finished in 2001 straightened it by about forty centimetres: it still leans, but is no longer at risk of falling.",
                "Galileo's telescope", "Galileo was born in Pisa; he is said to have dropped balls from the top of the tower to study how they fall.",
            ),
            StopEn(
                "Florence", "Italy",
                "The Ponte Vecchio, lined with goldsmiths' shops, and Brunelleschi's dome, raised in the 15th century with no scaffolding frame to support it.",
                "Cradle of the Renaissance: Michelangelo's David and Botticelli's paintings are on display here.",
                "The Ponte Vecchio is the only bridge in Florence that German troops did not destroy in 1944.",
                "Marbled paper", "Sheets decorated by hand with coloured swirls, a Florentine speciality.",
            ),
            StopEn(
                "Siena", "Italy",
                "The shell-shaped Piazza del Campo and the Torre del Mangia, nearly 90 metres tall.",
                "The Palio: twice each summer, ten horses ridden bareback race three laps of the square.",
                "The race lasts only about 90 seconds, and a horse can win even after losing its rider.",
                "Contrada scarf", "In the colours of one of the city's 17 districts that compete in the Palio.",
            ),
            StopEn(
                "Rome", "Italy",
                "The Colosseum, an amphitheatre opened in the year 80, could hold about 50,000 spectators.",
                "Carbonara and cacio e pepe, Roman pasta dishes made with a few well-chosen ingredients.",
                "Rome surrounds an entire country: the Vatican, the smallest state in the world.",
                "Sestertius", "A replica of a coin of the Roman Empire.",
            ),
            StopEn(
                "Pompeii", "Italy",
                "A Roman town frozen by the eruption of Vesuvius in 79: paved streets, houses, frescoes and bakeries.",
                "Nearby Naples invented pizza; the art of the Neapolitan pizzaiuolo is on UNESCO's intangible heritage list.",
                "Its amphitheatre is the oldest surviving stone Roman amphitheatre.",
                "Cornicello", "A small red horn that wards off bad luck, according to Neapolitan tradition.",
            ),
            StopEn(
                "Matera", "Italy",
                "The Sassi, districts of houses and churches carved into the rock, inhabited since prehistoric times.",
                "In the past, each family marked its bread with its own stamp before baking it in the shared oven.",
                "Its alleys have served as the setting for several films set in Jerusalem, including “The Passion of the Christ”.",
                "Bread stamp", "A carved wooden stamp with the family's initials.",
            ),
            StopEn(
                "Alberobello", "Italy",
                "The trulli, white dry-stone houses topped with conical roofs, built without mortar.",
                "Apulia produces a large share of Italy's olive oil, under olive trees that are sometimes a thousand years old.",
                "They were built without mortar, it is said, so they could be taken down quickly to escape the tax on houses.",
                "Miniature trullo", "The trophy of those who have travelled across Italy.",
            ),
        ),
    )

    private val spain = JourneyEn(
        "Spain",
        "From the Sagrada Família to Santiago de Compostela, through Andalusia and Madrid.",
        listOf(
            StopEn(
                "Barcelona", "Spain",
                "Gaudí's Sagrada Família, under construction since 1882, whose towers rise above the city.",
                "Castells, human towers several storeys high, are on UNESCO's intangible heritage list.",
                "Gaudí wanted the tallest tower to stay a little lower than Montjuïc hill, so as not to surpass the work of God.",
                "Mosaic lizard", "A replica of the dragon in Park Güell, covered in ceramic fragments.",
            ),
            StopEn(
                "Valencia", "Spain",
                "The City of Arts and Sciences, white buildings shaped like an eye and a skeleton, in the old bed of the Turia river.",
                "The Fallas: in March, huge cardboard statues are burned in the streets; the festival is listed by UNESCO.",
                "Paella comes from here: its name is that of the wide, flat pan it is cooked in.",
                "Painted fan", "A hand-decorated fan, a Valencian tradition.",
            ),
            StopEn(
                "Granada", "Spain",
                "The Alhambra, palace and fortress of the Nasrid rulers, with halls decorated in stucco as fine as lace.",
                "In bars, a tapa is often offered with every drink you order.",
                "Alhambra comes from the Arabic for “the red one”, after the colour of its walls.",
                "Ceramic pomegranate", "The fruit that gave the city its name, painted on the blue and green Fajalauza pottery.",
            ),
            StopEn(
                "Seville", "Spain",
                "The Giralda, a former minaret turned bell tower of the cathedral, the largest Gothic cathedral in the world.",
                "Flamenco, song, dance and guitar, is on UNESCO's intangible heritage list.",
                "You climb the Giralda by ramps rather than steps, so that it could be ridden up on horseback.",
                "Castanets", "Two wooden shells that click to the rhythm of flamenco.",
            ),
            StopEn(
                "Córdoba", "Spain",
                "The Mezquita, a mosque-cathedral with its forest of more than 800 columns holding red and white striped arches.",
                "In May, the Patio Festival: residents open their flower-filled courtyards to visitors.",
                "In the 16th century, a cathedral was built in the middle of the mosque, without destroying it entirely.",
                "Blue flowerpot", "Like those covering the white walls of the patios.",
            ),
            StopEn(
                "Madrid", "Spain",
                "The Royal Palace, with more than 3,000 rooms, one of the largest palaces in Europe.",
                "The Prado Museum brings together Velázquez, Goya and El Greco.",
                "On the Puerta del Sol, a plaque marks kilometre zero of Spain's roads.",
                "Chocolate cup", "For dipping churros in thick hot chocolate.",
            ),
            StopEn(
                "Segovia", "Spain",
                "The Roman aqueduct, 167 granite arches assembled without mortar, which still supplied the city in the 20th century.",
                "Roast suckling pig, so tender it is cut with the edge of a plate.",
                "The Alcázar, a castle with pointed towers perched on a rock, is said to have inspired the castles in Disney cartoons.",
                "Miniature arch", "A small granite arch, a replica of those of the aqueduct.",
            ),
            StopEn(
                "Bilbao", "Spain",
                "Frank Gehry's Guggenheim Museum, with titanium-clad curves, opened in 1997.",
                "Pintxos, small bites held by a toothpick, are eaten standing at the counter.",
                "In front of the museum, Puppy, a giant puppy over 12 metres tall covered in flowers, changes colour with the seasons.",
                "Txapela", "The beret worn in the Basque Country.",
            ),
            StopEn(
                "Santiago de Compostela", "Spain",
                "The Romanesque cathedral at the end of the Way of St James, behind its Baroque Obradoiro façade.",
                "During some masses, the botafumeiro, a censer of about 50 kilos, swings from one end of the transept to the other.",
                "Pilgrims who have walked at least 100 km receive the Compostela, a pilgrimage certificate.",
                "Scallop shell", "The pilgrims' trophy, hung on the backpack all along the way.",
            ),
        ),
    )

    private val portugal = JourneyEn(
        "Portugal",
        "From the cliffs of the Algarve to the vineyards of the Douro, through Lisbon and Porto.",
        listOf(
            StopEn(
                "Lagos", "Portugal",
                "The golden cliffs of Ponta da Piedade, hollowed into arches and caves, and their little lighthouse.",
                "The Algarve was a starting point for the great voyages of the 15th century, in the time of Henry the Navigator.",
                "Fishermen take visitors by boat into the caves, which can only be reached from the sea.",
                "Cataplana", "A shell-shaped copper pot for cooking fish and seafood.",
            ),
            StopEn(
                "Évora", "Portugal",
                "A Roman temple with Corinthian columns, at the heart of an old town of whitewashed houses.",
                "Portugal produces about half of the world's cork; a cork oak is stripped only every nine years.",
                "The Chapel of Bones has its walls lined with bones, with an inscription inviting you to reflect on the passing of time.",
                "Cork bag", "A small cork bag, soft and light as fabric.",
            ),
            StopEn(
                "Lisbon", "Portugal",
                "Façades covered in azulejos, painted earthenware tiles, and the Belém Tower on the banks of the Tagus.",
                "Fado, a melancholy song accompanied by the Portuguese guitar, is on UNESCO's intangible heritage list.",
                "Tram 28, yellow and wooden, climbs streets so narrow that it brushes the façades on the bends.",
                "Azulejo", "A hand-painted blue and white earthenware tile.",
            ),
            StopEn(
                "Sintra", "Portugal",
                "The Pena Palace, with yellow and red towers, perched on a forested hill.",
                "Travesseiros, almond cream pastries, have been enjoyed here for generations.",
                "The poet Lord Byron described Sintra as a “glorious Eden”.",
                "Forest lantern", "For following the misty trails of the Sintra hills.",
            ),
            StopEn(
                "Óbidos", "Portugal",
                "A small town entirely surrounded by medieval walls, which you can walk all the way around.",
                "Ginja, a sour cherry liqueur, is drunk from a small chocolate cup.",
                "In 1282, King Denis gave the town to Queen Isabel as a wedding present; his successors long did the same.",
                "Chocolate cup", "To taste the ginja, then eat the cup.",
            ),
            StopEn(
                "Nazaré", "Portugal",
                "The Fort of St Michael the Archangel and its red lighthouse, on a headland above the ocean.",
                "The fishermen's colourful boats were once pulled up the beach by oxen.",
                "Its waves are among the biggest ever surfed, over 25 metres, thanks to an underwater canyon.",
                "Painted boat", "A miniature of the fishing boats with eyes painted on the bow.",
            ),
            StopEn(
                "Coimbra", "Portugal",
                "The tower of one of the oldest universities in Europe and the Joanina Library, all in gilded wood.",
                "Students wear a black cape and sing Coimbra fado, with a guitar, beneath windows.",
                "Bats live in the Joanina Library: at night, they eat the insects that would damage the books.",
                "Student cape", "The long black cape of Coimbra's students.",
            ),
            StopEn(
                "Porto", "Portugal",
                "The Ribeira district, with colourful houses huddled on the Douro, and the iron-arched Dom Luís bridge.",
                "Port wine ages in the cellars of Vila Nova de Gaia, on the other bank.",
                "Locals are nicknamed “tripeiros”, tripe eaters: they are said to have given their meat to the ships that sailed in 1415.",
                "Rooster of Barcelos", "The colourful ceramic rooster, symbol of Portugal, born of a legend from the north of the country.",
            ),
            StopEn(
                "Douro Valley", "Portugal",
                "Terraced vineyards sculpt the hills above the river, on UNESCO's World Heritage list.",
                "The harvest is done by hand; grapes are still sometimes trodden underfoot in stone tanks, the lagares.",
                "Rabelo boats once carried the wine barrels down the river to Porto.",
                "Bottle of port", "The trophy of those who have travelled across Portugal.",
            ),
        ),
    )

    private val uk = JourneyEn(
        "United Kingdom",
        "From Big Ben to the Giant's Causeway, via Stonehenge, Scotland and Wales.",
        listOf(
            StopEn(
                "London", "England",
                "The Elizabeth Tower and its clock, at the end of the Palace of Westminster, on the banks of the Thames.",
                "Afternoon tea, with scones, clotted cream and jam.",
                "“Big Ben” is not the name of the tower but of its great bell, which weighs more than 13 tonnes.",
                "Double-decker bus", "A miniature of the red two-storey bus.",
            ),
            StopEn(
                "Oxford", "England",
                "The Radcliffe Camera, a domed rotunda, among the “dreaming spires” of the colleges.",
                "The oldest university in the English-speaking world: teaching was already taking place there in the late 11th century.",
                "Lewis Carroll taught mathematics there; he invented “Alice's Adventures in Wonderland” for the daughter of his college's dean.",
                "Alice in Wonderland", "An illustrated edition of the tale born on a rowing boat, on Oxford's river.",
            ),
            StopEn(
                "Stonehenge", "England",
                "A circle of giant stones raised about 4,500 years ago; the smallest come from Wales, more than 200 km away.",
                "At the summer solstice, thousands of people come to watch the sun rise in line with the stones.",
                "We still do not know for sure what the monument was for: temple, calendar or burial site.",
                "Sundial", "A pocket sundial, to tell the time by the sun as before clocks.",
            ),
            StopEn(
                "Bath", "England",
                "The Roman baths, built around a hot spring, and the Royal Crescent, an arc of 30 Georgian houses.",
                "Jane Austen lived there and set two of her novels there.",
                "The spring water comes out at about 46 °C, as in Roman times.",
                "Porcelain cup", "For tea, with its flowered saucer.",
            ),
            StopEn(
                "Snowdonia", "Wales",
                "Edward I's castles, such as Caernarfon and Conwy, guard the valleys at the foot of Snowdon.",
                "Welsh, a Celtic language, is spoken by hundreds of thousands of people; road signs are bilingual.",
                "A village on nearby Anglesey has a 58-letter name, one of the longest in the world.",
                "Lovespoon", "A carved wooden spoon once given to one's sweetheart.",
            ),
            StopEn(
                "Liverpool", "England",
                "The Royal Liver Building, on the waterfront, topped by two copper birds, the Liver Birds.",
                "The Beatles got their start at the Cavern Club in the early 1960s.",
                "Legend says that if the Liver Birds flew away, the city would cease to exist.",
                "Vinyl record", "A Beatles single.",
            ),
            StopEn(
                "York", "England",
                "York Minster, one of the largest Gothic cathedrals in northern Europe, and its east window, as big as a tennis court.",
                "The Shambles, a medieval lane whose overhanging houses almost touch.",
                "Founded by the Romans, the city became Jorvik, a Viking capital.",
                "Little longship", "A miniature of the Viking ship of Jorvik.",
            ),
            StopEn(
                "Edinburgh", "Scotland",
                "The castle, on an old volcano, overlooks the Royal Mile running down to the Palace of Holyroodhouse.",
                "In August, the Fringe, the largest arts festival in the world, fills every venue in the city.",
                "Every day except Sunday, a cannon is fired from the castle at 1 pm.",
                "Tartan kilt", "The Scottish skirt in a clan's check.",
            ),
            StopEn(
                "Loch Ness", "Scotland",
                "The ruins of Urquhart Castle, on the shore of a loch 37 km long.",
                "Bagpipes accompany festivals and the Highland Games, where tree trunks are tossed.",
                "The loch holds more fresh water than all the lakes of England and Wales combined.",
                "Nessie plush", "The loch's monster, searched for since 1933.",
            ),
            StopEn(
                "Giant's Causeway", "Northern Ireland",
                "About 40,000 basalt columns, mostly hexagonal, formed as a lava flow cooled.",
                "Traditional music rings out in the pubs: fiddle, flute and bodhrán.",
                "Legend says the giant Finn McCool built the causeway to go and fight a Scottish giant.",
                "Bodhrán", "The trophy of those who have travelled across the United Kingdom: a skin drum played with a small stick.",
            ),
        ),
    )

    private val germany = JourneyEn(
        "Germany",
        "From the Brandenburg Gate to the port of Hamburg, through Bavaria and the Black Forest.",
        listOf(
            StopEn(
                "Berlin", "Germany",
                "The Brandenburg Gate, topped by a quadriga, a symbol of reunification since the fall of the Wall in 1989.",
                "Five great museums stand on an island in the Spree, Museum Island, listed by UNESCO.",
                "A double row of cobblestones marks the line of the Wall across the city.",
                "Ampelmännchen", "The little hatted man of East Berlin's pedestrian lights, now the city's mascot.",
            ),
            StopEn(
                "Potsdam", "Germany",
                "Sanssouci Palace, Frederick the Great's summer residence, at the top of terraced vineyards.",
                "The parks and palaces of Potsdam form one of the largest UNESCO-listed ensembles in Germany.",
                "Visitors leave potatoes on the grave of Frederick II, who encouraged growing them in Prussia.",
                "Flute", "Frederick the Great played it and composed for it.",
            ),
            StopEn(
                "Dresden", "Germany",
                "The Frauenkirche, a Baroque church with a stone dome, destroyed in 1945 and rebuilt exactly as it was in 2005.",
                "The Striezelmarkt, a Christmas market held since 1434, and stollen, a Christmas cake with dried fruit.",
                "The original blackened stones were put back in place: they form dark patches on the pale façade.",
                "Nutcracker", "The painted wooden soldier from the nearby Ore Mountains.",
            ),
            StopEn(
                "Nuremberg", "Germany",
                "Half-timbered houses and the imperial castle, above an old town surrounded by walls.",
                "Nuremberg's small grilled sausages are served three to a bread roll.",
                "The painter Albrecht Dürer was born there in 1471; his house can still be visited.",
                "Gingerbread", "Lebkuchen, Nuremberg's round gingerbread.",
            ),
            StopEn(
                "Munich", "Germany",
                "The neo-Gothic New Town Hall and its glockenspiel, whose figures dance every day on Marienplatz.",
                "Oktoberfest, the largest beer festival in the world, begins in September.",
                "The first Oktoberfest, in 1810, celebrated the wedding of Crown Prince Ludwig of Bavaria.",
                "Stoneware stein", "The large beer mug with a pewter lid.",
            ),
            StopEn(
                "Neuschwanstein", "Germany",
                "Ludwig II of Bavaria's fairy-tale castle, perched on a rocky spur above the gorges.",
                "Ludwig II admired Wagner's operas: the castle's rooms depict their legends.",
                "The castle inspired Sleeping Beauty's castle at Disney.",
                "Snow globe", "The castle in the snow, in miniature.",
            ),
            StopEn(
                "Black Forest", "Germany",
                "Farmhouses with huge roofs that reach almost to the ground, among the fir trees.",
                "Black Forest gateau: chocolate, cherries and cream, flavoured with the region's kirsch.",
                "There are cuckoo clocks so big you can walk inside them.",
                "Cuckoo clock", "Carved from wood, with its bird popping out every hour.",
            ),
            StopEn(
                "Heidelberg", "Germany",
                "The red sandstone ruins of the castle overlook the Neckar and the Old Bridge.",
                "The oldest university in Germany, founded in 1386.",
                "The castle cellar holds a giant barrel of more than 200,000 litres.",
                "Student kiss", "The Studentenkuss, a chocolate students once gave as a gift.",
            ),
            StopEn(
                "Cologne", "Germany",
                "The Gothic cathedral and its two 157-metre spires, completed in 1880 after six centuries of work.",
                "Carnival, the “fifth season”, opens every 11 November at 11:11.",
                "Eau de Cologne was created here in the early 18th century by an Italian perfumer, Johann Maria Farina.",
                "Bottle of eau de Cologne", "The fresh citrus scent named after the city.",
            ),
            StopEn(
                "Hamburg", "Germany",
                "The brick warehouses of the Speicherstadt and the Elbphilharmonie, a wave-shaped glass concert hall.",
                "The Sunday fish market opens at 5 am, when night owls are heading home.",
                "The city has more bridges than Venice and Amsterdam combined.",
                "Captain's cap", "The trophy of those who have travelled across Germany.",
            ),
        ),
    )
}
