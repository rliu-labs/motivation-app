/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.progress

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.sentier.ui.common.ComingSoon

@Composable
fun ProgressScreen(onOpenCredits: () -> Unit) {
    ComingSoon(
        title = "Progrès",
        icon = Icons.Filled.EmojiEvents,
        headline = "Le jeu arrive à l'étape 3",
        lines = listOf(
            "Des points d'expérience et des niveaux à chaque tâche validée.",
            "De l'or à dépenser dans ta propre boutique de récompenses.",
            "Des séries de jours, avec des jours de repos pour ne jamais tout perdre.",
            "Des badges à collectionner.",
        ),
    ) {
        ListItem(
            headlineContent = { Text("Crédits et licence") },
            supportingContent = { Text("Logiciel libre, GPL-3.0") },
            leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onOpenCredits).padding(horizontal = 4.dp),
        )
    }
}
