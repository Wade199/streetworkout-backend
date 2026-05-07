package com.streetworkout.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Composant responsable de la création, validation et extraction des JWT.
 *
 * Un JWT (JSON Web Token) est composé de 3 parties séparées par des points :
 *   Header.Payload.Signature
 *
 * - Header : algorithme de signature (HS256)
 * - Payload : données (userId, email, expiration)
 * - Signature : garantit l'intégrité du token
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    /**
     * Génère la clé de signature à partir du secret configuré.
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Génère un token JWT à partir de l'objet Authentication de Spring Security.
     * Appelé après une connexion réussie.
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return buildToken(userDetails.getUsername());
    }

    /**
     * Génère un token JWT directement depuis un email.
     * Utilisé lors de l'inscription.
     */
    public String generateTokenFromEmail(String email) {
        return buildToken(email);
    }

    private String buildToken(String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
            .setSubject(subject)          // email de l'utilisateur
            .setIssuedAt(now)             // date de création
            .setExpiration(expiryDate)    // date d'expiration (24h par défaut)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * Extrait l'email (subject) depuis un token JWT.
     */
    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

    /**
     * Valide un token JWT.
     * Retourne true si le token est valide, false sinon.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Token JWT invalide : {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Token JWT expiré : {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Token JWT non supporté : {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims vide : {}", e.getMessage());
        }
        return false;
    }
}
