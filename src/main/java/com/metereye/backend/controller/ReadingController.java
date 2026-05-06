package com.metereye.backend.controller;

import com.metereye.backend.dto.ManualReadingRequest;
import com.metereye.backend.dto.ReadingResponse;
import com.metereye.backend.dto.SensorReadingRequest;
import com.metereye.backend.entity.User;
import com.metereye.backend.repository.UserRepository;
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
import org.springframework.security.core.Authentication;
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
    private final UserRepository userRepository;

    @PostMapping("/manual")
    @Operation(summary = "Créer un relevé manuel")
    public ResponseEntity<ReadingResponse> createManualReading(
            @Valid @RequestBody ManualReadingRequest request,
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);

        log.info("Requête relevé manuel pour compteur {} par utilisateur {}",
                request.getMeterId(), user.getId());

        ReadingResponse response = readingService.createManualReading(request, user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload")
    @Operation(summary = "Uploader une image pour relevé")
    public ResponseEntity<ReadingResponse> uploadImageReading(
            @RequestParam("meterId") Long meterId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) throws IOException {
        User user = getAuthenticatedUser(authentication);

        log.info("Upload image pour relevé compteur {} par utilisateur {}", meterId, user.getId());

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier image est obligatoire");
        }

        ReadingResponse response = readingService.createImageReading(meterId, file, user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sensor")
    @Operation(summary = "Créer un relevé capteur")
    public ResponseEntity<ReadingResponse> createSensorReading(
            @Valid @RequestBody SensorReadingRequest request,
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);

        log.info("Requête relevé capteur {} pour compteur {} par utilisateur {}",
                request.getSensorId(), request.getMeterId(), user.getId());

        ReadingResponse response = readingService.createSensorReading(request, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/meters/{meterId}")
    @Operation(summary = "Lister les relevés d'un compteur")
    public ResponseEntity<Page<ReadingResponse>> getMeterReadings(
            @Parameter(description = "ID du compteur") @PathVariable Long meterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);

        log.info("Récupération relevés compteur {} page {} size {} par utilisateur {}",
                meterId, page, size, user.getId());

        Pageable pageable = PageRequest.of(page, size);
        Page<ReadingResponse> readings = readingService.getMeterReadings(meterId, pageable, user);

        return ResponseEntity.ok(readings);
    }

    @GetMapping("/meters/{meterId}/latest")
    @Operation(summary = "Dernier relevé d'un compteur")
    public ResponseEntity<ReadingResponse> getLatestReading(
            @Parameter(description = "ID du compteur") @PathVariable Long meterId,
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);

        log.info("Récupération dernier relevé compteur {} par utilisateur {}", meterId, user.getId());

        ReadingResponse response = readingService.getLatestReading(meterId, user);
        return ResponseEntity.ok(response);
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Utilisateur non authentifié");
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté introuvable"));
    }
}