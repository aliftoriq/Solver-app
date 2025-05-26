package com.example.solvr.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.solvr.local.dao.PlafonDao
import com.example.solvr.local.entity.PlafonEntity

@Database(entities = [PlafonEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun plafonDao(): PlafonDao
}
