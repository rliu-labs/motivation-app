/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

private const val WITH_COUNTS = """
    SELECT t.id, t.title, t.notes, t.list_id, t.priority, t.due_day, t.recurrence,
           t.completed_at, t.last_completed_day,
           (SELECT COUNT(*) FROM task s WHERE s.parent_id = t.id) AS subtask_count,
           (SELECT COUNT(*) FROM task s WHERE s.parent_id = t.id AND s.completed_at IS NOT NULL) AS subtask_done
    FROM task t
"""

@Dao
interface TaskDao {
    @Query("SELECT COUNT(*) FROM task")
    suspend fun countTasks(): Int

    /** Tâches à faire aujourd'hui ou en retard, plus celles validées aujourd'hui. */
    @Query(
        WITH_COUNTS + """
        WHERE t.parent_id IS NULL
          AND ((t.completed_at IS NULL AND t.due_day IS NOT NULL AND t.due_day <= :today)
               OR t.last_completed_day = :today)
        ORDER BY t.priority DESC, t.due_day, t.position, t.id
        """
    )
    fun observeToday(today: Long): Flow<List<TaskWithCounts>>

    /** Toutes les tâches principales, filtrées par liste si [listId] n'est pas null. */
    @Query(
        WITH_COUNTS + """
        WHERE t.parent_id IS NULL AND (:listId IS NULL OR t.list_id = :listId)
        ORDER BY t.completed_at IS NOT NULL, t.due_day IS NULL, t.due_day, t.priority DESC, t.position, t.id
        """
    )
    fun observeTasks(listId: Long?): Flow<List<TaskWithCounts>>

    @Query("SELECT * FROM task WHERE id = :id")
    fun observeTask(id: Long): Flow<Task?>

    @Query("SELECT * FROM task WHERE id = :id")
    suspend fun getTask(id: Long): Task?

    @Query("SELECT * FROM task WHERE parent_id = :parentId ORDER BY position, id")
    fun observeSubtasks(parentId: Long): Flow<List<Task>>

    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("UPDATE task SET completed_at = NULL WHERE parent_id = :parentId")
    suspend fun resetSubtasks(parentId: Long)

    @Query("SELECT COALESCE(MAX(position), 0) FROM task WHERE parent_id IS :parentId")
    suspend fun maxPosition(parentId: Long?): Int

    // --- Listes ---

    @Query("SELECT * FROM task_list ORDER BY position, id")
    fun observeLists(): Flow<List<TaskList>>

    @Insert
    suspend fun insertList(list: TaskList): Long

    @Update
    suspend fun updateList(list: TaskList)

    @Delete
    suspend fun deleteList(list: TaskList)

}
