package com.example.project.network

import com.example.project.data.RoomEntity
import com.example.project.models.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Room Amenities Endpoints (Updated)
    @GET("api/admin/room-amenities")
    fun getAllRoomAmenities(): Call<List<RoomAmenitiesTable>>

    @GET("api/admin/room-amenities/by-room/{roomId}")
    fun getRoomAmenitiesByRoomId(@Path("roomId") roomId: Int): Call<List<RoomAmenitiesTable>>

    @POST("api/admin/room-amenities")
    fun createRoomAmenity(@Body roomAmenity: RoomAmenitiesTable): Call<RoomAmenitiesTable>

    @PUT("api/admin/room-amenities/{id}")
    fun updateRoomAmenity(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body roomAmenity: RoomAmenitiesTable
    ): Call<RoomAmenitiesTable>

    @DELETE("api/admin/room-amenities/{id}")
    fun deleteRoomAmenity(@Path("id") id: Int): Call<Void>

    // Rooms Endpoints (Unchanged)
    @GET("api/admin/rooms")
    fun getAllAdminRooms(): Call<List<RoomsTable>>

    @POST("api/admin/rooms")
    fun createRoom(@Body room: RoomsTable): Call<RoomsTable>

//    @PUT("api/admin/rooms/{id}")
//    fun updateRoom(@Path("id") id: Int, @Body room: RoomsTable): Call<RoomsTable>

    @DELETE("api/admin/rooms/{id}")
    fun deleteRoom(@Path("id") id: Int): Call<Void>

    // Bookings Endpoints (Unchanged)
    @GET("api/admin/bookings")
    fun getAllBookings(): Call<List<BookingsTable>>

    @POST("api/bookings")
    fun createBooking(@Body booking: BookingsTable): Call<BookingsTable>

    @PUT("api/admin/bookings/{id}")
    fun updateBooking(
        @Path("id") id: Int,
        @Body booking: BookingsTable,
        @Header("Authorization") token: String
    ): Call<BookingsTable>

    @PUT("api/admin/rooms/{id}")
    fun updateRoom(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body room: RoomsTable
    ): Call<RoomsTable>

    @DELETE("api/admin/bookings/{id}")
    fun deleteBooking(@Path("id") id: Int): Call<Void>

    @GET("api/admin/bookings/{date}")
    fun getBookingsByDate(@Path("date") date: String): Call<List<BookingsTable>>

    @GET("api/admin/bookings/user/{userId}/{date}")
    fun getBookingsByUserAndDate(
        @Path("userId") userId: Int,
        @Path("date") date: String
    ): Call<List<BookingsTable>>

    @GET("api/bookings/user/{userId}")
    fun getUserBookings(@Path("userId") userId: Int): Call<List<BookingsTable>>

    // Users Endpoints (Unchanged)
    @GET("api/admin/users")
    fun getAllUsers(): Call<List<UsersTable>>

    @GET("api/rooms")
    fun getAllAvailableRooms(): Call<List<RoomsTable>>

    @PUT("rooms/{id}")
    fun updateRoom(@Path("id") id: Int, @Body room: RoomsTable): Call<RoomsTable>

    @POST("api/admin/users")
    fun createUser(@Body user: CreateUserRequest): Call<UsersTable>

    @PUT("api/admin/users/{id}")
    fun updateUser(@Path("id") id: Int, @Body user: UsersTable): Call<UsersTable>

    @DELETE("api/admin/users/{id}")
    fun deleteUser(@Path("id") id: Int): Call<Void>

    @GET("api/user/profile")
    fun getUserProfile(): Call<UsersTable>

    @PUT("api/user/profile")
    fun updateUserProfile(@Body user: UsersTable): Call<UsersTable>

    // Roles Endpoints (Unchanged)
    @GET("api/admin/roles")
    fun getAllRoles(): Call<List<RolesTable>>

    @GET("api/floormanager/bookings")
    suspend fun getAllFloorManagerBookings(): Response<List<BookingsTable>>

    @GET("api/floormanager/bookings/room/{roomId}/date/{date}")
    suspend fun getBookingsByRoomAndDate(
        @Path("roomId") roomId: Int,
        @Path("date") date: String
    ): Response<List<BookingsTable>>

    @GET("api/floormanager/bookings/user/{userId}")
    suspend fun getBookingsByUser(@Path("userId") userId: Int): Response<List<BookingsTable>>

    @PATCH("api/floormanager/bookings/{id}/status")
    suspend fun updateBookingStatus(
        @Path("id") id: Int,
        @Query("status") status: String,
        @Header("Authorization") token: String
    ): Response<BookingsTable>

    @GET("api/floormanager/bookings/{id}")
    suspend fun getFloorManagerBookingById(@Path("id") id: Int): Response<BookingsTable>

    @GET("api/floormanager/bookings/pending")
    fun getPendingBookings(): Call<List<BookingsTable>>

    @POST("api/floormanager/bookings")
    fun createFloorManagerBooking(
        @Header("Authorization") token: String,
        @Body booking: BookingsTable
    ): Call<BookingsTable>
}



