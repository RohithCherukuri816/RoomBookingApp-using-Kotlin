package com.example.project.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "roomamenities")
data class RoomAmenitiesTable(
    @PrimaryKey
    @SerializedName("id")
    val amenityid: Int,

    @SerializedName("roomid")
    val roomid: Int,

    @SerializedName("amenityname")
    val amenityname: String,

    @SerializedName("isenabled")
    val isenabled: Boolean
)