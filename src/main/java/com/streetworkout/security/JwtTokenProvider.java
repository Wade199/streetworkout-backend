package com.streetworkout.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Composant responsable de la création, validation et extraction des JWT.
 * Sécurité renforcée : clé encodée en UTF-8, algorithme HS256, logs sans données sensibles.
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    /**
     * Génère la clé de signature HMAC-SHA256.
     * Utilise UTF-8 pour garantir la cohérence entre les environnements.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Génère un token JWT à partir de l'objet Authentication de Spring Security.
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return buildToken(userDetails.getUsername());
    }

    /**
     * Génère un token JWT directement depuis un email.
     */
    public String generateTokenFromEmail(String email) {
        return buildToken(email);
    }

    private String buildToken(String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
            .setSubject(subject)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * Extrait l'email depuis un token JWT.
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
     * Les logs ne contiennent jamais le token lui-même pour éviter les fuites.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            logger.warn("Token JWT malformé");
        } catch (ExpiredJwtException e) {
            logger.warn("Token JWT expiré");
        } catch (UnsupportedJwtException e) {
            logger.warn("Token JWT non supporté");
        } catch (IllegalArgumentException e) {
            logger.warn("Token JWT vide ou null");
        } catch (Exception e) {
            logger.warn("Erreur de validation JWT");
        }
        return false;
    }
}
