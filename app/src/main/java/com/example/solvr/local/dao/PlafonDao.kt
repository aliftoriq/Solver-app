package com.example.solvr.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.solvr.local.entity.PlafonEntity

@Dao
interface PlafonDao {

    @Query("SELECT * FROM plafon")
    suspend fun getAllPlafon(): List<PlafonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plafons: List<PlafonEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plafons: PlafonEntity)

    @Query("DELETE FROM plafon")
    suspend fun clearAll()
}
