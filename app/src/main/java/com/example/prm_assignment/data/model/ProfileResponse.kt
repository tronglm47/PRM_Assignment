package com.example.prm_assignment.data.model

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    val success: Boolean,
    val message: String,
    val data: ProfileData? = null
)

data class ProfileData(
    @SerializedName("_id")
    val id: String,
    val userId: UserId,
    val customerName: String?,
    val address: String?,
    val dateOfBirth: String?,
    val deviceTokens: List<String>?,
    val createdAt: String?,
    val updatedAt: String?
)

data class UserId(
    @SerializedName("_id")
    val id: String,
    val phone: String?,
    val role: String?,
    val email: String?
)

