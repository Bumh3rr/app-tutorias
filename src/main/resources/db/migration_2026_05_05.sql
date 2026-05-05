-- Migration: 2026-05-05
-- Run this script manually against the database before deploying.

-- 1.1 Tabla asistencia: eliminar campo activo, agregar UNIQUE(id_sesion, id_tutorado)
ALTER TABLE asistencia DROP COLUMN activo;
ALTER TABLE asistencia ADD CONSTRAINT uq_asistencia_sesion_tutorado UNIQUE (id_sesion, id_tutorado);

-- 1.2 Tabla actividad: cambiar tipo de fecha de DATETIME(6) a DATE
ALTER TABLE actividad MODIFY COLUMN fecha DATE NOT NULL;
