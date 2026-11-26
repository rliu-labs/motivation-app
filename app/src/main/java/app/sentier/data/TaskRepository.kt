/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.data

import app.sentier.domain.Recurrence
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** Résultat d'une validation, pour pouvoir l'annuler et l'annoncer. */
data class CompletionResult(
    val before: Task,
    /** Nouvelle échéance si la tâche est répétée. */
    val nextDue: LocalDate?,
    /** XP et or gagnés (null sans le jeu). */
    val gain: Gain? = null,
)

class TaskRepository(
    private val dao: TaskDao,
    private val game: GameRepository? = null,
    private val clock: () -> LocalDate = { LocalDate.now() },
) {
    fun today(): LocalDate = clock()

    fun observeToday(): Flow<List<TaskWithCounts>> = dao.observeToday(today().toEpochDay())
    fun observeTasks(listId: Long?): Flow<List<TaskWithCounts>> = dao.observeTasks(listId)
    fun observeTask(id: Long): Flow<Task?> = dao.observeTask(id)
    fun observeSubtasks(parentId: Long): Flow<List<Task>> = dao.observeSubtasks(parentId)
    fun observeLists(): Flow<List<TaskList>> = dao.observeLists()

    suspend fun addTask(title: String, listId: Long? = null, dueDay: LocalDate? = null, parentId: Long? = null): Long {
        val clean = title.trim()
        require(clean.isNotEmpty())
        return dao.insert(
            Task(
                title = clean,
                listId = listId,
                dueDay = dueDay?.toEpochDay(),
                parentId = parentId,
                position = dao.maxPosition(parentId) + 1,
            )
        )
    }

    suspend fun update(task: Task) = dao.update(task)

    suspend fun delete(task: Task) = dao.delete(task)

    /**
     * Valide une tâche. Une tâche répétée n'est pas marquée finie : elle passe
     * à sa prochaine échéance et ses sous-tâches sont remises à zéro
     * (comportement de Tasks.org).
     */
    suspend fun complete(id: Long): CompletionResult? {
        val task = dao.getTask(id) ?: return null
        if (task.isCompleted) return null
        val today = today()
        val recurrence = Recurrence.parse(task.recurrence)
        return if (recurrence != null && task.parentId == null) {
            val next = recurrence.nextDueDate(task.dueDay?.let(LocalDate::ofEpochDay), today, task.repeatFromCompletion)
            dao.update(
                task.copy(
                    dueDay = next.toEpochDay(),
                    lastCompletedDay = today.toEpochDay(),
                    completionCount = task.completionCount + 1,
                )
            )
            dao.resetSubtasks(task.id)
            CompletionResult(task, next, game?.onTaskCompleted(task))
        } else {
            dao.update(
                task.copy(
                    completedAt = System.currentTimeMillis(),
                    lastCompletedDay = today.toEpochDay(),
                    completionCount = task.completionCount + 1,
                )
            )
            CompletionResult(task, null, game?.onTaskCompleted(task))
        }
    }

    /** Remet une tâche finie à faire. */
    suspend fun uncomplete(id: Long) {
        val task = dao.getTask(id) ?: return
        if (!task.isCompleted) return
        dao.update(
            task.copy(
                completedAt = null,
                lastCompletedDay = null,
                completionCount = (task.completionCount - 1).coerceAtLeast(0),
            )
        )
        game?.onTaskUndone(id)
    }

    /** Annule une validation (bouton « Annuler »). */
    suspend fun undo(result: CompletionResult) {
        dao.update(result.before)
        game?.onTaskUndone(result.before.id)
    }

    suspend fun toggleSubtask(subtask: Task) {
        dao.update(
            subtask.copy(completedAt = if (subtask.isCompleted) null else System.currentTimeMillis())
        )
    }

    suspend fun addList(name: String, color: Long): Long =
        dao.insertList(TaskList(name = name.trim(), color = color, position = Int.MAX_VALUE / 2))

    suspend fun updateList(list: TaskList) = dao.updateList(list)
    suspend fun deleteList(list: TaskList) = dao.deleteList(list)
}
