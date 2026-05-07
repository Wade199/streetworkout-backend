package com.streetworkout.service;

import com.streetworkout.dto.UserResponse;
import com.streetworkout.entity.User;
import com.streetworkout.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Service gérant le profil utilisateur.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Récupère l'utilisateur actuellement connecté depuis le SecurityContext.
     * L'email est extrait du JWT par le JwtAuthenticationFilter.
     */
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    /**
     * Retourne le profil de l'utilisateur connecté.
     */
    public UserResponse getProfile() {
        return UserResponse.fromEntity(getCurrentUser());
    }

    /**
     * Met à jour le profil de l'utilisateur connecté.
     * Seuls les champs fournis sont mis à jour (mise à jour partielle).
     */
    @Transactional
    public UserResponse updateProfile(Map<String, Object> updates) {
        User user = getCurrentUser();

        if (updates.containsKey("firstName")) {
            user.setFirstName((String) updates.get("firstName"));
        }
        if (updates.containsKey("lastName")) {
            user.setLastName((String) updates.get("lastName"));
        }
        if (updates.containsKey("height")) {
            user.setHeight(new BigDecimal(updates.get("height").toString()));
        }
        if (updates.containsKey("weight")) {
            user.setWeight(new BigDecimal(updates.get("weight").toString()));
        }
        if (updates.containsKey("password")) {
            user.setPassword(passwordEncoder.encode((String) updates.get("password")));
        }

        return UserResponse.fromEntity(userRepository.save(user));
    }
}
