package com.example.data.repository

import com.example.data.database.UserDao
import com.example.data.database.WargaDao
import com.example.data.model.UserAccount
import com.example.data.model.Warga
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AppRepository(
    private val userDao: UserDao,
    private val wargaDao: WargaDao
) {
    val allWargaFlow: Flow<List<Warga>> = wargaDao.getAllWargaFlow()
    val allUsersFlow: Flow<List<UserAccount>> = userDao.getAllUsersFlow()

    fun getWargaByInputterFlow(username: String): Flow<List<Warga>> {
        return wargaDao.getWargaByInputterFlow(username)
    }

    suspend fun getWargaByInputter(username: String): List<Warga> = withContext(Dispatchers.IO) {
        wargaDao.getWargaByInputter(username)
    }

    suspend fun getUserByUsername(username: String): UserAccount? = withContext(Dispatchers.IO) {
        userDao.getUserByUsername(username)
    }

    suspend fun registerUser(user: UserAccount) = withContext(Dispatchers.IO) {
        userDao.registerUser(user)
    }

    suspend fun updateUser(user: UserAccount) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
    }

    suspend fun resetUserPassword(username: String, newPasswordHash: String) = withContext(Dispatchers.IO) {
        userDao.resetPassword(username, newPasswordHash)
    }

    suspend fun deleteUser(username: String) = withContext(Dispatchers.IO) {
        userDao.deleteUser(username)
    }

    suspend fun getWargaByNik(nik: String): Warga? = withContext(Dispatchers.IO) {
        wargaDao.getWargaByNik(nik)
    }

    suspend fun insertWarga(warga: Warga) = withContext(Dispatchers.IO) {
        wargaDao.insertWarga(warga)
    }

    suspend fun updateWarga(warga: Warga) = withContext(Dispatchers.IO) {
        wargaDao.updateWarga(warga)
    }

    suspend fun deleteWarga(warga: Warga) = withContext(Dispatchers.IO) {
        wargaDao.deleteWarga(warga)
    }

    suspend fun getAllWarga(): List<Warga> = withContext(Dispatchers.IO) {
        wargaDao.getAllWarga()
    }

    suspend fun clearAllWarga() = withContext(Dispatchers.IO) {
        wargaDao.clearAllWarga()
    }

    suspend fun getAllUsers(): List<UserAccount> = withContext(Dispatchers.IO) {
        userDao.getAllUsers()
    }

    suspend fun ensureAdminExists() = withContext(Dispatchers.IO) {
        if (userDao.getUserByUsername("admin") == null) {
            userDao.registerUser(
                UserAccount(
                    username = "admin",
                    passwordHash = "admin", // Default password is admin
                    fullName = "Administrator Kelurahan",
                    status = "APPROVED",
                    isAdmin = true
                )
            )
        }
    }
}
