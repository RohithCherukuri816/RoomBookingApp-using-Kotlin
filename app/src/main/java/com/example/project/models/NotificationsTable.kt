package com.example.project.models

import com.google.gson.annotations.SerializedName

data class NotificationsTable(
    @SerializedName("notificationid") val notificationId: Int,
    @SerializedName("userid") val userId: Int,
    @SerializedName("message") val message: String,
    @SerializedName("isread") val isRead: Boolean,
    @SerializedName("createdat") val createdAt: String
)