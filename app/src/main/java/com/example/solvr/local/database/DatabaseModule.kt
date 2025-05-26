package com.example.solvr.di

import android.content.Context
import androidx.room.Room
import com.example.solvr.local.AppDatabase
import com.example.solvr.local.dao.PlafonDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "solvr-db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun providePlafonDao(db: AppDatabase): PlafonDao = db.plafonDao()
}
