# 📚 Guide Complet - Street Workout Backend

## 🎯 Vue d'ensemble du projet

Ce backend est une **API REST complète** pour une application de Street Workout. Il gère :
- ✅ Authentification JWT (inscription/connexion)
- ✅ Gestion du profil utilisateur
- ✅ Catalogue de 20 exercices de calisthenics
- ✅ Création et suivi de séances d'entraînement
- ✅ Suivi de progression (poids, notes)
- ✅ Statistiques d'entraînement

---

## 🏗️ Architecture détaillée

### 1. **Couche Entity (Modèles de données)**

Les entités représentent les tables PostgreSQL via JPA/Hibernate.

#### **User.java**
```java
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue
    private Long id;
    private String username;  // unique
    private String email;     // unique
    private String password;  // hashé avec BCrypt
    private String firstName;
    private String lastName;
    private BigDecimal height;
    private BigDecimal weight;
    private LocalDateTime createdAt;
    
    // Relations
    @OneToMany(mappedBy = "user")
    private List<Workout> workouts;
    
    @OneToMany(mappedBy = "user")
    private List<Progress> progressList;
}
```

**Explications** :
- `@Entity` : indique que c'est une table en base
- `@GeneratedValue` : auto-incrémentation de l'ID
- `@OneToMany` : un utilisateur a plusieurs séances/progressions
- Le mot de passe est **toujours hashé** avec BCrypt (jamais en clair)

#### **Exercise.java**
```java
@Entity
@Table(name = "exercises")
public class Exercise {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String description;
    
    @Enumerated(EnumType.STRING)
    private Category category;  // PUSH, PULL, LEGS, CORE
    
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;  // BEGINNER, INTERMEDIATE, ADVANCED
    
    private String muscleGroup;
    private String imageUrl;
}
```

**Explications** :
- `@Enumerated(EnumType.STRING)` : stocke "PUSH" au lieu de 0 en base
- Les exercices sont **en lecture seule** pour les utilisateurs (créés par les admins)

#### **Workout.java**
```java
@Entity
@Table(name = "workouts")
public class Workout {
    @Id @GeneratedValue
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private String title;
    private LocalDate workoutDate;
    private Integer duration;
    private Integer totalCalories;
    private String notes;
    
    @OneToMany(mappedBy = "workout", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkoutExercise> workoutExercises;
}
```

**Explications** :
- `@ManyToOne` : plusieurs séances appartiennent à un utilisateur
- `cascade = CascadeType.ALL` : si on supprime une séance, ses exercices sont supprimés
- `orphanRemoval = true` : si on retire un exercice de la liste, il est supprimé en base

#### **WorkoutExercise.java** (table de liaison)
```java
@Entity
@Table(name = "workout_exercises")
public class WorkoutExercise {
    @Id @GeneratedValue
    private Long id;
    
    @ManyToOne
    private Workout workout;
    
    @ManyToOne
    private Exercise exercise;
    
    private Integer sets;  // nombre de séries
    private Integer reps;  // nombre de répétitions
}
```

**Explications** :
- Relie une séance à un exercice avec des données spécifiques (sets/reps)
- Permet de stocker : "Dans ma séance du lundi, j'ai fait 3 séries de 15 pompes"

#### **Progress.java**
```java
@Entity
@Table(name = "progress", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "progress_date"})
})
public class Progress {
    @Id @GeneratedValue
    private Long id;
    
    @ManyToOne
    private User user;
    
    private LocalDate progressDate;
    private BigDecimal weight;
    private String notes;
}
```

**Explications** :
- `@UniqueConstraint` : un utilisateur ne peut avoir qu'une seule entrée par date
- Permet de suivre l'évolution du poids dans le temps

---

### 2. **Couche Repository (Accès base de données)**

Les repositories utilisent **Spring Data JPA** pour générer automatiquement les requêtes SQL.

