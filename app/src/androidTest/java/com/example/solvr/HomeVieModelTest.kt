package com.example.solvr

import com.example.solvr.ui.home.HomeViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class HomeVieModelTest {


    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        viewModel = HomeViewModel()
    }

    @Test
    fun `hitungSimulasi dengan input valid menghasilkan hasil yang sesuai`() {
        // Arrange
        val amount = 3_000_000
        val tenor = 6
        val rate = 0.055
        val admin = 50000

        // Act
        viewModel.hitungSimulasi(amount, tenor, rate, admin)

        // Assert
        val result = viewModel.simulasiResult.value
        assertNotNull(result)
        assertEquals(3_000_000, result?.pinjaman)
        assertEquals(6, result?.tenor)
        assertEquals(0.055, result?.ratePerBulan)
        assertEquals(990_000, result?.bunga)
        assertEquals(50_000, result?.admin)
        assertEquals((673_333), result?.cicilanBulanan)
        assertEquals(3_000_000, result?.totalPembayaran)
    }
}
