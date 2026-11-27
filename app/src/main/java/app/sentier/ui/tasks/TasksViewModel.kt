/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.tasks

import app.sentier.domain.tr
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
import app.sentier.data.Settings
import app.sentier.data.TaskView
import java.text.Normalizer
import java.time.DayOfWeek
import java.time.YearMonth
import java.util.Locale
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
    val view: TaskView = TaskView.BY_DATE,
    /** Tâches à faire dans « Mon ordre ». */
    val ordered: List<TaskWithCounts> = emptyList(),
    /** Recherche en cours (vide = pas de recherche). */
    val query: String = "",
    val results: List<TaskWithCounts> = emptyList(),
    /** Calendrier : mois affiché, jour choisi et tâches à faire par jour (epoch day). */
    val month: YearMonth = YearMonth.from(today),
    val selectedDay: LocalDate = today,
    val dueByDay: Map<Long, List<TaskWithCounts>> = emptyMap(),
) {
    val listsById: Map<Long, TaskList> get() = lists.associateBy { it.id }
    val isEmpty: Boolean get() = groups.isEmpty() && completed.isEmpty()
    val searching: Boolean get() = query.isNotBlank()
    val selectedDayTasks: List<TaskWithCounts> get() = dueByDay[selectedDay.toEpochDay()].orEmpty()
}

/** Réglages d'affichage de l'onglet, réunis pour le calcul de l'état. */
private data class ViewPrefs(val view: TaskView, val query: String, val month: YearMonth, val day: LocalDate)

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModel(private val repo: TaskRepository, private val settings: Settings? = null) : ViewModel() {

    private val today = repo.today()
    private val selectedList = MutableStateFlow<Long?>(null)
    private val showCompleted = MutableStateFlow(false)
    private val localView = MutableStateFlow(TaskView.BY_DATE)
    private val query = MutableStateFlow("")
    private val month = MutableStateFlow(YearMonth.from(today))
    private val selectedDay = MutableStateFlow(today)

    private val prefs = combine(settings?.taskView ?: localView, query, month, selectedDay) { v, q, m, d -> ViewPrefs(v, q, m, d) }

    val state: StateFlow<TasksState> = combine(
        repo.observeLists(),
        selectedList,
        selectedList.flatMapLatest { repo.observeTasks(it) },
        showCompleted,
        prefs,
    ) { lists, selected, tasks, show, p ->
        val (completed, open) = tasks.partition { it.completedAt != null }
        TasksState(
            today = today,
            lists = lists,
            selectedListId = selected?.takeIf { id -> lists.any { it.id == id } },
            groups = groupByDate(open, today),
            completed = completed.sortedByDescending { it.completedAt },
            showCompleted = show,
            loading = false,
            view = p.view,
            ordered = manualOrder(open),
            query = p.query,
            results = search(tasks, p.query),
            month = p.month,
            selectedDay = p.day,
            dueByDay = dueByDay(open),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TasksState(today))

    fun setView(view: TaskView) {
        localView.value = view
        settings?.let { viewModelScope.launch { it.setTaskView(view) } }
    }

    fun setQuery(text: String) { query.value = text }

    fun showMonth(m: YearMonth) { month.value = m }

    fun selectDay(day: LocalDate) {
        selectedDay.value = day
        month.value = YearMonth.from(day)
    }

    /** Nouvel ordre choisi en faisant glisser les tâches. */
    fun reorder(ids: List<Long>) = viewModelScope.launch { repo.reorder(ids) }

    private val _completions = MutableSharedFlow<CompletionResult>(extraBufferCapacity = 1)
    val completions: SharedFlow<CompletionResult> = _completions

    fun selectList(id: Long?) { selectedList.value = id }
    fun toggleShowCompleted() { showCompleted.value = !showCompleted.value }

    fun toggle(task: TaskWithCounts) = viewModelScope.launch {
        if (task.completedAt != null) repo.uncomplete(task.id)
        else repo.complete(task.id)?.let { _completions.tryEmit(it) }
    }

    fun undo(result: CompletionResult) = viewModelScope.launch { repo.undo(result) }

    /** Ajoute une tâche ; dans le calendrier, elle est prévue le jour choisi. */
    fun add(title: String) = viewModelScope.launch {
        val s = state.value
        val due = if (s.view == TaskView.CALENDAR && !s.searching) s.selectedDay else null
        repo.addTask(title, listId = s.selectedListId, dueDay = due)
    }

    fun addList(name: String, color: Long) = viewModelScope.launch {
        if (name.isNotBlank()) selectedList.value = repo.addList(name, color)
    }

    fun updateList(list: TaskList) = viewModelScope.launch { repo.updateList(list) }

    fun deleteList(list: TaskList) = viewModelScope.launch {
        if (selectedList.value == list.id) selectedList.value = null
        repo.deleteList(list)
    }

    companion object {
        val Factory = viewModelFactory { initializer { TasksViewModel(app().tasks, app().settings) } }

        /** Tâches à faire dans l'ordre choisi à la main (les nouvelles arrivent en bas). */
        fun manualOrder(open: List<TaskWithCounts>): List<TaskWithCounts> = open.sortedWith(compareBy({ it.position }, { it.id }))

        /** Tâches à faire, rangées par jour d'échéance. */
        fun dueByDay(open: List<TaskWithCounts>): Map<Long, List<TaskWithCounts>> =
            open.filter { it.dueDay != null }.groupBy { it.dueDay!! }
                .mapValues { (_, l) -> l.sortedWith(compareByDescending<TaskWithCounts> { it.priority }.thenBy { it.position }) }

        /** Recherche dans les titres et les notes, sans tenir compte des majuscules ni des accents. */
        fun search(tasks: List<TaskWithCounts>, query: String): List<TaskWithCounts> {
            val words = normalize(query).split(' ').filter { it.isNotBlank() }
            if (words.isEmpty()) return emptyList()
            return tasks.filter { t ->
                val text = normalize(t.title + " " + t.notes)
                words.all { it in text }
            }.sortedWith(compareBy({ it.completedAt != null }, { it.dueDay == null }, { it.dueDay }))
        }

        private fun normalize(s: String): String =
            Normalizer.normalize(s.lowercase(Locale.FRANCE), Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")

        fun groupByDate(tasks: List<TaskWithCounts>, today: LocalDate): List<TaskGroup> {
            val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            val buckets = linkedMapOf<String, MutableList<TaskWithCounts>>()
            for (t in tasks) {
                val due = t.dueDay?.let(LocalDate::ofEpochDay)
                val key = when {
                    due == null -> tr("Sans date", "No date")
                    due.isBefore(today) -> tr("En retard", "Overdue")
                    due == today -> tr("Aujourd'hui", "Today")
                    due == today.plusDays(1) -> tr("Demain", "Tomorrow")
                    !due.isAfter(endOfWeek) -> tr("Cette semaine", "This week")
                    else -> tr("Plus tard", "Later")
                }
                buckets.getOrPut(key) { mutableListOf() }.add(t)
            }
            val order = listOf(tr("En retard", "Overdue"), tr("Aujourd'hui", "Today"), tr("Demain", "Tomorrow"), tr("Cette semaine", "This week"), tr("Plus tard", "Later"), tr("Sans date", "No date"))
            return order.mapNotNull { k -> buckets[k]?.let { TaskGroup(k, it, late = k == tr("En retard", "Overdue")) } }
        }
    }
}
