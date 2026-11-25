/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.habits

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhonelinkOff
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

/** Icônes proposées pour les habitudes (icônes Material). */
object HabitIcons {
    val all: List<Pair<String, ImageVector>> = listOf(
        "check" to Icons.Filled.CheckCircle,
        "water" to Icons.Filled.WaterDrop,
        "sleep" to Icons.Filled.Bedtime,
        "meditation" to Icons.Filled.SelfImprovement,
        "sport" to Icons.Filled.FitnessCenter,
        "book" to Icons.AutoMirrored.Filled.MenuBook,
        "phone_off" to Icons.Filled.PhonelinkOff,
        "walk" to Icons.AutoMirrored.Filled.DirectionsWalk,
        "food" to Icons.Filled.Restaurant,
        "heart" to Icons.Filled.Favorite,
        "music" to Icons.Filled.MusicNote,
        "sun" to Icons.Filled.WbSunny,
    )
    private val byName = all.toMap()

    fun of(name: String): ImageVector = byName[name] ?: Icons.Filled.CheckCircle
}
