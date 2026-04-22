// DataInitializer.java
package com.metereye.backend.config;

import com.metereye.backend.entity.Role;
import com.metereye.backend.enums.RoleName;
import com.metereye.backend.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialiser les rôles s'ils n'existent pas
        List<RoleName> rolesToCreate = Arrays.asList(
                RoleName.ADMIN,
                RoleName.PROPRIETAIRE,
                RoleName.LOCATAIRE,
                RoleName.PERSONNEL
                
        );

        for (RoleName roleName : rolesToCreate) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = Role.builder()
                        .name(roleName)
                        .build();
                roleRepository.save(role);
                System.out.println("Rôle créé: " + roleName);
            }
        }

        System.out.println("Initialisation des données terminée");
    }
}