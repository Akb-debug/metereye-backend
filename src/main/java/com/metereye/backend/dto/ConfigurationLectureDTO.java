// ConfigurationLectureDTO.java
package com.metereye.backend.dto;

import com.metereye.backend.enums.ModeLectureCompteur;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationLectureDTO {

    @NotNull(message = "Le mode de lecture est obligatoire")
    private ModeLectureCompteur modeLecture;

    private String commentaire;
}
