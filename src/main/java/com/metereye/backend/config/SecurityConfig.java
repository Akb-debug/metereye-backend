// SecurityConfig.java
package com.metereye.backend.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // ── Endpoints publics ──────────────────────────────────────────
                        .requestMatchers(
                                "/api/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // ── MAISONS : PROPRIETAIRE ou ADMIN ───────────────────────────
                        .requestMatchers("/api/maisons/**")
                            .hasAnyRole("PROPRIETAIRE", "ADMIN")

                        // ── SOUS-COMPTEURS : ordre des règles crucial ──────────────────
                        // Gestion locataires → PROPRIETAIRE ou ADMIN uniquement
                        .requestMatchers("/api/sous-compteurs/locataires/**")
                            .hasAnyRole("PROPRIETAIRE", "ADMIN")
                        // Mon additionneuse → LOCATAIRE (et ADMIN)
                        .requestMatchers("/api/sous-compteurs/mon-additionneuse")
                            .hasAnyRole("LOCATAIRE", "ADMIN")
                        // Reste des sous-compteurs → PROPRIETAIRE ou LOCATAIRE ou ADMIN
                        .requestMatchers("/api/sous-compteurs/**")
                            .hasAnyRole("PROPRIETAIRE", "LOCATAIRE", "ADMIN")

                        // ── RÉPARTITION ────────────────────────────────────────────────
                        // Mes factures → LOCATAIRE (et ADMIN)
                        .requestMatchers("/api/repartition/mes-factures")
                            .hasAnyRole("LOCATAIRE", "ADMIN")
                        // Générer / aperçu / par maison → PROPRIETAIRE ou ADMIN
                        .requestMatchers(
                                "/api/repartition/generer",
                                "/api/repartition/apercu"
                        ).hasAnyRole("PROPRIETAIRE", "ADMIN")
                        .requestMatchers("/api/repartition/maison/**")
                            .hasAnyRole("PROPRIETAIRE", "ADMIN")
                        // Détail d'une facture → PROPRIETAIRE ou LOCATAIRE ou ADMIN
                        .requestMatchers("/api/repartition/factures/**")
                            .hasAnyRole("PROPRIETAIRE", "LOCATAIRE", "ADMIN")

                        // ── PDF ────────────────────────────────────────────────────────
                        .requestMatchers("/api/pdf/factures/**")
                            .hasAnyRole("PROPRIETAIRE", "LOCATAIRE", "ADMIN")

                        // ── Toutes les autres routes → authentification simple ─────────
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Non authentifié"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accès refusé"))
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}