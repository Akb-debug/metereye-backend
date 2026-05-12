# MeterEye AI Backend - Documentation Complète

## Table des Matières

1. [Vue d'ensemble](#vue-densemble)
2. [Architecture du projet](#architecture-du-projet)
3. [Workflow du système](#workflow-du-système)
4. [Endpoints API](#endpoints-api)
   - [Authentification](#authentification)
   - [Gestion des utilisateurs](#gestion-des-utilisateurs)
   - [Gestion des compteurs](#gestion-des-compteurs)
   - [Gestion des relevés](#gestion-des-relevés)
   - [Gestion des dispositifs IoT](#gestion-des-dispositifs-iot)
   - [Gestion des images](#gestion-des-images)
   - [Alertes et notifications](#alertes-et-notifications)
5. [Modèles de données](#modèles-de-données)
6. [Sécurité](#sécurité)

---

## Vue d'ensemble

MeterEye AI est une solution de surveillance intelligente de compteurs électriques qui combine l'IoT, l'OCR et une architecture backend moderne pour automatiser le suivi de la consommation d'électricité.

### Caractéristiques principales
- Authentification utilisateur (JWT)
- Gestion de compteurs (Classique & CashPower)
- Relevés multi-sources : manuels, ESP32-CAM, capteurs
- Intégration IoT avec modules ESP32
- Pipeline de traitement d'images (OCR-ready)
- Analytiques de consommation et statistiques

### Stack technique
- **Java 21** avec Spring Boot 3.2.0
- **Spring Security** avec JWT
- **PostgreSQL** comme base de données
- **JPA/Hibernate** pour la persistance
- **Swagger/OpenAPI** pour la documentation

---

## Architecture du projet

### Structure des packages

```
com.metereye.backend/
├── config/          # Configuration Spring Security, CORS, etc.
├── controller/      # Contrôleurs REST API
├── dto/            # Data Transfer Objects
├── entity/         # Entités JPA
├── enums/          # Énumérations du domaine
├── mapper/         # MapStruct mappers
├── repository/     # Interfaces Spring Data JPA
├── service/        # Logique métier
└── utils/          # Utilitaires et classes de base
```

### Entités principales

- **User** : Utilisateurs avec rôles et préférences
- **Compteur** : Compteurs électriques (Classique/CashPower)
- **ModuleESP32** : Modules IoT pour surveillance
- **Releve** : Lectures de consommation
- **Alerte** : Notifications d'alertes
- **Image** : Images pour traitement OCR

---

## Workflow du système

### 1. Flux d'authentification
```
Client → LoginRequest → AuthController → AuthService → JWT Token
```

### 2. Flux de création de compteur
```
Client → CompteurRequestDTO → CompteurController → CompteurService → Base de données
```

### 3. Flux de relevé (multiple sources)
```
ESP32-CAM → Image → ReadingController → ReadingService → OCR → Releve
Client → ManualReadingRequest → ReadingController → ReadingService → Releve
Capteur → SensorReadingRequest → ReadingController → ReadingService → Releve
```

### 4. Flux IoT (ESP32)
```
ESP32 → Handshake → DeviceController → DeviceService → ModuleESP32
ESP32 → Heartbeat → DeviceController → DeviceService → Mise à jour statut
ESP32 → Image → ReadingController → ReadingService → OCR + Releve
```

### 5. Flux d'alertes
```
Releve → Vérification seuils → AlerteService → Création alerte → Notification
```

---

## Endpoints API

### Authentification

#### POST /api/auth/register
Inscription d'un nouvel utilisateur.

**Request Body:**
```json
{
  "nom": "Doe",
  "prenom": "John",
  "email": "john.doe@example.com",
  "motDePasse": "Password123!",
  "telephone": "+221123456789",
  "role": "USER"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "USER",
  "nomComplet": "John Doe",
  "userId": 1
}
```

#### POST /api/auth/login
Connexion d'un utilisateur existant.

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "motDePasse": "Password123!"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "USER",
  "nomComplet": "John Doe",
  "userId": 1
}
```

#### GET /api/auth/me
Récupérer les informations de l'utilisateur connecté.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "USER",
  "nomComplet": "John Doe",
  "userId": 1
}
```

---

### Gestion des utilisateurs

#### GET /api/users/profile
Récupérer le profil de l'utilisateur connecté.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "nom": "Doe",
    "prenom": "John",
    "email": "john.doe@example.com",
    "telephone": "+221123456789",
    "role": "USER",
    "seuilAlerteCredit": 5000.0,
    "seuilAlerteAnomalie": 30.0,
    "notificationPush": true,
    "notificationSms": false,
    "notificationEmail": true
  }
}
```

#### PUT /api/users/profile
Mettre à jour le profil de l'utilisateur connecté.

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "nom": "Doe",
  "prenom": "John",
  "email": "john.doe@example.com",
  "telephone": "+221123456789",
  "motDePasse": "NewPassword123!",
  "seuilAlerteCredit": 6000.0,
  "seuilAlerteAnomalie": 35.0,
  "notificationPush": true,
  "notificationSms": true,
  "notificationEmail": false
}
```

**Note:** Tous les champs sont optionnels. Seuls les champs fournis seront mis à jour.

**Response:**
```json
{
  "success": true,
  "message": "Profil mis à jour avec succès",
  "data": {
    "id": 1,
    "nom": "Doe",
    "prenom": "John",
    "email": "john.doe@example.com",
    "telephone": "+221123456789",
    "role": "USER",
    "seuilAlerteCredit": 6000.0,
    "seuilAlerteAnomalie": 35.0,
    "notificationPush": true,
    "notificationSms": true,
    "notificationEmail": false
  }
}
```

**Erreurs possibles:**
```json
{
  "success": false,
  "message": "Cet email est déjà utilisé par un autre utilisateur"
}
```

```json
{
  "success": false,
  "message": "Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial"
}
```

#### PUT /api/users/seuils
Mettre à jour les seuils d'alerte.

**Headers:**
```
Authorization: Bearer <token>
```

**Request Parameters:**
```
seuilCredit=5000.0&seuilAnomalie=30.0
```

**Response:**
```json
{
  "success": true,
  "message": "Seuils mis à jour avec succès",
  "data": {
    "id": 1,
    "seuilAlerteCredit": 5000.0,
    "seuilAlerteAnomalie": 30.0
  }
}
```

#### PUT /api/users/notifications
Mettre à jour les préférences de notification.

**Headers:**
```
Authorization: Bearer <token>
```

**Request Parameters:**
```
push=true&sms=false&email=true
```

**Response:**
```json
{
  "success": true,
  "message": "Préférences de notification mises à jour",
  "data": {
    "notificationPush": true,
    "notificationSms": false,
    "notificationEmail": true
  }
}
```

---

### Gestion des compteurs

#### POST /api/compteurs
Créer un nouveau compteur.

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "reference": "CMP-2024-001",
  "adresse": "123 Rue de la République, Dakar",
  "typeCompteur": "CLASSIQUE",
  "valeurInitiale": 1000.0
}
```

**Response:**
```json
{
  "success": true,
  "message": "Compteur créé avec succès",
  "data": {
    "id": 1,
    "reference": "CMP-2024-001",
    "adresse": "123 Rue de la République, Dakar",
    "typeCompteur": "CLASSIQUE",
    "statut": "EN_ATTENTE_CONFIGURATION",
    "valeurActuelle": 1000.0,
    "dateCreation": "2024-01-15T10:30:00"
  }
}
```

#### GET /api/compteurs
Récupérer tous les compteurs de l'utilisateur connecté.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "reference": "CMP-2024-001",
      "adresse": "123 Rue de la République, Dakar",
      "typeCompteur": "CLASSIQUE",
      "statut": "ACTIF",
      "valeurActuelle": 1250.5,
      "dateCreation": "2024-01-15T10:30:00"
    }
  ]
}
```

#### GET /api/compteurs/{id}
Récupérer un compteur par son ID.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "reference": "CMP-2024-001",
    "adresse": "123 Rue de la République, Dakar",
    "typeCompteur": "CLASSIQUE",
    "statut": "ACTIF",
    "valeurActuelle": 1250.5,
    "indexInitial": 1000.0,
    "indexPrecedent": 1200.0,
    "dateCreation": "2024-01-15T10:30:00"
  }
}
```

#### DELETE /api/compteurs/{id}
Désactiver un compteur.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "message": "Compteur désactivé avec succès",
  "data": {
    "id": 1,
    "statut": "INACTIF"
  }
}
```

#### POST /api/compteurs/releves
Ajouter un relevé manuel.

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "compteurId": 1,
  "valeur": 1300.0,
  "source": "MANUEL",
  "dateReleve": "2024-01-20T14:30:00"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Relevé ajouté avec succès",
  "data": {
    "id": 1,
    "valeur": 1300.0,
    "source": "MANUEL",
    "dateReleve": "2024-01-20T14:30:00",
    "compteurId": 1
  }
}
```

#### GET /api/compteurs/{id}/releves
Récupérer l'historique des relevés d'un compteur.

**Headers:**
```
Authorization: Bearer <token>
```

**Request Parameters:**
```
startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "valeur": 1300.0,
      "source": "MANUEL",
      "dateReleve": "2024-01-20T14:30:00",
      "consommation": 100.0
    }
  ]
}
```

#### POST /api/compteurs/recharge
Recharger un compteur Cash Power.

**Request Body:**
```json
{
  "compteurId": 2,
  "montant": 10000.0,
  "codeRecharge": "CP-123456789"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Recharge effectuée avec succès",
  "data": {
    "id": 2,
    "creditActuel": 15000.0,
    "dateDerniereRecharge": "2024-01-20T15:00:00"
  }
}
```

#### GET /api/compteurs/{id}/stats
Récupérer les statistiques de consommation.

**Request Parameters:**
```
periode=month
```

**Response:**
```json
{
  "success": true,
  "data": {
    "periode": "month",
    "consommationTotale": 450.5,
    "consommationMoyenne": 15.02,
    "consommationMax": 25.8,
    "consommationMin": 8.2,
    "nombreReleves": 30
  }
}
```

#### POST /api/compteurs/{id}/ocr
Ajouter un relevé par OCR.

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:**
```json
"data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQ..."
```

**Response:**
```json
{
  "success": true,
  "message": "Relevé OCR créé avec succès",
  "data": {
    "id": 2,
    "valeur": 1350.0,
    "source": "ESP32_CAM",
    "dateReleve": "2024-01-20T16:00:00",
    "confidence": 0.95
  }
}
```

---

### Gestion des relevés

#### POST /api/readings/manual
Créer un relevé manuel.

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "meterId": 1,
  "value": 1400.0,
  "date": "2024-01-21T10:00:00"
}
```

**Response:**
```json
{
  "id": 1,
  "meterId": 1,
  "value": 1400.0,
  "source": "MANUAL",
  "date": "2024-01-21T10:00:00",
  "consumption": 50.0
}
```

#### POST /api/readings/upload
Uploader une image pour relevé.

**Headers:**
```
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

**Request Parameters:**
```
meterId=1&file=@image.jpg
```

**Response:**
```json
{
  "id": 2,
  "meterId": 1,
  "value": 1425.0,
  "source": "IMAGE",
  "date": "2024-01-21T11:00:00",
  "imageUrl": "/api/images/123"
}
```

#### POST /api/readings/sensor
Créer un relevé capteur.

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "meterId": 1,
  "sensorId": "SENSOR-001",
  "value": 1450.0,
  "date": "2024-01-21T12:00:00"
}
```

**Response:**
```json
{
  "id": 3,
  "meterId": 1,
  "sensorId": "SENSOR-001",
  "value": 1450.0,
  "source": "SENSOR",
  "date": "2024-01-21T12:00:00"
}
```

#### GET /api/readings/meters/{meterId}
Lister les relevés d'un compteur.

**Headers:**
```
Authorization: Bearer <token>
```

**Request Parameters:**
```
page=0&size=20
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "meterId": 1,
      "value": 1400.0,
      "source": "MANUAL",
      "date": "2024-01-21T10:00:00"
    }
  ],
  "pageable": {
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

#### GET /api/readings/meters/{meterId}/latest
Dernier relevé d'un compteur.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
{
  "id": 3,
  "meterId": 1,
  "value": 1450.0,
  "source": "SENSOR",
  "date": "2024-01-21T12:00:00",
  "consumption": 25.0
}
```

---

### Gestion des dispositifs IoT

#### POST /api/devices/scan
Scanner et enregistrer un module via QR code.

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "qrCodeValue": "ESP32-DEVICE-001-ABC123",
  "deviceCode": "ESP32-001",
  "serialNumber": "SN123456789"
}
```

**Response:**
```json
{
  "id": 1,
  "deviceCode": "ESP32-001",
  "qrCodeValue": "ESP32-DEVICE-001-ABC123",
  "serialNumber": "SN123456789",
  "statut": "NON_CONFIGURE",
  "configured": false
}
```

#### POST /api/devices/{deviceCode}/associate
Associer un module à un compteur.

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "compteurId": 1,
  "captureInterval": 3600
}
```

**Response:**
```json
{
  "id": 1,
  "deviceCode": "ESP32-001",
  "statut": "ACTIF",
  "configured": true,
  "compteur": {
    "id": 1,
    "reference": "CMP-2024-001"
  },
  "captureInterval": 3600
}
```

#### POST /api/devices/{deviceCode}/handshake
Handshake initial du module avec le backend.

**Request Body:**
```json
{
  "firmwareVersion": "1.0.0",
  "ipAddress": "192.168.1.100",
  "wifiSsid": "HomeNetwork"
}
```

**Response:**
```json
{
  "id": 1,
  "deviceCode": "ESP32-001",
  "statut": "ACTIF",
  "lastSeenAt": "2024-01-21T13:00:00",
  "ipAddress": "192.168.1.100"
}
```

#### POST /api/devices/{deviceCode}/heartbeat
Heartbeat du module pour maintenir la connexion.

**Request Body:**
```json
{
  "batteryLevel": 85,
  "signalStrength": -45,
  "temperature": 25.5
}
```

**Response:**
```json
{
  "id": 1,
  "deviceCode": "ESP32-001",
  "statut": "ACTIF",
  "lastSeenAt": "2024-01-21T13:05:00",
  "enLigne": true
}
```

#### GET /api/devices/{deviceCode}/status
Obtenir le statut d'un module.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
{
  "id": 1,
  "deviceCode": "ESP32-001",
  "statut": "ACTIF",
  "configured": true,
  "lastSeenAt": "2024-01-21T13:05:00",
  "enLigne": true,
  "ipAddress": "192.168.1.100",
  "captureInterval": 3600
}
```

#### GET /api/devices/my
Lister les modules de l'utilisateur connecté.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
[
  {
    "id": 1,
    "deviceCode": "ESP32-001",
    "statut": "ACTIF",
    "configured": true,
    "enLigne": true,
    "compteur": {
      "id": 1,
      "reference": "CMP-2024-001"
    }
  }
]
```

#### PUT /api/devices/{deviceCode}/capture-interval
Mettre à jour l'intervalle de capture.

**Headers:**
```
Authorization: Bearer <token>
```

**Request Parameters:**
```
interval=1800
```

**Response:**
```json
{
  "id": 1,
  "deviceCode": "ESP32-001",
  "captureInterval": 1800,
  "message": "Intervalle de capture mis à jour"
}
```

---

### Gestion des images

#### GET /api/images/{imageId}
Télécharger une image de relevé.

**Response:**
```
Content-Type: image/jpeg
Content-Disposition: inline; filename="meter_reading_123.jpg"

[Binary image data]
```

---

### Alertes et notifications

#### GET /api/alertes
Récupérer les alertes de l'utilisateur.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
[
  {
    "id": 1,
    "type": "SEUIL_CREDIT",
    "message": "Votre crédit Cash Power est inférieur à 5000 FCFA",
    "dateCreation": "2024-01-21T14:00:00",
    "lue": false,
    "compteur": {
      "id": 2,
      "reference": "CMP-2024-002"
    }
  }
]
```

#### GET /api/alertes/non-lues
Récupérer les alertes non lues.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
[
  {
    "id": 1,
    "type": "SEUIL_CREDIT",
    "message": "Votre crédit Cash Power est inférieur à 5000 FCFA",
    "dateCreation": "2024-01-21T14:00:00",
    "lue": false
  }
]
```

#### PATCH /api/alertes/{id}/lue
Marquer une alerte comme lue.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```
204 No Content
```

#### PATCH /api/alertes/tout-lu
Marquer toutes les alertes comme lues.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```
204 No Content
```

#### GET /api/notifications
Récupérer les notifications de l'utilisateur.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
[
  {
    "id": 1,
    "titre": "Nouveau relevé enregistré",
    "message": "Un nouveau relevé a été enregistré pour votre compteur CMP-2024-001",
    "dateCreation": "2024-01-21T15:00:00",
    "type": "INFO",
    "lue": false
  }
]
```

---

## Modèles de données

### User
```json
{
  "id": 1,
  "nom": "Doe",
  "prenom": "John",
  "email": "john.doe@example.com",
  "motDePasse": "hashed_password",
  "telephone": "+221123456789",
  "actif": true,
  "role": {
    "id": 1,
    "name": "USER"
  },
  "seuilAlerteCredit": 5000.0,
  "seuilAlerteAnomalie": 30.0,
  "notificationPush": true,
  "notificationSms": false,
  "notificationEmail": true
}
```

### Compteur
```json
{
  "id": 1,
  "reference": "CMP-2024-001",
  "adresse": "123 Rue de la République, Dakar",
  "typeCompteur": "CLASSIQUE",
  "statut": "ACTIF",
  "modeLectureConfigure": "ESP32_CAM",
  "proprietaire": {
    "id": 1,
    "nomComplet": "John Doe"
  },
  "dateInitialisation": "2024-01-15",
  "actif": true,
  "indexActuel": 1250.5,
  "indexPrecedent": 1200.0,
  "indexInitial": 1000.0,
  "tauxConsommationMensuel": 50.2
}
```

### ModuleESP32
```json
{
  "id": 1,
  "deviceCode": "ESP32-001",
  "qrCodeValue": "ESP32-DEVICE-001-ABC123",
  "serialNumber": "SN123456789",
  "statut": "ACTIF",
  "configured": true,
  "lastSeenAt": "2024-01-21T13:05:00",
  "firmwareVersion": "1.0.0",
  "captureInterval": 3600,
  "wifiSsid": "HomeNetwork",
  "ipAddress": "192.168.1.100",
  "compteur": {
    "id": 1,
    "reference": "CMP-2024-001"
  },
  "proprietaire": {
    "id": 1,
    "nomComplet": "John Doe"
  }
}
```

### Releve
```json
{
  "id": 1,
  "valeur": 1300.0,
  "source": "ESP32_CAM",
  "dateReleve": "2024-01-20T14:30:00",
  "compteur": {
    "id": 1,
    "reference": "CMP-2024-001"
  },
  "image": {
    "id": 1,
    "fileName": "meter_reading_123.jpg",
    "filePath": "/uploads/images/2024/01/meter_reading_123.jpg"
  }
}
```

---

## Sécurité

### Authentification JWT
- Token valide pendant 24 heures
- Format : `Bearer <token>`
- Contient : userId, role, expiration

### Rôles
- **USER** : Utilisateur standard
- **ADMIN** : Administrateur système

### Validation des entrées
- Validation Bean Validation (Jakarta)
- Regex pour mots de passe : 8+ caractères, majuscule, minuscule, chiffre, caractère spécial
- Email format validation

### CORS
- Origines autorisées : `*` (configuration de développement)
- Méthodes autorisées : GET, POST, PUT, DELETE, PATCH
- Headers autorisés : Authorization, Content-Type

---

## Conclusion

Cette documentation complète présente l'ensemble des fonctionnalités de l'API MeterEye AI Backend. Le système offre une solution complète pour la gestion intelligente de compteurs électriques avec intégration IoT, traitement d'images et alertes automatiques.

Pour toute question ou mise à jour, veuillez contacter l'équipe de développement MeterEye AI.
