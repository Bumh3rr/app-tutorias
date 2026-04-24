CREATE DATABASE IF NOT EXISTS app_tutorias;
USE app_tutorias;

-- ── Carreras ──────────────────────────────────
INSERT INTO carrera (nombre, clave, activo) VALUES
('Ingeniería en Sistemas Computacionales', 'ISC', 1),
('Ingeniería en Gestión Empresarial', 'IGE', 1),
('Ingeniería Civil', 'IC', 1),
('Contaduría', 'CONT', 1),
('Informática', 'INF', 1);

-- ── Semestres ──────────────────────────────────
INSERT INTO semestre (periodo, anio, activo) VALUES
('Enero-Junio', 2025, 1),
('Agosto-Diciembre', 2025, 1),
('Enero-Junio', 2026, 1);

-- ── Coordinadores de Carrera ───────────────────
INSERT INTO coordinador_carrera (nombre, apellido, numero_control, email, foto, cargo, id_carrera, id_semestre, activo) VALUES
('Rosario',  'Vega Herrera',    'COORD-001', 'rosario.coord@tecnm.mx',  NULL, 'Coordinadora de Tutoría IGE y Contaduría',  2, 3, 1),
('Casildo',  'Méndez Torres',   'COORD-002', 'casildo.mendez@tecnm.mx', NULL, 'Coordinador de Tutoría IC',                 3, 3, 1),
('Pacheco',  'Ruiz Sandoval',   'COORD-003', 'pacheco.ruiz@tecnm.mx',   NULL, 'Coordinador de Tutoría ISC',                1, 3, 1),
('Lucía',    'Flores Aguilar',  'COORD-004', 'lucia.flores@tecnm.mx',   NULL, 'Coordinadora de Tutoría Informática',       5, 3, 1),
('Roberto',  'Castillo Fuentes','COORD-005', 'roberto.coord@tecnm.mx',  NULL, 'Coordinador de Tutoría IGE',                2, 2, 1);

-- ── Tutores ────────────────────────────────────
INSERT INTO tutor (nombre, apellido, numero_control, email, foto, aula, dia_semana, horario, id_carrera, id_semestre, activo) VALUES
('Carlos',   'Mendoza Rivas',    '22520434', 'carlos.mendoza@tecnm.mx',    NULL, 'R-101', 'Viernes', '08:00-09:00', 1, 3, 1),
('Patricia', 'Juárez López',     '22520826', 'patricia.juarez@tecnm.mx',   NULL, 'R-203', 'Viernes', '09:00-10:00', 2, 3, 1),
('Roberto',  'Castillo Fuentes', '22520827', 'roberto.castillo@tecnm.mx',  NULL, 'R-105', 'Viernes', '10:00-11:00', 3, 3, 1),
('Lorena',   'Pacheco Soto',     '22520927', 'lorena.pacheco@tecnm.mx',    NULL, 'R-202', 'Viernes', '08:00-09:00', 1, 3, 1),
('Miguel',   'Torres García',    '22520534', 'miguel.torres@tecnm.mx',     NULL, 'R-301', 'Viernes', '11:00-12:00', 4, 3, 1),
('Rosario',  'Vega Herrera',     '22520425', 'rosario.vega@tecnm.mx',      NULL, 'R-104', 'Viernes', '09:00-10:00', 5, 3, 1),
('Fernando', 'Alvarado Cruz',    '22520878', 'fernando.alvarado@tecnm.mx', NULL, 'R-201', 'Viernes', '10:00-11:00', 2, 2, 1),
('Claudia',  'Ramírez Peña',     '22520911', 'claudia.ramirez@tecnm.mx',   NULL, 'V-103', 'Viernes', '08:00-09:00', 3, 2, 1);