#### **UserRepository.java**
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
```

**Explications** :
- `JpaRepository<User, Long>` : CRUD automatique (save, findById, delete, etc.)
- Spring génère automatiquement le SQL à partir du nom de la méthode
- `findByEmail` → `SELECT * FROM users WHERE email = ?`

#### **WorkoutRepository.java**
```java
public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    List<Workout> findByUserIdOrderByWorkoutDateDesc(Long userId);
    
    List<Workout> findByUserIdAndWorkoutDateBetweenOrderByWorkoutDateDesc(
        Long userId, LocalDate startDate, LocalDate endDate
    );
    
    Optional<Workout> findByIdAndUserId(Long id, Long userId);
    
    @Query("SELECT COALESCE(SUM(w.totalCalories), 0) FROM Workout w WHERE w.user.id = :userId")
    Integer sumCaloriesByUserId(@Param("userId") Long userId);
}
```

**Explications** :
- `findByUserIdOrderByWorkoutDateDesc` : séances d'un utilisateur, triées par date
- `findByIdAndUserId` : vérifie que la séance appartient bien à l'utilisateur (sécurité)
- `@Query` : requête JPQL personnalisée pour les statistiques

---

### 3. **Couche Security (JWT)**

#### **JwtTokenProvider.java**

Génère et valide les tokens JWT.

```java
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpirationMs;
    
    // Génère un token JWT
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        
        return Jwts.builder()
            .setSubject(userDetails.getUsername())  // email
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }
    
    // Extrait l'email du token
    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }
    
    // Valide le token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

**Explications** :
- Un JWT contient 3 parties : `Header.Payload.Signature`
- Le payload contient l'email et la date d'expiration
- La signature garantit que le token n'a pas été modifié
- Durée de validité : 24h (configurable)

#### **JwtAuthenticationFilter.java**

Filtre HTTP qui s'exécute à chaque requête.

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) {
        // 1. Extraire le JWT du header Authorization
        String jwt = extractTokenFromRequest(request);
        
        // 2. Valider le token
        if (jwt != null && tokenProvider.validateToken(jwt)) {
            String email = tokenProvider.getEmailFromToken(jwt);
            
            // 3. Charger l'utilisateur depuis la base
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            
            // 4. Créer l'authentification Spring Security
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
                );
            
            // 5. Injecter dans le contexte de sécurité
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        // Continuer la chaîne de filtres
        filterChain.doFilter(request, response);
    }
    
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

**Explications** :
- S'exécute **avant** chaque requête HTTP
- Extrait le JWT du header `Authorization: Bearer <token>`
- Si le token est valide, l'utilisateur est authentifié automatiquement
- Les controllers peuvent ensuite accéder à l'utilisateur via `SecurityContextHolder`

#### **SecurityConfig.java**

Configuration globale de Spring Security.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())  // Désactiver CSRF (inutile avec JWT)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // Pas de session HTTP
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()  // Routes publiques
                .requestMatchers(HttpMethod.GET, "/api/exercises/**").permitAll()
                .anyRequest().authenticated()  // Tout le reste nécessite un JWT
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

**Explications** :
- `STATELESS` : pas de session HTTP, uniquement JWT
- `permitAll()` : routes accessibles sans JWT
- `authenticated()` : routes nécessitant un JWT valide
- Le filtre JWT s'exécute **avant** le filtre standard de Spring

---

### 4. **Couche Service (Logique métier)**

#### **AuthService.java**

Gère l'inscription et la connexion.

```java
@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    // Inscription
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 1. Vérifier l'unicité de l'email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }
        
        // 2. Créer l'utilisateur
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));  // Hachage BCrypt
        // ... autres champs
        
        User savedUser = userRepository.save(user);
        
        // 3. Générer le JWT
        String token = tokenProvider.generateTokenFromEmail(savedUser.getEmail());
        
        return new AuthResponse(token, UserResponse.fromEntity(savedUser));
    }
    
    // Connexion
    public AuthResponse login(LoginRequest request) {
        // 1. Vérifier email + mot de passe
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );
        
        // 2. Générer le JWT
        String token = tokenProvider.generateToken(authentication);
        
        // 3. Récupérer l'utilisateur
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        return new AuthResponse(token, UserResponse.fromEntity(user));
    }
}
```

**Explications** :
- `passwordEncoder.encode()` : hache le mot de passe avec BCrypt (irréversible)
- `authenticationManager.authenticate()` : vérifie email + mot de passe
- Si l'authentification réussit, un JWT est généré et retourné

#### **WorkoutService.java**

Gère les séances d'entraînement.

