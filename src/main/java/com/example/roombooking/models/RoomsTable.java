package com.example.roombooking.models;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "rooms")
public class RoomsTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generate the primary key
    private int roomid; 
    private String roomnumber; 
    private String block;
    private int floor;
    private int capacity;
    private String status;
    private LocalDateTime createdat; 
    private LocalDateTime updatedat;
    private String category;           
    private String subcategory;
    @ElementCollection
    private List<String> amenities;

    public RoomsTable() {}
}