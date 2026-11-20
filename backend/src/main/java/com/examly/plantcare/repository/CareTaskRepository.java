package com.examly.plantcare.repository;

import com.examly.plantcare.entity.CareTask;
import com.examly.plantcare.entity.CareTask.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CareTaskRepository extends JpaRepository<CareTask, Long> {

    List<CareTask> findByPlantId(Long plantId);

    @Query("SELECT c FROM CareTask c JOIN FETCH c.plant p JOIN FETCH p.owner o "
            + "WHERE c.status = :status AND (c.reminderSent IS NULL OR c.reminderSent = false) "
            + "AND c.scheduledDate <= :cutoff")
    List<CareTask> findDueForReminder(@Param("status") Status status, @Param("cutoff") LocalDateTime cutoff);

    List<CareTask> findByPlantIdAndStatus(Long plantId, Status status);

    @Query("SELECT c FROM CareTask c WHERE c.scheduledDate >= :start AND c.scheduledDate < :end")
    List<CareTask> findByScheduledDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT c FROM CareTask c WHERE c.status = 'PENDING' AND c.scheduledDate > CURRENT_TIMESTAMP")
    List<CareTask> findUpcoming();

    @Query("SELECT c FROM CareTask c WHERE c.status = 'PENDING' AND c.scheduledDate < CURRENT_TIMESTAMP")
    List<CareTask> findOverdue();
}
