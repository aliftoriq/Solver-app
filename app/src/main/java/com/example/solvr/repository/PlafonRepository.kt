package com.example.solvr.repository

import com.example.solvr.local.dao.PlafonDao
import com.example.solvr.local.entity.PlafonEntity
import com.example.solvr.models.PlafonDTO
import com.example.solvr.network.PlafonService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlafonRepository @Inject constructor(
    private val plafonDao: PlafonDao
) {
    suspend fun getLocalPlafon(): List<PlafonEntity> = withContext(Dispatchers.IO) {
        plafonDao.getAllPlafon()
    }

    suspend fun cachePlafon(data: List<PlafonEntity>) = withContext(Dispatchers.IO) {
        plafonDao.clearAll()
        plafonDao.insertAll(data)
    }

    suspend fun cachePlafonDefault(data: PlafonEntity) = withContext(Dispatchers.IO) {
        plafonDao.clearAll()
        plafonDao.insert(data)
    }

}




//class PlafonRepository @Inject constructor(
//    private val plafonService: PlafonService
//) {
//    fun getAllPlafon(): Call<PlafonDTO.ResponseAllPlafon> = plafonService.getAllPlafon()
//}
