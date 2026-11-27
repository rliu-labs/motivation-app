/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.sentier.data.AutoBackup
import app.sentier.data.BackupRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(onBack: () -> Unit, vm: BackupViewModel = viewModel(factory = BackupViewModel.Factory)) {
    val state by vm.state.collectAsStateWithLifecycle()
    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let(vm::export)
    }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(vm::open)
    }
    val auto by vm.auto.collectAsStateWithLifecycle()
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let(vm::chooseFolder)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sauvegarde") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour") } },
            )
        },
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Tes données restent sur ton téléphone. Pour les garder en cas de perte ou les passer sur un nouveau téléphone, " +
                    "enregistre une sauvegarde dans un fichier, puis range-le où tu veux (Drive, ordinateur, clé USB…).",
                style = MaterialTheme.typography.bodyLarge,
            )

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Exporter", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Tâches, habitudes, pas, XP, or, badges, voyage et réglages, dans un seul fichier.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Button(
                        onClick = { exporter.launch(BackupRepository.fileName(LocalDate.now())) },
                        enabled = !state.busy,
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Exporter mes données")
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Importer", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Remplace toutes les données de l'appli par celles d'une sauvegarde. On te demande de confirmer avant.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    OutlinedButton(
                        onClick = { importer.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
                        enabled = !state.busy,
                    ) {
                        Icon(Icons.Filled.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Importer une sauvegarde")
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val a = auto.settings
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Sauvegarde automatique", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Switch(
                            checked = a.on && a.folder != null,
                            onCheckedChange = { on -> if (on) folderPicker.launch(null) else vm.turnOffAuto() },
                            enabled = !state.busy,
                            modifier = Modifier.semantics { contentDescription = "Sauvegarde automatique" },
                        )
                    }
                    Text(
                        "Chaque semaine, Sentier enregistre une sauvegarde dans le dossier de ton choix " +
                            "et garde les ${AutoBackup.KEEP} dernières. Choisis un dossier synchronisé pour l'avoir aussi ailleurs.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (a.on && a.folder != null) {
                        Text(
                            "Dossier : " + (auto.folderName ?: "inaccessible"),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            a.lastAt?.let { "Dernière sauvegarde : " + formatDate(it) } ?: "Pas encore de sauvegarde.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        a.error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = vm::backUpNow, enabled = !state.busy) { Text("Sauvegarder maintenant") }
                            TextButton(onClick = { folderPicker.launch(null) }, enabled = !state.busy) { Text("Changer de dossier") }
                        }
                    }
                }
            }

            if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            state.message?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (state.error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
            }

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp).padding(top = 2.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Le fichier n'est pas chiffré et contient toutes tes données : ne le partage qu'avec toi-même.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    state.pending?.let { file ->
        val date = file.createdAt?.let(::formatDate)
        AlertDialog(
            onDismissRequest = vm::cancelRestore,
            title = { Text("Remplacer tes données ?") },
            text = {
                Text(
                    (date?.let { "Sauvegarde du $it : " } ?: "Cette sauvegarde contient ") +
                        "${file.tasks} tâches, ${file.habits} habitudes et ${file.events} actions dans l'historique du jeu. " +
                        "Toutes les données actuelles de l'appli seront remplacées.",
                )
            },
            confirmButton = { TextButton(onClick = vm::confirmRestore) { Text("Remplacer") } },
            dismissButton = { TextButton(onClick = vm::cancelRestore) { Text("Annuler") } },
        )
    }
}

private fun formatDate(millis: Long): String =
    DateTimeFormatter.ofPattern("d MMMM yyyy 'à' HH:mm", Locale.FRANCE).format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()))
