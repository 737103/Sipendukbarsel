package com.example.data.repository

import com.example.data.api.SupabaseClient
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
        try {
            SupabaseClient.api.upsertUsers(
                SupabaseClient.API_KEY,
                SupabaseClient.authHeader,
                users = listOf(user)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateUser(user: UserAccount) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
        try {
            SupabaseClient.api.upsertUsers(
                SupabaseClient.API_KEY,
                SupabaseClient.authHeader,
                users = listOf(user)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun resetUserPassword(username: String, newPasswordHash: String) = withContext(Dispatchers.IO) {
        userDao.resetPassword(username, newPasswordHash)
        try {
            val user = userDao.getUserByUsername(username)
            if (user != null) {
                SupabaseClient.api.upsertUsers(
                    SupabaseClient.API_KEY,
                    SupabaseClient.authHeader,
                    users = listOf(user)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteUser(username: String) = withContext(Dispatchers.IO) {
        userDao.deleteUser(username)
        try {
            SupabaseClient.api.deleteUser(
                SupabaseClient.API_KEY,
                SupabaseClient.authHeader,
                "eq.$username"
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getWargaByNik(nik: String): Warga? = withContext(Dispatchers.IO) {
        wargaDao.getWargaByNik(nik)
    }

    suspend fun insertWarga(warga: Warga) = withContext(Dispatchers.IO) {
        wargaDao.insertWarga(warga)
        try {
            SupabaseClient.api.upsertWarga(
                SupabaseClient.API_KEY,
                SupabaseClient.authHeader,
                wargaList = listOf(warga)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateWarga(warga: Warga) = withContext(Dispatchers.IO) {
        wargaDao.updateWarga(warga)
        try {
            SupabaseClient.api.upsertWarga(
                SupabaseClient.API_KEY,
                SupabaseClient.authHeader,
                wargaList = listOf(warga)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteWarga(warga: Warga) = withContext(Dispatchers.IO) {
        wargaDao.deleteWarga(warga)
        try {
            SupabaseClient.api.deleteWarga(
                SupabaseClient.API_KEY,
                SupabaseClient.authHeader,
                "eq.${warga.nik}"
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        var admin = userDao.getUserByUsername("admin")
        if (admin == null) {
            admin = UserAccount(
                username = "admin",
                passwordHash = "admin", // Default password is admin
                fullName = "Administrator Kelurahan",
                status = "APPROVED",
                isAdmin = true
            )
            userDao.registerUser(admin)
        }
        try {
            SupabaseClient.api.upsertUsers(
                SupabaseClient.API_KEY,
                SupabaseClient.authHeader,
                users = listOf(admin)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Dual synchronization between local SQLite (Room) and remote Supabase database.
     */
    suspend fun syncWithSupabase() = withContext(Dispatchers.IO) {
        // 1. Fetch remote users and save to local
        try {
            val remoteUsers = SupabaseClient.api.getAllUsers(SupabaseClient.API_KEY, SupabaseClient.authHeader)
            for (u in remoteUsers) {
                val localUser = userDao.getUserByUsername(u.username)
                if (localUser == null) {
                    userDao.registerUser(u)
                } else if (localUser != u) {
                    userDao.updateUser(u)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }

        // 2. Fetch remote warga and save to local
        try {
            val remoteWarga = SupabaseClient.api.getAllWarga(SupabaseClient.API_KEY, SupabaseClient.authHeader)
            for (w in remoteWarga) {
                val localWarga = wargaDao.getWargaByNik(w.nik)
                if (localWarga == null) {
                    wargaDao.insertWarga(w)
                } else if (localWarga != w) {
                    wargaDao.updateWarga(w)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }

        // 3. Push local users up
        try {
            val localUsers = userDao.getAllUsers()
            if (localUsers.isNotEmpty()) {
                SupabaseClient.api.upsertUsers(
                    SupabaseClient.API_KEY,
                    SupabaseClient.authHeader,
                    users = localUsers
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }

        // 4. Push local warga up
        try {
            val localWarga = wargaDao.getAllWarga()
            if (localWarga.isNotEmpty()) {
                SupabaseClient.api.upsertWarga(
                    SupabaseClient.API_KEY,
                    SupabaseClient.authHeader,
                    wargaList = localWarga
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
