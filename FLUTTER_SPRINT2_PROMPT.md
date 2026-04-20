# PROMPT FLUTTER SPRINT 2 - MeterEye AI

## CONTEXTE

Tu es un expert Flutter/Dart. J'ai déjà implémenté le Sprint 1 de l'application MeterEye AI qui inclut :
- Authentification (login/register)
- Gestion profil utilisateur
- Navigation et structure de base
- Services API avec JWT
- Architecture clean (Repository, Services, Models)

## OBJECTIF SPRINT 2

Implémenter la gestion complète des compteurs électriques avec support pour :
- Compteurs CLASSIQUE (index cumulatif)
- Compteurs CASH_POWER (crédit restant)
- Modes de lecture : Manuel, ESP32-CAM, Capteur
- Onboarding ESP32-CAM avec QR code
- Saisie de relevés (manuel, image, capteur)
- Statistiques et monitoring

## API BACKEND DISPONIBLE

### Base URL: http://localhost:8080/api
### Authentification: Bearer JWT token

### 1. GESTION COMPTEURS

#### Créer Compteur
```http
POST /compteurs
Authorization: Bearer {token}
Content-Type: application/json

{
  "reference": "COMP-001",
  "adresse": "123 Rue de la République, Dakar",
  "typeCompteur": "CLASSIQUE", // ou "CASH_POWER"
  "valeurInitiale": 1500.0
}

Réponse:
{
  "id": 1,
  "reference": "COMP-001",
  "adresse": "123 Rue de la République, Dakar",
  "typeCompteur": "CLASSIQUE",
  "valeurActuelle": 1500.0,
  "proprietaireNom": "Jean Dupont",
  "proprietaireId": 1,
  "dateInitialisation": "2026-04-13",
  "actif": true,
  "dateCreation": "2026-04-13T10:30:00Z"
}
```

#### Configurer Mode Lecture
```http
POST /compteurs/{id}/mode-lecture
Authorization: Bearer {token}
Content-Type: application/json

{
  "modeLecture": "ESP32_CAM", // ou "MANUAL", "SENSOR"
  "commentaire": "Configuration pour lecture automatique"
}
```

#### Réinitialiser Compteur (CLASSIQUE)
```http
POST /compteurs/{id}/reinitialiser
Authorization: Bearer {token}
Content-Type: application/json

{
  "motif": "Remplacement compteur défectueux",
  "commentaire": "Ancien compteur changé le 13/04/2026"
}
```

#### Recharger Compteur (CASH_POWER)
```http
POST /compteurs/recharger
Authorization: Bearer {token}
Content-Type: application/json

{
  "compteurId": 1,
  "montant": 5000.0,
  "codeRecharge": "123456789"
}
```

#### Lister Compteurs
```http
GET /compteurs
Authorization: Bearer {token}

Réponse:
[
  {
    "id": 1,
    "reference": "COMP-001",
    "adresse": "123 Rue de la République, Dakar",
    "typeCompteur": "CLASSIQUE",
    "valeurActuelle": 1500.0,
    "proprietaireNom": "Jean Dupont",
    "proprietaireId": 1,
    "dateInitialisation": "2026-04-13",
    "actif": true,
    "dateCreation": "2026-04-13T10:30:00Z"
  }
]
```

### 2. GESTION MODULES ESP32-CAM

#### Scanner QR Code
```http
POST /devices/scan
Authorization: Bearer {token}
Content-Type: application/json

{
  "qrCode": "MET-ESP32-ABC123XYZ",
  "userId": 1
}

Réponse:
{
  "deviceCode": "550e8400-e29b-41d4-a716-446655440000",
  "qrCodeValue": "MET-ESP32-ABC123XYZ",
  "statut": "NON_CONFIGURE",
  "configured": false,
  "proprietaireId": 1,
  "message": "Module enregistré, attente configuration Wi-Fi"
}
```

#### Associer Module-Compteur
```http
POST /devices/{deviceCode}/associate
Authorization: Bearer {token}
Content-Type: application/json

{
  "compteurId": 1,
  "captureInterval": 3600
}

Réponse:
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

#### Statut Module
```http
GET /devices/{deviceCode}/status
Authorization: Bearer {token}

Réponse:
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

#### Lister Modules
```http
GET /devices/my
Authorization: Bearer {token}
```

#### Configurer Intervalle Capture
```http
PUT /api/devices/{deviceCode}/capture-interval?interval=1800
Authorization: Bearer {token}
```

### 3. GESTION RELEVÉS

