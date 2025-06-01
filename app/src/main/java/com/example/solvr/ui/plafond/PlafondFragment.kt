package com.example.solvr.ui.plafond

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.solvr.R
import com.example.solvr.models.PlafonDTO
import com.example.solvr.ui.pengajuan.PengajuanViewModel
import com.example.solvr.utils.FormatRupiah
import com.example.solvr.viewmodel.PlafondViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlafondFragment : Fragment() {

    private val plafonViewModel: PlafondViewModel by viewModels()
    private val pengajuanViewModel: PengajuanViewModel by viewModels()
    private lateinit var plafonRecyclerView: RecyclerView
    private lateinit var plafonAdapter: PlafonAdapter
    private lateinit var shimmerLayout: ShimmerFrameLayout


    private lateinit var tvTitlePackage: TextView
    private lateinit var tvLevel: TextView
    private lateinit var tvRate: TextView
    private lateinit var tvNominal: TextView
    private lateinit var bgCard: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_plafond, container, false)

        tvTitlePackage = root.findViewById(R.id.title_package)
        tvLevel = root.findViewById(R.id.level_text)
        tvRate = root.findViewById(R.id.rate_tenor)
        tvNominal = root.findViewById(R.id.nominal)
        bgCard = root.findViewById(R.id.bg_card)

        val plafondContainer: LinearLayout = root.findViewById(R.id.plafonProgressContainer)

        val tvPinjamanAktif: TextView = root.findViewById(R.id.tvPinjamanAktif)
        val tvPlafon: TextView = root.findViewById(R.id.tvPlafon)
        val progressBar: ProgressBar = root.findViewById(R.id.progressBarPlafon)
        val tvPersentase: TextView = root.findViewById(R.id.tvPersentase)


        val animBottom = AnimationUtils.loadAnimation(inflater.context, R.anim.slide_in_bottom)
        root.findViewById<FrameLayout>(R.id.headerContainer).startAnimation(animBottom)

        shimmerLayout = root.findViewById(R.id.shimmerLayout)
        shimmerLayout.startShimmer()

        plafonRecyclerView = root.findViewById(R.id.recyclerPlafon)
        plafonRecyclerView.visibility = View.GONE
        plafonRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        plafonViewModel.fetchFromLocal()

        plafonViewModel.plafonList.observe(viewLifecycleOwner) { plafonList ->
            plafonList?.let {
                plafonAdapter = PlafonAdapter(it)
                plafonRecyclerView.adapter = plafonAdapter

                Handler(Looper.getMainLooper()).postDelayed({
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE
                    plafonRecyclerView.visibility = View.VISIBLE
                    plafonRecyclerView.startAnimation(animBottom)
                }, 1000)
            }
        }

        pengajuanViewModel.fetchLoanSummary()

        pengajuanViewModel.loanSummary.observe(viewLifecycleOwner) { summary ->
            summary?.let {

                Handler(Looper.getMainLooper()).postDelayed({
                    tvTitlePackage.text = it.data?.plafonPackage?.name
                    tvLevel.text = "Level : ${it.data?.plafonPackage?.level}"
                    tvRate.text =
                        "Rate ${it.data?.plafonPackage?.interestRate} - Tenor ${it.data?.plafonPackage?.maxTenorMonths}"
                    tvNominal.text = FormatRupiah.format(it.data?.plafonPackage?.amount ?: 0)


                    val plafonAmmount = it.data?.plafonPackage?.amount
                    val pinjamanAktif = it.data?.activeLoans?.firstOrNull()?.loanAmount ?: 0

                    if (plafonAmmount != null && pinjamanAktif != null) {
                        val persentase = pinjamanAktif.toDouble() / plafonAmmount.toDouble() * 100
                        if (persentase.isNaN()) {
                            progressBar.progress = 0
                        } else {
                            progressBar.progress = persentase.toInt()
                        }

                        tvPersentase.text = "Persentase: $persentase%"

                        plafondContainer.visibility = View.VISIBLE
                        plafondContainer.startAnimation(animBottom)
                    }

                    tvPinjamanAktif.text = "Pinjaman Aktif: Rp${FormatRupiah.format(pinjamanAktif.toInt())}"
                    tvPlafon.text = "Plafon: Rp${FormatRupiah.format(plafonAmmount?.toInt() ?: 0 )}"


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
                }, 1000)

            }
        }

        plafonViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            //       Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }

        return root
    }

}
