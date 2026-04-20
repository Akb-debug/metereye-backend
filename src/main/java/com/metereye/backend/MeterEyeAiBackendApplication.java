package com.metereye.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MeterEyeAiBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeterEyeAiBackendApplication.class, args);
        System.out.println("=== MeterEye AI Backend démarré avec succès ===");
        System.out.println("=== Authentification disponible sur /api/auth ===");
    }

}
