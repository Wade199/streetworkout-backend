package com.streetworkout.service;

import com.streetworkout.dto.AuthResponse;
import com.streetworkout.dto.LoginRequest;
import com.streetworkout.dto.RegisterRequest;
import com.streetworkout.dto.UserResponse;
import com.streetworkout.entity.User;
import com.streetworkout.repository.UserRepository;
import com.streetworkout.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service gérant l'authentification : inscription et connexion.
 *
 * Flux d'inscription :
 * 1. Vérifier que l'email/username n'existe pas déjà
 * 2. Hasher le mot de passe avec BCrypt
 * 3. Sauvegarder l'utilisateur en base
 * 4. Générer un JWT et le retourner
 *
 * Flux de connexion :
 * 1. AuthenticationManager vérifie email + mot de passe
 * 2. Si valide, générer un JWT
 * 3. Retourner le JWT + infos utilisateur
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * Inscription d'un nouvel utilisateur.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Vérifier l'unicité de l'email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        // Vérifier l'unicité du username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Ce nom d'utilisateur est déjà pris");
        }

        // Créer l'entité User
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        // Hachage BCrypt du mot de passe (jamais en clair en base !)
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setHeight(request.getHeight());
        user.setWeight(request.getWeight());

        User savedUser = userRepository.save(user);

        // Générer le JWT directement (pas besoin de re-authentifier)
        String token = tokenProvider.generateTokenFromEmail(savedUser.getEmail());

        return new AuthResponse(token, UserResponse.fromEntity(savedUser));
    }

    /**
     * Connexion d'un utilisateur existant.
     */
    public AuthResponse login(LoginRequest request) {
        // Spring Security vérifie email + mot de passe via BCrypt
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Injecter l'authentification dans le contexte Spring Security
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Générer le JWT
        String token = tokenProvider.generateToken(authentication);

        // Récupérer les infos utilisateur pour la réponse
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        return new AuthResponse(token, UserResponse.fromEntity(user));
    }
}
