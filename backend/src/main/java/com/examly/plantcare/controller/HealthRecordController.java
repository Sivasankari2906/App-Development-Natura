package com.examly.plantcare.controller;

import com.examly.plantcare.entity.HealthRecord;
import com.examly.plantcare.service.HealthRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/health-records")
public class HealthRecordController {

    @Autowired
    private HealthRecordService service;

    // GET /api/health-records?plantId={id}&fromDate={date}
    @GetMapping
    public ResponseEntity<List<HealthRecord>> getRecords(
            @RequestParam(required = false) Long plantId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate
    ) {
        return ResponseEntity.ok(service.getRecords(plantId, fromDate));
    }

    // POST /api/health-records
    @PostMapping
    public ResponseEntity<HealthRecord> createRecord(@RequestBody HealthRecord record) {
        return ResponseEntity.ok(service.save(record));
    }

    // GET /api/health-records/{id}
    @GetMapping("/{id}")
    public ResponseEntity<HealthRecord> getRecord(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/health-records/{id}
    @PutMapping("/{id}")
    public ResponseEntity<HealthRecord> updateRecord(
            @PathVariable Long id,
            @RequestBody HealthRecord updated
    ) {
        return ResponseEntity.ok(service.update(id, updated));
    }

    // POST /api/health-records/{id}/photos
    @PostMapping("/{id}/photos")
    public ResponseEntity<String> addPhotos(@PathVariable Long id, @RequestBody String photosJson) {
        return ResponseEntity.ok("Photos added for record " + id + ": " + photosJson);
    }

    // GET /api/health-records/{id}/treatments
    @GetMapping("/{id}/treatments")
    public ResponseEntity<String> getTreatments(@PathVariable Long id) {
        return ResponseEntity.ok("List of treatments for record " + id);
    }

    // POST /api/health-records/{id}/treatments
    @PostMapping("/{id}/treatments")
    public ResponseEntity<String> addTreatment(@PathVariable Long id, @RequestBody String treatment) {
        return ResponseEntity.ok("Treatment added to record " + id + ": " + treatment);
    }

    // PUT /api/health-records/{id}/recovery-status
    @PutMapping("/{id}/recovery-status")
    public ResponseEntity<String> updateRecoveryStatus(@PathVariable Long id, @RequestBody String status) {
        return ResponseEntity.ok("Recovery status updated for record " + id + " to " + status);
    }

    // GET /api/health-records/symptoms-checker
    @GetMapping("/symptoms-checker")
    public ResponseEntity<String> symptomsChecker(@RequestParam String symptoms) {
        return ResponseEntity.ok("Possible diagnoses for symptoms: " + symptoms);
    }

    // POST /api/health-records/diagnose
    @PostMapping("/diagnose")
    public ResponseEntity<String> diagnose(@RequestBody String diagnosisRequest) {
        return ResponseEntity.ok("Diagnosis result: " + diagnosisRequest);
    }

    // GET /api/health-records/trends/{plantId}
    @GetMapping("/trends/{plantId}")
    public ResponseEntity<String> getTrends(@PathVariable Long plantId) {
        return ResponseEntity.ok("Health trends for plant " + plantId);
    }
}
