-- ═══════════════════════════════════════════════════════════════
--  app_tutorias — Datos enfocados: Enero-Junio 2026
--  Carreras: ISC (id=1) y Civil (id=3)
--  2 grupos por carrera = 4 grupos en total
--
--  PRERREQUISITO: ejecutar script_insert.sql primero.
--  Este script asume los IDs generados por ese script:
--    - Semestre Enero-Junio 2026 → id = 3
--    - Carrera ISC              → id = 1
--    - Carrera IC (Civil)       → id = 3
--    - PAT General Ene-Jun 2026 → id = 1
--    - PAT ISC Ene-Jun 2026     → id = 2
--    - PAT IC Ene-Jun 2026      → id = 4
--    - Últimos tutores          → id = 8
--    - Últimos tutorados        → id = 21
--    - Últimos grupos           → id = 8
--
--  NOTA IMPORTANTE: los grupos creados aquí NO tienen sesiones,
--  lo que permite probar la funcionalidad de generación masiva
--  desde el panel de administración.
-- ═══════════════════════════════════════════════════════════════

USE app_tutorias;

-- ─────────────────────────────────────────────────────────────
--  1. TUTORES NUEVOS  (id 9-12)
-- ─────────────────────────────────────────────────────────────
INSERT INTO tutor (nombre, apellido, numero_control, email, foto, activo, fecha_registro) VALUES
('Arturo',   'Vidal Romero',     '22521001', 'arturo.vidal@tecnm.mx',     NULL, 1, '2026-01-08 09:20:00'),  -- 9  → Grupo C ISC
('Beatriz',  'Salinas Mora',     '22521002', 'beatriz.salinas@tecnm.mx',  NULL, 1, '2026-01-08 09:25:00'),  -- 10 → Grupo D ISC
('Eduardo',  'Garza Contreras',  '22521003', 'eduardo.garza@tecnm.mx',    NULL, 1, '2026-01-08 09:30:00'),  -- 11 → Grupo B IC
('Silvia',   'Pedraza Luna',     '22521004', 'silvia.pedraza@tecnm.mx',   NULL, 1, NOW());                  -- 12 → Grupo C IC

