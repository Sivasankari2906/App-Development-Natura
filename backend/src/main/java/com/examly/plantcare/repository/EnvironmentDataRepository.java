package com.examly.plantcare.repository;

import com.examly.plantcare.entity.EnvironmentData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EnvironmentDataRepository extends JpaRepository<EnvironmentData, Long> {

    List<EnvironmentData> findByPlantIdAndRecordedDateBetween(Long plantId, LocalDateTime from, LocalDateTime to);

    EnvironmentData findTopByPlantIdOrderByRecordedDateDesc(Long plantId);

    List<EnvironmentData> findByLocationIdAndRecordedDateBetween(String locationId, LocalDateTime from, LocalDateTime to);
}
