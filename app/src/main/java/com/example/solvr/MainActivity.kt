package com.example.solvr

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.solvr.network.FirebaseService
import com.example.solvr.ui.auth.SetPasswordActivity
import com.example.solvr.ui.home.HomeFragment
import com.example.solvr.ui.pengajuan.PengajuanFragment
import com.example.solvr.ui.plafond.PlafondFragment
import com.example.solvr.ui.user.UserActivity
import com.example.solvr.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.yourapp.ui.profile.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var viewPager: ViewPager2

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        ApiClient.init(applicationContext)
//        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
//
//        requestpermissionNotification()
//        getFCMToken()
//
//        val sessionManager = SessionManager(this)
//        if (sessionManager.getAuthToken() == null) {
//            sessionManager.saveIsPasswordSet(true)
//        }
//        val isPasswordSet = sessionManager.isPasswordSet()
//        if (!isPasswordSet) {
//            startActivity(Intent(this, SetPasswordActivity::class.java))
//            finish()
//            return
//        }
//
//        viewPager = findViewById(R.id.view_pager)
//        bottomNav = findViewById(R.id.bottom_nav)
//
//        viewPager.adapter = MainPagerAdapter(this)
//        viewPager.isUserInputEnabled = true
//
//        // BottomNav -> ViewPager
//        bottomNav.setOnItemSelectedListener {
//            viewPager.currentItem = when (it.itemId) {
//                R.id.nav_home -> 0
//                R.id.nav_pengajuan -> 1
//                R.id.nav_plafon -> 3
//                R.id.nav_profile -> 4
//                else -> 0
//            }
//            true
//        }
//
//
//        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
//            override fun onPageSelected(position: Int) {
//                when (position) {
//                    0 -> bottomNav.selectedItemId = R.id.nav_home
//                    1 -> bottomNav.selectedItemId = R.id.nav_pengajuan
//                    3 -> bottomNav.selectedItemId = R.id.nav_plafon
//                    4 -> bottomNav.selectedItemId = R.id.nav_profile
//                }
//            }
//        })
//
//    }


    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "FCM Token: $token")
            } else {
                Log.w("FCM", "Fetching FCM token failed", task.exception)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ApiClient.init(applicationContext)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

        // Set default fragment
        replaceFragment(HomeFragment())
        requestpermissionNotification()

        getFCMToken()

        val sessionManager = SessionManager(this)

        if(sessionManager.getAuthToken() == null){
            sessionManager.saveIsPasswordSet(true)
        }
        val isPasswordSet = sessionManager.isPasswordSet()
        if (!isPasswordSet) {
            val intent = Intent(this, SetPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }


        bottomNav = findViewById(R.id.bottom_nav)

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }

                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }

                R.id.nav_pengajuan -> {
                    replaceFragment(PengajuanFragment())
                    true
                }

                R.id.nav_plafon -> {
                    replaceFragment(PlafondFragment())
                    true
                }

                else -> false
            }
        }
        
    }


    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun requestpermissionNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Log.d("FCM", "Permission granted")
            } else {
                Log.e("FCM", "Permission denied")
            }
        }
    }
}

