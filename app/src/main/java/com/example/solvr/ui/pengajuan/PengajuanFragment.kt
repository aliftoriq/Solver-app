package com.example.solvr.ui.pengajuan

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.solvr.R
import com.example.solvr.models.LoanDTO
import com.example.solvr.ui.auth.LoginActivity
import com.example.solvr.ui.profile.EditProfileActivity
import com.example.yourapp.utils.SwitchAllertCustom
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.util.Locale
import kotlin.Int

class PengajuanFragment : Fragment() {

    private lateinit var seekBar: SeekBar
    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var edtLoanAmount: TextInputEditText
    private lateinit var txtCicilanBulanan: TextView
    private lateinit var txtTotalPembayaran: TextView
    private lateinit var btnSubmitLoan: Button
    private lateinit var viewModel: PengajuanViewModel
    private lateinit var loanCard: CardView

    private lateinit var tvName: TextView
    private lateinit var tvPlafonPackage: TextView
    private lateinit var tvRekening: TextView
    private lateinit var tvActiveLoan: TextView

    private lateinit var tvReviewStatus: TextView
    private lateinit var tvTangal: TextView
    private lateinit var tvJumlahPinjaman: TextView
    private lateinit var tvCicilan: TextView

    private lateinit var activeLoanApplicationContainer: FrameLayout

    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var userConfirmedSubmission = false


