package com.example.project.models

import java.util.Date

data class BookingsTable(
    val bookingid: Int,
    val roomid: Int,
    val userid: Int,
    val bookingdate: String, // Use Date instead of LocalDate
    val starttime: String, // Non-nullable
    val endtime: String?, // Nullable
    val purpose: String?, // Nullable
    val status: String = "PENDING",
    val attendees: Int? // Nullable
)