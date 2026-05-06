package com.metereye.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlerteResponseDTO {

    private Long id;

    private String typeAlerte;

    private String message;

    private Boolean lue;

    private LocalDateTime dateCreation;

    private Long compteurId;

    private String compteurReference;
}