package com.example.vqcalsnap.domain.repository

import com.example.vqcalsnap.domain.model.AuthSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeActiveSession(): Flow<AuthSession?>
    suspend fun login(username: String, password: String): Result<Unit>
    suspend fun register(username: String, password: String): Result<Unit>
    suspend fun logout()
}

