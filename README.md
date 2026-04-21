# app-tutorias
Sistema web de gestión del Programa de Tutorías del Tecnológico de Chilpancingo, desarrollado con Spring Boot, Thymeleaf y MySQL. Automatiza el registro de asistencias, sesiones, evidencias y detección de necesidades académicas.

## Tecnologías

| Capa | Tecnología |
|---|---|
| Framework | Spring Boot 3.5.13 |
| Lenguaje | Java 17 |
| Vistas | Thymeleaf + Bootstrap 5 (dark theme) |
| Base de datos | MySQL |
| ORM | Spring Data JPA + Hibernate |
| Generación de PDF | OpenPDF |
| Build tool | Maven |

## Módulos implementados

- **Catálogos** — Carreras y Semestres
- **Tutorías** — Tutores, Tutorados, Plan de Acción Tutorial (PAT),
  Actividades, Asignaciones, Coordinadores de Carrera
- **Seguimiento** — Sesiones semanales, Asistencia con cálculo
  automático del 80%, Evidencias de sesión, Detección de Necesidades

## Funcionalidades principales

- CRUD completo de todas las entidades con soft delete (campo `activo`)
- Registro masivo de asistencia por sesión con checkboxes
- Cálculo automático del porcentaje de asistencia por tutorado
- Acreditación automática basada en el 80% requerido
- Recuperación de inasistencias mediante talleres
- Instrumento digital de Detección de Necesidades (académicas,
  económicas y psicológicas)
- Validación y rechazo de evidencias por coordinador
- Búsquedas con filtros por múltiples criterios en cada módulo
- Sidebar colapsable con estado persistido en localStorage
- Generación de PDFs institucionales (en desarrollo)
- Spring Security con roles (en desarrollo)
