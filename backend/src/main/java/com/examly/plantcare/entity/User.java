package com.examly.plantcare.entity;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private Boolean isActive = true;
    private Boolean emailVerified = false;
    private String location;

    @Enumerated(EnumType.STRING)
    private GardeningExperience gardeningExperience;

    private String timezone;

    @Column(columnDefinition = "json")
    private String notificationPreferences; // store JSON as String

    private LocalDateTime createdDateTime = LocalDateTime.now();
    private LocalDateTime lastLogin;
    
    // Getter methods for frontend compatibility
    public LocalDateTime getCreatedDate() {
        return this.createdDateTime;
    }
    
    public String getGardeningExp() {
        return this.gardeningExperience != null ? this.gardeningExperience.toString() : null;
    }

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "user")
    private List<Plant> plants = new ArrayList<>();

    @OneToMany(mappedBy = "completedBy")
    @JsonManagedReference(value = "users")
    private List<CareTask> completedTasks;


    public enum Role {
        ADMIN,
        PLANT_CARE_SPECIALIST,
        PREMIUM_PLANT_OWNER,
        STANDARD_PLANT_OWNER,
        COMMUNITY_MEMBER,
        GUEST
    }

    public enum GardeningExperience {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED,
        EXPERT
    }

}
