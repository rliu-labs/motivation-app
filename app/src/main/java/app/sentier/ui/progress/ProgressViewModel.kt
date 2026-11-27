/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.sentier.data.ChallengeRepository
import app.sentier.data.GameRepository
import app.sentier.data.GameState
import app.sentier.data.SeasonRepository
import app.sentier.data.Reward
import app.sentier.data.Settings
import app.sentier.data.ThemeMode
import app.sentier.domain.AppLanguage
import app.sentier.domain.Lang
import app.sentier.domain.SeasonsState
import app.sentier.ui.common.AppLocale
import app.sentier.domain.WeekChallenges
import app.sentier.ui.common.app
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProgressViewModel(
    private val game: GameRepository,
    challenges: ChallengeRepository,
    private val settings: Settings,
    seasons: SeasonRepository? = null,
) : ViewModel() {

    /** Le défi saisonnier en cours, ou le prochain. */
    val seasons: StateFlow<SeasonsState?> = (seasons?.observe() ?: kotlinx.coroutines.flow.flowOf(null))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val state: StateFlow<GameState> = game.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GameState())

    val challenges: StateFlow<WeekChallenges?> = challenges.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val theme: StateFlow<ThemeMode> = settings.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { settings.setThemeMode(mode) }

    val language: StateFlow<AppLanguage> = settings.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppLanguage.FR)

    /** Enregistre la langue puis [then] (redémarrage de l'appli) si elle change vraiment. */
    fun setLanguage(language: AppLanguage, then: () -> Unit) = viewModelScope.launch {
        settings.setLanguage(language)
        if (Lang.resolve(language, AppLocale.systemLocale()) != Lang.english) then()
    }

    fun buy(reward: Reward) = viewModelScope.launch { game.buy(reward) }

    fun buyFreeze() = viewModelScope.launch { game.buyFreeze() }

    fun save(reward: Reward?, name: String, cost: Int) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        if (reward == null) game.addReward(name, cost) else game.updateReward(reward.copy(name = name, cost = cost))
    }

    fun delete(reward: Reward) = viewModelScope.launch { game.deleteReward(reward) }

    companion object {
        val Factory = viewModelFactory { initializer { ProgressViewModel(app().game, app().challenges, app().settings, app().seasons) } }
    }
}