-- ─────────────────────────────────────────────────────────────
--  2. TUTORADOS  (id 22-49)
--     7 alumnos por grupo, todos de nuevo ingreso (grado=1)
-- ─────────────────────────────────────────────────────────────
INSERT INTO tutorado (nombre, apellido, numero_control, email, foto, grado, id_carrera, activo, fecha_registro) VALUES
-- ── ISC — Grupo C (ids 22-28) ──────────────────────────────
('Ximena',     'Contreras Lara',    '26400001', 'ximena.contreras@tecnm.mx',    NULL, 1, 1, 1, '2026-01-13 11:00:00'),  -- 22
('Alberto',    'Medrano Vela',      '26400002', 'alberto.medrano@tecnm.mx',     NULL, 1, 1, 1, '2026-01-13 11:02:00'),  -- 23
('Verónica',   'Ibarra Solís',      '26400003', 'veronica.ibarra@tecnm.mx',     NULL, 1, 1, 1, '2026-01-13 11:04:00'),  -- 24
('Santiago',   'Quiroga Ruiz',      '26400004', 'santiago.quiroga@tecnm.mx',    NULL, 1, 1, 1, '2026-01-13 11:06:00'),  -- 25
('Adriana',    'Ponce Guzmán',      '26400005', 'adriana.ponce@tecnm.mx',       NULL, 1, 1, 1, '2026-01-13 11:08:00'),  -- 26
('Hugo',       'Cisneros Álvarez',  '26400006', 'hugo.cisneros@tecnm.mx',       NULL, 1, 1, 1, '2026-01-13 11:10:00'),  -- 27
('Natalia',    'Roldán Torres',     '26400007', 'natalia.roldan@tecnm.mx',      NULL, 1, 1, 1, '2026-01-13 11:12:00'),  -- 28
-- ── ISC — Grupo D (ids 29-35) ──────────────────────────────
('Alexis',     'Moreno Reyes',      '26400008', 'alexis.moreno@tecnm.mx',       NULL, 1, 1, 1, '2026-01-13 11:20:00'),  -- 29
('Camila',     'Espinosa Durán',    '26400009', 'camila.espinosa@tecnm.mx',     NULL, 1, 1, 1, '2026-01-13 11:22:00'),  -- 30
('David',      'Velázquez Reyna',   '26400010', 'david.velazquez@tecnm.mx',     NULL, 1, 1, 1, '2026-01-13 11:24:00'),  -- 31
('Estefanía',  'Córdoba Montes',    '26400011', 'estefania.cordoba@tecnm.mx',   NULL, 1, 1, 1, '2026-01-13 11:26:00'),  -- 32
('Germán',     'Fuentes Delgado',   '26400012', 'german.fuentes@tecnm.mx',      NULL, 1, 1, 1, '2026-01-13 11:28:00'),  -- 33
('Isabela',    'Rojas Cano',        '26400013', 'isabela.rojas@tecnm.mx',       NULL, 1, 1, 1, '2026-01-13 11:30:00'),  -- 34
('Joel',       'Tapia Núñez',       '26400014', 'joel.tapia@tecnm.mx',          NULL, 1, 1, 1, '2026-01-13 11:32:00'),  -- 35
-- ── IC (Civil) — Grupo B (ids 36-42) ───────────────────────
('Kevin',      'Olvera Jiménez',    '26400015', 'kevin.olvera@tecnm.mx',        NULL, 1, 3, 1, '2026-01-13 11:40:00'),  -- 36
('Laura',      'Sandoval Vega',     '26400016', 'laura.sandoval@tecnm.mx',      NULL, 1, 3, 1, '2026-01-13 11:42:00'),  -- 37
('Mauricio',   'Trujillo Campos',   '26400017', 'mauricio.trujillo@tecnm.mx',   NULL, 1, 3, 1, '2026-01-13 11:44:00'),  -- 38
('Norma',      'Gutiérrez Peña',    '26400018', 'norma.gutierrez@tecnm.mx',     NULL, 1, 3, 1, '2026-01-13 11:46:00'),  -- 39
('Óscar',      'Serrano Flores',    '26400019', 'oscar.serrano@tecnm.mx',       NULL, 1, 3, 1, '2026-01-13 11:48:00'),  -- 40
('Pamela',     'Acosta Herrera',    '26400020', 'pamela.acosta@tecnm.mx',       NULL, 1, 3, 1, '2026-01-13 11:50:00'),  -- 41
('Rafael',     'Carrillo Ríos',     '26400021', 'rafael.carrillo@tecnm.mx',     NULL, 1, 3, 1, '2026-01-13 11:52:00'),  -- 42
-- ── IC (Civil) — Grupo C (ids 43-49) ───────────────────────
('Sandra',     'Molina Díaz',       '26400022', 'sandra.molina@tecnm.mx',       NULL, 1, 3, 1, '2026-01-13 12:00:00'),  -- 43
('Tomás',      'Becerra Ortega',    '26400023', 'tomas.becerra@tecnm.mx',       NULL, 1, 3, 1, '2026-01-13 12:02:00'),  -- 44
('Úrsula',     'Vargas León',       '26400024', 'ursula.vargas@tecnm.mx',       NULL, 1, 3, 1, '2026-01-13 12:04:00'),  -- 45
('Vicente',    'Guerrero Mata',     '26400025', 'vicente.guerrero@tecnm.mx',    NULL, 1, 3, 1, '2026-01-13 12:06:00'),  -- 46
('Wendy',      'Alejo Soto',        '26400026', 'wendy.alejo@tecnm.mx',         NULL, 1, 3, 1, '2026-01-13 12:08:00'),  -- 47
('Xavier',     'Domínguez Cruz',    '26400027', 'xavier.dominguez@tecnm.mx',    NULL, 1, 3, 1, '2026-01-13 12:10:00'),  -- 48
('Yolanda',    'Espinoza Ramos',    '26400028', 'yolanda.espinoza@tecnm.mx',    NULL, 1, 3, 1, NOW());                  -- 49 ← reciente

