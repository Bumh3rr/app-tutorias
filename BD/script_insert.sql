-- ═══════════════════════════════════════════════════════════════
--  app_tutorias — Script de datos de prueba
--  Generado para la estructura post-refactoring (Grupo / GrupoTutorado)
--  Hibernate crea las tablas automáticamente (ddl-auto=create-drop).
--  Ejecutar este script UNA VEZ después de que la aplicación haya
--  levantado y creado el esquema.
--
--  NOTA: fecha_registro es poblado automáticamente por @CreationTimestamp
--  cuando se usa Hibernate. Al ejecutar este script directamente en MySQL
--  se asignan timestamps históricos realistas. Los últimos registros de
--  cada tabla usan NOW() para demostrar el resaltado de "Registrado recientemente".
-- ═══════════════════════════════════════════════════════════════

USE app_tutorias;

-- ─────────────────────────────────────────────────────────────
--  1. CARRERAS  (id 1-5)
-- ─────────────────────────────────────────────────────────────
INSERT INTO carrera (nombre, clave, activo, fecha_registro) VALUES
('Ingeniería en Sistemas Computacionales', 'ISC',  1, '2025-11-15 09:00:00'),  -- 1
('Ingeniería en Gestión Empresarial',      'IGE',  1, '2025-11-15 09:05:00'),  -- 2
('Ingeniería Civil',                       'IC',   1, '2025-11-15 09:10:00'),  -- 3
('Contaduría',                             'CONT', 1, '2025-11-15 09:15:00'),  -- 4
('Informática',                            'INF',  1, NOW());                  -- 5 ← reciente

-- ─────────────────────────────────────────────────────────────
--  2. SEMESTRES  (id 1-3)
-- ─────────────────────────────────────────────────────────────
INSERT INTO semestre (periodo, anio, activo, fecha_registro) VALUES
('Enero-Junio',       2025, 1, '2024-11-20 10:00:00'),  -- 1
('Agosto-Diciembre',  2025, 1, '2025-06-10 11:00:00'),  -- 2
('Enero-Junio',       2026, 1, NOW());                   -- 3 ← reciente

-- ─────────────────────────────────────────────────────────────
--  3. TUTORES  (id 1-8)
-- ─────────────────────────────────────────────────────────────
INSERT INTO tutor (nombre, apellido, numero_control, email, foto, activo, fecha_registro) VALUES
('Carlos',   'Mendoza Rivas',    '22520434', 'carlos.mendoza@tecnm.mx',    NULL, 1, '2026-01-08 08:30:00'),  -- 1
('Patricia', 'Juárez López',     '22520826', 'patricia.juarez@tecnm.mx',   NULL, 1, '2026-01-08 08:35:00'),  -- 2
('Roberto',  'Castillo Fuentes', '22520827', 'roberto.castillo@tecnm.mx',  NULL, 1, '2026-01-08 08:40:00'),  -- 3
('Lorena',   'Pacheco Soto',     '22520927', 'lorena.pacheco@tecnm.mx',    NULL, 1, '2026-01-08 08:45:00'),  -- 4
('Miguel',   'Torres García',    '22520534', 'miguel.torres@tecnm.mx',     NULL, 1, '2026-01-08 09:00:00'),  -- 5
('Rosario',  'Vega Herrera',     '22520425', 'rosario.vega@tecnm.mx',      NULL, 1, '2026-01-08 09:05:00'),  -- 6
('Fernando', 'Alvarado Cruz',    '22520878', 'fernando.alvarado@tecnm.mx', NULL, 1, '2026-01-08 09:10:00'),  -- 7
('Claudia',  'Ramírez Peña',     '22520911', 'claudia.ramirez@tecnm.mx',   NULL, 1, NOW());                  -- 8 ← reciente

