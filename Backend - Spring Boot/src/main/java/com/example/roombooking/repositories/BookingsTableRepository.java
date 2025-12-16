package com.example.roombooking.repositories;

import com.example.roombooking.models.BookingsTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface BookingsTableRepository extends JpaRepository<BookingsTable, Integer> {

    // Find all bookings for a specific user
    List<BookingsTable> findByUserid(int userId);

    // Find all bookings for a specific room
    List<BookingsTable> findByRoomid(int roomId);

    // Find all bookings on a specific date
    List<BookingsTable> findByBookingdate(Date bookingDate);

    // Find all bookings for a specific user on a specific date
    List<BookingsTable> findByUseridAndBookingdate(int userId, Date bookingDate);

    // Find all bookings with a specific status (e.g., "Confirmed", "Pending", "Cancelled")
    List<BookingsTable> findByStatus(String status);

    // Find all bookings for a specific room and date
    List<BookingsTable> findByRoomidAndBookingdate(int roomId, Date bookingDate);

    // Find all bookings between two dates
    List<BookingsTable> findByBookingdateBetween(Date startDate, Date endDate);

    // Find all bookings for a specific room, user, and date
    List<BookingsTable> findByRoomidAndUseridAndBookingdate(int roomId, int userId, Date bookingDate);

    // Find all bookings for a specific room and status
    List<BookingsTable> findByRoomidAndStatus(int roomId, String status);
}