-- ─────────────────────────────────────────────────────────────
--  3. GRUPOS  (id 9-12)
--     Días variados para ejercitar la detección de DIA_SEMANA
--     en el modal de generación masiva.
--     Ningún grupo tiene sesiones → aptos para generar.
-- ─────────────────────────────────────────────────────────────
INSERT INTO grupo (nombre, id_tutor, id_semestre, id_carrera, aula, dia_semana, horario, activo, fecha_registro) VALUES
--  ISC
('Grupo C — ISC',  9,  3, 1, 'A-201', 'Lunes',     '07:00-08:00', 1, '2026-01-15 09:00:00'),  -- 9
('Grupo D — ISC',  10, 3, 1, 'A-202', 'Miércoles', '10:00-11:00', 1, '2026-01-15 09:10:00'),  -- 10
--  IC (Civil)
('Grupo B — IC',   11, 3, 3, 'B-101', 'Martes',    '08:00-09:00', 1, '2026-01-15 09:20:00'),  -- 11
('Grupo C — IC',   12, 3, 3, 'B-102', 'Jueves',    '09:00-10:00', 1, NOW());                   -- 12 ← reciente

-- ─────────────────────────────────────────────────────────────
--  4. GRUPO_TUTORADO
-- ─────────────────────────────────────────────────────────────
INSERT INTO grupo_tutorado (id_grupo, id_tutorado, activo) VALUES
-- Grupo C — ISC (id=9) → tutorados 22-28
(9,  22, 1), (9,  23, 1), (9,  24, 1), (9,  25, 1),
(9,  26, 1), (9,  27, 1), (9,  28, 1),
-- Grupo D — ISC (id=10) → tutorados 29-35
(10, 29, 1), (10, 30, 1), (10, 31, 1), (10, 32, 1),
(10, 33, 1), (10, 34, 1), (10, 35, 1),
-- Grupo B — IC (id=11) → tutorados 36-42
(11, 36, 1), (11, 37, 1), (11, 38, 1), (11, 39, 1),
(11, 40, 1), (11, 41, 1), (11, 42, 1),
-- Grupo C — IC (id=12) → tutorados 43-49
(12, 43, 1), (12, 44, 1), (12, 45, 1), (12, 46, 1),
(12, 47, 1), (12, 48, 1), (12, 49, 1);

-- ─────────────────────────────────────────────────────────────
--  5. SESIONES
--     NINGUNA — los 4 grupos están listos para que el admin
--     use la funcionalidad "Generar 10 sesiones" desde el
--     panel de cada grupo.
--
--     PATs disponibles para generación:
--       Grupo C ISC → PAT ISC (id=2) + PAT General (id=1)
--       Grupo D ISC → PAT ISC (id=2) + PAT General (id=1)
--       Grupo B IC  → PAT IC  (id=4) + PAT General (id=1)
--       Grupo C IC  → PAT IC  (id=4) + PAT General (id=1)
-- ─────────────────────────────────────────────────────────────
-- (sin datos intencional)

-- ─────────────────────────────────────────────────────────────
--  Resumen de IDs después de ejecutar ambos scripts
-- ─────────────────────────────────────────────────────────────
--  Tutores:   1-12
--  Tutorados: 1-49
--  Grupos:    1-12  (9-12 sin sesiones)
--  Sesiones:  1-21  (grupos 9-12 vacíos)
-- ─────────────────────────────────────────────────────────────