-- ─────────────────────────────────────────────────────────────
--  4. TUTORADOS  (id 1-21)
-- ─────────────────────────────────────────────────────────────
INSERT INTO tutorado (nombre, apellido, numero_control, email, foto, grado, id_carrera, activo, fecha_registro) VALUES
-- ISC
('Ana Laura',   'Reyes Morales',    '24400001', 'ana.reyes@tecnm.mx',        NULL, 1, 1, 1, '2026-01-13 10:00:00'),  -- 1
('Juan Pablo',  'Gómez Vargas',     '24400002', 'juan.gomez@tecnm.mx',       NULL, 1, 1, 1, '2026-01-13 10:02:00'),  -- 2
('María José',  'Hernández Ruiz',   '24400003', 'maria.hernandez@tecnm.mx',  NULL, 1, 1, 1, '2026-01-13 10:04:00'),  -- 3
('Luis Ángel',  'Flores Ortiz',     '24400004', 'luis.flores@tecnm.mx',      NULL, 1, 1, 1, '2026-01-13 10:06:00'),  -- 4
('Gabriela',    'Luna Paredes',     '24400005', 'gabriela.luna@tecnm.mx',    NULL, 1, 1, 1, '2026-01-13 10:08:00'),  -- 5
('Marco',       'Ávila Sánchez',    '24400006', 'marco.avila@tecnm.mx',      NULL, 1, 1, 1, '2026-01-13 10:10:00'),  -- 6
('Itzel',       'Bravo Castillo',   '24400007', 'itzel.bravo@tecnm.mx',      NULL, 1, 1, 1, '2026-01-13 10:12:00'),  -- 7
('Renata',      'Domínguez Peña',   '24400008', 'renata.dominguez@tecnm.mx', NULL, 1, 1, 1, '2026-01-13 10:14:00'),  -- 8
-- IGE
('Sofía',       'Castro Blanco',    '24400009', 'sofia.castro@tecnm.mx',     NULL, 1, 2, 1, '2026-01-13 10:20:00'),  -- 9
('Diego',       'Ríos Aguilar',     '24400010', 'diego.rios@tecnm.mx',       NULL, 1, 2, 1, '2026-01-13 10:22:00'),  -- 10
('Valeria',     'Mora Espinoza',    '24400011', 'valeria.mora@tecnm.mx',     NULL, 1, 2, 1, '2026-01-13 10:24:00'),  -- 11
-- IC
('Alejandro',   'Núñez Bravo',      '24400012', 'alejandro.nunez@tecnm.mx',  NULL, 1, 3, 1, '2026-01-13 10:30:00'),  -- 12
('Fernanda',    'Salinas Torres',   '24400013', 'fernanda.salinas@tecnm.mx', NULL, 1, 3, 1, '2026-01-13 10:32:00'),  -- 13
('Omar',        'Gutiérrez Lima',   '24400014', 'omar.gutierrez@tecnm.mx',   NULL, 1, 3, 1, '2026-01-13 10:34:00'),  -- 14
-- CONT
('Daniela',     'Vázquez Serna',    '24400015', 'daniela.vazquez@tecnm.mx',  NULL, 1, 4, 1, '2026-01-13 10:40:00'),  -- 15
('Ricardo',     'Peña Sandoval',    '24400016', 'ricardo.pena@tecnm.mx',     NULL, 1, 4, 1, '2026-01-13 10:42:00'),  -- 16
-- INF
('Paola',       'Estrada Ramos',    '24400017', 'paola.estrada@tecnm.mx',    NULL, 1, 5, 1, '2026-01-13 10:50:00'),  -- 17
('Iván',        'Cervantes Díaz',   '24400018', 'ivan.cervantes@tecnm.mx',   NULL, 1, 5, 1, '2026-01-13 10:52:00'),  -- 18
-- Otros semestres / carreras
('Jorge',       'Medina Campos',    '24100001', 'jorge.medina@tecnm.mx',     NULL, 2, 1, 1, '2025-08-01 09:00:00'),  -- 19
('Karla',       'Soto Villanueva',  '24100002', 'karla.soto@tecnm.mx',       NULL, 2, 2, 1, '2025-08-01 09:05:00'),  -- 20
('Emmanuel',    'Cruz Mendoza',     '24100003', 'emmanuel.cruz@tecnm.mx',    NULL, 2, 3, 1, NOW());                   -- 21 ← reciente

