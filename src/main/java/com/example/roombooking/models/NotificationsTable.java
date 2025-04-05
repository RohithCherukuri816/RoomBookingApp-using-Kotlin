package com.example.roombooking.models;

import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.JoinColumn;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notification")
public class NotificationsTable {
    @Id
    private int notificationid; 
    private int userid; 
    private String message;
    private boolean isread; 
    private LocalDateTime createdat;

    @ManyToOne
    @JoinColumn(name = "userid", insertable = false, updatable = false)
    private UsersTable user;
}