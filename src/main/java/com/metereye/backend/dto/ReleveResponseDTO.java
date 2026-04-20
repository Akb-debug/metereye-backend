// ReleveResponseDTO.java
package com.metereye.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleveResponseDTO {
    private Long id;
    private Double valeur;
    private LocalDateTime dateTime;
    private Double consommationCalculee;
    private String source;
    private String statut;
    private String commentaire;
    private String imageUrl;
    private Long compteurId;
    private String compteurReference;
}