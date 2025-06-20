package com.example.solvr.ui.pengajuan

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
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
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.solvr.R
import com.example.solvr.models.LoanDTO
import com.example.solvr.ui.auth.LoginActivity
import com.example.solvr.ui.history.HistoryFragment
import com.example.solvr.ui.plafond.PlafondFragment
import com.example.solvr.ui.profile.EditProfileActivity
import com.example.yourapp.utils.SwitchAllertCustom
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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
    private lateinit var btnSubmitLoan: Button
    private lateinit var viewModel: PengajuanViewModel
    private lateinit var loanCard: CardView

    private lateinit var btnHistory: TextView

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
    private var currentLocation: Location? = null

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

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
        super.onViewCreated(view, savedInstanceState)

        val animTop = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_top)
        val animBottom = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_bottom)
        val animLeft = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_left)
        val animRight = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_right)
        val animFadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        val animFadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)

        view.findViewById<FrameLayout>(R.id.headerContainer).startAnimation(animBottom)

        // Initialize location services
        initializeLocationServices()

        // Initialize UI components
        initializeUI(view)

        // Set up observers
        setupObservers()

        // Request location permission and get location immediately
        requestLocationPermissionAndGetLocation()

        // Fetch loan summary
        viewModel.fetchLoanSummary()
    }

    private fun initializeLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(2000L)
            .setMaxUpdateDelayMillis(10000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                currentLocation = location
                Log.d("Location", "Location updated: ${location.latitude}, ${location.longitude}")

                fusedLocationClient.removeLocationUpdates(this)
            }
        }
    }

    private fun initializeUI(view: View) {
        shimmerLayout = view.findViewById(R.id.shimmerLoanCard)
        shimmerLayout.startShimmer()

        loanCard = view.findViewById(R.id.loanCard)
        loanCard.visibility = View.GONE

        seekBar = view.findViewById(R.id.seekLoanAmount)
        toggleGroup = view.findViewById(R.id.tenorToggleGroup)
        edtLoanAmount = view.findViewById(R.id.edtLoanAmount)
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

        btnHistory = view.findViewById(R.id.btnHistory)

        activeLoanApplicationContainer.visibility = View.GONE

        btnHistory.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HistoryFragment())
                .addToBackStack(null)
                .commit()
        }

        // SeekBar listener
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedAmount = (progress / 100000) * 100000
                edtLoanAmount.setText(formatRupiah(selectedAmount))
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // Tenor button setup
        val allTenorButtons = listOf(
            view.findViewById<MaterialButton>(R.id.btnTenor3Pengajuan),
            view.findViewById<MaterialButton>(R.id.btnTenor6Pengajuan),
            view.findViewById<MaterialButton>(R.id.btnTenor9Pengajuan),
            view.findViewById<MaterialButton>(R.id.btnTenor12Pengajuan)
        )

        toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
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

                selectedTenor = when (checkedId) {
                    R.id.btnTenor3Pengajuan -> 3
                    R.id.btnTenor6Pengajuan -> 6
                    R.id.btnTenor9Pengajuan -> 9
                    R.id.btnTenor12Pengajuan -> 12
                    else -> 6
                }
            }
        }

        // Submit button listener
        btnSubmitLoan.setOnClickListener {
            if (hasLocationPermission()) {
                showPreviewDialog()
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun setupObservers() {
        viewModel.loanDetail.observe(viewLifecycleOwner) { response ->
            response?.let {
                //       //       Toast.makeText(
//                    requireContext(),
//                    "Pengajuan berhasil: ${it.message}",
//                    Toast.LENGTH_SHORT
//                ).show()
                viewModel.fetchLoanSummary()

                Handler(Looper.getMainLooper()).postDelayed({
                    loanCard.visibility = View.VISIBLE
                    loanCard.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            R.anim.slide_in_bottom
                        )
                    )
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE
                }, 1000)
            }
        }

        viewModel.isExceedPlafon.observe(viewLifecycleOwner) { error ->
            error?.let {
                //       //       Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
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
                    //       //       Toast.makeText(requireContext(), errorCode, Toast.LENGTH_SHORT).show()
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                loanCard.visibility = View.VISIBLE
                loanCard.startAnimation(
                    AnimationUtils.loadAnimation(
                        requireContext(),
                        R.anim.slide_in_bottom
                    )
                )
                shimmerLayout.stopShimmer()
                shimmerLayout.visibility = View.GONE
            }, 1000)
        }

        viewModel.loanSummary.observe(viewLifecycleOwner) { summary ->
            summary?.let {
                tvName.text = "Halo, ${it.data?.name ?: "User"}!"
                tvPlafonPackage.text = it.data?.plafonPackage?.name ?: "-"
                tvRekening.text = "Rekening ${it.data?.accountNumber ?: "-"}"
                val activeLoan = (it.data?.remainingLoan ?: 0).toString().toDoubleOrNull() ?: 0.0
                tvActiveLoan.text = formatRupiah(activeLoan.toInt())

                interestRate = (it.data?.plafonPackage?.interestRate ?: 0.015) as Double

                val remainingLoan =
                    it.data?.remainingPlafon?.toString()?.toDoubleOrNull() ?: 10_000_000.0
                val maxLoan = maxOf(remainingLoan.toInt())
                seekBar.max = maxLoan

                if (it.data?.activeLoanApplication != null) {
                    tvReviewStatus.text = it.data.activeLoanApplication.status ?: "-"
                    tvTangal.text = it.data.activeLoanApplication.requestedAt ?: "-"
                    tvJumlahPinjaman.text =
                        formatRupiah((it.data.activeLoanApplication.loanAmount ?: 0.0).toInt())

                    val monthlyInstallment =
                        it.data.activeLoanApplication.monthlyPayment?.let { formatRupiah(it.toInt()) }
                            ?: "-"
                    tvCicilan.text = monthlyInstallment
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    if (it.data?.activeLoanApplication?.status != null) {
                        activeLoanApplicationContainer.visibility = View.VISIBLE
                        activeLoanApplicationContainer.startAnimation(
                            AnimationUtils.loadAnimation(
                                requireContext(),
                                R.anim.fade_in
                            )
                        )
                    }
                    loanCard.visibility = View.VISIBLE
                    loanCard.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            R.anim.slide_in_bottom
                        )
                    )
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE
                }, 1000)
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissionAndGetLocation() {
        if (hasLocationPermission()) {
            getCurrentLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        // Try to get last known location first
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null && isLocationRecentAndAccurate(location)) {
                currentLocation = location
                Log.d(
                    "Location",
                    "Got last known location: ${location.latitude}, ${location.longitude}"
                )
            } else {
                // Request fresh location updates
                Log.d("Location", "Requesting fresh location updates...")
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )

                // Stop location updates after 15 seconds if no location received
                Handler(Looper.getMainLooper()).postDelayed({
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }, 15000)
            }
        }.addOnFailureListener { exception ->
            Log.e("Location", "Failed to get location", exception)
            // Still try to request location updates
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun isLocationRecentAndAccurate(location: Location): Boolean {
        val locationAge = System.currentTimeMillis() - location.time
        return locationAge < 5 * 60 * 1000 && location.accuracy < 100 // 5 minutes and 100m accuracy
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            //       //       Toast.makeText(requireContext(), "Izin lokasi diperlukan untuk mengajukan pinjaman", Toast.LENGTH_LONG).show()
        }
    }

    private fun formatRupiah(number: Int): String {
        val localeID = Locale("in", "ID")
        val format = NumberFormat.getCurrencyInstance(localeID)
        return format.format(number).replace(",00", "")
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocationAndSubmit() {
        if (currentLocation != null) {
            // Use cached location if available
            submitWithLocation(currentLocation!!)
        } else {
            // Get fresh location
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    submitWithLocation(location)
                } else {
                    // Request new location update
                    val tempCallback = object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            val loc = result.lastLocation
                            if (loc != null) {
                                submitWithLocation(loc)
                                fusedLocationClient.removeLocationUpdates(this)
                            }
                        }
                    }

                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        tempCallback,
                        Looper.getMainLooper()
                    )

                    // Timeout after 10 seconds
                    Handler(Looper.getMainLooper()).postDelayed({
                        fusedLocationClient.removeLocationUpdates(tempCallback)
                        //       //       Toast.makeText(requireContext(), "Gagal mendapatkan lokasi terkini", Toast.LENGTH_SHORT).show()
                    }, 10000)
                }
            }.addOnFailureListener {
                //       //       Toast.makeText(requireContext(), "Gagal mendapatkan lokasi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitWithLocation(location: Location) {
        Log.d("Location", "Submitting with location: ${location.latitude}, ${location.longitude}")

        val request = LoanDTO.Request(
            loanAmount = selectedAmount.toDouble(),
            loanTenor = selectedTenor,
            longitude = location.longitude,
            latitude = location.latitude
        )

        viewModel.applyLoan(request)
        loanCard.visibility = View.INVISIBLE
        shimmerLayout.startShimmer()
        shimmerLayout.visibility = View.VISIBLE
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun showPreviewDialog() {

        viewModel.calculateMonthlyPayment(
            LoanDTO.RequestCalculate(
                loanAmount = selectedAmount.toDouble(),
                loanTenor = selectedTenor,
            )
        )

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.layout_konfirmasi_pengajuan)
        dialog.setCancelable(true)

        val window = dialog.window
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvLocationStatus = dialog.findViewById<TextView>(R.id.tvLocationStatus)

        // Check if we have current location
        if (currentLocation != null) {
            val latitude = currentLocation!!.latitude
            val longitude = currentLocation!!.longitude
            tvLocationStatus.text = "Lokasi: $latitude, $longitude"
            isiDataPreview(dialog)
        } else {
            // Try to get location again
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLocation = location
                    val latitude = location.latitude
                    val longitude = location.longitude
                    tvLocationStatus.text = "Lokasi: $latitude, $longitude"
                    isiDataPreview(dialog)
                } else {
                    dialog.dismiss()
                    SwitchAllertCustom(requireContext()).show(
                        message = "Gagal mendapatkan lokasi. Mohon aktifkan GPS terlebih dahulu.",
                        onYes = {
                            startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        },
                        onNo = { }
                    )
                }
            }.addOnFailureListener {
                dialog.dismiss()
                //       //       Toast.makeText(requireContext(), "Terjadi kesalahan saat mendapatkan lokasi", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun isiDataPreview(dialog: Dialog) {
        val txtCicilan = dialog.findViewById<TextView>(R.id.txtCicilan)
        val txtTotal = dialog.findViewById<TextView>(R.id.txtTotal)
        val txtBunga = dialog.findViewById<TextView>(R.id.txtBunga)
        val txtAdmin = dialog.findViewById<TextView>(R.id.txtAdmin)
        val txtRate = dialog.findViewById<TextView>(R.id.txtRate)
        val txtPinjaman = dialog.findViewById<TextView>(R.id.txtPinjaman)
        val txtTenor = dialog.findViewById<TextView>(R.id.txtTenor)
        val txtDisbursedAmount = dialog.findViewById<TextView>(R.id.txtDisbursedAmount)
        val txtAccountNumber = dialog.findViewById<TextView>(R.id.txtAccountNumber)
        val txtAddress = dialog.findViewById<TextView>(R.id.txtAddress)

        viewModel.calculateLoan.observe(viewLifecycleOwner) { response ->
            val data = response?.data ?: return@observe

            txtCicilan.text = formatRupiah(data.monthlyPayment?.toInt() ?: 0)
            txtTotal.text = formatRupiah(data.totalPayment?.toInt() ?: 0)
            txtBunga.text = formatRupiah(data.totalInterest?.toInt() ?: 0)
            txtAdmin.text = formatRupiah(data.adminFee?.toInt() ?: 0)

            txtRate.text = "Rate: ${data.rate ?: 0.0}%"
            txtPinjaman.text = "Pinjaman: ${formatRupiah(data.amount?.toInt() ?: 0)}"
            txtTenor.text = "Tenor: ${data.tenor ?: 0} bulan"

            txtDisbursedAmount.text = formatRupiah(data.amountDisbursed?.toInt() ?: 0)
            txtAccountNumber.text = data.accountNumber ?: "-"
            txtAddress.text = "Alamat Saat Ini: ${data.address ?: "-"}"

            txtDisbursedAmount.text = formatRupiah(data.amountDisbursed?.toInt() ?: 0)
            txtAccountNumber.text = data.accountNumber ?: "-"
            txtAddress.text = "Alamat Saat Ini: ${data.address ?: "-"}"
        }

        dialog.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            dialog.dismiss()
            val switchAlert = SwitchAllertCustom(requireContext())
            switchAlert.show(
                message = "Apakah Anda yakin untuk mengajukan?",
                onYes = {
                    getCurrentLocationAndSubmit()
                },
                onNo = {}
            )
        }
    }

    override fun onPause() {
        super.onPause()
        // Remove location updates when fragment is paused
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up location updates
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


//
//    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
//    private fun showPreviewDialog() {
//        val dialog = Dialog(requireContext())
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.setContentView(R.layout.layout_konfirmasi_pengajuan)
//        dialog.setCancelable(true)
//
//        // Set dialog window properties
//        val window = dialog.window
//        window?.setLayout(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )
//        window?.setBackgroundDrawableResource(android.R.color.transparent)
//
//        val tvLocationStatus = dialog.findViewById<TextView>(R.id.tvLocationStatus)
//
//        fusedLocationClient.lastLocation
//            .addOnSuccessListener { location: Location? ->
//                if (location != null) {
//                    val latitude = location.latitude
//                    val longitude = location.longitude
//                    tvLocationStatus.text = "Lokasi: $latitude, $longitude"
//                } else {
//                    //       //       Toast.makeText(requireContext(), "Gagal mendapatkan lokasi, Hidupkan GPS terlebih dahulu", Toast.LENGTH_SHORT).show()
//                }
//            }
//            .addOnFailureListener {
//                tvLocationStatus.text = "Gagal mendapatkan lokasi"
//            }
//
//        // Initialize dialog views
//        val txtCicilan = dialog.findViewById<TextView>(R.id.txtCicilan)
//        val txtTotal = dialog.findViewById<TextView>(R.id.txtTotal)
//        val txtBunga = dialog.findViewById<TextView>(R.id.txtBunga)
//        val txtAdmin = dialog.findViewById<TextView>(R.id.txtAdmin)
//        val txtRate = dialog.findViewById<TextView>(R.id.txtRate)
//        val txtPinjaman = dialog.findViewById<TextView>(R.id.txtPinjaman)
//        val txtTenor = dialog.findViewById<TextView>(R.id.txtTenor)
//        val txtDisbursedAmount = dialog.findViewById<TextView>(R.id.txtDisbursedAmount)
//        val txtAccountNumber = dialog.findViewById<TextView>(R.id.txtAccountNumber)
//        val txtAddress = dialog.findViewById<TextView>(R.id.txtAddress)
//        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
//        val btnConfirm = dialog.findViewById<Button>(R.id.btnConfirm)
//
//        val request : LoanDTO.RequestCalculate = LoanDTO.RequestCalculate(
//            loanAmount = selectedAmount.toDouble(),
//            loanTenor = selectedTenor,
//        )
//
//        viewModel.calculateMonthlyPayment(request)
//
////        // Calculate loan details
////        val totalInterest = selectedAmount * interestRate
////        val adminFee = 50000.0 // Biaya admin tetap
////        val totalPayment = selectedAmount + totalInterest + adminFee
////        val monthlyInstallment = totalPayment / selectedTenor
////        val disbursedAmount = selectedAmount - adminFee // Dana yang dicairkan
////
////        // Set calculated values to dialog
////        txtCicilan.text = formatRupiah(monthlyInstallment.toInt())
////        txtTotal.text = formatRupiah(totalPayment.toInt())
////        txtBunga.text = formatRupiah(totalInterest.toInt())
////        txtAdmin.text = formatRupiah(adminFee.toInt())
////
////        // Set rate info
////        txtRate.text = "Rate: ${(interestRate * 100)}%"
////        txtPinjaman.text = "Pinjaman: ${formatRupiah(selectedAmount.toInt())}"
////        txtTenor.text = "Tenor: $selectedTenor"
////
////        // Set consumer information (dummy data)
////        txtDisbursedAmount.text = formatRupiah(disbursedAmount.toInt())
////        txtAccountNumber.text = "2910–025–5465"
////        txtAddress.text = "Alamat Saat Ini: JL Pondok Indah No. 123, Jakarta Selatan"
//
//        // Handle cancel button
//        btnCancel.setOnClickListener {
//            dialog.dismiss()
//        }
//
//        // Handle confirm button
//        btnConfirm.setOnClickListener {
//            dialog.dismiss()
//            // Show original confirmation dialog setelah preview
//            val switchAlert = SwitchAllertCustom(requireContext())
//            switchAlert.show(
//                message = "Apakah Anda yakin untuk mengajukan pinjaman sebesar ${formatRupiah(selectedAmount.toInt())}?",
//                onYes = {
//                    userConfirmedSubmission = true
//                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//                },
//                onNo = {
//                    userConfirmedSubmission = false
//                }
//            )
//        }
//
//        viewModel.calculateLoan.observe(viewLifecycleOwner) { response ->
//            response?.let {
//                txtCicilan.text = formatRupiah(it.data?.monthlyPayment?.toInt() ?: 0)
//                txtTotal.text = formatRupiah(it.data?.totalPayment?.toInt() ?: 0)
//                txtBunga.text = formatRupiah(it.data?.totalInterest?.toInt() ?: 0)
//                txtAdmin.text = formatRupiah(it.data?.adminFee?.toInt() ?: 0)
//                txtRate.text = "Rate: ${(it.data?.rate ?: 0.015)}%"
//                txtPinjaman.text = "Pinjaman: ${formatRupiah(it.data?.amount?.toInt() ?: 0)}"
//                txtTenor.text = "Tenor: $selectedTenor"
//                txtDisbursedAmount.text = formatRupiah(it.data?.amountDisbursed?.toInt() ?: 0)
//                txtAccountNumber.text = it.data?.accountNumber ?: "-"
//                txtAddress.text = "Alamat Saat Ini: ${it.data?.address ?: "-"}"
//
//                interestRate = it.data?.rate ?: 0.015
//            }
//            dialog.show()
//        }
//    }


}
