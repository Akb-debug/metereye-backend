package com.metereye.backend.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTokenRequestDTO {

    private String token;

    private String platform;
}