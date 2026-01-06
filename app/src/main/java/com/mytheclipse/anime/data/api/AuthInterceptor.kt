package com.mytheclipse.anime.data.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import com.mytheclipse.anime.utils.SessionManager

class AuthInterceptor(context: Context) : Interceptor {
    
    private val sessionManager = SessionManager(context)
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Get the access token from session manager
        val token = sessionManager.getAccessToken()
        
        // If token is null or empty, proceed with original request
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }
        
        // Add Authorization header with Bearer token
        val authorizedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .build()
        
        return chain.proceed(authorizedRequest)
    }
}
