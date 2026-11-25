/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Une liste de tâches (Perso, Maison…). */
@Entity(tableName = "task_list")
data class TaskList(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Couleur ARGB. */
    val color: Long,
    val position: Int = 0,
)

/**
 * Une tâche. Les sous-tâches sont des tâches avec un [parentId].
 * Les dates sont stockées en « epoch day » (jours depuis le 01/01/1970).
 */
@Entity(
    tableName = "task",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["parent_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TaskList::class,
            parentColumns = ["id"],
            childColumns = ["list_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("parent_id"), Index("list_id"), Index("due_day")],
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    @ColumnInfo(name = "list_id") val listId: Long? = null,
    /** 0 = aucune, 1 = basse, 2 = moyenne, 3 = haute (comme les couleurs de Tasks.org). */
    val priority: Int = 0,
    /** 1 = facile, 2 = moyen, 3 = difficile (utilisé par le jeu à l'étape 3). */
    val difficulty: Int = 1,
    @ColumnInfo(name = "due_day") val dueDay: Long? = null,
    /** Règle de répétition RRULE simplifiée, null si pas de répétition. */
    val recurrence: String? = null,
    /** Répéter à partir de la date de fin plutôt que de l'échéance. */
    @ColumnInfo(name = "repeat_from_completion") val repeatFromCompletion: Boolean = false,
    @ColumnInfo(name = "parent_id") val parentId: Long? = null,
    /** Date/heure de fin (millisecondes), null si à faire. Jamais rempli pour une tâche répétée. */
    @ColumnInfo(name = "completed_at") val completedAt: Long? = null,
    /** Dernier jour où la tâche a été validée (même répétée). */
    @ColumnInfo(name = "last_completed_day") val lastCompletedDay: Long? = null,
    /** Nombre total de validations. */
    @ColumnInfo(name = "completion_count") val completionCount: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    val position: Int = 0,
) {
    val isCompleted: Boolean get() = completedAt != null
}

/** Ligne de liste : une tâche avec le compte de ses sous-tâches. */
data class TaskWithCounts(
    val id: Long,
    val title: String,
    val notes: String,
    @ColumnInfo(name = "list_id") val listId: Long?,
    val priority: Int,
    @ColumnInfo(name = "due_day") val dueDay: Long?,
    val recurrence: String?,
    @ColumnInfo(name = "completed_at") val completedAt: Long?,
    @ColumnInfo(name = "last_completed_day") val lastCompletedDay: Long?,
    @ColumnInfo(name = "subtask_count") val subtaskCount: Int,
    @ColumnInfo(name = "subtask_done") val subtaskDone: Int,
)
