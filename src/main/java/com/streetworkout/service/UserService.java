package com.streetworkout.service;

import com.streetworkout.dto.UpdateProfileRequest;
import com.streetworkout.dto.UserResponse;
import com.streetworkout.entity.User;
import com.streetworkout.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Recupere l'utilisateur connecte depuis le SecurityContext (extrait du JWT).
     */
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouve"));
    }

    /**
     * Retourne le profil de l'utilisateur connecte.
     */
    @Transactional(readOnly = true)
    public UserResponse getProfile() {
        return UserResponse.fromEntity(getCurrentUser());
    }

    /**
     * Met a jour le profil — seuls les champs non-null sont modifies.
     * Validation via UpdateProfileRequest (@Valid dans le controller).
     */
    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName().trim());
        }
        if (request.getHeight() != null) {
            if (request.getHeight().doubleValue() < 50 || request.getHeight().doubleValue() > 300) {
                throw new RuntimeException("La taille doit etre entre 50 et 300 cm");
            }
            user.setHeight(request.getHeight());
        }
        if (request.getWeight() != null) {
            if (request.getWeight().doubleValue() < 20 || request.getWeight().doubleValue() > 500) {
                throw new RuntimeException("Le poids doit etre entre 20 et 500 kg");
            }
            user.setWeight(request.getWeight());
        }
        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return UserResponse.fromEntity(userRepository.save(user));
    }

    /**
     * Supprime le compte de l'utilisateur connecte (RGPD).
     * Supprime en cascade toutes ses donnees (workouts, progress).
     */
    @Transactional
    public void deleteAccount() {
        User user = getCurrentUser();
        userRepository.delete(user);
    }
}
