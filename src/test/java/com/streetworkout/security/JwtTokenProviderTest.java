package com.streetworkout.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Le secret JWT est la clé de toute l'authentification : un secret faible ou copié
 * d'un exemple permet de forger des tokens pour n'importe quel compte.
 */
class JwtTokenProviderTest {

    private static final String STRONG_SECRET = "k3Jf9sL0pQ2xVb7nT5yHcR8uW1eZaM4dG6oXiNqPvS9tYjBwAhUlCfDrEgKz2mO7";

    private JwtTokenProvider providerWith(String secret) {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", secret);
        ReflectionTestUtils.setField(provider, "jwtExpirationMs", 60_000L);
        return provider;
    }

    @Test
    void acceptsLongRandomSecret() {
        assertDoesNotThrow(() -> providerWith(STRONG_SECRET).validateSecret());
    }

    @Test
    void rejectsSecretShorterThan256Bits() {
        assertThrows(IllegalStateException.class, () -> providerWith("trop-court").validateSecret());
    }

    @Test
    void rejectsNullSecret() {
        assertThrows(IllegalStateException.class, () -> providerWith(null).validateSecret());
    }

    @Test
    void rejectsExampleLookingSecret() {
        String documentationStyle = "VotreCleSecreteDeMinimum256BitsIciPourLApplication2024";
        assertThrows(IllegalStateException.class, () -> providerWith(documentationStyle).validateSecret());
    }

    @Test
    void tokenRoundTripKeepsSubject() {
        JwtTokenProvider provider = providerWith(STRONG_SECRET);
        String token = provider.generateTokenFromEmail("user@example.org");
        assertTrue(provider.validateToken(token));
        assertEquals("user@example.org", provider.getEmailFromToken(token));
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        String forged = providerWith("Z9yX8wV7uT6sR5qP4oN3mL2kJ1iH0gF9eD8cB7aA6zY5xW4vU3tS2rQ1pO0nM9l").generateTokenFromEmail("victim@example.org");
        assertFalse(providerWith(STRONG_SECRET).validateToken(forged));
    }

    @Test
    void garbageTokenIsRejected() {
        assertFalse(providerWith(STRONG_SECRET).validateToken("not.a.jwt"));
    }
}
