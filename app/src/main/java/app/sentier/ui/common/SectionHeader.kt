/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = color,
        modifier = modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp),
    )
}
