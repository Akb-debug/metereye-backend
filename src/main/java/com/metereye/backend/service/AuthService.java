// AuthService.java
package com.metereye.backend.service;

import com.metereye.backend.dto.AuthResponse;
import com.metereye.backend.dto.LoginRequest;
import com.metereye.backend.dto.RegisterRequest;
import com.metereye.backend.entity.Role;
import com.metereye.backend.entity.Token;
import com.metereye.backend.entity.User;
import com.metereye.backend.enums.RoleName;
import com.metereye.backend.repository.RoleRepository;
import com.metereye.backend.repository.TokenRepository;
import com.metereye.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Enregistre un nouvel utilisateur dans le système
     * @param request Les informations d'inscription
     * @return La réponse d'authentification avec le token JWT
     * @throws RuntimeException Si l'email existe déjà ou si le rôle est invalide
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un compte avec cet email existe déjà");
        }

        // Seul ADMIN ne peut pas s'auto-attribuer lors de l'inscription
        RoleName roleName;
        try {
            roleName = RoleName.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Rôle invalide. Les rôles valides sont: PERSONNEL, PROPRIETAIRE, LOCATAIRE");
        }
        if (roleName == RoleName.ADMIN) {
            throw new RuntimeException("L'auto-inscription avec le rôle ADMIN n'est pas autorisée");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé: " + roleName));

        // Créer l'utilisateur
        User user = User.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .telephone(request.getTelephone())
                .role(role)
                .actif(true)
                .build();

        User savedUser = userRepository.save(user);

        // Générer le token JWT
     //   String token = jwtService.generateToken(savedUser);

        // Sauvegarder le token
      //  saveToken(savedUser, token);

        // Retourner la réponse
        return AuthResponse.builder()
             //   .token(token)
                .role(role.getName().name())
                .nomComplet(savedUser.getNomComplet())
                .userId(savedUser.getId())
                .build();
    }

    /**
     * Authentifie un utilisateur existant
     * @param request Les informations de connexion
     * @return La réponse d'authentification avec le nouveau token JWT
     * @throws RuntimeException Si l'utilisateur n'existe pas ou si le mot de passe est incorrect
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Authentifier l'utilisateur
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse())
        );

        // Trouver l'utilisateur
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));

        // Révoquer tous les tokens actifs de cet utilisateur
        revokeAllUserTokens(user);

        // Générer un nouveau token
        String token = jwtService.generateToken(user);

        // Sauvegarder le nouveau token
        saveToken(user, token);

        // Retourner la réponse
        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().getName().name())
                .nomComplet(user.getNomComplet())
                .userId(user.getId())
                .build();
    }

    /**
     * Récupère les informations de l'utilisateur connecté
     * @param token Le token JWT de l'utilisateur
     * @return Les informations de l'utilisateur
     * @throws RuntimeException Si le token est invalide ou si l'utilisateur n'existe pas
     */
    public AuthResponse getMe(String token) {
        // Extraire l'email du token
        String email = jwtService.extractEmail(token);

        // Trouver l'utilisateur
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Retourner la réponse
        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().getName().name())
                .nomComplet(user.getNomComplet())
                .userId(user.getId())
                .build();
    }

    /**
     * Sauvegarde un token pour un utilisateur
     * @param user L'utilisateur
     * @param token Le token à sauvegarder
     */
    private void saveToken(User user, String token) {
        Token tokenEntity = Token.builder()
                .token(token)
                .user(user)
                .revoked(false)
                .expired(false)
                .build();
        tokenRepository.save(tokenEntity);
    }

    /**
     * Révoque tous les tokens actifs d'un utilisateur
     * @param user L'utilisateur
     */
    private void revokeAllUserTokens(User user) {
        List<Token> validTokens = tokenRepository.findAllByUserAndRevokedFalseAndExpiredFalse(user);
        if (validTokens.isEmpty()) {
            return;
        }

        validTokens.forEach(token -> {
            token.setRevoked(true);
            token.setExpired(true);
        });
        tokenRepository.saveAll(validTokens);
    }

}