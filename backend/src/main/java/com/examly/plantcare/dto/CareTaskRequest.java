package com.examly.plantcare.dto;

import java.time.LocalDateTime;

import com.examly.plantcare.entity.CareTask;

import lombok.Data;

@Data
public class CareTaskRequest {
    private Long plantId;
    private CareTask.TaskType taskType;
    private LocalDateTime scheduledDate;
    private CareTask.Status status;
    private CareTask.Priority priority;
    private String notes;
}
