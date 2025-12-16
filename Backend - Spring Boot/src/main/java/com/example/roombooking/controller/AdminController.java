package com.example.roombooking.controller;

import com.example.roombooking.models.RolesTable;
import com.example.roombooking.models.UsersTable;
import com.example.roombooking.repositories.RolesTableRepository;
import com.example.roombooking.repositories.UsersTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UsersTableRepository usersTableRepository;

    @Autowired
    private RolesTableRepository rolesTableRepository;

    // Users endpoints
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsersTable>> getAllUsers() {
        List<UsersTable> users = usersTableRepository.findAll();
        return users.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(users);
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsersTable> createUser(@RequestBody UsersTable user) {
        UsersTable savedUser = usersTableRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsersTable> updateUser(@PathVariable int id, @RequestBody UsersTable user) {
        return usersTableRepository.findById(id)
                .map(existing -> {
                    existing.setUsername(user.getUsername());
                    existing.setEmail(user.getEmail());
                    existing.setPasswordhash(user.getPasswordhash());
                    existing.setRoleid(user.getRoleid());
                    UsersTable updated = usersTableRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable int id) {
        if (usersTableRepository.existsById(id)) {
            usersTableRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Roles endpoint
    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RolesTable>> getAllRoles() {
        List<RolesTable> roles = rolesTableRepository.findAll();
        return roles.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(roles);
    }
}