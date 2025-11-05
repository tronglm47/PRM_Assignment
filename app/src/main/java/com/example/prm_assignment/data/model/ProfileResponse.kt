package com.example.prm_assignment.data.model

data class ProfileResponse(
    val success: Boolean,
    val message: String,
    val data: UserProfile? = null
)

data class UserProfile(
    val id: String,
    val email: String,
    val fullName: String?,
    val phoneNumber: String?,
    val avatar: String?,
    val createdAt: String?,
    val updatedAt: String?
)

