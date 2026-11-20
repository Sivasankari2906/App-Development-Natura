package com.examly.plantcare.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // link to plant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    @JsonBackReference(value = "plant-health")
    private Plant plant;

    // link to specialist (optional)
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "specialist_id")
    // private User specialist;

    @Column(name = "assessment_date", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime assessmentDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private OverallHealth overallHealth;

    // store lists as JSON string (no @ElementCollection → no extra tables)
    @Column(columnDefinition = "TEXT")
    private String symptoms;   // JSON array string

    @Column(columnDefinition = "TEXT")
    private String diagnosedIssues;

    @Column(columnDefinition = "TEXT")
    private String treatmentsApplied;

    @Column(columnDefinition = "TEXT")
    private String photos;

    @Column(columnDefinition = "TEXT")
    private String growthMeasurements;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDate followUpDate;

    @Enumerated(EnumType.STRING)
    private RecoveryStatus recoveryStatus = RecoveryStatus.NOT_APPLICABLE;

    // enums
    public enum OverallHealth {
        EXCELLENT, GOOD, FAIR, POOR, CRITICAL
    }

    public enum RecoveryStatus {
        NOT_APPLICABLE, RECOVERING, RECOVERED, WORSENING
    }
}
