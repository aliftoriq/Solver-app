package com.example.solvr.viewmodel

import androidx.lifecycle.*
import com.example.solvr.local.entity.PlafonEntity
import com.example.solvr.models.PlafonDTO
import com.example.solvr.repository.PlafonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class PlafondViewModel @Inject constructor(
    private val plafonRepository: PlafonRepository
) : ViewModel() {

    private val _plafonList = MutableLiveData<List<PlafonDTO.DataItem>>()
    val plafonList: LiveData<List<PlafonDTO.DataItem>> get() = _plafonList

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun fetchFromLocal() {
        viewModelScope.launch {
            try {
                val cached = plafonRepository.getLocalPlafon()
                if (cached.isNotEmpty()) {
                    _plafonList.postValue(cached.map {
                        PlafonDTO.DataItem(
                            id = it.id,
                            name = it.name,
                            amount = it.amount,
                            level = it.level,
                            interestRate = it.interestRate,
                            maxTenorMonths = it.maxTenorMonths
                        )
                    })
                } else {
                    fetchAllPlafon()
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Gagal load dari local DB: ${e.message}")
            }
        }
    }

    fun fetchAllPlafon() {
        ApiClient.plafonService.getAllPlafon()
            .enqueue(object : Callback<PlafonDTO.ResponseAllPlafon> {
                override fun onResponse(
                    call: Call<PlafonDTO.ResponseAllPlafon>,
                    response: Response<PlafonDTO.ResponseAllPlafon>
                ) {
                    if (response.isSuccessful && response.body()?.status == 200) {
                        val dataList = response.body()?.data ?: emptyList()
                        _plafonList.postValue(dataList as List<PlafonDTO.DataItem>?)

                        // Cache ke local Room
                        viewModelScope.launch {
                            plafonRepository.cachePlafon(dataList.map {
                                PlafonEntity(
                                    id = it.id,
                                    name = it.name,
                                    amount = it.amount,
                                    level = it.level,
                                    interestRate = it.interestRate as Double?,
                                    maxTenorMonths = it.maxTenorMonths
                                )
                            })
                        }
                    } else {
                        _errorMessage.postValue("Gagal memuat data plafon: ${response.body()?.message ?: response.message()}")
                    }
                }

                override fun onFailure(call: Call<PlafonDTO.ResponseAllPlafon>, t: Throwable) {
                    _errorMessage.postValue("Terjadi kesalahan: ${t.message}")
                }
            })
    }
}
