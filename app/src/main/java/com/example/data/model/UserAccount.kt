package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey val username: String,
    val passwordHash: String,
    val fullName: String,
    val status: String, // "PENDING", "APPROVED", "REJECTED"
    val isAdmin: Boolean = false,
    val rt: String = "",
    val rw: String = ""
)
