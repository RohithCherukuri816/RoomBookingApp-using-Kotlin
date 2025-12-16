package com.example.roombooking.controller;

import com.example.roombooking.models.UsersTable;
import com.example.roombooking.repositories.UsersTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UsersTableRepository usersTableRepository;

    // Get the authenticated user's profile
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsersTable> getUserProfile() {
        String email = getAuthenticatedUserEmail();
        Optional<UsersTable> userOptional = usersTableRepository.findByEmail(email);
        return userOptional.map(ResponseEntity::ok)
        		.orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    // Update the authenticated user's profile
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsersTable> updateUserProfile(@RequestBody UsersTable updatedUser) {
        String email = getAuthenticatedUserEmail();
        Optional<UsersTable> userOptional = usersTableRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            return ResponseEntity.status(404).body(null);
        }

        UsersTable existingUser = userOptional.get();
        if (updatedUser.getUsername() != null) existingUser.setUsername(updatedUser.getUsername());
        if (updatedUser.getEmail() != null) existingUser.setEmail(updatedUser.getEmail());
        if (updatedUser.getPasswordhash() != null) existingUser.setPasswordhash(updatedUser.getPasswordhash());
        existingUser.setUpdatedat(LocalDateTime.now());

        UsersTable savedUser = usersTableRepository.save(existingUser);
        return ResponseEntity.ok(savedUser);
    }

    // Helper method to get the authenticated user's email
    private String getAuthenticatedUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }
}