#### Relevé Manuel
```http
POST /readings/manual
Authorization: Bearer {token}
Content-Type: application/json

{
  "meterId": 1,
  "value": 1525.5,
  "comment": "Lecture manuelle du 13/04/2026"
}

Réponse:
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

#### Upload Image ESP32-CAM
```http
POST /readings/upload
Authorization: Bearer {token}
Content-Type: multipart/form-data

file: [image.jpg]
deviceCode: "550e8400-e29b-41d4-a716-446655440000"
commentaire: "Capture automatique ESP32-CAM"
```

#### Relevé Capteur
```http
POST /readings/sensor
Authorization: Bearer {token}
Content-Type: application/json

{
  "compteurId": 1,
  "valeur": 1532.0,
  "deviceCode": "550e8400-e29b-41d4-a716-446655440000",
  "commentaire": "Lecture capteur IoT"
}
```

#### Historique Relevés
```http
GET /readings/historique/{compteurId}?startDate=2026-04-01&endDate=2026-04-30&page=0&size=20
Authorization: Bearer {token}

Réponse:
{
  "content": [
    {
      "id": 124,
      "valeur": 1530.0,
      "dateTime": "2026-04-13T10:30:00Z",
      "source": "ESP32_CAM",
      "statut": "VALIDE",
      "consommationCalculee": 30.0
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 45,
  "totalPages": 3
}
```

#### Dernier Relevé
```http
GET /readings/latest/{compteurId}
Authorization: Bearer {token}
```

### 4. STATISTIQUES

#### Consommation
```http
GET /compteurs/{id}/consommation?startDate=2026-04-01&endDate=2026-04-30
Authorization: Bearer {token}

Réponse:
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

#### Statistiques Complètes
```http
GET /compteurs/{id}/statistiques?periode=mois
Authorization: Bearer {token}

Réponse:
{
  "consommationJour": 41.68,
  "consommationSemaine": 291.76,
  "consommationMois": 1250.5,
  "consommationMoyenneJour": 41.68,
  "periode": "mois"
}
```

## SPÉCIFICATIONS FLUTTER À IMPLÉMENTER

### 1. MODELS (lib/models/)

```dart
// models/compteur.dart
class Compteur {
  final int id;
  final String reference;
  final String adresse;
  final String typeCompteur; // CLASSIQUE, CASH_POWER
  final double valeurActuelle;
  final String proprietaireNom;
  final int proprietaireId;
  final DateTime dateInitialisation;
  final bool actif;
  final DateTime dateCreation;

  Compteur({required this.id, required this.reference, /* ... */});

  factory Compteur.fromJson(Map<String, dynamic> json) { /* ... */ }
  Map<String, dynamic> toJson() { /* ... */ }
}

// models/device_esp32.dart
class DeviceEsp32 {
  final String deviceCode;
  final String qrCodeValue;
  final String statut; // NON_CONFIGURE, EN_CONFIGURATION, ACTIF, HORS_LIGNE
  final bool configured;
  final DateTime? lastSeenAt;
  final String? firmwareVersion;
  final int captureInterval;
  final String? ipAddress;
  final String? wifiSsid;
  final int proprietaireId;
  final int? compteurId;
  final String? compteurReference;

  DeviceEsp32({required this.deviceCode, /* ... */});

  factory DeviceEsp32.fromJson(Map<String, dynamic> json) { /* ... */ }
}

// models/releve.dart
class Releve {
  final int id;
  final double valeur;
  final DateTime dateTime;
  final String source; // MANUEL, ESP32_CAM, SENSOR
  final String statut;
  final double? consommationCalculee;
  final String? commentaire;
  final CompteurSimple compteur;

  Releve({required this.id, /* ... */});

  factory Releve.fromJson(Map<String, dynamic> json) { /* ... */ }
}

// models/statistiques.dart
class Statistiques {
  final double consommationJour;
  final double consommationSemaine;
  final double consommationMois;
  final double consommationMoyenneJour;
  final String periode;

  Statistiques({required this.consommationJour, /* ... */});

  factory Statistiques.fromJson(Map<String, dynamic> json) { /* ... */ }
}
```

### 2. SERVICES (lib/services/)

```dart
// services/compteur_service.dart
class CompteurService {
  final ApiService _apiService;

  CompteurService(this._apiService);

  Future<List<Compteur>> getCompteurs() async {
    final response = await _apiService.get('/compteurs');
    return (response as List).map((json) => Compteur.fromJson(json)).toList();
  }

  Future<Compteur> createCompteur({
    required String reference,
    required String adresse,
    required String typeCompteur,
    required double valeurInitiale,
  }) async {
    final response = await _apiService.post('/compteurs', {
      'reference': reference,
      'adresse': adresse,
      'typeCompteur': typeCompteur,
      'valeurInitiale': valeurInitiale,
    });
    return Compteur.fromJson(response);
  }

  Future<void> configureModeLecture({
    required int compteurId,
    required String modeLecture,
    String? commentaire,
  }) async {
    await _apiService.post('/compteurs/$compteurId/mode-lecture', {
      'modeLecture': modeLecture,
      'commentaire': commentaire,
    });
  }

  Future<void> reinitialiserCompteur({
    required int compteurId,
    required String motif,
    String? commentaire,
  }) async {
    await _apiService.post('/compteurs/$compteurId/reinitialiser', {
      'motif': motif,
      'commentaire': commentaire,
    });
  }

  Future<void> rechargerCompteur({
    required int compteurId,
    required double montant,
    required String codeRecharge,
  }) async {
    await _apiService.post('/compteurs/recharger', {
      'compteurId': compteurId,
      'montant': montant,
      'codeRecharge': codeRecharge,
    });
  }
}

// services/device_service.dart
class DeviceService {
  final ApiService _apiService;

  DeviceService(this._apiService);

  Future<DeviceEsp32> scanQRCode({
    required String qrCode,
    required int userId,
  }) async {
    final response = await _apiService.post('/devices/scan', {
      'qrCode': qrCode,
      'userId': userId,
    });
    return DeviceEsp32.fromJson(response);
  }

  Future<DeviceEsp32> associateDevice({
    required String deviceCode,
    required int compteurId,
    required int captureInterval,
  }) async {
    final response = await _apiService.post('/devices/$deviceCode/associate', {
      'compteurId': compteurId,
      'captureInterval': captureInterval,
    });
    return DeviceEsp32.fromJson(response);
  }

  Future<DeviceEsp32> getDeviceStatus(String deviceCode) async {
    final response = await _apiService.get('/devices/$deviceCode/status');
    return DeviceEsp32.fromJson(response);
  }

  Future<List<DeviceEsp32>> getUserDevices() async {
    final response = await _apiService.get('/devices/my');
    return (response as List).map((json) => DeviceEsp32.fromJson(json)).toList();
  }

  Future<void> updateCaptureInterval({
    required String deviceCode,
    required int interval,
  }) async {
    await _apiService.put('/devices/$deviceCode/capture-interval', null, 
        queryParameters: {'interval': interval.toString()});
  }
}

// services/releve_service.dart
class ReleveService {
  final ApiService _apiService;

  ReleveService(this._apiService);

  Future<Releve> createManualReading({
    required int meterId,
    required double value,
    String? comment,
  }) async {
    final response = await _apiService.post('/readings/manual', {
      'meterId': meterId,
      'value': value,
      'comment': comment,
    });
    return Releve.fromJson(response);
  }

  Future<Releve> uploadImageReading({
    required File image,
    required String deviceCode,
    String? commentaire,
  }) async {
    final request = http.MultipartRequest(
      'POST',
      Uri.parse('${_apiService.baseUrl}/readings/upload'),
    );
    
    request.headers.addAll({
      'Authorization': 'Bearer ${_apiService.token}',
    });
    
    request.files.add(await http.MultipartFile.fromPath('file', image.path));
    request.fields['deviceCode'] = deviceCode;
    if (commentaire != null) {
      request.fields['commentaire'] = commentaire;
    }

    final streamedResponse = await request.send();
    final response = await http.Response.fromStream(streamedResponse);
    
    return Releve.fromJson(json.decode(response.body));
  }

  Future<Releve> createSensorReading({
    required int compteurId,
    required double valeur,
    required String deviceCode,
    String? commentaire,
  }) async {
    final response = await _apiService.post('/readings/sensor', {
      'compteurId': compteurId,
      'valeur': valeur,
      'deviceCode': deviceCode,
      'commentaire': commentaire,
    });
    return Releve.fromJson(response);
  }

  Future<PaginatedResponse<Releve>> getHistoriqueReleves({
    required int compteurId,
    DateTime? startDate,
    DateTime? endDate,
    int page = 0,
    int size = 20,
  }) async {
    final queryParams = <String, String>{
      'page': page.toString(),
      'size': size.toString(),
    };
    
    if (startDate != null) {
      queryParams['startDate'] = DateFormat('yyyy-MM-dd').format(startDate);
    }
    if (endDate != null) {
      queryParams['endDate'] = DateFormat('yyyy-MM-dd').format(endDate);
    }

    final response = await _apiService.get(
      '/readings/historique/$compteurId',
      queryParameters: queryParams,
    );
    
    return PaginatedResponse<Releve>.fromJson(
      response,
      (json) => Releve.fromJson(json),
    );
  }

  Future<Releve?> getLatestReleve(int compteurId) async {
    try {
      final response = await _apiService.get('/readings/latest/$compteurId');
      return Releve.fromJson(response);
    } catch (e) {
      if (e is ApiException && e.statusCode == 404) {
        return null;
      }
      rethrow;
    }
  }
}

// services/statistique_service.dart
class StatistiqueService {
  final ApiService _apiService;

  StatistiqueService(this._apiService);

  Future<ConsommationStats> getConsommation({
    required int compteurId,
    required DateTime startDate,
    required DateTime endDate,
  }) async {
    final response = await _apiService.get(
      '/compteurs/$compteurId/consommation',
      queryParameters: {
        'startDate': DateFormat('yyyy-MM-dd').format(startDate),
        'endDate': DateFormat('yyyy-MM-dd').format(endDate),
      },
    );
    return ConsommationStats.fromJson(response);
  }

  Future<Statistiques> getStatistiques({
    required int compteurId,
    required String periode, // jour, semaine, mois, annee
  }) async {
    final response = await _apiService.get(
      '/compteurs/$compteurId/statistiques',
      queryParameters: {'periode': periode},
    );
    return Statistiques.fromJson(response);
  }
}
```

### 3. UI SCREENS (lib/screens/)

#### 3.1 Liste des Compteurs (screens/compteurs_list_screen.dart)
```dart
class CompteursListScreen extends StatefulWidget {
  @override
  _CompteursListScreenState createState() => _CompteursListScreenState();
}

class _CompteursListScreenState extends State<CompteursListScreen> {
  final CompteurService _compteurService = getIt<CompteurService>();
  List<Compteur> _compteurs = [];
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _loadCompteurs();
  }

  Future<void> _loadCompteurs() async {
    setState(() => _isLoading = true);
    try {
      final compteurs = await _compteurService.getCompteurs();
      setState(() => _compteurs = compteurs);
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Erreur: ${e.toString()}')),
      );
    } finally {
      setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Mes Compteurs'),
        actions: [
          IconButton(
            icon: Icon(Icons.add),
            onPressed: () => Navigator.push(
              context,
              MaterialPageRoute(builder: (_) => CreateCompteurScreen()),
            ).then((_) => _loadCompteurs()),
          ),
        ],
      ),
      body: _isLoading
          ? Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: _loadCompteurs,
              child: ListView.builder(
                itemCount: _compteurs.length,
                itemBuilder: (context, index) {
                  final compteur = _compteurs[index];
                  return CompteurCard(
                    compteur: compteur,
                    onTap: () => Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => CompteurDetailScreen(compteurId: compteur.id),
                      ),
                    ),
                  );
                },
              ),
            ),
    );
  }
}
```

#### 3.2 Création Compteur (screens/create_compteur_screen.dart)
```dart
class CreateCompteurScreen extends StatefulWidget {
  @override
  _CreateCompteurScreenState createState() => _CreateCompteurScreenState();
}

class _CreateCompteurScreenState extends State<CreateCompteurScreen> {
  final _formKey = GlobalKey<FormState>();
  final CompteurService _compteurService = getIt<CompteurService>();
  
  final _referenceController = TextEditingController();
  final _adresseController = TextEditingController();
  final _valeurInitialeController = TextEditingController();
  
  String _typeCompteur = 'CLASSIQUE';
  bool _isLoading = false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Nouveau Compteur')),
      body: Form(
        key: _formKey,
        child: Padding(
          padding: EdgeInsets.all(16.0),
          child: Column(
            children: [
              TextFormField(
                controller: _referenceController,
                decoration: InputDecoration(labelText: 'Référence'),
                validator: (value) => value?.isEmpty ?? true ? 'Obligatoire' : null,
              ),
              TextFormField(
                controller: _adresseController,
                decoration: InputDecoration(labelText: 'Adresse'),
                validator: (value) => value?.isEmpty ?? true ? 'Obligatoire' : null,
              ),
              DropdownButtonFormField<String>(
                value: _typeCompteur,
                decoration: InputDecoration(labelText: 'Type de compteur'),
                items: [
                  DropdownMenuItem(value: 'CLASSIQUE', child: Text('Classique')),
                  DropdownMenuItem(value: 'CASH_POWER', child: Text('Cash Power')),
                ],
                onChanged: (value) => setState(() => _typeCompteur = value!),
              ),
              TextFormField(
                controller: _valeurInitialeController,
                decoration: InputDecoration(labelText: 'Valeur initiale'),
                keyboardType: TextInputType.number,
                validator: (value) {
                  if (value?.isEmpty ?? true) return 'Obligatoire';
                  if (double.tryParse(value!) == null) return 'Numéro invalide';
                  return null;
                },
              ),
              SizedBox(height: 20),
              _isLoading
                  ? CircularProgressIndicator()
                  : ElevatedButton(
                      onPressed: _createCompteur,
                      child: Text('Créer'),
                    ),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _createCompteur() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);
    try {
      await _compteurService.createCompteur(
        reference: _referenceController.text,
        adresse: _adresseController.text,
        typeCompteur: _typeCompteur,
        valeurInitiale: double.parse(_valeurInitialeController.text),
      );
      
      Navigator.of(context).pop();
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Compteur créé avec succès')),
      );
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Erreur: ${e.toString()}')),
      );
    } finally {
      setState(() => _isLoading = false);
    }
  }
}
```

#### 3.3 Détail Compteur (screens/compteur_detail_screen.dart)
```dart
class CompteurDetailScreen extends StatefulWidget {
  final int compteurId;

  CompteurDetailScreen({required this.compteurId});

  @override
  _CompteurDetailScreenState createState() => _CompteurDetailScreenState();
}

class _CompteurDetailScreenState extends State<CompteurDetailScreen> {
  final CompteurService _compteurService = getIt<CompteurService>();
  final ReleveService _releveService = getIt<ReleveService>();
  final StatistiqueService _statistiqueService = getIt<StatistiqueService>();
  
  Compteur? _compteur;
  Releve? _latestReleve;
  Statistiques? _statistiques;
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    setState(() => _isLoading = true);
    try {
      // Charger les données en parallèle
      final results = await Future.wait([
        _compteurService.getCompteur(widget.compteurId),
        _releveService.getLatestReleve(widget.compteurId),
        _statistiqueService.getStatistiques(compteurId: widget.compteurId, periode: 'mois'),
      ]);
      
      setState(() {
        _compteur = results[0] as Compteur;
        _latestReleve = results[1] as Releve?;
        _statistiques = results[2] as Statistiques;
      });
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Erreur: ${e.toString()}')),
      );
    } finally {
      setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) return Scaffold(body: Center(child: CircularProgressIndicator()));
    if (_compteur == null) return Scaffold(body: Center(child: Text('Compteur non trouvé')));

    return Scaffold(
      appBar: AppBar(
        title: Text(_compteur!.reference),
        actions: [
          PopupMenuButton(
            itemBuilder: (context) => [
              PopupMenuItem(
                value: 'configure',
                child: Text('Configurer mode lecture'),
              ),
              PopupMenuItem(
                value: 'reset',
                child: Text('Réinitialiser'),
                enabled: _compteur!.typeCompteur == 'CLASSIQUE',
              ),
              PopupMenuItem(
                value: 'recharge',
                child: Text('Recharger'),
                enabled: _compteur!.typeCompteur == 'CASH_POWER',
              ),
            ],
            onSelected: _handleMenuAction,
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildInfoCard(),
            SizedBox(height: 20),
            _buildStatistiquesCard(),
            SizedBox(height: 20),
            _buildLatestReadingCard(),
            SizedBox(height: 20),
            _buildActionButtons(),
          ],
        ),
      ),
    );
  }

  Widget _buildInfoCard() {
    return Card(
      child: Padding(
        padding: EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Informations générales', style: Theme.of(context).textTheme.headline6),
            SizedBox(height: 10),
            _buildInfoRow('Référence', _compteur!.reference),
            _buildInfoRow('Type', _compteur!.typeCompteur),
            _buildInfoRow('Adresse', _compteur!.adresse),
            _buildInfoRow('Valeur actuelle', '${_compteur!.valeurActuelle.toStringAsFixed(2)}'),
            _buildInfoRow('Date initialisation', DateFormat('dd/MM/yyyy').format(_compteur!.dateInitialisation)),
          ],
        ),
      ),
    );
  }

  Widget _buildStatistiquesCard() {
    if (_statistiques == null) return SizedBox.shrink();

    return Card(
      child: Padding(
        padding: EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Statistiques du mois', style: Theme.of(context).textTheme.headline6),
            SizedBox(height: 10),
            _buildInfoRow('Consommation journalière', '${_statistiques!.consommationJour.toStringAsFixed(2)}'),
            _buildInfoRow('Consommation hebdomadaire', '${_statistiques!.consommationSemaine.toStringAsFixed(2)}'),
            _buildInfoRow('Consommation mensuelle', '${_statistiques!.consommationMois.toStringAsFixed(2)}'),
          ],
        ),
      ),
    );
  }

  Widget _buildLatestReadingCard() {
    if (_latestReleve == null) {
      return Card(
        child: Padding(
          padding: EdgeInsets.all(16.0),
          child: Text('Aucun relevé enregistré'),
        ),
      );
    }

    return Card(
      child: Padding(
        padding: EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Dernier relevé', style: Theme.of(context).textTheme.headline6),
            SizedBox(height: 10),
            _buildInfoRow('Valeur', '${_latestReleve!.valeur.toStringAsFixed(2)}'),
            _buildInfoRow('Date', DateFormat('dd/MM/yyyy HH:mm').format(_latestReleve!.dateTime)),
            _buildInfoRow('Source', _latestReleve!.source),
            if (_latestReleve!.consommationCalculee != null)
              _buildInfoRow('Consommation', '${_latestReleve!.consommationCalculee!.toStringAsFixed(2)}'),
          ],
        ),
      ),
    );
  }

  Widget _buildActionButtons() {
    return Column(
      children: [
        SizedBox(
          width: double.infinity,
          child: ElevatedButton.icon(
            onPressed: () => Navigator.push(
              context,
              MaterialPageRoute(
                builder: (_) => HistoriqueRelevesScreen(compteurId: widget.compteurId),
              ),
            ),
            icon: Icon(Icons.history),
            label: Text('Historique des relevés'),
          ),
        ),
        SizedBox(height: 10),
        SizedBox(
          width: double.infinity,
          child: ElevatedButton.icon(
            onPressed: () => Navigator.push(
              context,
              MaterialPageRoute(
                builder: (_) => AddReleveScreen(compteurId: widget.compteurId),
              ),
            ).then((_) => _loadData()),
            icon: Icon(Icons.add),
            label: Text('Ajouter un relevé'),
          ),
        ),
      ],
    );
  }

  Widget _buildInfoRow(String label, String value) {
    return Padding(
      padding: EdgeInsets.symmetric(vertical: 4.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: TextStyle(fontWeight: FontWeight.w500)),
          Text(value),
        ],
      ),
    );
  }

  void _handleMenuAction(String action) async {
    switch (action) {
      case 'configure':
        await Navigator.push(
          context,
          MaterialPageRoute(
            builder: (_) => ConfigureModeLectureScreen(compteurId: widget.compteurId),
          ),
        );
        _loadData();
        break;
      case 'reset':
        await _showResetDialog();
        break;
      case 'recharge':
        await Navigator.push(
          context,
          MaterialPageRoute(
            builder: (_) => RechargeCompteurScreen(compteurId: widget.compteurId),
          ),
        );
        _loadData();
        break;
    }
  }

  Future<void> _showResetDialog() async {
    final motif = await showDialog<String>(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Réinitialiser le compteur'),
        content: TextFormField(
          decoration: InputDecoration(labelText: 'Motif de réinitialisation'),
          autofocus: true,
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: Text('Annuler'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, 'Remplacement'),
            child: Text('Confirmer'),
          ),
        ],
      ),
    );

    if (motif != null) {
      try {
        await _compteurService.reinitialiserCompteur(
          compteurId: widget.compteurId,
          motif: motif,
        );
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Compteur réinitialisé')),
        );
        _loadData();
      } catch (e) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Erreur: ${e.toString()}')),
        );
      }
    }
  }
}
```

#### 3.4 Onboarding ESP32-CAM (screens/device_onboarding_screen.dart)
```dart
class DeviceOnboardingScreen extends StatefulWidget {
  @override
  _DeviceOnboardingScreenState createState() => _DeviceOnboardingScreenState();
}

class _DeviceOnboardingScreenState extends State<DeviceOnboardingScreen> {
  final DeviceService _deviceService = getIt<DeviceService>();
  final CompteurService _compteurService = getIt<CompteurService>();
  
  String _qrCode = '';
  DeviceEsp32? _currentDevice;
  List<Compteur> _compteurs = [];
  bool _isLoading = false;
  int _currentStep = 0;

  @override
  void initState() {
    super.initState();
    _loadCompteurs();
  }

  Future<void> _loadCompteurs() async {
    try {
      final compteurs = await _compteurService.getCompteurs();
      setState(() => _compteurs = compteurs.where((c) => c.typeCompteur != null).toList());
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Erreur: ${e.toString()}')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Configuration Module ESP32-CAM')),
      body: Stepper(
        currentStep: _currentStep,
        onStepContinue: _handleStepContinue,
        onStepCancel: _handleStepCancel,
        steps: [
          Step(
            title: Text('Scanner QR Code'),
            content: _buildQRScanStep(),
            isActive: _currentStep >= 0,
          ),
          Step(
            title: Text('Associer au compteur'),
            content: _buildAssociationStep(),
            isActive: _currentStep >= 1,
            state: _currentStep > 1 ? StepState.complete : StepState.indexed,
          ),
          Step(
            title: Text('Configuration Wi-Fi'),
            content: _buildWiFiConfigStep(),
            isActive: _currentStep >= 2,
            state: _currentStep > 2 ? StepState.complete : StepState.indexed,
          ),
          Step(
            title: Text('Vérification'),
            content: _buildVerificationStep(),
            isActive: _currentStep >= 3,
            state: _currentStep > 3 ? StepState.complete : StepState.indexed,
          ),
        ],
      ),
    );
  }

  Widget _buildQRScanStep() {
    return Column(
      children: [
        Text(
          'Scannez le QR code de votre module ESP32-CAM',
          style: Theme.of(context).textTheme.subtitle1,
        ),
        SizedBox(height: 20),
        TextField(
          decoration: InputDecoration(
            labelText: 'Code QR',
            hintText: 'MET-ESP32-XXX',
            suffixIcon: IconButton(
              icon: Icon(Icons.qr_code_scanner),
              onPressed: _scanQRCode,
            ),
          ),
          onChanged: (value) => setState(() => _qrCode = value),
        ),
        SizedBox(height: 20),
        if (_currentDevice != null)
          Card(
            child: Padding(
              padding: EdgeInsets.all(16.0),
              child: Column(
                children: [
                  Text('Module détecté:', style: TextStyle(fontWeight: FontWeight.bold)),
                  SizedBox(height: 10),
                  Text('Device Code: ${_currentDevice!.deviceCode}'),
                  Text('Statut: ${_currentDevice!.statut}'),
                ],
              ),
            ),
          ),
      ],
    );
  }

  Widget _buildAssociationStep() {
    if (_currentDevice == null) {
      return Text('Veuillez d\'abord scanner un QR code');
    }

    return Column(
      children: [
        Text(
          'Associez le module à un compteur existant',
          style: Theme.of(context).textTheme.subtitle1,
        ),
        SizedBox(height: 20),
        DropdownButtonFormField<int>(
          decoration: InputDecoration(labelText: 'Compteur'),
          items: _compteurs.map((compteur) {
            return DropdownMenuItem<int>(
              value: compteur.id,
              child: Text('${compteur.reference} - ${compteur.adresse}'),
            );
          }).toList(),
          onChanged: (value) {
            // Sélection du compteur
          },
        ),
        SizedBox(height: 20),
        TextFormField(
          decoration: InputDecoration(
            labelText: 'Intervalle de capture (secondes)',
            hintText: '3600',
          ),
          initialValue: '3600',
          keyboardType: TextInputType.number,
        ),
      ],
    );
  }

  Widget _buildWiFiConfigStep() {
    return Column(
      children: [
        Card(
          color: Colors.orange.shade50,
          child: Padding(
            padding: EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('Instructions de configuration Wi-Fi:', style: TextStyle(fontWeight: FontWeight.bold)),
                SizedBox(height: 10),
                Text('1. Le module crée un point d\'accès Wi-Fi: "MeterEye-Setup"'),
                Text('2. Connectez-vous à ce Wi-Fi'),
                Text('3. Ouvrez votre navigateur à l\'adresse: http://192.168.4.1'),
                Text('4. Configurez votre Wi-Fi domestique'),
                Text('5. Le module redémarrera automatiquement'),
              ],
            ),
          ),
        ),
        SizedBox(height: 20),
        ElevatedButton(
          onPressed: _checkDeviceStatus,
          child: Text('Vérifier le statut du module'),
        ),
      ],
    );
  }

  Widget _buildVerificationStep() {
    if (_currentDevice == null) {
      return Text('Module non configuré');
    }

    return Column(
      children: [
        Card(
          color: _currentDevice!.statut == 'ACTIF' ? Colors.green.shade50 : Colors.orange.shade50,
          child: Padding(
            padding: EdgeInsets.all(16.0),
            child: Column(
              children: [
                Icon(
                  _currentDevice!.statut == 'ACTIF' ? Icons.check_circle : Icons.settings,
                  color: _currentDevice!.statut == 'ACTIF' ? Colors.green : Colors.orange,
                  size: 48,
                ),
                SizedBox(height: 10),
                Text(
                  _currentDevice!.statut == 'ACTIF' 
                    ? 'Module configuré avec succès!' 
                    : 'Configuration en cours...',
                  style: TextStyle(fontWeight: FontWeight.bold),
                ),
                SizedBox(height: 10),
                Text('Device Code: ${_currentDevice!.deviceCode}'),
                Text('Statut: ${_currentDevice!.statut}'),
                if (_currentDevice!.lastSeenAt != null)
                  Text('Dernière vue: ${DateFormat('dd/MM/yyyy HH:mm').format(_currentDevice!.lastSeenAt!)}'),
              ],
            ),
          ),
        ),
        SizedBox(height: 20),
        ElevatedButton(
          onPressed: _checkDeviceStatus,
          child: Text('Actualiser le statut'),
        ),
      ],
    );
  }

  Future<void> _scanQRCode() async {
    // Implémentation du scan QR code
    // Utiliser un package comme mobile_scanner
    final scannedCode = await Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => QRScannerScreen()),
    );

    if (scannedCode != null) {
      setState(() => _qrCode = scannedCode);
      await _registerDevice();
    }
  }

  Future<void> _registerDevice() async {
    if (_qrCode.isEmpty) return;

    setState(() => _isLoading = true);
    try {
      final device = await _deviceService.scanQRCode(
        qrCode: _qrCode,
        userId: 1, // TODO: Récupérer l'ID utilisateur connecté
      );
      setState(() => _currentDevice = device);
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Erreur: ${e.toString()}')),
      );
    } finally {
      setState(() => _isLoading = false);
    }
  }

  Future<void> _checkDeviceStatus() async {
    if (_currentDevice == null) return;

    setState(() => _isLoading = true);
    try {
      final device = await _deviceService.getDeviceStatus(_currentDevice!.deviceCode);
      setState(() => _currentDevice = device);
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Erreur: ${e.toString()}')),
      );
    } finally {
      setState(() => _isLoading = false);
    }
  }

  void _handleStepContinue() async {
    switch (_currentStep) {
      case 0:
        if (_currentDevice == null) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Veuillez scanner un QR code')),
          );
          return;
        }
        break;
      case 1:
        // Logique d'association
        break;
      case 2:
        await _checkDeviceStatus();
        if (_currentDevice?.statut != 'ACTIF') {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Attendez que le module se connecte')),
          );
          return;
        }
        break;
      case 3:
        Navigator.of(context).pop();
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Module configuré avec succès!')),
        );
        return;
    }

    setState(() => _currentStep++);
  }

  void _handleStepCancel() {
    if (_currentStep > 0) {
      setState(() => _currentStep--);
    }
  }
}
```

### 4. UTILITAIRES (lib/utils/)

```dart
// utils/paginated_response.dart
class PaginatedResponse<T> {
  final List<T> content;
  final int page;
  final int size;
  final int totalElements;
  final int totalPages;

  PaginatedResponse({
    required this.content,
    required this.page,
    required this.size,
    required this.totalElements,
    required this.totalPages,
  });

  factory PaginatedResponse.fromJson(
    Map<String, dynamic> json,
    T Function(Map<String, dynamic>) fromJsonT,
  ) {
    return PaginatedResponse<T>(
      content: (json['content'] as List)
          .map((item) => fromJsonT(item as Map<String, dynamic>))
          .toList(),
      page: json['page'],
      size: json['size'],
      totalElements: json['totalElements'],
      totalPages: json['totalPages'],
    );
  }
}

// utils/api_exception.dart
class ApiException implements Exception {
  final String message;
  final int statusCode;

  ApiException(this.message, this.statusCode);

  @override
  String toString() => 'ApiException: $message ($statusCode)';
}
```

### 5. DÉPENDANCES À AJOUTER (pubspec.yaml)

```yaml
dependencies:
  # ... existing dependencies
  
  # Pour le scan QR code
  mobile_scanner: ^3.5.6
  
  # Pour les images
  image_picker: ^1.0.4
  
  # Pour les graphiques
  fl_chart: ^0.63.0
  
  # Pour les dates
  intl: ^0.18.1
  
  # Pour le stockage local
  shared_preferences: ^2.2.2
  
  # Pour les animations
  lottie: ^2.7.0
```

## INSTRUCTIONS D'IMPLÉMENTATION

1. **Créer tous les models** selon les spécifications
2. **Implémenter les services** avec gestion d'erreurs
3. **Créer les écrans UI** avec Material Design
4. **Ajouter la navigation** entre les écrans
5. **Tester tous les flux** avec le backend
6. **Gérer les états de chargement** et les erreurs
7. **Optimiser les performances** avec lazy loading
8. **Ajouter des tests unitaires** pour les services

## WORKFLOWS À TESTER

1. **Création compteur** -> **Configuration mode lecture** -> **Saisie relevé**
2. **Scan QR code** -> **Association module** -> **Configuration Wi-Fi** -> **Vérification**
3. **Dashboard** -> **Liste compteurs** -> **Détails** -> **Historique** -> **Statistiques**

Le tout doit être intégré dans l'architecture existante du Sprint 1 avec cohérence et réutilisabilité.
