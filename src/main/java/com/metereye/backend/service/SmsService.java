package com.metereye.backend.service;

public interface SmsService {

    void send(String phone, String message);

}