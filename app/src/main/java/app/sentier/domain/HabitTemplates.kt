/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.domain

import app.sentier.data.Habit

/** Modèles d'habitudes prêts à l'emploi. */
data class HabitTemplate(
    val name: String,
    val icon: String,
    val color: Long,
    val type: Int = Habit.TYPE_BOOLEAN,
    val target: Double = 1.0,
    val unit: String = "",
    val freqNum: Int = 1,
    val freqDen: Int = 1,
)

val HabitTemplates = listOf(
    HabitTemplate("Boire de l'eau", "water", 0xFF42A5F5, Habit.TYPE_NUMERIC, 8.0, "verres"),
    HabitTemplate("Dormir 7 heures", "sleep", 0xFF5C6BC0, Habit.TYPE_NUMERIC, 7.0, "heures"),
    HabitTemplate("Méditer", "meditation", 0xFF26A69A),
    HabitTemplate("Faire du sport", "sport", 0xFFEF5350, freqNum = 3, freqDen = 7),
    HabitTemplate("Lire", "book", 0xFF8D6E63, Habit.TYPE_NUMERIC, 10.0, "pages"),
    HabitTemplate("Pas d'écran au coucher", "phone_off", 0xFFAB47BC),
)
