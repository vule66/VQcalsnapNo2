package com.example.vqcalsnap.domain.usecase.auth

import com.example.vqcalsnap.domain.model.AuthSession
import com.example.vqcalsnap.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAuthSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<AuthSession?> {
        return authRepository.observeActiveSession()
    }
}

