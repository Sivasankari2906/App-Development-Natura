package com.examly.plantcare.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.examly.plantcare.dto.CareTaskRequest;
import com.examly.plantcare.entity.CareTask;
import com.examly.plantcare.service.CareTaskService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/care-tasks")
@RequiredArgsConstructor
public class CareTaskController {

    private final CareTaskService careTaskService;

    @GetMapping
    public ResponseEntity<List<CareTask>> getTasks(
            @RequestParam(required = false) Long plantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(required = false) CareTask.Status status) {
        return ResponseEntity.ok(careTaskService.getTasks(plantId, date, status));
    }

    @PostMapping
    public ResponseEntity<CareTask> create(@RequestBody CareTaskRequest request) {
        return ResponseEntity.ok(careTaskService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CareTask> getById(@PathVariable Long id) {
        return careTaskService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CareTask> update(@PathVariable Long id, @RequestBody CareTaskRequest request) {
        return ResponseEntity.ok(careTaskService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        careTaskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<CareTask> complete(@PathVariable Long id) {
        return ResponseEntity.ok(careTaskService.markComplete(id));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<CareTask>> getUpcoming() {
        return ResponseEntity.ok(careTaskService.getUpcoming());
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<CareTask>> getOverdue() {
        return ResponseEntity.ok(careTaskService.getOverdue());
    }

    @PostMapping("/bulk-complete")
    public ResponseEntity<List<CareTask>> bulkComplete(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(careTaskService.bulkComplete(ids));
    }

    @GetMapping("/calendar/{month}/{year}")
    public ResponseEntity<List<CareTask>> getCalendar(@PathVariable int month, @PathVariable int year) {
        return ResponseEntity.ok(careTaskService.getCalendar(month, year));
    }

    @PostMapping("/generate-schedule")
    public ResponseEntity<List<CareTask>> generateSchedule(@RequestParam Long plantId) {
        return ResponseEntity.ok(careTaskService.generateSchedule(plantId));
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<CareTask> reschedule(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newDate) {
        return ResponseEntity.ok(careTaskService.reschedule(id, newDate));
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> statistics() {
        return ResponseEntity.ok(careTaskService.statistics());
    }
}
