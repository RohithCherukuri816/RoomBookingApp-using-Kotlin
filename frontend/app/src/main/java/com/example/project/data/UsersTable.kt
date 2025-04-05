package com.example.project.data

// UsersTable.kt
data class UsersTable(
    val userid: Int,
    val username: String,
    val email: String,
    val passwordhash: String,
    val roleid: Int,
    val createdat: String,
    val updatedat: String
)

// CreateUserRequest.kt
data class CreateUserRequest(
    val username: String,
    val email: String,
    val passwordHash: String,
    val roleId: Int
)