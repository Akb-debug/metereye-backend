# 📚 MeterEye AI Backend - Documentation API Complète

## 🚀 **Introduction**

MeterEye AI est une plateforme de gestion intelligente de compteurs électriques avec support pour :
- **Compteurs CLASSIQUE** (index cumulatif)
- **Compteurs CASH POWER** (crédit restant)
- **Lectures multiples** : Manuel, ESP32-CAM, Capteur
- **Onboarding ESP32-CAM** avec QR code et configuration Wi-Fi

## 📋 **Ordre d'Utilisation Recommandé**

1. **Authentification** → Login/Register
2. **Création Compteur** → Ajouter un nouveau compteur
3. **Configuration Mode Lecture** → Manuel, ESP32-CAM ou Capteur
4. **Onboarding ESP32-CAM** (si applicable) → Scan QR + Association
5. **Saisie Relevés** → Manuel, Upload Image ou Capteur
6. **Monitoring** → Statut modules et historique

---

## 🔐 **1. Authentification**

### **Login (Étape 1)**
**Objectif** : Obtenir un token JWT pour les requêtes authentifiées
**Quand l'utiliser** : Au début de chaque session utilisateur
**Prérequis** : Email et mot de passe valides

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "motDePasse": "password123"
}
```

**Réponse :**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "role": "USER"
  }
}
```

**Important** : Conserver le `{token}` pour toutes les requêtes suivantes

**Headers requis pour toutes les requêtes :**
```
Authorization: Bearer {token}
Content-Type: application/json
```

### **Register (Étape 1 Alternative)**
**Objectif** : Créer un nouveau compte utilisateur
**Quand l'utiliser** : Nouvel utilisateur s'inscrit
**Prérequis** : Email unique, mot de passe sécurisé

```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "nouveau@example.com",
  "motDePasse": "password123",
  "nom": "Dupont",
  "prenom": "Jean",
  "telephone": "+221123456789"
}
```

### **Profil Utilisateur (Étape 1.5)**
**Objectif** : Récupérer les informations du profil connecté
**Quand l'utiliser** : Pour afficher le profil utilisateur
**Prérequis** : Token JWT valide

```http
GET /api/users/profile
Authorization: Bearer {token}
```

**Réponse :**
```json
{
  "id": 1,
  "email": "user@example.com",
  "nom": "Dupont",
  "prenom": "Jean",
  "telephone": "+221123456789",
  "seuilAlerteCredit": 100.0,
  "role": {
    "name": "USER"
  }
}
```

---

## 📊 **2. Gestion Compteurs**

### **Création Compteur (Étape 2)**
**Objectif** : Ajouter un nouveau compteur électrique au système
**Quand l'utiliser** : Après authentification, pour chaque nouveau compteur
**Prérequis** : Token JWT valide, référence unique

```http
POST /api/compteurs
Authorization: Bearer {token}
Content-Type: application/json

{
  "reference": "COMP-001",
  "adresse": "123 Rue de la République, Dakar",
  "typeCompteur": "CLASSIQUE",
  "valeurInitiale": 1500.0
}
```

**Réponse :**
```json
{
  "id": 1,
  "reference": "COMP-001",
  "adresse": "123 Rue de la République, Dakar",
  "typeCompteur": "CLASSIQUE",
  "valeurActuelle": 1500.0,
  "statut": "EN_ATTENTE_CONFIGURATION",
  "modeLectureConfigure": null,
  "dateInitialisation": "2026-04-13",
  "actif": true,
  "proprietaire": {
    "id": 1,
    "email": "user@example.com"
  }
}
```

**Important** : Le compteur est créé avec statut `EN_ATTENTE_CONFIGURATION`

### **Configuration Mode Lecture (Étape 3)**
**Objectif** : Définir comment les relevés seront saisis pour ce compteur
**Quand l'utiliser** : Après création du compteur, avant toute saisie de relevés
**Prérequis** : Token JWT valide, ID compteur valide

