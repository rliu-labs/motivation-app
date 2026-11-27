/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.room.withTransaction
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.Instant

/** Une sauvegarde lue et vérifiée, prête à être restaurée. */
class BackupFile internal constructor(
    internal val json: JSONObject,
    /** Date de la sauvegarde (millisecondes), si connue. */
    val createdAt: Long?,
    val tasks: Int,
    val habits: Int,
    val events: Int,
)

/** Une sauvegarde illisible ou incompatible, avec un message à montrer tel quel. */
class BackupException(message: String) : Exception(message)

/**
 * Sauvegarde de toutes les données dans un fichier JSON lisible, et restauration.
 * Rien ne quitte le téléphone : c'est l'utilisateur qui choisit où ranger le fichier.
 */
class BackupRepository(
    private val db: AppDatabase,
    private val settings: Settings,
) {
    /** Toutes les données, en JSON. */
    suspend fun export(now: Long = System.currentTimeMillis()): String {
        val tables = JSONObject()
        db.withTransaction {
            val sql = db.openHelper.readableDatabase
            for (table in TABLES) {
                val rows = JSONArray()
                sql.query("SELECT * FROM `$table`").use { c ->
                    while (c.moveToNext()) rows.put(rowOf(c))
                }
                tables.put(table, rows)
            }
        }
        return JSONObject()
            .put("format", FORMAT)
            .put("schema", SCHEMA)
            .put("created_at", now)
            .put("created_at_text", Instant.ofEpochMilli(now).toString())
            .put("settings", settings.exportJson())
            .put("tables", tables)
            .toString(1)
    }

    /** Lit et vérifie une sauvegarde, sans rien modifier. */
    fun read(text: String): BackupFile {
        if (text.length > MAX_CHARS) throw BackupException("Ce fichier est trop gros pour être une sauvegarde de Sentier.")
        val json = try {
            JSONObject(text)
        } catch (e: JSONException) {
            throw BackupException("Ce fichier n'est pas une sauvegarde de Sentier.")
        }
        if (json.optString("format") != FORMAT) throw BackupException("Ce fichier n'est pas une sauvegarde de Sentier.")
        val schema = json.optInt("schema", -1)
        if (schema > SCHEMA) throw BackupException("Cette sauvegarde vient d'une version plus récente de Sentier. Mets l'appli à jour, puis réessaie.")
        if (schema < MIN_SCHEMA) throw BackupException("Cette sauvegarde vient d'une version trop ancienne de Sentier.")
        val tables = json.optJSONObject("tables") ?: throw BackupException("Cette sauvegarde est incomplète.")
        if (REQUIRED_TABLES.any { tables.optJSONArray(it) == null }) throw BackupException("Cette sauvegarde est incomplète.")
        return BackupFile(
            json = json,
            createdAt = json.optLong("created_at", 0L).takeIf { it > 0 },
            tasks = tables.getJSONArray("task").length(),
            habits = tables.getJSONArray("habit").length(),
            events = tables.getJSONArray("game_event").length(),
        )
    }

    /** Remplace toutes les données par celles de la sauvegarde. Tout ou rien : en cas d'erreur, rien ne change. */
    suspend fun restore(file: BackupFile) {
        val tables = file.json.getJSONObject("tables")
        try {
            db.withTransaction {
                val sql = db.openHelper.writableDatabase
                // Les liens (sous-tâches, listes, valeurs des habitudes) ne sont vérifiés qu'à la fin.
                sql.execSQL("PRAGMA defer_foreign_keys = ON")
                for (table in TABLES.reversed()) sql.execSQL("DELETE FROM `$table`")
                for (table in TABLES) {
                    val columns = columnsOf(table)
                    // Une table ajoutée après la sauvegarde (les notes, par exemple) reste vide.
                    val rows = tables.optJSONArray(table) ?: JSONArray()
                    for (i in 0 until rows.length()) {
                        val row = rows.optJSONObject(i) ?: throw BackupException("Cette sauvegarde est abîmée.")
                        sql.insert(table, SQLiteDatabase.CONFLICT_ABORT, valuesOf(row, columns))
                    }
                }
            }
        } catch (e: BackupException) {
            throw e
        } catch (e: Exception) {
            throw BackupException("Cette sauvegarde est abîmée : rien n'a été modifié.")
        }
        file.json.optJSONObject("settings")?.let { settings.importJson(it) }
    }

    private fun columnsOf(table: String): Set<String> {
        val names = mutableSetOf<String>()
        db.openHelper.readableDatabase.query("PRAGMA table_info(`$table`)").use { c ->
            val nameIndex = c.getColumnIndexOrThrow("name")
            while (c.moveToNext()) names += c.getString(nameIndex)
        }
        return names
    }

    companion object {
        const val FORMAT = "sentier-sauvegarde"

        /** Version du schéma de la base (voir AppDatabase). */
        const val SCHEMA = 5

        /** Plus ancienne version encore lisible : les colonnes ajoutées depuis gardent leur valeur par défaut. */
        const val MIN_SCHEMA = 3

        /** Une sauvegarde de plusieurs années tient en quelques Mo ; au-delà, ce n'est pas une sauvegarde. */
        const val MAX_CHARS = 30_000_000

        /** Tables sauvegardées, les tables liées après celles dont elles dépendent. */
        val TABLES = listOf("task_list", "task", "habit", "habit_log", "habit_note", "daily_steps", "game_event", "reward")

        /** Tables présentes dans toute sauvegarde lisible (les notes n'existent que depuis le schéma 5). */
        private val REQUIRED_TABLES = TABLES - "habit_note"

        /** Nom proposé pour le fichier de sauvegarde. */
        fun fileName(day: java.time.LocalDate): String = "sentier-sauvegarde-$day.json"

        private fun rowOf(c: Cursor): JSONObject {
            val o = JSONObject()
            for (i in 0 until c.columnCount) {
                val name = c.getColumnName(i)
                when (c.getType(i)) {
                    Cursor.FIELD_TYPE_NULL -> o.put(name, JSONObject.NULL)
                    Cursor.FIELD_TYPE_INTEGER -> o.put(name, c.getLong(i))
                    Cursor.FIELD_TYPE_FLOAT -> o.put(name, c.getDouble(i))
                    Cursor.FIELD_TYPE_STRING -> o.put(name, c.getString(i))
                    else -> Unit // Aucune donnée binaire dans la base.
                }
            }
            return o
        }

        /** Seules les colonnes qui existent vraiment sont reprises : un nom inconnu est ignoré. */
        private fun valuesOf(row: JSONObject, columns: Set<String>): ContentValues {
            val values = ContentValues()
            for (name in row.keys()) {
                if (name !in columns) continue
                when (val v = row.get(name)) {
                    JSONObject.NULL -> values.putNull(name)
                    is Int -> values.put(name, v.toLong())
                    is Long -> values.put(name, v)
                    is Double -> values.put(name, v)
                    is Float -> values.put(name, v.toDouble())
                    is Boolean -> values.put(name, if (v) 1L else 0L)
                    is String -> values.put(name, v)
                    is Number -> values.put(name, v.toDouble())
                    else -> throw BackupException("Cette sauvegarde est abîmée.")
                }
            }
            return values
        }
    }
}
