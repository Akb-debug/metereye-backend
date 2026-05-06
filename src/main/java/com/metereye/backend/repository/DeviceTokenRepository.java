package com.metereye.backend.repository;

import com.metereye.backend.entity.DeviceToken;
import com.metereye.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    List<DeviceToken> findByUserAndActifTrue(User user);

    Optional<DeviceToken> findByToken(String token);

    void deleteByToken(String token);
}