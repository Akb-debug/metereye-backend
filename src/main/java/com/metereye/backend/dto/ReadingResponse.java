// ReadingResponse.java
package com.metereye.backend.dto;

import com.metereye.backend.enums.SourceReleve;
import com.metereye.backend.enums.StatutReleve;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReadingResponse {
    
    private Long id;
    private Double value;
    private LocalDateTime dateTime;
    private SourceReleve source;
    private StatutReleve statut;
    private Double consumption;
    private String comment;
    private String imageUrl;
    private Double ocrConfidence;
    private Long meterId;
    private String meterReference;
    
    // Méthodes utilitaires pour la compatibilité
    public void setConsommation(Double consommation) {
        this.consumption = consommation;
    }
    
    public void setCommentaire(String commentaire) {
        this.comment = commentaire;
    }
}
