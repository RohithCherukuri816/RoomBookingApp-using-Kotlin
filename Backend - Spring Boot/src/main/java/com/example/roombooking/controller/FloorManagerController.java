package com.example.roombooking.controller;

import com.example.roombooking.models.BookingsTable;
import com.example.roombooking.models.UsersTable;
import com.example.roombooking.repositories.BookingsTableRepository;
import com.example.roombooking.repositories.RoomsTableRepository;
import com.example.roombooking.repositories.UsersTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/floormanager")
public class FloorManagerController {

    @Autowired
    private BookingsTableRepository bookingsTableRepository;
    
    @Autowired
    private UsersTableRepository usersTableRepository;
    
    @Autowired
    private RoomsTableRepository roomsTableRepository;

    @PostMapping("/bookings")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'FLOORMANAGER')")
    public ResponseEntity<?> createBookingRequest(
        @RequestBody @Valid BookingsTable bookingRequest,
        Authentication authentication) {

        // 1. Verify authentication
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Full authentication required");
        }

        try {
            // 2. Get authenticated user details
            User principal = (User) authentication.getPrincipal();
            UsersTable user = usersTableRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "User not found in database"));

            // 3. Validate room exists
            if (!roomsTableRepository.existsById(bookingRequest.getRoomid())) {
                return ResponseEntity.badRequest().body("Room does not exist");
            }

            // 4. Set booking properties
            bookingRequest.setUserid(user.getUserid());
            bookingRequest.setStatus("Pending");
            bookingRequest.setCreatedat(LocalDateTime.now());
            
            // 5. Save booking
            BookingsTable savedBooking = bookingsTableRepository.save(bookingRequest);
            return ResponseEntity.ok(savedBooking);
            
        } catch (ClassCastException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid authentication principal");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error: " + e.getMessage());
        }
    }

    // [Keep all other methods exactly the same as in your original code]
    @GetMapping("/bookings")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLOORMANAGER')")
    public ResponseEntity<List<BookingsTable>> getAllBookingRequests() {
        List<BookingsTable> bookings = bookingsTableRepository.findAll();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/bookings/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLOORMANAGER')")
    public ResponseEntity<List<BookingsTable>> getPendingBookings() {
        List<BookingsTable> bookings = bookingsTableRepository.findByStatus("Pending");
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/bookings/room/{roomId}/date/{date}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLOORMANAGER')")
    public ResponseEntity<List<BookingsTable>> getBookingsByRoomAndDate(
            @PathVariable int roomId,
            @PathVariable Date date) {
        if (!roomsTableRepository.existsById(roomId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room does not exist");
        }
        
        List<BookingsTable> bookings = bookingsTableRepository.findByRoomidAndBookingdate(roomId, date);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/bookings/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLOORMANAGER')")
    public ResponseEntity<List<BookingsTable>> getBookingsByUser(@PathVariable int userId) {
        if (!usersTableRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User does not exist");
        }
        
        List<BookingsTable> bookings = bookingsTableRepository.findByUserid(userId);
        return ResponseEntity.ok(bookings);
    }

    @PatchMapping("/bookings/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLOORMANAGER')")
    public ResponseEntity<BookingsTable> updateBookingStatus(
            @PathVariable int id,
            @RequestParam String status) {
        
        if (!status.equalsIgnoreCase("Approved") && !status.equalsIgnoreCase("Rejected")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be 'Approved' or 'Rejected'");
        }

        return bookingsTableRepository.findById(id)
                .map(booking -> {
                    booking.setStatus(status);
                    booking.setUpdatedat(LocalDateTime.now());
                    return ResponseEntity.ok(bookingsTableRepository.save(booking));
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    }

    @GetMapping("/bookings/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLOORMANAGER')")
    public ResponseEntity<BookingsTable> getBookingById(@PathVariable int id) {
        return bookingsTableRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    }
}