// JwtAuthFilter.java
package com.metereye.backend.config;

import com.metereye.backend.repository.TokenRepository;
import com.metereye.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Vérifier si le header Authorization est présent et commence par "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraire le token JWT
        jwt = authHeader.substring(7);

        try {
            // Extraire l'email du token
            userEmail = jwtService.extractEmail(jwt);

            // Si l'email est présent et qu'aucun utilisateur n'est déjà authentifié
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Vérifier si le token est valide et non révoqué
                var isTokenValid = tokenRepository.findByToken(jwt)
                        .map(t -> !t.isRevoked() && !t.isExpired())
                        .orElse(false);

                if (jwtService.isTokenValid(jwt, userDetails) && isTokenValid) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // En cas d'erreur de validation du token, on continue sans authentification
            logger.error("Erreur de validation du token JWT: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}