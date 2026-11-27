/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import app.sentier.domain.AppLanguage
import app.sentier.domain.Lang
import java.util.Locale
import kotlin.system.exitProcess

/**
 * Applique la langue choisie (français ou anglais) à toute l'appli, y compris aux éléments fournis
 * par Android : calendrier du choix de date, horloge du choix d'heure, libellés d'accessibilité.
 */
object AppLocale {
    /** Langue du téléphone, lue dans les réglages du système (Locale.getDefault() suit celle de l'appli). */
    fun systemLocale(): Locale = Resources.getSystem().configuration.locales[0] ?: Locale.getDefault()

    /** Fixe la langue au lancement de l'appli. */
    fun apply(choice: AppLanguage) {
        Lang.english = Lang.resolve(choice, systemLocale())
        Locale.setDefault(Lang.locale)
    }

    fun wrap(base: Context): Context {
        val config = Configuration(base.resources.configuration)
        config.setLocale(Lang.locale)
        return base.createConfigurationContext(config)
    }

    /** Redémarre l'appli pour que tous les textes passent dans la nouvelle langue. */
    fun restart(activity: Activity) {
        val intent = activity.packageManager.getLaunchIntentForPackage(activity.packageName) ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        activity.startActivity(intent)
        activity.finishAffinity()
        exitProcess(0)
    }
}