-- ── Tutorados ──────────────────────────────────
INSERT INTO tutorado (nombre, apellido, numero_control, email, foto, id_carrera, id_semestre, activo) VALUES
('Ana Laura',   'Reyes Morales',    '24400001', 'ana.reyes@tecnm.mx',       NULL, 1, 3, 1),
('Juan Pablo',  'Gómez Vargas',     '24400002', 'juan.gomez@tecnm.mx',      NULL, 1, 3, 1),
('María José',  'Hernández Ruiz',   '24400003', 'maria.hernandez@tecnm.mx', NULL, 1, 3, 1),
('Luis Ángel',  'Flores Ortiz',     '24400004', 'luis.flores@tecnm.mx',     NULL, 1, 3, 1),
('Sofía',       'Castro Blanco',    '24400005', 'sofia.castro@tecnm.mx',    NULL, 2, 3, 1),
('Diego',       'Ríos Aguilar',     '24400006', 'diego.rios@tecnm.mx',      NULL, 2, 3, 1),
('Valeria',     'Mora Espinoza',    '24400007', 'valeria.mora@tecnm.mx',    NULL, 2, 3, 1),
('Alejandro',   'Núñez Bravo',      '24400008', 'alejandro.nunez@tecnm.mx', NULL, 3, 3, 1),
('Fernanda',    'Salinas Torres',   '24400009', 'fernanda.salinas@tecnm.mx',NULL, 3, 3, 1),
('Omar',        'Gutiérrez Lima',   '24400010', 'omar.gutierrez@tecnm.mx',  NULL, 3, 3, 1),
('Daniela',     'Vázquez Serna',    '24400011', 'daniela.vazquez@tecnm.mx', NULL, 4, 3, 1),
('Ricardo',     'Peña Sandoval',    '24400012', 'ricardo.pena@tecnm.mx',    NULL, 4, 3, 1),
('Paola',       'Estrada Ramos',    '24400013', 'paola.estrada@tecnm.mx',   NULL, 5, 3, 1),
('Iván',        'Cervantes Díaz',   '24400014', 'ivan.cervantes@tecnm.mx',  NULL, 5, 3, 1),
('Gabriela',    'Luna Paredes',     '24400015', 'gabriela.luna@tecnm.mx',   NULL, 1, 3, 1),
('Jorge',       'Medina Campos',    '24100001', 'jorge.medina@tecnm.mx',    NULL, 1, 2, 1),
('Karla',       'Soto Villanueva',  '24100002', 'karla.soto@tecnm.mx',      NULL, 2, 2, 1),
('Emmanuel',    'Cruz Mendoza',     '24100003', 'emmanuel.cruz@tecnm.mx',   NULL, 3, 2, 1);

-- ── PAT ────────────────────────────────────────
INSERT INTO pat (nombre, descripcion, foto, id_semestre, id_carrera, es_general, activo) VALUES
('PAT General Enero-Junio 2026',
 'Plan de Acción Tutorial institucional para el semestre Enero-Junio 2026. Aplica a todas las carreras del tecnológico.',
 NULL, 3, NULL, 1, 1),
('PAT ISC Enero-Junio 2026',
 'Plan adaptado para Ingeniería en Sistemas. Incluye actividades enfocadas en álgebra y cálculo diferencial.',
 NULL, 3, 1, 0, 1),
('PAT IGE Enero-Junio 2026',
 'Plan adaptado para Ingeniería en Gestión Empresarial. Incluye actividades enfocadas en introducción al derecho y contabilidad.',
 NULL, 3, 2, 0, 1),
('PAT IC Enero-Junio 2026',
 'Plan adaptado para Ingeniería Civil. Incluye actividades de nivelación en cálculo diferencial y geometría.',
 NULL, 3, 3, 0, 1),
('PAT General Agosto-Diciembre 2025',
 'Plan de Acción Tutorial institucional para el semestre Agosto-Diciembre 2025.',
 NULL, 2, NULL, 1, 1);

-- ── Actividades ────────────────────────────────
INSERT INTO actividad (nombre, descripcion, fecha, semana, foto, id_pat, activo) VALUES
('Bienvenida e Inducción',          'Presentación del programa de tutorías, reglas y expectativas del semestre.',                              '2026-01-23', 1,  NULL, 1, 1),
('Detección de Necesidades',        'Aplicación del instrumento de detección de necesidades académicas, económicas y psicológicas.',            '2026-01-30', 2,  NULL, 1, 1),
('Técnicas de Estudio',             'Taller sobre estrategias y técnicas de estudio efectivas para nivel superior.',                            '2026-02-06', 3,  NULL, 1, 1),
('Orientación Vocacional',          'Sesión de orientación sobre el perfil de egreso y campo laboral de la carrera.',                           '2026-02-13', 4,  NULL, 1, 1),
('Seguimiento Académico',           'Revisión del desempeño académico del alumno en sus materias del semestre.',                                '2026-02-20', 5,  NULL, 1, 1),
('Manejo del Estrés',               'Taller de desarrollo humano: manejo del estrés y ansiedad escolar.',                                      '2026-02-27', 6,  NULL, 1, 1),
('Habilidades Socioemocionales',    'Actividad de desarrollo humano enfocada en inteligencia emocional y relaciones interpersonales.',           '2026-03-06', 7,  NULL, 1, 1),
('Asesorías Académicas',            'Canalización de alumnos con bajo rendimiento a asesorías en materias críticas.',                           '2026-03-13', 8,  NULL, 1, 1),
('Proyecto de Vida',                'Taller sobre metas personales y proyecto de vida a corto, mediano y largo plazo.',                         '2026-03-20', 9,  NULL, 1, 1),
('Cierre y Evaluación',             'Sesión de cierre del programa. Evaluación de la satisfacción del tutorado y entrega de evidencias.',       '2026-03-27', 10, NULL, 1, 1),
('Nivelación Álgebra',              'Identificación de alumnos con deficiencias en álgebra y canalización a asesorías.',                        '2026-02-06', 3,  NULL, 2, 1),
('Nivelación Cálculo Diferencial',  'Identificación de alumnos con deficiencias en cálculo diferencial y canalización a asesorías.',            '2026-02-13', 4,  NULL, 2, 1),
('Introducción al Derecho Empresarial', 'Actividad de nivelación en conceptos básicos de derecho para alumnos de IGE.',                        '2026-02-06', 3,  NULL, 3, 1),
('Fundamentos de Contabilidad',     'Actividad de apoyo en conceptos básicos de contabilidad para alumnos de nuevo ingreso.',                   '2026-02-13', 4,  NULL, 3, 1),
('Bienvenida Agosto-Diciembre 2025','Sesión de bienvenida e inducción al programa de tutorías semestre Agosto-Diciembre 2025.',                 '2025-08-22', 1,  NULL, 5, 1),
('Detección de Necesidades Ago-Dic 2025','Aplicación del instrumento de detección de necesidades semestre Agosto-Diciembre 2025.',              '2025-08-29', 2,  NULL, 5, 1);

