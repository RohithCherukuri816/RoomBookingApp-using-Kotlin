package com.example.project.models

import java.time.LocalDateTime

data class AmenitiesTable(
    val amenityid: Int, // Changed from amenity_id
    val amenityname: String, // Changed from amenity_name
    val description: String?,
    val isactive: Boolean, // Changed from is_active
    val createdat: LocalDateTime, // Changed from created_at
    val updatedat: LocalDateTime // Changed from updated_at
)