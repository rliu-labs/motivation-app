/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.progress

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Tv
import androidx.compose.ui.graphics.vector.ImageVector

/** Icônes des badges et des récompenses (icônes Material). */
object GameIcons {
    private val byName: Map<String, ImageVector> = mapOf(
        "flag" to Icons.Filled.Flag,
        "task" to Icons.Filled.TaskAlt,
        "rocket" to Icons.Filled.RocketLaunch,
        "spa" to Icons.Filled.SelfImprovement,
        "walk" to Icons.AutoMirrored.Filled.DirectionsWalk,
        "fire" to Icons.Filled.LocalFireDepartment,
        "calendar" to Icons.Filled.CalendarMonth,
        "star" to Icons.Filled.Star,
        "trophy" to Icons.Filled.EmojiEvents,
        "gift" to Icons.Filled.CardGiftcard,
        "tv" to Icons.Filled.Tv,
        "cake" to Icons.Filled.Cake,
        "game" to Icons.Filled.SportsEsports,
        "map" to Icons.Filled.Map,
        "globe" to Icons.Filled.Public,
        "bolt" to Icons.Filled.Bolt,
        "explore" to Icons.Filled.Explore,
        "sport" to Icons.Filled.FitnessCenter,
    )

    fun of(name: String): ImageVector = byName[name] ?: Icons.Filled.Redeem
}
