package com.example.roombooking.models;

import lombok.Data;
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class UsersTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userid")
    private int userid;

    @JsonProperty("username")
    private String username;

    @JsonProperty("email")
    private String email;

    @JsonProperty("passwordHash")
    private String passwordhash;

    @JsonProperty("roleId")
    private int roleid;

    @JsonProperty("createdAt")
    private LocalDateTime createdat;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedat;

    @ManyToOne
    @JoinColumn(name = "roleid", insertable = false, updatable = false)
    private RolesTable role;
}