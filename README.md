# 🏋️ Street Workout API - Backend

API REST complète pour l'application Street Workout, développée avec **Spring Boot 3.3.4** et **PostgreSQL**.

---

## 📋 Table des matières

1. [Architecture](#architecture)
2. [Technologies](#technologies)
3. [Installation](#installation)
4. [Configuration](#configuration)
5. [Lancer l'application](#lancer-lapplication)
6. [API Endpoints](#api-endpoints)
7. [Authentification JWT](#authentification-jwt)
8. [Connexion avec le Front-end](#connexion-avec-le-front-end)
9. [Structure du projet](#structure-du-projet)

---

## 🏗️ Architecture

L'application suit une architecture **en couches** (Layered Architecture) :

```
┌─────────────────────────────────────┐
│         Controllers (REST)          │  ← Endpoints HTTP
├─────────────────────────────────────┤
│            Services                 │  ← Logique métier
├─────────────────────────────────────┤
│          Repositories               │  ← Accès base de données (JPA)
├─────────────────────────────────────┤
│            Entities                 │  ← Modèles de données
└─────────────────────────────────────┘
           ↓
    PostgreSQL Database
```

**Sécurité** : JWT (JSON Web Token) pour l'authentification stateless.

---

## 🛠️ Technologies

| Technologie | Version | Rôle |
|------------|---------|------|
| **Java** | 17 | Langage |
| **Spring Boot** | 3.3.4 | Framework backend |
| **Spring Security** | 6.x | Sécurité + JWT |
| **Spring Data JPA** | 3.x | ORM (Hibernate) |
| **PostgreSQL** | 12+ | Base de données |
| **JJWT** | 0.11.5 | Génération/validation JWT |
| **Lombok** | 1.18.x | Réduction boilerplate |
| **Maven** | 3.8+ | Gestionnaire de dépendances |

---

## 📦 Installation

### Prérequis

- **Java 17** ou supérieur ([Télécharger](https://adoptium.net/))
- **PostgreSQL 12+** ([Télécharger](https://www.postgresql.org/download/))
- **Maven 3.8+** (inclus dans le projet via `mvnw`)

### 1. Cloner le projet

```bash
git clone <url-du-repo>
cd streetworkout-backend
```

### 2. Créer la base de données PostgreSQL

Ouvrir **pgAdmin** ou **psql** et exécuter :

```sql
CREATE DATABASE streetworkout;
```

### 3. Initialiser le schéma et les données

Exécuter le script SQL fourni :

```bash
psql -U postgres -d streetworkout -f database/schema.sql
```

Ou copier-coller le contenu de `database/schema.sql` dans pgAdmin.

---

## ⚙️ Configuration

Éditer le fichier `src/main/resources/application.properties` :

```properties
# Base de données PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/streetworkout
spring.datasource.username=postgres
spring.datasource.password=VotreMotDePasse

# JWT (changer le secret en production !)
jwt.secret=VotreCleSuperSecreteDeMinimum256BitsIciPourJWTStreetWorkoutApplicationSecure2024
jwt.expiration=86400000

# CORS (adapter selon votre front-end)
cors.allowed-origins=http://localhost:4200,http://localhost:3000
```

**⚠️ IMPORTANT** : En production, utiliser des variables d'environnement pour les secrets !

---

## 🚀 Lancer l'application

### Avec Maven Wrapper (recommandé)

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### Avec Maven installé

```bash
mvn spring-boot:run
```

L'API démarre sur **http://localhost:8080**

---

## 📡 API Endpoints

### 🔓 Routes publiques (pas de JWT requis)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `POST` | `/api/auth/register` | Inscription |
| `POST` | `/api/auth/login` | Connexion |
| `GET` | `/api/exercises` | Liste des exercices |
| `GET` | `/api/exercises/{id}` | Détails d'un exercice |

### 🔒 Routes protégées (JWT requis)

#### 👤 Profil utilisateur

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/api/users/me` | Mon profil |
| `PUT` | `/api/users/me` | Modifier mon profil |

#### 💪 Séances d'entraînement

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/api/workouts` | Mes séances |
| `GET` | `/api/workouts/{id}` | Détails d'une séance |
| `POST` | `/api/workouts` | Créer une séance |
| `PUT` | `/api/workouts/{id}` | Modifier une séance |
| `DELETE` | `/api/workouts/{id}` | Supprimer une séance |
| `GET` | `/api/workouts/stats` | Mes statistiques |

#### 📈 Progression

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/api/progress` | Mon historique |
| `POST` | `/api/progress` | Ajouter une entrée |
| `PUT` | `/api/progress/{id}` | Modifier une entrée |
| `DELETE` | `/api/progress/{id}` | Supprimer une entrée |

---

## 🔐 Authentification JWT

### 1. Inscription

**Requête** :
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "motdepasse123",
  "firstName": "John",
  "lastName": "Doe",
  "height": 180.5,
  "weight": 75.0
}
```

**Réponse** :
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwiaWF0IjoxNzA1MzIwMDAwLCJleHAiOjE3MDU0MDY0MDB9.signature",
  "type": "Bearer",
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "height": 180.5,
    "weight": 75.0,
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

### 2. Connexion

**Requête** :
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "motdepasse123"
}
```

**Réponse** : identique à l'inscription.

### 3. Utiliser le JWT

Pour toutes les routes protégées, ajouter le header :

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Durée de validité** : 24 heures (configurable dans `application.properties`).

---

## 🌐 Connexion avec le Front-end

### Angular (HttpClient)

```typescript
// auth.service.ts
import { HttpClient, HttpHeaders } from '@angular/common/http';

const API_URL = 'http://localhost:8080/api';

// Inscription
register(data: any) {
  return this.http.post(`${API_URL}/auth/register`, data);
}

// Connexion
login(credentials: any) {
  return this.http.post(`${API_URL}/auth/login`, credentials);
}

// Requête protégée
getProfile() {
  const token = localStorage.getItem('token');
  const headers = new HttpHeaders({
    'Authorization': `Bearer ${token}`
  });
  return this.http.get(`${API_URL}/users/me`, { headers });
}
```

### React (Axios)

```javascript
// api.js
import axios from 'axios';

const API = axios.create({
  baseURL: 'http://localhost:8080/api'
});

// Intercepteur pour ajouter le JWT automatiquement
API.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Inscription
export const register = (data) => API.post('/auth/register', data);

// Connexion
export const login = (credentials) => API.post('/auth/login', credentials);

// Profil
export const getProfile = () => API.get('/users/me');
```

### Gestion du token

1. **Stocker** le token après connexion :
   ```javascript
   localStorage.setItem('token', response.data.token);
   ```

2. **Envoyer** le token dans chaque requête :
   ```
   Authorization: Bearer <token>
   ```

3. **Supprimer** le token à la déconnexion :
   ```javascript
   localStorage.removeItem('token');
   ```

---

## 📁 Structure du projet

```
streetworkout-backend/
├── src/main/java/com/streetworkout/
│   ├── StreetWorkoutApplication.java    # Point d'entrée
│   ├── config/
│   │   └── SecurityConfig.java          # Configuration Spring Security + CORS
│   ├── controller/
│   │   ├── AuthController.java          # Login/Register
│   │   ├── UserController.java          # Profil
│   │   ├── ExerciseController.java      # Exercices
│   │   ├── WorkoutController.java       # Séances
│   │   └── ProgressController.java      # Progression
│   ├── dto/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── WorkoutRequest.java
│   │   ├── UserResponse.java
│   │   └── ...
│   ├── entity/
│   │   ├── User.java
│   │   ├── Exercise.java
│   │   ├── Workout.java
│   │   ├── WorkoutExercise.java
│   │   └── Progress.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── ExerciseRepository.java
│   │   ├── WorkoutRepository.java
│   │   └── ProgressRepository.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── UserService.java
│   │   ├── ExerciseService.java
│   │   ├── WorkoutService.java
│   │   └── ProgressService.java
│   ├── security/
│   │   ├── JwtTokenProvider.java        # Génération/validation JWT
│   │   ├── JwtAuthenticationFilter.java # Filtre HTTP
│   │   └── CustomUserDetailsService.java
│   └── exception/
│       └── GlobalExceptionHandler.java  # Gestion des erreurs
├── src/main/resources/
│   └── application.properties           # Configuration
├── database/
│   └── schema.sql                       # Script SQL
├── pom.xml                              # Dépendances Maven
└── README.md
```

---

## 🧪 Tester l'API

### Avec Postman

1. Importer la collection (à créer) ou tester manuellement
2. Inscription : `POST http://localhost:8080/api/auth/register`
3. Copier le `token` de la réponse
4. Ajouter le header `Authorization: Bearer <token>` pour les routes protégées

### Avec cURL

```bash
# Inscription
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@test.com","password":"test123"}'

# Connexion
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test123"}'

# Profil (remplacer <TOKEN>)
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 🔧 Dépannage

### Erreur : "Port 8080 already in use"

Changer le port dans `application.properties` :
```properties
server.port=8081
```

### Erreur : "Connection refused" (PostgreSQL)

Vérifier que PostgreSQL est démarré :
```bash
# Windows
net start postgresql-x64-14

# Linux
sudo systemctl start postgresql
```

### Erreur : "JWT signature does not match"

Le secret JWT a changé. Supprimer les anciens tokens et se reconnecter.

---

## 📝 Licence

Projet éducatif - Libre d'utilisation.

---

## 👨‍💻 Auteur

Développé pour le projet Street Workout.

---

**Bon développement ! 💪🚀**
