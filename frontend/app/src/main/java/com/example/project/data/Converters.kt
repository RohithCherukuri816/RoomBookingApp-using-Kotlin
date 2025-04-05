package com.example.project.data

import androidx.room.TypeConverter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Converters {

    // Calendar converters (for RoomEntity's createdAt and updatedAt)
    @TypeConverter
    fun fromCalendar(calendar: Calendar?): Long? {
        return calendar?.timeInMillis
    }

    @TypeConverter
    fun toCalendar(millis: Long?): Calendar? {
        return millis?.let {
            Calendar.getInstance().apply {
                timeInMillis = it
            }
        }
    }

    // String to Date converter (for Booking's bookingdate parsing to Date internally if needed)
    @TypeConverter
    fun fromStringToDate(dateString: String?): Long? {
        return dateString?.let {
            try {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(it)?.time
            } catch (e: ParseException) {
                null // Return null if parsing fails
            }
        }
    }

    // Date to String converter (for Booking's bookingdate formatting from internal Date if needed)
    @TypeConverter
    fun fromTimestampToString(timestamp: Long?): String? {
        return timestamp?.let {
            try {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
            } catch (e: IllegalArgumentException) {
                null // Return null if formatting fails
            }
        }
    }
}