**Modes disponibles :**
- `MANUAL` : Saisie manuelle uniquement
- `ESP32_CAM` : Capture automatique via module ESP32-CAM
- `SENSOR` : Lecture via capteur IoT

```http
POST /api/compteurs/{id}/mode-lecture
Authorization: Bearer {token}
Content-Type: application/json

{
  "modeLecture": "ESP32_CAM",
  "commentaire": "Configuration pour lecture automatique"
}
```

**Important** : Une fois configuré, seul le mode défini peut accepter des relevés

### **Réinitialisation Compteur (CLASSIQUE) (Étape 3.5)**
**Objectif** : Remettre à zéro un compteur CLASSIQUE (remplacement, erreur)
**Quand l'utiliser** : En cas de remplacement ou d'erreur de comptage
**Prérequis** : Token JWT valide, compteur de type CLASSIQUE

```http
POST /api/compteurs/{id}/reinitialiser
Authorization: Bearer {token}
Content-Type: application/json

{
  "motif": "Remplacement compteur défectueux",
  "commentaire": "Ancien compteur changé le 13/04/2026"
}
```

**Important** : Sauvegarde l'ancienne valeur comme index précédent

### **Recharge Compteur (CASH_POWER) (Étape 3.6)**
**Objectif** : Ajouter du crédit à un compteur CASH POWER
**Quand l'utiliser** : Pour recharger un compteur prépayé
**Prérequis** : Token JWT valide, compteur de type CASH_POWER

```http
POST /api/compteurs/recharger
Authorization: Bearer {token}
Content-Type: application/json

{
  "compteurId": 1,
  "montant": 5000.0,
  "codeRecharge": "123456789"
}
```

### **Liste Compteurs (Étape 4)**
**Objectif** : Récupérer tous les compteurs de l'utilisateur connecté
**Quand l'utiliser** : Pour afficher le dashboard des compteurs
**Prérequis** : Token JWT valide

```http
GET /api/compteurs
Authorization: Bearer {token}
```

### **Détails Compteur (Étape 4.1)**
**Objectif** : Obtenir les informations détaillées d'un compteur spécifique
**Quand l'utiliser** : Pour afficher la page de détail d'un compteur
**Prérequis** : Token JWT valide, ID compteur valide

```http
GET /api/compteurs/{id}
Authorization: Bearer {token}
```

---

## 📷 **3. Gestion Modules ESP32-CAM**

### **Scan QR Code (Étape 4a)**
**Objectif** : Enregistrer un nouveau module ESP32-CAM dans le système
**Quand l'utiliser** : Après avoir scanné un QR code sur un module physique
**Prérequis** : Token JWT valide, QR code unique du module

```http
POST /api/devices/scan
Authorization: Bearer {token}
Content-Type: application/json

{
  "qrCode": "MET-ESP32-ABC123XYZ",
  "userId": 1
}
```

**Réponse :**
```json
{
  "deviceCode": "550e8400-e29b-41d4-a716-446655440000",
  "qrCodeValue": "MET-ESP32-ABC123XYZ",
  "statut": "NON_CONFIGURE",
  "configured": false,
  "proprietaireId": 1,
  "message": "Module enregistré, attente configuration Wi-Fi"
}
```

**Important** : Le deviceCode généré servira d'identifiant unique pour le module

### **Association Module-Compteur (Étape 4b)**
**Objectif** : Lier un module ESP32-CAM à un compteur spécifique
**Quand l'utiliser** : Après scan QR code, pour associer le module à un compteur
**Prérequis** : Token JWT valide, deviceCode valide, compteur configuré en mode ESP32_CAM

```http
POST /api/devices/{deviceCode}/associate
Authorization: Bearer {token}
Content-Type: application/json

{
  "compteurId": 1,
  "captureInterval": 3600
}
```

