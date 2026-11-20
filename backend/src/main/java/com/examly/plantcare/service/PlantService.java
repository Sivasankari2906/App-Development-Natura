package com.examly.plantcare.service;

import com.examly.plantcare.entity.Plant;
import com.examly.plantcare.entity.User;
import com.examly.plantcare.repository.PlantRepository;
import com.examly.plantcare.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.examly.plantcare.util.Access;

import java.util.*;

@Service
public class PlantService {

    private final PlantRepository plantRepository;
    private final UserRepository userRepository;

    @Autowired
    public PlantService(PlantRepository plantRepository, UserRepository userRepository) {
        this.plantRepository = plantRepository;
        this.userRepository = userRepository;
    }

    // ✅ Basic CRUD
    public Page<Plant> getAllPlants(Pageable pageable) {
        if (Access.isAdmin()) {
            return plantRepository.findAll(pageable);
        }
        return plantRepository.findByOwnerUsername(Access.currentUsername(), pageable);
    }

    public Optional<Plant> getPlantById(Long id) {
        return plantRepository.findById(id).filter(Access::canAccess);
    }

    public Plant savePlant(Plant plant) {
        return plantRepository.save(plant);
    }

    public Plant updatePlant(Long id, Plant plantDetails) {
        return plantRepository.findById(id)
                .filter(Access::canAccess)
                .map(plant -> {
                    plant.setNickname(plantDetails.getNickname());
                    plant.setLocation(plantDetails.getLocation());
                    plant.setHealthStatus(plantDetails.getHealthStatus());
                    plant.setNotes(plantDetails.getNotes());
                    return plantRepository.save(plant);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plant not found"));
    }

    public void deletePlant(Long id) {
        Plant plant = plantRepository.findById(id)
                .filter(Access::canAccess)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plant not found"));
        plantRepository.delete(plant);
    }

    public Plant createPlantForLoggedInUser(String username, Plant plant) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        plant.setOwner(user);
        return plantRepository.save(plant);
    }

    // // ✅ Advanced methods
    // public List<Plant> searchPlants(String query, Map<String, String> filters) {
    //     return plantRepository.findAll(); // placeholder
    // }

    // public void addPhoto(Long plantId, String photoUrl) {
    //     Plant plant = plantRepository.findById(plantId)
    //             .orElseThrow(() -> new RuntimeException("Plant not found"));
    //     List<String> photos = Optional.ofNullable(plant.getPhotos()).orElse(new ArrayList<>());
    //     photos.add(photoUrl);
    //     plant.setPhotos(photos);
    //     plantRepository.save(plant);
    // }

    // public List<String> getPhotos(Long plantId) {
    //     return plantRepository.findById(plantId)
    //             .map(Plant::getPhotos)
    //             .orElse(Collections.emptyList());
    // }

    // public Plant updateHealthStatus(Long plantId, String status) {
    //     Plant plant = plantRepository.findById(plantId)
    //             .orElseThrow(() -> new RuntimeException("Plant not found"));
    //     plant.setHealthStatus(status);
    //     return plantRepository.save(plant);
    // }

    // public List<String> getCareHistory(Long plantId) {
    //     return Arrays.asList("Watered on 2025-09-01", "Fertilized on 2025-08-20");
    // }

    // public Plant addNotes(Long plantId, String notes) {
    //     Plant plant = plantRepository.findById(plantId)
    //             .orElseThrow(() -> new RuntimeException("Plant not found"));
    //     plant.setNotes(notes);
    //     return plantRepository.save(plant);
    // }

    // public Map<String, Object> getDashboardSummary() {
    //     Map<String, Object> summary = new HashMap<>();
    //     summary.put("totalPlants", plantRepository.count());
    //     summary.put("healthyPlants", plantRepository.findAll().stream()
    //             .filter(p -> "HEALTHY".equalsIgnoreCase(p.getHealthStatus()))
    //             .count());
    //     return summary;
    // }

    // public List<Plant> getPlantsNeedingAttention() {
    //     // Example: Plants with "SICK" or "LOW_WATER"
    //     return plantRepository.findAll().stream()
    //             .filter(p -> !"HEALTHY".equalsIgnoreCase(p.getHealthStatus()))
    //             .toList();
    // }

    // public void bulkUpdate(List<Plant> plants) {
    //     plantRepository.saveAll(plants);
    // }
}
