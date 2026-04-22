package com.example.vqcalsnap.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vqcalsnap.data.datastore.ProfileDataStore
import com.example.vqcalsnap.domain.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ThemeViewModel @Inject constructor(
    profileDataStore: ProfileDataStore,
    themeGoalCoordinator: ThemeGoalCoordinator
) : ViewModel() {

    private val persistedGoal = profileDataStore.userProfile
        .map { it.goal }

    val activeGoal: StateFlow<String> = combine(
        persistedGoal,
        themeGoalCoordinator.previewGoal
    ) { storedGoal, previewGoal ->
        previewGoal ?: storedGoal
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile().goal
        )
}



