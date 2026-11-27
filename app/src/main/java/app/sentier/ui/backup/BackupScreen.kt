/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.backup

import app.sentier.domain.tr
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
                title = { Text(tr("Sauvegarde", "Backup")) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, tr("Retour", "Back")) } },
            )
        },
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                tr("Tes données restent sur ton téléphone. Pour les garder en cas de perte ou les passer sur un nouveau téléphone, ", "Your data stays on your phone. To keep it safe or move it to a new phone, ") +
                    tr("enregistre une sauvegarde dans un fichier, puis range-le où tu veux (Drive, ordinateur, clé USB…).", "save a backup to a file, then keep it wherever you like (Drive, computer, USB stick…)."),
                style = MaterialTheme.typography.bodyLarge,
            )

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(tr("Exporter", "Export"), style = MaterialTheme.typography.titleMedium)
                    Text(
                        tr("Tâches, habitudes, pas, XP, or, badges, voyage et réglages, dans un seul fichier.", "Tasks, habits, steps, XP, gold, badges, journey and settings, in a single file."),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Button(
                        onClick = { exporter.launch(BackupRepository.fileName(LocalDate.now())) },
                        enabled = !state.busy,
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(tr("Exporter mes données", "Export my data"))
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(tr("Importer", "Import"), style = MaterialTheme.typography.titleMedium)
                    Text(
                        tr("Remplace toutes les données de l'appli par celles d'une sauvegarde. On te demande de confirmer avant.", "Replaces all the app's data with a backup's. You are asked to confirm first."),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    OutlinedButton(
                        onClick = { importer.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
                        enabled = !state.busy,
                    ) {
                        Icon(Icons.Filled.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(tr("Importer une sauvegarde", "Import a backup"))
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val a = auto.settings
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(tr("Sauvegarde automatique", "Automatic backup"), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Switch(
                            checked = a.on && a.folder != null,
                            onCheckedChange = { on -> if (on) folderPicker.launch(null) else vm.turnOffAuto() },
                            enabled = !state.busy,
                            modifier = Modifier.semantics { contentDescription = tr("Sauvegarde automatique", "Automatic backup") },
                        )
                    }
                    Text(
                        tr("Chaque semaine, Sentier enregistre une sauvegarde dans le dossier de ton choix ", "Every week, Sentier saves a backup to the folder of your choice ") +
                            tr("et garde les ${AutoBackup.KEEP} dernières. Choisis un dossier synchronisé pour l'avoir aussi ailleurs.", "and keeps the last ${AutoBackup.KEEP}. Pick a synced folder to have it elsewhere too."),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (a.on && a.folder != null) {
                        Text(
                            tr("Dossier : ", "Folder: ") + (auto.folderName ?: "inaccessible"),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            a.lastAt?.let { tr("Dernière sauvegarde : ", "Last backup: ") + formatDate(it) } ?: tr("Pas encore de sauvegarde.", "No backup yet."),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        a.error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = vm::backUpNow, enabled = !state.busy) { Text(tr("Sauvegarder maintenant", "Back up now")) }
                            TextButton(onClick = { folderPicker.launch(null) }, enabled = !state.busy) { Text(tr("Changer de dossier", "Change folder")) }
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
                    tr("Le fichier n'est pas chiffré et contient toutes tes données : ne le partage qu'avec toi-même.", "The file is not encrypted and holds all your data: share it only with yourself."),
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
            title = { Text(tr("Remplacer tes données ?", "Replace your data?")) },
            text = {
                Text(
                    (date?.let { tr("Sauvegarde du $it : ", "Backup from $it: ") } ?: tr("Cette sauvegarde contient ", "This backup contains ")) +
                        tr("${file.tasks} tâches, ${file.habits} habitudes et ${file.events} actions dans l'historique du jeu. ", "${file.tasks} tasks, ${file.habits} habits and ${file.events} actions in the game history. ") +
                        tr("Toutes les données actuelles de l'appli seront remplacées.", "All the app's current data will be replaced."),
                )
            },
            confirmButton = { TextButton(onClick = vm::confirmRestore) { Text(tr("Remplacer", "Replace")) } },
            dismissButton = { TextButton(onClick = vm::cancelRestore) { Text(tr("Annuler", "Cancel")) } },
        )
    }
}

private fun formatDate(millis: Long): String =
    DateTimeFormatter.ofPattern(tr("d MMMM yyyy 'à' HH:mm", "d MMMM yyyy 'at' HH:mm"), app.sentier.domain.Lang.locale).format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()))