**Réponse :**
```json
{
  "deviceCode": "550e8400-e29b-41d4-a716-446655440000",
  "statut": "EN_CONFIGURATION",
  "configured": false,
  "compteurId": 1,
  "compteurReference": "COMP-001",
  "captureInterval": 3600,
  "message": "Module associé au compteur COMP-001"
}
```

**Important** : Le module passe en statut `EN_CONFIGURATION` et doit être configuré Wi-Fi

### **Handshake Module (Étape 4c - Côté Firmware)**
**Objectif** : Première connexion du module configuré au backend
**Quand l'utiliser** : Automatiquement par le firmware ESP32 après configuration Wi-Fi
**Prérequis** : Module associé à un compteur, configuration Wi-Fi terminée

```http
POST /api/devices/{deviceCode}/handshake
Content-Type: application/json

{
  "firmwareVersion": "1.0.0",
  "ipAddress": "192.168.1.100",
  "wifiSsid": "HomeWiFi"
}
```

**Réponse :**
```json
{
  "deviceCode": "550e8400-e29b-41d4-a716-446655440000",
  "statut": "ACTIF",
  "configured": true,
  "firmwareVersion": "1.0.0",
  "ipAddress": "192.168.1.100",
  "wifiSsid": "HomeWiFi",
  "lastSeenAt": "2026-04-13T10:30:00Z",
  "message": "Module connecté et opérationnel"
}
```

**Important** : Le module devient `ACTIF` et peut commencer à envoyer des relevés

### **Heartbeat Module (Étape 4d - Côté Firmware)**
**Objectif** : Maintenir la connexion et signaler que le module est en ligne
**Quand l'utiliser** : Automatiquement toutes les 60 secondes par le firmware
**Prérequis** : Module en statut ACTIF

```http
POST /api/devices/{deviceCode}/heartbeat
Content-Type: application/json

{
  "timestamp": "2026-04-13T10:30:00Z",
  "batteryLevel": 85,
  "signalStrength": -45,
  "temperature": 25.5
}
```

**Important** : Si pas de heartbeat pendant 5 minutes, module passe en HORS_LIGNE

### **Statut Module (Étape 5)**
**Objectif** : Vérifier l'état actuel d'un module ESP32-CAM
**Quand l'utiliser** : Pour afficher le statut dans l'interface frontend
**Prérequis** : Token JWT valide, deviceCode valide

```http
GET /api/devices/{deviceCode}/status
Authorization: Bearer {token}
```

**Réponse :**
```json
{
  "deviceCode": "550e8400-e29b-41d4-a716-446655440000",
  "qrCodeValue": "MET-ESP32-ABC123XYZ",
  "statut": "ACTIF",
  "configured": true,
  "lastSeenAt": "2026-04-13T10:30:00Z",
  "firmwareVersion": "1.0.0",
  "captureInterval": 3600,
  "ipAddress": "192.168.1.100",
  "wifiSsid": "HomeWiFi",
  "proprietaireId": 1,
  "compteurId": 1,
  "compteurReference": "COMP-001"
}
```

**Statuts possibles :**
- `NON_CONFIGURE` : Module scanné mais non configuré
- `EN_CONFIGURATION` : Module associé mais Wi-Fi non configuré
- `ACTIF` : Module opérationnel et connecté
- `HORS_LIGNE` : Perte de connexion
- `ERREUR` : Erreur de configuration
- `MAINTENANCE` : Mode maintenance

### **Liste Modules Utilisateur (Étape 5.1)**
**Objectif** : Récupérer tous les modules ESP32-CAM de l'utilisateur
**Quand l'utiliser** : Pour afficher le dashboard des modules
**Prérequis** : Token JWT valide

```http
GET /api/devices/my
Authorization: Bearer {token}
```

### **Configuration Intervalle Capture (Étape 5.2)**
**Objectif** : Modifier la fréquence de capture des images
**Quand l'utiliser** : Pour ajuster la fréquence des relevés automatiques
**Prérequis** : Token JWT valide, deviceCode valide, module ACTIF

