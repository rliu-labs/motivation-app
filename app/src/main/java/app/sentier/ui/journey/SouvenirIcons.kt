/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.journey

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiFoodBeverage
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.Toys
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sailing
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Soap
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.ui.graphics.vector.ImageVector

/** Icônes des souvenirs (icônes Material). */
object SouvenirIcons {
    private val byName: Map<String, ImageVector> = mapOf(
        "passport" to Icons.Filled.CardTravel,
        "shoe" to Icons.Filled.Hiking,
        "coffee" to Icons.Filled.Coffee,
        "jewel" to Icons.Filled.Diamond,
        "eye" to Icons.Filled.RemoveRedEye,
        "sand" to Icons.Filled.HourglassBottom,
        "pottery" to Icons.Filled.EmojiFoodBeverage,
        "textile" to Icons.Filled.Checkroom,
        "spice" to Icons.Filled.LocalFlorist,
        "flag" to Icons.Filled.Flag,
        "tea" to Icons.Filled.EmojiFoodBeverage,
        "mask" to Icons.Filled.TheaterComedy,
        "toy" to Icons.Filled.Toys,
        "light" to Icons.Filled.Lightbulb,
        "music" to Icons.Filled.MusicNote,
        "art" to Icons.Filled.Palette,
        "car" to Icons.Filled.LocalTaxi,
        "globe" to Icons.Filled.Public,
        "wine" to Icons.Filled.WineBar,
        "food" to Icons.Filled.Restaurant,
        "soap" to Icons.Filled.Soap,
        "key" to Icons.Filled.Key,
        "bike" to Icons.AutoMirrored.Filled.DirectionsBike,
        "boat" to Icons.Filled.Sailing,
        "book" to Icons.AutoMirrored.Filled.MenuBook,
        "sun" to Icons.Filled.WbSunny,
        "clock" to Icons.Filled.Schedule,
    )

    /** Noms connus, pour vérifier que chaque souvenir a son icône. */
    val names: Set<String> get() = byName.keys

    fun of(name: String): ImageVector = byName[name] ?: Icons.Filled.AutoAwesome
}
