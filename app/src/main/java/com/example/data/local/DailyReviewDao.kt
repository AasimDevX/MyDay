package com.example.data.local

import androidx.room.*
import com.example.data.model.DailyReview
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyReviewDao {
    @Query("SELECT * FROM daily_reviews ORDER BY dateString DESC")
    fun getAllReviewsFlow(): Flow<List<DailyReview>>

    @Query("SELECT * FROM daily_reviews WHERE dateString = :date LIMIT 1")
    fun getReviewForDateFlow(date: String): Flow<DailyReview?>

    @Query("SELECT * FROM daily_reviews WHERE dateString = :date LIMIT 1")
    suspend fun getReviewForDate(date: String): DailyReview?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateReview(review: DailyReview): Long

    @Delete
    suspend fun deleteReview(review: DailyReview)
}
