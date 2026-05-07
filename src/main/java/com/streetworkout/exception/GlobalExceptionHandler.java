package com.streetworkout.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire global des exceptions — sécurisé.
 *
 * Règles de sécurité appliquées :
 * - Les messages d'erreur ne révèlent jamais les détails internes
 * - Les erreurs 500 ne retournent pas la stack trace au client
 * - Les erreurs d'auth retournent toujours le même message (anti-enumération)
 * - Les logs internes sont détaillés mais les réponses client sont génériques
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Erreurs de validation des DTOs (@Valid).
     * Retourne les champs invalides au client (utile pour le front-end).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "timestamp", LocalDateTime.now(),
            "status", HttpStatus.BAD_REQUEST.value(),
            "error", "Validation échouée",
            "errors", fieldErrors
        ));
    }

    /**
     * Mauvais email ou mot de passe.
     * Message générique pour ne pas révéler si l'email existe.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        // Log interne détaillé
        logger.warn("Tentative de connexion échouée");
        // Réponse générique au client (ne révèle pas si l'email existe)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
            "timestamp", LocalDateTime.now(),
            "status", HttpStatus.UNAUTHORIZED.value(),
            "error", "Unauthorized",
            "message", "Email ou mot de passe incorrect"
        ));
    }

    /**
     * Accès refusé (ressource d'un autre utilisateur).
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AuthorizationDeniedException ex) {
        logger.warn("Accès refusé : {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            "timestamp", LocalDateTime.now(),
            "status", HttpStatus.FORBIDDEN.value(),
            "error", "Forbidden",
            "message", "Accès refusé"
        ));
    }

    /**
     * Erreurs métier (ressource non trouvée, email déjà utilisé, etc.).
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        logger.warn("Erreur métier : {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "timestamp", LocalDateTime.now(),
            "status", HttpStatus.BAD_REQUEST.value(),
            "error", "Bad Request",
            "message", ex.getMessage()
        ));
    }

    /**
     * Erreurs inattendues — ne jamais exposer les détails au client.
     * La stack trace est loggée en interne uniquement.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        // Log complet en interne pour le debug
        logger.error("Erreur interne non gérée", ex);
        // Réponse générique au client (pas de stack trace !)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "timestamp", LocalDateTime.now(),
            "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "error", "Internal Server Error",
            "message", "Une erreur inattendue s'est produite"
        ));
    }
}

/**
 * Gestionnaire global des exceptions.
 *
 * Intercepte toutes les exceptions levées dans les controllers
 * et retourne des réponses JSON standardisées.
 *
 * Format de réponse d'erreur :
 * {
 *   "timestamp": "2024-01-15T10:30:00",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Message d'erreur détaillé",
 *   "errors": { ... } // optionnel pour les erreurs de validation
 * }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gère les erreurs de validation (@Valid sur les DTOs).
     *
     * Exemple : champ email invalide, mot de passe trop court, etc.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("message", "Erreur de validation des données");
        response.put("errors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Gère les erreurs d'authentification (mauvais email/mot de passe).
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("error", "Unauthorized");
        response.put("message", "Email ou mot de passe incorrect");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Gère les RuntimeException génériques (ex: "Exercice non trouvé").
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Bad Request");
        response.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Gère toutes les autres exceptions non prévues.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal Server Error");
        response.put("message", "Une erreur inattendue s'est produite");

        // Log l'erreur pour le debug (en production, utiliser un vrai logger)
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
