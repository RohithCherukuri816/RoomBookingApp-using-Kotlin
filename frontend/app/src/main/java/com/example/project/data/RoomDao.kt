package com.example.project.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms")
    fun getAllRooms(): Flow<List<RoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rooms: List<RoomEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(room: RoomEntity)

    @Update
    suspend fun update(room: RoomEntity)

    @Query("DELETE FROM rooms")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(room: RoomEntity)

}