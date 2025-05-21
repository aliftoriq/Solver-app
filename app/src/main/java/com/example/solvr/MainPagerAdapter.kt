package com.example.solvr

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.solvr.ui.home.HomeFragment
import com.example.solvr.ui.pengajuan.PengajuanFragment
import com.example.solvr.ui.plafond.PlafondFragment
import com.yourapp.ui.profile.ProfileFragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class MainPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    private val fragments = listOf(
        HomeFragment(),         // 0
        PengajuanFragment(),    // 1         // 2 -> Dummy
        PlafondFragment(),      // 3
        ProfileFragment()       // 4
    )

    override fun getItemCount() = fragments.size
    override fun createFragment(position: Int): Fragment = fragments[position]
}


