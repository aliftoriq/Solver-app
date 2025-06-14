package com.example.solvr.ui.home

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.solvr.R
import com.example.solvr.ui.pengajuan.PengajuanViewModel
import com.example.solvr.utils.FormatRupiah
import com.example.solvr.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {

    private var selectedTenor = 0
    private var selectedAmount = 0

    private lateinit var viewModel: PengajuanViewModel
    private lateinit var viewModelHome: HomeViewModel

    private lateinit var tvTitlePackage: TextView
    private lateinit var tvLevel: TextView
    private lateinit var tvRate: TextView
    private lateinit var tvNominal: TextView
    private lateinit var bgCard: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val formatRupiah = FormatRupiah

        viewModel = ViewModelProvider(this)[PengajuanViewModel::class.java]
        viewModelHome = ViewModelProvider(this)[HomeViewModel::class.java]

        val animTop = AnimationUtils.loadAnimation(inflater.context, R.anim.slide_in_top)
        val animBottom = AnimationUtils.loadAnimation(inflater.context, R.anim.slide_in_bottom)
        val animLeft = AnimationUtils.loadAnimation(inflater.context, R.anim.slide_out_left)
        val animRight = AnimationUtils.loadAnimation(inflater.context, R.anim.slide_in_right)
        val animFadeIn = AnimationUtils.loadAnimation(inflater.context, R.anim.fade_in)
        val animFadeOut = AnimationUtils.loadAnimation(inflater.context, R.anim.fade_out)

        val sessionManager = SessionManager(requireContext())
        val greetingText: TextView = view.findViewById(R.id.greetingText)
        val userName = sessionManager.getUserName()

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        // Set up SeekBar for Loan Amount
        val seekLoanAmount = view.findViewById<SeekBar>(R.id.seekLoanAmount)
        val edtLoanAmount = view.findViewById<TextInputEditText>(R.id.edtLoanAmount)

        val simulationContainer =
            view.findViewById<LinearLayout>(R.id.simulationResultContainer)

        simulationContainer.visibility = View.GONE


        val greeting = when (hour) {
            in 4..10 -> "Selamat Pagi"
            in 11..14 -> "Selamat Siang"
            in 15..17 -> "Selamat Sore"
            else -> "Selamat Malam"
        }

        var bungaPerBulan = 0.055
        val biayaAdmin = 50_000.0

        tvTitlePackage = view.findViewById(R.id.title_package)
        tvLevel = view.findViewById(R.id.level_text)
        tvRate = view.findViewById(R.id.rate_tenor)
        tvNominal = view.findViewById(R.id.nominal)
        bgCard = view.findViewById(R.id.bg_card)

// Terapkan animasi ke masing-masing view
//        view.findViewById<TextView>(R.id.greetingText).startAnimation(animLeft)
        view.findViewById<RelativeLayout>(R.id.bg_card).startAnimation(animFadeIn)
        view.findViewById<FrameLayout>(R.id.headerContainer).startAnimation(animBottom)
//        view.findViewById<SeekBar>(R.id.seekLoanAmount).startAnimation(animLeft)
//        view.findViewById<MaterialButtonToggleGroup>(R.id.tenorToggleGroup).startAnimation(animBottom)


        viewModel.fetchLoanSummary()

        viewModel.loanSummary.observe(viewLifecycleOwner) { summary ->
            summary?.let {

                Handler(Looper.getMainLooper()).postDelayed({
                    tvTitlePackage.text = it.data?.plafonPackage?.name
                    tvLevel.text = "Level : ${it.data?.plafonPackage?.level}"
                    tvRate.text =
                        "Rate ${it.data?.plafonPackage?.interestRate} - Tenor ${it.data?.plafonPackage?.maxTenorMonths}"
                    tvNominal.text = formatRupiah.format(it.data?.plafonPackage?.amount ?: 0)

                    seekLoanAmount.max = (it.data?.remainingPlafon?.toInt() ?: 10_000_000) / 100_000
                    bungaPerBulan = (it.data?.plafonPackage?.interestRate ?: 0.015) as Double


                    val plafon = it.data?.plafonPackage
                    if (plafon?.level == 1) {
                        bgCard.setBackgroundResource(R.drawable.card_bronze)
                    } else if (plafon?.level == 2) {
                        bgCard.setBackgroundResource(R.drawable.card_silver)
                    } else if (plafon?.level == 3) {
                        bgCard.setBackgroundResource(R.drawable.card_gold)
                    } else if (plafon?.level == 4) {
                        bgCard.setBackgroundResource(R.drawable.card_platinum)
                    } else if (plafon?.level == 5) {
                        bgCard.setBackgroundResource(R.drawable.card_diamond)
                    } else {
                        bgCard.setBackgroundResource(R.drawable.card)
                    }
                    bgCard.startAnimation(animRight)
                }, 1000)

            }
        }



        greetingText.text = if (userName.isNullOrEmpty()) {
            "$greeting,\nSolvr-gengs!"
        } else {
            "$greeting,\n$userName!"
        }

        seekLoanAmount.max = 100
        seekLoanAmount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedAmount = progress * 100_000
                edtLoanAmount.setText(selectedAmount.formatRupiah())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Set up Tenor Toggle Button Group
        val tenorToggleGroup = view.findViewById<MaterialButtonToggleGroup>(R.id.tenorToggleGroup)
        val allTenorButtons = listOf(
            view.findViewById<MaterialButton>(R.id.btnTenor3),
            view.findViewById<MaterialButton>(R.id.btnTenor6),
            view.findViewById<MaterialButton>(R.id.btnTenor9),
            view.findViewById<MaterialButton>(R.id.btnTenor12)
        )

        tenorToggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->

            if (isChecked) {
                allTenorButtons.forEach { button ->
                    if (button.id == checkedId) {
                        button.setBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.secondary
                            )
                        )
                        button.setTextColor(Color.WHITE)
                    } else {
                        button.setBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.primary
                            )
                        )
                        button.setTextColor(Color.WHITE)
                    }
                }

                // Set selected tenor value
                selectedTenor = when (checkedId) {
                    R.id.btnTenor3 -> 3
                    R.id.btnTenor6 -> 6
                    R.id.btnTenor9 -> 9
                    R.id.btnTenor12 -> 12
                    else -> 0
                }
            }
        }


        // Set up Calculate Button
        val btnSimulasi = view.findViewById<Button>(R.id.btnCalculate)
        btnSimulasi.setOnClickListener {
            if (selectedAmount == 0 || selectedTenor == 0) {
                       Toast.makeText(
                    requireContext(),
                    "Pilih nominal dan tenor terlebih dahulu.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            viewModelHome.hitungSimulasi(selectedAmount, selectedTenor, bungaPerBulan, biayaAdmin.toInt())

            val txtCicilan = view.findViewById<TextView>(R.id.txtCicilanBulanan)
            val txtTotal = view.findViewById<TextView>(R.id.txtTotalPembayaran)
            val txtBunga = view.findViewById<TextView>(R.id.txtBiayaBunga)
            val txtAdmin = view.findViewById<TextView>(R.id.txtBiayaAdmin)

            val txtTenor = view.findViewById<TextView>(R.id.txtTenor)
            val txtPinjaman = view.findViewById<TextView>(R.id.txtPinjaman)
            val txtRate = view.findViewById<TextView>(R.id.txtRate)

            txtTenor.text = "$selectedTenor Bulan"
            txtPinjaman.text = "Rp ${selectedAmount.toInt().formatRupiah()}"
            txtRate.text = "${(bungaPerBulan*100)}%"


            viewModelHome.simulasiResult.observe(viewLifecycleOwner) { result ->
                result?.let {
                    txtCicilan.text = it.cicilanBulanan.formatRupiah()
                    txtTotal.text = it.totalPembayaran.formatRupiah()
                    txtBunga.text = it.bunga.formatRupiah()
                    txtAdmin.text = it.admin.formatRupiah()

                    txtTenor.text = "${it.tenor} bulan"
                    txtPinjaman.text = it.pinjaman.formatRupiah()
                    txtRate.text = "${(it.ratePerBulan * 100)}% / bulan"

                    simulationContainer.visibility = View.VISIBLE
                }
            }

            simulationContainer.visibility = View.VISIBLE
            simulationContainer.startAnimation(animTop)
        }

        return view
    }

    private fun updateSimulationResult(
        view: View,
        selectedTenor: Int,
        selectedAmount: Int,
        bungaPerBulan: Double,
        biayaAdmin: Double
    ) {
        val totalBunga = selectedAmount * bungaPerBulan * selectedTenor
        val totalPembayaran = selectedAmount + totalBunga + biayaAdmin
        val cicilanBulanan = totalPembayaran / selectedTenor

        view.findViewById<TextView>(R.id.txtTenor).text = "$selectedTenor Bulan"
        view.findViewById<TextView>(R.id.txtPinjaman).text = "Rp ${selectedAmount.formatRupiah()}"
        view.findViewById<TextView>(R.id.txtRate).text = "${(bungaPerBulan * 100)}%"
        view.findViewById<TextView>(R.id.txtCicilanBulanan).text = "Rp ${cicilanBulanan.toInt().formatRupiah()}"
        view.findViewById<TextView>(R.id.txtTotalPembayaran).text = "Rp ${totalPembayaran.toInt().formatRupiah()}"
        view.findViewById<TextView>(R.id.txtBiayaBunga).text = "Rp ${totalBunga.toInt().formatRupiah()}"
        view.findViewById<TextView>(R.id.txtBiayaAdmin).text = "Rp ${biayaAdmin.toInt().formatRupiah()}"
    }


    fun Int.formatRupiah(): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return formatter.format(this).replace(",00", "")
    }


}


