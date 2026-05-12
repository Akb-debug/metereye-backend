// UpdateProfileRequest.java
package com.metereye.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    private String nom;

    private String prenom;

    @Email(message = "Format d'email invalide")
    private String email;

    private String telephone;

    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=?!])(?=\\S+$).{8,}$",
            message = "Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial")
    private String motDePasse;

    private Double seuilAlerteCredit;

    private Double seuilAlerteAnomalie;

    private Boolean notificationPush;

    private Boolean notificationSms;

    private Boolean notificationEmail;
}