-- ── Asignaciones Tutorado-Tutor ─────────────────
INSERT INTO asignacion_tutorado (id_tutor, id_tutorado, id_semestre, activo) VALUES
(1, 1,  3, NULL, 1),
(1, 2,  3, NULL, 1),
(1, 3,  3, NULL, 1),
(1, 4,  3, NULL, 1),
(1, 15, 3, NULL, 1),
(2, 5,  3, NULL, 1),
(2, 6,  3, NULL, 1),
(2, 7,  3, NULL, 1),
(3, 8,  3, NULL, 1),
(3, 9,  3, NULL, 1),
(3, 10, 3, NULL, 1),
(5, 11, 3, NULL, 1),
(5, 12, 3, NULL, 1),
(6, 13, 3, NULL, 1),
(6, 14, 3, NULL, 1),
(7, 16, 2, NULL, 1),
(7, 17, 2, NULL, 1),
(8, 18, 2, NULL, 1);

-- ── Sesiones ───────────────────────────────────
INSERT INTO sesion (id_tutor, id_actividad, semana, fecha_imparticion, estatus_registro, activo) VALUES
-- Carlos Mendoza (tutor 1) — semanas 1 a 5
(1, 1,  1,  '2026-01-23', 'REALIZADA',  1),
(1, 2,  2,  '2026-01-30', 'REALIZADA',  1),
(1, 3,  3,  '2026-02-06', 'REALIZADA',  1),
(1, 4,  4,  '2026-02-13', 'REALIZADA',  1),
(1, 5,  5,  '2026-02-20', 'REALIZADA',  1),
(1, 6,  6,  '2026-02-27', 'PENDIENTE',  1),
(1, 7,  7,  '2026-03-06', 'PENDIENTE',  1),
(1, 8,  8,  '2026-03-13', 'PENDIENTE',  1),
(1, 9,  9,  '2026-03-20', 'PENDIENTE',  1),
(1, 10, 10, '2026-03-27', 'PENDIENTE',  1),
-- Patricia Juárez (tutor 2) — semanas 1 a 3
(2, 1,  1,  '2026-01-23', 'REALIZADA',  1),
(2, 2,  2,  '2026-01-30', 'REALIZADA',  1),
(2, 3,  3,  '2026-02-06', 'REALIZADA',  1),
(2, 4,  4,  '2026-02-13', 'PENDIENTE',  1),
(2, 5,  5,  '2026-02-20', 'PENDIENTE',  1),
-- Roberto Castillo (tutor 3) — semanas 1 a 2
(3, 1,  1,  '2026-01-23', 'REALIZADA',  1),
(3, 2,  2,  '2026-01-30', 'REALIZADA',  1),
(3, 3,  3,  '2026-02-06', 'PENDIENTE',  1),
-- Semestre anterior — tutores 7 y 8
(7, 15, 1,  '2025-08-22', 'REALIZADA',  1),
(7, 16, 2,  '2025-08-29', 'REALIZADA',  1),
(8, 15, 1,  '2025-08-22', 'REALIZADA',  1);

-- ── Asistencias ────────────────────────────────
-- Sesión 1 (Carlos, semana 1) — todos presentes
INSERT INTO asistencia (id_sesion, id_tutorado, presente, recuperada, activo) VALUES
(1, 1,  1, 0, 1),
(1, 2,  1, 0, 1),
(1, 3,  1, 0, 1),
(1, 4,  1, 0, 1),
(1, 15, 1, 0, 1);

