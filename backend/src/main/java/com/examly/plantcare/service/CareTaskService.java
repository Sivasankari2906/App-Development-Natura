package com.examly.plantcare.service;

import com.examly.plantcare.entity.CareTask;
import com.examly.plantcare.repository.CareTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CareTaskService {

    private final CareTaskRepository careTaskRepository;

    public List<CareTask> getTasks(Long plantId, LocalDateTime date, CareTask.Status status) {
        if (plantId != null && status != null) {
            return careTaskRepository.findByPlantIdAndStatus(plantId, status);
        } else if (plantId != null) {
            return careTaskRepository.findByPlantId(plantId);
        } else if (date != null) {
            LocalDateTime start = date.withHour(0).withMinute(0);
            LocalDateTime end = date.withHour(23).withMinute(59);
            return careTaskRepository.findByScheduledDateBetween(start, end);
        }
        return careTaskRepository.findAll();
    }

    public CareTask save(CareTask careTask) {
        return careTaskRepository.save(careTask);
    }

    public Optional<CareTask> getById(Long id) {
        return careTaskRepository.findById(id);
    }

    public void delete(Long id) {
        careTaskRepository.deleteById(id);
    }

    public CareTask markComplete(Long id) {
        CareTask task = careTaskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(CareTask.Status.COMPLETED);
        task.setCompletedDate(LocalDateTime.now());
        return careTaskRepository.save(task);
    }

    public List<CareTask> getUpcoming() {
        return careTaskRepository.findUpcoming();
    }

    public List<CareTask> getOverdue() {
        return careTaskRepository.findOverdue();
    }

    public List<CareTask> getCalendar(int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59);
        return careTaskRepository.findByScheduledDateBetween(start, end);
    }

    public List<CareTask> bulkComplete(List<Long> ids) {
        List<CareTask> tasks = careTaskRepository.findAllById(ids);
        tasks.forEach(task -> {
            task.setStatus(CareTask.Status.COMPLETED);
            task.setCompletedDate(LocalDateTime.now());
        });
        return careTaskRepository.saveAll(tasks);
    }

    public CareTask reschedule(Long id, LocalDateTime newDate) {
        CareTask task = careTaskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setScheduledDate(newDate);
        task.setStatus(CareTask.Status.PENDING);
        return careTaskRepository.save(task);
    }

    // stub for generate schedule
    public List<CareTask> generateSchedule(Long plantId) {
        // Implement based on plant care plan
        throw new UnsupportedOperationException("Schedule generation not implemented yet");
    }
}
