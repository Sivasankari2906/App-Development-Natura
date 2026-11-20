package com.examly.plantcare.service;

import com.examly.plantcare.entity.EnvironmentData;
import com.examly.plantcare.repository.EnvironmentDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnvironmentDataService {

    @Autowired
    private EnvironmentDataRepository repository;

    public List<EnvironmentData> getEnvironmentData(Long plantId, String locationId, LocalDateTime fromDate) {
        if (plantId != null && fromDate != null) {
            return repository.findByPlantIdAndRecordedDateBetween(plantId, fromDate, LocalDateTime.now());
        }
        if (locationId != null && fromDate != null) {
            return repository.findByLocationIdAndRecordedDateBetween(locationId, fromDate, LocalDateTime.now());
        }
        return repository.findAll();
    }

    public EnvironmentData save(EnvironmentData data) {
        return repository.save(data);
    }

    public EnvironmentData getCurrent(Long plantId) {
        return repository.findTopByPlantIdOrderByRecordedDateDesc(plantId);
    }

    public List<EnvironmentData> getTrends(Long plantId, LocalDateTime from, LocalDateTime to) {
        return repository.findByPlantIdAndRecordedDateBetween(plantId, from, to);
    }
}
