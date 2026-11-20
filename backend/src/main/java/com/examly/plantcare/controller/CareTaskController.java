package com.examly.plantcare.controller;

import com.examly.plantcare.entity.CareTask;
import com.examly.plantcare.service.CareTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/care-tasks")
@RequiredArgsConstructor
public class CareTaskController {

    private final CareTaskService careTaskService;

    @GetMapping
    public ResponseEntity<List<CareTask>> getTasks(
            @RequestParam(required = false) Long plantId,
            @RequestParam(required = false) LocalDateTime date,
            @RequestParam(required = false) CareTask.Status status) {
        return ResponseEntity.ok(careTaskService.getTasks(plantId, date, status));
    }

    @PostMapping
    public ResponseEntity<CareTask> create(@RequestBody CareTask task) {
        return ResponseEntity.ok(careTaskService.save(task));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CareTask> getById(@PathVariable Long id) {
        return careTaskService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CareTask> update(@PathVariable Long id, @RequestBody CareTask updated) {
        return careTaskService.getById(id).map(task -> {
            updated.setId(id);
            return ResponseEntity.ok(careTaskService.save(updated));
        }).orElse(ResponseEntity.notFound().build());
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
    public ResponseEntity<?> generateSchedule(@RequestParam Long plantId) {
        return ResponseEntity.ok(careTaskService.generateSchedule(plantId));
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<CareTask> reschedule(@PathVariable Long id, @RequestParam LocalDateTime newDate) {
        return ResponseEntity.ok(careTaskService.reschedule(id, newDate));
    }

    @GetMapping("/statistics")
    public ResponseEntity<String> statistics() {
        return ResponseEntity.ok("Statistics not implemented yet");
    }
}
