// esp32_cam_onboarding.cpp
#include <WiFi.h>
#include <WebServer.h>
#include <Preferences.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include <UUID.h>

// Configuration
#define AP_SSID "MeterEye-Setup"
#define AP_PASSWORD "metereye123"
#define BACKEND_URL "https://api.metereye.ai/api/devices"
#define SETUP_PORT 80

// États
enum DeviceState {
    SETUP_MODE,
    CONNECTING_WIFI,
    ONLINE_READY,
    CAPTURING,
    OFFLINE_BUFFERING
};

// Variables globales
DeviceState currentState = SETUP_MODE;
WebServer server(SETUP_PORT);
Preferences preferences;
String deviceCode;
String qrCodeValue;
unsigned long lastHeartbeat = 0;
const unsigned long HEARTBEAT_INTERVAL = 60000; // 1 minute

void setup() {
    Serial.begin(115200);
    Serial.println("MeterEye ESP32-CAM démarrage...");
    
    // Initialiser les préférences
    preferences.begin("metereye", false);
    
    // Générer ou récupérer le deviceCode
    deviceCode = preferences.getString("deviceCode", "");
    if (deviceCode == "") {
        deviceCode = generateDeviceCode();
        preferences.putString("deviceCode", deviceCode);
        Serial.println("Nouveau deviceCode généré: " + deviceCode);
    }
    
    // Récupérer le QR code (sera configuré via scan)
    qrCodeValue = preferences.getString("qrCodeValue", "");
    
    // Vérifier la configuration Wi-Fi
    if (isWiFiConfigured()) {
        currentState = CONNECTING_WIFI;
        connectToWiFi();
    } else {
        currentState = SETUP_MODE;
        startSetupMode();
    }
}

void loop() {
    switch (currentState) {
        case SETUP_MODE:
            server.handleClient();
            checkSetupTimeout();
            break;
            
        case CONNECTING_WIFI:
            handleWiFiConnection();
            break;
            
        case ONLINE_READY:
            handleOnlineMode();
            break;
            
        case CAPTURING:
            handleCapturingMode();
            break;
            
        case OFFLINE_BUFFERING:
            handleOfflineMode();
            break;
    }
    
    delay(100);
}

// === MODE SETUP ===
void startSetupMode() {
    Serial.println("Démarrage mode setup...");
    
    // Créer le point d'accès
    WiFi.softAP(AP_SSID, AP_PASSWORD);
    IPAddress IP = WiFi.softAPIP();
    
    Serial.print("AP IP address: ");
    Serial.println(IP);
    
    // Configurer le serveur web
    setupWebServer();
    server.begin();
    
    Serial.println("Serveur web de configuration démarré");
    Serial.println("Connectez-vous à: " + String(AP_SSID));
}

void setupWebServer() {
    server.on("/", handleRoot);
    server.on("/config", HTTP_POST, handleConfig);
    server.on("/status", handleStatus);
    server.onNotFound(handleNotFound);
}

