package com.examly.plantcare.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.examly.plantcare.entity.HealthRecord;
import com.examly.plantcare.entity.Plant;
import com.examly.plantcare.repository.HealthRecordRepository;
import com.examly.plantcare.repository.PlantRepository;
import com.examly.plantcare.util.Access;

/** Health records. Every operation is limited to plants the current user owns (admins see all). */
@Service
public class HealthRecordService {

    @Autowired
    private HealthRecordRepository repository;

    @Autowired
    private PlantRepository plantRepository;

    // Fetch health records by plant and optional fromDate
    public List<HealthRecord> getRecords(Long plantId, LocalDateTime fromDate) {
        List<HealthRecord> records;
        if (plantId != null && fromDate != null) {
            records = repository.findByPlant_IdAndAssessmentDateAfter(plantId, fromDate);
        } else if (plantId != null) {
            records = repository.findByPlant_Id(plantId);
        } else {
            records = repository.findAll();
        }
        return records.stream().filter(r -> Access.canAccess(r.getPlant())).toList();
    }

    // Save new record
    public HealthRecord save(HealthRecord record) {
        record.setId(null);
        record.setPlant(accessiblePlant(record.getPlant()));
        return repository.save(record);
    }

    // Get by ID
    public Optional<HealthRecord> getById(Long id) {
        return repository.findById(id).filter(r -> Access.canAccess(r.getPlant()));
    }

    // Update record
    public HealthRecord update(Long id, HealthRecord updated) {
        HealthRecord existing = getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Health record not found"));
        updated.setId(id);
        updated.setPlant(existing.getPlant());
        return repository.save(updated);
    }

    // Delete record
    public void delete(Long id) {
        repository.delete(getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Health record not found")));
    }

    private Plant accessiblePlant(Plant reference) {
        if (reference == null || reference.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "plant.id is required.");
        }
        return plantRepository.findById(reference.getId())
                .filter(Access::canAccess)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plant not found"));
    }
}
