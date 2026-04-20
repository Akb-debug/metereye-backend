// AlerteResponseDTO.java
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
public class AlerteResponseDTO {
    private Long id;
    private String typeAlerte;
    private String message;
    private String canal;
    private Boolean lue;
    private Boolean envoyee;
    private LocalDateTime dateCreation;
    private LocalDateTime dateEnvoi;
    private Long compteurId;
    private String compteurReference;
}