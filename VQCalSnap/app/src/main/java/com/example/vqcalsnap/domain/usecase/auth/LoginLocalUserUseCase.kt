package com.example.vqcalsnap.domain.usecase.auth

import com.example.vqcalsnap.domain.repository.AuthRepository
import javax.inject.Inject

class LoginLocalUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String): Result<Unit> {
        return authRepository.login(username, password)
    }
}

