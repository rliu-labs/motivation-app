/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.journey

import app.sentier.domain.tr
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import app.sentier.domain.WorldTour

/** Les scènes de chaque voyage, une par escale, dans l'ordre des escales. */
object JourneyScenes {
    private val france = listOf(
        Scene(Landmark.CATHEDRAL, 0xFFA1C4FD, 0xFFE3ECF7, 0xFF7FA87A, 0xFF5D5A6B, label = tr("Notre-Dame de Paris", "Notre-Dame de Paris")),
        Scene(Landmark.VINEYARD, 0xFFFFE0B2, 0xFFFFF8E1, 0xFF9CB36B, 0xFF4E6B2F, label = tr("les coteaux de Champagne", "the Champagne hillsides")),
        Scene(Landmark.HALF_TIMBERED, 0xFFB3E5FC, 0xFFE1F5FE, 0xFF6E9E6E, 0xFF5D4037, label = tr("les maisons à colombages de la Petite France", "the half-timbered houses of La Petite France")),
        Scene(Landmark.SNOW_PEAKS, 0xFF64B5F6, 0xFFE3F2FD, 0xFF7CB342, 0xFF546E7A, label = tr("le massif du Mont-Blanc", "the Mont Blanc massif")),
        Scene(Landmark.HILL_CHURCH, 0xFFFFCCBC, 0xFFFFF3E0, 0xFF8D9F6A, 0xFF8D8478, label = tr("la basilique de Fourvière sur sa colline", "the Fourvière basilica on its hill")),
        Scene(Landmark.BROKEN_BRIDGE, 0xFF81D4FA, 0xFFFFF9C4, 0xFFC8B98A, 0xFFB08D57, label = tr("le pont d'Avignon et le palais des Papes", "the Avignon bridge and the Palace of the Popes")),
        Scene(Landmark.HARBOUR, 0xFF4FC3F7, 0xFFFFF3E0, 0xFFD7C29B, 0xFF6D5D4B, label = tr("le Vieux-Port et ses bateaux", "the Old Port and its boats")),
        Scene(Landmark.CASTLE, 0xFFFFD180, 0xFFFFF3E0, 0xFF9E9D5C, 0xFF8C7B65, label = tr("les remparts de la Cité de Carcassonne", "the ramparts of the Cité of Carcassonne")),
        Scene(Landmark.LIGHTHOUSE, 0xFF4DD0E1, 0xFFE0F7FA, 0xFF2E7D9A, 0xFFC62828, label = tr("le phare au-dessus de l'océan", "the lighthouse above the ocean")),
        Scene(Landmark.PALACE, 0xFFFFB74D, 0xFFFFE0B2, 0xFF7BA4B8, 0xFFB39770, label = tr("la place de la Bourse et son miroir d'eau", "the Place de la Bourse and its water mirror")),
        Scene(Landmark.CHATEAU, 0xFF90CAF9, 0xFFE8F5E9, 0xFF6A994E, 0xFF8A8478, label = tr("le château de Chambord", "the Château de Chambord")),
        Scene(Landmark.STONES, 0xFFB0BEC5, 0xFFECEFF1, 0xFF8BA36B, 0xFF6F6A60, label = tr("les alignements de menhirs", "the rows of standing stones")),
        Scene(Landmark.ABBEY_ISLAND, 0xFFFFAB91, 0xFFFFE0B2, 0xFFBCAAA4, 0xFF5D4E46, label = tr("le Mont-Saint-Michel entouré par la mer", "Mont-Saint-Michel surrounded by the sea")),
        Scene(Landmark.CLOCK_TOWER, 0xFFB39DDB, 0xFFEDE7F6, 0xFF7E8F6A, 0xFF8D4F3A, label = tr("le beffroi de Lille", "the belfry of Lille")),
        Scene(Landmark.ARCH, 0xFFFF8A65, 0xFFFFE0B2, 0xFF9E9E9E, 0xFFD7C9A7, label = tr("l'arc de triomphe", "the Arc de Triomphe")),
    )

    private val italie = listOf(
        Scene(Landmark.CANAL, 0xFFFFCC80, 0xFFFFF3E0, 0xFF3F7F93, 0xFFB5651D, label = tr("un canal de Venise et sa gondole", "a Venetian canal and its gondola")),
        Scene(Landmark.CATHEDRAL, 0xFF90CAF9, 0xFFF5F5F5, 0xFFBDBDBD, 0xFFE8E4DA, label = tr("le Duomo de Milan", "Milan's Duomo")),
        Scene(Landmark.CLIFF_VILLAGE, 0xFF4FC3F7, 0xFFE1F5FE, 0xFF1E88A8, 0xFF6B6B47, label = tr("des maisons colorées accrochées à la falaise", "colourful houses clinging to the cliff")),
        Scene(Landmark.LEANING_TOWER, 0xFF81D4FA, 0xFFFFFFFF, 0xFF7CB342, 0xFFEDE7DC, label = tr("la tour penchée de Pise", "the Leaning Tower of Pisa")),
        Scene(Landmark.BRIDGE, 0xFFFFB74D, 0xFFFFE0B2, 0xFF5D8AA8, 0xFFB86B3C, label = tr("le Ponte Vecchio sur l'Arno", "the Ponte Vecchio over the Arno")),
        Scene(Landmark.CLOCK_TOWER, 0xFF90CAF9, 0xFFFFF8E1, 0xFFC89F6E, 0xFFA0522D, label = tr("la tour du Mangia", "the Torre del Mangia")),
        Scene(Landmark.COLOSSEUM, 0xFFFFCCBC, 0xFFFFF8E1, 0xFFB5A27A, 0xFFB08B5A, label = tr("le Colisée", "the Colosseum")),
        Scene(Landmark.VOLCANO, 0xFF80DEEA, 0xFFFFF8E1, 0xFFA1887F, 0xFF5D4037, label = tr("le Vésuve au-dessus des ruines de Pompéi", "Vesuvius above the ruins of Pompeii")),
        Scene(Landmark.SASSI, 0xFFFFE082, 0xFFFFF8E1, 0xFFBCAAA4, 0xFFC2A878, label = tr("les maisons troglodytes des Sassi", "the cave houses of the Sassi")),
        Scene(Landmark.TRULLI, 0xFF81D4FA, 0xFFE1F5FE, 0xFF9CCC65, 0xFF78909C, label = tr("les trulli aux toits coniques", "the trulli with their conical roofs")),
    )

    private val espagne = listOf(
        Scene(Landmark.SPIRES, 0xFFFFCC80, 0xFFFFF8E1, 0xFFBCAAA4, 0xFFA1887F, label = tr("la Sagrada Família et sa grue", "the Sagrada Família and its crane")),
        Scene(Landmark.MODERN, 0xFF4FC3F7, 0xFFE1F5FE, 0xFF29B6F6, 0xFFF5F5F5, label = tr("la Cité des arts et des sciences", "the City of Arts and Sciences")),
        Scene(Landmark.CASTLE, 0xFFFFAB91, 0xFFFFE0B2, 0xFF7C8B4E, 0xFFB5523B, label = tr("l'Alhambra", "the Alhambra")),
        Scene(Landmark.MINARET, 0xFFFFCC80, 0xFFFFF3E0, 0xFFD9B26F, 0xFFC0803F, label = tr("la Giralda", "the Giralda")),
        Scene(Landmark.MOORISH, 0xFFFFE0B2, 0xFFFFF8E1, 0xFFD7B98E, 0xFF6D4C41, label = tr("les arcs rayés de la Mezquita", "the striped arches of the Mezquita")),
        Scene(Landmark.PALACE, 0xFF90CAF9, 0xFFE3F2FD, 0xFFBCAAA4, 0xFFE0D6C2, label = tr("le Palais royal de Madrid", "the Royal Palace of Madrid")),
        Scene(Landmark.AQUEDUCT, 0xFF64B5F6, 0xFFE3F2FD, 0xFFA1887F, 0xFF8D8073, label = tr("l'aqueduc de Ségovie", "the aqueduct of Segovia")),
        Scene(Landmark.CURVES, 0xFF90A4AE, 0xFFECEFF1, 0xFF607D8B, 0xFFB0BEC5, label = tr("les courbes de titane du musée Guggenheim", "the titanium curves of the Guggenheim Museum")),
        Scene(Landmark.CATHEDRAL, 0xFFB0BEC5, 0xFFECEFF1, 0xFF8D9F7A, 0xFF8D7B68, label = tr("la cathédrale de Saint-Jacques-de-Compostelle", "the cathedral of Santiago de Compostela")),
    )

