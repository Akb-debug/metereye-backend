package com.metereye.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDTO {

    private Long id;

    private String canal;

    private String status;

    private String titre;

    private String message;

    private LocalDateTime dateEnvoi;
}