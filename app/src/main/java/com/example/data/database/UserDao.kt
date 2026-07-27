package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_accounts WHERE username = :username")
    suspend fun getUserByUsername(username: String): UserAccount?

    @Query("SELECT * FROM user_accounts ORDER BY username ASC")
    fun getAllUsersFlow(): Flow<List<UserAccount>>

    @Query("SELECT * FROM user_accounts ORDER BY username ASC")
    suspend fun getAllUsers(): List<UserAccount>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registerUser(user: UserAccount)

    @Update
    suspend fun updateUser(user: UserAccount)

    @Query("UPDATE user_accounts SET passwordHash = :newPassword WHERE username = :username")
    suspend fun resetPassword(username: String, newPassword: String)

    @Query("DELETE FROM user_accounts WHERE username = :username")
    suspend fun deleteUser(username: String)
}
