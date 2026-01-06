package com.mytheclipse.anime.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.mytheclipse.anime.data.model.UserResponse

class SessionManager(context: Context) {
    
    companion object {
        private const val PREF_NAME = "anime_app_session"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER = "user"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_TOKEN_EXPIRES_AT = "token_expires_at"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    // ==================== TOKEN MANAGEMENT ====================
    
    fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Long) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putLong(KEY_TOKEN_EXPIRES_AT, System.currentTimeMillis() + (expiresIn * 1000))
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }
    
    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }
    
    fun isTokenExpired(): Boolean {
        val expiresAt = prefs.getLong(KEY_TOKEN_EXPIRES_AT, 0)
        return System.currentTimeMillis() >= expiresAt
    }
    
    // ==================== USER MANAGEMENT ====================
    
    fun saveUser(user: UserResponse) {
        val userJson = gson.toJson(user)
        prefs.edit().putString(KEY_USER, userJson).apply()
    }
    
    fun getUser(): UserResponse? {
        val userJson = prefs.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(userJson, UserResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    // ==================== SESSION STATUS ====================
    
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && !getAccessToken().isNullOrEmpty()
    }
    
    fun clearSession() {
        prefs.edit().clear().apply()
    }
    
    // ==================== CONVENIENCE METHODS ====================
    
    fun saveLoginData(accessToken: String, refreshToken: String, expiresIn: Long, user: UserResponse) {
        saveTokens(accessToken, refreshToken, expiresIn)
        saveUser(user)
    }
}
