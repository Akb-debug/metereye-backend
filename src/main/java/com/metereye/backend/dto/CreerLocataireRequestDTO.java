// ✅ CRÉÉ — CreerLocataireRequestDTO.java
package com.metereye.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreerLocataireRequestDTO {

    @NotBlank
    private String nom;

    @NotBlank
    private String prenom;

    @NotBlank
    @Email
    private String email;

    private String telephone;

    @NotNull
    private Long sousCompteurId;

    private String motDePasseTemporaire;
}
