package com.example.project.models

import com.google.gson.annotations.SerializedName

data class AuditLogsTable(
    @SerializedName("logid") val logId: Int,
    @SerializedName("bookingid") val bookingId: Int,
    @SerializedName("changedby") val changedBy: Int,
    @SerializedName("oldstatus") val oldStatus: String,
    @SerializedName("newstatus") val newStatus: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("comments") val comments: String
)