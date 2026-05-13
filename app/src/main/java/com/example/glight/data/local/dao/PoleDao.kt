package com.example.glight.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.glight.data.local.entity.PoleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PoleDao {
    @Query("SELECT * FROM poles")
    fun getAllPoles(): Flow<List<PoleEntity>>

    @Query("SELECT * FROM poles WHERE id = :id")
    suspend fun getPoleById(id: String): PoleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoles(poles: List<PoleEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPole(pole: PoleEntity)
    
    @Query("DELETE FROM poles")
    suspend fun clearAll()
}
