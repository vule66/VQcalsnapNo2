package com.example.vqcalsnap.presentation.auth

data class AuthUiState(
    val isCheckingSession: Boolean = true,
    val isLoggedIn: Boolean = false,
    val username: String = "",
    val password: String = "",
    val isRegisterMode: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

