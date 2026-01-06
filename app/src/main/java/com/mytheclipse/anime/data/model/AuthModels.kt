package com.mytheclipse.anime.data.model

import com.google.gson.annotations.SerializedName

// ==================== AUTH MODELS ====================

data class LoginRequest(
    val email: String,
    val password: String,
    @SerializedName("remember_me") val rememberMe: Boolean = false
)

data class LoginResponse(
    val user: UserResponse,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Long
)

data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("password_confirmation") val passwordConfirmation: String? = null
)

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val user: UserResponse,
    @SerializedName("verification_token") val verificationToken: String? = null
)

data class RefreshRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

data class RefreshResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Long
)

data class LogoutRequest(
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("logout_all") val logoutAll: Boolean = false
)

data class LogoutResponse(
    val success: Boolean,
    val message: String
)

data class UserResponse(
    val id: String,
    val email: String? = null,
    val name: String? = null,
    val image: String? = null,
    @SerializedName("email_verified") val emailVerified: Boolean,
    val role: String
)
