package com.example.solvr.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.solvr.MainActivity
import com.example.solvr.R
import com.example.solvr.utils.SessionManager
import com.google.android.material.textfield.TextInputEditText

class SetPasswordActivity : AppCompatActivity() {

    private lateinit var edtPassword: TextInputEditText
    private lateinit var edtConfirmPassword: TextInputEditText
    private lateinit var btnSavePassword: Button
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_password)

        val animBottom = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom)
        findViewById<FrameLayout>(R.id.headerContainer).startAnimation(animBottom)

        edtPassword = findViewById(R.id.edtPassword)
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword)
        btnSavePassword = findViewById(R.id.btnSavePassword)

        btnSavePassword.setOnClickListener {
            val pass = edtPassword.text.toString()
            val confirmPass = edtConfirmPassword.text.toString()

            if (pass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Password harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != confirmPass) {
                Toast.makeText(this, "Password dan konfirmasi harus sama", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.setPassword(pass)
        }

        viewModel.setPasswordResult.observe(this) { success ->
            if (success) {
                val sessionManager = SessionManager(this)
                sessionManager.saveIsPasswordSet(true)
                Toast.makeText(this, "Password berhasil disimpan", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Gagal menyimpan password", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.errorMessage.observe(this) { errorMsg ->
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Reset session
        val sessionManager = SessionManager(this)
        sessionManager.clearSession()

        // Kembali ke LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


}
