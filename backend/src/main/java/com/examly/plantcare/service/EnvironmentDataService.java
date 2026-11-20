package com.examly.plantcare.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.examly.plantcare.entity.EnvironmentData;
import com.examly.plantcare.entity.Plant;
import com.examly.plantcare.repository.EnvironmentDataRepository;
import com.examly.plantcare.repository.PlantRepository;
import com.examly.plantcare.util.Access;

/** Environment readings. Every operation is limited to plants the current user owns (admins see all). */
@Service
public class EnvironmentDataService {

    @Autowired
    private EnvironmentDataRepository repository;

    @Autowired
    private PlantRepository plantRepository;

    public List<EnvironmentData> getEnvironmentData(Long plantId, String locationId, LocalDateTime fromDate) {
        List<EnvironmentData> data;
        if (plantId != null && fromDate != null) {
            data = repository.findByPlantIdAndRecordedDateBetween(plantId, fromDate, LocalDateTime.now());
        } else if (locationId != null && fromDate != null) {
            data = repository.findByLocationIdAndRecordedDateBetween(locationId, fromDate, LocalDateTime.now());
        } else {
            data = repository.findAll();
        }
        return data.stream().filter(d -> Access.canAccess(d.getPlant())).toList();
    }

    public EnvironmentData save(EnvironmentData data) {
        if (data.getPlant() == null || data.getPlant().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "plant.id is required.");
        }
        data.setId(null);
        data.setPlant(accessiblePlant(data.getPlant().getId()));
        return repository.save(data);
    }

    public EnvironmentData getCurrent(Long plantId) {
        accessiblePlant(plantId);
        return repository.findTopByPlantIdOrderByRecordedDateDesc(plantId);
    }

    public List<EnvironmentData> getTrends(Long plantId, LocalDateTime from, LocalDateTime to) {
        accessiblePlant(plantId);
        return repository.findByPlantIdAndRecordedDateBetween(plantId, from, to);
    }

    private Plant accessiblePlant(Long plantId) {
        return plantRepository.findById(plantId)
                .filter(Access::canAccess)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plant not found"));
    }
}