-- ─────────────────────────────────────────────────────────────
--  5. COORDINADORES DE CARRERA
-- ─────────────────────────────────────────────────────────────
INSERT INTO coordinador_carrera (nombre, apellido, numero_control, email, foto, cargo, id_carrera, id_semestre, activo, fecha_registro) VALUES
('Pacheco',  'Ruiz Sandoval',    'COORD-001', 'pacheco.ruiz@tecnm.mx',    NULL, 'Coordinador de Tutoría ISC',               1, 3, 1, '2026-01-09 08:00:00'),
('Rosario',  'Vega Herrera',     'COORD-002', 'rosario.coord@tecnm.mx',   NULL, 'Coordinadora de Tutoría IGE y Contaduría', 2, 3, 1, '2026-01-09 08:10:00'),
('Casildo',  'Méndez Torres',    'COORD-003', 'casildo.mendez@tecnm.mx',  NULL, 'Coordinador de Tutoría IC',                3, 3, 1, '2026-01-09 08:20:00'),
('Lucía',    'Flores Aguilar',   'COORD-004', 'lucia.flores@tecnm.mx',    NULL, 'Coordinadora de Tutoría Informática',      5, 3, 1, '2026-01-09 08:30:00'),
('Roberto',  'Castillo Fuentes', 'COORD-005', 'roberto.coord@tecnm.mx',   NULL, 'Coordinador de Tutoría IGE',               2, 2, 1, NOW());

-- ─────────────────────────────────────────────────────────────
--  6. PAT  (id 1-5)
-- ─────────────────────────────────────────────────────────────
INSERT INTO pat (nombre, descripcion, foto, id_semestre, id_carrera, es_general, activo, fecha_registro) VALUES
('PAT General Enero-Junio 2026',
 'Plan de Acción Tutorial institucional para el semestre Enero-Junio 2026. Aplica a todas las carreras del tecnológico.',
 NULL, 3, NULL, 1, 1, '2026-01-10 10:00:00'),                                                                               -- 1
('PAT ISC Enero-Junio 2026',
 'Plan adaptado para ISC. Incluye actividades de nivelación en álgebra y cálculo diferencial.',
 NULL, 3, 1, 0, 1, '2026-01-10 10:15:00'),                                                                                  -- 2
('PAT IGE Enero-Junio 2026',
 'Plan adaptado para IGE. Incluye introducción al derecho empresarial y fundamentos de contabilidad.',
 NULL, 3, 2, 0, 1, '2026-01-10 10:30:00'),                                                                                  -- 3
('PAT IC Enero-Junio 2026',
 'Plan adaptado para IC. Incluye nivelación en cálculo diferencial y geometría descriptiva.',
 NULL, 3, 3, 0, 1, '2026-01-10 10:45:00'),                                                                                  -- 4
('PAT General Agosto-Diciembre 2025',
 'Plan de Acción Tutorial institucional para el semestre Agosto-Diciembre 2025.',
 NULL, 2, NULL, 1, 1, '2025-06-15 09:00:00');                                                                               -- 5

