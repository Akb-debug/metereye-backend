package com.metereye.backend.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceResponseDTO {

    private Boolean pushEnabled;
    private Boolean emailEnabled;
    private Boolean smsEnabled;

    private Boolean creditAlerts;
    private Boolean anomalyAlerts;
    private Boolean systemAlerts;
}