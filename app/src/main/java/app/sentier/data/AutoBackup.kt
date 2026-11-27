/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.data

import app.sentier.domain.tr
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.sentier.SentierApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * Sauvegarde automatique : chaque semaine, toutes les données sont écrites dans le dossier choisi
 * (sur le téléphone, une carte SD ou un dossier synchronisé). Les quatre dernières sont gardées.
 */
object AutoBackup {
    private const val WORK = "sauvegarde-auto"
    const val PREFIX = "sentier-auto-"
    const val KEEP = 4

    /** Nom du fichier d'une sauvegarde automatique. */
    fun fileName(day: LocalDate): String = "$PREFIX$day.json"

    /** Fichiers à effacer pour n'en garder que [KEEP] (les noms datés se trient dans l'ordre). */
    fun toDelete(names: List<String>): List<String> =
        names.filter { it.startsWith(PREFIX) && it.endsWith(".json") }.sortedDescending().drop(KEEP)

    fun schedule(context: Context, on: Boolean, policy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE) {
        val wm = WorkManager.getInstance(context)
        if (!on) {
            wm.cancelUniqueWork(WORK)
            return
        }
        val request = PeriodicWorkRequestBuilder<AutoBackupWorker>(7, TimeUnit.DAYS)
            .setConstraints(Constraints.Builder().setRequiresStorageNotLow(true).build())
            .build()
        wm.enqueueUniquePeriodicWork(WORK, policy, request)
    }

    /** À l'ouverture de l'appli : s'assure que la sauvegarde hebdomadaire est programmée. */
    suspend fun ensureScheduled(app: SentierApplication) {
        val s = app.settings.currentAutoBackup()
        if (s.on && s.folder != null) schedule(app, true, ExistingPeriodicWorkPolicy.KEEP)
    }

    /** Garde l'accès au dossier choisi, même après un redémarrage. */
    fun keepAccess(context: Context, folder: Uri) {
        context.contentResolver.takePersistableUriPermission(
            folder, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
        )
    }

    /** Nom lisible du dossier choisi, ou null s'il n'est plus accessible. */
    fun folderName(context: Context, folder: Uri): String? = runCatching {
        val doc = DocumentsContract.buildDocumentUriUsingTree(folder, DocumentsContract.getTreeDocumentId(folder))
        context.contentResolver.query(doc, arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        }
    }.getOrNull()

    /** Écrit une sauvegarde maintenant. Renvoie un message d'erreur, ou null si tout s'est bien passé. */
    suspend fun run(app: SentierApplication, now: Long = System.currentTimeMillis()): String? {
        val s = app.settings.currentAutoBackup()
        val folder = s.folder?.let(Uri::parse) ?: return tr("Choisis d'abord un dossier.", "Choose a folder first.")
        val error = try {
            val text = app.backup.export(now)
            withContext(Dispatchers.IO) { write(app, folder, fileName(LocalDate.now()), text) }
            null
        } catch (e: SecurityException) {
            tr("Sentier n'a plus accès au dossier choisi : choisis-le à nouveau.", "Sentier no longer has access to the chosen folder: choose it again.")
        } catch (e: Exception) {
            tr("La sauvegarde automatique n'a pas pu être écrite dans le dossier choisi.", "The automatic backup could not be written to the chosen folder.")
        }
        app.settings.setAutoBackupResult(if (error == null) now else null, error)
        return error
    }

    private fun write(context: Context, folder: Uri, name: String, text: String) {
        val resolver = context.contentResolver
        val treeId = DocumentsContract.getTreeDocumentId(folder)
        val parent = DocumentsContract.buildDocumentUriUsingTree(folder, treeId)
        val children = DocumentsContract.buildChildDocumentsUriUsingTree(folder, treeId)
        val existing = mutableMapOf<String, Uri>()
        resolver.query(
            children,
            arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME),
            null, null, null,
        )?.use { c ->
            while (c.moveToNext()) {
                existing[c.getString(1)] = DocumentsContract.buildDocumentUriUsingTree(folder, c.getString(0))
            }
        }
        // Celle du jour remplace une éventuelle sauvegarde du même jour.
        val target = existing[name]
            ?: DocumentsContract.createDocument(resolver, parent, "application/json", name)
            ?: throw IllegalStateException(tr("Création du fichier impossible", "Could not create the file"))
        resolver.openOutputStream(target, "wt")?.use { it.write(text.toByteArray(Charsets.UTF_8)) }
            ?: throw IllegalStateException(tr("Écriture impossible", "Could not write"))
        for (old in toDelete(existing.keys.toList() + name)) {
            existing[old]?.let { runCatching { DocumentsContract.deleteDocument(resolver, it) } }
        }
    }
}

class AutoBackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as SentierApplication
        val s = app.settings.currentAutoBackup()
        if (!s.on || s.folder == null) return Result.success()
        AutoBackup.run(app)
        return Result.success()
    }
}
