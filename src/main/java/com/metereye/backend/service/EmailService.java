package com.metereye.backend.service;

public interface EmailService {

    void send(String to, String message);

}