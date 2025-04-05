package com.example.roombooking.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.roombooking.models.RoomAmenitiesTable;

import java.util.List;

public interface RoomAmenitiesTableRepository extends JpaRepository<RoomAmenitiesTable, Integer> {

    // Find all amenities for a specific room
    List<RoomAmenitiesTable> findByRoomid(int roomid);
}