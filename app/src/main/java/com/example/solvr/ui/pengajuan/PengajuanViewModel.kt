package com.example.solvr.ui.pengajuan

import ApiClient
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.solvr.models.LoanDTO
import com.example.solvr.models.LoanSummaryDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PengajuanViewModel : ViewModel() {

    private val _loanSummary = MutableLiveData<LoanSummaryDTO.Response>()
    val loanSummary: MutableLiveData<LoanSummaryDTO.Response> = _loanSummary

    private val _loanDetail = MutableLiveData<LoanDTO.ResponseApplyLoan>()
    val loanDetail: MutableLiveData<LoanDTO.ResponseApplyLoan> = _loanDetail

    private val _calculateLoan = MutableLiveData<LoanDTO.ResponseCalculate>()
    val calculateLoan: MutableLiveData<LoanDTO.ResponseCalculate> = _calculateLoan

    private val _isExceedPlafon = MutableLiveData<String>()
    val isExceedPlafon: LiveData<String> get() = _isExceedPlafon

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun applyLoan(request: LoanDTO.Request) {
        ApiClient.userService.applyLoan(request)
            .enqueue(object : Callback<LoanDTO.ResponseApplyLoan> {
                override fun onResponse(
                    call: Call<LoanDTO.ResponseApplyLoan>,
                    response: Response<LoanDTO.ResponseApplyLoan>
                ) {
                    if (response.isSuccessful) {
                        _loanDetail.postValue(response.body())
                    } else if (response.code() == 400) {
                        _isExceedPlafon.postValue("Pengajuan melebihi Plafon: ${response.message()}")
                    } else {
                        _errorMessage.postValue("Failed to apply loan: ${response.code()} ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<LoanDTO.ResponseApplyLoan>, t: Throwable) {
                    _errorMessage.postValue("Network error: ${t.localizedMessage ?: "Unknown error"}")
                }
            })
    }

    fun calculateMonthlyPayment(request: LoanDTO.RequestCalculate) {
        ApiClient.userService.calculateLoan(request)
            .enqueue(object : Callback<LoanDTO.ResponseCalculate> {
                override fun onResponse(
                    call: Call<LoanDTO.ResponseCalculate>,
                    response: Response<LoanDTO.ResponseCalculate>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()?.data
                        if (data != null) {
                            _calculateLoan.postValue(response.body())
                        }
                    } else if (response.code() == 400) {
                        _isExceedPlafon.postValue("Pengajuan melebihi Plafon: ${response.message()}")
                    } else {
                        _errorMessage.postValue("Failed to calculate loan: ${response.code()} ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<LoanDTO.ResponseCalculate>, t: Throwable) {
                    _errorMessage.postValue("Network error: ${t.localizedMessage ?: "Unknown error"}")
                }
            })

    }

    fun fetchLoanSummary() {
        ApiClient.userService.getLoanSummary()
            .enqueue(object : Callback<LoanSummaryDTO.Response> {
                override fun onResponse(
                    call: Call<LoanSummaryDTO.Response>,
                    response: Response<LoanSummaryDTO.Response>
                ) {
                    if (response.isSuccessful) {
                        _loanSummary.postValue(response.body())
                    } else if (response.code() == 401) {
                        _errorMessage.postValue("Unauthorized: ${response.message()}")
                    } else if (response.code() == 403) {
                        _errorMessage.postValue("FORBIDDEN")
                    } else if (response.code() == 404) {
                        _errorMessage.postValue("NOT_FOUND")
                    } else {
                        _errorMessage.postValue("Failed to load loan summary: ${response.code()} ${response.message()}")
                    }
                }

                override fun onFailure(
                    call: Call<LoanSummaryDTO.Response>,
                    t: Throwable
                ) {
                    _errorMessage.postValue("Network error: ${t.localizedMessage ?: "Unknown error"}")
                }
            })
    }


}