/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Journal du jeu : chaque gain (tâche, habitude, pas) ou dépense (récompense) est une ligne.
 * L'XP et l'or sont la somme du journal, ce qui permet d'annuler une action simplement.
 */
@Entity(tableName = "game_event", indices = [Index("kind", "ref_id", "day"), Index("day")])
data class GameEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: Int,
    /** Tâche, habitude ou récompense concernée (0 pour les pas). */
    @ColumnInfo(name = "ref_id") val refId: Long,
    val day: Long,
    val xp: Int,
    val gold: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
) {
    companion object {
        const val TASK = 1
        const val HABIT = 2
        const val STEPS = 3
        const val PURCHASE = 4
    }
}

/** Récompense de la boutique, choisie par l'utilisateur. */
@Entity(tableName = "reward")
data class Reward(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Prix en or. */
    val cost: Int,
    val icon: String = "gift",
    val position: Int = 0,
)

data class GameTotals(val xp: Int, val gold: Int)

data class KindCount(val kind: Int, val count: Int)
