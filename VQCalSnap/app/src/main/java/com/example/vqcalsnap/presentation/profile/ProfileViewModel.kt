package com.example.vqcalsnap.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vqcalsnap.data.datastore.ProfileDataStore
import com.example.vqcalsnap.domain.model.UserProfile
import com.example.vqcalsnap.presentation.theme.ThemeGoalCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileDataStore: ProfileDataStore,
    private val themeGoalCoordinator: ThemeGoalCoordinator
) : ViewModel() {

    val profile = profileDataStore.userProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfile()
    )

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            profileDataStore.saveProfile(profile)
            themeGoalCoordinator.clearPreviewGoal()
        }
    }

    fun previewGoal(goal: String) {
        themeGoalCoordinator.setPreviewGoal(goal)
    }

    fun clearGoalPreview() {
        themeGoalCoordinator.clearPreviewGoal()
    }
}