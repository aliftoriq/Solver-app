package com.example.solvr.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.solvr.models.LoanDTO


class HomeViewModel : ViewModel() {
    private val _simulasiResult = MutableLiveData<LoanDTO.SimulasiResult>()
    val simulasiResult: LiveData<LoanDTO.SimulasiResult> get() = _simulasiResult

    fun hitungSimulasi(
        amount: Int,
        tenor: Int,
        ratePerBulan: Double,
        biayaAdmin: Int
    ) {
        if (amount <= 0 || tenor <= 0 || ratePerBulan < 0) {
            // Optional: throw IllegalArgumentException("Input tidak valid")
            return
        }

        val totalBunga = amount * ratePerBulan * tenor
        val totalPembayaran = amount + totalBunga + biayaAdmin
        val cicilanBulanan = totalPembayaran / tenor

        _simulasiResult.value = LoanDTO.SimulasiResult(
            pinjaman = amount,
            tenor = tenor,
            ratePerBulan = ratePerBulan,
            bunga = totalBunga.toInt(),
            admin = biayaAdmin,
            cicilanBulanan = cicilanBulanan.toInt(),
            totalPembayaran = totalPembayaran.toInt()
        )
    }
}