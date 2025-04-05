package com.example.project.data

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val roomnumber: String,
    val block: String,
    val floor: Int,
    val capacity: Int,
    val status: String? = null,
    val category: String?,
    val subCategory: String?,
    val createdAt: Calendar? = null,
    val updatedAt: Calendar? = null
) : Parcelable {

    // Parcelable implementation
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readSerializable() as Calendar?,
        parcel.readSerializable() as Calendar?
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(roomnumber)
        parcel.writeString(block)
        parcel.writeInt(floor)
        parcel.writeInt(capacity)
        parcel.writeString(status)
        parcel.writeString(category)
        parcel.writeString(subCategory)
        parcel.writeSerializable(createdAt)
        parcel.writeSerializable(updatedAt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RoomEntity> {
        override fun createFromParcel(parcel: Parcel): RoomEntity {
            return RoomEntity(parcel)
        }

        override fun newArray(size: Int): Array<RoomEntity?> {
            return arrayOfNulls(size)
        }
    }
}