/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Carte « bientôt » pour les onglets des prochaines étapes. */
@Composable
fun ComingSoon(
    title: String,
    icon: ImageVector,
    headline: String,
    lines: List<String>,
    modifier: Modifier = Modifier,
    extra: @Composable () -> Unit = {},
) {
    Column(modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        Text(
            title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp),
        )
        Card(Modifier.fillMaxWidth().padding(16.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                }
                Text(headline, style = MaterialTheme.typography.titleLarge)
                for (line in lines) {
                    Text("• $line", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        extra()
    }
}
