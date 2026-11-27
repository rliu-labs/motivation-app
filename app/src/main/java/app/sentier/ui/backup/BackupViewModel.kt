/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.backup

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.sentier.SentierApplication
import app.sentier.data.AutoBackup
import app.sentier.data.AutoBackupSettings
import app.sentier.data.BackupException
import app.sentier.data.BackupFile
import app.sentier.notify.HabitReminders
import app.sentier.notify.Reminders
import app.sentier.ui.common.app
import app.sentier.widget.Widgets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AutoBackupUi(val settings: AutoBackupSettings = AutoBackupSettings(), val folderName: String? = null)

data class BackupUiState(
    val busy: Boolean = false,
    /** Sauvegarde lue, en attente de confirmation avant de remplacer les données. */
    val pending: BackupFile? = null,
    /** Résultat de la dernière opération, à afficher. */
    val message: String? = null,
    val error: Boolean = false,
)

class BackupViewModel(private val sentier: SentierApplication) : AndroidViewModel(sentier) {
    private val _state = MutableStateFlow(BackupUiState())
    val state: StateFlow<BackupUiState> = _state

    fun export(uri: Uri) = viewModelScope.launch {
        _state.value = BackupUiState(busy = true)
        _state.value = try {
            val text = sentier.backup.export()
            withContext(Dispatchers.IO) {
                sentier.contentResolver.openOutputStream(uri, "wt")?.use { it.write(text.toByteArray(Charsets.UTF_8)) }
                    ?: throw BackupException("Impossible d'écrire le fichier.")
            }
            BackupUiState(message = "Sauvegarde enregistrée. Garde ce fichier en lieu sûr : il contient toutes tes données.")
        } catch (e: BackupException) {
            BackupUiState(message = e.message, error = true)
        } catch (e: Exception) {
            BackupUiState(message = "Impossible d'écrire le fichier.", error = true)
        }
    }

    /** Lit le fichier choisi et demande confirmation avant de remplacer quoi que ce soit. */
    fun open(uri: Uri) = viewModelScope.launch {
        _state.value = BackupUiState(busy = true)
        _state.value = try {
            val text = withContext(Dispatchers.IO) {
                sentier.contentResolver.openInputStream(uri)?.use { input ->
                    val bytes = input.readNBytesCompat(MAX_BYTES + 1)
                    if (bytes.size > MAX_BYTES) throw BackupException("Ce fichier est trop gros pour être une sauvegarde de Sentier.")
                    String(bytes, Charsets.UTF_8)
                } ?: throw BackupException("Impossible de lire ce fichier.")
            }
            BackupUiState(pending = sentier.backup.read(text))
        } catch (e: BackupException) {
            BackupUiState(message = e.message, error = true)
        } catch (e: Exception) {
            BackupUiState(message = "Impossible de lire ce fichier.", error = true)
        }
    }

    fun confirmRestore() {
        val file = _state.value.pending ?: return
        viewModelScope.launch {
            _state.value = BackupUiState(busy = true)
            _state.value = try {
                sentier.backup.restore(file)
                Reminders.apply(sentier, sentier.settings.currentReminders())
                runCatching { HabitReminders.rescheduleAll(sentier) }
                if (Widgets.anyPlaced(sentier)) runCatching { Widgets.updateAll(sentier) }
                BackupUiState(message = "Données restaurées : ${file.tasks} tâches, ${file.habits} habitudes et tout ton historique.")
            } catch (e: BackupException) {
                BackupUiState(message = e.message, error = true)
            } catch (e: Exception) {
                BackupUiState(message = "La restauration a échoué : rien n'a été modifié.", error = true)
            }
        }
    }

    fun cancelRestore() {
        _state.value = BackupUiState()
    }

    /** Sauvegarde automatique, avec le nom du dossier choisi. */
    val auto: StateFlow<AutoBackupUi> = sentier.settings.autoBackup
        .map { s -> AutoBackupUi(s, s.folder?.let { withContext(Dispatchers.IO) { AutoBackup.folderName(sentier, Uri.parse(it)) } }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AutoBackupUi())

    /** Un dossier vient d'être choisi : la sauvegarde automatique démarre, avec une première sauvegarde tout de suite. */
    fun chooseFolder(folder: Uri) = viewModelScope.launch {
        val ok = runCatching { AutoBackup.keepAccess(sentier, folder) }.isSuccess
        if (!ok) {
            _state.value = BackupUiState(message = "Sentier ne peut pas écrire dans ce dossier. Choisis-en un autre.", error = true)
            return@launch
        }
        sentier.settings.setAutoBackup(true, folder.toString())
        runCatching { AutoBackup.schedule(sentier, true) }
        backUpNow()
    }

    fun turnOffAuto() = viewModelScope.launch {
        sentier.settings.setAutoBackup(false, null)
        runCatching { AutoBackup.schedule(sentier, false) }
    }

    fun backUpNow() = viewModelScope.launch {
        _state.value = BackupUiState(busy = true)
        val error = AutoBackup.run(sentier)
        _state.value = if (error == null) {
            BackupUiState(message = "Sauvegarde automatique faite. La prochaine aura lieu dans une semaine.")
        } else {
            BackupUiState(message = error, error = true)
        }
    }

    companion object {
        private const val MAX_BYTES = 30_000_000

        val Factory = viewModelFactory { initializer { BackupViewModel(app()) } }
    }
}

/** Lit au plus [limit] octets (InputStream.readNBytes n'existe qu'à partir d'Android 13). */
private fun java.io.InputStream.readNBytesCompat(limit: Int): ByteArray {
    val out = java.io.ByteArrayOutputStream()
    val buffer = ByteArray(64 * 1024)
    while (out.size() < limit) {
        val n = read(buffer, 0, minOf(buffer.size, limit - out.size()))
        if (n < 0) break
        out.write(buffer, 0, n)
    }
    return out.toByteArray()
}
