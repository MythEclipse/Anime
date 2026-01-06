package com.mytheclipse.anime.data.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    private const val BASE_URL = "https://ws.asepharyana.tech/"
    private const val TIMEOUT_SECONDS = 30L
    
    @Volatile
    private var retrofit: Retrofit? = null
    
    @Volatile
    private var apiService: ApiService? = null
    
    private fun getLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    private fun getOkHttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .addInterceptor(getLoggingInterceptor())
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }
    
    private fun getRetrofit(context: Context): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(getOkHttpClient(context))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .also { retrofit = it }
        }
    }
    
    fun getApiService(context: Context): ApiService {
        return apiService ?: synchronized(this) {
            apiService ?: getRetrofit(context).create(ApiService::class.java)
                .also { apiService = it }
        }
    }
    
    // Reset client when user logs out (to clear old token interceptor)
    fun resetClient() {
        synchronized(this) {
            retrofit = null
            apiService = null
        }
    }
}
