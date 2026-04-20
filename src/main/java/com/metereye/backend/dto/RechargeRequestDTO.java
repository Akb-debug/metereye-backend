// RechargeRequestDTO.java
package com.metereye.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechargeRequestDTO {

    @NotNull(message = "L'ID du compteur est obligatoire")
    private Long compteurId;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private Double montant;

    @Size(min = 10, max = 20, message = "Le code de recharge doit contenir entre 10 et 20 caractères")
    private String codeRecharge;
}