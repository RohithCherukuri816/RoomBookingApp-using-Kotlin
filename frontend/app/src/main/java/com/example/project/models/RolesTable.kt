package com.example.project.models

import com.google.gson.annotations.SerializedName

data class RolesTable(
    @SerializedName("roleid") val roleId: Int,
    @SerializedName("rolename") val roleName: String,
    @SerializedName("description") val description: String
)