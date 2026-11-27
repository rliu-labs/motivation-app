/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.sentier.data.Settings
import app.sentier.data.Stats
import app.sentier.data.StatsPeriod
import app.sentier.data.StatsRepository
import app.sentier.ui.common.app
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(repo: StatsRepository, settings: Settings) : ViewModel() {
    private val _period = MutableStateFlow(StatsPeriod.WEEK)
    val period: StateFlow<StatsPeriod> = _period

    val stats: StateFlow<Stats> = _period.flatMapLatest { repo.observe(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Stats())

    val goal: StateFlow<Int> = settings.steps.map { it.goal }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 8000)

    fun select(p: StatsPeriod) {
        _period.value = p
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = app()
                StatsViewModel(StatsRepository(app.database.gameDao(), app.database.habitDao(), app.habits), app.settings)
            }
        }
    }
}