```http
PUT /api/devices/{deviceCode}/capture-interval?interval=1800
Authorization: Bearer {token}
```

**Important** : Intervalles recommandés :
- `1800` : 30 minutes (fréquent)
- `3600` : 1 heure (standard)
- `7200` : 2 heures (économique)
- `86400` : 24 heures (minimal)

---

## 📖 **4. Gestion Relevés**

### **Relevé Manuel (Étape 6a)**
**Objectif** : Saisir manuellement une valeur de compteur
**Quand l'utiliser** : Pour les compteurs en mode MANUAL ou en backup
**Prérequis** : Token JWT valide, compteur configuré en mode MANUAL

**Validation automatique :**
- Compteur CLASSIQUE : valeur ≥ précédente
- Compteur CASH POWER : valeur acceptée (débit/crédit)
- Mode lecture compatible obligatoire

```http
POST /api/readings/manual
Authorization: Bearer {token}
Content-Type: application/json

{
  "compteurId": 1,
  "valeur": 1525.5,
  "commentaire": "Lecture manuelle du 13/04/2026"
}
```

**Réponse :**
```json
{
  "id": 123,
  "valeur": 1525.5,
  "dateTime": "2026-04-13T10:30:00Z",
  "source": "MANUEL",
  "statut": "VALIDE",
  "consommationCalculee": 25.5,
  "commentaire": "Lecture manuelle du 13/04/2026",
  "compteur": {
    "id": 1,
    "reference": "COMP-001"
  }
}
```

**Important** : La consommation est calculée automatiquement pour les compteurs CLASSIQUE

### **Upload Image ESP32-CAM (Étape 6b)**
**Objectif** : Uploader une image capturée par module ESP32-CAM
**Quand l'utiliser** : Pour les relevés automatiques via caméra
**Prérequis** : Token JWT valide, module ESP32-CAM ACTIF, image valide

**Processus :**
1. Upload image (JPG/PNG, max 10MB)
2. Traitement OCR pour extraction valeur
3. Validation automatique
4. Stockage image et relevé

```http
POST /api/readings/upload
Authorization: Bearer {token}
Content-Type: multipart/form-data

file: [image.jpg]
deviceCode: "550e8400-e29b-41d4-a716-446655440000"
commentaire: "Capture automatique ESP32-CAM"
```

**Réponse :**
```json
{
  "id": 124,
  "valeur": 1530.0,
  "dateTime": "2026-04-13T10:30:00Z",
  "source": "ESP32_CAM",
  "statut": "EN_TRAITEMENT",
  "commentaire": "Capture automatique ESP32-CAM",
  "imageUrl": "/uploads/images/IMG_20260413_103000.jpg",
  "compteur": {
    "id": 1,
    "reference": "COMP-001"
  }
}
```

**Statuts possibles :**
- `EN_TRAITEMENT` : Image en cours de traitement OCR
- `VALIDE` : Valeur extraite et validée
- `ERREUR` : Échec extraction ou validation

### **Relevé Capteur (Étape 6c)**
**Objectif** : Enregistrer une lecture provenant d'un capteur IoT
**Quand l'utiliser** : Pour les relevés automatiques via capteurs
**Prérequis** : Token JWT valide, compteur configuré en mode SENSOR

```http
POST /api/readings/sensor
Authorization: Bearer {token}
Content-Type: application/json

{
  "compteurId": 1,
  "valeur": 1532.0,
  "deviceCode": "550e8400-e29b-41d4-a716-446655440000",
  "commentaire": "Lecture capteur IoT"
}
```

**Important** : Le deviceCode permet de lier le relevé au module physique

### **Historique Relevés (Étape 7)**
**Objectif** : Récupérer l'historique paginé des relevés d'un compteur
**Quand l'utiliser** : Pour afficher les graphiques et tableaux historiques
**Prérequis** : Token JWT valide, ID compteur valide

