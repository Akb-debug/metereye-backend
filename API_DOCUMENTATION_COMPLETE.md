# Documentation Complète des Endpoints REST - MeterEye AI Backend

## Table des Matières

1. [Authentification](#authentification)
2. [Gestion des Utilisateurs](#gestion-des-utilisateurs)
3. [Gestion des Compteurs](#gestion-des-compteurs)
4. [Configuration des Compteurs](#configuration-des-compteurs)
5. [Gestion des Modules Bluetooth](#gestion-des-modules-bluetooth)
6. [Gestion des Modules Devices](#gestion-des-modules-devices)
7. [Gestion des Devices (ESP32-CAM)](#gestion-des-devices-esp32-cam)
8. [Gestion des Relevés](#gestion-des-relevés)
9. [Gestion des Images](#gestion-des-images)
10. [Gestion des Alertes](#gestion-des-alertes)

---

## Authentification

### Base URL: `/api/auth`

#### POST `/api/auth/register`
**Description**: Inscription d'un nouvel utilisateur

**Request Body**:
```json
{
  "nom": "string",
  "prenom": "string",
  "email": "string",
  "motDePasse": "string",
  "telephone": "string",
  "role": "string"
}
```

**Response**:
```json
{
  "token": "string",
  "type": "Bearer",
  "id": 1,
  "email": "string",
  "role": "string",
  "nom": "string",
  "prenom": "string"
}
```

#### POST `/api/auth/login`
**Description**: Connexion d'un utilisateur

**Request Body**:
```json
{
  "email": "string",
  "motDePasse": "string"
}
```

**Response**: Identique à l'inscription

#### GET `/api/auth/me`
**Description**: Récupérer les informations de l'utilisateur connecté

**Headers**: 
- `Authorization: Bearer {token}`

**Response**: Identique à l'inscription

---

## Gestion des Utilisateurs

### Base URL: `/api/users`

#### GET `/api/users/profile`
**Description**: Récupérer le profil de l'utilisateur connecté

**Response**:
```json
{
  "id": 1,
  "nom": "string",
  "prenom": "string",
  "email": "string",
  "telephone": "string",
  "role": "string",
  "seuilAlerteCredit": 100.0,
  "seuilAlerteAnomalie": 50.0,
  "notificationPush": true,
  "notificationSms": false,
  "notificationEmail": true
}
```

#### PUT `/api/users/seuils`
**Description**: Mettre à jour les seuils d'alerte

**Query Parameters**:
- `seuilCredit` (Double): Seuil d'alerte de crédit
- `seuilAnomalie` (Double): Seuil d'alerte d'anomalie

#### PUT `/api/users/notifications`
**Description**: Mettre à jour les préférences de notification

**Query Parameters**:
- `push` (Boolean, optionnel): Notifications push
- `sms` (Boolean, optionnel): Notifications SMS
- `email` (Boolean, optionnel): Notifications email

---

## Gestion des Compteurs

### Base URL: `/api/compteurs`

#### POST `/api/compteurs`
**Description**: Créer un nouveau compteur

**Request Body**:
```json
{
  "reference": "string",
  "adresse": "string",
  "typeCompteur": "CLASSIQUE|CASH_POWER",
  "valeurInitiale": 1000.0
}
```

**Response**:
```json
{
  "id": 1,
  "reference": "string",
  "adresse": "string",
  "typeCompteur": "CLASSIQUE|CASH_POWER",
  "valeurActuelle": 1000.0,
  "proprietaireNom": "string",
  "proprietaireId": 1,
  "dateInitialisation": "2024-01-01",
  "actif": true,
  "dateCreation": "2024-01-01T10:00:00"
}
```

#### GET `/api/compteurs`
**Description**: Récupérer tous les compteurs de l'utilisateur connecté

**Response**: Liste de `CompteurResponseDTO`

#### GET `/api/compteurs/{id}`
**Description**: Récupérer un compteur par son ID

#### DELETE `/api/compteurs/{id}`
**Description**: Désactiver un compteur

#### POST `/api/compteurs/releves`
**Description**: Ajouter un relevé manuel

**Request Body**:
```json
{
  "meterId": 1,
  "value": 1500.0,
  "comment": "string"
}
```

#### GET `/api/compteurs/{id}/releves`
**Description**: Récupérer l'historique des relevés d'un compteur

**Query Parameters**:
- `startDate` (LocalDateTime, optionnel): Date de début
- `endDate` (LocalDateTime, optionnel): Date de fin

#### POST `/api/compteurs/recharge`
**Description**: Recharger un compteur Cash Power

**Request Body**:
```json
{
  "compteurId": 1,
  "montant": 5000.0,
  "codeRecharge": "string"
}
```

#### GET `/api/compteurs/{id}/stats`
**Description**: Récupérer les statistiques de consommation

**Query Parameters**:
- `periode` (String): "day", "week", "month", "year"

#### POST `/api/compteurs/{id}/ocr`
**Description**: Ajouter un relevé par OCR

**Request Body**: Image en base64 (String)

---

## Configuration des Compteurs

### Base URL: `/api/compteurs`

#### POST `/api/compteurs/{id}/mode-lecture`
**Description**: Configurer le mode de lecture d'un compteur

**Request Body**:
```json
{
  "modeLecture": "MANUAL|ESP32_CAM|SENSOR"
}
```

#### POST `/api/compteurs/{id}/reinitialiser`
**Description**: Réinitialiser un compteur classique

**Request Body**:
```json
{
  "motif": "string"
}
```

#### GET `/api/compteurs/{id}/statut-configuration`
**Description**: Vérifier le statut de configuration d'un compteur

**Response**:
```json
{
  "reference": "string",
  "statut": "ACTIF|INACTIF",
  "modeLectureConfigure": "MANUAL|ESP32_CAM|SENSOR",
  "configurePourLecture": true
}
```

---

## Gestion des Modules Bluetooth

### Base URL: `/api/bluetooth`

#### POST `/api/bluetooth/scan`
**Description**: Scanner et enregistrer un module Bluetooth

**Request Body**:
```json
{
  "bluetoothAddress": "string",
  "typeModule": "ESP32_CAM|ESP32_PZEM",
  "firmwareVersion": "string",
  "signalStrength": -50
}
```

#### POST `/api/bluetooth/configure`
**Description**: Configurer et associer un module à un compteur

**Request Body**:
```json
{
  "bluetoothAddress": "string",
  "compteurId": 1,
  "wifiSsid": "string",
  "wifiPassword": "string",
  "captureInterval": 3600
}
```

#### GET `/api/bluetooth/available`
**Description**: Lister les modules non configurés de l'utilisateur

#### POST `/api/bluetooth/compatibility`
**Description**: Vérifier la compatibilité entre un module et un compteur

**Request Body**:
```json
{
  "bluetoothAddress": "string",
  "compteurId": 1
}
```

#### GET `/api/bluetooth/{bluetoothAddress}`
**Description**: Récupérer les détails d'un module par adresse Bluetooth

#### POST `/api/bluetooth/direct-configure`
**Description**: Configurer un module directement via adresse Bluetooth

**Request Body**:
```json
{
  "bluetoothAddress": "string",
  "compteurId": 1,
  "wifiSsid": "string",
  "wifiPassword": "string",
  "captureInterval": 3600,
  "pzemSettings": {
    "calibrationFactor": 1.0,
    "currentRatio": 1.0,
    "voltageRatio": 1.0
  }
}
```

#### GET `/api/bluetooth/search/{bluetoothAddress}`
**Description**: Rechercher un module par adresse Bluetooth

#### DELETE `/api/bluetooth/{bluetoothAddress}`
**Description**: Supprimer un module non configuré

---

## Gestion des Modules Devices

### Base URL: `/api/module-devices`

#### POST `/api/module-devices/scan`
**Description**: Scanner un module device (obsolète - utiliser Bluetooth)

#### POST `/api/module-devices/{deviceCode}/associate`
**Description**: Associer un module à un compteur (obsolète)

#### POST `/api/module-devices/{deviceCode}/handshake`
**Description**: Première connexion du module au backend

**Request Body**:
```json
{
  "firmwareVersion": "string",
  "ipAddress": "string",
  "wifiSsid": "string"
}
```

#### POST `/api/module-devices/{deviceCode}/heartbeat`
**Description**: Signaler que le module est en ligne

#### GET `/api/module-devices/{deviceCode}/status`
**Description**: Récupérer le statut actuel d'un module

#### GET `/api/module-devices/my`
**Description**: Lister tous les modules de l'utilisateur connecté

#### PUT `/api/module-devices/{deviceCode}/capture-interval`
**Description**: Modifier la fréquence de capture des relevés

**Query Parameters**:
- `interval` (Integer): Intervalle en secondes

#### POST `/api/module-devices/compteurs/{compteurId}/changer-mode`
**Description**: Changer le mode de lecture d'un compteur

**Request Body**:
```json
{
  "nouveauMode": "MANUAL|ESP32_CAM|SENSOR",
  "motif": "string"
}
```

#### POST `/api/module-devices/{deviceCode}/configurer-pzem`
**Description**: Configurer les paramètres spécifiques du capteur PZEM004T

**Request Body**:
```json
{
  "calibrationFactor": 1.0,
  "currentRatio": 1.0,
  "voltageRatio": 1.0,
  "powerThreshold": 1000.0,
  "energyThreshold": 1000.0
}
```

---

## Gestion des Devices (ESP32-CAM)

### Base URL: `/api/devices`

#### POST `/api/devices/scan`
**Description**: Scanner et enregistrer un module via QR code

**Request Body**:
```json
{
  "qrCode": "string",
  "deviceType": "ESP32_CAM",
  "metadata": {}
}
```

#### POST `/api/devices/{deviceCode}/associate`
**Description**: Associer un module à un compteur

**Request Body**:
```json
{
  "meterId": 1,
  "captureInterval": 3600
}
```

#### POST `/api/devices/{deviceCode}/handshake`
**Description**: Handshake initial du module avec le backend

**Request Body**:
```json
{
  "firmwareVersion": "string",
  "ipAddress": "string",
  "wifiSsid": "string"
}
```

#### POST `/api/devices/{deviceCode}/heartbeat`
**Description**: Heartbeat du module pour maintenir la connexion

**Request Body**:
```json
{
  "status": "ONLINE",
  "batteryLevel": 80,
  "signalStrength": -50
}
```

#### GET `/api/devices/{deviceCode}/status`
**Description**: Obtenir le statut d'un module

#### GET `/api/devices/my`
**Description**: Lister les modules de l'utilisateur connecté

#### PUT `/api/devices/{deviceCode}/capture-interval`
**Description**: Mettre à jour l'intervalle de capture

**Query Parameters**:
- `interval` (Integer): Nouvel intervalle en secondes

---

## Gestion des Relevés

### Base URL: `/api/readings`

#### POST `/api/readings/manual`
**Description**: Créer un relevé manuel

**Request Body**:
```json
{
  "meterId": 1,
  "value": 1500.0,
  "comment": "string"
}
```

#### POST `/api/readings/upload`
**Description**: Uploader une image pour relevé (ESP32-CAM)

**Request Parameters**:
- `meterId` (Long): ID du compteur
- `file` (MultipartFile): Image du relevé

#### POST `/api/readings/sensor`
**Description**: Créer un relevé capteur (ESP32-PZEM004T)

**Request Body**:
```json
{
  "meterId": 1,
  "sensorId": "string",
  "value": 1500.0,
  "voltage": 220.0,
  "current": 10.0,
  "power": 2200.0,
  "energy": 1000.0
}
```

#### GET `/api/readings/meters/{meterId}`
**Description**: Lister les relevés d'un compteur (paginé)

**Query Parameters**:
- `page` (int, défaut: 0): Page
- `size` (int, défaut: 20): Taille de page
- `source` (String, optionnel): Source du relevé

#### GET `/api/readings/meters/{meterId}/latest`
**Description**: Récupérer le dernier relevé d'un compteur

---

## Gestion des Images

### Base URL: `/api/images`

#### GET `/api/images/{imageId}`
**Description**: Télécharger une image de relevé

**Response**: Fichier image (Resource)

---

## Gestion des Alertes

### Base URL: `/api/alertes`

*Note: Ce contrôleur est actuellement désactivé pour résoudre les problèmes de démarrage*

#### GET `/api/alertes`
**Description**: Récupérer toutes les alertes de l'utilisateur

#### GET `/api/alertes/non-lues`
**Description**: Récupérer les alertes non lues

#### PUT `/api/alertes/{id}/lire`
**Description**: Marquer une alerte comme lue

---

## Formats de Réponse Standard

### BaseResponse
La plupart des endpoints retournent une réponse au format `BaseResponse`:

```json
{
  "success": true,
  "message": "string",
  "data": {},
  "timestamp": "2024-01-01T10:00:00",
  "status": 200
}
```

### ModuleDeviceResponseDTO
```json
{
  "deviceCode": "string",
  "bluetoothAddress": "string",
  "typeModule": "ESP32_CAM|ESP32_PZEM",
  "statut": "ACTIF|INACTIF|HORS_LIGNE",
  "configured": true,
  "lastSeenAt": "2024-01-01T10:00:00",
  "firmwareVersion": "string",
  "captureInterval": 3600,
  "wifiSsid": "string",
  "ipAddress": "string",
  "proprietaireId": 1,
  "compteurId": 1,
  "compteurReference": "string",
  "modeLectureAssocie": "MANUAL|ESP32_CAM|SENSOR"
}
```

### ReadingResponse
```json
{
  "id": 1,
  "meterId": 1,
  "value": 1500.0,
  "dateTime": "2024-01-01T10:00:00",
  "source": "MANUAL|ESP32_CAM|SENSOR",
  "comment": "string",
  "imageUrl": "string",
  "consumption": 500.0,
  "status": "VALID"
}
```

---

## Codes d'Erreur

- **200**: Succès
- **201**: Créé avec succès
- **400**: Requête invalide
- **401**: Non authentifié
- **403**: Accès refusé
- **404**: Ressource non trouvée
- **500**: Erreur interne du serveur

---

## Sécurité

- **Authentication**: JWT Bearer Token requis pour la plupart des endpoints
- **Authorization**: L'utilisateur ne peut accéder qu'à ses propres ressources
- **Validation**: Les DTOs utilisent `@Valid` pour la validation des entrées

---

## Notes importantes

1. Les endpoints Bluetooth sont les plus récents et remplacent progressivement les anciens endpoints QR code
2. Le contrôleur d'alertes est temporairement désactivé
3. Les endpoints de configuration directe Bluetooth permettent une configuration sans scan préalable
4. Les modes de lecture supportés sont: MANUAL, ESP32_CAM, SENSOR
5. Les types de compteurs supportés sont: CLASSIQUE, CASH_POWER

---

## Exemples d'utilisation

### Configuration complète d'un module ESP32-CAM

1. **Scanner le module**:
```bash
POST /api/bluetooth/scan
{
  "bluetoothAddress": "AA:BB:CC:DD:EE:FF",
  "typeModule": "ESP32_CAM",
  "firmwareVersion": "1.0.0"
}
```

2. **Vérifier la compatibilité**:
```bash
POST /api/bluetooth/compatibility
{
  "bluetoothAddress": "AA:BB:CC:DD:EE:FF",
  "compteurId": 1
}
```

3. **Configurer le module**:
```bash
POST /api/bluetooth/configure
{
  "bluetoothAddress": "AA:BB:CC:DD:EE:FF",
  "compteurId": 1,
  "wifiSsid": "MonWiFi",
  "wifiPassword": "password123",
  "captureInterval": 3600
}
```

4. **Handshake du module**:
```bash
POST /api/module-devices/{deviceCode}/handshake
{
  "firmwareVersion": "1.0.0",
  "ipAddress": "192.168.1.100",
  "wifiSsid": "MonWiFi"
}
```

### Configuration d'un module ESP32-PZEM004T

1. **Configuration directe**:
```bash
POST /api/bluetooth/direct-configure
{
  "bluetoothAddress": "AA:BB:CC:DD:EE:FF",
  "compteurId": 1,
  "wifiSsid": "MonWiFi",
  "wifiPassword": "password123",
  "captureInterval": 1800,
  "pzemSettings": {
    "calibrationFactor": 1.0,
    "currentRatio": 1.0,
    "voltageRatio": 1.0
  }
}
```

2. **Envoyer un relevé capteur**:
```bash
POST /api/readings/sensor
{
  "meterId": 1,
  "sensorId": "AA:BB:CC:DD:EE:FF",
  "value": 1500.0,
  "voltage": 220.0,
  "current": 10.0,
  "power": 2200.0,
  "energy": 1000.0
}
```

---

*Documentation générée le 24 avril 2026 - MeterEye AI Backend v1.0.0*
