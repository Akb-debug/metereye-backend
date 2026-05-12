// UserController.java
package com.metereye.backend.controller;

import com.metereye.backend.dto.UpdateProfileRequest;
import com.metereye.backend.dto.UserProfileDTO;
import com.metereye.backend.entity.User;
import com.metereye.backend.mapper.UserMapper;
import com.metereye.backend.repository.UserRepository;
import com.metereye.backend.utils.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Récupérer le profil de l'utilisateur connecté
     * GET /api/users/profile
     */
    @GetMapping("/profile")
    public BaseResponse<UserProfileDTO> getProfile() {
        try {
            User currentUser = getCurrentUser();
            UserProfileDTO profile = userMapper.toProfileDTO(currentUser);
            return BaseResponse.success(profile);
        } catch (Exception e) {
            return BaseResponse.error(500, e.getMessage());
        }
    }

    /**
     * Mettre à jour le profil de l'utilisateur connecté
     * PUT /api/users/profile
     */
    @PutMapping("/profile")
    public BaseResponse<UserProfileDTO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        try {
            User currentUser = getCurrentUser();
            
            // Mise à jour des informations de base
            if (request.getNom() != null) {
                currentUser.setNom(request.getNom());
            }
            if (request.getPrenom() != null) {
                currentUser.setPrenom(request.getPrenom());
            }
            if (request.getEmail() != null) {
                // Vérifier si l'email n'est pas déjà utilisé par un autre utilisateur
                if (!currentUser.getEmail().equals(request.getEmail()) && 
                    userRepository.findByEmail(request.getEmail()).isPresent()) {
                    return BaseResponse.badRequest("Cet email est déjà utilisé par un autre utilisateur");
                }
                currentUser.setEmail(request.getEmail());
            }
            if (request.getTelephone() != null) {
                currentUser.setTelephone(request.getTelephone());
            }
            
            // Mise à jour du mot de passe si fourni
            if (request.getMotDePasse() != null && !request.getMotDePasse().trim().isEmpty()) {
                currentUser.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
            }
            
            // Mise à jour des seuils d'alerte
            if (request.getSeuilAlerteCredit() != null) {
                currentUser.setSeuilAlerteCredit(request.getSeuilAlerteCredit());
            }
            if (request.getSeuilAlerteAnomalie() != null) {
                currentUser.setSeuilAlerteAnomalie(request.getSeuilAlerteAnomalie());
            }
            
            // Mise à jour des préférences de notification
            if (request.getNotificationPush() != null) {
                currentUser.setNotificationPush(request.getNotificationPush());
            }
            if (request.getNotificationSms() != null) {
                currentUser.setNotificationSms(request.getNotificationSms());
            }
            if (request.getNotificationEmail() != null) {
                currentUser.setNotificationEmail(request.getNotificationEmail());
            }
            
            User saved = userRepository.save(currentUser);
            return BaseResponse.success("Profil mis à jour avec succès", userMapper.toProfileDTO(saved));
        } catch (Exception e) {
            return BaseResponse.error(500, e.getMessage());
        }
    }

    /**
     * Mettre à jour les seuils d'alerte
     * PUT /api/users/seuils
     */
    @PutMapping("/seuils")
    public BaseResponse<UserProfileDTO> updateSeuils(
            @RequestParam Double seuilCredit,
            @RequestParam Double seuilAnomalie) {
        try {
            User currentUser = getCurrentUser();
            currentUser.setSeuilAlerteCredit(seuilCredit);
            currentUser.setSeuilAlerteAnomalie(seuilAnomalie);
            User saved = userRepository.save(currentUser);
            return BaseResponse.success(userMapper.toProfileDTO(saved));
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

    /**
     * Mettre à jour les préférences de notification
     * PUT /api/users/notifications
     */
    @PutMapping("/notifications")
    public BaseResponse<UserProfileDTO> updateNotifications(
            @RequestParam(required = false) Boolean push,
            @RequestParam(required = false) Boolean sms,
            @RequestParam(required = false) Boolean email) {
        try {
            User currentUser = getCurrentUser();
            if (push != null) currentUser.setNotificationPush(push);
            if (sms != null) currentUser.setNotificationSms(sms);
            if (email != null) currentUser.setNotificationEmail(email);
            User saved = userRepository.save(currentUser);
            return BaseResponse.success(userMapper.toProfileDTO(saved));
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }
}