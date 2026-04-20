// DeviceScanDTO.java
package com.metereye.backend.dto;

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
public class DeviceScanDTO {

    @NotBlank(message = "Le QR code est obligatoire")
    private String qrCode;

    @NotNull(message = "L'ID utilisateur est obligatoire")
    private Long userId;
}
