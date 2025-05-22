package com.example.solvr.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.solvr.R
import com.example.solvr.models.LoanDTO

class HistoryAdapter(private var historyList: List<LoanDTO.DataItem>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    fun updateData(newHistoryList: List<LoanDTO.DataItem>) {
        historyList = newHistoryList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_loan_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(historyList[position])
    }

    override fun getItemCount(): Int = historyList.size

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvReviewStatus: TextView = itemView.findViewById(R.id.tvReviewStatus)
        private val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        private val tvJumlahPinjaman: TextView = itemView.findViewById(R.id.tvJumlahPinjaman)
        private val tvCicilan: TextView = itemView.findViewById(R.id.tvCicilan)
        private val tvTenor: TextView = itemView.findViewById(R.id.tvTenor)

        fun bind(item: LoanDTO.DataItem) {
            // Set default if null
            tvReviewStatus.text = when (item.status?.lowercase()) {
                "approved" -> "Disetujui"
                "rejected" -> "Ditolak"
                else -> "On Review"
            }

            tvTanggal.text = item.requestedAt?.substring(0, 10) ?: "-"

            val formattedAmount = item.loanAmount?.let { formatRupiah(it.toInt()) } ?: "-"
            tvJumlahPinjaman.text = "IDR $formattedAmount"

            val monthlyInstallment = item.monthlyPayment?.let { formatRupiah(it.toInt()) } ?: "-"
            tvCicilan.text = "IDR $monthlyInstallment"

            tvTenor.text = "${item.loanTenor ?: 0} Bulan"
        }

        private fun formatRupiah(amount: Int): String {
            return String.format("%,d", amount).replace(',', '.')
        }
    }

}
