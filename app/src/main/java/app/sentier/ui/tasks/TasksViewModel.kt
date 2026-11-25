/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.sentier.data.CompletionResult
import app.sentier.data.TaskList
import app.sentier.data.TaskRepository
import app.sentier.data.TaskWithCounts
import app.sentier.ui.common.app
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** Un groupe de tâches affiché sous un titre (« En retard », « Demain »…). */
data class TaskGroup(val title: String, val tasks: List<TaskWithCounts>, val late: Boolean = false)

data class TasksState(
    val today: LocalDate,
    val lists: List<TaskList> = emptyList(),
    val selectedListId: Long? = null,
    val groups: List<TaskGroup> = emptyList(),
    val completed: List<TaskWithCounts> = emptyList(),
    val showCompleted: Boolean = false,
    val loading: Boolean = true,
) {
    val listsById: Map<Long, TaskList> get() = lists.associateBy { it.id }
    val isEmpty: Boolean get() = groups.isEmpty() && completed.isEmpty()
}

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModel(private val repo: TaskRepository) : ViewModel() {

    private val today = repo.today()
    private val selectedList = MutableStateFlow<Long?>(null)
    private val showCompleted = MutableStateFlow(false)

    val state: StateFlow<TasksState> = combine(
        repo.observeLists(),
        selectedList,
        selectedList.flatMapLatest { repo.observeTasks(it) },
        showCompleted,
    ) { lists, selected, tasks, show ->
        val (completed, open) = tasks.partition { it.completedAt != null }
        TasksState(
            today = today,
            lists = lists,
            selectedListId = selected?.takeIf { id -> lists.any { it.id == id } },
            groups = groupByDate(open, today),
            completed = completed.sortedByDescending { it.completedAt },
            showCompleted = show,
            loading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TasksState(today))

    private val _completions = MutableSharedFlow<CompletionResult>(extraBufferCapacity = 1)
    val completions: SharedFlow<CompletionResult> = _completions

    fun selectList(id: Long?) { selectedList.value = id }
    fun toggleShowCompleted() { showCompleted.value = !showCompleted.value }

    fun toggle(task: TaskWithCounts) = viewModelScope.launch {
        if (task.completedAt != null) repo.uncomplete(task.id)
        else repo.complete(task.id)?.let { _completions.tryEmit(it) }
    }

    fun undo(result: CompletionResult) = viewModelScope.launch { repo.undo(result) }

    fun add(title: String) = viewModelScope.launch { repo.addTask(title, listId = state.value.selectedListId) }

    fun addList(name: String, color: Long) = viewModelScope.launch {
        if (name.isNotBlank()) selectedList.value = repo.addList(name, color)
    }

    fun updateList(list: TaskList) = viewModelScope.launch { repo.updateList(list) }

    fun deleteList(list: TaskList) = viewModelScope.launch {
        if (selectedList.value == list.id) selectedList.value = null
        repo.deleteList(list)
    }

    companion object {
        val Factory = viewModelFactory { initializer { TasksViewModel(app().tasks) } }

        fun groupByDate(tasks: List<TaskWithCounts>, today: LocalDate): List<TaskGroup> {
            val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            val buckets = linkedMapOf<String, MutableList<TaskWithCounts>>()
            for (t in tasks) {
                val due = t.dueDay?.let(LocalDate::ofEpochDay)
                val key = when {
                    due == null -> "Sans date"
                    due.isBefore(today) -> "En retard"
                    due == today -> "Aujourd'hui"
                    due == today.plusDays(1) -> "Demain"
                    !due.isAfter(endOfWeek) -> "Cette semaine"
                    else -> "Plus tard"
                }
                buckets.getOrPut(key) { mutableListOf() }.add(t)
            }
            val order = listOf("En retard", "Aujourd'hui", "Demain", "Cette semaine", "Plus tard", "Sans date")
            return order.mapNotNull { k -> buckets[k]?.let { TaskGroup(k, it, late = k == "En retard") } }
        }
    }
}