void handleRoot() {
    String html = R"(
<!DOCTYPE html>
<html>
<head>
    <title>MeterEye ESP32-CAM Configuration</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <style>
        body { font-family: Arial; margin: 20px; }
        .container { max-width: 400px; }
        input { width: 100%; padding: 8px; margin: 5px 0; }
        button { background: #007bff; color: white; padding: 10px 20px; border: none; cursor: pointer; }
        .status { margin: 10px 0; padding: 10px; }
        .success { background: #d4edda; color: #155724; }
        .error { background: #f8d7da; color: #721c24; }
    </style>
</head>
<body>
    <div class="container">
        <h1>Configuration MeterEye</h1>
        <p>Veuillez configurer votre réseau Wi-Fi</p>
        
        <form action="/config" method="post">
            <label>SSID Wi-Fi:</label>
            <input type="text" name="ssid" required>
            
            <label>Mot de passe:</label>
            <input type="password" name="password">
            
            <button type="submit">Sauvegarder et Redémarrer</button>
        </form>
        
        <div id="status"></div>
    </div>
</body>
</html>
)";
    server.send(200, "text/html", html);
}

void handleConfig() {
    String ssid = server.arg("ssid");
    String password = server.arg("password");
    
    if (ssid.length() > 0) {
        // Sauvegarder la configuration
        preferences.putString("wifiSSID", ssid);
        preferences.putString("wifiPassword", password);
        
        Serial.println("Configuration Wi-Fi sauvegardée");
        Serial.println("SSID: " + ssid);
        
        // Réponse de succès
        server.send(200, "application/json", "{\"status\":\"success\",\"message\":\"Configuration sauvegardée\"}");
        
        // Attendre un peu puis redémarrer
        delay(2000);
        ESP.restart();
    } else {
        server.send(400, "application/json", "{\"status\":\"error\",\"message\":\"SSID requis\"}");
    }
}

void handleStatus() {
    String json = "{\"deviceCode\":\"" + deviceCode + "\",\"qrCode\":\"" + qrCodeValue + "\"}";
    server.send(200, "application/json", json);
}

void handleNotFound() {
    server.send(404, "text/plain", "Not found");
}

// === CONNEXION WI-FI ===
bool isWiFiConfigured() {
    return preferences.getString("wifiSSID", "").length() > 0;
}

void connectToWiFi() {
    String ssid = preferences.getString("wifiSSID", "");
    String password = preferences.getString("wifiPassword", "");
    
    Serial.println("Connexion au Wi-Fi: " + ssid);
    
    WiFi.begin(ssid.c_str(), password.c_str());
    
    int attempts = 0;
    while (WiFi.status() != WL_CONNECTED && attempts < 30) {
        delay(1000);
        Serial.print(".");
        attempts++;
    }
    
    if (WiFi.status() == WL_CONNECTED) {
        Serial.println("\nWi-Fi connecté!");
        Serial.print("IP: ");
        Serial.println(WiFi.localIP());
        
        currentState = ONLINE_READY;
        performHandshake();
    } else {
        Serial.println("\nÉchec connexion Wi-Fi");
        // Retour au mode setup
        preferences.remove("wifiSSID");
        preferences.remove("wifiPassword");
        currentState = SETUP_MODE;
        startSetupMode();
    }
}

void handleWiFiConnection() {
    // La connexion est gérée dans connectToWiFi()
    delay(100);
}

// === MODE ONLINE ===
void performHandshake() {
    Serial.println("Handshake avec le backend...");
    
    HTTPClient http;
    String url = BACKEND_URL + "/" + deviceCode + "/handshake";
    
    // Préparer le payload
    DynamicJsonDocument doc(1024);
    doc["firmwareVersion"] = "1.0.0";
    doc["ipAddress"] = WiFi.localIP().toString();
    doc["wifiSsid"] = WiFi.SSID();
    
    String payload;
    serializeJson(doc, payload);
    
    http.begin(url);
    http.addHeader("Content-Type", "application/json");
    
    int httpResponseCode = http.POST(payload);
    
    if (httpResponseCode == 200) {
        Serial.println("Handshake réussi");
        currentState = CAPTURING;
        lastHeartbeat = millis();
    } else {
        Serial.printf("Handshake échoué: %d\n", httpResponseCode);
        String response = http.getString();
        Serial.println(response);
        
        // Retenter plus tard
        delay(30000);
    }
    
    http.end();
}

void handleOnlineMode() {
    // Envoyer un heartbeat périodique
    if (millis() - lastHeartbeat > HEARTBEAT_INTERVAL) {
        sendHeartbeat();
        lastHeartbeat = millis();
    }
    
    // Passer en mode capture
    currentState = CAPTURING;
}

void sendHeartbeat() {
    HTTPClient http;
    String url = BACKEND_URL + "/" + deviceCode + "/heartbeat";
    
    DynamicJsonDocument doc(512);
    doc["timestamp"] = getISO8601Time();
    doc["batteryLevel"] = 85; // Simulé
    doc["signalStrength"] = WiFi.RSSI();
    
    String payload;
    serializeJson(doc, payload);
    
    http.begin(url);
    http.addHeader("Content-Type", "application/json");
    
    int httpResponseCode = http.POST(payload);
    
    if (httpResponseCode == 200) {
        Serial.println("Heartbeat envoyé");
    } else {
        Serial.printf("Heartbeat échoué: %d\n", httpResponseCode);
        currentState = OFFLINE_BUFFERING;
    }
    
    http.end();
}

// === MODE CAPTURE ===
void handleCapturingMode() {
    // Logique de capture d'image et envoi
    Serial.println("Mode capture - prêt à envoyer des relevés");
    
    // Simuler une capture toutes les heures
    static unsigned long lastCapture = 0;
    unsigned long captureInterval = preferences.getInt("captureInterval", 3600) * 1000; // Convertir en ms
    
    if (millis() - lastCapture > captureInterval) {
        captureAndSendImage();
        lastCapture = millis();
    }
    
    // Vérifier heartbeat
    if (millis() - lastHeartbeat > HEARTBEAT_INTERVAL) {
        sendHeartbeat();
        lastHeartbeat = millis();
    }
}

void captureAndSendImage() {
    Serial.println("Capture et envoi d'image...");
    
    // TODO: Implémenter la capture caméra
    // Pour l'instant, simuler l'envoi
    
    HTTPClient http;
    String url = BACKEND_URL + "/" + deviceCode + "/capture";
    
    DynamicJsonDocument doc(1024);
    doc["timestamp"] = getISO8601Time();
    doc["imageData"] = "base64_image_data_here"; // TODO: Remplacer par vraie image
    
    String payload;
    serializeJson(doc, payload);
    
    http.begin(url);
    http.addHeader("Content-Type", "application/json");
    
    int httpResponseCode = http.POST(payload);
    
    if (httpResponseCode == 200) {
        Serial.println("Image envoyée avec succès");
    } else {
        Serial.printf("Envoi image échoué: %d\n", httpResponseCode);
        currentState = OFFLINE_BUFFERING;
    }
    
    http.end();
}

// === MODE OFFLINE ===
void handleOfflineMode() {
    Serial.println("Mode offline - tentative de reconnexion...");
    
    // Tenter de se reconnecter
    if (WiFi.status() != WL_CONNECTED) {
        connectToWiFi();
    } else {
        // Retenter le handshake
        performHandshake();
    }
    
    delay(30000); // Attendre 30 secondes avant de réessayer
}

// === UTILITAIRES ===
String generateDeviceCode() {
    // Générer un UUID simple
    return "MET-" + String(random(0xFFFF), HEX) + "-" + String(random(0xFFFF), HEX);
}

String getISO8601Time() {
    // TODO: Implémenter la conversion de temps ISO8601
    return "2026-04-13T10:00:00Z";
}

void checkSetupTimeout() {
    // Timeout après 10 minutes en mode setup
    static unsigned long setupStartTime = millis();
    
    if (millis() - setupStartTime > 600000) { // 10 minutes
        Serial.println("Timeout mode setup - redémarrage...");
        ESP.restart();
    }
}
