package com.examly.plantcare.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.examly.plantcare.entity.CareTask;
import com.examly.plantcare.entity.User;
import com.examly.plantcare.repository.CareTaskRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/** Emails each user one digest of care tasks that are due soon, once per task. */
@Service
@RequiredArgsConstructor
public class ReminderService {

    private static final Logger log = LoggerFactory.getLogger(ReminderService.class);
    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("EEE d MMM, HH:mm");

    private final CareTaskRepository careTasks;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @Value("${app.reminders.lookahead-hours:24}")
    private long lookaheadHours;

    @Scheduled(fixedDelayString = "${app.reminders.interval-ms:3600000}",
            initialDelayString = "${app.reminders.initial-delay-ms:60000}")
    @Transactional
    public int sendDueReminders() {
        LocalDateTime cutoff = LocalDateTime.now().plusHours(lookaheadHours);
        List<CareTask> due = careTasks.findDueForReminder(CareTask.Status.PENDING, cutoff);

        Map<Long, List<CareTask>> byOwner = due.stream()
                .filter(t -> t.getPlant() != null && t.getPlant().getOwner() != null)
                .collect(Collectors.groupingBy(t -> t.getPlant().getOwner().getId()));

        int reminded = 0;
        for (List<CareTask> tasks : byOwner.values()) {
            User owner = tasks.get(0).getPlant().getOwner();
            if (!remindersEnabled(owner)) {
                continue;
            }
            String lines = tasks.stream()
                    .sorted((a, b) -> a.getScheduledDate().compareTo(b.getScheduledDate()))
                    .map(t -> "- " + t.getTaskType() + " for " + t.getPlant().getNickname()
                            + ", due " + t.getScheduledDate().format(WHEN))
                    .collect(Collectors.joining("\n"));
            boolean sent = emailService.send(owner.getEmail(), "Plant care reminder",
                    "Hi " + owner.getUsername() + ",\n\nThese care tasks are due soon:\n" + lines);
            if (sent) {
                tasks.forEach(t -> t.setReminderSent(true));
                careTasks.saveAll(tasks);
                reminded += tasks.size();
            }
        }
        if (reminded > 0) {
            log.info("Sent reminders for {} care tasks", reminded);
        }
        return reminded;
    }

    /** Reminders are on unless the user's notification preferences turn off email or care reminders. */
    boolean remindersEnabled(User user) {
        String json = user.getNotificationPreferences();
        if (json == null || json.isBlank()) {
            return true;
        }
        try {
            JsonNode prefs = objectMapper.readTree(json);
            return prefs.path("email").asBoolean(true) && prefs.path("careReminders").asBoolean(true);
        } catch (Exception e) {
            return true;
        }
    }
}
