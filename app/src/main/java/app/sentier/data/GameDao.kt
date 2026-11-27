/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Insert
    suspend fun insertEvent(event: GameEvent): Long

    @Query("SELECT COUNT(*) FROM game_event")
    suspend fun countAllEvents(): Int

    @Query("SELECT COUNT(*) FROM game_event WHERE kind = :kind AND ref_id = :refId AND day = :day")
    suspend fun countEvents(kind: Int, refId: Long, day: Long): Int

    @Query("SELECT COUNT(*) FROM game_event WHERE kind = :kind AND ref_id = :refId")
    suspend fun countEventsEver(kind: Int, refId: Long): Int

    @Query("DELETE FROM game_event WHERE kind = :kind AND ref_id = :refId AND day = :day")
    suspend fun deleteEvents(kind: Int, refId: Long, day: Long)

    /** Retire le dernier gain d'une tâche (annulation d'une validation). */
    @Query(
        "DELETE FROM game_event WHERE id = " +
            "(SELECT id FROM game_event WHERE kind = :kind AND ref_id = :refId ORDER BY id DESC LIMIT 1)"
    )
    suspend fun deleteLastEvent(kind: Int, refId: Long)

    @Query("SELECT COALESCE(SUM(xp), 0) AS xp, COALESCE(SUM(gold), 0) AS gold FROM game_event")
    fun observeTotals(): Flow<GameTotals>

    @Query("SELECT COALESCE(SUM(gold), 0) FROM game_event")
    suspend fun gold(): Int

    /** Jours avec au moins une action (les achats, les gels et les bonus de défi ne comptent pas). */
    @Query("SELECT DISTINCT day FROM game_event WHERE kind NOT IN (4, 6, 7, 8) ORDER BY day")
    fun observeActiveDays(): Flow<List<Long>>

    /** Jours d'achat des gels de série (un par gel). */
    @Query("SELECT day FROM game_event WHERE kind = 7 ORDER BY day")
    fun observeFreezeDays(): Flow<List<Long>>

    @Query("SELECT * FROM game_event WHERE day >= :fromDay ORDER BY day")
    fun observeEventsSince(fromDay: Long): Flow<List<GameEvent>>

    /** Clés gagnées d'un type (escales, défis saisonniers…), avec le premier jour où chacune l'a été. */
    @Query("SELECT ref_id, MIN(day) AS day FROM game_event WHERE kind = :kind GROUP BY ref_id ORDER BY day, ref_id")
    fun observeRefDays(kind: Int): Flow<List<RefDay>>

    @Query("SELECT kind, COUNT(*) AS count FROM game_event GROUP BY kind")
    fun observeCounts(): Flow<List<KindCount>>

    @Query("SELECT * FROM reward ORDER BY cost, name")
    fun observeRewards(): Flow<List<Reward>>

    @Insert
    suspend fun insertReward(reward: Reward): Long

    @Update
    suspend fun updateReward(reward: Reward)

    @Delete
    suspend fun deleteReward(reward: Reward)
}
