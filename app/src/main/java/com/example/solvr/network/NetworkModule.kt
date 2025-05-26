//package com.example.solvr.network
//
//import android.util.Log
//import com.example.solvr.network.PlafonService
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.components.SingletonComponent
//import okhttp3.OkHttpClient
//import retrofit2.Call
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import java.lang.reflect.Method
//import java.lang.reflect.Proxy
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class)
//object NetworkModule {
//
//    private const val BASE_URL = "http://34.45.191.98/be/api/v1/"
//    private const val TAG = "API_LOGGER"
//
//    @Provides
//    @Singleton
//    fun provideOkHttpClient(): OkHttpClient {
//        return OkHttpClient.Builder()
//            .addInterceptor { chain ->
//                val request = chain.request()
//                Log.d(TAG, "Request: ${request.method} ${request.url}")
//                Log.d(TAG, "Headers: ${request.headers}")
//                request.body?.let { Log.d(TAG, "Body: $it") }
//
//                val response = chain.proceed(request)
//
//                Log.d(TAG, "Response: ${response.code} ${response.message}")
//                response
//            }
//            .build()
//    }
//
//
//    @Provides
//    @Singleton
//    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
//        return Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .client(okHttpClient)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//    }
//
//    @Provides
//    @Singleton
//    fun providePlafonService(retrofit: Retrofit): PlafonService {
//        return retrofit.create(PlafonService::class.java)
//    }
//
//
//    private fun logRequest(method: Method, args: Array<Any>?) {
//        Log.d(TAG, "===== REQUEST: ${method.name} =====")
//        args?.forEachIndexed { index, arg ->
//            Log.d(TAG, "Arg $index: ${arg.toString()}")
//            // Tambahkan log body jika Call<>
//            if (arg is Call<*>) {
//                val request = arg.request()
//                Log.d(TAG, "URL: ${request.url}")
//                Log.d(TAG, "Method: ${request.method}")
//                Log.d(TAG, "Headers: ${request.headers}")
//                val body = request.body
//                Log.d(TAG, "Body: $body")
//            }
//        }
//    }
//
//    private fun logResponse(method: Method, response: Any?) {
//        Log.d(TAG, "===== RESPONSE: ${method.name} =====")
//        when (response) {
//            is Call<*> -> {
//                val request = response.request()
//                Log.d(TAG, "Response Call URL: ${request.url}")
//                Log.d(TAG, "Method: ${request.method}")
//            }
//            else -> {
//                Log.d(TAG, "Response: ${response?.toString()}")
//            }
//        }
//    }
//}
