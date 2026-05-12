package com.streetworkout.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO pour la mise a jour du profil utilisateur.
 * Tous les champs sont optionnels — seuls les champs non-null sont mis a jour.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(min = 1, max = 50, message = "Le prenom doit contenir entre 1 et 50 caracteres")
    private String firstName;

    @Size(min = 1, max = 50, message = "Le nom doit contenir entre 1 et 50 caracteres")
    private String lastName;

    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caracteres")
    private String password;

    private BigDecimal height;
    private BigDecimal weight;
}
