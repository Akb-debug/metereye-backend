// ImageUploadRequest.java
package com.metereye.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import org.springframework.web.multipart.MultipartFile;

@Data
public class ImageUploadRequest {
    
    @NotNull(message = "L'ID du compteur est obligatoire")
    private Long meterId;
    
    private MultipartFile file;
}
