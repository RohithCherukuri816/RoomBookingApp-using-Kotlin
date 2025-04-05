package com.example.roombooking.repositories;

import com.example.roombooking.models.NotificationsTable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationsTableRepository extends JpaRepository<NotificationsTable, Integer> {
}