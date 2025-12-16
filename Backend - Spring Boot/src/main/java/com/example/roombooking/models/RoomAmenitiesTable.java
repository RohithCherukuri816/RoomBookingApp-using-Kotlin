package com.example.roombooking.models;

import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.JoinColumn;

@Data
@Entity
@Table(name = "roomamenities")
public class RoomAmenitiesTable {
    @Id
    private int amenityid; // Primary key

    private int roomid; // Foreign key referencing rooms(roomid)

    private String amenityname; // Corrected to String

    private boolean isenabled; // TINYINT(1) in the database maps to boolean

    @ManyToOne
    @JoinColumn(name = "roomid", insertable = false, updatable = false)
    private RoomsTable room; // Relationship to RoomsTable
}