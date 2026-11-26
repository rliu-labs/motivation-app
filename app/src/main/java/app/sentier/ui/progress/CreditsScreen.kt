/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.progress

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp

private data class Credit(val name: String, val what: String, val license: String, val url: String)

private val Credits = listOf(
    Credit(
        "Tasks.org", "Inspiration pour la gestion des tâches : priorités en couleur, sous-tâches, listes, règles de répétition.",
        "GPL-3.0", "https://github.com/tasks/tasks",
    ),
    Credit(
        "Habitica", "Inspiration pour le jeu : expérience, niveaux, or et boutique de récompenses. Aucun graphisme de Habitica n'est utilisé.",
        "GPL-3.0", "https://github.com/HabitRPG/habitica-android",
    ),
    Credit(
        "Loop Habit Tracker", "Inspiration pour le style Material et la liste d'habitudes. Le calcul du score de force des habitudes reprend sa formule (Copyright © Álinson Santos Xavier).",
        "GPL-3.0", "https://github.com/iSoron/uhabits",
    ),
    Credit(
        "Natural Earth", "Contours des terres utilisés pour dessiner la carte du tour du monde.",
        "Domaine public", "https://www.naturalearthdata.com",
    ),
    Credit(
        "Android Jetpack, Compose et icônes Material", "Bibliothèques et icônes de Google.",
        "Apache 2.0", "https://developer.android.com/jetpack",
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsScreen(onBack: () -> Unit) {
    val uri = LocalUriHandler.current
    val context = LocalContext.current
    val version = runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crédits et licence") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour") } },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
            Column(Modifier.padding(20.dp)) {
                Text("Sentier ${version ?: ""}", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Sentier est un logiciel libre, publié sous la licence GNU GPL version 3 ou ultérieure. " +
                        "Tu peux l'utiliser, l'étudier, le modifier et le partager, à condition de partager aussi son code source sous la même licence. " +
                        "Il est fourni sans aucune garantie.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    "Toutes tes données restent sur ton téléphone.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            HorizontalDivider()
            Text(
                "Merci à ces projets",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 4.dp),
            )
            for (c in Credits) {
                ListItem(
                    headlineContent = { Text(c.name) },
                    supportingContent = { Text("${c.what}\nLicence : ${c.license}") },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Ouvrir") },
                    modifier = Modifier.clickable { uri.openUri(c.url) },
                )
            }
            ListItem(
                headlineContent = { Text("Texte complet de la licence GPL-3.0") },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Ouvrir") },
                modifier = Modifier.clickable { uri.openUri("https://www.gnu.org/licenses/gpl-3.0.html") },
            )
        }
    }
}
