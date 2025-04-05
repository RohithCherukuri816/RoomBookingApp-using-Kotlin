package com.example.roombooking.controller;

import com.example.roombooking.models.BookingsTable;
import com.example.roombooking.repositories.BookingsTableRepository;
import com.example.roombooking.repositories.RoomsTableRepository;
import com.example.roombooking.repositories.UsersTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Date;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BookingsController {

    private final BookingsTableRepository bookingsTableRepository;
    private final UsersTableRepository usersTableRepository;
    private final RoomsTableRepository roomsTableRepository;

    @Autowired
    public BookingsController(BookingsTableRepository bookingsTableRepository,
                              UsersTableRepository usersTableRepository,
                              RoomsTableRepository roomsTableRepository) {
        this.bookingsTableRepository = bookingsTableRepository;
        this.usersTableRepository = usersTableRepository;
        this.roomsTableRepository = roomsTableRepository;
    }

    // User-accessible: Create a booking
    @PostMapping("/bookings")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingsTable booking) {
    	System.out.println("[DEBUG] Booking Request - User ID: " + booking.getUserid());
        System.out.println("[DEBUG] Booking Request - Room ID: " + booking.getRoomid());
        // Check if user exists
        if (!usersTableRepository.existsById(booking.getUserid())) {
        	System.out.println("[ERROR] User ID " + booking.getUserid() + " not found!");
            return ResponseEntity.badRequest().body("User does not exist");
            //return ResponseEntity.badRequest().body("User does not exist");
        }
        
        
        
        // Check if room exists
        if (!roomsTableRepository.existsById(booking.getRoomid())) {
            return ResponseEntity.badRequest().body("Room does not exist");
        }

        BookingsTable savedBooking = bookingsTableRepository.save(booking);
        return ResponseEntity.ok(savedBooking);
    }

    // User-accessible: Get bookings for a specific user
    @GetMapping("/bookings/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<BookingsTable>> getUserBookings(@PathVariable int userId) {
        List<BookingsTable> bookings = bookingsTableRepository.findByUserid(userId);
        return ResponseEntity.ok(bookings);
    }

    // Admin-only: Get all bookings (for admin management)
    @GetMapping("/admin/bookings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingsTable>> getAllBookings() {
        List<BookingsTable> bookings = bookingsTableRepository.findAll();
        return ResponseEntity.ok(bookings);
    }

    // Admin-only: Update a booking
    @PutMapping("/admin/bookings/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingsTable> updateBooking(@PathVariable int id, @RequestBody BookingsTable booking) {
        return bookingsTableRepository.findById(id)
                .map(existing -> {
                    existing.setUserid(booking.getUserid());
                    existing.setRoomid(booking.getRoomid());
                    existing.setBookingdate(booking.getBookingdate());
                    existing.setStarttime(booking.getStarttime());
                    existing.setEndtime(booking.getEndtime());
                    existing.setStatus(booking.getStatus());
                    BookingsTable updated = bookingsTableRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Admin-only: Delete a booking
    @DeleteMapping("/admin/bookings/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBooking(@PathVariable int id) {
        if (bookingsTableRepository.existsById(id)) {
            bookingsTableRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Admin-only: Get bookings by date
    @GetMapping("/admin/bookings/by-date/{date}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingsTable>> getBookingsByDate(@PathVariable Date date) {
        List<BookingsTable> bookings = bookingsTableRepository.findByBookingdate(date);
        return ResponseEntity.ok(bookings);
    }

    // Admin-only: Get bookings by user ID and date
    @GetMapping("/admin/bookings/by-user-and-date/{userId}/{date}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingsTable>> getBookingsByUserAndDate(
            @PathVariable int userId, @PathVariable Date date
    ) {
        List<BookingsTable> bookings = bookingsTableRepository.findByUseridAndBookingdate(userId, date);
        return ResponseEntity.ok(bookings);
    }
}