    private val portugal = listOf(
        Scene(Landmark.LIGHTHOUSE, 0xFFFFCC80, 0xFFFFF3E0, 0xFF26A69A, 0xFFD58936, label = tr("le phare au bord des falaises dorées", "the lighthouse on the golden cliffs")),
        Scene(Landmark.TEMPLE, 0xFF81D4FA, 0xFFFFFDE7, 0xFFD7C9A7, 0xFF9E8B6E, label = tr("le temple romain d'Évora", "the Roman temple of Évora")),
        Scene(Landmark.TRAM, 0xFF4FC3F7, 0xFFE1F5FE, 0xFFBDBDBD, 0xFF37474F, label = tr("un tramway jaune dans une rue en pente", "a yellow tram on a steep street")),
        Scene(Landmark.FAIRY_CASTLE, 0xFFB2EBF2, 0xFFE0F7FA, 0xFF388E3C, 0xFFE0A030, label = tr("le palais de Pena sur sa colline", "the Pena Palace on its hill")),
        Scene(Landmark.CASTLE, 0xFFFFE082, 0xFFFFF8E1, 0xFF9CCC65, 0xFFB0A493, label = tr("les remparts d'Óbidos", "the walls of Óbidos")),
        Scene(Landmark.HARBOUR, 0xFF4DD0E1, 0xFFE0F7FA, 0xFFE6D3A3, 0xFF5D4037, label = tr("les barques de pêcheurs sur la plage", "fishing boats on the beach")),
        Scene(Landmark.CLOCK_TOWER, 0xFF9FA8DA, 0xFFE8EAF6, 0xFF8D9F6A, 0xFFD7CCB7, label = tr("la tour de l'université de Coimbra", "the tower of Coimbra University")),
        Scene(Landmark.CLIFF_VILLAGE, 0xFFFFAB91, 0xFFFFE0B2, 0xFF3E7C8C, 0xFF6D5D4B, label = tr("les maisons de la Ribeira au bord du Douro", "the Ribeira houses on the Douro")),
        Scene(Landmark.VINEYARD, 0xFFFFCC80, 0xFFFFF8E1, 0xFF8BA05A, 0xFF5B7A2E, label = tr("les vignes en terrasses du Douro", "the terraced vineyards of the Douro")),
    )

    private val royaumeUni = listOf(
        Scene(Landmark.CLOCK_TOWER, 0xFF90A4AE, 0xFFECEFF1, 0xFF6E8B6E, 0xFFC2A15A, label = tr("la tour de Big Ben", "the Big Ben tower")),
        Scene(Landmark.DOME, 0xFFB3E5FC, 0xFFFFF8E1, 0xFF7CB342, 0xFFC9B48A, label = tr("la Radcliffe Camera", "the Radcliffe Camera")),
        Scene(Landmark.STONEHENGE, 0xFFFFAB91, 0xFFFFE0B2, 0xFF8BAA5B, 0xFF7D776B, label = tr("le cercle de pierres de Stonehenge", "the stone circle of Stonehenge")),
        Scene(Landmark.TEMPLE, 0xFFB0BEC5, 0xFFF5F5F5, 0xFF6E9E8E, 0xFFC9B48A, label = tr("les thermes romains de Bath", "the Roman baths of Bath")),
        Scene(Landmark.SNOW_PEAKS, 0xFF90A4AE, 0xFFE0E0E0, 0xFF558B2F, 0xFF4E5B4E, label = tr("le mont Snowdon", "Snowdon")),
        Scene(Landmark.HARBOUR, 0xFF90A4AE, 0xFFECEFF1, 0xFF8D8D8D, 0xFF455A64, label = tr("les quais de Liverpool", "the Liverpool waterfront")),
        Scene(Landmark.CATHEDRAL, 0xFFB0BEC5, 0xFFF5F5F5, 0xFF7E9C76, 0xFFBFAF8F, label = tr("York Minster", "York Minster")),
        Scene(Landmark.CASTLE, 0xFF78909C, 0xFFCFD8DC, 0xFF5E7D5A, 0xFF4E4A47, label = tr("le château d'Édimbourg sur son rocher", "Edinburgh Castle on its rock")),
        Scene(Landmark.LOCH, 0xFFB0BEC5, 0xFFECEFF1, 0xFF4E6E58, 0xFF4A4A44, label = tr("le loch, le château en ruine et le monstre", "the loch, the ruined castle and the monster")),
        Scene(Landmark.BASALT, 0xFF78909C, 0xFFCFD8DC, 0xFF37627A, 0xFF3E3B3A, label = tr("les colonnes de basalte de la chaussée des Géants", "the basalt columns of the Giant's Causeway")),
    )

    private val allemagne = listOf(
        Scene(Landmark.GATE, 0xFF90CAF9, 0xFFE3F2FD, 0xFF9E9E9E, 0xFFC8B68E, label = tr("la porte de Brandebourg", "the Brandenburg Gate")),
        Scene(Landmark.PALACE, 0xFFFFE082, 0xFFFFFDE7, 0xFF7CB342, 0xFFE6C35C, label = tr("le palais de Sanssouci", "Sanssouci Palace")),
        Scene(Landmark.DOME, 0xFFFFCCBC, 0xFFFFF3E0, 0xFF8D9F7A, 0xFFD1C2A0, label = tr("la Frauenkirche de Dresde", "the Frauenkirche in Dresden")),
        Scene(Landmark.HALF_TIMBERED, 0xFFB3E5FC, 0xFFE1F5FE, 0xFF8BA36B, 0xFF5D4037, label = tr("les maisons à colombages de Nuremberg", "the half-timbered houses of Nuremberg")),
        Scene(Landmark.CLOCK_TOWER, 0xFF90CAF9, 0xFFE3F2FD, 0xFFBDBDBD, 0xFF6D6255, label = tr("l'hôtel de ville de Munich", "Munich's town hall")),
        Scene(Landmark.FAIRY_CASTLE, 0xFF80DEEA, 0xFFE0F7FA, 0xFF2E7D32, 0xFFEFEBE4, label = tr("le château de Neuschwanstein", "Neuschwanstein Castle")),
        Scene(Landmark.FOREST, 0xFFA5D6A7, 0xFFE8F5E9, 0xFF33691E, 0xFF4E342E, label = tr("une ferme au grand toit dans les sapins", "a big-roofed farmhouse among the firs")),
        Scene(Landmark.CASTLE, 0xFFFFAB91, 0xFFFFE0B2, 0xFF6A8F5A, 0xFF9C4A3A, label = tr("les ruines du château de Heidelberg", "the ruins of Heidelberg Castle")),
        Scene(Landmark.CATHEDRAL, 0xFF9FA8DA, 0xFFE8EAF6, 0xFF8D9F7A, 0xFF4E4A52, label = tr("la cathédrale de Cologne", "Cologne Cathedral")),
        Scene(Landmark.WAREHOUSE, 0xFF81D4FA, 0xFFE1F5FE, 0xFF3D6E8C, 0xFF8D3B2F, label = tr("les entrepôts de brique et l'Elbphilharmonie", "the brick warehouses and the Elbphilharmonie")),
    )

    private val paysBas = listOf(
        Scene(Landmark.CANAL, 0xFF90CAF9, 0xFFE3F2FD, 0xFF4A7C8C, 0xFF6D4C41, label = tr("les maisons à pignons le long d'un canal", "gabled houses along a canal")),
        Scene(Landmark.WINDMILL, 0xFF81D4FA, 0xFFE1F5FE, 0xFF7CB342, 0xFF2E5E3E, label = tr("les moulins de bois de la Zaanse Schans", "the wooden windmills of the Zaanse Schans")),
        Scene(Landmark.TULIPS, 0xFF90CAF9, 0xFFE3F2FD, 0xFF689F38, 0xFF5D4037, label = tr("des champs de tulipes et un moulin", "tulip fields and a windmill")),
        Scene(Landmark.CATHEDRAL, 0xFFB3E5FC, 0xFFF5F5F5, 0xFF8D9F7A, 0xFF7B5E4A, label = tr("la Nouvelle Église de Delft", "the New Church in Delft")),
        Scene(Landmark.CUBES, 0xFF81D4FA, 0xFFE1F5FE, 0xFF9E9E9E, 0xFFFFC107, label = tr("les maisons cubes de Rotterdam", "the cube houses of Rotterdam")),
        Scene(Landmark.WINDMILL, 0xFFFFCC80, 0xFFFFF3E0, 0xFF7CB342, 0xFF4E342E, label = tr("les moulins de Kinderdijk", "the windmills of Kinderdijk")),
        Scene(Landmark.CLOCK_TOWER, 0xFFB0BEC5, 0xFFECEFF1, 0xFF7E9C76, 0xFF8D6E63, label = tr("la tour du Dom d'Utrecht", "the Dom Tower of Utrecht")),
        Scene(Landmark.FOREST, 0xFFC5E1A5, 0xFFF1F8E9, 0xFF8D8F4E, 0xFF5D4037, label = tr("une ferme dans les bois de la Hoge Veluwe", "a farmhouse in the woods of the Hoge Veluwe")),
        Scene(Landmark.THATCH, 0xFF81D4FA, 0xFFE1F5FE, 0xFF6A994E, 0xFF8D6E3F, label = tr("les maisons au toit de chaume de Giethoorn", "the thatched houses of Giethoorn")),
    )

