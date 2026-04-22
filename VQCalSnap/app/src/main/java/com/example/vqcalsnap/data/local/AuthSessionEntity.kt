package com.example.vqcalsnap.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auth_session")
data class AuthSessionEntity(
    @PrimaryKey
    val id: Int = 1,
    val userId: Int,
    val username: String,
    val loggedInAt: Long = System.currentTimeMillis()
)

