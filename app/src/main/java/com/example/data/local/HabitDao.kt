package com.example.data.local

import androidx.room.*
import com.example.data.model.HabitCompletion
import com.example.data.model.HabitItem
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY createdAt ASC")
    fun getAllHabitsFlow(): Flow<List<HabitItem>>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getHabitById(id: Long): HabitItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitItem>): List<Long>

    @Update
    suspend fun updateHabit(habit: HabitItem)

    @Delete
    suspend fun deleteHabit(habit: HabitItem)

    @Query("SELECT * FROM habit_completions ORDER BY dateString DESC")
    fun getAllCompletionsFlow(): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE dateString = :date")
    fun getCompletionsForDateFlow(date: String): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY dateString DESC")
    suspend fun getCompletionsForHabit(habitId: Long): List<HabitCompletion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletion): Long

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND dateString = :dateString")
    suspend fun deleteCompletion(habitId: Long, dateString: String)

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun countAllHabits(): Int
}
