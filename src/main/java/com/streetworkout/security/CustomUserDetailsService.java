package com.streetworkout.security;

import com.streetworkout.entity.User;
import com.streetworkout.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Service qui charge les détails d'un utilisateur depuis la base de données.
 * Utilisé par Spring Security pour l'authentification.
 *
 * Le "username" dans Spring Security correspond ici à l'EMAIL de l'utilisateur.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Charge un utilisateur par son email.
     * Appelé automatiquement par Spring Security lors de l'authentification.
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(
                "Utilisateur non trouvé avec l'email : " + email
            ));

        // Retourne un UserDetails Spring Security standard
        // Collections.emptyList() = pas de rôles pour le MVP
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            Collections.emptyList()
        );
    }
}
