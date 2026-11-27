/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.domain

import java.util.Locale

/** Langue choisie dans les réglages. */
enum class AppLanguage { SYSTEM, FR, EN }

/**
 * Langue de l'appli : le français, ou l'anglais. Elle est fixée au lancement (voir SentierApplication)
 * et l'appli redémarre quand on la change, si bien que chaque texte peut être choisi une fois pour toutes.
 */
object Lang {
    @Volatile
    var english: Boolean = false

    /** Locale des dates et des nombres. */
    val locale: Locale get() = if (english) Locale.UK else Locale.FRANCE

    /** Anglais si le réglage le demande, ou s'il suit un téléphone qui n'est pas en français. */
    fun resolve(choice: AppLanguage, system: Locale = Locale.getDefault()): Boolean = when (choice) {
        AppLanguage.FR -> false
        AppLanguage.EN -> true
        AppLanguage.SYSTEM -> system.language != "fr"
    }
}

/** Le texte dans la langue de l'appli : [fr] en français, [en] en anglais. */
fun tr(fr: String, en: String): String = if (Lang.english) en else fr
