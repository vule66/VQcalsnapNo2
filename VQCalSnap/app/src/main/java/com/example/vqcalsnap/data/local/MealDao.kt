package com.example.vqcalsnap.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity)

    @Query("SELECT * FROM meals WHERE userId = :userId AND timestamp >= :startOfDay ORDER BY timestamp DESC")
    fun getMealsToday(userId: Int, startOfDay: Long): Flow<List<MealEntity>>

    @Query("DELETE FROM meals WHERE id = :id AND userId = :userId")
    suspend fun deleteMeal(id: Int, userId: Int)

    @Query("SELECT SUM(calories) FROM meals WHERE userId = :userId AND timestamp >= :startOfDay")
    fun getTotalCaloriesToday(userId: Int, startOfDay: Long): Flow<Int?>
}