/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.habits

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.sentier.data.Habit
import app.sentier.data.HabitNote
import app.sentier.data.HabitRepository
import app.sentier.data.HabitWithStats
import app.sentier.domain.HabitTemplate
import app.sentier.notify.HabitReminders
import app.sentier.ui.common.app
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Création (id = 0) ou modification d'une habitude. */
class HabitEditViewModel(
    private val repo: HabitRepository,
    val habitId: Long,
    /** Contexte pour programmer le rappel (null dans les tests). */
    private val context: Context? = null,
) : ViewModel() {

    val isNew: Boolean get() = habitId == 0L

    private val _draft = MutableStateFlow<Habit?>(
        if (habitId == 0L) Habit(name = "", color = 0xFF2E7D5B, createdDay = repo.today().toEpochDay()) else null
    )
    /** Habitude en cours de modification (null pendant le chargement). */
    val draft: StateFlow<Habit?> = _draft

    val stats: StateFlow<HabitWithStats?> =
        (if (habitId == 0L) flowOf(null) else repo.observeHabitWithStats(habitId))
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Journal de l'habitude, la note la plus récente en premier. */
    val notes: StateFlow<List<HabitNote>> =
        (if (habitId == 0L) flowOf(emptyList()) else repo.observeNotes(habitId))
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val today: LocalDate get() = repo.today()

    fun setNote(day: LocalDate, text: String) = viewModelScope.launch {
        if (!isNew) repo.setNote(habitId, day, text)
    }

    init {
        if (habitId != 0L) viewModelScope.launch { _draft.value = repo.getHabit(habitId) }
    }

    fun edit(change: (Habit) -> Habit) {
        _draft.value = _draft.value?.let(change)
    }

    fun applyTemplate(t: HabitTemplate) = edit {
        it.copy(
            name = t.name, icon = t.icon, color = t.color, type = t.type, target = t.target, unit = t.unit,
            freqNum = t.freqNum, freqDen = t.freqDen,
        )
    }

    fun save(onDone: () -> Unit) = viewModelScope.launch {
        val h = _draft.value ?: return@launch
        if (h.name.isBlank()) return@launch
        val clean = h.copy(
            name = h.name.trim(),
            unit = h.unit.trim(),
            target = if (h.isNumeric) h.target.coerceAtLeast(0.1) else 1.0,
            freqNum = h.freqNum.coerceIn(1, h.freqDen.coerceAtLeast(1)),
        )
        val saved = if (isNew) clean.copy(id = repo.addHabit(clean)) else clean.also { repo.update(it) }
        context?.let { runCatching { HabitReminders.update(it, saved) } }
        onDone()
    }

    fun delete(onDone: () -> Unit) = viewModelScope.launch {
        _draft.value?.takeIf { !isNew }?.let { h ->
            repo.delete(h)
            context?.let { runCatching { HabitReminders.cancel(it, h.id) } }
        }
        onDone()
    }

    companion object {
        fun factory(id: Long) = viewModelFactory { initializer { HabitEditViewModel(app().habits, id, app()) } }
    }
}
