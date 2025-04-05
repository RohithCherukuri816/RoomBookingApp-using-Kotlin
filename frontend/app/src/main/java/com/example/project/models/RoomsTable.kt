package com.example.project.models

data class RoomsTable(
    val roomid: Int,
    val roomnumber: String,
    val block: String,
    val floor: Int,
    val capacity: Int,
    val status: String?, // Make nullable
    val category: String?, // Make nullable
    val subCategory: String?, // Make nullable
    val createdat: String,
    val updatedat: String
)