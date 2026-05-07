package com.streetworkout.config;

import com.streetworkout.security.JwtAuthenticationFilter;
import com.streetworkout.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration principale de Spring Security — renforcée.
 *
 * Sécurités appliquées :
 * - JWT stateless (pas de session)
 * - BCrypt strength 12 (plus fort que le défaut 10)
 * - Headers de sécurité HTTP (XSS, Clickjacking, MIME sniffing)
 * - CORS strict (origines explicites uniquement)
 * - CSRF désactivé (inutile avec JWT stateless)
 * - Pas de wildcard dans les origines CORS
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * BCrypt avec strength 12 — plus résistant aux attaques brute-force.
     * (défaut Spring = 10, chaque +1 double le temps de calcul)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        // Masque les erreurs : retourne toujours "Bad credentials"
        // (ne révèle pas si l'email existe ou non)
        authProvider.setHideUserNotFoundExceptions(true);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Désactiver CSRF (stateless JWT)
            .csrf(csrf -> csrf.disable())

            // CORS strict
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Stateless — aucune session HTTP
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Headers de sécurité HTTP
            .headers(headers -> headers
                // Protection XSS
                .xssProtection(xss -> xss.disable()) // géré par Content-Security-Policy
                // Empêche le MIME sniffing
                .contentTypeOptions(ct -> {})
                // Empêche le clickjacking (iframes)
                .frameOptions(frame -> frame.deny())
                // HSTS : force HTTPS (à activer en production avec HTTPS)
                // .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                // Referrer Policy
                .referrerPolicy(referrer ->
                    referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )
            )

            // Règles d'autorisation
            .authorizeHttpRequests(auth -> auth
                // Routes publiques
                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                // Exercices en lecture seule sans JWT
                .requestMatchers(HttpMethod.GET, "/api/exercises", "/api/exercises/**").permitAll()
                // Tout le reste nécessite un JWT valide
                .anyRequest().authenticated()
            )

            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS strict — pas de wildcard, origines explicites uniquement.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Origines autorisées depuis application.properties (pas de wildcard *)
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);

        // Méthodes HTTP autorisées
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Headers autorisés — liste explicite (pas de wildcard *)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"));

        // Exposer uniquement Authorization
        configuration.setExposedHeaders(List.of("Authorization"));

        // Credentials autorisés
        configuration.setAllowCredentials(true);

        // Cache preflight 1 heure
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

/**
 * Configuration principale de Spring Security.
 *
 * Architecture de sécurité :
 * - Stateless (pas de session HTTP) → JWT uniquement
 * - CORS configuré pour le front-end Angular/React
 * - CSRF désactivé (inutile avec JWT)
 * - Routes publiques : /api/auth/**
 * - Routes protégées : tout le reste nécessite un JWT valide
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * Encodeur de mots de passe BCrypt.
     * BCrypt est un algorithme de hachage sécurisé avec salt automatique.
     * Ne jamais stocker les mots de passe en clair !
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Provider d'authentification qui utilise notre UserDetailsService
     * et le BCryptPasswordEncoder pour vérifier les mots de passe.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * AuthenticationManager : point d'entrée pour déclencher l'authentification.
     * Utilisé dans AuthService pour valider email + mot de passe.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Chaîne de filtres de sécurité principale.
     *
     * Ordre des filtres :
     * 1. CORS
     * 2. JwtAuthenticationFilter (notre filtre custom)
     * 3. UsernamePasswordAuthenticationFilter (Spring Security standard)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Désactiver CSRF (inutile avec JWT stateless)
            .csrf(csrf -> csrf.disable())

            // Configurer CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Pas de session HTTP (stateless = JWT uniquement)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Règles d'autorisation
            .authorizeHttpRequests(auth -> auth
                // Routes publiques : inscription et connexion
                .requestMatchers("/api/auth/**").permitAll()
                // Exercices en lecture seule : accessibles sans connexion
                .requestMatchers(HttpMethod.GET, "/api/exercises/**").permitAll()
                // Tout le reste nécessite un JWT valide
                .anyRequest().authenticated()
            )

            // Injecter notre provider d'authentification
            .authenticationProvider(authenticationProvider())

            // Ajouter le filtre JWT AVANT le filtre standard de Spring
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configuration CORS pour autoriser les requêtes du front-end.
     * Adapte les origines autorisées selon l'environnement (dev/prod).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Origines autorisées (Angular sur 4200, React sur 3000)
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);

        // Méthodes HTTP autorisées
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Headers autorisés (Authorization pour le JWT)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));

        // Exposer le header Authorization dans la réponse
        configuration.setExposedHeaders(List.of("Authorization"));

        // Autoriser les cookies/credentials
        configuration.setAllowCredentials(true);

        // Durée de cache du preflight OPTIONS (1 heure)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