    private val belgique = listOf(
        Scene(Landmark.ATOMIUM, 0xFF90CAF9, 0xFFE3F2FD, 0xFF8D9F7A, 0xFFB0BEC5, label = tr("l'Atomium", "the Atomium")),
        Scene(Landmark.DOME, 0xFFB0BEC5, 0xFFF5F5F5, 0xFF9E9E9E, 0xFF6D6255, label = tr("la gare centrale d'Anvers", "Antwerp Central Station")),
        Scene(Landmark.CASTLE, 0xFFB3E5FC, 0xFFE1F5FE, 0xFF4A7C8C, 0xFF5D5A55, label = tr("le château des Comtes de Gand", "the Castle of the Counts in Ghent")),
        Scene(Landmark.CLOCK_TOWER, 0xFFFFCC80, 0xFFFFF3E0, 0xFF8D9F7A, 0xFF7B5E4A, label = tr("le beffroi de Bruges", "the belfry of Bruges")),
        Scene(Landmark.ARCH, 0xFF90A4AE, 0xFFECEFF1, 0xFF7E9C76, 0xFFD7CCB7, label = tr("la porte de Menin", "the Menin Gate")),
        Scene(Landmark.MOUND, 0xFFB3E5FC, 0xFFFFF8E1, 0xFF9CB36B, 0xFF5D4E46, label = tr("la butte du Lion", "the Lion's Mound")),
        Scene(Landmark.CLIFF_CHURCH, 0xFF81D4FA, 0xFFE1F5FE, 0xFF5E8C61, 0xFF6D6255, label = tr("la collégiale de Dinant au pied de la falaise", "Dinant's collegiate church at the foot of the cliff")),
        Scene(Landmark.MODERN, 0xFF90CAF9, 0xFFE3F2FD, 0xFF607D8B, 0xFFF5F5F5, label = tr("la gare des Guillemins", "Guillemins station")),
        Scene(Landmark.FOREST, 0xFFA5D6A7, 0xFFE8F5E9, 0xFF33691E, 0xFF4E342E, label = tr("les forêts des Ardennes", "the forests of the Ardennes")),
    )

    private val suisse = listOf(
        Scene(Landmark.FOUNTAIN, 0xFF81D4FA, 0xFFE1F5FE, 0xFF4A90A4, 0xFF607D8B, label = tr("le jet d'eau de Genève", "the Geneva water jet")),
        Scene(Landmark.VINEYARD, 0xFF81D4FA, 0xFFE1F5FE, 0xFF7CB342, 0xFF4E6B2F, label = tr("les vignes en terrasses de Lavaux", "the terraced vineyards of Lavaux")),
        Scene(Landmark.CASTLE, 0xFF4FC3F7, 0xFFE1F5FE, 0xFF3F7F93, 0xFFBCAAA4, label = tr("le château de Chillon au bord du lac", "Chillon Castle on the lakeshore")),
        Scene(Landmark.SNOW_PEAKS, 0xFF42A5F5, 0xFFE3F2FD, 0xFF7CB342, 0xFF5D4037, label = tr("le Cervin", "the Matterhorn")),
        Scene(Landmark.RAILWAY, 0xFF64B5F6, 0xFFE3F2FD, 0xFFECEFF1, 0xFF546E7A, label = tr("un train rouge au pied des sommets enneigés", "a red train below snowy peaks")),
        Scene(Landmark.CLOCK_TOWER, 0xFFB3E5FC, 0xFFFFF8E1, 0xFF9E9E9E, 0xFF8D6E63, label = tr("la tour de l'Horloge de Berne", "Bern's clock tower")),
        Scene(Landmark.BRIDGE, 0xFF81D4FA, 0xFFE1F5FE, 0xFF4A90A4, 0xFF8D5A3B, label = tr("le pont de la Chapelle", "the Chapel Bridge")),
        Scene(Landmark.CATHEDRAL, 0xFFB0BEC5, 0xFFF5F5F5, 0xFF7E9C76, 0xFFC9B48A, label = tr("le Grossmünster de Zurich", "Zurich's Grossmünster")),
        Scene(Landmark.HALF_TIMBERED, 0xFF90CAF9, 0xFFE3F2FD, 0xFF7CB342, 0xFF6D4C41, label = tr("les façades peintes d'Appenzell", "the painted façades of Appenzell")),
    )

    private val autriche = listOf(
        Scene(Landmark.PALACE, 0xFF90CAF9, 0xFFE3F2FD, 0xFF7CB342, 0xFFF2C14E, label = tr("le château de Schönbrunn", "Schönbrunn Palace")),
        Scene(Landmark.CATHEDRAL, 0xFF81D4FA, 0xFFFFF8E1, 0xFF7CB342, 0xFFE8C468, label = tr("l'abbaye de Melk", "Melk Abbey")),
        Scene(Landmark.MODERN, 0xFF1A237E, 0xFF4A148C, 0xFF1E3A5F, 0xFF80DEEA, night = true, label = tr("l'Ars Electronica Center illuminé", "the Ars Electronica Center lit up")),
        Scene(Landmark.LAKE_VILLAGE, 0xFF90CAF9, 0xFFE3F2FD, 0xFF4A7C8C, 0xFF5D4E46, label = tr("Hallstatt entre le lac et la montagne", "Hallstatt between the lake and the mountain")),
        Scene(Landmark.CASTLE, 0xFFFFCC80, 0xFFFFF3E0, 0xFF7CB342, 0xFFD7CCB7, label = tr("la forteresse de Hohensalzburg", "Hohensalzburg Fortress")),
        Scene(Landmark.SKI_JUMP, 0xFF64B5F6, 0xFFE3F2FD, 0xFFECEFF1, 0xFF455A64, label = tr("le tremplin de Bergisel", "the Bergisel ski jump")),
        Scene(Landmark.SNOW_PEAKS, 0xFF42A5F5, 0xFFE3F2FD, 0xFF689F38, 0xFF4E5B4E, label = tr("le Grossglockner", "the Grossglockner")),
        Scene(Landmark.CLOCK_TOWER, 0xFFFFE0B2, 0xFFFFF8E1, 0xFF6A994E, 0xFFD7CCB7, label = tr("la tour de l'Horloge de Graz", "the clock tower of Graz")),
        Scene(Landmark.HARBOUR, 0xFFB3E5FC, 0xFFE1F5FE, 0xFFC5B783, 0xFF8D6E63, label = tr("les voiliers sur le lac de Neusiedl", "sailing boats on Lake Neusiedl")),
    )

    private val grece = listOf(
        Scene(Landmark.ACROPOLIS, 0xFF4FC3F7, 0xFFE1F5FE, 0xFFC9B48A, 0xFFEDE7DC, label = tr("l'Acropole au-dessus de la ville", "the Acropolis above the city")),
        Scene(Landmark.CANYON, 0xFF4FC3F7, 0xFFE1F5FE, 0xFFC8A97E, 0xFFB08D57, label = tr("le canal de Corinthe", "the Corinth Canal")),
        Scene(Landmark.LION_GATE, 0xFF81D4FA, 0xFFFFF8E1, 0xFFB5A27A, 0xFF9E8B6E, label = tr("la porte des Lionnes", "the Lion Gate")),
        Scene(Landmark.TEMPLE, 0xFFFFCC80, 0xFFFFF3E0, 0xFF8D9F6A, 0xFFC9B48A, label = tr("le temple de Zeus à Olympie", "the Temple of Zeus at Olympia")),
        Scene(Landmark.RUINS, 0xFF81D4FA, 0xFFE1F5FE, 0xFF8D9F6A, 0xFFE0D6C2, label = tr("la tholos de Delphes", "the tholos of Delphi")),
        Scene(Landmark.PINNACLES, 0xFFFFAB91, 0xFFFFE0B2, 0xFF6A8F5A, 0xFF8D7B68, label = tr("les monastères perchés des Météores", "the perched monasteries of the Meteora")),
        Scene(Landmark.SNOW_PEAKS, 0xFF64B5F6, 0xFFE3F2FD, 0xFF6A8F5A, 0xFF5D5A55, label = tr("le mont Olympe", "Mount Olympus")),
        Scene(Landmark.LIGHTHOUSE, 0xFF4FC3F7, 0xFFE1F5FE, 0xFF2E7D9A, 0xFF90A4AE, label = tr("la tour Blanche au bord de la mer", "the White Tower by the sea")),
        Scene(Landmark.BLUE_DOMES, 0xFF29B6F6, 0xFFE1F5FE, 0xFF1565C0, 0xFF1E5AA8, label = tr("les maisons blanches et les coupoles bleues de Santorin", "the white houses and blue domes of Santorini")),
    )

    private val byId: Map<String, List<Scene>> = mapOf(
        WorldTour.journey.id to StopScenes,
        "france" to france,
        "italie" to italie,
        "espagne" to espagne,
        "portugal" to portugal,
        "royaume-uni" to royaumeUni,
        "allemagne" to allemagne,
        "pays-bas" to paysBas,
        "belgique" to belgique,
        "suisse" to suisse,
        "autriche" to autriche,
        "grece" to grece,
    )

    fun of(journeyId: String): List<Scene> = byId[journeyId] ?: StopScenes
}

private val Gold = Color(0xFFFFD54F)
private val Shade = Color.Black.copy(alpha = 0.3f)

