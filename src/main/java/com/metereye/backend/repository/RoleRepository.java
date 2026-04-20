// RoleRepository.java
package com.metereye.backend.repository;

import com.metereye.backend.entity.Role;
import com.metereye.backend.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}