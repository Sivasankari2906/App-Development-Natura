package com.examly.plantcare.repository;

import com.examly.plantcare.entity.Plant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlantRepository extends JpaRepository<Plant, Long> {
    Page<Plant> findByOwnerUsername(String username, Pageable pageable);
    
}
