/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.sentier.data.CompletionResult
import app.sentier.data.TaskList
import app.sentier.data.TaskRepository
import app.sentier.data.TaskWithCounts
import app.sentier.ui.common.app
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TodayState(
    val today: LocalDate,
    val todo: List<TaskWithCounts> = emptyList(),
    val done: List<TaskWithCounts> = emptyList(),
    val lists: Map<Long, TaskList> = emptyMap(),
    val loading: Boolean = true,
) {
    val total: Int get() = todo.size + done.size
}

class TodayViewModel(private val repo: TaskRepository) : ViewModel() {

    private val today = repo.today()

    val state: StateFlow<TodayState> = combine(repo.observeToday(), repo.observeLists()) { tasks, lists ->
        val todayDay = today.toEpochDay()
        val (done, todo) = tasks.partition { it.completedAt != null || it.lastCompletedDay == todayDay && (it.dueDay ?: 0) > todayDay }
        TodayState(today, todo, done, lists.associateBy { it.id }, loading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayState(today))

    private val _completions = MutableSharedFlow<CompletionResult>(extraBufferCapacity = 1)
    /** Validations, pour afficher « Bravo » et proposer « Annuler ». */
    val completions: SharedFlow<CompletionResult> = _completions

    /** Dernière validation de chaque tâche répétée, pour pouvoir la décocher. */
    private val recentCompletions = mutableMapOf<Long, CompletionResult>()

    fun toggle(task: TaskWithCounts, isDone: Boolean) = viewModelScope.launch {
        if (isDone) {
            val previous = recentCompletions.remove(task.id)
            when {
                task.completedAt != null -> repo.uncomplete(task.id)
                previous != null -> repo.undo(previous)
            }
        } else {
            repo.complete(task.id)?.let {
                if (it.nextDue != null) recentCompletions[task.id] = it
                _completions.tryEmit(it)
            }
        }
    }

    fun undo(result: CompletionResult) = viewModelScope.launch {
        recentCompletions.remove(result.before.id)
        repo.undo(result)
    }

    fun add(title: String) = viewModelScope.launch { repo.addTask(title, dueDay = today) }

    companion object {
        val Factory = viewModelFactory { initializer { TodayViewModel(app().tasks) } }
    }
}
