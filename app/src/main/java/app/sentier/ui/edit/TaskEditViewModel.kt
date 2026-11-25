/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.sentier.data.CompletionResult
import app.sentier.data.Task
import app.sentier.data.TaskList
import app.sentier.data.TaskRepository
import app.sentier.domain.Recurrence
import app.sentier.ui.common.app
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TaskEditState(
    val task: Task? = null,
    val subtasks: List<Task> = emptyList(),
    val lists: List<TaskList> = emptyList(),
    val loading: Boolean = true,
)

class TaskEditViewModel(private val repo: TaskRepository, private val taskId: Long) : ViewModel() {

    val today: LocalDate = repo.today()

    val state: StateFlow<TaskEditState> = combine(
        repo.observeTask(taskId), repo.observeSubtasks(taskId), repo.observeLists(),
    ) { task, subs, lists -> TaskEditState(task, subs, lists, loading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskEditState())

    private val _completions = MutableSharedFlow<CompletionResult>(extraBufferCapacity = 1)
    val completions: SharedFlow<CompletionResult> = _completions

    private fun edit(change: (Task) -> Task) = viewModelScope.launch {
        val current = state.value.task ?: return@launch
        repo.update(change(current))
    }

    fun setTitle(title: String) { if (title.isNotBlank()) edit { it.copy(title = title.trim()) } }
    fun setNotes(notes: String) = edit { it.copy(notes = notes) }
    fun setPriority(priority: Int) = edit { it.copy(priority = priority) }
    fun setList(listId: Long?) = edit { it.copy(listId = listId) }

    fun setDueDate(date: LocalDate?) = edit {
        // Une tâche répétée garde toujours une échéance.
        if (date == null && it.recurrence != null) it else it.copy(dueDay = date?.toEpochDay())
    }

    fun setRecurrence(recurrence: Recurrence?, fromCompletion: Boolean) = edit {
        it.copy(
            recurrence = recurrence?.toRRule(),
            repeatFromCompletion = recurrence != null && fromCompletion,
            dueDay = if (recurrence != null && it.dueDay == null) today.toEpochDay() else it.dueDay,
        )
    }

    fun toggleDone() = viewModelScope.launch {
        val task = state.value.task ?: return@launch
        if (task.isCompleted) repo.uncomplete(task.id)
        else repo.complete(task.id)?.let { _completions.tryEmit(it) }
    }

    fun undo(result: CompletionResult) = viewModelScope.launch { repo.undo(result) }

    fun addSubtask(title: String) = viewModelScope.launch {
        val task = state.value.task ?: return@launch
        repo.addTask(title, listId = task.listId, parentId = task.id)
    }

    fun toggleSubtask(sub: Task) = viewModelScope.launch { repo.toggleSubtask(sub) }
    fun deleteSubtask(sub: Task) = viewModelScope.launch { repo.delete(sub) }
    fun renameSubtask(sub: Task, title: String) = viewModelScope.launch {
        if (title.isNotBlank()) repo.update(sub.copy(title = title.trim()))
    }

    fun delete(onDone: () -> Unit) = viewModelScope.launch {
        state.value.task?.let { repo.delete(it) }
        onDone()
    }

    companion object {
        fun factory(taskId: Long) = viewModelFactory { initializer { TaskEditViewModel(app().tasks, taskId) } }
    }
}
