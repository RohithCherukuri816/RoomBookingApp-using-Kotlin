package com.example.roombooking.repositories;

import com.example.roombooking.models.RoomsTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomsTableRepository extends JpaRepository<RoomsTable, Integer> {

    // Find all rooms by status (e.g., "Available", "Occupied")
    List<RoomsTable> findByStatus(String status);

    // Find rooms by category (e.g., "Classroom", "Lab")
    List<RoomsTable> findByCategory(String category);

    // Find rooms by block (e.g., "Block A", "Block B")
    List<RoomsTable> findByBlock(String block);

    // Find rooms by floor
    List<RoomsTable> findByFloor(int floor);

    // Find rooms by capacity greater than or equal to a given value
    List<RoomsTable> findByCapacityGreaterThanEqual(int capacity);

    // Find rooms by subcategory (e.g., "Physics Lab", "Chemistry Lab")
    List<RoomsTable> findBySubcategory(String subCategory);
}