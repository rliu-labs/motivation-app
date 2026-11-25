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
import app.sentier.data.HabitRepository
import app.sentier.data.HabitWithStats
import app.sentier.data.StepSource
import app.sentier.domain.HabitTemplate
import app.sentier.steps.StepSampleWorker
import app.sentier.steps.StepsRepository
import app.sentier.steps.StepsState
import app.sentier.ui.common.app
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HabitsState(
    val today: LocalDate,
    val habits: List<HabitWithStats> = emptyList(),
    val steps: StepsState,
    val loading: Boolean = true,
) {
    /** Les 5 derniers jours, du plus ancien à aujourd'hui. */
    val days: List<LocalDate> get() = (4 downTo 0).map { today.minusDays(it.toLong()) }
}

class HabitsViewModel(
    private val habits: HabitRepository,
    private val steps: StepsRepository,
) : ViewModel() {

    private val today = habits.today()

    val state: StateFlow<HabitsState> = combine(habits.observeHabitsWithStats(), steps.observe()) { h, s ->
        HabitsState(today, h, s, loading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitsState(today, steps = StepsState(today)))

    fun toggle(habit: Habit, day: LocalDate = today) = viewModelScope.launch { habits.toggle(habit, day) }

    fun setValue(habit: Habit, day: LocalDate, value: Double) = viewModelScope.launch { habits.setValue(habit, day, value) }

    fun addTemplate(t: HabitTemplate) = viewModelScope.launch { habits.addFromTemplate(t) }

    fun refreshSteps() = viewModelScope.launch { steps.refresh() }

    fun useHealthConnect(context: Context) = viewModelScope.launch {
        StepSampleWorker.cancel(context.applicationContext)
        steps.useSource(StepSource.HEALTH_CONNECT)
    }

    fun useSensor(context: Context) = viewModelScope.launch {
        steps.useSource(StepSource.SENSOR)
        StepSampleWorker.schedule(context.applicationContext)
    }

    fun setGoal(goal: Int, stepLengthCm: Int) = viewModelScope.launch {
        steps.setGoal(goal)
        steps.setStepLength(stepLengthCm)
    }

    companion object {
        val Factory = viewModelFactory { initializer { HabitsViewModel(app().habits, app().steps) } }
    }
}
