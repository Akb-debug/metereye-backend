package com.metereye.backend.service.impl;

import com.metereye.backend.dto.DeviceTokenRequestDTO;
import com.metereye.backend.entity.DeviceToken;
import com.metereye.backend.entity.User;
import com.metereye.backend.enums.DevicePlatform;
import com.metereye.backend.repository.DeviceTokenRepository;
import com.metereye.backend.service.DeviceTokenService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceTokenServiceImpl implements DeviceTokenService {

    private final DeviceTokenRepository repository;

    @Override
    public void registerToken(User user, DeviceTokenRequestDTO request) {

        DeviceToken token = DeviceToken.builder()
                .user(user)
                .token(request.getToken())
                .platform(DevicePlatform.valueOf(request.getPlatform()))
                .actif(true)
                .build();

        repository.save(token);
    }

    @Override
    public void removeToken(String token) {
        repository.deleteByToken(token);
    }
}