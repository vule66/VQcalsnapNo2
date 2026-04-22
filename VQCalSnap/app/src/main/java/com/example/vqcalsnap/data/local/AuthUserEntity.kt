package com.example.vqcalsnap.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "auth_users",
    indices = [Index(value = ["username"], unique = true)]
)
data class AuthUserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis()
)

