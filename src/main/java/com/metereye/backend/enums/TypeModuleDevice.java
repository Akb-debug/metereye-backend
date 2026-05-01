// TypeModuleDevice.java
package com.metereye.backend.enums;

/**
 * Types de modules devices supportés
 * Correspond aux solutions techniques pour chaque mode métier
 */
public enum TypeModuleDevice {
    ESP32_CAM("ESP32-CAM", "Module ESP32 avec caméra pour capture d'images"),
    ESP32_PZEM004T("ESP32-PZEM004T", "Module ESP32 avec capteur d'énergie PZEM004T"),
    SENSOR_GENERIC("SENSOR-GENERIC", "Capteur générique pour relevés"),
    IOT_MODULE("IOT-MODULE", "Module IoT générique");

    private final String code;
    private final String description;

    TypeModuleDevice(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getModeLectureAssocie() {
        return switch (this) {
            case ESP32_CAM -> "ESP32_CAM";
            case ESP32_PZEM004T, SENSOR_GENERIC, IOT_MODULE -> "SENSOR";
        };
    }

    public boolean estSupporte() {
        return this == ESP32_CAM || this == ESP32_PZEM004T;
    }
}