**Paramètres optionnels :**
- `startDate` : Date début (format YYYY-MM-DD)
- `endDate` : Date fin (format YYYY-MM-DD)
- `page` : Page (défaut 0)
- `size` : Taille page (défaut 20, max 100)

```http
GET /api/readings/historique/{compteurId}?startDate=2026-04-01&endDate=2026-04-30&page=0&size=20
Authorization: Bearer {token}
```

**Réponse :**
```json
{
  "content": [
    {
      "id": 124,
      "valeur": 1530.0,
      "dateTime": "2026-04-13T10:30:00Z",
      "source": "ESP32_CAM",
      "statut": "VALIDE",
      "consommationCalculee": 30.0
    },
    {
      "id": 123,
      "valeur": 1525.5,
      "dateTime": "2026-04-12T10:30:00Z",
      "source": "MANUEL",
      "statut": "VALIDE",
      "consommationCalculee": 25.5
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 45,
  "totalPages": 3
}
```

**Important** : Les relevés sont triés par date décroissante (plus récent d'abord)

### **Dernier Relevé (Étape 7.1)**
**Objectif** : Obtenir le relevé le plus récent d'un compteur
**Quand l'utiliser** : Pour afficher la valeur actuelle sur le dashboard
**Prérequis** : Token JWT valide, ID compteur valide

```http
GET /api/readings/latest/{compteurId}
Authorization: Bearer {token}
```

**Réponse :**
```json
{
  "id": 124,
  "valeur": 1530.0,
  "dateTime": "2026-04-13T10:30:00Z",
  "source": "ESP32_CAM",
  "statut": "VALIDE",
  "consommationCalculee": 30.0,
  "imageUrl": "/uploads/images/IMG_20260413_103000.jpg"
}
```

**Important** : Retourne null si aucun relevé n'existe pour ce compteur

---

## 📈 **5. Statistiques**

### **Consommation (Étape 8)**
**Objectif** : Calculer la consommation sur une période donnée
**Quand l'utiliser** : Pour afficher les graphiques de consommation
**Prérequis** : Token JWT valide, ID compteur valide

**Paramètres :**
- `startDate` : Date début (format YYYY-MM-DD)
- `endDate` : Date fin (format YYYY-MM-DD)

```http
GET /api/compteurs/{id}/consommation?startDate=2026-04-01&endDate=2026-04-30
Authorization: Bearer {token}
```

**Réponse :**
```json
{
  "consommationTotale": 1250.5,
  "consommationJournaliere": 41.68,
  "nombreJours": 30,
  "periode": {
    "debut": "2026-04-01",
    "fin": "2026-04-30"
  }
}
```

**Important** : Calculée uniquement pour les compteurs CLASSIQUE

### **Statistiques Complètes (Étape 8.1)**
**Objectif** : Obtenir des statistiques détaillées sur différentes périodes
**Quand l'utiliser** : Pour le dashboard avec indicateurs clés
**Prérequis** : Token JWT valide, ID compteur valide

**Périodes disponibles :**
- `jour` : Statistiques du jour
- `semaine` : Statistiques de la semaine
- `mois` : Statistiques du mois
- `annee` : Statistiques de l'année

```http
GET /api/compteurs/{id}/statistiques?periode=mois
Authorization: Bearer {token}
```

**Réponse :**
```json
{
  "consommationJour": 41.68,
  "consommationSemaine": 291.76,
  "consommationMois": 1250.5,
  "consommationMoyenneJour": 41.68,
  "periode": "mois"
}
```

**Important** : Les valeurs sont calculées automatiquement selon la période demandée

---

## 🔍 **Codes d'Erreur**

### **Format Standard**
```json
{
  "timestamp": "2026-04-13T10:30:00Z",
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "La valeur du compteur ne peut pas être inférieure à la précédente",
  "path": "/api/readings/manual"
}
```

### **Codes Principaux**
- **400** : Requête invalide (validation échouée)
- **401** : Non authentifié
- **403** : Non autorisé
- **404** : Ressource non trouvée
- **409** : Conflit (doublon, règle métier)
- **500** : Erreur serveur interne

---

## 🔄 **Workflow Complet d'Utilisation**

### **Scénario 1 : Installation Compteur Manuel**
1. **Login** → `POST /api/auth/login`
2. **Création Compteur** → `POST /api/compteurs`
3. **Configuration Mode Lecture** → `POST /api/compteurs/{id}/mode-lecture` (MANUAL)
4. **Saisie Relevés** → `POST /api/readings/manual`
5. **Consultation Historique** → `GET /api/readings/historique/{id}`

### **Scénario 2 : Installation ESP32-CAM**
1. **Login** → `POST /api/auth/login`
2. **Création Compteur** → `POST /api/compteurs`
3. **Configuration Mode Lecture** → `POST /api/compteurs/{id}/mode-lecture` (ESP32_CAM)
4. **Scan QR Code** → `POST /api/devices/scan`
5. **Association Module** → `POST /api/devices/{deviceCode}/associate`
6. **Configuration Wi-Fi** → Via AP local (firmware)
7. **Vérification Statut** → `GET /api/devices/{deviceCode}/status`
8. **Relevés Automatiques** → `POST /api/readings/upload` (firmware)

### **Scénario 3 : Monitoring Dashboard**
1. **Login** → `POST /api/auth/login`
2. **Liste Compteurs** → `GET /api/compteurs`
3. **Derniers Relevés** → `GET /api/readings/latest/{id}` pour chaque compteur
4. **Statistiques** → `GET /api/compteurs/{id}/statistiques`
5. **Modules Status** → `GET /api/devices/my`

---

## 🔄 **Flux Complet Onboarding ESP32-CAM**

### **Étape 1 : Scan QR Code**
```javascript
// Frontend
const scanQRCode = async (qrCode) => {
  try {
    const response = await fetch('/api/devices/scan', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        qrCode: qrCode,
        userId: currentUser.id
      })
    });
    
    const device = await response.json();
    console.log('Module scanné:', device.deviceCode);
    
    // Rediriger vers la page d'association
    window.location.href = `/devices/${device.deviceCode}/associate`;
  } catch (error) {
    console.error('Erreur scan QR:', error);
  }
};
```

### **Étape 2 : Association Compteur**
```javascript
const associateDevice = async (deviceCode, compteurId) => {
  try {
    const response = await fetch(`/api/devices/${deviceCode}/associate`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        compteurId: compteurId,
        captureInterval: 3600
      })
    });
    
    const result = await response.json();
    console.log('Module associé:', result.message);
    
    // Afficher les instructions de configuration Wi-Fi
    showWiFiInstructions(deviceCode);
  } catch (error) {
    console.error('Erreur association:', error);
  }
};
```

### **Étape 3 : Configuration Wi-Fi (Côté ESP32)**
1. **Module crée un AP** : `MeterEye-Setup`
2. **User se connecte** au Wi-Fi
3. **Page web locale** : `http://192.168.4.1`
4. **Configuration** SSID + mot de passe
5. **Redémarrage automatique**

### **Étape 4 : Vérification Statut**
```javascript
const checkDeviceStatus = async (deviceCode) => {
  try {
    const response = await fetch(`/api/devices/${deviceCode}/status`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    const device = await response.json();
    
    if (device.statut === 'ACTIF') {
      console.log('Module prêt pour les relevés');
      enableCaptureButton();
    } else {
      console.log('Module en attente:', device.statut);
      // Afficher le statut actuel
      updateDeviceStatus(device);
    }
  } catch (error) {
    console.error('Erreur vérification statut:', error);
  }
};
```

---

## 🛡️ **Règles Métier Important**

### **Validation Relevés**
- **Compteur non configuré** → Rejet automatique
- **Source incompatible** → Erreur 409
- **Valeur CLASSIQUE** → Doit être ≥ précédente
- **Module ESP32-CAM** → Doit être ACTIF et configuré

### **Sécurité**
- **DeviceCode unique** comme identifiant principal
- **Association QR code** obligatoire avant utilisation
- **Autorisation utilisateur** vérifiée à chaque requête
- **HTTPS requis** en production

---

## 📱 **Exemples Frontend**

### **React Hook pour Gestion Module**
```javascript
import { useState, useEffect } from 'react';
import api from '../services/api';

export const useDevice = (deviceCode) => {
  const [device, setDevice] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDevice = async () => {
      try {
        const response = await api.get(`/devices/${deviceCode}/status`);
        setDevice(response.data);
      } catch (error) {
        console.error('Erreur chargement device:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchDevice();
    
    // Polling toutes les 30 secondes
    const interval = setInterval(fetchDevice, 30000);
    return () => clearInterval(interval);
  }, [deviceCode]);

  const updateCaptureInterval = async (interval) => {
    try {
      await api.put(`/devices/${deviceCode}/capture-interval`, null, {
        params: { interval }
      });
      setDevice(prev => ({ ...prev, captureInterval: interval }));
    } catch (error) {
      console.error('Erreur mise à jour intervalle:', error);
    }
  };

  return { device, loading, updateCaptureInterval };
};
```

### **Composant Statut Module**
```jsx
import React from 'react';
import { useDevice } from '../hooks/useDevice';

const DeviceStatus = ({ deviceCode }) => {
  const { device, loading } = useDevice(deviceCode);

  if (loading) return <div>Chargement...</div>;

  const getStatusColor = (status) => {
    switch (status) {
      case 'ACTIF': return 'green';
      case 'HORS_LIGNE': return 'red';
      case 'EN_CONFIGURATION': return 'orange';
      default: return 'gray';
    }
  };

  return (
    <div className="device-status">
      <h3>Module: {device.qrCodeValue}</h3>
      <div className={`status-badge ${getStatusColor(device.statut)}`}>
        {device.statut}
      </div>
      
      {device.statut === 'ACTIF' && (
        <div className="device-details">
          <p>Dernière vue: {new Date(device.lastSeenAt).toLocaleString()}</p>
          <p>Version: {device.firmwareVersion}</p>
          <p>Intervalle: {device.captureInterval}s</p>
        </div>
      )}
      
      {device.statut === 'NON_CONFIGURE' && (
        <div className="setup-instructions">
          <p>1. Connectez-vous au Wi-Fi "MeterEye-Setup"</p>
          <p>2. Ouvrez http://192.168.4.1</p>
          <p>3. Configurez votre Wi-Fi domestique</p>
        </div>
      )}
    </div>
  );
};
```

---

## 🚀 **Déploiement**

### **Variables d'Environnement**
```bash
# Production
SPRING_PROFILES_ACTIVE=prod
BACKEND_URL=https://api.metereye.ai

# Développement
SPRING_PROFILES_ACTIVE=dev
BACKEND_URL=http://localhost:8080
```

### **Base de Données**
```sql
-- Tables principales créées automatiquement par Hibernate
-- Compteurs, ModulesESP32, Releves, Users, Roles, Images
```

---

## 📞 **Support**

### **URL Base**
- **Développement** : `http://localhost:8080/api`
- **Production** : `https://api.metereye.ai/api`

### **Contact Support**
- **Email** : support@metereye.ai
- **Documentation** : https://docs.metereye.ai

---

## 📋 **Checklist Intégration Frontend**

- [ ] Authentification JWT
- [ ] Gestion compteurs CRUD
- [ ] Scan QR code modules
- [ ] Association module-compteur
- [ ] Configuration mode lecture
- [ ] Saisie relevés manuels
- [ ] Upload images ESP32-CAM
- [ ] Affichage historique
- [ ] Statistiques et graphiques
- [ ] Monitoring temps réel modules
- [ ] Gestion erreurs et alerts

---

**🎯 Cette documentation couvre 100% des besoins pour intégrer le frontend MeterEye AI**
