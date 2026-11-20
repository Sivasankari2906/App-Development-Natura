package com.examly.plantcare.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.examly.plantcare.entity.CareTask;
import com.examly.plantcare.entity.CareTask.Status;
import com.examly.plantcare.entity.CareTask.TaskType;
import com.examly.plantcare.entity.Plant;
import com.examly.plantcare.entity.User;
import com.examly.plantcare.repository.CareTaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock CareTaskRepository careTasks;
    @Mock EmailService email;
    ReminderService service;

    @BeforeEach
    void setUp() {
        service = new ReminderService(careTasks, email, new ObjectMapper());
        ReflectionTestUtils.setField(service, "lookaheadHours", 24L);
    }

    private CareTask dueTask(User owner, String plantName, TaskType type) {
        Plant plant = new Plant();
        plant.setNickname(plantName);
        plant.setOwner(owner);
        return CareTask.builder().plant(plant).taskType(type).status(Status.PENDING)
                .scheduledDate(LocalDateTime.now().plusHours(2)).reminderSent(false).build();
    }

    private User user(long id, String name, String prefs) {
        return User.builder().id(id).username(name).email(name + "@example.com").notificationPreferences(prefs).build();
    }

    @Test
    void sendsOneDigestPerUserAndMarksTasksReminded() {
        User alice = user(1, "alice", null);
        CareTask a1 = dueTask(alice, "Fern", TaskType.WATERING);
        CareTask a2 = dueTask(alice, "Cactus", TaskType.PRUNING);
        when(careTasks.findDueForReminder(eq(Status.PENDING), any(), any())).thenReturn(List.of(a1, a2));
        when(email.send(anyString(), anyString(), anyString())).thenReturn(true);

        int reminded = service.sendDueReminders();

        assertEquals(2, reminded);
        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(email, times(1)).send(eq("alice@example.com"), anyString(), body.capture());
        assertTrue(body.getValue().contains("WATERING for Fern"));
        assertTrue(body.getValue().contains("PRUNING for Cactus"));
        assertTrue(a1.getReminderSent() && a2.getReminderSent());
        verify(careTasks).markReminded(any());
    }

    @Test
    void usersWhoTurnedRemindersOffAreSkipped() {
        CareTask t = dueTask(user(2, "bob", "{\"email\":false}"), "Fern", TaskType.WATERING);
        when(careTasks.findDueForReminder(eq(Status.PENDING), any(), any())).thenReturn(List.of(t));

        assertEquals(0, service.sendDueReminders());
        verifyNoInteractions(email);
        assertFalse(t.getReminderSent());
    }

    @Test
    void failedEmailLeavesTasksForTheNextRun() {
        CareTask t = dueTask(user(3, "cara", null), "Fern", TaskType.WATERING);
        when(careTasks.findDueForReminder(eq(Status.PENDING), any(), any())).thenReturn(List.of(t));
        when(email.send(anyString(), anyString(), anyString())).thenReturn(false);

        assertEquals(0, service.sendDueReminders());
        assertFalse(t.getReminderSent());
        verify(careTasks, never()).markReminded(any());
    }

    @Test
    void optOutIsHonouredWhenTheDatabaseReturnsTheJsonAsAQuotedString() {
        // H2 returns a JSON column as a JSON string, i.e. with the text quoted and escaped.
        User quoted = user(4, "dave", "\"{\\\"email\\\":false}\"");
        CareTask t = dueTask(quoted, "Fern", TaskType.WATERING);
        when(careTasks.findDueForReminder(eq(Status.PENDING), any(), any())).thenReturn(List.of(t));

        assertEquals(0, service.sendDueReminders());
        verifyNoInteractions(email);
    }
}
