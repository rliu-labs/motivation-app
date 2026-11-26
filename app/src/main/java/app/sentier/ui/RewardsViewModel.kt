/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.sentier.data.ChallengeRepository
import app.sentier.data.GameRepository
import app.sentier.data.Settings
import app.sentier.domain.Challenge
import app.sentier.domain.Treasure
import app.sentier.ui.common.app
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Surveille, tant que l'appli est ouverte, les défis relevés (pour verser leur bonus aussitôt)
 * et les trésors gagnés (pour les annoncer, où que l'on soit dans l'appli).
 */
class RewardsViewModel(
    game: GameRepository,
    private val challenges: ChallengeRepository,
    private val settings: Settings,
) : ViewModel() {

    private val _challengeDone = MutableSharedFlow<Challenge>(extraBufferCapacity = 8)
    /** Défis qui viennent d'être relevés. */
    val challengeDone: SharedFlow<Challenge> = _challengeDone

    /** Trésors gagnés pas encore annoncés (le plus précieux en dernier). */
    val newTreasures: StateFlow<List<Treasure>> =
        combine(game.observe(), settings.treasuresSeen) { g, seen -> g.treasures.filter { it.owned && it.id !in seen } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            challenges.observe().collect { week ->
                if (week.list.any { it.done && it.index !in week.rewarded }) {
                    runCatching { challenges.sync() }.getOrDefault(emptyList()).forEach { _challengeDone.tryEmit(it) }
                }
            }
        }
    }

    fun markSeen(treasures: List<Treasure>) = viewModelScope.launch { settings.addTreasuresSeen(treasures.map { it.id }) }

    companion object {
        val Factory = viewModelFactory { initializer { RewardsViewModel(app().game, app().challenges, app().settings) } }
    }
}
