package com.example.roombooking.repositories;

import com.example.roombooking.models.UsersTable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsersTableRepository extends JpaRepository<UsersTable, Integer> {
    Optional<UsersTable> findByEmail(String email);
}