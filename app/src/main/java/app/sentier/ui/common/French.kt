/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.common

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Sentier est entièrement en français, quelle que soit la langue du téléphone : sans cela, les éléments
 * fournis par Android (calendrier du choix de date, horloge du choix d'heure, libellés d'accessibilité…)
 * suivraient la langue du téléphone, par exemple l'anglais.
 */
object French {
    val locale: Locale = Locale.FRANCE

    fun wrap(base: Context): Context {
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }
}
