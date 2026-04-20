// User.java
package com.metereye.backend.entity;

import com.metereye.backend.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "users")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity implements UserDetails {

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String motDePasse;

    private String telephone;

    @Builder.Default
    private boolean actif = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // Implémentation des méthodes UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.getName()));
    }

    @Override
    public String getPassword() {
        return motDePasse;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return actif;
    }

    // Méthode utilitaire pour obtenir le nom complet
    public String getNomComplet() {
        return nom + " " + prenom;
    }

    // ===== NOUVEAUX CHAMPS POUR SPRINT 2 =====

    @Column(name = "seuil_alerte_credit")
    @Builder.Default
    private Double seuilAlerteCredit = 5000.0;  // Seuil d'alerte pour Cash Power (ex: 5000 FCFA)

    @Column(name = "seuil_alerte_anomalie")
    @Builder.Default
    private Double seuilAlerteAnomalie = 30.0;  // Seuil en pourcentage pour anomalies (ex: 30%)

    @Column(name = "notification_push")
    @Builder.Default
    private Boolean notificationPush = true;  // Activer/désactiver notifications push

    @Column(name = "notification_sms")
    @Builder.Default
    private Boolean notificationSms = false;  // Activer/désactiver notifications SMS

    @Column(name = "notification_email")
    @Builder.Default
    private Boolean notificationEmail = true;  // Activer/désactiver notifications Email

    // ... getters et setters existants ...

    // Ajouter ces getters/setters
    public Double getSeuilAlerteCredit() {
        return seuilAlerteCredit != null ? seuilAlerteCredit : 5000.0;
    }

    public Double getSeuilAlerteAnomalie() {
        return seuilAlerteAnomalie != null ? seuilAlerteAnomalie : 30.0;
    }
}