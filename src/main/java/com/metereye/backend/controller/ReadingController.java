// ReadingController.java
package com.metereye.backend.controller;

import com.metereye.backend.dto.ManualReadingRequest;
import com.metereye.backend.dto.ReadingResponse;
import com.metereye.backend.dto.SensorReadingRequest;
import com.metereye.backend.entity.User;
import com.metereye.backend.service.ReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/readings")
@RequiredArgsConstructor
@Tag(name = "Relevés", description = "Gestion des relevés de compteurs")
public class ReadingController {

    private final ReadingService readingService;

    @PostMapping("/manual")
    @Operation(summary = "Créer un relevé manuel", description = "Enregistre un relevé saisi manuellement")
    public ResponseEntity<ReadingResponse> createManualReading(
            @Valid @RequestBody ManualReadingRequest request,
            @AuthenticationPrincipal User user) {
        
        log.info("Requête relevé manuel pour compteur {} par utilisateur {}", 
                request.getMeterId(), user.getId());
        
        ReadingResponse response = readingService.createManualReading(request, user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload")
    @Operation(summary = "Uploader une image pour relevé", description = "Enregistre un relevé via image ESP32-CAM")
    public ResponseEntity<ReadingResponse> uploadImageReading(
            @RequestParam("meterId") Long meterId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) throws IOException {
        
        log.info("Upload image pour relevé compteur {} par utilisateur {}", meterId, user.getId());
        
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier image est obligatoire");
        }

        ReadingResponse response = readingService.createImageReading(meterId, file, user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sensor")
    @Operation(summary = "Créer un relevé capteur", description = "Enregistre un relevé provenant d'un capteur")
    public ResponseEntity<ReadingResponse> createSensorReading(
            @Valid @RequestBody SensorReadingRequest request,
            @AuthenticationPrincipal User user) {
        
        log.info("Requête relevé capteur {} pour compteur {} par utilisateur {}", 
                request.getSensorId(), request.getMeterId(), user.getId());
        
        ReadingResponse response = readingService.createSensorReading(request, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/meters/{meterId}")
    @Operation(summary = "Lister les relevés d'un compteur", description = "Récupère les relevés paginés pour un compteur")
    public ResponseEntity<Page<ReadingResponse>> getMeterReadings(
            @Parameter(description = "ID du compteur") @PathVariable Long meterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String source,
            @AuthenticationPrincipal User user) {
        
        log.info("Récupération relevés compteur {} page {} size {} par utilisateur {}", 
                meterId, page, size, user.getId());
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ReadingResponse> readings = readingService.getMeterReadings(meterId, pageable, user);
        
        return ResponseEntity.ok(readings);
    }

    @GetMapping("/meters/{meterId}/latest")
    @Operation(summary = "Dernier relevé d'un compteur", description = "Récupère le dernier relevé enregistré pour un compteur")
    public ResponseEntity<ReadingResponse> getLatestReading(
            @Parameter(description = "ID du compteur") @PathVariable Long meterId,
            @AuthenticationPrincipal User user) {
        
        log.info("Récupération dernier relevé compteur {} par utilisateur {}", meterId, user.getId());
        
        ReadingResponse response = readingService.getLatestReading(meterId, user);
        return ResponseEntity.ok(response);
    }
}
