package com.example.data.api

import com.example.BuildConfig
import com.example.data.model.UserAccount
import com.example.data.model.Warga
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface SupabaseApi {
    @GET("user_accounts")
    suspend fun getAllUsers(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String
    ): List<UserAccount>

    @POST("user_accounts?on_conflict=username")
    suspend fun upsertUsers(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @Body users: List<UserAccount>
    )

    @DELETE("user_accounts")
    suspend fun deleteUser(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("username") query: String // format "eq.username"
    )

    @GET("warga_list")
    suspend fun getAllWarga(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String
    ): List<Warga>

    @POST("warga_list?on_conflict=nik")
    suspend fun upsertWarga(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @Body wargaList: List<Warga>
    )

    @DELETE("warga_list")
    suspend fun deleteWarga(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("nik") query: String // format "eq.nik"
    )
}

object SupabaseClient {
    private val BASE_URL = BuildConfig.SUPABASE_URL
    const val API_KEY = BuildConfig.SUPABASE_KEY

    val authHeader = "Bearer $API_KEY"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: SupabaseApi by lazy {
        val baseUrlWithSlash = if (BASE_URL.endsWith("/")) BASE_URL else "$BASE_URL/"
        Retrofit.Builder()
            .baseUrl(baseUrlWithSlash)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SupabaseApi::class.java)
    }
}
