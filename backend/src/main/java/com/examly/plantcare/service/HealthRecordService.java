package com.examly.plantcare.service;

import com.examly.plantcare.entity.HealthRecord;
import com.examly.plantcare.repository.HealthRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class HealthRecordService {

    @Autowired
    private HealthRecordRepository repository;

    // Fetch health records by plant and optional fromDate
    public List<HealthRecord> getRecords(Long plantId, LocalDateTime fromDate) {
        if (plantId != null && fromDate != null) {
            return repository.findByPlant_IdAndAssessmentDateAfter(plantId, fromDate);
        } else if (plantId != null) {
            return repository.findByPlant_Id(plantId);
        }
        return repository.findAll();
    }

    // Save new record
    public HealthRecord save(HealthRecord record) {
        return repository.save(record);
    }

    // Get by ID
    public Optional<HealthRecord> getById(Long id) {
        return repository.findById(id);
    }

    // Update record
    public HealthRecord update(Long id, HealthRecord updated) {
        return repository.findById(id)
                .map(existing -> {
                    updated.setId(id);
                    return repository.save(updated);
                })
                .orElseThrow(() -> new RuntimeException("HealthRecord not found with id " + id));
    }

    // Delete record
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
