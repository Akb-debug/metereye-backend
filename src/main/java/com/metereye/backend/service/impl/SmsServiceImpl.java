package com.metereye.backend.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.metereye.backend.service.SmsService;

@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Override
    public void send(String phone, String message) {
        log.info("SMS envoyé à {} : {}", phone, message);
    }
}