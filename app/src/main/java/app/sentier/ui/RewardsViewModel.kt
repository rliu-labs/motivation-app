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
import app.sentier.ui.celebrate.Celebration
import app.sentier.ui.progress.GameIcons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import kotlinx.coroutines.flow.first
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
    private val gameRepo: GameRepository,
    private val challenges: ChallengeRepository,
    private val settings: Settings,
) : ViewModel() {

    private val _challengeDone = MutableSharedFlow<Challenge>(extraBufferCapacity = 8)
    /** Défis qui viennent d'être relevés. */
    val challengeDone: SharedFlow<Challenge> = _challengeDone

    /** Trésors gagnés pas encore annoncés (le plus précieux en dernier). */
    val newTreasures: StateFlow<List<Treasure>> =
        combine(gameRepo.observe(), settings.treasuresSeen) { g, seen -> g.treasures.filter { it.owned && it.id !in seen } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Niveau ou badge à fêter (null s'il n'y a rien de nouveau). */
    val celebration: StateFlow<Celebration?> =
        combine(gameRepo.observe(), settings.celebrated) { g, done ->
            val unlocked = g.badges.filter { it.unlocked }
            if (done == null) {
                // Premier lancement avec cette version : rien de ce qui est déjà acquis n'est refêté.
                settings.setCelebrated(g.level.level, unlocked.map { it.id }.toSet())
                return@combine null
            }
            val (level, badges) = done
            val badge = unlocked.firstOrNull { it.id !in badges }
            when {
                g.level.level > level -> Celebration(
                    key = "level-${g.level.level}",
                    title = "Niveau ${g.level.level} !",
                    text = "Bravo, tu passes au niveau ${g.level.level}. Continue comme ça : chaque action compte.",
                    icon = Icons.Filled.Star,
                )
                badge != null -> Celebration(
                    key = "badge-${badge.id}",
                    title = "Badge obtenu : ${badge.title}",
                    text = badge.description + ".",
                    icon = GameIcons.of(badge.icon),
                )
                else -> null
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** La fête a été vue. S'il y a encore autre chose à fêter (un badge en plus du niveau), il suit. */
    fun dismissCelebration() = viewModelScope.launch {
        val shown = celebration.value ?: return@launch
        val g = gameRepo.observe().first()
        val (level, badges) = settings.celebrated.first() ?: (g.level.level to emptySet())
        if (shown.key.startsWith("level-")) {
            settings.setCelebrated(g.level.level, badges)
        } else {
            settings.setCelebrated(level, badges + shown.key.removePrefix("badge-"))
        }
    }

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
