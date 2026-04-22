package com.example.vqcalsnap.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: AuthUserEntity): Long

    @Query("SELECT * FROM auth_users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): AuthUserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: AuthSessionEntity)

    @Query("SELECT * FROM auth_session WHERE id = 1 LIMIT 1")
    fun observeSession(): Flow<AuthSessionEntity?>

    @Query("SELECT * FROM auth_session WHERE id = 1 LIMIT 1")
    suspend fun getCurrentSession(): AuthSessionEntity?

    @Query("DELETE FROM auth_session WHERE id = 1")
    suspend fun clearSession()
}