-- ─────────────────────────────────────────────────────────────
--  7. ACTIVIDADES  (id 1-16)
-- ─────────────────────────────────────────────────────────────
INSERT INTO actividad (nombre, descripcion, fecha, semana, foto, id_pat, activo, fecha_registro) VALUES
-- PAT General Ene-Jun 2026 (id_pat=1) — 10 sesiones
('Bienvenida e Inducción',             'Presentación del programa, reglas y expectativas del semestre.',                                   '2026-01-23', 1,  NULL, 1, 1, '2026-01-12 09:00:00'),  -- 1
('Detección de Necesidades',           'Aplicación del instrumento de detección de necesidades académicas, económicas y psicológicas.',     '2026-01-30', 2,  NULL, 1, 1, '2026-01-12 09:05:00'),  -- 2
('Técnicas de Estudio',                'Taller sobre estrategias y técnicas de estudio efectivas para nivel superior.',                     '2026-02-06', 3,  NULL, 1, 1, '2026-01-12 09:10:00'),  -- 3
('Orientación Vocacional',             'Sesión sobre el perfil de egreso y campo laboral de cada carrera.',                                 '2026-02-13', 4,  NULL, 1, 1, '2026-01-12 09:15:00'),  -- 4
('Seguimiento Académico',              'Revisión del desempeño del alumno en sus materias del semestre.',                                   '2026-02-20', 5,  NULL, 1, 1, '2026-01-12 09:20:00'),  -- 5
('Manejo del Estrés',                  'Taller de desarrollo humano: manejo del estrés y ansiedad escolar.',                               '2026-02-27', 6,  NULL, 1, 1, '2026-01-12 09:25:00'),  -- 6
('Habilidades Socioemocionales',       'Inteligencia emocional y habilidades interpersonales.',                                            '2026-03-06', 7,  NULL, 1, 1, '2026-01-12 09:30:00'),  -- 7
('Asesorías Académicas',               'Canalización de alumnos con bajo rendimiento a asesorías en materias críticas.',                   '2026-03-13', 8,  NULL, 1, 1, '2026-01-12 09:35:00'),  -- 8
('Proyecto de Vida',                   'Taller sobre metas personales a corto, mediano y largo plazo.',                                    '2026-03-20', 9,  NULL, 1, 1, '2026-01-12 09:40:00'),  -- 9
('Cierre y Evaluación',                'Cierre del programa. Evaluación de satisfacción y entrega de evidencias.',                         '2026-03-27', 10, NULL, 1, 1, '2026-01-12 09:45:00'),  -- 10
-- PAT ISC (id_pat=2)
('Nivelación Álgebra',                 'Identificación de alumnos con deficiencias en álgebra y canalización a asesorías.',                '2026-02-06', 3,  NULL, 2, 1, '2026-01-12 10:00:00'),  -- 11
('Nivelación Cálculo Diferencial',     'Identificación y canalización de alumnos con deficiencias en cálculo diferencial.',                '2026-02-13', 4,  NULL, 2, 1, '2026-01-12 10:05:00'),  -- 12
-- PAT IGE (id_pat=3)
('Introducción al Derecho Empresarial','Nivelación en conceptos básicos de derecho para alumnos de IGE.',                                  '2026-02-06', 3,  NULL, 3, 1, '2026-01-12 10:10:00'),  -- 13
('Fundamentos de Contabilidad',        'Apoyo en conceptos básicos de contabilidad para alumnos de nuevo ingreso.',                        '2026-02-13', 4,  NULL, 3, 1, '2026-01-12 10:15:00'),  -- 14
-- PAT General Ago-Dic 2025 (id_pat=5)
('Bienvenida Agosto-Diciembre 2025',   'Sesión de bienvenida e inducción al programa, semestre Ago-Dic 2025.',                             '2025-08-22', 1,  NULL, 5, 1, '2025-06-16 09:00:00'),  -- 15
('Detección de Necesidades Ago-Dic',   'Aplicación del instrumento de detección de necesidades, semestre Ago-Dic 2025.',                   '2025-08-29', 2,  NULL, 5, 1, NOW());                   -- 16 ← reciente

-- ─────────────────────────────────────────────────────────────
--  8. GRUPOS  (id 1-8)
-- ─────────────────────────────────────────────────────────────
INSERT INTO grupo (nombre, id_tutor, id_semestre, id_carrera, aula, dia_semana, horario, activo, fecha_registro) VALUES
-- Semestre Enero-Junio 2026
('Grupo A — ISC',  1, 3, 1, 'R-101', 'Viernes', '08:00-09:00', 1, '2026-01-15 08:00:00'),  -- 1
('Grupo B — ISC',  4, 3, 1, 'R-202', 'Viernes', '09:00-10:00', 1, '2026-01-15 08:10:00'),  -- 2
('Grupo A — IGE',  2, 3, 2, 'R-203', 'Viernes', '10:00-11:00', 1, '2026-01-15 08:20:00'),  -- 3
('Grupo A — IC',   3, 3, 3, 'R-105', 'Viernes', '08:00-09:00', 1, '2026-01-15 08:30:00'),  -- 4
('Grupo A — CONT', 5, 3, 4, 'R-301', 'Viernes', '11:00-12:00', 1, '2026-01-15 08:40:00'),  -- 5
('Grupo A — INF',  6, 3, 5, 'R-104', 'Viernes', '09:00-10:00', 1, '2026-01-15 08:50:00'),  -- 6
-- Semestre Agosto-Diciembre 2025
('Grupo A — ISC Ago-Dic', 7, 2, 1, 'V-101', 'Viernes', '08:00-09:00', 1, '2025-08-05 08:00:00'),  -- 7
('Grupo A — IC Ago-Dic',  8, 2, 3, 'V-103', 'Viernes', '08:00-09:00', 1, NOW());                   -- 8 ← reciente

