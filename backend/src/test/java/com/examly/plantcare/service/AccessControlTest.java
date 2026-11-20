package com.examly.plantcare.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import com.examly.plantcare.entity.EnvironmentData;
import com.examly.plantcare.entity.HealthRecord;
import com.examly.plantcare.entity.Plant;
import com.examly.plantcare.entity.User;
import com.examly.plantcare.repository.EnvironmentDataRepository;
import com.examly.plantcare.repository.HealthRecordRepository;
import com.examly.plantcare.repository.PlantRepository;
import com.examly.plantcare.repository.UserRepository;

/** Users must only reach plants, health records and environment data they own. */
@ExtendWith(MockitoExtension.class)
class AccessControlTest {

    @Mock PlantRepository plants;
    @Mock UserRepository users;
    @Mock HealthRecordRepository healthRepo;
    @Mock EnvironmentDataRepository envRepo;
    PlantService plantService;
    HealthRecordService healthService;
    EnvironmentDataService envService;
    Plant alicesPlant;

    @BeforeEach
    void setUp() {
        plantService = new PlantService(plants, users);
        healthService = new HealthRecordService();
        envService = new EnvironmentDataService();
        org.springframework.test.util.ReflectionTestUtils.setField(healthService, "repository", healthRepo);
        org.springframework.test.util.ReflectionTestUtils.setField(healthService, "plantRepository", plants);
        org.springframework.test.util.ReflectionTestUtils.setField(envService, "repository", envRepo);
        org.springframework.test.util.ReflectionTestUtils.setField(envService, "plantRepository", plants);
        alicesPlant = new Plant();
        alicesPlant.setId(10L);
        alicesPlant.setOwner(User.builder().id(1L).username("alice").build());
        loginAs("bob");
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(String name) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                name, null, List.of(new SimpleGrantedAuthority("ROLE_STANDARD_PLANT_OWNER"))));
    }

    private Plant reference(long id) {
        Plant p = new Plant();
        p.setId(id);
        return p;
    }

    @Test
    void creatingAPlantWithSomeoneElsesIdCannotOverwriteThatPlant() {
        User bob = User.builder().id(2L).username("bob").build();
        when(users.findByUsername("bob")).thenReturn(Optional.of(bob));
        when(plants.save(any())).thenAnswer(i -> i.getArgument(0));
        Plant body = reference(10L); // bob sends alice's plant id
        body.setNickname("mine now");

        Plant saved = plantService.createPlantForLoggedInUser("bob", body);

        assertNull(saved.getId(), "id from the request body must be ignored so a new row is created");
        assertSame(bob, saved.getOwner());
    }

    @Test
    void otherUsersCannotUpdateOrDeleteAPlant() {
        when(plants.findById(10L)).thenReturn(Optional.of(alicesPlant));

        assertThrows(ResponseStatusException.class, () -> plantService.updatePlant(10L, new Plant()));
        assertThrows(ResponseStatusException.class, () -> plantService.deletePlant(10L));
        assertTrue(plantService.getPlantById(10L).isEmpty());
        verify(plants, never()).delete(any());
    }

    @Test
    void healthRecordsOfOtherUsersAreHiddenAndCannotBeCreatedOrChanged() {
        HealthRecord record = new HealthRecord();
        record.setId(3L);
        record.setPlant(alicesPlant);
        when(healthRepo.findAll()).thenReturn(List.of(record));
        when(healthRepo.findById(3L)).thenReturn(Optional.of(record));
        when(plants.findById(10L)).thenReturn(Optional.of(alicesPlant));

        assertTrue(healthService.getRecords(null, null).isEmpty());
        assertTrue(healthService.getById(3L).isEmpty());
        assertThrows(ResponseStatusException.class, () -> healthService.update(3L, new HealthRecord()));
        HealthRecord incoming = new HealthRecord();
        incoming.setPlant(reference(10L));
        assertThrows(ResponseStatusException.class, () -> healthService.save(incoming));
        verify(healthRepo, never()).save(any());
    }

    @Test
    void ownerCanCreateAHealthRecordAndTheServerAttachesTheRealPlant() {
        loginAs("alice");
        when(plants.findById(10L)).thenReturn(Optional.of(alicesPlant));
        when(healthRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        HealthRecord incoming = new HealthRecord();
        incoming.setId(99L);
        incoming.setPlant(reference(10L));

        HealthRecord saved = healthService.save(incoming);

        assertNull(saved.getId());
        assertSame(alicesPlant, saved.getPlant());
    }

    @Test
    void environmentDataOfOtherUsersIsHiddenAndCannotBeWritten() {
        EnvironmentData reading = new EnvironmentData();
        reading.setPlant(alicesPlant);
        when(envRepo.findAll()).thenReturn(List.of(reading));
        when(plants.findById(10L)).thenReturn(Optional.of(alicesPlant));

        assertTrue(envService.getEnvironmentData(null, null, null).isEmpty());
        assertThrows(ResponseStatusException.class, () -> envService.getCurrent(10L));
        assertThrows(ResponseStatusException.class,
                () -> envService.getTrends(10L, LocalDateTime.now().minusDays(1), LocalDateTime.now()));
        EnvironmentData incoming = new EnvironmentData();
        incoming.setPlant(reference(10L));
        assertThrows(ResponseStatusException.class, () -> envService.save(incoming));
        verify(envRepo, never()).save(any());
    }

    @Test
    void environmentReadingWithoutAPlantIsRejected() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> envService.save(new EnvironmentData()));
        assertEquals(400, e.getStatus().value());
    }
}
