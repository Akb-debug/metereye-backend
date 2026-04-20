// ImageController.java
package com.metereye.backend.controller;

import com.metereye.backend.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Tag(name = "Images", description = "Gestion des images de relevés")
public class ImageController {

    private final ImageService imageService;

    @GetMapping("/{imageId}")
    @Operation(summary = "Télécharger une image", description = "Retourne le fichier image d'un relevé")
    public ResponseEntity<Resource> downloadImage(
            @Parameter(description = "ID de l'image") @PathVariable Long imageId) throws IOException {
        
        log.info("Téléchargement image {}", imageId);
        
        var image = imageService.getImage(imageId);
        Path filePath = Paths.get(image.getFilePath());
        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("Image non trouvée ou non lisible");
        }

        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "inline; filename=\"" + image.getFileName() + "\"")
                .body(resource);
    }
}