-- ─────────────────────────────────────────────────────────────
--  9. GRUPO_TUTORADO  — sin fecha_registro (tabla de relación)
-- ─────────────────────────────────────────────────────────────
INSERT INTO grupo_tutorado (id_grupo, id_tutorado, activo) VALUES
(1, 1,  1), (1, 2,  1), (1, 3,  1), (1, 4,  1), (1, 5,  1),
(2, 6,  1), (2, 7,  1), (2, 8,  1),
(3, 9,  1), (3, 10, 1), (3, 11, 1),
(4, 12, 1), (4, 13, 1), (4, 14, 1),
(5, 15, 1), (5, 16, 1),
(6, 17, 1), (6, 18, 1),
(7, 19, 1),
(8, 21, 1);

-- ─────────────────────────────────────────────────────────────
-- 10. SESIONES  (id 1-21)
-- ─────────────────────────────────────────────────────────────
INSERT INTO sesion (id_grupo, id_actividad, semana, fecha_imparticion, estatus_registro, activo, fecha_registro) VALUES
-- Grupo 1 — Carlos / ISC — semanas 1 a 10
(1, 1,  1,  '2026-01-23', 'REALIZADA', 1, '2026-01-23 08:00:00'),   -- 1
(1, 2,  2,  '2026-01-30', 'REALIZADA', 1, '2026-01-30 08:00:00'),   -- 2
(1, 3,  3,  '2026-02-06', 'REALIZADA', 1, '2026-02-06 08:00:00'),   -- 3
(1, 4,  4,  '2026-02-13', 'REALIZADA', 1, '2026-02-13 08:00:00'),   -- 4
(1, 5,  5,  '2026-02-20', 'REALIZADA', 1, '2026-02-20 08:00:00'),   -- 5
(1, 6,  6,  '2026-02-27', 'PENDIENTE', 1, '2026-02-27 08:00:00'),   -- 6
(1, 7,  7,  '2026-03-06', 'PENDIENTE', 1, '2026-03-06 08:00:00'),   -- 7
(1, 8,  8,  '2026-03-13', 'PENDIENTE', 1, '2026-03-13 08:00:00'),   -- 8
(1, 9,  9,  '2026-03-20', 'PENDIENTE', 1, '2026-03-20 08:00:00'),   -- 9
(1, 10, 10, '2026-03-27', 'PENDIENTE', 1, '2026-03-27 08:00:00'),   -- 10
-- Grupo 3 — Patricia / IGE — semanas 1 a 5
(3, 1,  1,  '2026-01-23', 'REALIZADA', 1, '2026-01-23 10:00:00'),   -- 11
(3, 2,  2,  '2026-01-30', 'REALIZADA', 1, '2026-01-30 10:00:00'),   -- 12
(3, 3,  3,  '2026-02-06', 'REALIZADA', 1, '2026-02-06 10:00:00'),   -- 13
(3, 4,  4,  '2026-02-13', 'PENDIENTE', 1, '2026-02-13 10:00:00'),   -- 14
(3, 5,  5,  '2026-02-20', 'PENDIENTE', 1, '2026-02-20 10:00:00'),   -- 15
-- Grupo 4 — Roberto / IC — semanas 1 a 3
(4, 1,  1,  '2026-01-23', 'REALIZADA', 1, '2026-01-23 08:00:00'),   -- 16
(4, 2,  2,  '2026-01-30', 'REALIZADA', 1, '2026-01-30 08:00:00'),   -- 17
(4, 3,  3,  '2026-02-06', 'PENDIENTE', 1, '2026-02-06 08:00:00'),   -- 18
-- Grupo 7 — Fernando / ISC Ago-Dic 2025
(7, 15, 1,  '2025-08-22', 'REALIZADA', 1, '2025-08-22 08:00:00'),   -- 19
(7, 16, 2,  '2025-08-29', 'REALIZADA', 1, '2025-08-29 08:00:00'),   -- 20
-- Grupo 8 — Claudia / IC Ago-Dic 2025
(8, 15, 1,  '2025-08-22', 'REALIZADA', 1, NOW());                    -- 21 ← reciente

-- ─────────────────────────────────────────────────────────────
-- 11. ASISTENCIAS
-- ─────────────────────────────────────────────────────────────

INSERT INTO asistencia (id_sesion, id_tutorado, presente, recuperada, activo, fecha_registro) VALUES
(1, 1, 1, 0, 1, '2026-01-23 09:00:00'),
(1, 2, 1, 0, 1, '2026-01-23 09:01:00'),
(1, 3, 1, 0, 1, '2026-01-23 09:02:00'),
(1, 4, 1, 0, 1, '2026-01-23 09:03:00'),
(1, 5, 1, 0, 1, '2026-01-23 09:04:00');

