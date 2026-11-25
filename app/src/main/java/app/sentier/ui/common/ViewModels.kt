/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.common

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import app.sentier.SentierApplication

/** Accès à l'application depuis une fabrique de ViewModel. */
fun CreationExtras.app(): SentierApplication = this[APPLICATION_KEY] as SentierApplication
