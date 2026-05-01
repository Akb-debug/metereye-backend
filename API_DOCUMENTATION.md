# Documentation des APIs — MeterEye AI Backend

**Base URL :** `http://localhost:8080`  
**Format :** JSON (sauf upload de fichier)  
**Authentification :** JWT Bearer Token (sauf endpoints publics)

Pour tous les endpoints protégés, inclure le header :
```
Authorization: Bearer <token>
```

---

## Table des matières

1. [Authentification](#1-authentification)
2. [Compteurs](#2-compteurs)
3. [Configuration des Compteurs](#3-configuration-des-compteurs)
4. [Relevés (Readings)](#4-relevés-readings)
5. [Modules ESP32 (QR Code)](#5-modules-esp32-qr-code)
6. [Modules Bluetooth](#6-modules-bluetooth)
7. [Module Devices (Bluetooth avancé)](#7-module-devices-bluetooth-avancé)
8. [Utilisateurs](#8-utilisateurs)
9. [Images](#9-images)
10. [Codes de réponse et énumérations](#10-codes-de-réponse-et-énumérations)

---

## 1. Authentification

> Base path : `/api/auth` — Endpoints **publics**, pas de token requis

---

### 1.1 Inscription

**`POST /api/auth/register`**

Crée un nouveau compte utilisateur. Les rôles `ADMIN` et `PERSONNEL` ne peuvent pas être auto-attribués.

**Corps de la requête :**
```json
{
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "jean.dupont@email.com",
  "motDePasse": "Azerty@123",
  "telephone": "+237699000000",
  "role": "PROPRIETAIRE"
}
```

| Champ | Type | Obligatoire | Règles |
|-------|------|-------------|--------|
| `nom` | string | oui | Non vide |
| `prenom` | string | oui | Non vide |
| `email` | string | oui | Format email valide |
| `motDePasse` | string | oui | Min 8 caractères, 1 majuscule, 1 minuscule, 1 chiffre, 1 caractère spécial (`@#$%^&+=?!`) |
| `telephone` | string | non | — |
| `role` | string | oui | `PROPRIETAIRE` ou `LOCATAIRE` uniquement |

**Réponse `201 Created` :**
```json
{
  "token": null,
  "role": "PROPRIETAIRE",
  "nomComplet": "Dupont Jean",
  "userId": 1
}
```

> Le token n'est pas retourné à l'inscription. Appeler `/login` pour obtenir un token.

**Erreurs :**
- `400` — Email déjà utilisé
- `400` — Rôle invalide ou tentative de s'inscrire en ADMIN/PERSONNEL

---

### 1.2 Connexion

**`POST /api/auth/login`**

Authentifie un utilisateur et retourne un token JWT. Révoque tous les anciens tokens actifs.

**Corps de la requête :**
```json
{
  "email": "jean.dupont@email.com",
  "motDePasse": "Azerty@123"
}
```

**Réponse `200 OK` :**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "PROPRIETAIRE",
  "nomComplet": "Dupont Jean",
  "userId": 1
}
```

**Erreurs :**
- `400` — Email ou mot de passe incorrect

---

### 1.3 Profil du compte connecté

**`GET /api/auth/me`**  
🔒 Token requis

Retourne les informations de l'utilisateur à partir de son token.

**Réponse `200 OK` :**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "PROPRIETAIRE",
  "nomComplet": "Dupont Jean",
  "userId": 1
}
```

---

## 2. Compteurs

> Base path : `/api/compteurs` — 🔒 Token requis pour tous les endpoints

---

### 2.1 Créer un compteur

**`POST /api/compteurs`**

Enregistre un nouveau compteur associé à l'utilisateur connecté. Un relevé initial est automatiquement créé.

**Corps de la requête :**
```json
{
  "reference": "CPT-001",
  "adresse": "Rue des Fleurs, Yaoundé",
  "typeCompteur": "CLASSIQUE",
  "valeurInitiale": 1500.0
}
```

| Champ | Type | Obligatoire | Valeurs |
|-------|------|-------------|---------|
| `reference` | string | oui | Unique dans le système |
| `adresse` | string | oui | — |
| `typeCompteur` | string | oui | `CLASSIQUE`, `CASH_POWER` |
| `valeurInitiale` | number | oui | Nombre positif |

**Réponse `201 Created` :**
```json
{
  "success": true,
  "message": "Créé avec succès",
  "data": {
    "id": 1,
    "reference": "CPT-001",
    "adresse": "Rue des Fleurs, Yaoundé",
    "typeCompteur": "CLASSIQUE",
    "valeurActuelle": 1500.0,
    "proprietaireNom": "Dupont Jean",
    "proprietaireId": 1,
    "dateInitialisation": "2026-04-25",
    "actif": true,
    "dateCreation": "2026-04-25T10:00:00"
  }
}
```

**Erreurs :**
- `400` — Référence déjà existante

---

### 2.2 Lister mes compteurs

**`GET /api/compteurs`**

Retourne tous les compteurs actifs de l'utilisateur connecté.

**Réponse `200 OK` :**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "reference": "CPT-001",
      "adresse": "Rue des Fleurs, Yaoundé",
      "typeCompteur": "CLASSIQUE",
      "valeurActuelle": 1620.5,
      "proprietaireNom": "Dupont Jean",
      "proprietaireId": 1,
      "actif": true,
      "dateCreation": "2026-04-25T10:00:00"
    }
  ]
}
```

---

### 2.3 Récupérer un compteur par ID

**`GET /api/compteurs/{id}`**

Seul le propriétaire, le PERSONNEL ou l'ADMIN peut accéder à un compteur.

**Paramètre :** `id` — ID du compteur (Long)

**Réponse `200 OK` :**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "reference": "CPT-001",
    "adresse": "Rue des Fleurs, Yaoundé",
    "typeCompteur": "CLASSIQUE",
    "valeurActuelle": 1620.5,
    "actif": true
  }
}
```

**Erreurs :**
- `404` — Compteur non trouvé
- `404` — Non autorisé (pas propriétaire)

---

### 2.4 Désactiver un compteur

**`DELETE /api/compteurs/{id}`**

Désactivation logique (soft delete) — le compteur et ses relevés restent en base.

**Paramètre :** `id` — ID du compteur

**Réponse `200 OK` :**
```json
{
  "success": true,
  "message": "Compteur désactivé avec succès",
  "data": {
    "id": 1,
    "reference": "CPT-001",
    "actif": false
  }
}
```

---

### 2.5 Ajouter un relevé manuel

**`POST /api/compteurs/releves`**

Enregistre une valeur saisie manuellement. La consommation est calculée automatiquement.

**Corps de la requête :**
```json
{
  "compteurId": 1,
  "valeur": 1620.5,
  "commentaire": "Relevé mensuel avril"
}
```

**Réponse `201 Created` :**
```json
{
  "success": true,
  "data": {
    "id": 10,
    "valeur": 1620.5,
    "dateTime": "2026-04-25T14:30:00",
    "consommationCalculee": 120.5,
    "source": "MANUEL",
    "statut": "VALIDE",
    "commentaire": "Relevé mensuel avril",
    "compteurId": 1,
    "compteurReference": "CPT-001"
  }
}
```

**Erreurs :**
- `400` — Valeur inférieure à la précédente (compteur CLASSIQUE)
- `400` — Compteur non configuré pour le mode MANUAL
- `400` — Compteur inactif

---

### 2.6 Historique des relevés d'un compteur

**`GET /api/compteurs/{id}/releves`**

Retourne tous les relevés, avec filtrage optionnel par période.

**Paramètres de requête :**

| Paramètre | Type | Obligatoire | Format |
|-----------|------|-------------|--------|
| `startDate` | datetime | non | `2026-01-01T00:00:00` |
| `endDate` | datetime | non | `2026-04-25T23:59:59` |

**Exemple :**
```
GET /api/compteurs/1/releves?startDate=2026-01-01T00:00:00&endDate=2026-04-25T23:59:59
```

**Réponse `200 OK` :**
```json
{
  "success": true,
  "data": [
    {
      "id": 10,
      "valeur": 1620.5,
      "dateTime": "2026-04-25T14:30:00",
      "consommationCalculee": 120.5,
      "source": "MANUEL",
      "statut": "VALIDE",
      "commentaire": "Relevé mensuel avril",
      "imageUrl": null,
      "compteurId": 1,
      "compteurReference": "CPT-001"
    }
  ]
}
```

---

### 2.7 Statistiques de consommation

**`GET /api/compteurs/{id}/stats`**

Calcule la consommation réelle sur les 1, 7 et 30 derniers jours.

**Paramètre de requête :**

| Paramètre | Type | Défaut | Valeurs |
|-----------|------|--------|---------|
| `periode` | string | `month` | `day`, `week`, `month` |

**Réponse `200 OK` :**
```json
{
  "success": true,
  "data": {
    "consommationJour": 12.5,
    "consommationSemaine": 87.3,
    "consommationMois": 342.1,
    "consommationMoyenneJour": 11.4,
    "creditRestant": 4500.0,
    "dateEstimationEpuisement": "2026-05-20T10:00:00",
    "consommationParJour": null
  }
}
```

> `creditRestant` et `dateEstimationEpuisement` sont présents uniquement pour les compteurs `CASH_POWER`.

---

### 2.8 Recharger un compteur Cash Power

**`POST /api/compteurs/recharge`**

Ajoute du crédit à un compteur de type `CASH_POWER`. Un relevé de recharge est créé automatiquement.

**Corps de la requête :**
```json
{
  "compteurId": 2,
  "montant": 5000.0,
  "codeRecharge": "1234-5678-9012-3456"
}
```

**Réponse `200 OK` :**
```json
{
  "success": true,
  "message": "Recharge effectuée avec succès",
  "data": {
    "id": 2,
    "reference": "CPT-002",
    "typeCompteur": "CASH_POWER",
    "valeurActuelle": 9500.0,
    "actif": true
  }
}
```

**Erreurs :**
- `400` — Compteur n'est pas de type CASH_POWER

---

## 3. Configuration des Compteurs

> Base path : `/api/compteurs` — 🔒 Token requis

---

### 3.1 Configurer le mode de lecture

**`POST /api/compteurs/{id}/mode-lecture`**

Définit comment le compteur sera relevé. Doit correspondre au type de module associé.

**Corps de la requête :**
```json
{
  "modeLecture": "ESP32_CAM"
}
```

| Valeur `modeLecture` | Module requis |
|----------------------|---------------|
| `MANUAL` | Aucun — saisie utilisateur |
| `ESP32_CAM` | Module ESP32-CAM |
| `SENSOR` | Module ESP32-PZEM004T |

**Réponse `200 OK` :**
```
Compteur CPT-001 configuré en mode ESP32_CAM
```

**Erreurs :**
- `400` — Non autorisé (pas propriétaire)

---

### 3.2 Réinitialiser un compteur classique

**`POST /api/compteurs/{id}/reinitialiser`**

Remet le compteur à zéro relatif (ex : changement de locataire). Uniquement pour les compteurs `CLASSIQUE`.

**Corps de la requête :**
```json
{
  "motif": "Changement de locataire - avril 2026"
}
```

**Réponse `200 OK` :**
```
Compteur CPT-001 réinitialisé: Changement de locataire - avril 2026
```

**Erreurs :**
- `400` — Seuls les compteurs CLASSIQUE peuvent être réinitialisés

---

### 3.3 Statut de configuration d'un compteur

**`GET /api/compteurs/{id}/statut-configuration`**

Vérifie si le compteur est prêt à recevoir des relevés.

**Réponse `200 OK` :**
```json
{
  "reference": "CPT-001",
  "statut": "ACTIF",
  "modeLectureConfigure": "ESP32_CAM",
  "configurePourLecture": true
}
```

| Valeur `statut` | Description |
|-----------------|-------------|
| `EN_ATTENTE_CONFIGURATION` | Compteur créé, mode de lecture non défini |
| `ACTIF` | Compteur actif et opérationnel |
| `INACTIF` | Compteur désactivé |

---

## 4. Relevés (Readings)

> Base path : `/api/readings` — 🔒 Token requis

---

### 4.1 Créer un relevé manuel

**`POST /api/readings/manual`**

**Corps de la requête :**
```json
{
  "meterId": 1,
  "value": 1720.5,
  "comment": "Relevé avril"
}
```

**Réponse `200 OK` :**
```json
{
  "id": 11,
  "value": 1720.5,
  "dateTime": "2026-04-25T15:00:00",
  "source": "MANUEL",
  "statut": "VALIDE",
  "consumption": 100.0,
  "comment": "Relevé avril",
  "meterId": 1,
  "meterReference": "CPT-001"
}
```

---

### 4.2 Upload image pour relevé OCR

**`POST /api/readings/upload`**

Envoie une photo du compteur. L'OCR extrait automatiquement la valeur affichée.

**Requête :** `multipart/form-data`

| Champ | Type | Description |
|-------|------|-------------|
| `meterId` | Long | ID du compteur |
| `file` | File | Image JPEG ou PNG du compteur |

**Exemple curl :**
```bash
curl -X POST http://localhost:8080/api/readings/upload \
  -H "Authorization: Bearer <token>" \
  -F "meterId=1" \
  -F "file=@photo_compteur.jpg"
```

**Réponse `200 OK` — OCR réussi :**
```json
{
  "id": 12,
  "value": 1820.0,
  "dateTime": "2026-04-25T15:30:00",
  "source": "ESP32_CAM",
  "statut": "VALIDE",
  "consumption": 99.5,
  "imageUrl": "/api/images/5",
  "ocrConfidence": 0.97,
  "meterId": 1,
  "meterReference": "CPT-001"
}
```

**Réponse `200 OK` — OCR échoué :**
```json
{
  "id": 12,
  "source": "ESP32_CAM",
  "statut": "ERREUR",
  "comment": "Impossible de lire la valeur OCR"
}
```

> `ocrConfidence` est un score entre 0 et 1. Plus il est proche de 1, plus la lecture est fiable.

---

### 4.3 Relevé capteur automatique (ESP32-PZEM004T)

**`POST /api/readings/sensor`**

Enregistre un relevé envoyé automatiquement par le capteur d'énergie physique.

**Corps de la requête :**
```json
{
  "meterId": 1,
  "value": 1920.0,
  "sensorId": "PZEM-A1B2C3"
}
```

**Réponse `200 OK` :**
```json
{
  "id": 13,
  "value": 1920.0,
  "dateTime": "2026-04-25T16:00:00",
  "source": "SENSOR",
  "statut": "VALIDE",
  "consumption": 100.0,
  "comment": "Capteur: PZEM-A1B2C3",
  "meterId": 1,
  "meterReference": "CPT-001"
}
```

---

### 4.4 Lister les relevés d'un compteur (paginé)

**`GET /api/readings/meters/{meterId}`**

**Paramètres de requête :**

| Paramètre | Type | Défaut | Description |
|-----------|------|--------|-------------|
| `page` | int | `0` | Numéro de page (commence à 0) |
| `size` | int | `20` | Éléments par page |

**Exemple :**
```
GET /api/readings/meters/1?page=0&size=10
```

**Réponse `200 OK` :**
```json
{
  "content": [
    {
      "id": 13,
      "value": 1920.0,
      "dateTime": "2026-04-25T16:00:00",
      "source": "SENSOR",
      "statut": "VALIDE",
      "consumption": 100.0,
      "meterId": 1,
      "meterReference": "CPT-001"
    }
  ],
  "totalElements": 13,
  "totalPages": 2,
  "size": 10,
  "number": 0,
  "first": true,
  "last": false
}
```

---

### 4.5 Dernier relevé d'un compteur

**`GET /api/readings/meters/{meterId}/latest`**

**Réponse `200 OK` :**
```json
{
  "id": 13,
  "value": 1920.0,
  "dateTime": "2026-04-25T16:00:00",
  "source": "SENSOR",
  "statut": "VALIDE",
  "consumption": 100.0,
  "meterId": 1,
  "meterReference": "CPT-001"
}
```

**Erreurs :**
- `500` — Aucun relevé enregistré pour ce compteur

---

## 5. Modules ESP32 (QR Code)

> Base path : `/api/devices` — 🔒 Token requis  
> Système basé sur scan de QR code. Pour les nouveaux déploiements, préférer `/api/bluetooth`.

---

### 5.1 Scanner et enregistrer un module

**`POST /api/devices/scan`**

Enregistre un module ESP32 après scan de son QR code.

**Corps de la requête :**
```json
{
  "deviceCode": "QR-ABC123",
  "serialNumber": "SN-00042",
  "firmwareVersion": "1.2.0"
}
```

**Réponse `200 OK` :**
```json
{
  "deviceCode": "QR-ABC123",
  "serialNumber": "SN-00042",
  "statut": "NON_CONFIGURE",
  "configured": false,
  "proprietaireId": 1
}
```

---

### 5.2 Associer un module à un compteur

**`POST /api/devices/{deviceCode}/associate`**

Lie le module scanné à un compteur et lui envoie la configuration réseau.

**Corps de la requête :**
```json
{
  "meterId": 1,
  "captureInterval": 3600,
  "wifiSsid": "MonWiFi",
  "wifiPassword": "motdepasse123"
}
```

**Réponse `200 OK` :**
```json
{
  "deviceCode": "QR-ABC123",
  "statut": "EN_CONFIGURATION",
  "configured": true,
  "compteurId": 1,
  "captureInterval": 3600
}
```

---

### 5.3 Handshake initial du module

**`POST /api/devices/{deviceCode}/handshake`**

Appelé par le firmware du module ESP32 au premier démarrage sur le réseau WiFi.

**Corps de la requête :**
```json
{
  "ipAddress": "192.168.1.45",
  "firmwareVersion": "1.2.0",
  "wifiSsid": "MonWiFi"
}
```

**Réponse `200 OK` :**
```json
{
  "deviceCode": "QR-ABC123",
  "statut": "ACTIF",
  "configured": true,
  "ipAddress": "192.168.1.45",
  "lastSeenAt": "2026-04-25T10:00:00"
}
```

---

### 5.4 Heartbeat du module

**`POST /api/devices/{deviceCode}/heartbeat`**

Envoyé périodiquement par le module pour indiquer qu'il est en ligne.

**Corps de la requête :**
```json
{
  "signalStrength": -65,
  "freeHeapMemory": 120000
}
```

**Réponse `200 OK` :**
```json
{
  "deviceCode": "QR-ABC123",
  "statut": "ACTIF",
  "lastSeenAt": "2026-04-25T10:15:00"
}
```

---

### 5.5 Statut d'un module

**`GET /api/devices/{deviceCode}/status`**

**Réponse `200 OK` :**
```json
{
  "deviceCode": "QR-ABC123",
  "statut": "ACTIF",
  "configured": true,
  "lastSeenAt": "2026-04-25T10:15:00",
  "ipAddress": "192.168.1.45",
  "firmwareVersion": "1.2.0",
  "compteurId": 1
}
```

---

### 5.6 Lister mes modules

**`GET /api/devices/my`**

**Réponse `200 OK` :** Tableau de modules, même structure que [5.5](#55-statut-dun-module).

---

### 5.7 Modifier l'intervalle de capture

**`PUT /api/devices/{deviceCode}/capture-interval?interval=1800`**

| Paramètre | Type | Description |
|-----------|------|-------------|
| `interval` | Integer | Intervalle en secondes entre chaque capture |

**Réponse `200 OK` :**
```json
{
  "deviceCode": "QR-ABC123",
  "captureInterval": 1800
}
```

---

## 6. Modules Bluetooth

> Base path : `/api/bluetooth` — 🔒 Token requis  
> Système actuel de configuration des modules depuis l'application mobile via Bluetooth.

---

### 6.1 Scanner un module Bluetooth

**`POST /api/bluetooth/scan`**

Enregistre un module détecté via Bluetooth par l'application mobile.

**Corps de la requête :**
```json
{
  "bluetoothAddress": "AA:BB:CC:DD:EE:FF",
  "typeModule": "ESP32_CAM",
  "moduleName": "Module-Salon",
  "firmwareVersion": "2.1.0",
  "serialNumber": "SN-00042",
  "signalStrength": -55.0
}
```

> `userId` est injecté automatiquement depuis le token JWT — ne pas l'envoyer.

| Valeur `typeModule` | Description |
|---------------------|-------------|
| `ESP32_CAM` | Module caméra pour relevé optique |
| `ESP32_PZEM004T` | Module capteur d'énergie électrique |

**Réponse `201 Created` :**
```json
{
  "success": true,
  "data": {
    "deviceCode": "550e8400-e29b-41d4-a716-446655440000",
    "bluetoothAddress": "AA:BB:CC:DD:EE:FF",
    "typeModule": "ESP32_CAM",
    "statut": "NON_CONFIGURE",
    "configured": false,
    "firmwareVersion": "2.1.0",
    "proprietaireId": 1,
    "modeLectureAssocie": "ESP32_CAM"
  }
}
```

**Erreurs :**
- `400` — Adresse Bluetooth déjà enregistrée

---

### 6.2 Configurer et associer un module

**`POST /api/bluetooth/configure`**

Configure le module et l'associe à un compteur. Définit automatiquement le mode de lecture du compteur si non encore configuré.

**Corps de la requête (ESP32-CAM) :**
```json
{
  "bluetoothAddress": "AA:BB:CC:DD:EE:FF",
  "compteurId": 1,
  "captureInterval": 3600,
  "wifiSsid": "MonWiFi",
  "wifiPassword": "motdepasse123",
  "resolutionCamera": "2MP",
  "flashActive": true,
  "qualiteImage": 85,
  "angleCapture": 90
}
```

**Corps de la requête (ESP32-PZEM004T) :**
```json
{
  "bluetoothAddress": "AA:BB:CC:DD:EE:FF",
  "compteurId": 1,
  "captureInterval": 60,
  "wifiSsid": "MonWiFi",
  "wifiPassword": "motdepasse123",
  "seuilAlerte": 0.15,
  "facteurCorrection": 1.0,
  "modeCalibrage": "AUTO",
  "tensionMax": 500.0,
  "courantMax": 100.0,
  "puissanceMax": 22000.0
}
```

**Réponse `200 OK` :**
```json
{
  "success": true,
  "message": "Module configuré avec succès",
  "data": {
    "deviceCode": "550e8400-e29b-41d4-a716-446655440000",
    "bluetoothAddress": "AA:BB:CC:DD:EE:FF",
    "typeModule": "ESP32_CAM",
    "statut": "EN_CONFIGURATION",
    "configured": true,
    "captureInterval": 3600,
    "compteurId": 1,
    "compteurReference": "CPT-001",
    "modeLectureAssocie": "ESP32_CAM"
  }
}
```

**Erreurs :**
- `400` — Module non trouvé avec cette adresse Bluetooth
- `400` — Incompatibilité de mode : le compteur est déjà configuré pour un autre type de module

---

### 6.3 Modules disponibles (non configurés)

**`GET /api/bluetooth/available`**

Liste les modules de l'utilisateur pas encore associés à un compteur.

**Réponse `200 OK` :**
```json
{
  "success": true,
  "data": [
    {
      "deviceCode": "550e8400-e29b-41d4-a716-446655440000",
      "bluetoothAddress": "AA:BB:CC:DD:EE:FF",
      "typeModule": "ESP32_CAM",
      "statut": "NON_CONFIGURE",
      "configured": false,
      "modeLectureAssocie": "ESP32_CAM"
    }
  ]
}
```

---

### 6.4 Vérifier la compatibilité module/compteur

**`POST /api/bluetooth/compatibility`**

À appeler avant la configuration pour vérifier que le module est compatible avec le compteur cible.

**Corps de la requête :**
```json
{
  "bluetoothAddress": "AA:BB:CC:DD:EE:FF",
  "compteurId": 1
}
```

**Réponse `200 OK` :**
```json
{
  "success": true,
  "data": {
    "bluetoothAddress": "AA:BB:CC:DD:EE:FF",
    "compteurId": 1,
    "compatible": true,
    "message": "Module compatible avec le compteur"
  }
}
```

> Règle : un module `ESP32_CAM` est compatible avec un compteur en mode `ESP32_CAM`. Un compteur sans mode configuré est compatible avec n'importe quel module.

---

### 6.5 Informations d'un module par adresse Bluetooth

**`GET /api/bluetooth/{bluetoothAddress}`**

Exemple : `GET /api/bluetooth/AA:BB:CC:DD:EE:FF`

**Réponse `200 OK` :**
```json
{
  "success": true,
  "data": {
    "deviceCode": "550e8400-e29b-41d4-a716-446655440000",
    "bluetoothAddress": "AA:BB:CC:DD:EE:FF",
    "typeModule": "ESP32_CAM",
    "statut": "ACTIF",
    "configured": true,
    "lastSeenAt": "2026-04-25T10:15:00",
    "compteurId": 1,
    "compteurReference": "CPT-001"
  }
}
```

---

### 6.6 Configuration directe (scan + configure en une seule étape)

**`POST /api/bluetooth/direct-configure`**

Combine l'enregistrement et la configuration en une seule opération. Idéal pour les modules jamais enregistrés.

**Corps de la requête (ESP32-CAM) :**
```json
{
  "bluetoothAddress": "AA:BB:CC:DD:EE:FF",
  "typeModule": "ESP32_CAM",
  "compteurId": 1,
  "captureInterval": 3600,
  "firmwareVersion": "2.1.0",
  "serialNumber": "SN-00099",
  "wifiSsid": "MonWiFi",
  "wifiPassword": "motdepasse123",
  "resolutionCamera": "2MP",
  "flashActive": true,
  "qualiteImage": 85
}
```

**Corps de la requête (ESP32-PZEM004T) :**
```json
{
  "bluetoothAddress": "AA:BB:CC:DD:EE:FF",
  "typeModule": "ESP32_PZEM004T",
  "compteurId": 1,
  "captureInterval": 60,
  "firmwareVersion": "2.1.0",
  "serialNumber": "SN-00099",
  "wifiSsid": "MonWiFi",
  "wifiPassword": "motdepasse123",
  "seuilAlerte": 0.1,
  "facteurCorrection": 1.0,
  "modeCalibrage": "AUTO"
}
```

**Réponse `200 OK` :** Identique à [6.2](#62-configurer-et-associer-un-module).

---

### 6.7 Rechercher un module

**`GET /api/bluetooth/search/{bluetoothAddress}`**

Indique si un module existe, s'il appartient à l'utilisateur et s'il peut être configuré.

**Réponse `200 OK` :**
```json
{
  "success": true,
  "data": {
    "deviceCode": "550e8400-e29b-41d4-a716-446655440000",
    "bluetoothAddress": "AA:BB:CC:DD:EE:FF",
    "typeModule": "ESP32_CAM",
    "statut": "NON_CONFIGURE",
    "configured": false,
    "moduleName": "Module-ESP32_CAM",
    "exists": true,
    "belongsToUser": true,
    "canConfigure": true,
    "modeLectureAssocie": "ESP32_CAM"
  }
}
```

---

### 6.8 Supprimer un module non configuré

**`DELETE /api/bluetooth/{bluetoothAddress}`**

Supprime définitivement un module qui n'a pas encore été associé à un compteur.

**Réponse `200 OK` :**
```json
{
  "success": true,
  "message": "Module supprimé avec succès"
}
```

**Erreurs :**
- `400` — Impossible de supprimer un module déjà configuré
- `400` — Module n'appartient pas à l'utilisateur

---

## 7. Module Devices (Bluetooth avancé)

> Base path : `/api/module-devices` — 🔒 Token requis  
> Endpoints pour la gestion opérationnelle des modules après leur configuration initiale.

---

### 7.1 Scanner un module

**`POST /api/module-devices/scan`**

Identique à [6.1 Scanner un module Bluetooth](#61-scanner-un-module-bluetooth).

---

### 7.2 Handshake du module

**`POST /api/module-devices/{deviceCode}/handshake`**

Appelé par le firmware du module après avoir reçu sa configuration WiFi via Bluetooth. Passe le statut à `ACTIF`.

**Corps de la requête :**
```json
{
  "firmwareVersion": "2.1.0",
  "ipAddress": "192.168.1.60",
  "wifiSsid": "MonWiFi"
}
```

**Réponse `200 OK` :**
```json
{
  "success": true,
  "message": "Handshake réussi",
  "data": {
    "deviceCode": "550e8400-e29b-41d4-a716-446655440000",
    "statut": "ACTIF",
    "configured": true,
    "ipAddress": "192.168.1.60",
    "firmwareVersion": "2.1.0",
    "lastSeenAt": "2026-04-25T10:00:00"
  }
}
```

---

### 7.3 Heartbeat

**`POST /api/module-devices/{deviceCode}/heartbeat`**

Signal de vie envoyé périodiquement. Remet le module `ACTIF` s'il était `HORS_LIGNE`.

**Réponse `200 OK` :**
```json
{
  "success": true,
  "message": "Heartbeat enregistré"
}
```

---

### 7.4 Statut complet d'un module

**`GET /api/module-devices/{deviceCode}/status`**

**Réponse `200 OK` :**
```json
{
  "success": true,
  "data": {
    "deviceCode": "550e8400-e29b-41d4-a716-446655440000",
    "bluetoothAddress": "AA:BB:CC:DD:EE:FF",
    "typeModule": "ESP32_CAM",
    "statut": "ACTIF",
    "configured": true,
    "lastSeenAt": "2026-04-25T10:15:00",
    "firmwareVersion": "2.1.0",
    "captureInterval": 3600,
    "wifiSsid": "MonWiFi",
    "ipAddress": "192.168.1.60",
    "proprietaireId": 1,
    "compteurId": 1,
    "compteurReference": "CPT-001",
    "modeLectureAssocie": "ESP32_CAM"
  }
}
```

---

### 7.5 Lister mes modules

**`GET /api/module-devices/my`**

**Réponse `200 OK` :** Tableau de modules, même structure que [7.4](#74-statut-complet-dun-module).

---

### 7.6 Modifier l'intervalle de capture

**`PUT /api/module-devices/{deviceCode}/capture-interval?interval=1800`**

| Paramètre | Type | Description |
|-----------|------|-------------|
| `interval` | Integer | Délai en secondes entre chaque capture/mesure |

**Réponse `200 OK` :**
```json
{
  "success": true,
  "message": "Intervalle de capture configuré"
}
```

---

### 7.7 Changer le mode de lecture (migration)

**`POST /api/module-devices/compteurs/{compteurId}/changer-mode`**

Migre un compteur vers un nouveau mode de lecture. Désactive l'ancienne configuration et conserve un historique du changement.

**Corps de la requête :**
```json
{
  "nouveauMode": "SENSOR",
  "motif": "Remplacement caméra par capteur PZEM après panne"
}
```

**Réponse `200 OK` :**
```json
{
  "success": true,
  "message": "Mode de lecture changé avec succès"
}
```

---

## 8. Utilisateurs

> Base path : `/api/users` — 🔒 Token requis

---

### 8.1 Profil de l'utilisateur connecté

**`GET /api/users/profile`**

**Réponse `200 OK` :**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "jean.dupont@email.com",
    "email": "jean.dupont@email.com",
    "role": "PROPRIETAIRE",
    "nomComplet": "Dupont Jean",
    "telephone": "+237699000000",
    "seuilAlerteCredit": 5000.0,
    "seuilAlerteAnomalie": 30.0,
    "notificationPush": true,
    "notificationSms": false,
    "notificationEmail": true
  }
}
```

---

### 8.2 Mettre à jour les seuils d'alerte

**`PUT /api/users/seuils?seuilCredit=3000&seuilAnomalie=25`**

| Paramètre | Type | Description |
|-----------|------|-------------|
| `seuilCredit` | Double | Crédit minimum avant alerte (FCFA) pour compteurs CASH_POWER |
| `seuilAnomalie` | Double | Variation en % déclenchant une alerte de consommation anormale |

**Réponse `200 OK` :** Profil complet avec les nouvelles valeurs (même structure que [8.1](#81-profil-de-lutilisateur-connecté)).

---

### 8.3 Mettre à jour les préférences de notification

**`PUT /api/users/notifications?push=true&sms=false&email=true`**

Tous les paramètres sont optionnels. Seuls les paramètres fournis sont mis à jour.

| Paramètre | Type | Description |
|-----------|------|-------------|
| `push` | Boolean | Notifications push mobile |
| `sms` | Boolean | Notifications par SMS |
| `email` | Boolean | Notifications par email |

**Réponse `200 OK` :** Profil complet avec les nouvelles valeurs (même structure que [8.1](#81-profil-de-lutilisateur-connecté)).

---

## 9. Images

> Base path : `/api/images` — 🔒 Token requis

---

### 9.1 Télécharger une image de relevé

**`GET /api/images/{imageId}`**

Retourne le fichier image binaire d'un relevé capturé par caméra.

**Réponse `200 OK` :**
- `Content-Type` : `image/jpeg` ou `image/png`
- `Content-Disposition` : `inline; filename="nom_fichier.jpg"`
- Body : Fichier image binaire

**Erreurs :**
- `500` — Image non trouvée sur le serveur

---

## 10. Codes de réponse et énumérations

### Codes HTTP

| Code | Signification |
|------|---------------|
| `200` | Succès |
| `201` | Ressource créée |
| `400` | Données invalides ou règle métier non respectée |
| `401` | Token absent ou expiré |
| `403` | Action non autorisée |
| `404` | Ressource non trouvée |
| `500` | Erreur interne du serveur |

---

### Structure de réponse standard (`BaseResponse`)

La majorité des endpoints retournent ce format :

**Succès :**
```json
{
  "success": true,
  "message": "Opération réussie",
  "status": 200,
  "data": { ... }
}
```

**Erreur :**
```json
{
  "success": false,
  "message": "Description de l'erreur",
  "status": 400,
  "data": null
}
```

---

### Rôles (`RoleName`)

| Valeur | Description | Auto-inscription |
|--------|-------------|-----------------|
| `PROPRIETAIRE` | Propriétaire de compteurs | Oui |
| `LOCATAIRE` | Accès en lecture à ses compteurs | Oui |
| `PERSONNEL` | Accès à tous les compteurs | Non (admin uniquement) |
| `ADMIN` | Administrateur système | Non (admin uniquement) |

---

### Types de compteur (`TypeCompteur`)

| Valeur | Description |
|--------|-------------|
| `CLASSIQUE` | Compteur à index croissant (kWh) |
| `CASH_POWER` | Compteur à prépaiement (crédit en FCFA) |

---

### Modes de lecture (`ModeLectureCompteur`)

| Valeur | Description | Module requis |
|--------|-------------|---------------|
| `MANUAL` | Saisie manuelle par l'utilisateur | Aucun |
| `ESP32_CAM` | Lecture automatique par caméra + OCR | ESP32-CAM |
| `SENSOR` | Mesure directe par capteur d'énergie | ESP32-PZEM004T |

---

### Sources de relevé (`SourceReleve`)

| Valeur | Description |
|--------|-------------|
| `MANUEL` | Saisi manuellement dans l'app |
| `ESP32_CAM` | Capturé et lu par caméra |
| `SENSOR` | Mesuré par le capteur PZEM004T |

---

### Statuts de relevé (`StatutReleve`)

| Valeur | Description |
|--------|-------------|
| `EN_ATTENTE` | Image reçue, traitement OCR en cours |
| `VALIDE` | Relevé confirmé et enregistré |
| `ERREUR` | Échec de la lecture OCR |

---

### Types de module (`TypeModuleDevice`)

| Valeur | Description | Mode lecture associé |
|--------|-------------|----------------------|
| `ESP32_CAM` | Module caméra OV2640 | `ESP32_CAM` |
| `ESP32_PZEM004T` | Module capteur d'énergie | `SENSOR` |

---

### Statuts de module (`StatutModuleDevice`)

| Valeur | Description |
|--------|-------------|
| `NON_CONFIGURE` | Module scanné mais jamais configuré |
| `EN_CONFIGURATION` | Configuration en cours (WiFi envoyé, handshake attendu) |
| `ACTIF` | Module en ligne et opérationnel |
| `HORS_LIGNE` | Module injoignable depuis plus de 5 minutes |
