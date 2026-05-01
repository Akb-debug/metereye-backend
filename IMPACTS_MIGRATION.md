# 📋 **IMPACTS SUR L'EXISTANT**

## 8. **Analyse des Impacts**

### **8.1 Modifications Nécessaires**

#### **Entités Impactées**
- **Compteur.java** : Ajouter relation avec `ModuleDevice` générique
- **Releve.java** : Adapter validation pour `deviceCode` au lieu de `sensorId`
- **ModuleESP32.java** : Conserver pour compatibilité mais déprécié

#### **Controllers Impactés**
- **ReadingController** : Modifier `createSensorReading()` pour utiliser `deviceCode`
- **ConfigurationCompteurController** : Ajouter logique de changement de mode
- **DeviceController** : Conserver pour compatibilité existante

#### **Services Impactés**
- **ReadingService** : Adapter validation des relevés capteurs
- **CompteurService** : Ajouter gestion changement de mode

### **8.2 Scripts de Migration SQL**

```sql
-- 1. Création nouvelles tables
CREATE TABLE module_devices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_code VARCHAR(255) UNIQUE NOT NULL,
    qr_code_value VARCHAR(255) UNIQUE NOT NULL,
    serial_number VARCHAR(255),
    type_module VARCHAR(50) NOT NULL,
    statut VARCHAR(50) DEFAULT 'NON_CONFIGURE',
    configured BOOLEAN DEFAULT FALSE,
    last_seen_at TIMESTAMP,
    firmware_version VARCHAR(50),
    capture_interval INT DEFAULT 3600,
    wifi_ssid VARCHAR(100),
    ip_address VARCHAR(50),
    compteur_id BIGINT,
    proprietaire_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    dtype VARCHAR(50) -- Pour héritage JPA
);

CREATE TABLE esp32_cam_devices (
    device_code BIGINT PRIMARY KEY,
    resolution_camera VARCHAR(20) DEFAULT '2MP',
    flash_active BOOLEAN DEFAULT TRUE,
    qualite_image INT DEFAULT 80,
    angle_capture INT DEFAULT 90,
    format_image VARCHAR(10) DEFAULT 'JPEG',
    FOREIGN KEY (device_code) REFERENCES module_devices(id)
);

CREATE TABLE esp32_pzem_devices (
    device_code BIGINT PRIMARY KEY,
    tension_max DOUBLE DEFAULT 500.0,
    courant_max DOUBLE DEFAULT 100.0,
    puissance_max DOUBLE DEFAULT 22000.0,
    precision DOUBLE DEFAULT 1.0,
    frequence_echantillonnage INT DEFAULT 1000,
    mode_calibrage VARCHAR(20) DEFAULT 'AUTO',
    facteur_correction DOUBLE DEFAULT 1.0,
    seuil_alerte DOUBLE DEFAULT 0.1,
    FOREIGN KEY (device_code) REFERENCES module_devices(id)
);

CREATE TABLE historique_configuration_mode (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    compteur_id BIGINT NOT NULL,
    ancien_mode VARCHAR(50),
    nouveau_mode VARCHAR(50) NOT NULL,
    date_changement TIMESTAMP NOT NULL,
    change_par_user_id BIGINT,
    motif_changement VARCHAR(500),
    ancien_device_code VARCHAR(255),
    nouveau_device_code VARCHAR(255),
    configuration_desactivee BOOLEAN DEFAULT FALSE,
    donnees_migrees BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (compteur_id) REFERENCES compteurs(id),
    FOREIGN KEY (change_par_user_id) REFERENCES users(id)
);

-- 2. Migration des données existantes
INSERT INTO module_devices (
    device_code, qr_code_value, serial_number, type_module, statut, 
    configured, last_seen_at, firmware_version, capture_interval, 
    wifi_ssid, ip_address, compteur_id, proprietaire_id, dtype
)
SELECT 
    device_code, qr_code_value, serial_number, 'ESP32_CAM', statut,
    configured, last_seen_at, firmware_version, capture_interval,
    wifi_ssid, ip_address, compteur_id, proprietaire_id, 'ESP32CamDevice'
FROM modules_esp32;

INSERT INTO esp32_cam_devices (device_code)
SELECT id FROM module_devices WHERE dtype = 'ESP32CamDevice';

-- 3. Mise à jour des clés étrangères
UPDATE module_devices md 
SET compteur_id = (SELECT compteur_id FROM modules_esp32 me WHERE me.device_code = md.device_code)
WHERE dtype = 'ESP32CamDevice';

UPDATE module_devices md 
SET proprietaire_id = (SELECT proprietaire_id FROM modules_esp32 me WHERE me.device_code = md.device_code)
WHERE dtype = 'ESP32CamDevice';
```

