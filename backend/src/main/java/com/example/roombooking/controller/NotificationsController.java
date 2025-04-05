package com.example.roombooking.controller;

import com.example.roombooking.models.NotificationsTable;
import com.example.roombooking.repositories.NotificationsTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class NotificationsController {

    @Autowired
    private NotificationsTableRepository notificationsTableRepository;

    @GetMapping("/notifications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationsTable>> getAllNotifications() {
        List<NotificationsTable> notifications = notificationsTableRepository.findAll();
        return notifications.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(notifications);
    }

    @PostMapping("/notifications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationsTable> createNotification(@RequestBody NotificationsTable notification) {
        NotificationsTable savedNotification = notificationsTableRepository.save(notification);
        return ResponseEntity.ok(savedNotification);
    }

    @PutMapping("/notifications/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationsTable> updateNotification(@PathVariable int id, @RequestBody NotificationsTable notification) {
        return notificationsTableRepository.findById(id)
                .map(existing -> {
                    existing.setUserid(notification.getUserid());
                    existing.setMessage(notification.getMessage());
                    existing.setIsread(notification.isIsread());
                    NotificationsTable updated = notificationsTableRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/notifications/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNotification(@PathVariable int id) {
        if (notificationsTableRepository.existsById(id)) {
            notificationsTableRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}