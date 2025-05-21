package com.example.solvr.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.solvr.models.AuthDTO
import com.example.solvr.models.AuthDTO.LoginRequest
import com.example.solvr.models.AuthDTO.LoginResponse
import com.example.solvr.repository.AuthRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _loginResult = MutableLiveData<LoginResponse?>()
    val loginResult: LiveData<LoginResponse?> = _loginResult

    private val _loginFirebaseResult = MutableLiveData<LoginResponse?>()
    val loginFirebaseResult: LiveData<LoginResponse?> = _loginFirebaseResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _fcmTokenResult = MutableLiveData<Boolean>()
    val fcmTokenResult: LiveData<Boolean> = _fcmTokenResult

    private val _setPasswordResult = MutableLiveData<Boolean>()
    val setPasswordResult: LiveData<Boolean> = _setPasswordResult


    fun sendFcmToken(token: String) {
        repository.saveFirebaseToken(token).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                _fcmTokenResult.postValue(response.isSuccessful)
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                _fcmTokenResult.postValue(false)
            }
        })
    }

    fun loginUser(email: String, password: String) {
        val request = LoginRequest(email, password)

        ApiClient.authService.login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    _loginResult.postValue(response.body())
                } else {
                    _errorMessage.postValue("Login gagal: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _errorMessage.postValue("Error: ${t.message}")
            }
        })
    }

    fun loginFirebase(token: String) {
        val request = AuthDTO.LoginFirebaseRequest(token)

        ApiClient.authService.loginFirebase(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                if (response.isSuccessful) {
                    _loginFirebaseResult.postValue(response.body())
                } else {
                    _errorMessage.postValue("Login gagal: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse?>, t: Throwable) {
                _errorMessage.postValue("Error: ${t.message}")
            }
        })
    }

    fun setPassword(newPassword: String) {
        val request = AuthDTO.SetPasswordRequest(newPassword)

        ApiClient.authService.setPassword(request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    _setPasswordResult.postValue(response.isSuccessful)
                }
                else{
                    val message = when (response.code()) {
                        400 -> "Bad request"
                        else -> "Registrasi gagal (Error ${response.code()})"
                    }
                    _errorMessage.value = message
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                _setPasswordResult.postValue(false)
                _errorMessage.postValue("Error: ${t.message}")
            }
        })
    }
}
