package com.examly.plantcare.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "species")
@Data
public class Species {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "common_name", nullable = false, length = 200)
    private String commonName;

    @Column(name = "scientific_name", nullable = false, unique = true, length = 200)
    private String scientificName;

    @Column(name = "family_name", length = 100)
    private String familyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "care_difficulty")
    private CareDifficulty careDifficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "light_requirements")
    private LightRequirements lightRequirements;

    private Integer waterFrequencyDays;
    private Integer humidityMin;
    private Integer humidityMax;

    private BigDecimal temperatureMinCelsius;
    private BigDecimal temperatureMaxCelsius;

    private BigDecimal soilPhMin;
    private BigDecimal soilPhMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "growth_rate")
    private GrowthRate growthRate;

    private Integer maxHeightCm;
    private Integer fertilizerFrequencyDays;
    private Integer pruningFrequencyDays;
    private Integer repottingFrequencyMonths;

    @ElementCollection
    @Column(name = "issue")
    private List<String> commonIssues = new ArrayList<>();

    @ElementCollection
    @Column(name = "tip")
    private List<String> careTips = new ArrayList<>();


    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    public enum CareDifficulty { EASY, MODERATE, DIFFICULT, EXPERT }
    public enum LightRequirements { LOW, MEDIUM, HIGH, DIRECT }
    public enum GrowthRate { SLOW, MODERATE, FAST }
}
