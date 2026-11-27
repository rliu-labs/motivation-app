/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.common

import app.sentier.domain.Lang
import app.sentier.domain.tr
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// La langue est fixée au lancement de l'appli (elle redémarre quand on la change).
private val FR: Locale get() = Lang.locale
private val SHORT get() = DateTimeFormatter.ofPattern("EEE d MMM", FR)
private val SHORT_YEAR get() = DateTimeFormatter.ofPattern("d MMM yyyy", FR)
private val LONG get() = DateTimeFormatter.ofPattern("EEEE d MMMM", FR)

/** « Aujourd'hui », « Demain », « Hier », « lun. 3 déc. »… */
fun friendlyDate(date: LocalDate, today: LocalDate = LocalDate.now()): String = when {
    date == today -> tr("Aujourd'hui", "Today")
    date == today.plusDays(1) -> tr("Demain", "Tomorrow")
    date == today.minusDays(1) -> tr("Hier", "Yesterday")
    date.year != today.year -> date.format(SHORT_YEAR)
    else -> date.format(SHORT)
}

/** « Mercredi 25 novembre ». */
fun longDate(date: LocalDate): String = date.format(LONG).replaceFirstChar { it.titlecase(FR) }

fun monthName(date: LocalDate): String = date.month.getDisplayName(TextStyle.FULL, FR)
