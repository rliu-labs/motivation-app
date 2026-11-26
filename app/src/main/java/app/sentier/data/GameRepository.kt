/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.data

import app.sentier.domain.Badge
import app.sentier.domain.Badges
import app.sentier.domain.GameRules
import app.sentier.domain.WorldTour
import app.sentier.domain.GameStats
import app.sentier.domain.LevelInfo
import app.sentier.domain.StreakInfo
import app.sentier.domain.Streaks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

/** Gain affiché après une action (« +20 XP · +4 or »). */
data class Gain(val xp: Int, val gold: Int)

data class GameState(
    val level: LevelInfo = GameRules.levelOf(0),
    val totalXp: Int = 0,
    val gold: Int = 0,
    val streak: StreakInfo = StreakInfo(),
    val badges: List<Badge> = Badges.of(GameStats()),
    val rewards: List<Reward> = emptyList(),
)

class GameRepository(
    private val dao: GameDao,
    private val clock: () -> LocalDate = { LocalDate.now() },
) {
    fun today(): LocalDate = clock()

    fun observe(): Flow<GameState> {
        val today = clock().toEpochDay()
        return combine(
            dao.observeTotals(),
            dao.observeActiveDays(),
            dao.observeCounts(),
            dao.observeRewards(),
        ) { totals, days, counts, rewards ->
            val level = GameRules.levelOf(totals.xp)
            val streak = Streaks.compute(days.toSet(), today)
            val byKind = counts.associate { it.kind to it.count }
            val stats = GameStats(
                tasksDone = byKind[GameEvent.TASK] ?: 0,
                habitsDone = byKind[GameEvent.HABIT] ?: 0,
                stepGoals = byKind[GameEvent.STEPS] ?: 0,
                rewardsTaken = byKind[GameEvent.PURCHASE] ?: 0,
                stops = byKind[GameEvent.STOP] ?: 0,
                level = level.level,
                bestStreak = streak.best,
            )
            GameState(level, totals.xp, totals.gold, streak, Badges.of(stats), rewards)
        }
    }

    /** Une tâche vient d'être validée. */
    suspend fun onTaskCompleted(task: Task): Gain {
        val xp = GameRules.xpFor(task.difficulty)
        val gain = Gain(xp, GameRules.goldFor(xp))
        dao.insertEvent(GameEvent(kind = GameEvent.TASK, refId = task.id, day = today().toEpochDay(), xp = gain.xp, gold = gain.gold))
        return gain
    }

    /** La dernière validation d'une tâche est annulée. */
    suspend fun onTaskUndone(taskId: Long) = dao.deleteLastEvent(GameEvent.TASK, taskId)

    /**
     * Une habitude est faite (ou ne l'est plus) un jour donné.
     * Une seule récompense par habitude et par jour.
     */
    suspend fun onHabit(habit: Habit, day: Long, done: Boolean): Gain? {
        if (!done) {
            dao.deleteEvents(GameEvent.HABIT, habit.id, day)
            return null
        }
        if (dao.countEvents(GameEvent.HABIT, habit.id, day) > 0) return null
        val xp = GameRules.xpFor(habit.difficulty)
        val gain = Gain(xp, GameRules.goldFor(xp))
        dao.insertEvent(GameEvent(kind = GameEvent.HABIT, refId = habit.id, day = day, xp = gain.xp, gold = gain.gold))
        return gain
    }

    /** L'objectif de pas est atteint ce jour-là (une seule fois par jour). */
    suspend fun onStepsGoal(day: Long) {
        if (dao.countEvents(GameEvent.STEPS, 0, day) > 0) return
        val xp = GameRules.STEPS_XP
        dao.insertEvent(GameEvent(kind = GameEvent.STEPS, refId = 0, day = day, xp = xp, gold = GameRules.goldFor(xp)))
    }

    /** Une étape du voyage est atteinte. Récompensée une seule fois ; renvoie null si déjà fait. */
    suspend fun onStopReached(key: Long, day: Long = today().toEpochDay()): Gain? {
        if (dao.countEventsEver(GameEvent.STOP, key) > 0) return null
        val xp = WorldTour.STOP_XP
        val gain = Gain(xp, GameRules.goldFor(xp))
        dao.insertEvent(GameEvent(kind = GameEvent.STOP, refId = key, day = day, xp = gain.xp, gold = gain.gold))
        return gain
    }

    /** Prend une récompense si l'or suffit. Renvoie false sinon. */
    suspend fun buy(reward: Reward): Boolean {
        if (dao.gold() < reward.cost) return false
        dao.insertEvent(GameEvent(kind = GameEvent.PURCHASE, refId = reward.id, day = today().toEpochDay(), xp = 0, gold = -reward.cost))
        return true
    }

    suspend fun addReward(name: String, cost: Int): Long =
        dao.insertReward(Reward(name = name.trim(), cost = cost.coerceAtLeast(1)))

    suspend fun updateReward(reward: Reward) = dao.updateReward(reward.copy(name = reward.name.trim(), cost = reward.cost.coerceAtLeast(1)))
    suspend fun deleteReward(reward: Reward) = dao.deleteReward(reward)
}
