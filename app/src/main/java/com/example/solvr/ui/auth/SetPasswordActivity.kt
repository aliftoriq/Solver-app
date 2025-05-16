package com.example.solvr.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
    }
}
