// ✅ CRÉÉ — CreerLocataireResponseDTO.java
package com.metereye.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreerLocataireResponseDTO {

    private Long locataireId;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasseTemporaire;
    private String identifiantConnexion;
    private Long sousCompteurId;
    private String sousCompteurReference;
}
