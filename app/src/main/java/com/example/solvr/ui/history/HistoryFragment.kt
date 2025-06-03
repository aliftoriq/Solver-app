package com.example.solvr.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.solvr.R

class HistoryFragment : Fragment() {

    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val animTop = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_top)
        val animBottom = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_bottom)
        val animLeft = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_left)
        val animRight = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_right)
        val animFadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        val animFadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)


        recyclerView = view.findViewById(R.id.historyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = HistoryAdapter(emptyList())
        recyclerView.adapter = adapter

        val tvTidakAdaData = view.findViewById<TextView>(R.id.tvTidakAdaData)

        viewModel.fetchHistory()

        viewModel.loanHistory.observe(viewLifecycleOwner) { history ->
            if (history.isNotEmpty()) {
                recyclerView.startAnimation(animBottom)
                adapter.updateData(history)
            } else {

                tvTidakAdaData.visibility = View.VISIBLE
                tvTidakAdaData.startAnimation(animBottom)
                showError("No loan history available")
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            showError(errorMessage)
            tvTidakAdaData.startAnimation(animBottom)
            showError("No loan history available")
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
