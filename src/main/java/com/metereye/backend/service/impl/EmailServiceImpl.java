package com.metereye.backend.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.metereye.backend.service.EmailService;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Override
    public void send(String to, String message) {
        log.info("EMAIL envoyé à {} : {}", to, message);
    }
}