INSERT INTO asistencia (id_sesion, id_tutorado, presente, recuperada, activo, fecha_registro) VALUES
(2, 1, 1, 0, 1, '2026-01-30 09:00:00'),
(2, 2, 0, 0, 1, '2026-01-30 09:01:00'),
(2, 3, 1, 0, 1, '2026-01-30 09:02:00'),
(2, 4, 1, 0, 1, '2026-01-30 09:03:00'),
(2, 5, 1, 0, 1, '2026-01-30 09:04:00');

INSERT INTO asistencia (id_sesion, id_tutorado, presente, recuperada, activo, fecha_registro) VALUES
(3, 1, 1, 0, 1, '2026-02-06 09:00:00'),
(3, 2, 0, 1, 1, '2026-02-06 09:01:00'),
(3, 3, 0, 0, 1, '2026-02-06 09:02:00'),
(3, 4, 1, 0, 1, '2026-02-06 09:03:00'),
(3, 5, 1, 0, 1, '2026-02-06 09:04:00');

INSERT INTO asistencia (id_sesion, id_tutorado, presente, recuperada, activo, fecha_registro) VALUES
(4, 1, 1, 0, 1, '2026-02-13 09:00:00'),
(4, 2, 1, 0, 1, '2026-02-13 09:01:00'),
(4, 3, 1, 0, 1, '2026-02-13 09:02:00'),
(4, 4, 1, 0, 1, '2026-02-13 09:03:00'),
(4, 5, 1, 0, 1, '2026-02-13 09:04:00');

INSERT INTO asistencia (id_sesion, id_tutorado, presente, recuperada, activo, fecha_registro) VALUES
(5, 1, 1, 0, 1, '2026-02-20 09:00:00'),
(5, 2, 1, 0, 1, '2026-02-20 09:01:00'),
(5, 3, 1, 0, 1, '2026-02-20 09:02:00'),
(5, 4, 0, 0, 1, '2026-02-20 09:03:00'),
(5, 5, 1, 0, 1, '2026-02-20 09:04:00');

INSERT INTO asistencia (id_sesion, id_tutorado, presente, recuperada, activo, fecha_registro) VALUES
(11,  9, 1, 0, 1, '2026-01-23 10:50:00'),
(11, 10, 1, 0, 1, '2026-01-23 10:51:00'),
(11, 11, 1, 0, 1, '2026-01-23 10:52:00');

INSERT INTO asistencia (id_sesion, id_tutorado, presente, recuperada, activo, fecha_registro) VALUES
(12,  9, 1, 0, 1, '2026-01-30 10:50:00'),
(12, 10, 0, 0, 1, '2026-01-30 10:51:00'),
(12, 11, 1, 0, 1, '2026-01-30 10:52:00');

INSERT INTO asistencia (id_sesion, id_tutorado, presente, recuperada, activo, fecha_registro) VALUES
(13,  9, 1, 0, 1, '2026-02-06 10:50:00'),
(13, 10, 1, 0, 1, '2026-02-06 10:51:00'),
(13, 11, 1, 0, 1, '2026-02-06 10:52:00');

INSERT INTO asistencia (id_sesion, id_tutorado, presente, recuperada, activo, fecha_registro) VALUES
(16, 12, 1, 0, 1, '2026-01-23 09:50:00'),
(16, 13, 1, 0, 1, '2026-01-23 09:51:00'),
(16, 14, 1, 0, 1, '2026-01-23 09:52:00');

INSERT INTO asistencia (id_sesion, id_tutorado, presente, recuperada, activo, fecha_registro) VALUES
(17, 12, 1, 0, 1, '2026-01-30 09:50:00'),
(17, 13, 0, 1, 1, '2026-01-30 09:51:00'),
(17, 14, 1, 0, 1, '2026-01-30 09:52:00');

INSERT INTO asistencia (id_sesion, id_tutorado, presente, recuperada, activo, fecha_registro) VALUES
(19, 19, 1, 0, 1, '2025-08-22 09:00:00');

INSERT INTO asistencia (id_sesion, id_tutorado, presente, recuperada, activo, fecha_registro) VALUES
(20, 19, 0, 0, 1, '2025-08-29 09:00:00');

