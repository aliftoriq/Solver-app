package com.yourapp.ui.profile

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.solvr.R
import com.example.solvr.ui.auth.LoginActivity
import com.example.solvr.ui.history.HistoryFragment
import com.example.solvr.ui.profile.EditProfileActivity
import android.app.Activity
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.example.solvr.ui.auth.ChangePasswordActivity
import com.example.solvr.ui.plafond.PlafondFragment
import com.example.solvr.utils.FileUtil
import com.example.yourapp.utils.SwitchAllertCustom

class ProfileFragment : Fragment() {

    private val profileViewModel: ProfileViewModel by viewModels()
    private lateinit var imageView: ImageView
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val animTop = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_top)
        val animBottom = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_bottom)
        val animLeft = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_left)
        val animRight = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_right)
        val animFadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        val animFadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)

        view.findViewById<FrameLayout>(R.id.headerContainer).startAnimation(animBottom)
        view.findViewById<ImageView>(R.id.profile_image).startAnimation(animBottom)
        view.findViewById<TextView>(R.id.profile_name).startAnimation(animBottom)
//        view.findViewById<TextView>(R.id.btnEditProfile).startAnimation(animFadeIn)
//        view.findViewById<TextView>(R.id.btnHistory).startAnimation(animFadeIn)
//        view.findViewById<TextView>(R.id.btnLogout).startAnimation(animBottom)
//        view.findViewById<TextView>(R.id.btnChangePassword).startAnimation(animBottom)

        val nameTextView = view.findViewById<TextView>(R.id.profile_name)
        val btnEditProfile = view.findViewById<LinearLayout>(R.id.btnEditProfile)
        val btnHistory = view.findViewById<TextView>(R.id.btnHistory)
        val btnFaq = view.findViewById<LinearLayout>(R.id.btnFaq)
        val btnPlafon = view.findViewById<LinearLayout>(R.id.btnPlafon)
        val btnLogout = view.findViewById<LinearLayout>(R.id.btnLogout)
        val btnChangePassword = view.findViewById<LinearLayout>(R.id.btnChangePassword)
        val btnLogin = view.findViewById<LinearLayout>(R.id.btnLogin)

        profileViewModel.isUserLoggedIn.observe(viewLifecycleOwner) { isLoggedIn ->
            if (!isLoggedIn) {

                btnEditProfile.visibility = View.GONE
                btnHistory.visibility = View.GONE
                btnFaq.visibility = View.GONE
                btnPlafon.visibility = View.GONE
                btnLogout.visibility = View.GONE
                btnChangePassword.visibility = View.GONE


            }
        }

        profileViewModel.userName.observe(viewLifecycleOwner) { userName ->
            nameTextView.text = userName
            btnLogin.visibility = View.GONE
        }

        profileViewModel.uploadResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Upload berhasil", Toast.LENGTH_SHORT).show()
            }
        }

        profileViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            val switchAlert = SwitchAllertCustom(requireContext())
            switchAlert.show(
                message = "Apakah Anda yakin untuk logout?",
                onYes = {
                    profileViewModel.logout()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                },
                onNo = {
                }
            )

        }

        btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        btnHistory.setOnClickListener {
            startActivity(Intent(requireContext(), HistoryFragment::class.java))
        }

        btnPlafon.setOnClickListener {
            startActivity(Intent(requireContext(), PlafondFragment::class.java))
        }

        btnFaq.setOnClickListener {

        }

        btnLogin.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        btnChangePassword.setOnClickListener {
            startActivity(Intent(requireContext(), ChangePasswordActivity::class.java))
        }

        imageView = view.findViewById(R.id.profile_image)
        imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data!!
            imageView.setImageURI(imageUri) // Tampilkan di ImageView

            val file = FileUtil.from(requireContext(), imageUri)
            profileViewModel.uploadProfileImage(requireContext(), file)
        }
    }
}