    private var selectedAmount: Int = 1_000_000
    private var selectedTenor: Int = 6
    private var interestRate: Double = 0.015

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pengajuan, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val animTop = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_top)
        val animBottom = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_bottom)
        val animLeft = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_left)
        val animRight = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_right)
        val animFadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        val animFadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)

        view.findViewById<FrameLayout>(R.id.headerContainer).startAnimation(animBottom)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Init shimmer AFTER inflating the view
        shimmerLayout = view.findViewById(R.id.shimmerLoanCard)
        shimmerLayout.startShimmer()

        loanCard = view.findViewById(R.id.loanCard)
        loanCard.visibility = View.GONE

        seekBar = view.findViewById(R.id.seekLoanAmount)
        toggleGroup = view.findViewById(R.id.tenorToggleGroup)
        edtLoanAmount = view.findViewById(R.id.edtLoanAmount)
        txtCicilanBulanan = view.findViewById(R.id.txtCicilanBulanan)
        txtTotalPembayaran = view.findViewById(R.id.txtTotalPembayaran)
        btnSubmitLoan = view.findViewById(R.id.btnAjukan)

        viewModel = ViewModelProvider(this)[PengajuanViewModel::class.java]
        tvName = view.findViewById(R.id.etName)
        tvPlafonPackage = view.findViewById(R.id.etPlafonPackage)
        tvRekening = view.findViewById(R.id.etRekening)
        tvActiveLoan = view.findViewById(R.id.ecActiveLoan)

        tvReviewStatus = view.findViewById(R.id.tvReviewStatus)
        tvTangal = view.findViewById(R.id.tvTanggal)
        tvJumlahPinjaman = view.findViewById(R.id.tvJumlahPinjaman)
        tvCicilan = view.findViewById(R.id.tvCicilan)
        activeLoanApplicationContainer = view.findViewById(R.id.activeLoanApplicationContainer)

        activeLoanApplicationContainer.visibility = View.GONE

        // Update nominal pinjaman
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedAmount = (progress / 100000) * 100000
                edtLoanAmount.setText(formatRupiah(selectedAmount))
                updateSimulation()
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        val allTenorButtons = listOf(
            view.findViewById<MaterialButton>(R.id.btnTenor3),
            view.findViewById<MaterialButton>(R.id.btnTenor6),
            view.findViewById<MaterialButton>(R.id.btnTenor9),
            view.findViewById<MaterialButton>(R.id.btnTenor12)
        )

        // Pilih tenor
        toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {

                allTenorButtons.forEach { button ->
                    if (button.id == checkedId) {
                        // Change selected button to primary color
                        button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
                        button.setTextColor(Color.WHITE)
                    } else {
                        // Reset unselected buttons to secondary color
                        button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary))
                        button.setTextColor(Color.WHITE )
                    }
                }

                selectedTenor = when (checkedId) {
                    R.id.btnTenor3 -> 3
                    R.id.btnTenor6 -> 6
                    R.id.btnTenor9 -> 9
                    R.id.btnTenor12 -> 12
                    else -> 6
                }
                updateSimulation()
            }
        }

        // Ajukan pinjaman
        btnSubmitLoan.setOnClickListener {
            val switchAlert = SwitchAllertCustom(requireContext())
            switchAlert.show(
                message = "Apakah Anda yakin untuk mengajukan?",
                onYes = {
                    userConfirmedSubmission = true
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                },
                onNo = {
                    userConfirmedSubmission = false
                }
            )
        }


        viewModel.loanDetail.observe(viewLifecycleOwner) { response ->
            response?.let {
                Toast.makeText(
                    requireContext(),
                    "Pengajuan berhasil: ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.fetchLoanSummary()

                Handler(Looper.getMainLooper()).postDelayed({
                    loanCard.visibility = View.VISIBLE
                    loanCard.startAnimation(animBottom)
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE
                }, 1000)
            }
        }

        viewModel.isExceedPlafon.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorCode ->
            val switchAlert = SwitchAllertCustom(requireContext())

            when (errorCode) {
                "FORBIDDEN" -> {
                    switchAlert.show(
                        message = "Anda harus login terlebih dahulu untuk melihat data pinjaman.",
                        onYes = {
                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            startActivity(intent)
                        },
                        onNo = { }
                    )
                }

                "NOT_FOUND" -> {
                    switchAlert.show(
                        message = "Data belum lengkap. Silakan lengkapi data profil Anda terlebih dahulu.",
                        onYes = {
                            val intent = Intent(requireContext(), EditProfileActivity::class.java)
                            startActivity(intent)
                        },
                        onNo = { }
                    )
                }

                else -> {
                    Toast.makeText(requireContext(), errorCode, Toast.LENGTH_SHORT).show()
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                loanCard.visibility = View.VISIBLE
                loanCard.startAnimation(animBottom)
                shimmerLayout.stopShimmer()
                shimmerLayout.visibility = View.GONE
            }, 1000)
        }


        updateSimulation()

        viewModel.loanSummary.observe(viewLifecycleOwner) { summary ->
            summary?.let {
                tvName.text = "Halo, ${it.data?.name ?: "User"}!"
                tvPlafonPackage.text = it.data?.plafonPackage?.name ?: "-"
                tvRekening.text = "Rekening ${it.data?.accountNumber ?: "-"}"
                val activeLoan = (it.data?.remainingLoan ?: 0).toString().toDoubleOrNull() ?: 0.0
                tvActiveLoan.text = formatRupiah(activeLoan.toInt())

                interestRate = (it.data?.plafonPackage?.interestRate ?: 0.015) as Double

                val remainingLoan = it.data?.remainingPlafon?.toString()?.toDoubleOrNull() ?: 10_000_000.0
                val maxLoan = maxOf(remainingLoan.toInt())
                seekBar.max = maxLoan

               if (it.data?.activeLoanApplication != null){
                   tvReviewStatus.text = it.data.activeLoanApplication.status ?: "-"
                   tvTangal.text = it.data.activeLoanApplication.requestedAt ?: "-"
                   tvJumlahPinjaman.text = formatRupiah((it.data.activeLoanApplication.loanAmount ?: 0.0).toInt())
                   tvCicilan.text = formatRupiah((it.data.activeLoanApplication.monthlyPayment ?: 0.0).toInt())
               }

                Handler(Looper.getMainLooper()).postDelayed({
                    if (it.data?.activeLoanApplication != null) {
                        activeLoanApplicationContainer.visibility = View.VISIBLE
                        activeLoanApplicationContainer.startAnimation(animFadeIn)
                    }
                    loanCard.visibility = View.VISIBLE
                    loanCard.startAnimation(animBottom)
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE
                }, 1000)

            }
        }

        viewModel.fetchLoanSummary()


    }

    @SuppressLint("SetTextI18n")
    private fun updateSimulation() {
        interestRate = 0.015
        val totalInterest = selectedAmount * interestRate
        val totalPayment = selectedAmount + totalInterest
        val monthlyInstallment = totalPayment / selectedTenor

        txtCicilanBulanan.text = "Cicilan per bulan: ${formatRupiah(monthlyInstallment.toInt())}"
        txtTotalPembayaran.text = "Total pembayaran: ${formatRupiah(totalPayment.toInt())}"
    }

    private fun formatRupiah(number: Int): String {
        val localeID = Locale("in", "ID")
        val format = NumberFormat.getCurrencyInstance(localeID)
        return format.format(number).replace(",00", "")
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && userConfirmedSubmission) {
            Log.d("Location", "Klik Ya")
            getCurrentLocationAndSubmit()
        } else {
            Toast.makeText(requireContext(), "Izin lokasi diperlukan untuk melanjutkan", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocationAndSubmit() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            val loc = location ?: return@addOnSuccessListener
            Log.d("Location", "Latitude: ${loc.latitude}, Longitude: ${loc.longitude}")

            val request = LoanDTO.Request(
                loanAmount = selectedAmount.toDouble(),
                loanTenor = selectedTenor,
                longitude = loc.longitude,
                latitude = loc.latitude
            )

            viewModel.applyLoan(request)
            loanCard.visibility = View.INVISIBLE
            shimmerLayout.startShimmer()
            shimmerLayout.visibility = View.VISIBLE
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Gagal mendapatkan lokasi", Toast.LENGTH_SHORT).show()
        }
    }


}
