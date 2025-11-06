package com.example.prm_assignment.data.remote

import com.example.prm_assignment.data.model.LoginRequest
import com.example.prm_assignment.data.model.LoginResponse
import com.example.prm_assignment.data.model.ProfileResponse
import com.example.prm_assignment.data.model.RegisterRequest
import com.example.prm_assignment.data.model.VerifyOtpRequest
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Header

interface AuthApi {
    @POST("auth/login-by-password")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/register-customer")
    suspend fun register(@Body request: RegisterRequest): LoginResponse

    @POST("auth/register-customer/verify")
    suspend fun verifyRegister(@Body request: VerifyOtpRequest): LoginResponse

    @GET("auth/profile")
    suspend fun getProfile(@Header("Authorization") token: String): ProfileResponse

    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") token: String): LoginResponse
}
