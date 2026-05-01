# Documentation Complète du Système d'Alertes - MeterEye AI Backend

## Vue d'ensemble

Le système d'alertes du projet MeterEye AI Backend est conçu pour notifier les utilisateurs concernant divers événements liés à leurs compteurs électriques. Ce document couvre tous les aspects du système : endpoints, services, entités, et flux de données.

---

## 🏗️ Architecture du Système

### Composants Principaux
1. **AlerteController** - API REST (non implémenté)
2. **AlerteService** - Logique métier
3. **AlerteRepository** - Accès aux données
4. **Alerte** - Entité principale
5. **AlerteResponseDTO** - Objet de transfert
6. **AlerteMapper** - Mapping entité/DTO

---

## 📡 Endpoints API

### État Actuel
⚠️ **Le controller est actuellement vide et marqué comme "à implémenter dans un prochain sprint"**

```java
@RestController
@RequestMapping("/api/alertes")
public class AlerteController extends BaseController {
    // Controller vide - endpoints à implémenter
}
```

### Endpoints Prévus (basés sur le service)
- `GET /api/alertes` - Récupérer toutes les alertes de l'utilisateur
- `GET /api/alertes/non-lues` - Récupérer les alertes non lues
- `PUT /api/alertes/{id}/lire` - Marquer une alerte comme lue
- `POST /api/alertes/envoyer` - Déclencher l'envoi des alertes

---

## 🗃️ Modèle de Données

### Entité Alerte

```java
@Entity
@Table(name = "alertes")
public class Alerte extends BaseEntity {
    private User destination;           // Destinataire de l'alerte
    private Compteur compteur;          // Compteur concerné
    private TypeAlerte typeAlerte;      // Type d'alerte
    private String message;             // Message de l'alerte
    private CanalEnvoi canal;           // Canal d'envoi
    private Boolean lue = false;        // Statut de lecture
    private Boolean envoyee = false;    // Statut d'envoi
    private LocalDateTime dateEnvoi;    // Date d'envoi
}
```

### Types d'Alertes

```java
public enum TypeAlerte {
    CREDIT_FAIBLE,      // Crédit faible sur le compteur
    COUPURE_IMMINENTE,  // Coupure imminente
    ANOMALIE,          // Anomalie détectée
    RAPPORT_DISPONIBLE // Nouveau rapport disponible
}
```

### Canaux d'Envoi

```java
public enum CanalEnvoi {
    PUSH_MOBILE,  // Notifications push mobile
    SMS,          // Messages SMS
    EMAIL         // Emails
}
```

---

## 🔧 Services Métier

### AlerteService Interface

```java
public interface AlerteService {
    List<AlerteResponseDTO> getAlertesByUser(User user);
    List<AlerteResponseDTO> getAlertesNonLues(User user);
    void marquerCommeLue(Long alerteId);
    void envoyerAlertes();
}
```

### Fonctionnalités Implémentées

#### 1. Récupération des Alertes
- **getAlertesByUser()** : Retourne toutes les alertes d'un utilisateur triées par date
- **getAlertesNonLues()** : Filtre uniquement les alertes non lues

#### 2. Gestion du Statut
- **marquerCommeLue()** : Marque une alerte comme lue

#### 3. Envoi d'Alertes
- **envoyerAlertes()** : Traite toutes les alertes non envoyées
- Support multi-canaux (Push, SMS, Email)

---

## 🗄️ Accès aux Données

### AlerteRepository

```java
public interface AlerteRepository extends JpaRepository<Alerte, Long> {
    List<Alerte> findByDestinationAndLueFalse(User destination);
    List<Alerte> findByDestinationOrderByDateCreationDesc(User destination);
    List<Alerte> findByEnvoyeeFalse();
}
```

### Requetes Disponibles
- **findByDestinationAndLueFalse()** : Alertes non lues par utilisateur
- **findByDestinationOrderByDateCreationDesc()** : Toutes les alertes par utilisateur (triées)
- **findByEnvoyeeFalse()** : Alertes en attente d'envoi

---

## 🔄 Flux de Données

### 1. Création d'une Alerte
```
Trigger (crédit faible, anomalie, etc.) 
→ Création entité Alerte 
→ Sauvegarde en base 
→ Statut: non lue, non envoyée
```

### 2. Envoi d'Alertes
```
envoyerAlertes() 
→ Recherche alertes non envoyées 
→ Traitement par canal (SMS/Email/Push) 
→ Mise à jour statut envoyée + date envoi
```

