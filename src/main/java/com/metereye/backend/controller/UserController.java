// UserController.java
package com.metereye.backend.controller;

import com.metereye.backend.dto.UserProfileDTO;
import com.metereye.backend.entity.User;
import com.metereye.backend.mapper.UserMapper;
import com.metereye.backend.repository.UserRepository;
import com.metereye.backend.utils.BaseResponse;
import lombok.RequiredArgsConstructor;
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