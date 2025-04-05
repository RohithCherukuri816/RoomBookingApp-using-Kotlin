package com.example.project.repository

import android.content.Context
import android.util.Log
import com.example.project.data.AppDatabase
import com.example.project.data.Booking
import com.example.project.data.RoomEntity
import com.example.project.models.BookingsTable
import com.example.project.models.RoomsTable
import com.example.project.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RoomRepository(private val database: AppDatabase, private val scope: CoroutineScope, private val context: Context) {
    private val apiService = RetrofitClient.apiService
    private val roomDao = database.roomDao()
    private val bookingDao = database.bookingDao()

    sealed class Result<out T> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Error(val exception: Exception) : Result<Nothing>()
    }

    fun getAllRooms(): Flow<List<RoomEntity>> = roomDao.getAllRooms()

    suspend fun syncAdminRooms(): Result<List<RoomEntity>> = withContext(Dispatchers.IO) {
        try {
            val response: Response<List<RoomsTable>> = apiService.getAllAdminRooms().execute()
            if (response.isSuccessful) {
                val rooms = response.body()
                if (rooms != null) {
                    val entities = rooms.map { it.toRoomEntity() }
                    roomDao.deleteAll()
                    roomDao.insertAll(entities)
                    Result.Success(entities)
                } else {
                    Result.Error(Exception("Empty response"))
                }
            } else {
                Result.Error(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun addRoom(room: RoomEntity): Result<RoomEntity> = withContext(Dispatchers.IO) {
        try {
            val roomsTable = RoomsTable(
                roomid = 0,
                roomnumber = room.roomnumber,
                block = room.block,
                floor = room.floor,
                capacity = room.capacity,
                status = room.status,
                category = room.category,
                subCategory = room.subCategory,
                createdat = "",
                updatedat = ""
            )
            val response: Response<RoomsTable> = apiService.createRoom(roomsTable).execute()
            if (response.isSuccessful) {
                val savedRoom = response.body()
                if (savedRoom != null) {
                    val entity = savedRoom.toRoomEntity()
                    roomDao.insert(entity)
                    Result.Success(entity)
                } else {
                    Result.Error(Exception("Empty response body"))
                }
            } else {
                Result.Error(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // New updateRoom method
    suspend fun updateRoom(room: RoomEntity): Result<RoomEntity> = withContext(Dispatchers.IO) {
        try {
            val roomsTable = RoomsTable(
                roomid = room.id,
                roomnumber = room.roomnumber,
                block = room.block,
                floor = room.floor,
                capacity = room.capacity,
                status = room.status,
                category = room.category,
                subCategory = room.subCategory,
                createdat = formatDate(room.createdAt),
                updatedat = formatDate(Calendar.getInstance())
            )

            val authToken = "Bearer ${getAuthToken()}"
            Log.d("RoomRepository", "Updating room with ID: ${room.id}, Token: $authToken, Data: $roomsTable")
            val response: Response<RoomsTable> = apiService.updateRoom(authToken, room.id, roomsTable).execute()
            Log.d("RoomRepository", "Response code: ${response.code()}, Body: ${response.body()}")
            if (response.isSuccessful) {
                val updatedRoom = response.body()
                if (updatedRoom != null) {
                    val entity = updatedRoom.toRoomEntity()
                    roomDao.update(entity)
                    Result.Success(entity)
                } else {
                    Result.Error(Exception("Empty response body"))
                }
            } else {
                Log.e("RoomRepository", "API error: ${response.code()}, Error body: ${response.errorBody()?.string()}")
                Result.Error(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("RoomRepository", "Update failed", e)
            Result.Error(e)
        }
    }

    suspend fun loadAvailableRooms(): Result<List<RoomEntity>> = withContext(Dispatchers.IO) {
        try {
            val response: Response<List<RoomsTable>> = apiService.getAllAvailableRooms().execute()
            if (response.isSuccessful) {
                val rooms = response.body()
                if (rooms != null) {
                    val entities = rooms.map { it.toRoomEntity() }
                    Result.Success(entities)
                } else {
                    Result.Error(Exception("Empty response"))
                }
            } else {
                Result.Error(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun deleteRoom(room: RoomEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response: Response<Void> = apiService.deleteRoom(room.id).execute()
            if (response.isSuccessful) {
                roomDao.delete(room)
                Result.Success(Unit)
            } else {
                Result.Error(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun createBooking(booking: Booking): Result<Booking> = withContext(Dispatchers.IO) {
        try {
            val authToken = "Bearer ${getAuthToken()}"
            val bookingsTable = booking.toBookingsTable()
            val response: Response<BookingsTable> = apiService.createFloorManagerBooking(authToken, bookingsTable).execute()
            Log.d("RoomRepository", "Response code: ${response.code()}, Body: ${response.body()}")
            if (response.isSuccessful) {
                val savedBooking = response.body()
                if (savedBooking != null) {
                    val entity = savedBooking.toBookingEntity()
                    bookingDao.insert(entity)
                    Result.Success(entity)
                } else {
                    Result.Error(Exception("Empty response body"))
                }
            } else {
                Log.e("RoomRepository", "API error: ${response.errorBody()?.string()}")
                Result.Error(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("RoomRepository", "Failed to create booking", e)
            Result.Error(e)
        }
    }

    suspend fun getBookingsByDate(date: String): Result<List<Booking>> = withContext(Dispatchers.IO) {
        try {
            val response: Response<List<BookingsTable>> = apiService.getBookingsByDate(date).execute()
            if (response.isSuccessful) {
                val bookings = response.body()
                if (bookings != null) {
                    val bookingEntities = bookings.map { it.toBookingEntity() }
                    bookingDao.insertAll(bookingEntities)
                    Result.Success(bookingEntities)
                } else {
                    Result.Error(Exception("Empty response body"))
                }
            } else {
                Result.Error(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun updateBooking(booking: Booking): Result<Booking> = withContext(Dispatchers.IO) {
        try {
            val token = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                .getString("authToken", "") ?: ""
            val response = apiService.updateBookingStatus(
                id = booking.bookingid,
                status = booking.status,
                token = "Bearer $token"
            )
            when {
                !response.isSuccessful -> {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("Repository", "Update failed: ${response.code()} - $errorMsg")
                    Result.Error(Exception("Update failed: ${response.code()}"))
                }
                response.body() == null -> {
                    Result.Error(Exception("Empty response from server"))
                }
                else -> {
                    val updatedBooking = response.body()!!.toBookingEntity()
                    bookingDao.update(updatedBooking)
                    Result.Success(updatedBooking)
                }
            }
        } catch (e: Exception) {
            Log.e("Repository", "Update error", e)
            Result.Error(e)
        }
    }

    suspend fun getPendingBookings(): Result<List<Booking>> = withContext(Dispatchers.IO) {
        try {
            val response: Response<List<BookingsTable>> = apiService.getPendingBookings().execute()
            if (response.isSuccessful) {
                val bookings = response.body()?.map { it.toBookingEntity() } ?: emptyList()
                Result.Success(bookings)
            } else {
                Result.Error(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getUserBookings(userId: Int): Result<List<Booking>> = withContext(Dispatchers.IO) {
        try {
            val response: Response<List<BookingsTable>> = apiService.getUserBookings(userId).execute()
            if (response.isSuccessful) {
                val bookings = response.body()
                if (bookings != null) {
                    val bookingEntities = bookings.map { it.toBookingEntity() }
                    bookingDao.insertAll(bookingEntities)
                    Result.Success(bookingEntities)
                } else {
                    Result.Error(Exception("Empty response body"))
                }
            } else {
                Result.Error(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getBookingsByUserAndDate(userId: Int, date: String): Result<List<Booking>> = withContext(Dispatchers.IO) {
        try {
            val response: Response<List<BookingsTable>> = apiService.getBookingsByUserAndDate(userId, date).execute()
            if (response.isSuccessful) {
                val bookings = response.body()
                if (bookings != null) {
                    val bookingEntities = bookings.map { it.toBookingEntity() }
                    bookingDao.insertAll(bookingEntities)
                    Result.Success(bookingEntities)
                } else {
                    Result.Error(Exception("Empty response body"))
                }
            } else {
                Result.Error(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun getAuthToken(): String {
        return context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            .getString("authToken", "") ?: ""
    }

    private fun RoomsTable.toRoomEntity(): RoomEntity {
        require(roomnumber.isNotEmpty()) { "Room number cannot be empty or null" }
        require(block.isNotEmpty()) { "Block cannot be empty or null" }
        return RoomEntity(
            id = roomid,
            roomnumber = roomnumber,
            block = block,
            floor = floor,
            capacity = capacity,
            status = status,
            category = category,
            subCategory = subCategory,
            createdAt = parseDate(createdat),
            updatedAt = parseDate(updatedat)
        )
    }

    private fun BookingsTable.toBookingEntity() = Booking(
        bookingid = bookingid,
        roomid = roomid,
        userid = userid,
        bookingdate = bookingdate,
        starttime = starttime,
        endtime = endtime,
        purpose = purpose,
        status = status ?: "Pending",
        attendees = attendees
    )

    private fun Booking.toBookingsTable() = BookingsTable(
        bookingid = bookingid,
        userid = userid,
        roomid = roomid,
        bookingdate = bookingdate,
        starttime = starttime ?: "",
        endtime = endtime,
        purpose = purpose,
        status = status ?: "Pending",
        attendees = attendees
    )

    private fun parseDate(dateString: String?): Calendar? {
        return try {
            dateString?.let {
                Calendar.getInstance().apply {
                    time = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(it)!!
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun formatDate(calendar: Calendar?): String {
        return calendar?.let {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(it.time)
        } ?: ""
    }
}