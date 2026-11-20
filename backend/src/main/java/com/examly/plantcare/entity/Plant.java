package com.examly.plantcare.entity;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "plants")
public class Plant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(length = 200)
    private String location;

    @Column(name = "acquisition_date")
    private LocalDate acquisitionDate;

    @Column(name = "current_height_cm", precision = 5, scale = 1)
    private BigDecimal currentHeightCm;

    @Column(name = "current_width_cm", precision = 5, scale = 1)
    private BigDecimal currentWidthCm;

    @Column(name = "pot_size", length = 50)
    private String potSize;

    @Column(name = "soil_type", length = 100)
    private String soilType;

    @Column(name = "last_watered_date")
    private LocalDateTime lastWateredDate;

    @Column(name = "last_fertilized_date")
    private LocalDateTime lastFertilizedDate;

    @Column(name = "last_pruned_date")
    private LocalDateTime lastPrunedDate;

    @Column(name = "last_repotted_date")
    private LocalDateTime lastRepottedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "health_status", nullable = false)
    private HealthStatus healthStatus;

    @Lob
    private String notes;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "updated_date")
    private LocalDateTime updatedDate = LocalDateTime.now();

    @PreUpdate
    public void setUpdatedDate() {
        this.updatedDate = LocalDateTime.now();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonBackReference(value = "user")
    private User owner;

    // In Plant
    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "care-tasks")
    private List<CareTask> careTasks;

    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "plant-environment")
    private List<EnvironmentData> environmentDataList = new ArrayList<>();

    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "plant-health")
    private List<HealthRecord> healthRecords = new ArrayList<>();

    public enum HealthStatus {
        EXCELLENT, GOOD, FAIR, POOR, CRITICAL
    }
}