/** Eau du premier plan, de [y] jusqu'au bas de l'image. */
private fun Units.water(y: Float, c: Color) =
    d.drawRect(c, topLeft = Offset(0f, y * s), size = Size(d.size.width, d.size.height - y * s))

private fun Units.line(x1: Float, y1: Float, x2: Float, y2: Float, c: Color, w: Float = 0.6f) =
    d.drawLine(c, p(x1, y1), p(x2, y2), strokeWidth = w * s)

/** Fenêtre ou porte en arc. */
private fun Units.archOpening(cx: Float, top: Float, w: Float, bottom: Float, c: Color) {
    rect(cx - w / 2, top, w, bottom - top, c)
    dome(cx, top, w / 2, c)
}

/** Les monuments des voyages par pays. */
internal fun Units.europeLandmark(kind: Landmark, ink: Color, light: Color) {
    val g = 50f
    when (kind) {
        Landmark.CATHEDRAL -> {
            rect(32f, 22f, 36f, 28f, ink)
            for (x in listOf(28f, 60f)) {
                rect(x, 10f, 12f, 40f, ink)
                for (k in 0..2) tri(x + k * 4f, 10f, x + k * 4f + 2f, 6f, x + k * 4f + 4f, 10f, ink)
                archOpening(x + 6f, 16f, 3f, 24f, light.copy(alpha = 0.45f))
            }
            tri(40f, 22f, 50f, 13f, 60f, 22f, ink)
            circle(50f, 28f, 4.5f, light.copy(alpha = 0.6f))
            circle(50f, 28f, 2f, ink.copy(alpha = 0.6f))
            archOpening(50f, 40f, 7f, g, Shade)
            for (x in listOf(34f, 66f)) archOpening(x, 42f, 4f, g, Shade)
        }
        Landmark.SPIRES -> {
            rect(26f, 38f, 48f, 12f, ink)
            for ((x, h) in listOf(30f to 18f, 37f to 26f, 44f to 32f, 50f to 38f, 56f to 32f, 63f to 26f, 70f to 18f)) {
                val top = 38f - h
                poly(ink, x - 3f to 38f, x - 1.2f to top, x + 1.2f to top, x + 3f to 38f)
                circle(x, top, 1.6f, light.copy(alpha = 0.9f))
            }
            for (x in listOf(32f, 44f, 56f, 68f)) archOpening(x, 42f, 4f, g, Shade)
            // La grue du chantier.
            line(84f, 4f, 84f, g, ink, 0.8f)
            line(70f, 6f, 96f, 6f, ink, 0.8f)
            line(76f, 6f, 76f, 14f, ink, 0.3f)
        }
        Landmark.CASTLE -> {
            rect(20f, 32f, 60f, 18f, ink)
            for (i in 0..10) rect(21f + i * 5.5f, 29.5f, 2.6f, 2.5f, ink)
            for ((x, top) in listOf(15f to 22f, 45f to 16f, 75f to 22f)) {
                rect(x, top, 10f, g - top, ink)
                tri(x - 1f, top, x + 5f, top - 9f, x + 11f, top, ink)
                rect(x + 4f, top + 5f, 2f, 3f, Shade)
            }
            archOpening(50f, 40f, 6f, g, Shade)
        }
        Landmark.CHATEAU -> {
            rect(20f, 32f, 60f, 18f, ink)
            for (x in listOf(12f, 78f)) {
                rect(x, 28f, 10f, 22f, ink)
                tri(x - 1f, 28f, x + 5f, 18f, x + 11f, 28f, ink)
            }
            for (x in listOf(28f, 66f)) tri(x - 6f, 32f, x, 22f, x + 6f, 32f, ink)
            for (x in listOf(34f, 38f, 60f, 64f)) rect(x, 22f, 1.6f, 10f, ink)
            rect(45f, 16f, 10f, 16f, ink)
            tri(44f, 16f, 50f, 6f, 56f, 16f, ink)
            rect(49.3f, 2f, 1.4f, 4f, ink)
            for (row in listOf(36f, 43f)) for (i in 0..8) rect(23f + i * 6.4f, row, 2.4f, 4f, light.copy(alpha = 0.5f))
        }
        Landmark.FAIRY_CASTLE -> {
            poly(Color.Black.copy(alpha = 0.25f), 16f to g, 26f to 38f, 74f to 36f, 84f to g)
            rect(34f, 24f, 30f, 16f, ink)
            rect(30f, 14f, 6f, 26f, ink)
            tri(29f, 14f, 33f, 4f, 37f, 14f, Color(0xFF5C6B8A))
            rect(56f, 8f, 7f, 32f, ink)
            tri(55f, 8f, 59.5f, -2f, 64f, 8f, Color(0xFF5C6B8A))
            rect(43f, 18f, 5f, 8f, ink)
            tri(42f, 18f, 45.5f, 11f, 49f, 18f, Color(0xFF5C6B8A))
            for (x in listOf(38f, 44f, 50f)) archOpening(x, 29f, 2.4f, 34f, Shade)
        }
        Landmark.ABBEY_ISLAND -> {
            water(46f, Color(0xFF6FA8C7))
            poly(ink.copy(alpha = 0.75f), 22f to 48f, 34f to 38f, 42f to 30f, 58f to 30f, 66f to 38f, 78f to 48f)
            for ((x, y) in listOf(30f to 41f, 36f to 38f, 62f to 38f, 68f to 41f)) rect(x, y, 4f, 3f, ink)
            rect(42f, 22f, 16f, 9f, ink)
            tri(48.5f, 22f, 50f, 4f, 51.5f, 22f, ink)
            circle(50f, 3.6f, 0.9f, Gold)
            line(10f, 52f, 90f, 52f, Color.White.copy(alpha = 0.5f), 0.4f)
        }
        Landmark.STONES -> {
            for ((row, base) in listOf(0 to 42f, 1 to 46f, 2 to 50f)) {
                val h = 6f + row * 3f
                val w = 2.5f + row * 1f
                for (i in 0..8) {
                    val x = 6f + i * 10.5f + row * 2f
                    val hh = h * (0.8f + ((i * 7 + row * 3) % 5) * 0.08f)
                    rect(x, base - hh, w, hh, ink.copy(alpha = 0.7f + row * 0.15f))
                    dome(x + w / 2, base - hh, w / 2, ink.copy(alpha = 0.7f + row * 0.15f))
                }
            }
        }
        Landmark.STONEHENGE -> {
            for (x in listOf(16f, 34f, 52f, 70f)) {
                rect(x, 30f, 4f, 20f, ink)
                rect(x + 9f, 30f, 4f, 20f, ink)
                rect(x - 1f, 27f, 15f, 3.5f, ink)
            }
            rect(40f, 47f, 10f, 3f, ink.copy(alpha = 0.8f))
            rect(86f, 36f, 3.5f, 14f, ink.copy(alpha = 0.8f))
        }
        Landmark.BRIDGE -> {
            water(46f, Color(0xFF5D8AA8))
            rect(12f, 30f, 76f, 18f, ink)
            for (cx in listOf(28f, 50f, 72f)) {
                rect(cx - 8f, 42f, 16f, 6f, light)
                dome(cx, 42f, 8f, light)
            }
            for (i in 0..6) {
                val h = 8f + (i % 3) * 2f
                rect(13f + i * 10.6f, 30f - h, 10f, h, ink.copy(alpha = 0.85f + (i % 2) * 0.15f))
                rect(15f + i * 10.6f, 30f - h + 2f, 2f, 2.5f, light.copy(alpha = 0.6f))
                rect(19f + i * 10.6f, 30f - h + 2f, 2f, 2.5f, light.copy(alpha = 0.6f))
            }
        }
        Landmark.BROKEN_BRIDGE -> {
            rect(66f, 22f, 26f, 28f, ink.copy(alpha = 0.55f))
            for (i in 0..5) rect(66.5f + i * 4.4f, 20f, 2.4f, 2f, ink.copy(alpha = 0.55f))
            rect(84f, 12f, 7f, 10f, ink.copy(alpha = 0.55f))
            water(46f, Color(0xFF6FA8C7))
            rect(0f, 32f, 58f, 16f, ink)
            for (cx in listOf(9f, 24f, 39f)) {
                rect(cx - 5.5f, 41f, 11f, 7f, light)
                dome(cx, 41f, 5.5f, light)
            }
            rect(18f, 24f, 9f, 8f, ink)
            tri(17f, 24f, 22.5f, 19f, 28f, 24f, ink)
        }
        Landmark.LEANING_TOWER -> {
            rect(8f, 38f, 26f, 12f, ink.copy(alpha = 0.85f))
            dome(21f, 38f, 6f, ink.copy(alpha = 0.85f))
            poly(ink, 46f to g, 58f to g, 64f to 12f, 52f to 12f)
            for (k in 1..6) {
                val t = k / 7f
                line(46f + 6f * t * 1f + t * 0f, g - 38f * t, 58f + 6f * t, g - 38f * t, light.copy(alpha = 0.7f), 0.5f)
            }
            poly(ink, 53f to 12f, 63f to 12f, 63.6f to 7f, 54.6f to 7f)
            dome(59f, 7f, 3f, ink)
        }
        Landmark.CANAL -> {
            water(42f, Color(0xFF3F7F93))
            val houses = listOf(
                Triple(0f, 14f, 26f), Triple(14f, 12f, 22f), Triple(26f, 10f, 18f),
                Triple(64f, 10f, 20f), Triple(74f, 12f, 26f), Triple(86f, 14f, 22f),
            )
            houses.forEachIndexed { i, (x, w, h) ->
                val c = listOf(ink, Color(0xFFE8A87C), Color(0xFFD4A373), Color(0xFFC97B63))[i % 4]
                rect(x, 42f - h, w, h, c)
                for (wx in 0 until (w / 4).toInt()) archOpening(x + 2.5f + wx * 4f, 42f - h + 4f, 1.6f, 42f - h + 8f, Shade)
            }
            poly(Color(0xFF1B1B1B), 34f to 49f, 64f to 49f, 70f to 45f, 66f to 46.5f, 34f to 46.5f, 30f to 44f)
            rect(58f, 38f, 1.5f, 8.5f, Color(0xFF1B1B1B))
            circle(58.75f, 37f, 1.2f, Color(0xFF1B1B1B))
            line(54f, 52f, 61f, 38f, Color(0xFF1B1B1B), 0.4f)
        }
        Landmark.COLOSSEUM -> {
            poly(ink, 14f to g, 14f to 18f, 58f to 18f, 68f to 24f, 78f to 26f, 86f to 32f, 86f to g)
            for (row in 0..2) {
                val y = 22f + row * 9f
                for (i in 0..8) {
                    val cx = 18.5f + i * 7.6f
                    if (row == 0 && cx > 60f) continue
                    archOpening(cx, y + 2f, 4f, y + 7f, light.copy(alpha = 0.55f))
                }
            }
        }
        Landmark.CLIFF_VILLAGE -> {
            water(44f, Color(0xFF2A7FA8))
            poly(ink, -50f to 60f, -50f to 14f, 22f to 14f, 44f to 22f, 62f to 40f, 72f to 60f)
            val colors = listOf(Color(0xFFF4A261), Color(0xFFE76F51), Color(0xFFF6D365), Color(0xFF90BE6D), Color(0xFFF28482), Color(0xFF84A59D))
            for (i in 0..8) {
                val x = 2f + i * 6.4f
                val y = 10f + i * 3.4f
                rect(x, y, 7f, 8f, colors[i % colors.size])
                rect(x + 1.5f, y + 2f, 1.6f, 2f, Shade)
                rect(x + 4f, y + 2f, 1.6f, 2f, Shade)
            }
            for (i in 0..4) rect(4f + i * 6.4f, 20f + i * 3.4f, 7f, 7f, colors[(i + 3) % colors.size])
        }
        Landmark.SASSI -> {
            poly(ink.copy(alpha = 0.55f), 0f to g, 0f to 26f, 30f to 16f, 70f to 18f, 100f to 28f, 100f to g)
            for (row in 0..3) for (col in 0..6) {
                val x = 4f + col * 13f + (row % 2) * 6f
                val y = 20f + row * 7.5f
                rect(x, y, 10f, 7f, ink)
                archOpening(x + 5f, y + 3f, 2.4f, y + 7f, Shade)
            }
            rect(46f, 6f, 5f, 14f, ink)
            tri(45.5f, 6f, 48.5f, 2f, 51.5f, 6f, ink)
        }
        Landmark.LIGHTHOUSE -> {
            water(44f, Color(0xFF2E7D9A))
            poly(Color(0xFF6D5D4B), 30f to g, 36f to 42f, 44f to 38f, 62f to 38f, 70f to 44f, 76f to g)
            poly(Color(0xFFFFFFFF), 45f to 39f, 55f to 39f, 53f to 13f, 47f to 13f)
            poly(ink, 45.8f to 32f, 54.2f to 32f, 53.7f to 26f, 46.3f to 26f)
            poly(ink, 46.8f to 20f, 53.2f to 20f, 53f to 16f, 47f to 16f)
            rect(46f, 9f, 8f, 4f, Color(0xFF37474F))
            rect(47.5f, 9.8f, 5f, 2.4f, Gold)
            dome(50f, 9f, 4f, ink)
            poly(Gold.copy(alpha = 0.3f), 54f to 10f, 100f to 2f, 100f to 20f)
        }
        Landmark.CLOCK_TOWER -> {
            rect(8f, 34f, 36f, 16f, ink.copy(alpha = 0.8f))
            rect(56f, 34f, 36f, 16f, ink.copy(alpha = 0.8f))
            for (i in 0..5) {
                rect(11f + i * 5.6f, 38f, 2.4f, 5f, light.copy(alpha = 0.45f))
                rect(59f + i * 5.6f, 38f, 2.4f, 5f, light.copy(alpha = 0.45f))
            }
            rect(44f, 16f, 12f, 34f, ink)
            rect(45f, 10f, 10f, 6f, ink)
            tri(44f, 10f, 50f, -1f, 56f, 10f, ink)
            circle(50f, 20f, 4f, Color(0xFFFFF8E1))
            line(50f, 20f, 50f, 17.4f, Color(0xFF263238), 0.5f)
            line(50f, 20f, 52f, 20f, Color(0xFF263238), 0.5f)
            for (y in listOf(28f, 36f)) rect(48.5f, y, 3f, 5f, light.copy(alpha = 0.45f))
        }
        Landmark.MOORISH -> {
            rect(6f, 22f, 88f, 4f, ink)
            for (i in 0..6) {
                val cx = 12f + i * 12.6f
                rect(cx - 1f, 30f, 2f, 20f, ink)
                for (k in 0..5) {
                    val c = if (k % 2 == 0) Color(0xFFB23A2E) else Color(0xFFF3E6CC)
                    d.drawArc(c, 180f + k * 30f, 30f, true, topLeft = p(cx - 6f, 26f), size = Size(12f * s, 12f * s))
                }
                dome(cx, 32f, 3.6f, light)
            }
            rect(6f, 26f, 88f, 1f, ink)
        }
        Landmark.MODERN -> {
            water(46f, Color(0xFF29B6F6))
            val eye = Path().apply {
                moveTo(ox + 10f * s, 40f * s)
                quadraticTo(ox + 34f * s, 18f * s, ox + 58f * s, 40f * s)
                quadraticTo(ox + 34f * s, 50f * s, ox + 10f * s, 40f * s)
                close()
            }
            d.drawPath(eye, ink)
            circle(34f, 38f, 4.5f, Color(0xFF90A4AE))
            for (i in 0..7) {
                val x = 62f + i * 4.5f
                line(x, 46f, x + 2f, 24f, ink, 0.9f)
            }
            line(63f, 24f, 96f, 24f, ink, 0.9f)
        }
        Landmark.CURVES -> {
            water(47f, Color(0xFF607D8B))
            fun blob(x: Float, y: Float, w: Float, h: Float, c: Color) {
                val path = Path().apply {
                    moveTo(ox + x * s, 47f * s)
                    cubicTo(ox + (x - w * 0.2f) * s, (y + h * 0.3f) * s, ox + (x + w * 0.2f) * s, y * s, ox + (x + w * 0.5f) * s, y * s)
                    cubicTo(ox + (x + w * 0.9f) * s, y * s, ox + (x + w * 1.1f) * s, (y + h * 0.5f) * s, ox + (x + w) * s, 47f * s)
                    close()
                }
                d.drawPath(path, c)
            }
            blob(16f, 26f, 22f, 21f, ink)
            blob(34f, 16f, 20f, 31f, ink.copy(alpha = 0.85f))
            blob(50f, 22f, 26f, 25f, ink)
            blob(28f, 32f, 30f, 15f, Color.White.copy(alpha = 0.35f))
            rect(80f, 20f, 5f, 27f, ink.copy(alpha = 0.9f))
        }
        Landmark.VINEYARD -> {
            for (i in 0..12) line(-20f + i * 11f, 60f, -2f + i * 11f, 36f, ink.copy(alpha = 0.6f), 0.9f)
            rect(60f, 30f, 20f, 10f, Color(0xFFF5E6C8))
            tri(58f, 30f, 70f, 23f, 82f, 30f, Color(0xFF8D4F3A))
            rect(66f, 34f, 3f, 6f, Shade)
            rect(84f, 22f, 2f, 18f, Color(0xFF2E4D2C))
            d.drawOval(Color(0xFF2E4D2C), topLeft = p(82.5f, 14f), size = Size(5f * s, 14f * s))
        }
        Landmark.VOLCANO -> {
            poly(ink.copy(alpha = 0.85f), 4f to g, 36f to 14f, 44f to 16f, 50f to 13f, 58f to 15f, 94f to g)
            for ((x, y, r) in listOf(Triple(48f, 8f, 3f), Triple(52f, 4f, 3.6f), Triple(57f, 1f, 4f))) circle(x, y, r, Color.White.copy(alpha = 0.55f))
            rect(14f, 36f, 40f, 2.5f, Color(0xFFEFE6D8))
            for (i in 0..6) rect(15f + i * 6f, 38.5f, 2.6f, 11.5f, Color(0xFFEFE6D8))
            rect(66f, 44f, 3f, 6f, Color(0xFFEFE6D8))
            rect(74f, 41f, 3f, 9f, Color(0xFFEFE6D8))
        }
        Landmark.SNOW_PEAKS -> {
            poly(ink, -50f to g, 18f to 22f, 30f to 30f, 50f to 4f, 66f to 24f, 78f to 16f, 150f to g)
            poly(Color.White, 43f to 13f, 50f to 4f, 57f to 13f, 53.5f to 11.5f, 50f to 14f, 46.5f to 11.5f)
            poly(Color.White, 15f to 26f, 18f to 22f, 21f to 26f, 18f to 25f)
            poly(Color.White, 74f to 21f, 78f to 16f, 82f to 21f, 78f to 20f)
            for (x in listOf(10f, 16f, 84f, 90f)) tri(x - 3f, g, x, g - 10f, x + 3f, g, Color(0xFF1B4332))
            rect(30f, 42f, 14f, 8f, Color(0xFF6D4C41))
            tri(28f, 42f, 37f, 36f, 46f, 42f, Color(0xFF4E342E))
            rect(35.5f, 45f, 3f, 5f, Shade)
        }
        Landmark.HALF_TIMBERED -> {
            val wall = Color(0xFFF5E6C8)
            for (i in 0..3) {
                val x = 13f + i * 19f
                val w = 18f
                val h = 22f + (i % 2) * 5f
                val top = g - h
                rect(x, top, w, h, wall)
                tri(x - 0.5f, top, x + w / 2, top - 11f, x + w + 0.5f, top, Color(0xFF9C3D2E))
                for (y in listOf(top + 0.5f, top + h * 0.45f)) rect(x, y, w, 1f, ink)
                for (k in 0..3) rect(x + k * (w - 1f) / 3f, top, 1f, h, ink)
                line(x + 1f, top + h * 0.45f, x + w / 3f, top + 1f, ink, 0.7f)
                line(x + w - 1f, top + h * 0.45f, x + w * 2 / 3f, top + 1f, ink, 0.7f)
                rect(x + w / 2 - 2f, g - 7f, 4f, 7f, ink.copy(alpha = 0.7f))
            }
        }
        Landmark.GATE -> {
            rect(18f, 46f, 64f, 4f, ink)
            for (i in 0..5) rect(22f + i * 11f, 26f, 4f, 20f, ink)
            rect(18f, 20f, 64f, 6f, ink)
            rect(38f, 15f, 24f, 5f, ink)
            rect(45f, 10f, 10f, 5f, Color(0xFF4E6E58))
            for (x in listOf(45f, 48.5f, 52f)) rect(x, 8f, 2f, 3f, Color(0xFF4E6E58))
        }
        Landmark.AQUEDUCT -> {
            rect(-50f, 12f, 200f, 38f, ink)
            for (i in -1..8) {
                val cx = 4f + i * 12f
                rect(cx - 4f, 34f, 8f, 16f, light)
                dome(cx, 34f, 4f, light)
                rect(cx - 3f, 20f, 6f, 9f, light)
                dome(cx, 20f, 3f, light)
            }
        }
        Landmark.BASALT -> {
            water(46f, Color(0xFF37627A))
            for (i in 0..13) {
                val x = 2f + i * 6.8f
                val h = 22f - kotlin.math.abs(i - 4) * 2.2f
                if (h < 3f) continue
                val c = ink.copy(alpha = if (i % 2 == 0) 1f else 0.82f)
                rect(x, g - h, 6.4f, h, c)
                rect(x, g - h, 6.4f, 1.2f, light.copy(alpha = 0.35f))
            }
            line(0f, 53f, 100f, 53f, Color.White.copy(alpha = 0.45f), 0.4f)
        }
        Landmark.PALACE -> {
            rect(8f, 30f, 84f, 20f, ink)
            rect(8f, 28f, 84f, 2f, ink.copy(alpha = 0.85f))
            rect(40f, 22f, 20f, 28f, ink)
            tri(38f, 22f, 50f, 14f, 62f, 22f, ink)
            for (row in listOf(33f, 41f)) for (i in 0..13) {
                val x = 10.5f + i * 5.9f
                if (x in 39f..60f) continue
                rect(x, row, 2.4f, 4.5f, light.copy(alpha = 0.55f))
            }
            for (x in listOf(43f, 48.8f, 54.6f)) archOpening(x + 1.2f, 30f, 2.6f, 37f, light.copy(alpha = 0.55f))
            archOpening(50f, 42f, 5f, g, Shade)
        }
        Landmark.ARCH -> {
            line(-50f, g, 150f, g, Color.White.copy(alpha = 0.4f), 0.4f)
            rect(30f, 16f, 40f, 34f, ink)
            rect(28f, 12f, 44f, 4f, ink)
            rect(30f, 20f, 40f, 2f, Color.Black.copy(alpha = 0.12f))
            archOpening(50f, 30f, 13f, g, light)
            for (x in listOf(34f, 63f)) rect(x, 26f, 3f, 12f, Color.Black.copy(alpha = 0.12f))
            line(50f, 12f, 50f, 6f, Color(0xFF455A64), 0.3f)
            rect(50f, 6f, 1.5f, 1f, Color(0xFF1E3A8A))
            rect(51.5f, 6f, 1.5f, 1f, Color.White)
            rect(53f, 6f, 1.5f, 1f, Color(0xFFC62828))
        }
        Landmark.HILL_CHURCH -> {
            poly(Color(0xFF6E7F57), -50f to g, 14f to 36f, 36f to 26f, 64f to 26f, 86f to 36f, 150f to g)
            rect(40f, 18f, 20f, 10f, ink)
            for (x in listOf(37f, 59f)) {
                rect(x, 12f, 4f, 16f, ink)
                rect(x - 0.3f, 11f, 4.6f, 1.2f, ink)
            }
            rect(48f, 6f, 4f, 12f, ink)
            dome(50f, 6f, 2f, ink)
            circle(50f, 3f, 1f, Gold)
            for (i in 0..7) rect(6f + i * 12f, 42f + (i % 3) * 1.5f, 8f, 8f - (i % 3) * 1.5f, ink.copy(alpha = 0.55f))
        }
        Landmark.TRULLI -> {
            for ((x, w, h) in listOf(Triple(8f, 14f, 10f), Triple(25f, 16f, 12f), Triple(44f, 14f, 10f), Triple(61f, 16f, 12f), Triple(80f, 12f, 9f))) {
                val top = g - h
                rect(x, top, w, h, Color.White)
                tri(x - 1f, top, x + w / 2, top - 15f, x + w + 1f, top, ink)
                circle(x + w / 2, top - 15f, 1.1f, Color.White)
                archOpening(x + w / 2, top + 4f, 3f, g, Shade)
            }
        }
        Landmark.HARBOUR -> {
            for (i in 0..9) rect(i * 10f, 28f + (i % 3) * 2f, 10f, 12f - (i % 3) * 2f, ink.copy(alpha = 0.55f + (i % 2) * 0.1f))
            water(40f, Color(0xFF2E6E9E))
            for (i in 0..4) {
                val x = 8f + i * 18f
                poly(if (i % 2 == 0) Color.White else Color(0xFFEF8354), x to 44f, x + 12f to 44f, x + 10f to 47.5f, x + 2f to 47.5f)
                line(x + 6f, 44f, x + 6f, 26f + (i % 2) * 4f, Color(0xFF37474F), 0.5f)
                if (i % 2 == 1) tri(x + 6.4f, 28f + 4f, x + 6.4f, 42f, x + 12f, 42f, Color.White.copy(alpha = 0.85f))
            }
            line(0f, 50f, 100f, 50f, Color.White.copy(alpha = 0.35f), 0.4f)
        }
        Landmark.TRAM -> {
            val colors = listOf(Color(0xFF64B5F6), Color(0xFFFFCC80), Color(0xFFF48FB1), Color(0xFFFFF59D), Color(0xFFA5D6A7), Color(0xFFCE93D8))
            for (i in 0..6) {
                val x = i * 15f - 4f
                val top = 10f + i * 2.5f
                rect(x, top, 15f, g - top, colors[i % colors.size])
                for (wy in 0..2) rect(x + 3f, top + 4f + wy * 7f, 3f, 4f, Shade)
                for (wy in 0..2) rect(x + 9f, top + 4f + wy * 7f, 3f, 4f, Shade)
            }
            line(-50f, 22f, 150f, 26f, Color(0xFF263238), 0.3f)
            rect(30f, 34f, 40f, 13f, Color(0xFFFFC107))
            rect(31f, 32.5f, 38f, 1.8f, ink)
            for (i in 0..5) rect(32f + i * 6.2f, 36f, 4.6f, 5f, Color(0xFFE3F2FD))
            rect(30f, 43f, 40f, 1f, Color(0xFFE53935))
            for (x in listOf(37f, 63f)) circle(x, 48f, 2f, ink)
            line(48f, 32.5f, 52f, 24.5f, ink, 0.4f)
            line(52f, 24.5f, 56f, 32.5f, ink, 0.4f)
        }
        Landmark.LOCH -> {
            poly(ink.copy(alpha = 0.5f), -50f to 44f, 0f to 26f, 26f to 34f, 44f to 44f)
            poly(ink.copy(alpha = 0.4f), 46f to 44f, 70f to 30f, 100f to 22f, 150f to 44f)
            water(42f, Color(0xFF455A64))
            poly(ink, 12f to 44f, 12f to 26f, 15f to 24f, 18f to 27f, 22f to 25f, 24f to 28f, 24f to 44f)
            rect(24f, 36f, 14f, 8f, ink)
            for (x in listOf(15f, 19f)) rect(x, 30f, 2f, 3f, Shade)
            val nessie = Color(0xFF2E7D32)
            dome(64f, 47f, 3f, nessie)
            dome(71f, 47f, 2.5f, nessie)
            rect(56.5f, 39f, 1.8f, 8f, nessie)
            circle(57.8f, 39f, 1.8f, nessie)
            line(52f, 48f, 80f, 48f, Color.White.copy(alpha = 0.35f), 0.4f)
        }
        Landmark.FOREST -> {
            val fir = Color(0xFF1B4332)
            for ((x, h) in listOf(6f to 24f, 16f to 30f, 26f to 22f, 74f to 26f, 84f to 32f, 94f to 22f)) {
                for (k in 0..2) tri(x - 6f + k, g - k * h / 3.5f, x, g - h - k * 0f, x + 6f - k, g - k * h / 3.5f, fir)
            }
            rect(38f, 38f, 24f, 12f, Color(0xFFF5E6C8))
            poly(ink, 30f to 41f, 50f to 20f, 70f to 41f, 66f to 41f, 50f to 25f, 34f to 41f)
            poly(ink.copy(alpha = 0.85f), 34f to 41f, 50f to 25f, 66f to 41f, 62f to 41f, 50f to 29f, 38f to 41f)
            for (x in listOf(41f, 47f, 53f)) rect(x, 42f, 3f, 3f, Shade)
            rect(57f, 43f, 3f, 7f, ink.copy(alpha = 0.7f))
        }
        Landmark.WAREHOUSE -> {
            water(44f, Color(0xFF3D6E8C))
            val brick = ink
            rect(6f, 28f, 40f, 16f, brick)
            for (i in 0..3) tri(6f + i * 10f, 28f, 11f + i * 10f, 22f, 16f + i * 10f, 28f, brick)
            for (row in listOf(31f, 37f)) for (i in 0..7) rect(8f + i * 4.8f, row, 2f, 3f, light.copy(alpha = 0.4f))
            rect(56f, 26f, 32f, 18f, brick)
            poly(Color(0xFFB3D4E5), 56f to 26f, 56f to 14f, 62f to 10f, 69f to 15f, 75f to 8f, 82f to 13f, 88f to 11f, 88f to 26f)
            for (i in 0..5) line(58f + i * 5f, 25f, 58f + i * 5f, 15f, Color.White.copy(alpha = 0.6f), 0.4f)
        }
        Landmark.WINDMILL -> {
            for ((x, sc) in listOf(26f to 0.7f, 66f to 1f)) {
                val top = g - 30f * sc
                poly(ink, x - 6f * sc to g, x - 3.5f * sc to top, x + 3.5f * sc to top, x + 6f * sc to g)
                tri(x - 4.5f * sc, top, x, top - 6f * sc, x + 4.5f * sc, top, ink)
                val c = Offset(ox + x * s, (top - 1f * sc) * s)
                for (a in 0..3) {
                    val ang = Math.toRadians(25.0 + a * 90.0)
                    val e = Offset(c.x + (kotlin.math.cos(ang) * 18f * sc * s).toFloat(), c.y + (kotlin.math.sin(ang) * 18f * sc * s).toFloat())
                    d.drawLine(Color(0xFFF5E6C8), c, e, strokeWidth = 2.2f * sc * s)
                }
                rect(x - 1.2f * sc, g - 6f * sc, 2.4f * sc, 6f * sc, Shade)
            }
            line(-50f, 53f, 150f, 53f, Color(0xFF4A90A4), 1.2f)
        }
        Landmark.TULIPS -> {
            val colors = listOf(Color(0xFFE53935), Color(0xFFFDD835), Color(0xFFEC407A), Color(0xFFFF7043), Color(0xFFAB47BC))
            for (i in 0..9) {
                val c = colors[i % colors.size]
                poly(c, -10f + i * 12f to 60f, -2f + i * 12f to 60f, 14f + i * 7f to 42f, 10f + i * 7f to 42f)
            }
            rect(78f, 22f, 6f, 20f, ink)
            tri(76f, 22f, 81f, 16f, 86f, 22f, ink)
            for (a in listOf(-12f to -10f, 12f to 10f, -12f to 10f, 12f to -10f)) line(81f, 20f, 81f + a.first, 20f + a.second, Color(0xFFF5E6C8), 1.4f)
        }
        Landmark.CUBES -> {
            rect(0f, 40f, 100f, 3f, Color(0xFF9E9E9E))
            for (i in 0..4) {
                val cx = 14f + i * 18f
                rect(cx - 1f, 36f, 2f, 14f, Color(0xFF9E9E9E))
                poly(ink, cx to 20f, cx + 9f to 29f, cx to 38f, cx - 9f to 29f)
                poly(Color.Black.copy(alpha = 0.18f), cx to 29f, cx + 9f to 29f, cx to 38f)
                rect(cx - 2f, 26f, 4f, 3f, Color(0xFF90A4AE))
            }
        }
        Landmark.THATCH -> {
            water(46f, Color(0xFF4A90A4))
            for ((x, w) in listOf(6f to 26f, 40f to 22f, 70f to 26f)) {
                rect(x + 3f, 34f, w - 6f, 12f, Color(0xFFF5E6C8))
                poly(ink, x to 36f, x + w / 2 to 18f, x + w to 36f)
                rect(x + w / 2 - 2f, 39f, 4f, 7f, Color(0xFF2E5E3E))
            }
            d.drawArc(Color(0xFF8D6E63), 180f, 180f, false, topLeft = p(30f, 42f), size = Size(12f * s, 8f * s), style = androidx.compose.ui.graphics.drawscope.Stroke(1f * s))
            poly(Color(0xFF6D4C41), 52f to 52f, 66f to 52f, 64f to 54f, 54f to 54f)
        }
        Landmark.ATOMIUM -> {
            val pts = listOf(50f to 26f, 50f to 8f, 50f to 44f, 34f to 17f, 66f to 17f, 34f to 35f, 66f to 35f, 40f to 26f, 60f to 26f)
            for ((x, y) in pts.drop(1)) line(50f, 26f, x, y, ink, 1.2f)
            for ((a, b) in listOf(1 to 3, 1 to 4, 2 to 5, 2 to 6, 3 to 5, 4 to 6)) line(pts[a].first, pts[a].second, pts[b].first, pts[b].second, ink, 1.2f)
            line(50f, 44f, 50f, g, ink, 1.2f)
            for ((x, y) in pts) {
                circle(x, y, 3.6f, ink)
                circle(x - 1f, y - 1f, 1.2f, Color.White.copy(alpha = 0.7f))
            }
        }
        Landmark.MOUND -> {
            poly(Color(0xFF7CB342), 20f to g, 46f to 22f, 54f to 22f, 80f to g)
            for (i in 0..6) line(47f + i * 0.2f, 22f + i * 4f, 48f - i * 1.2f, 26f + i * 4f, Color(0xFF8D6E63), 0.4f)
            rect(46f, 18f, 8f, 4f, ink)
            poly(ink, 46.5f to 18f, 47f to 13f, 52f to 12f, 54f to 14f, 53.5f to 18f)
            circle(47f, 12.5f, 2.2f, ink)
        }
        Landmark.CLIFF_CHURCH -> {
            poly(Color(0xFF8D8D7A), 56f to g, 58f to 30f, 62f to 10f, 70f to 4f, 150f to 4f, 150f to g)
            rect(70f, 0f, 24f, 4f, ink.copy(alpha = 0.7f))
            water(46f, Color(0xFF4A90A4))
            rect(34f, 28f, 20f, 18f, ink)
            rect(40f, 16f, 8f, 12f, ink)
            d.drawOval(ink, topLeft = p(39f, 6f), size = Size(10f * s, 12f * s))
            tri(43f, 7f, 44f, 1f, 45f, 7f, ink)
            for (x in listOf(37f, 47f)) archOpening(x + 1.5f, 32f, 3f, 40f, light.copy(alpha = 0.5f))
            for (i in 0..3) rect(4f + i * 7f, 34f + (i % 2) * 2f, 6f, 12f - (i % 2) * 2f, ink.copy(alpha = 0.6f))
        }
        Landmark.FOUNTAIN -> {
            water(40f, Color(0xFF4A90A4))
            poly(Color.White.copy(alpha = 0.85f), 48.5f to 41f, 49.5f to 4f, 50.5f to 4f, 51.5f to 41f)
            poly(Color.White.copy(alpha = 0.4f), 49.5f to 4f, 56f to 8f, 60f to 18f, 52f to 10f)
            for (x in listOf(47f, 53f)) circle(x, 40f, 2f, Color.White.copy(alpha = 0.6f))
            rect(10f, 36f, 3f, 4f, ink)
            line(46f, 41f, 54f, 41f, ink, 0.6f)
            poly(ink.copy(alpha = 0.5f), -50f to 40f, 0f to 30f, 20f to 34f, 40f to 40f)
            poly(ink.copy(alpha = 0.4f), 60f to 40f, 90f to 28f, 150f to 40f)
        }
        Landmark.RAILWAY -> {
            poly(ink, -50f to g, 14f to 16f, 30f to 28f, 50f to 6f, 72f to 26f, 86f to 14f, 150f to g)
            poly(Color.White, 44f to 14f, 50f to 6f, 56f to 14f, 50f to 12f)
            poly(Color.White, 10f to 22f, 14f to 16f, 18f to 22f)
            poly(Color.White, 82f to 20f, 86f to 14f, 90f to 20f)
            line(-50f, 46f, 150f, 40f, Color(0xFF37474F), 0.6f)
            for (i in 0..1) {
                val x = 30f + i * 18f
                poly(Color(0xFFD32F2F), x to 45f - i * 1.1f, x + 16f to 44f - i * 1.1f, x + 16f to 37f - i * 1.1f, x to 38f - i * 1.1f)
                for (k in 0..2) rect(x + 2f + k * 4.6f, 39.2f - i * 1.1f, 3f, 2.6f, Color(0xFFE3F2FD))
            }
        }
        Landmark.LAKE_VILLAGE -> {
            poly(ink.copy(alpha = 0.8f), -50f to 44f, 10f to 8f, 30f to 20f, 54f to 4f, 80f to 18f, 150f to 44f)
            poly(Color.White.copy(alpha = 0.8f), 50f to 8f, 54f to 4f, 58f to 8f)
            water(44f, Color(0xFF4A7C8C))
            val houses = listOf(Color(0xFFF5E6C8), Color(0xFFFFCC80), Color(0xFFBCAAA4), Color(0xFFF5F5F5), Color(0xFFFFE0B2))
            for (i in 0..6) {
                val x = 8f + i * 8f + (if (i > 3) 14f else 0f)
                val top = 32f + (i % 3) * 2f
                rect(x, top, 7f, 44f - top, houses[i % houses.size])
                tri(x - 0.5f, top, x + 3.5f, top - 4f, x + 7.5f, top, Color(0xFF5D4037))
            }
            rect(44f, 20f, 4f, 24f, Color(0xFFF5F5F5))
            tri(43.5f, 20f, 46f, 8f, 48.5f, 20f, Color(0xFF455A64))
            line(0f, 49f, 100f, 49f, Color.White.copy(alpha = 0.35f), 0.4f)
        }
        Landmark.SKI_JUMP -> {
            poly(ink.copy(alpha = 0.55f), -50f to g, 0f to 20f, 30f to 30f, 70f to 14f, 150f to g)
            val ramp = Path().apply {
                moveTo(ox + 30f * s, 22f * s)
                quadraticTo(ox + 52f * s, 30f * s, ox + 60f * s, 40f * s)
                quadraticTo(ox + 70f * s, 46f * s, ox + 96f * s, 48f * s)
            }
            d.drawPath(ramp, Color.White, style = androidx.compose.ui.graphics.drawscope.Stroke(2f * s))
            rect(26f, 10f, 3f, 14f, ink)
            poly(ink, 22f to 10f, 34f to 8f, 36f to 12f, 24f to 14f)
            circle(64f, 30f, 1f, Color(0xFFD32F2F))
            line(63f, 31f, 67f, 29f, Color(0xFF263238), 0.5f)
        }
        Landmark.CANYON -> {
            poly(ink, -50f to 60f, -50f to 10f, 40f to 10f, 44f to 60f)
            poly(ink.copy(alpha = 0.8f), 56f to 60f, 60f to 10f, 150f to 10f, 150f to 60f)
            rect(44f, 10f, 12f, 50f, Color(0xFF1E88A8))
            for (y in listOf(20f, 30f, 40f)) {
                line(20f, y, 41f, y + 1f, Color.Black.copy(alpha = 0.15f), 0.5f)
                line(59f, y + 1f, 80f, y, Color.Black.copy(alpha = 0.15f), 0.5f)
            }
            poly(Color.White, 47f to 50f, 53f to 50f, 52f to 54f, 48f to 54f)
            rect(49f, 47f, 2f, 3f, Color.White)
            line(38f, 18f, 62f, 18f, Color(0xFF455A64), 1f)
        }
        Landmark.LION_GATE -> {
            for (row in 0..4) for (i in 0..5) {
                val w = 13f
                val x = 10f + i * w + (row % 2) * 4f
                if (x in 38f..58f && row >= 1) continue
                rect(x, 22f + row * 5.6f, w - 0.8f, 5f, ink.copy(alpha = 0.85f + (i + row) % 2 * 0.15f))
            }
            rect(38f, 28f, 3f, 22f, ink)
            rect(57f, 28f, 3f, 22f, ink)
            rect(36f, 24f, 26f, 4f, ink)
            tri(40f, 24f, 49f, 10f, 58f, 24f, ink.copy(alpha = 0.9f))
            rect(48f, 13f, 2f, 10f, light.copy(alpha = 0.5f))
            for (x in listOf(43f, 53f)) circle(x, 19f, 2f, light.copy(alpha = 0.4f))
        }
        Landmark.RUINS -> {
            poly(Color(0xFF8D8D7A), -50f to g, 0f to 30f, 30f to 22f, 70f to 18f, 150f to 36f, 150f to g)
            rect(28f, 44f, 44f, 3f, ink)
            rect(30f, 41f, 40f, 3f, ink)
            for ((i, h) in listOf(16f, 22f, 22f, 6f, 10f).withIndex()) {
                rect(33f + i * 8f, 41f - h, 3.2f, h, ink)
            }
            rect(38f, 17f, 20f, 2.5f, ink)
            rect(70f, 47f, 8f, 3f, ink.copy(alpha = 0.8f))
        }
        Landmark.ACROPOLIS -> {
            poly(Color(0xFFB5A27A), 6f to g, 18f to 32f, 30f to 26f, 72f to 26f, 84f to 32f, 94f to g)
            rect(32f, 22f, 36f, 3f, ink)
            for (i in 0..7) rect(33f + i * 4.6f, 13f, 2f, 9f, ink)
            rect(32f, 11f, 36f, 2f, ink)
            tri(31f, 11f, 50f, 6f, 69f, 11f, ink)
            for (i in 0..8) rect(2f + i * 11f, 44f + (i % 3), 9f, 6f - (i % 3), Color.White.copy(alpha = 0.75f))
        }
        Landmark.PINNACLES -> {
            for ((x, w, top) in listOf(Triple(10f, 18f, 20f), Triple(34f, 16f, 12f), Triple(58f, 18f, 24f), Triple(80f, 16f, 16f))) {
                val path = Path().apply {
                    moveTo(ox + x * s, g * s)
                    quadraticTo(ox + (x - 2f) * s, (top + 8f) * s, ox + (x + 3f) * s, top * s)
                    lineTo(ox + (x + w - 3f) * s, top * s)
                    quadraticTo(ox + (x + w + 2f) * s, (top + 8f) * s, ox + (x + w) * s, g * s)
                    close()
                }
                d.drawPath(path, ink)
            }
            rect(37f, 6f, 10f, 6f, Color(0xFFD7CCB7))
            tri(36f, 6f, 42f, 2f, 48f, 6f, Color(0xFFA1503A))
            rect(83f, 10f, 9f, 6f, Color(0xFFD7CCB7))
            tri(82f, 10f, 87.5f, 6f, 93f, 10f, Color(0xFFA1503A))
            line(47f, 9f, 60f, 26f, Color(0xFF263238), 0.3f)
        }
        Landmark.BLUE_DOMES -> {
            water(44f, Color(0xFF1565C0))
            poly(Color(0xFF8D6E63), -50f to 60f, -50f to 30f, 70f to 34f, 80f to 44f, 80f to 60f)
            val white = Color(0xFFFAFAFA)
            for (i in 0..7) {
                val x = 2f + i * 8f
                val top = 18f + (i % 3) * 4f
                rect(x, top, 7.5f, 34f - top, white)
                rect(x + 2.5f, top + 3f, 2f, 3f, Color(0xFF1565C0).copy(alpha = 0.6f))
            }
            rect(30f, 16f, 12f, 12f, white)
            dome(36f, 16f, 6f, ink)
            rect(35.6f, 6f, 0.8f, 4f, white)
            rect(56f, 18f, 8f, 8f, white)
            dome(60f, 18f, 4f, ink)
        }
        else -> Unit
    }
}
