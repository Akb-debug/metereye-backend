// FileUploadConfig.java
package com.metereye.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${app.upload.directory:uploads}")
    private String uploadDirectory;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Créer le répertoire d'upload s'il n'existe pas
        try {
            Path uploadPath = Paths.get(uploadDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le répertoire d'upload", e);
        }

        // Configurer le handler pour servir les fichiers
        registry.addResourceHandler("/api/images/**")
                .addResourceLocations("file:" + uploadDirectory + "/");
    }
}
