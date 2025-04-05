package com.example.project.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings")
    fun getAllBookings(): Flow<List<Booking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bookings: List<Booking>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(booking: Booking)

    @Update
    suspend fun update(booking: Booking)

    @Query("DELETE FROM bookings")
    suspend fun deleteAll()
}