INSERT INTO asistencia (id_sesion, id_tutorado, presente, recuperada, activo, fecha_registro) VALUES
(21, 21, 1, 0, 1, NOW());   -- ← reciente

-- ─────────────────────────────────────────────────────────────
-- 12. EVIDENCIAS DE SESIÓN
-- ─────────────────────────────────────────────────────────────
INSERT INTO evidencia_sesion (id_sesion, archivo_url, notas_coordinador, estatus_validacion, fecha_subida, activo, fecha_registro) VALUES
(1,  NULL, 'Sesión realizada sin inconvenientes.',          'VALIDADA',  '2026-01-23', 1, '2026-01-23 10:00:00'),
(2,  NULL, 'Se registró una inasistencia.',                 'VALIDADA',  '2026-01-30', 1, '2026-01-30 10:00:00'),
(3,  NULL, NULL,                                            'PENDIENTE', '2026-02-06', 1, '2026-02-06 10:00:00'),
(4,  NULL, 'Evidencia aprobada.',                           'VALIDADA',  '2026-02-13', 1, '2026-02-13 10:00:00'),
(5,  NULL, 'Sesión realizada correctamente.',               'VALIDADA',  '2026-02-20', 1, '2026-02-20 10:00:00'),
(11, NULL, 'Sesión introductoria de IGE completada.',       'VALIDADA',  '2026-01-23', 1, '2026-01-23 11:00:00'),
(12, NULL, NULL,                                            'PENDIENTE', '2026-01-30', 1, '2026-01-30 11:00:00'),
(16, NULL, 'Primera sesión de IC completada.',              'VALIDADA',  '2026-01-23', 1, '2026-01-23 10:00:00'),
(19, NULL, 'Sesión del semestre Ago-Dic 2025 registrada.',  'VALIDADA',  '2025-08-22', 1, NOW());   -- ← reciente

-- ─────────────────────────────────────────────────────────────
-- 13. DETECCIÓN DE NECESIDADES
-- ─────────────────────────────────────────────────────────────
INSERT INTO deteccion_necesidades
  (id_tutorado, id_sesion, necesidad_algebra, necesidad_calculo, necesidad_derecho,
   necesidad_otra, necesidad_economica, necesidad_psicologica, observaciones, activo, fecha_registro)
VALUES
(2, 2, 1, 1, 0, NULL, 0, 0, 'Alumno con dificultades en álgebra y cálculo. Se canalizó a asesorías.', 1, '2026-01-30 11:30:00'),
(3, 2, 0, 1, 0, NULL, 1, 0, 'Dificultad en cálculo diferencial. Situación económica a monitorear.', 1, '2026-01-30 11:35:00'),
(4, 2, 0, 0, 0, NULL, 0, 1, 'Alumno presenta signos de estrés académico. Se recomendó psicólogo.', 1, NOW());   -- ← reciente

-- ─────────────────────────────────────────────────────────────
-- 14. REPORTES DE SESIÓN
-- ─────────────────────────────────────────────────────────────
INSERT INTO reporte_sesion
  (id_sesion, descripcion_actividad, observaciones, alumnos_presentes, fecha_entrega, estatus_revision, activo, fecha_registro)
VALUES
(1,  'Se presentó el programa de tutorías, se firmaron las listas de asistencia y se establecieron los horarios de atención individual.',
     'Todos los alumnos asistieron. Buena disposición del grupo.',                                     5, '2026-01-23', 'APROBADO',  1, '2026-01-23 12:00:00'),
(2,  'Se aplicó el instrumento de detección de necesidades. Se identificaron tres alumnos con necesidades académicas y/o personales.',
     'Se detectaron necesidades de apoyo en cálculo y situación económica en dos alumnos.',            5, '2026-01-30', 'APROBADO',  1, '2026-01-30 12:00:00'),
(3,  'Taller de técnicas de estudio: mapas mentales, método Cornell y técnica Pomodoro.',
     'Alta participación del grupo. Se distribuyó material impreso.',                                  4, '2026-02-06', 'PENDIENTE', 1, '2026-02-06 12:00:00'),
(11, 'Sesión de bienvenida del grupo IGE. Presentación del plan tutorial y actividades del semestre.',
     'Participación del 100%. Grupo muy motivado.',                                                    3, '2026-01-23', 'APROBADO',  1, NOW());   -- ← reciente