```java
@Service
public class WorkoutService {
    @Autowired
    private WorkoutRepository workoutRepository;
    
    @Autowired
    private ExerciseRepository exerciseRepository;
    
    @Autowired
    private UserService userService;
    
    // Créer une séance
    @Transactional
    public WorkoutResponse create(WorkoutRequest request) {
        User user = userService.getCurrentUser();  // Utilisateur connecté
        
        Workout workout = new Workout();
        workout.setUser(user);
        workout.setTitle(request.getTitle());
        workout.setWorkoutDate(request.getWorkoutDate());
        // ... autres champs
        
        // Ajouter les exercices
        for (WorkoutRequest.WorkoutExerciseRequest exReq : request.getExercises()) {
            Exercise exercise = exerciseRepository.findById(exReq.getExerciseId())
                .orElseThrow(() -> new RuntimeException("Exercice non trouvé"));
            
            WorkoutExercise workoutExercise = new WorkoutExercise();
            workoutExercise.setWorkout(workout);
            workoutExercise.setExercise(exercise);
            workoutExercise.setSets(exReq.getSets());
            workoutExercise.setReps(exReq.getReps());
            
            workout.getWorkoutExercises().add(workoutExercise);
        }
        
        Workout saved = workoutRepository.save(workout);
        return WorkoutResponse.fromEntity(saved);
    }
}
```

**Explications** :
- `getCurrentUser()` : récupère l'utilisateur depuis le JWT
- `@Transactional` : si une erreur survient, tout est annulé (rollback)
- Les exercices sont ajoutés à la séance via la relation `@OneToMany`

---

### 5. **Couche Controller (API REST)**

