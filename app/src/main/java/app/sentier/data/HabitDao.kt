/*
 * Sentier — appli de motivation personnelle.
 * Copyright (C) 2026 Les auteurs de Sentier
 * Licence GPL-3.0 ou ultérieure, voir le fichier LICENSE.
 */
package app.sentier.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habit WHERE archived = 0 ORDER BY position, id")
    fun observeHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habit WHERE id = :id")
    fun observeHabit(id: Long): Flow<Habit?>

    @Query("SELECT * FROM habit WHERE id = :id")
    suspend fun getHabit(id: Long): Habit?

    @Query("SELECT COALESCE(MAX(position), 0) FROM habit")
    suspend fun maxPosition(): Int

    @Insert
    suspend fun insert(habit: Habit): Long

    @Update
    suspend fun update(habit: Habit)

    @Delete
    suspend fun delete(habit: Habit)

    /** Toutes les valeurs depuis [fromDay] (pour les cases et le score). */
    @Query("SELECT * FROM habit_log WHERE day >= :fromDay ORDER BY day")
    fun observeLogsSince(fromDay: Long): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_log WHERE habit_id = :habitId ORDER BY day")
    fun observeLogs(habitId: Long): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_log WHERE habit_id = :habitId AND day = :day")
    suspend fun getLog(habitId: Long, day: Long): HabitLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putLog(log: HabitLog)

    @Query("DELETE FROM habit_log WHERE habit_id = :habitId AND day = :day")
    suspend fun deleteLog(habitId: Long, day: Long)

    // --- Pas ---

    @Upsert
    suspend fun putSteps(steps: DailySteps)

    @Query("SELECT * FROM daily_steps WHERE day >= :fromDay ORDER BY day")
    fun observeStepsSince(fromDay: Long): Flow<List<DailySteps>>

    @Query("SELECT COALESCE(SUM(steps), 0) FROM daily_steps")
    fun observeTotalSteps(): Flow<Long>
}
