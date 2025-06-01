package com.example.solvr.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.solvr.MainActivity
import com.example.solvr.R
import com.example.solvr.models.AuthDTO
import com.example.solvr.network.FirebaseAuthService
import com.example.solvr.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()

    private lateinit var firebaseAuthService: FirebaseAuthService
    private val RC_SIGN_IN = 1001

    private lateinit var progressBar: ProgressBar
    private lateinit var btnLogin: Button
    private lateinit var btnGoogleSignIn: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        firebaseAuthService = FirebaseAuthService(this)

        progressBar = findViewById(R.id.progressBar)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)

        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val btnRegisterPage = findViewById<TextView>(R.id.btnRegisterPage)
        val btnForgetPassword = findViewById<TextView>(R.id.btnForgetPassword)

        viewModel.loginResult.observe(this) { response ->
            progressBar.visibility = View.GONE
            btnLogin.isEnabled = true
            response?.let { handleLoginResponse(it.data?.token, it.data?.user) }
        }

        viewModel.loginFirebaseResult.observe(this) { response ->
            progressBar.visibility = View.GONE
            btnGoogleSignIn.isEnabled = true
            response?.let { handleLoginResponse(it.data?.token, it.data?.user) }
        }

        viewModel.fcmTokenResult.observe(this) { success ->
            if (success) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                //       Toast.makeText(this, "Gagal simpan token FCM", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.errorMessage.observe(this) { errorMsg ->
            progressBar.visibility = View.GONE
            btnLogin.isEnabled = true
            btnGoogleSignIn.isEnabled = true
            //       Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }

        btnRegisterPage.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnForgetPassword.setOnClickListener {
            startActivity(Intent(this, ForgetPasswordActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Format email tidak valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnLogin.isEnabled = false
            viewModel.loginUser(email, password)
        }

        btnGoogleSignIn.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            btnGoogleSignIn.isEnabled = false

            firebaseAuthService.getSignInIntent { intent ->
                startActivityForResult(intent, RC_SIGN_IN)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task =
                com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(
                    data
                )
            try {
                val account =
                    task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                val idToken = account.idToken

                if (idToken != null) {
                    firebaseAuthService.signInWithGoogle(idToken, {
                        val sessionManager = SessionManager(this)
                        sessionManager.saveUserName(account.displayName ?: "")
                        sessionManager.saveUserEmail(account.email ?: "")

                        // ONLY trigger loginFirebase AFTER Google sign in is successful
                        FirebaseAuth.getInstance().currentUser?.getIdToken(false)
                            ?.addOnCompleteListener { tokenTask ->
                                if (tokenTask.isSuccessful) {
                                    val firebaseIdToken = tokenTask.result?.token
                                    firebaseIdToken?.let { token ->
                                        viewModel.loginFirebase(token)
                                    }
                                } else {
                                    progressBar.visibility = View.GONE
                                    btnGoogleSignIn.isEnabled = true
                                    //       Toast.makeText(this, "Gagal mendapatkan Firebase token", Toast.LENGTH_SHORT).show()
                                }
                            }

                    }, { errorMsg ->
                        progressBar.visibility = View.GONE
                        btnGoogleSignIn.isEnabled = true
                        //       Toast.makeText(this, "Login Google gagal: $errorMsg", Toast.LENGTH_SHORT).show()
                    })
                } else {
                    progressBar.visibility = View.GONE
                    btnGoogleSignIn.isEnabled = true
                    //       Toast.makeText(this, "ID Token Google kosong", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                btnGoogleSignIn.isEnabled = true
                //       Toast.makeText(this, "Login Google error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleLoginResponse(token: String?, user: AuthDTO.User?) {
        if (user == null || token == null) {
            Toast.makeText(this, "Login gagal. Data tidak lengkap", Toast.LENGTH_SHORT).show()
            return
        }

        val sessionManager = SessionManager(this)
        sessionManager.saveAuthToken(token)
        sessionManager.saveUserName(user.name ?: "")
        sessionManager.saveUserEmail(user.username ?: "")

        val status = user.status?.lowercase()?.trim()
        Log.d("LoginDebug", "User status: $status")

        if (user.verified == false) {
            Toast.makeText(this, "Akun belum diverifikasi, silahkan cek email Anda", Toast.LENGTH_SHORT).show()
            return
        }

        val needsPassword = status == "needs_password"
        sessionManager.saveIsPasswordSet(!needsPassword)

        if (needsPassword) {
            startActivity(Intent(this, SetPasswordActivity::class.java))
        } else {
            //       Toast.makeText(this, "${user.name} Berhasil Login", Toast.LENGTH_SHORT).show()

            // Kirim FCM token ke server
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val fcmToken = task.result
                    Log.d("FCM_TokenDebug", "Token: $fcmToken")
                    viewModel.sendFcmToken(fcmToken)
                } else {
                    //       Toast.makeText(this, "Gagal ambil FCM token", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}
