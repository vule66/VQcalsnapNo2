package com.example.vqcalsnap.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vqcalsnap.domain.usecase.auth.LoginLocalUserUseCase
import com.example.vqcalsnap.domain.usecase.auth.LogoutLocalUserUseCase
import com.example.vqcalsnap.domain.usecase.auth.ObserveAuthSessionUseCase
import com.example.vqcalsnap.domain.usecase.auth.RegisterLocalUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    observeAuthSessionUseCase: ObserveAuthSessionUseCase,
    private val loginLocalUserUseCase: LoginLocalUserUseCase,
    private val registerLocalUserUseCase: RegisterLocalUserUseCase,
    private val logoutLocalUserUseCase: LogoutLocalUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeAuthSessionUseCase().collect { session ->
                _uiState.update {
                    it.copy(
                        isCheckingSession = false,
                        isLoggedIn = session != null,
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun onUsernameChanged(value: String) {
        _uiState.update { it.copy(username = value, errorMessage = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun toggleMode() {
        _uiState.update { it.copy(isRegisterMode = !it.isRegisterMode, errorMessage = null) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val result = if (state.isRegisterMode) {
                registerLocalUserUseCase(state.username, state.password)
            } else {
                loginLocalUserUseCase(state.username, state.password)
            }

            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    errorMessage = result.exceptionOrNull()?.message,
                    password = if (result.isSuccess) "" else it.password
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutLocalUserUseCase()
            _uiState.update { it.copy(username = "", password = "", isRegisterMode = false) }
        }
    }
}

