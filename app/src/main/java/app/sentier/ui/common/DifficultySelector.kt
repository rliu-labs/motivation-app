/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import app.sentier.domain.GameRules

/** Choix de la difficulté (1 facile, 2 moyen, 3 difficile), avec l'XP rapportée. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DifficultySelector(selected: Int, onSelect: (Int) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for ((value, label) in listOf(1 to "Facile", 2 to "Moyen", 3 to "Difficile")) {
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                label = { Text("$label · ${GameRules.xpFor(value)} XP") },
            )
        }
    }
}
