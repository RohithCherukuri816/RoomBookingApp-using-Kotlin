package com.example.project.models

import com.google.gson.annotations.SerializedName

data class UsersTable(
    @SerializedName("userid") val userid: Int = 0, // Default value for new users
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("passwordhash") val passwordHash: String,
    @SerializedName("roleid") val roleId: Int,
    @SerializedName("createdat") val createdAt: String = "", // Will be set by server
    @SerializedName("updatedat") val updatedAt: String = "" // Will be set by server
)

// Create a separate DTO for user creation without server-generated fields
data class CreateUserRequest(
    val username: String,
    val email: String,
    val passwordHash: String,
    val roleId: Int
)