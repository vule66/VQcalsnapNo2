package com.example.vqcalsnap.data.repository

import com.example.vqcalsnap.data.local.AuthDao
import com.example.vqcalsnap.data.local.AuthSessionEntity
import com.example.vqcalsnap.data.local.AuthUserEntity
import com.example.vqcalsnap.domain.model.AuthSession
import com.example.vqcalsnap.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.text.Normalizer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalAuthRepository @Inject constructor(
    private val authDao: AuthDao
) : AuthRepository {

    override fun observeActiveSession(): Flow<AuthSession?> {
        return authDao.observeSession().map { entity ->
            entity?.let { AuthSession(userId = it.userId, username = it.username) }
        }
    }

    override suspend fun login(username: String, password: String): Result<Unit> {
        val normalizedUsername = username.normalizeUsername()
        if (normalizedUsername.isBlank()) {
            return Result.failure(IllegalArgumentException("Tên đăng nhập không được để trống"))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Mật khẩu không được để trống"))
        }

        val user = authDao.getUserByUsername(normalizedUsername)
            ?: return Result.failure(IllegalArgumentException("Tài khoản không tồn tại"))

        val inputHash = password.sha256()
        if (user.passwordHash != inputHash) {
            return Result.failure(IllegalArgumentException("Sai mật khẩu"))
        }

        authDao.upsertSession(
            AuthSessionEntity(
                userId = user.id,
                username = user.username
            )
        )
        return Result.success(Unit)
    }

    override suspend fun register(username: String, password: String): Result<Unit> {
        val normalizedUsername = username.normalizeUsername()
        if (normalizedUsername.length < 3) {
            return Result.failure(IllegalArgumentException("Tên đăng nhập phải từ 3 ký tự"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Mật khẩu phải từ 6 ký tự"))
        }

        val existingUser = authDao.getUserByUsername(normalizedUsername)
        if (existingUser != null) {
            return Result.failure(IllegalArgumentException("Tên đăng nhập đã tồn tại"))
        }

        val userId = authDao.insertUser(
            AuthUserEntity(
                username = normalizedUsername,
                passwordHash = password.sha256()
            )
        ).toInt()

        authDao.upsertSession(
            AuthSessionEntity(
                userId = userId,
                username = normalizedUsername
            )
        )
        return Result.success(Unit)
    }

    override suspend fun logout() {
        authDao.clearSession()
    }

    private fun String.sha256(): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(toByteArray())
        return bytes.joinToString(separator = "") { "%02x".format(it) }
    }

    private fun String.normalizeUsername(): String {
        return Normalizer.normalize(trim(), Normalizer.Form.NFC)
    }
}