### 3. Lecture par Utilisateur
```
Requête API 
→ getAlertesByUser() ou getAlertesNonLues() 
→ Mapping vers AlerteResponseDTO 
→ Retour au client
```

---

## 📤 DTO et Mapping

### AlerteResponseDTO

```java
public class AlerteResponseDTO {
    private Long id;
    private String typeAlerte;           // Nom du type d'alerte
    private String message;
    private String canal;               // Nom du canal
    private Boolean lue;
    private Boolean envoyee;
    private LocalDateTime dateCreation;
    private LocalDateTime dateEnvoi;
    private Long compteurId;             // ID du compteur
    private String compteurReference;    // Référence du compteur
}
```

### Mapping avec MapStruct
- Conversion automatique entité → DTO
- Mapping des enums vers strings
- Inclusion des informations du compteur

---

## 🔔 Implémentation des Canaux

### 1. Push Mobile (Firebase)
```java
private void envoyerPushNotification(Alerte alerte) {
    // TODO: Implémentation avec Firebase Cloud Messaging
    log.info("Envoi push notification à {}: {}", 
        alerte.getDestination().getEmail(), alerte.getMessage());
}
```

### 2. SMS
```java
private void envoyerSMS(Alerte alerte) {
    // TODO: Implémentation avec API SMS
    log.info("Envoi SMS à {}: {}", 
        alerte.getDestination().getTelephone(), alerte.getMessage());
}
```

### 3. Email
```java
private void envoyerEmail(Alerte alerte) {
    // TODO: Implémentation avec JavaMailSender
    log.info("Envoi email à {}: {}", 
        alerte.getDestination().getEmail(), alerte.getMessage());
}
```

---

## 📊 Schéma de la Base de Données

### Table `alertes`

| Colonne | Type | Description |
|---------|------|-------------|
| id | BIGINT | Clé primaire |
| destination_id | BIGINT | FK vers users |
| compteur_id | BIGINT | FK vers compteurs |
| type_alerte | VARCHAR | Type d'alerte |
| message | VARCHAR(500) | Message |
| canal | VARCHAR | Canal d'envoi |
| lue | BOOLEAN | Statut lecture |
| envoyee | BOOLEAN | Statut envoi |
| date_envoi | TIMESTAMP | Date d'envoi |
| date_creation | TIMESTAMP | Date création |
| date_modification | TIMESTAMP | Date modification |

---

## 🚀 Étapes Suivantes

### 1. Implémentation du Controller
```java
@GetMapping
public ResponseEntity<List<AlerteResponseDTO>> getMesAlertes() {
    // Implémentation requise
}

@GetMapping("/non-lues")
public ResponseEntity<List<AlerteResponseDTO>> getAlertesNonLues() {
    // Implémentation requise
}

@PutMapping("/{id}/lire")
public ResponseEntity<Void> marquerCommeLue(@PathVariable Long id) {
    // Implémentation requise
}
```

### 2. Configuration des Services Externes
- Firebase Cloud Messaging pour les push
- API SMS (Twilio, Orange, etc.)
- Serveur SMTP pour les emails

### 3. Planification des Envois
- Configuration d'un job schedulé pour `envoyerAlertes()`
- Gestion des retry en cas d'échec

---

## 🔧 Dépendances Techniques

### Frameworks et Bibliothèques
- **Spring Boot** - Framework principal
- **Spring Data JPA** - Accès aux données
- **MapStruct** - Mapping entité/DTO
- **Lombok** - Réduction de code boilerplate
- **Hibernate** - ORM

### Base de Données
- PostgreSQL/MySQL - Persistance des alertes

---

## 📝 Résumé des Fonctionnalités

| Fonctionnalité | Statut | Description |
|----------------|--------|-------------|
| Création d'alertes | ✅ Implémenté | Entité et repository prêts |
| Récupération par utilisateur | ✅ Implémenté | Service et mapper opérationnels |
| Gestion du statut lu/non lu | ✅ Implémenté | Logique complète |
| Envoi multi-canaux | 🔄 Partiel | Structure prête, APIs externes à configurer |
| API REST | ❌ Non implémenté | Controller vide |
| Planification automatique | ❌ Non implémenté | Job scheduler à configurer |

---

## 📞 Points de Contact

Pour toute question ou modification du système d'alertes :
- **Repository** : `AlerteRepository.java`
- **Service** : `AlerteService.java` / `AlerteServiceImpl.java`
- **Controller** : `AlerteController.java` (à compléter)
- **Entité** : `Alerte.java`
- **DTO** : `AlerteResponseDTO.java`

---

*Document généré le 1 mai 2026 - MeterEye AI Backend*
