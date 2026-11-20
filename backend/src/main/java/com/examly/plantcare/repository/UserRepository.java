package com.examly.plantcare.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.examly.plantcare.entity.User;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmailIgnoreCase(String email);
    
    // Admin queries
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
    Page<User> findByRole(User.Role role, Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLogin > ?1")
    long countByLastLoginAfter(LocalDateTime date);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdDateTime > ?1")
    long countByCreatedDateAfter(LocalDateTime date);
    
    long countByRole(User.Role role);
}
