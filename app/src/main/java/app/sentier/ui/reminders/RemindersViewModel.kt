/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.reminders

import app.sentier.domain.tr
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.sentier.SentierApplication
import app.sentier.data.ReminderSettings
import app.sentier.notify.Notifier
import app.sentier.notify.ReminderKind
import app.sentier.notify.Reminders
import app.sentier.ui.common.app
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RemindersViewModel(private val app: SentierApplication) : ViewModel() {

    val settings: StateFlow<ReminderSettings> = app.settings.reminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReminderSettings())

    private val _preview = MutableStateFlow<String?>(null)
    /** Petit message sous le bouton d'essai. */
    val preview: StateFlow<String?> = _preview

    fun update(new: ReminderSettings) = viewModelScope.launch {
        app.settings.setReminders(new)
        Reminders.apply(app, app.settings.currentReminders())
    }

    fun sendPreview() = viewModelScope.launch {
        _preview.value = when {
            !Notifier.canPost(app) -> tr("Autorise d'abord les notifications.", "Allow notifications first.")
            Reminders.show(app, ReminderKind.EVENING) -> tr("C'est envoyé : regarde tes notifications.", "Sent: check your notifications.")
            else -> tr("Rien à rappeler : tout est fait pour aujourd'hui.", "Nothing to remind you of: everything is done for today.")
        }
    }

    fun showHint(text: String) {
        _preview.value = text
    }

    companion object {
        val Factory = viewModelFactory { initializer { RemindersViewModel(app()) } }
    }
}
