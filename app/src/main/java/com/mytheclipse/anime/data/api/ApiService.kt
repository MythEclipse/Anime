package com.mytheclipse.anime.data.api

import com.mytheclipse.anime.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // ==================== AUTH ENDPOINTS ====================
    
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
    
    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshRequest): Response<RefreshResponse>
    
    @POST("api/auth/logout")
    suspend fun logout(@Body request: LogoutRequest): Response<LogoutResponse>
    
    @GET("api/auth/me")
    suspend fun getCurrentUser(): Response<UserResponse>
    
    // ==================== ANIME ENDPOINTS ====================
    
    @GET("api/anime")
    suspend fun getAnimeHome(): Response<AnimeResponse>
    
    @GET("api/anime/ongoing-anime/{page}")
    suspend fun getOngoingAnime(@Path("page") page: Int = 1): Response<OngoingAnimeResponse>
    
    @GET("api/anime/complete-anime/{page}")
    suspend fun getCompleteAnime(@Path("page") page: Int = 1): Response<OngoingAnimeResponse>
    
    @GET("api/anime/detail/{slug}")
    suspend fun getAnimeDetail(@Path("slug") slug: String): Response<DetailResponse>
    
    @GET("api/anime/full/{slug}")
    suspend fun getAnimeFull(@Path("slug") slug: String): Response<FullResponse>
    
    @GET("api/anime/search")
    suspend fun searchAnime(@Query("q") query: String): Response<SearchResponse>
}
