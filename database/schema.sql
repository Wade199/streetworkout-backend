-- ===================================================
-- BASE DE DONNÉES STREET WORKOUT - MVP
-- ===================================================
-- PostgreSQL 12+
-- Encodage : UTF-8
-- ===================================================

-- Supprimer les tables si elles existent (pour réinitialisation)
DROP TABLE IF EXISTS workout_exercises CASCADE;
DROP TABLE IF EXISTS workouts CASCADE;
DROP TABLE IF EXISTS progress CASCADE;
DROP TABLE IF EXISTS exercises CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ===================================================
-- 1. TABLE USERS
-- ===================================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    height DECIMAL(5,2),
    weight DECIMAL(5,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===================================================
-- 2. TABLE EXERCISES
-- ===================================================
CREATE TABLE exercises (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL, -- PUSH, PULL, LEGS, CORE
    difficulty VARCHAR(20) NOT NULL, -- BEGINNER, INTERMEDIATE, ADVANCED
    muscle_group VARCHAR(50),
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===================================================
-- 3. TABLE WORKOUTS
-- ===================================================
CREATE TABLE workouts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    workout_date DATE NOT NULL,
    duration INTEGER, -- en minutes
    total_calories INTEGER,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ===================================================
-- 4. TABLE WORKOUT_EXERCISES (table de liaison)
-- ===================================================
CREATE TABLE workout_exercises (
    id BIGSERIAL PRIMARY KEY,
    workout_id BIGINT NOT NULL,
    exercise_id BIGINT NOT NULL,
    sets INTEGER NOT NULL,
    reps INTEGER NOT NULL,
    FOREIGN KEY (workout_id) REFERENCES workouts(id) ON DELETE CASCADE,
    FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE
);

-- ===================================================
-- 5. TABLE PROGRESS
-- ===================================================
CREATE TABLE progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    progress_date DATE NOT NULL,
    weight DECIMAL(5,2),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(user_id, progress_date)
);

-- ===================================================
-- INDEX POUR PERFORMANCES
-- ===================================================
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_workouts_user ON workouts(user_id);
CREATE INDEX idx_workouts_date ON workouts(workout_date);
CREATE INDEX idx_progress_user ON progress(user_id);
CREATE INDEX idx_workout_exercises_workout ON workout_exercises(workout_id);
CREATE INDEX idx_workout_exercises_exercise ON workout_exercises(exercise_id);

-- ===================================================
-- DONNÉES INITIALES - 20 EXERCICES DE BASE
-- ===================================================
INSERT INTO exercises (name, description, category, difficulty, muscle_group, image_url) VALUES
-- PUSH
('Push-ups', 'Pompes classiques au sol', 'PUSH', 'BEGINNER', 'CHEST', '/assets/exercises/pushups.jpg'),
('Diamond Push-ups', 'Pompes avec mains en diamant', 'PUSH', 'INTERMEDIATE', 'TRICEPS', '/assets/exercises/diamond-pushups.jpg'),
('Pike Push-ups', 'Pompes pike pour les épaules', 'PUSH', 'INTERMEDIATE', 'SHOULDERS', '/assets/exercises/pike-pushups.jpg'),
('Dips', 'Dips sur barres parallèles', 'PUSH', 'INTERMEDIATE', 'CHEST', '/assets/exercises/dips.jpg'),
('Handstand Push-ups', 'Pompes en équilibre sur les mains', 'PUSH', 'ADVANCED', 'SHOULDERS', '/assets/exercises/handstand-pushups.jpg'),

-- PULL
('Pull-ups', 'Tractions pronation', 'PULL', 'INTERMEDIATE', 'BACK', '/assets/exercises/pullups.jpg'),
('Chin-ups', 'Tractions supination', 'PULL', 'INTERMEDIATE', 'BICEPS', '/assets/exercises/chinups.jpg'),
('Australian Pull-ups', 'Tractions australiennes', 'PULL', 'BEGINNER', 'BACK', '/assets/exercises/australian-pullups.jpg'),
('Muscle-ups', 'Muscle-ups complets', 'PULL', 'ADVANCED', 'BACK', '/assets/exercises/muscle-ups.jpg'),

-- LEGS
('Squats', 'Squats au poids du corps', 'LEGS', 'BEGINNER', 'LEGS', '/assets/exercises/squats.jpg'),
('Jump Squats', 'Squats sautés', 'LEGS', 'INTERMEDIATE', 'LEGS', '/assets/exercises/jump-squats.jpg'),
('Lunges', 'Fentes avant', 'LEGS', 'BEGINNER', 'LEGS', '/assets/exercises/lunges.jpg'),
('Pistol Squats', 'Squats sur une jambe', 'LEGS', 'ADVANCED', 'LEGS', '/assets/exercises/pistol-squats.jpg'),
('Calf Raises', 'Élévations mollets', 'LEGS', 'BEGINNER', 'CALVES', '/assets/exercises/calf-raises.jpg'),

-- CORE
('Plank', 'Planche abdominale', 'CORE', 'BEGINNER', 'CORE', '/assets/exercises/plank.jpg'),
('Side Plank', 'Planche latérale', 'CORE', 'INTERMEDIATE', 'CORE', '/assets/exercises/side-plank.jpg'),
('Leg Raises', 'Élévations de jambes', 'CORE', 'INTERMEDIATE', 'CORE', '/assets/exercises/leg-raises.jpg'),
('Crunches', 'Crunchs classiques', 'CORE', 'BEGINNER', 'ABS', '/assets/exercises/crunches.jpg'),
('Mountain Climbers', 'Mountain climbers', 'CORE', 'BEGINNER', 'CORE', '/assets/exercises/mountain-climbers.jpg'),
('Burpees', 'Burpees complets', 'CORE', 'INTERMEDIATE', 'FULL_BODY', '/assets/exercises/burpees.jpg');

-- ===================================================
-- VÉRIFICATION
-- ===================================================
SELECT 'Base de données créée avec succès !' AS message;
SELECT COUNT(*) AS nombre_exercices FROM exercises;
