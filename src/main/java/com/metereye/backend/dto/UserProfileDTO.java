// UserProfileDTO.java
package com.metereye.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String nomComplet;
    private String telephone;
    private Double seuilAlerteCredit;
    private Double seuilAlerteAnomalie;
    private Boolean notificationPush;
    private Boolean notificationSms;
    private Boolean notificationEmail;
}