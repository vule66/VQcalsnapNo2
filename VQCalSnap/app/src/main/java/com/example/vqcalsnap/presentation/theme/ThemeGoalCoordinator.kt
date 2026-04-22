package com.example.vqcalsnap.presentation.theme

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class ThemeGoalCoordinator @Inject constructor() {
    private val _previewGoal = MutableStateFlow<String?>(null)
    val previewGoal: StateFlow<String?> = _previewGoal.asStateFlow()

    fun setPreviewGoal(goal: String) {
        _previewGoal.value = goal
    }

    fun clearPreviewGoal() {
        _previewGoal.value = null
    }
}

