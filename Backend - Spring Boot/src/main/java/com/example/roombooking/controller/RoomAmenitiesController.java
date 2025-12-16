package com.example.roombooking.controller;

import com.example.roombooking.models.RoomAmenitiesTable;
import com.example.roombooking.repositories.RoomAmenitiesTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/admin")
public class RoomAmenitiesController {
	
	private static final Logger logger = LoggerFactory.getLogger(RoomAmenitiesController.class);

    @Autowired
    private RoomAmenitiesTableRepository roomAmenitiesTableRepository;

    // Get all room amenities
    @GetMapping("/room-amenities")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoomAmenitiesTable>> getAllRoomAmenities() {
        List<RoomAmenitiesTable> roomAmenities = roomAmenitiesTableRepository.findAll();
        return roomAmenities.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(roomAmenities);
    }

    // Get amenities by room ID
    @GetMapping("/room-amenities/by-room/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoomAmenitiesTable>> getAmenitiesByRoomId(@PathVariable int roomId) {
        List<RoomAmenitiesTable> amenities = roomAmenitiesTableRepository.findByRoomid(roomId);
        return amenities.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(amenities);
    }

    // Get all amenities grouped by room ID
    @GetMapping("/room-amenities/all-grouped")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<Integer, List<RoomAmenitiesTable>>> getAllAmenitiesGroupedByRoom() {
        List<RoomAmenitiesTable> amenities = roomAmenitiesTableRepository.findAll();

        // Group amenities by roomid
        Map<Integer, List<RoomAmenitiesTable>> amenitiesByRoom = amenities.stream()
            .collect(Collectors.groupingBy(RoomAmenitiesTable::getRoomid));

        return amenitiesByRoom.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(amenitiesByRoom);
    }

    // Create a new room amenity
    @PostMapping("/room-amenities")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomAmenitiesTable> createRoomAmenity(@RequestBody RoomAmenitiesTable roomAmenity) {
        RoomAmenitiesTable savedRoomAmenity = roomAmenitiesTableRepository.save(roomAmenity);
        return ResponseEntity.ok(savedRoomAmenity);
    }

    // Update an existing room amenity
    @PutMapping("/room-amenities/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomAmenitiesTable> updateRoomAmenity(
            @PathVariable int id,
            @RequestBody RoomAmenitiesTable roomAmenity
    ) {
        return roomAmenitiesTableRepository.findById(id)
                .map(existing -> {
                    existing.setIsenabled(roomAmenity.isIsenabled());
                    existing.setAmenityname(roomAmenity.getAmenityname());
                    existing.setRoomid(roomAmenity.getRoomid());
                    RoomAmenitiesTable updated = roomAmenitiesTableRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete a room amenity
    @DeleteMapping("/room-amenities/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRoomAmenity(@PathVariable int id) {
        if (roomAmenitiesTableRepository.existsById(id)) {
            roomAmenitiesTableRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}