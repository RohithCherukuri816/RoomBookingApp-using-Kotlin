package com.example.project.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

@Entity(tableName = "bookings")
@TypeConverters(Converters::class)
data class Booking(
    @PrimaryKey(autoGenerate = true) val bookingid: Int = 0,
    val roomid: Int,
    val userid: Int,
    val bookingdate: String, // Use Date instead of LocalDate
    val starttime: String?, // nullable
    val endtime: String?, // Nullable
    val purpose: String?, // Nullable
    val status:String,
    val attendees: Int? // Nullable
)