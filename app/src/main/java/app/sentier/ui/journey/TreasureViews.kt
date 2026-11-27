/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.journey

import app.sentier.domain.tr
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.EmojiObjects
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FlutterDash
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material.icons.filled.Woman
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.sentier.domain.Rarity
import app.sentier.domain.Treasure
import app.sentier.domain.TreasureSource
import java.util.Locale

/** Icônes des trésors (icônes Material). */
object TreasureIcons {
    private val byName: Map<String, ImageVector> = mapOf(
        "compass" to Icons.Filled.Explore,
        "stars" to Icons.Filled.NightsStay,
        "seal" to Icons.Filled.Approval,
        "sun" to Icons.Filled.WbSunny,
        "laurel" to Icons.Filled.Eco,
        "bowl" to Icons.Filled.LocalCafe,
        "doll" to Icons.Filled.Woman,
        "glass" to Icons.Filled.WineBar,
        "bird" to Icons.Filled.FlutterDash,
        "scroll" to Icons.Filled.HistoryEdu,
        "map" to Icons.Filled.TravelExplore,
        "drum" to Icons.Filled.GraphicEq,
        "egg" to Icons.Filled.Egg,
        "lamp" to Icons.Filled.EmojiObjects,
        "hat" to Icons.Filled.Checkroom,
        "rain" to Icons.Filled.WaterDrop,
        "moai" to Icons.Filled.Face,
    )

    fun of(name: String): ImageVector = byName[name] ?: Icons.Filled.AutoAwesome
}

/** Couleur de chaque rareté, lisible sur fond clair et sombre. */
fun Rarity.color(): Color = when (this) {
    Rarity.RARE -> Color(0xFF1E88E5)
    Rarity.EPIC -> Color(0xFF8E24AA)
    Rarity.LEGENDARY -> Color(0xFFE0A100)
}

/** Condition en quelques mots, sous un trésor pas encore gagné. */
private fun Treasure.shortHowTo(): String = when (source) {
    TreasureSource.LEVEL -> tr("Niveau $goal", "Level $goal")
    TreasureSource.STREAK -> tr("Série de $goal j", "$goal-day streak")
    TreasureSource.BADGES -> "$goal badges"
    TreasureSource.CHALLENGES -> if (goal == 1) tr("1 défi", "1 challenge") else tr("$goal défis", "$goal challenges")
}

@Composable
fun TreasureBadge(treasure: Treasure, size: Int) {
    val rarity = treasure.rarity.color()
    val owned = treasure.owned
    Box(
        Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(if (owned) rarity.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant)
            .border(2.dp, if (owned) rarity else MaterialTheme.colorScheme.outlineVariant, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (owned) TreasureIcons.of(treasure.icon) else Icons.Filled.Lock,
            contentDescription = null,
            tint = if (owned) rarity else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size((size * 0.5).dp),
        )
    }
}

/** Les trésors, 4 par ligne : ceux gagnés, et ceux qui attendent (avec ce qu'il faut faire). */
@Composable
fun TreasureGrid(treasures: List<Treasure>, onOpen: (Treasure) -> Unit) {
    Column(Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        treasures.chunked(4).forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEach { t ->
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { onOpen(t) }
                            .padding(vertical = 4.dp)
                            .semantics {
                                contentDescription = if (t.owned) tr("Trésor ${t.rarity.label.lowercase(app.sentier.domain.Lang.locale)} : ${t.name}", "${t.rarity.label} treasure: ${t.name}") else tr("Trésor à gagner : ${t.howTo}", "Treasure to earn: ${t.howTo}")
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        TreasureBadge(t, size = 48)
                        Text(
                            if (t.owned) t.name else t.shortHowTo(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (t.owned) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp, start = 2.dp, end = 2.dp),
                        )
                    }
                }
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** Fiche d'un trésor. Son anecdote reste cachée tant qu'il n'est pas gagné. */
@Composable
fun TreasureDialog(treasure: Treasure, title: String, footer: String?, onDismiss: () -> Unit) {
    val rarity = treasure.rarity.color()
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { TreasureBadge(treasure, size = 64) },
        title = { Text(title, textAlign = TextAlign.Center) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "${treasure.rarity.label.uppercase(app.sentier.domain.Lang.locale)} · ${treasure.origin}",
                    style = MaterialTheme.typography.labelMedium,
                    color = rarity,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(treasure.description, style = MaterialTheme.typography.bodyMedium)
                HorizontalDivider()
                if (treasure.owned) {
                    Row {
                        Icon(
                            Icons.Filled.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp).padding(top = 2.dp),
                        )
                        Column(Modifier.padding(start = 10.dp)) {
                            Text(tr("Le savais-tu ?", "Did you know?"), style = MaterialTheme.typography.labelLarge)
                            Text(treasure.anecdote, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    Text(
                        tr("Pour le gagner : ${treasure.howTo.replaceFirstChar { it.lowercase(app.sentier.domain.Lang.locale) }} ", "To earn it: ${treasure.howTo.replaceFirstChar { it.lowercase(app.sentier.domain.Lang.locale) }} ") +
                            "(${treasure.current.coerceAtMost(treasure.goal)} / ${treasure.goal}).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                footer?.let {
                    Text(it, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(if (footer != null) tr("Super !", "Great!") else tr("Fermer", "Close")) } },
    )
}
