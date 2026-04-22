package com.example.vqcalsnap.data.repository

import com.example.vqcalsnap.data.local.AuthDao
import com.example.vqcalsnap.data.local.MealDao
import com.example.vqcalsnap.data.local.MealEntity
import com.example.vqcalsnap.util.CalorieCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class MealRepository @Inject constructor(
    private val mealDao: MealDao,
    private val authDao: AuthDao
) {
    suspend fun insertMeal(meal: MealEntity) {
        val session = authDao.getCurrentSession() ?: return
        mealDao.insertMeal(meal.copy(userId = session.userId))
    }

    fun getMealsToday(): Flow<List<MealEntity>> {
        val startOfDay = CalorieCalculator.getStartOfDay()
        return authDao.observeSession().flatMapLatest { session ->
            if (session == null) {
                flowOf(emptyList())
            } else {
                mealDao.getMealsToday(session.userId, startOfDay)
            }
        }
    }

    fun getTotalCaloriesToday(): Flow<Int?> {
        val startOfDay = CalorieCalculator.getStartOfDay()
        return authDao.observeSession().flatMapLatest { session ->
            if (session == null) {
                flowOf(0)
            } else {
                mealDao.getTotalCaloriesToday(session.userId, startOfDay)
            }
        }
    }

    suspend fun deleteMeal(id: Int) {
        val session = authDao.getCurrentSession() ?: return
        mealDao.deleteMeal(id, session.userId)
    }
}