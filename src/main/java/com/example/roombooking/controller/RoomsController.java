package com.example.roombooking.controller;

import com.example.roombooking.models.RoomsTable;
import com.example.roombooking.repositories.RoomsTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RoomsController {

    @Autowired
    private RoomsTableRepository roomsTableRepository;

    // Admin-only: Get all rooms (for admin management)
    @GetMapping("/admin/rooms")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoomsTable>> getAllAdminRooms() {
        List<RoomsTable> rooms = roomsTableRepository.findAll();
        return ResponseEntity.ok(rooms);
    }

    // Admin-only: Create a new room
    @PostMapping("/admin/rooms")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomsTable> createRoom(@RequestBody RoomsTable room) {
        room.setRoomid(0); // Ensure ID is auto-generated
        RoomsTable savedRoom = roomsTableRepository.save(room);
        return ResponseEntity.ok(savedRoom);
    }

    // Admin-only: Update a room
    @PutMapping("/admin/rooms/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomsTable> updateRoom(@PathVariable int id, @RequestBody RoomsTable room) {
        return roomsTableRepository.findById(id)
                .map(existing -> {
                    existing.setRoomnumber(room.getRoomnumber());
                    existing.setBlock(room.getBlock());
                    existing.setFloor(room.getFloor());
                    existing.setCapacity(room.getCapacity());
                    existing.setStatus(room.getStatus());
                    existing.setAmenities(room.getAmenities());
                    RoomsTable updated = roomsTableRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Admin-only: Delete a room
    @DeleteMapping("/admin/rooms/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable int id) {
        if (roomsTableRepository.existsById(id)) {
            roomsTableRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // User-accessible: Get all available rooms
    @GetMapping("/rooms")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<RoomsTable>> getAllAvailableRooms() {
        List<RoomsTable> rooms = roomsTableRepository.findByStatus("Available");
        return ResponseEntity.ok(rooms);
    }
}