### **8.3 Compatibilité Ascendante**

#### **Endpoints Conservés**
- `/api/devices/scan` : Compatible avec payload existant
- `/api/devices/{deviceCode}/associate` : Compatible
- `/api/devices/{deviceCode}/handshake` : Compatible
- `/api/devices/{deviceCode}/heartbeat` : Compatible

#### **Endpoints Modifiés**
- `/api/readings/sensor` : `sensorId` → `deviceCode`
- `/api/compteurs/{id}/mode-lecture` : Ajout gestion historique

#### **Endpoints Nouveaux**
- `/api/devices/compteurs/{compteurId}/changer-mode`
- `/api/devices/{deviceCode}/configurer-pzem`

### **8.4 Impact sur le Frontend**

#### **Changements Mineurs**
- Utiliser `deviceCode` au lieu de `sensorId` pour les relevés capteurs
- Gérer les nouveaux types de modules dans l'interface
- Ajouter interface de configuration PZEM004T

#### **Compatibilité Maintenue**
- Flux ESP32-CAM inchangé
- Authentification inchangée
- Gestion compteurs inchangée

### **8.5 Risques et Mitigations**

#### **Risque 1: Perte de données pendant migration**
- **Mitigation** : Scripts de migration testés, backup préalable
- **Validation** : Tests sur environnement de staging

#### **Risque 2: Incompatibilité frontend**
- **Mitigation** : Maintien compatibilité endpoints existants
- **Validation** : Tests intégrés frontend-backend

#### **Risque 3: Performance impact**
- **Mitigation** : Indexation appropriée, requêtes optimisées
- **Validation** : Tests de charge

### **8.6 Plan de Déploiement**

#### **Phase 1: Préparation**
1. Backup base de données
2. Déployer nouveau code en mode shadow
3. Tests sur environnement de staging

#### **Phase 2: Migration**
1. Exécuter scripts SQL pendant maintenance
2. Valider migration des données
3. Déployer nouvelle version

#### **Phase 3: Validation**
1. Tests end-to-end complets
2. Monitoring performance
3. Validation frontend

#### **Phase 4: Nettoyage**
1. Suppression anciennes tables (après validation)
2. Documentation mise à jour
3. Formation équipe

### **8.7 Tests de Validation**

#### **Tests Unitaires**
- Création modules ESP32-CAM et ESP32-PZEM004T
- Changement de mode compteur
- Validation relevés capteurs

#### **Tests d'Intégration**
- Flux complet onboarding ESP32-CAM
- Flux complet configuration ESP32-PZEM004T
- Migration mode MANUAL → SENSOR

#### **Tests End-to-End**
- Scénarios utilisateur complets
- Tests charge et performance
- Validation compatibilité frontend

---

## 🎯 **RÉSUMÉ DE L'ARCHITECTURE FINALE**

### **Avantages**
- ✅ **Propre** : Séparation claire entre modes métier et solutions techniques
- ✅ **Extensible** : Facile d'ajouter de nouveaux types de modules
- ✅ **Compatible** : Maintient compatibilité avec l'existant
- ✅ **Traçable** : Historique complet des changements de mode
- ✅ **Robuste** : Gestion propre des migrations et désactivations

### **Mode d'Emploi**
1. **ESP32-CAM** : Mode métier `ESP32_CAM` → Solution technique `ESP32_CAM`
2. **ESP32-PZEM004T** : Mode métier `SENSOR` → Solution technique `ESP32_PZEM004T`
3. **Changement de mode** : Géré automatiquement avec historique
4. **Relevés** : Validés selon mode configuré

Cette architecture permet une évolution propre tout en préservant l'investissement existant.**
