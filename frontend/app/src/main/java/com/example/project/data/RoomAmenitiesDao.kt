package com.example.project.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.project.models.RoomAmenitiesTable
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomAmenitiesDao {

    @Insert
    suspend fun insert(roomAmenity: RoomAmenitiesTable)

    @Query("SELECT * FROM roomamenities WHERE roomid = :roomId")
    suspend fun getAmenitiesForRoom(roomId: Int): List<RoomAmenitiesTable>

    @Query("UPDATE roomamenities SET isenabled = :isEnabled WHERE id = :id")
    suspend fun updateAmenityStatus(id: Int, isEnabled: Boolean)

    @Query("DELETE FROM roomamenities")
    suspend fun deleteAll()
}