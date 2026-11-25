/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.ui.common

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val FR = Locale.FRENCH
private val SHORT = DateTimeFormatter.ofPattern("EEE d MMM", FR)
private val SHORT_YEAR = DateTimeFormatter.ofPattern("d MMM yyyy", FR)
private val LONG = DateTimeFormatter.ofPattern("EEEE d MMMM", FR)

/** « Aujourd'hui », « Demain », « Hier », « lun. 3 déc. »… */
fun friendlyDate(date: LocalDate, today: LocalDate = LocalDate.now()): String = when {
    date == today -> "Aujourd'hui"
    date == today.plusDays(1) -> "Demain"
    date == today.minusDays(1) -> "Hier"
    date.year != today.year -> date.format(SHORT_YEAR)
    else -> date.format(SHORT)
}

/** « Mercredi 25 novembre ». */
fun longDate(date: LocalDate): String = date.format(LONG).replaceFirstChar { it.titlecase(FR) }

fun monthName(date: LocalDate): String = date.month.getDisplayName(TextStyle.FULL, FR)
