/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.sentier.SentierApplication
import app.sentier.domain.HabitTemplate
import app.sentier.notify.Reminders
import app.sentier.ui.common.app
import kotlinx.coroutines.launch

/** Choix faits pendant l'accueil du premier lancement. */
data class OnboardingChoices(
    val habits: List<HabitTemplate> = emptyList(),
    val stepGoal: Int = 8000,
    val adaptive: Boolean = false,
    val morning: Boolean = false,
    val evening: Boolean = false,
)

class OnboardingViewModel(private val app: SentierApplication) : ViewModel() {

    /** Enregistre les choix, puis ferme l'accueil pour de bon. */
    fun finish(choices: OnboardingChoices, onDone: () -> Unit) = viewModelScope.launch {
        runCatching { apply(app, choices) }
        app.settings.setOnboarded()
        onDone()
    }

    /** L'accueil est passé : rien n'est créé, il ne reviendra pas. */
    fun skip(onDone: () -> Unit) = viewModelScope.launch {
        app.settings.setOnboarded()
        onDone()
    }

    companion object {
        val Factory = viewModelFactory { initializer { OnboardingViewModel(app()) } }

        suspend fun apply(app: SentierApplication, choices: OnboardingChoices) {
            for (t in choices.habits) app.habits.addFromTemplate(t)
            app.settings.setGoal(choices.stepGoal)
            app.settings.setAdaptive(choices.adaptive)
            if (choices.morning || choices.evening) {
                val reminders = app.settings.currentReminders().copy(morningOn = choices.morning, eveningOn = choices.evening)
                app.settings.setReminders(reminders)
                Reminders.apply(app, reminders)
            }
        }

        /**
         * Faut-il montrer l'accueil ? Seulement au tout premier lancement : quelqu'un qui a déjà
         * des tâches, des habitudes ou de l'XP (mise à jour depuis une version sans accueil) ne le voit pas.
         */
        suspend fun needed(app: SentierApplication): Boolean {
            if (app.settings.onboarded() != null) return false
            val db = app.database
            val used = db.taskDao().countTasks() > 0 || db.habitDao().countHabits() > 0 || db.gameDao().countAllEvents() > 0
            if (used) app.settings.setOnboarded()
            return !used
        }
    }
}
