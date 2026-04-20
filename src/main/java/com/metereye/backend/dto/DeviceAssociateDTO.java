// DeviceAssociateDTO.java
package com.metereye.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceAssociateDTO {

    @NotNull(message = "L'ID du compteur est obligatoire")
    private Long compteurId;

    @Positive(message = "L'intervalle de capture doit être positif")
    private Integer captureInterval = 3600; // 1h par défaut
}
