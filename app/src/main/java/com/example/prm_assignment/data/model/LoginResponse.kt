package com.example.prm_assignment.data.model

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData? = null
)

data class LoginData(
    val accessToken: String,
    val refreshToken: String?,
    val user: UserInfo?
)

data class UserInfo(
    val id: String,
    val email: String,
    val fullName: String?,
    val phoneNumber: String?,
    val avatar: String?
)

