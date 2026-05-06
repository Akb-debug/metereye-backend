package com.metereye.backend.service;

import com.metereye.backend.dto.DeviceTokenRequestDTO;
import com.metereye.backend.entity.User;

public interface DeviceTokenService {

    void registerToken(User user, DeviceTokenRequestDTO request);

    void removeToken(String token);

}