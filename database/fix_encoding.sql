-- ===================================================
-- CORRECTION DES ACCENTS DANS LA TABLE EXERCISES
-- ===================================================
-- À exécuter dans pgAdmin Query Tool

UPDATE exercises SET description = 'Pompes pike pour les épaules' WHERE id = 3;
UPDATE exercises SET description = 'Dips sur barres parallèles' WHERE id = 4;
UPDATE exercises SET description = 'Pompes en équilibre sur les mains' WHERE id = 5;
UPDATE exercises SET description = 'Squats sautés' WHERE id = 11;
UPDATE exercises SET description = 'Élévations mollets' WHERE id = 14;
UPDATE exercises SET description = 'Planche latérale' WHERE id = 16;
UPDATE exercises SET description = 'Élévations de jambes' WHERE id = 17;

-- Vérification
SELECT id, name, description FROM exercises WHERE id IN (3, 4, 5, 11, 14, 16, 17);
