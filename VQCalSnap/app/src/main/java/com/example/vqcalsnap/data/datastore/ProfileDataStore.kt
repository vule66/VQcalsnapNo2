package com.example.vqcalsnap.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.vqcalsnap.data.local.AuthDao
import com.example.vqcalsnap.domain.model.UserProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_profile")

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authDao: AuthDao
) {
    private data class ProfileKeys(
        val name: Preferences.Key<String>,
        val age: Preferences.Key<Int>,
        val gender: Preferences.Key<String>,
        val weight: Preferences.Key<Float>,
        val height: Preferences.Key<Float>,
        val goal: Preferences.Key<String>,
        val calorieTarget: Preferences.Key<Int>
    )

    private fun profileKeys(userId: Int): ProfileKeys {
        return ProfileKeys(
            name = stringPreferencesKey("name_$userId"),
            age = intPreferencesKey("age_$userId"),
            gender = stringPreferencesKey("gender_$userId"),
            weight = floatPreferencesKey("weight_$userId"),
            height = floatPreferencesKey("height_$userId"),
            goal = stringPreferencesKey("goal_$userId"),
            calorieTarget = intPreferencesKey("calorie_target_$userId")
        )
    }

    private fun defaultProfile(): UserProfile {
        return UserProfile(
            name = "",
            age = 22,
            gender = "Nam",
            weight = 60f,
            height = 165f,
            goal = "Duy trì cân nặng",
            dailyCalorieTarget = 2000
        )
    }

    val userProfile: Flow<UserProfile> = authDao.observeSession().flatMapLatest { session ->
        if (session == null) {
            flowOf(defaultProfile())
        } else {
            val keys = profileKeys(session.userId)
            context.dataStore.data.map { prefs ->
                UserProfile(
                    name = prefs[keys.name] ?: "",
                    age = prefs[keys.age] ?: 22,
                    gender = prefs[keys.gender] ?: "Nam",
                    weight = prefs[keys.weight] ?: 60f,
                    height = prefs[keys.height] ?: 165f,
                    goal = prefs[keys.goal] ?: "Duy trì cân nặng",
                    dailyCalorieTarget = prefs[keys.calorieTarget] ?: 2000
                )
            }
        }
    }

    suspend fun saveProfile(profile: UserProfile) {
        val session = authDao.getCurrentSession() ?: return
        val keys = profileKeys(session.userId)
        context.dataStore.edit { prefs ->
            prefs[keys.name] = profile.name
            prefs[keys.age] = profile.age
            prefs[keys.gender] = profile.gender
            prefs[keys.weight] = profile.weight
            prefs[keys.height] = profile.height
            prefs[keys.goal] = profile.goal
            prefs[keys.calorieTarget] = profile.dailyCalorieTarget
        }
    }
}