-- Sesión 2 (Carlos, semana 2) — uno ausente
INSERT INTO asistencia (id_sesion, id_tutorado, presente, recuperada, activo) VALUES
(2, 1,  1, 0, 1),
(2, 2,  0, 0, 1),
(2, 3,  1, 0, 1),
(2, 4,  1, 0, 1),
(2, 15, 1, 0, 1);

-- Sesión 3 (Carlos, semana 3) — dos ausentes, uno recuperado
INSERT INTO asistencia (id_sesion, id_tutorado, presente, recuperada, activo) VALUES
(3, 1,  1, 0, 1),
(3, 2,  0, 1, 1),
(3, 3,  0, 0, 1),
(3, 4,  1, 0, 1),
(3, 15, 1, 0, 1);

-- Sesión 4 (Carlos, semana 4) — todos presentes
INSERT INTO asistencia (id_sesion, id_tutorado, presente, recuperada, activo) VALUES
(4, 1,  1, 0, 1),
(4, 2,  1, 0, 1),
(4, 3,  1, 0, 1),
(4, 4,  1, 0, 1),
(4, 15, 1, 0, 1);

-- Sesión 5 (Carlos, semana 5) — uno ausente
INSERT INTO asistencia (id_sesion, id_tutorado, presente, recuperada, activo) VALUES
(5, 1,  1, 0, 1),
(5, 2,  1, 0, 1),
(5, 3,  1, 0, 1),
(5, 4,  0, 0, 1),
(5, 15, 1, 0, 1);

-- Sesión 11 (Patricia, semana 1) — todos presentes
INSERT INTO asistencia (id_sesion, id_tutorado, presente, recuperada, activo) VALUES
(11, 5, 1, 0, 1),
(11, 6, 1, 0, 1),
(11, 7, 1, 0, 1);

-- Sesión 12 (Patricia, semana 2) — uno ausente
INSERT INTO asistencia (id_sesion, id_tutorado, presente, recuperada, activo) VALUES
(12, 5, 1, 0, 1),
(12, 6, 0, 0, 1),
(12, 7, 1, 0, 1);

-- Sesión 13 (Patricia, semana 3) — todos presentes
INSERT INTO asistencia (id_sesion, id_tutorado, presente, recuperada, activo) VALUES
(13, 5, 1, 0, 1),
(13, 6, 1, 0, 1),
(13, 7, 1, 0, 1);

-- Sesión 16 (Roberto, semana 1) — todos presentes
INSERT INTO asistencia (id_sesion, id_tutorado, presente, recuperada, activo) VALUES
(16, 8,  1, 0, 1),
(16, 9,  1, 0, 1),
(16, 10, 1, 0, 1);

-- Sesión 17 (Roberto, semana 2) — uno ausente recuperado
INSERT INTO asistencia (id_sesion, id_tutorado, presente, recuperada, activo) VALUES
(17, 8,  1, 0, 1),
(17, 9,  0, 1, 1),
(17, 10, 1, 0, 1);

-- Sesión 19 (tutor 7, semestre anterior) 
INSERT INTO asistencia (id_sesion, id_tutorado, presente, recuperada, activo) VALUES
(19, 16, 1, 0, 1),
(19, 17, 1, 0, 1);

-- Sesión 20 (tutor 7, semestre anterior semana 2)
INSERT INTO asistencia (id_sesion, id_tutorado, presente, recuperada, activo) VALUES
(20, 16, 0, 0, 1),
(20, 17, 1, 0, 1);

-- Sesión 21 (tutor 8, semestre anterior)
INSERT INTO asistencia (id_sesion, id_tutorado, presente, recuperada, activo) VALUES
(21, 18, 1, 0, 1);

-- ── Evidencias de Sesión ───────────────────────
INSERT INTO evidencia_sesion (id_sesion, archivo_url, notas_coordinador, estatus_validacion, fecha_subida, activo) VALUES
(1,  NULL, 'Sesión realizada sin inconvenientes.',          'VALIDADA',   '2026-01-23', 1),
(2,  NULL, 'Se registró una inasistencia.',                 'VALIDADA',   '2026-01-30', 1),
(3,  NULL, 'Pendiente de revisión por coordinador.',        'PENDIENTE',  '2026-02-06', 1),
(4,  NULL, 'Evidencia aprobada.',                           'VALIDADA',   '2026-02-13', 1),
(5,  NULL, 'Sesión realizada correctamente.',               'VALIDADA',   '2026-02-20', 1),
(11, NULL, 'Sesión introductoria completada.',              'VALIDADA',   '2026-01-23', 1),
(12, NULL, NULL,                                            'PENDIENTE',  '2026-01-30', 1),
(16, NULL, 'Primera sesión de IC completada.',              'VALIDADA',   '2026-01-23', 1),
(19, NULL, 'Sesión del semestre anterior registrada.',      'VALIDADA',   '2025-08-22', 1);




