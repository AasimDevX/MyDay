package com.example.data.local

import androidx.room.*
import com.example.data.model.TaskItem
import com.example.data.model.TaskStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY scheduledDate DESC, startTime ASC")
    fun getAllTasksFlow(): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE scheduledDate = :date OR isRecurring = 1 ORDER BY startTime ASC")
    fun getTasksForDateFlow(date: String): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE scheduledDate BETWEEN :startDate AND :endDate OR isRecurring = 1 ORDER BY scheduledDate ASC, startTime ASC")
    fun getTasksBetweenDatesFlow(startDate: String, endDate: String): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TaskItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskItem>): List<Long>

    @Update
    suspend fun updateTask(task: TaskItem)

    @Delete
    suspend fun deleteTask(task: TaskItem)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun countAllTasks(): Int

    @Query("UPDATE tasks SET status = :status, completionPercentage = :completionPercentage, actualDurationMinutes = :actualDuration, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Long, status: TaskStatus, completionPercentage: Int, actualDuration: Int, updatedAt: Long)
}
