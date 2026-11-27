/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.journey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.sentier.data.JourneyRepository
import app.sentier.data.GameRepository
import app.sentier.data.JourneyState
import app.sentier.domain.Treasure
import app.sentier.domain.Treasures
import app.sentier.domain.GameStats
import app.sentier.steps.StepsRepository
import app.sentier.ui.common.app
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JourneyViewModel(
    private val journey: JourneyRepository,
    private val steps: StepsRepository,
    game: GameRepository,
) : ViewModel() {

    val state: StateFlow<JourneyState> = journey.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), JourneyState())

    /** Les objets rares du carnet. */
    val treasures: StateFlow<List<Treasure>> = game.observe().map { it.treasures }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Treasures.of(GameStats(), 0))

    /** Relit les pas (ce qui récompense aussi les nouvelles étapes). */
    fun refresh() = viewModelScope.launch {
        runCatching { steps.refresh() }
        journey.sync()
    }

    /** Les nouvelles étapes ont été annoncées. */
    fun dismissNews() = viewModelScope.launch { journey.markSeen() }

    /** Part pour le voyage suivant choisi. */
    fun start(id: String) = viewModelScope.launch { journey.start(id) }

    companion object {
        val Factory = viewModelFactory { initializer { JourneyViewModel(app().journey, app().steps, app().game) } }
    }
}
