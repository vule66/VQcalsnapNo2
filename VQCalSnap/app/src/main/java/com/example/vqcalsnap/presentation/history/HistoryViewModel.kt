package com.example.vqcalsnap.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vqcalsnap.data.local.MealEntity
import com.example.vqcalsnap.data.repository.MealRepository
import com.example.vqcalsnap.data.datastore.ProfileDataStore
import com.example.vqcalsnap.domain.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val mealRepository: MealRepository,
    private val profileDataStore: ProfileDataStore
) : ViewModel() {

    val meals = mealRepository.getMealsToday().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalCalories = mealRepository.getTotalCaloriesToday().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val profile = profileDataStore.userProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfile()
    )

    fun deleteMeal(id: Int) {
        viewModelScope.launch {
            mealRepository.deleteMeal(id)
        }
    }
}