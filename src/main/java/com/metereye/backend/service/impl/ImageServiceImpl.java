// ImageServiceImpl.java
package com.metereye.backend.service.impl;

import com.metereye.backend.entity.Image;
import com.metereye.backend.entity.Releve;
import com.metereye.backend.repository.ImageRepository;
import com.metereye.backend.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    
    @Value("${app.upload.directory:uploads}")
    private String uploadDirectory;

    @Override
    public Image saveImage(MultipartFile file, Releve releve) throws IOException {
        // Créer le répertoire si nécessaire
        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Générer un nom de fichier unique
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        // Sauvegarder le fichier
        Files.copy(file.getInputStream(), filePath);

        // Créer l'entité Image
        Image image = Image.builder()
                .releve(releve)
                .fileName(fileName)
                .filePath(filePath.toString())
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .uploadDate(LocalDateTime.now())
                .processed(false)
                .build();

        return imageRepository.save(image);
    }

    @Override
    public String getImageUrl(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image non trouvée"));
        
        return "/api/images/" + imageId;
    }

    @Override
    public Image getImage(Long imageId) {
        return imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image non trouvée"));
    }

    @Override
    public void processImageWithOCR(Long imageId) {
        // TODO: Implémenter le traitement OCR
        // Pour l'instant, on simule avec des valeurs par défaut
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image non trouvée"));
        
        try {
            // Simulation OCR (à remplacer par Tesseract ou autre)
            image.setOcrValue(1234.5);
            image.setOcrConfidence(0.95);
            image.setProcessed(true);
            
            imageRepository.save(image);
            log.info("OCR traité pour l'image {}", imageId);
        } catch (Exception e) {
            image.setProcessingError("Erreur OCR: " + e.getMessage());
            imageRepository.save(image);
            log.error("Erreur lors du traitement OCR de l'image {}", imageId, e);
        }
    }

    @Override
    public void deleteImage(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image non trouvée"));
        
        try {
            // Supprimer le fichier physique
            Files.deleteIfExists(Paths.get(image.getFilePath()));
            
            // Supprimer l'entité
            imageRepository.delete(image);
            
            log.info("Image {} supprimée avec succès", imageId);
        } catch (IOException e) {
            log.error("Erreur lors de la suppression de l'image {}", imageId, e);
            throw new RuntimeException("Erreur lors de la suppression de l'image", e);
        }
    }
}
