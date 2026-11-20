package com.examly.plantcare.controller;

import com.examly.plantcare.entity.EnvironmentData;
import com.examly.plantcare.service.EnvironmentDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/environment")
public class EnvironmentDataController {

    @Autowired
    private EnvironmentDataService service;

    // GET /api/environment?plantId={id}&locationId={location}&fromDate=(date)
    @GetMapping
    public ResponseEntity<List<EnvironmentData>> getEnvironmentData(
            @RequestParam(required = false) Long plantId,
            @RequestParam(required = false) String locationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate
    ) {
        return ResponseEntity.ok(service.getEnvironmentData(plantId, locationId, fromDate));
    }

    // POST /api/environment/manual-entry
    @PostMapping("/manual-entry")
    public ResponseEntity<EnvironmentData> addManualEntry(@RequestBody EnvironmentData data) {
        data.setDataSource(EnvironmentData.DataSource.MANUAL);
        return ResponseEntity.ok(service.save(data));
    }

    // GET /api/environment/{plantId}/current
    @GetMapping("/{plantId}/current")
    public ResponseEntity<EnvironmentData> getCurrent(@PathVariable Long plantId) {
        return ResponseEntity.ok(service.getCurrent(plantId));
    }

    // GET /api/environment/{plantId}/trends
    @GetMapping("/{plantId}/trends")
    public ResponseEntity<List<EnvironmentData>> getTrends(
            @PathVariable Long plantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.ok(service.getTrends(plantId, from, to));
    }

    // POST /api/environment/sensor-data
    @PostMapping("/sensor-data")
    public ResponseEntity<EnvironmentData> addSensorData(@RequestBody EnvironmentData data) {
        data.setDataSource(EnvironmentData.DataSource.SENSOR);
        return ResponseEntity.ok(service.save(data));
    }

    // GET /api/environment/alerts
    @GetMapping("/alerts")
    public ResponseEntity<String> getAlerts() {
        return ResponseEntity.ok("List of active alerts will go here.");
    }

    // PUT /api/environment/alerts/{id}/acknowledge
    @PutMapping("/alerts/{id}/acknowledge")
    public ResponseEntity<String> acknowledgeAlert(@PathVariable Long id) {
        return ResponseEntity.ok("Alert " + id + " acknowledged.");
    }

    // GET /api/environment/optimal-conditions/{speciesId}
    @GetMapping("/optimal-conditions/{speciesId}")
    public ResponseEntity<String> getOptimalConditions(@PathVariable Long speciesId) {
        return ResponseEntity.ok("Optimal conditions for species " + speciesId);
    }

    // POST /api/environment/calibrate-sensor
    @PostMapping("/calibrate-sensor")
    public ResponseEntity<String> calibrateSensor(@RequestParam String sensorId) {
        return ResponseEntity.ok("Sensor " + sensorId + " calibrated successfully.");
    }

    // GET /api/environment/sensors/status
    @GetMapping("/sensors/status")
    public ResponseEntity<String> getSensorStatus() {
        return ResponseEntity.ok("All sensors are online.");
    }
}
