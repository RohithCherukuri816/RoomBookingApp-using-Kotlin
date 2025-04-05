package com.example.roombooking.models;

import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "roles")
public class RolesTable {
    @Id
    private int roleid; 
    private String rolename; 
    private String description;
}