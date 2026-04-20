// AuthController.java
package com.metereye.backend.controller;

import com.metereye.backend.dto.AuthResponse;
import com.metereye.backend.dto.LoginRequest;
import com.metereye.backend.dto.RegisterRequest;
import com.metereye.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint d'inscription
     * @param request Les informations d'inscription
     * @return La réponse d'authentification
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint de connexion
     * @param request Les informations de connexion
     * @return La réponse d'authentification
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint pour récupérer les informations de l'utilisateur connecté
     * @param request La requête HTTP
     * @return Les informations de l'utilisateur
     */
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getMe(@RequestHeader("Authorization") String authorizationHeader) {
        // Extraire le token du header Authorization
        String token = authorizationHeader.substring(7);
        AuthResponse response = authService.getMe(token);
        return ResponseEntity.ok(response);
    }

    /**
     * Gestionnaire d'exceptions global
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex, WebRequest request) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        errorResponse.put("status", "400");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex, WebRequest request) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Une erreur est survenue: " + ex.getMessage());
        errorResponse.put("status", "500");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}