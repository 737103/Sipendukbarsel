package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.data.model.Warga
import kotlinx.coroutines.flow.Flow

@Dao
interface WargaDao {
    @Query("SELECT * FROM warga_list ORDER BY timestamp DESC")
    fun getAllWargaFlow(): Flow<List<Warga>>

    @Query("SELECT * FROM warga_list ORDER BY timestamp DESC")
    suspend fun getAllWarga(): List<Warga>

    @Query("SELECT * FROM warga_list WHERE inputtedBy = :username ORDER BY timestamp DESC")
    fun getWargaByInputterFlow(username: String): Flow<List<Warga>>

    @Query("SELECT * FROM warga_list WHERE inputtedBy = :username ORDER BY timestamp DESC")
    suspend fun getWargaByInputter(username: String): List<Warga>

    @Query("SELECT * FROM warga_list WHERE nik = :nik")
    suspend fun getWargaByNik(nik: String): Warga?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWarga(warga: Warga)

    @Update
    suspend fun updateWarga(warga: Warga)

    @Delete
    suspend fun deleteWarga(warga: Warga)

    @Query("DELETE FROM warga_list WHERE inputtedBy = :username")
    suspend fun deleteWargaByInputter(username: String)

    @Query("DELETE FROM warga_list")
    suspend fun clearAllWarga()
}
