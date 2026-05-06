package com.metereye.backend.controller;

import com.metereye.backend.dto.DeviceTokenRequestDTO;
import com.metereye.backend.entity.User;
import com.metereye.backend.service.DeviceTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/device-tokens")
@RequiredArgsConstructor
public class DeviceTokenController {

    private final DeviceTokenService service;

    @PostMapping
    public void registerToken(
            @AuthenticationPrincipal User user,
            @RequestBody DeviceTokenRequestDTO request
    ) {
        service.registerToken(user, request);
    }

    @DeleteMapping("/{token}")
    public void deleteToken(@PathVariable String token) {
        service.removeToken(token);
    }
}