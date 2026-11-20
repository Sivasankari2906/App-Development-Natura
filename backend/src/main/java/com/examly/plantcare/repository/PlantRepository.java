package com.examly.plantcare.repository;

import com.examly.plantcare.entity.Plant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlantRepository extends JpaRepository<Plant, Long> {
    Page<Plant> findByOwnerUsername(String username, Pageable pageable);

    /** Locks the plant row until the transaction ends, to serialize schedule generation. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Plant p WHERE p.id = :id")
    Optional<Plant> findByIdForUpdate(@Param("id") Long id);
    
}
