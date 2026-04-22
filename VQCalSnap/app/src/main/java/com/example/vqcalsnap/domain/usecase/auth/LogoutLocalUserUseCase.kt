package com.example.vqcalsnap.domain.usecase.auth

import com.example.vqcalsnap.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutLocalUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}

