package com.examly.plantcare.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.examly.plantcare.entity.Plant;

/** Ownership checks based on the authenticated user of the current request. */
public final class Access {

    private Access() {
    }

    public static String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : auth.getName();
    }

    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    /** True if the current user is an admin or owns the plant. */
    public static boolean canAccess(Plant plant) {
        if (plant == null) {
            return false;
        }
        if (isAdmin()) {
            return true;
        }
        String username = currentUsername();
        return username != null && plant.getOwner() != null
                && username.equals(plant.getOwner().getUsername());
    }
}