#### **AuthController.java**

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
```

**Explications** :
- `@RestController` : retourne du JSON automatiquement
- `@RequestMapping("/api/auth")` : préfixe de toutes les routes
- `@Valid` : valide les données avec les annotations `@NotBlank`, `@Email`, etc.
- `ResponseEntity` : permet de contrôler le code HTTP (200, 201, 400, etc.)

#### **WorkoutController.java**

```java
@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {
    @Autowired
    private WorkoutService workoutService;
    
    @GetMapping
    public ResponseEntity<List<WorkoutResponse>> getMyWorkouts(
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate
    ) {
        return ResponseEntity.ok(workoutService.getMyWorkouts(startDate, endDate));
    }
    
    @PostMapping
    public ResponseEntity<WorkoutResponse> create(@Valid @RequestBody WorkoutRequest request) {
        WorkoutResponse response = workoutService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workoutService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

**Explications** :
- `@GetMapping` : route GET
- `@PostMapping` : route POST
- `@DeleteMapping("/{id}")` : route DELETE avec paramètre d'URL
- `@RequestParam` : paramètre de requête (`?startDate=2024-01-01`)
- `@PathVariable` : paramètre d'URL (`/api/workouts/5`)

---

## 🔐 Flux d'authentification complet

### 1. Inscription

```
Client                    Backend                   Database
  |                          |                          |
  |-- POST /api/auth/register -->                       |
  |    { email, password }   |                          |
  |                          |                          |
  |                          |-- Vérifier email unique -->
  |                          |<-- OK -------------------|
  |                          |                          |
  |                          |-- Hasher mot de passe    |
  |                          |                          |
  |                          |-- INSERT INTO users ---->
  |                          |<-- User créé ------------|
  |                          |                          |
  |                          |-- Générer JWT            |
  |                          |                          |
  |<-- 201 Created -----------|                          |
  |    { token, user }       |                          |
```

### 2. Connexion

```
Client                    Backend                   Database
  |                          |                          |
  |-- POST /api/auth/login -->                          |
  |    { email, password }   |                          |
  |                          |                          |
  |                          |-- SELECT * FROM users -->
  |                          |<-- User trouvé ----------|
  |                          |                          |
  |                          |-- Vérifier BCrypt        |
  |                          |    (compare hashes)      |
  |                          |                          |
  |                          |-- Générer JWT            |
  |                          |                          |
  |<-- 200 OK ---------------|                          |
  |    { token, user }       |                          |
```

### 3. Requête protégée

```
Client                    Backend                   Database
  |                          |                          |
  |-- GET /api/users/me ---->                           |
  |    Authorization:        |                          |
  |    Bearer <JWT>          |                          |
  |                          |                          |
  |                          |-- Valider JWT            |
  |                          |-- Extraire email         |
  |                          |                          |
  |                          |-- SELECT * FROM users -->
  |                          |<-- User trouvé ----------|
  |                          |                          |
  |<-- 200 OK ---------------|                          |
  |    { user data }         |                          |
```

---

## 📊 Schéma de base de données

```
┌─────────────────┐
│     USERS       │
├─────────────────┤
│ id (PK)         │
│ username        │◄──────┐
│ email           │       │
│ password        │       │
│ first_name      │       │
│ last_name       │       │
│ height          │       │
│ weight          │       │
│ created_at      │       │
└─────────────────┘       │
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
        │                 │                 │
┌───────▼────────┐ ┌──────▼──────┐ ┌────────▼────────┐
│   WORKOUTS     │ │  PROGRESS   │ │   EXERCISES     │
├────────────────┤ ├─────────────┤ ├─────────────────┤
│ id (PK)        │ │ id (PK)     │ │ id (PK)         │
│ user_id (FK)   │ │ user_id (FK)│ │ name            │
│ title          │ │ date        │ │ description     │
│ workout_date   │ │ weight      │ │ category        │
│ duration       │ │ notes       │ │ difficulty      │
│ total_calories │ └─────────────┘ │ muscle_group    │
│ notes          │                 │ image_url       │
└────────┬───────┘                 └────────┬────────┘
         │                                  │
         │         ┌────────────────────────┘
         │         │
         │         │
┌────────▼─────────▼──┐
│ WORKOUT_EXERCISES   │
├─────────────────────┤
│ id (PK)             │
│ workout_id (FK)     │
│ exercise_id (FK)    │
│ sets                │
│ reps                │
└─────────────────────┘
```

---

## 🚀 Démarrage rapide

### 1. Créer la base de données

```bash
psql -U postgres
CREATE DATABASE streetworkout;
\q

psql -U postgres -d streetworkout -f database/schema.sql
```

### 2. Configurer `application.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/streetworkout
spring.datasource.username=postgres
spring.datasource.password=VotreMotDePasse
```

### 3. Lancer l'application

```bash
./mvnw spring-boot:run
```

### 4. Tester avec cURL

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

## 🌐 Connexion avec le front-end

### Angular

```typescript
// auth.service.ts
import { HttpClient } from '@angular/common/http';

export class AuthService {
  private API_URL = 'http://localhost:8080/api';
  
  constructor(private http: HttpClient) {}
  
  register(data: any) {
    return this.http.post(`${this.API_URL}/auth/register`, data);
  }
  
  login(credentials: any) {
    return this.http.post(`${this.API_URL}/auth/login`, credentials)
      .pipe(tap((response: any) => {
        localStorage.setItem('token', response.token);
      }));
  }
  
  getProfile() {
    const token = localStorage.getItem('token');
    const headers = { 'Authorization': `Bearer ${token}` };
    return this.http.get(`${this.API_URL}/users/me`, { headers });
  }
}
```

### React

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

export const register = (data) => API.post('/auth/register', data);
export const login = (credentials) => API.post('/auth/login', credentials);
export const getProfile = () => API.get('/users/me');
export const getWorkouts = () => API.get('/workouts');
export const createWorkout = (data) => API.post('/workouts', data);
```

---

## 📝 Résumé des concepts clés

| Concept | Explication |
|---------|-------------|
| **JWT** | Token d'authentification stateless (pas de session) |
| **BCrypt** | Algorithme de hachage sécurisé pour les mots de passe |
| **JPA** | ORM qui transforme les objets Java en tables SQL |
| **@Transactional** | Garantit l'atomicité (tout ou rien) |
| **@OneToMany** | Relation 1-N (un utilisateur a plusieurs séances) |
| **@ManyToOne** | Relation N-1 (plusieurs séances appartiennent à un utilisateur) |
| **CORS** | Autorise les requêtes depuis le front-end |
| **DTO** | Objet de transfert de données (évite d'exposer les entités) |
| **Repository** | Interface d'accès aux données (génère le SQL automatiquement) |

---

**Bon développement ! 💪🚀**
