package com.examly.plantcare.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "environment_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnvironmentData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id")
    @JsonBackReference(value = "plant-environment")
    private Plant plant;

    private String locationId;
    private String sensorId;

    private Double temperatureCelsius;
    private Double humidityPercentage;
    private Integer lightLevelLux;
    private Double soilMoisturePercentage;
    private Double phLevel;

    private LocalDateTime recordedDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private DataSource dataSource;

    public enum DataSource {
        MANUAL, SENSOR, WEATHER_API
    }
}
