# Sistema de Tutorías — TecNM Chilpancingo
> Análisis exhaustivo del proyecto `app-tutorias` · Generado 2026-05-25

---

## Índice

1. [Resumen ejecutivo](#1-resumen-ejecutivo)
2. [Stack tecnológico](#2-stack-tecnológico)
3. [Configuración del sistema](#3-configuración-del-sistema)
4. [Seguridad y autenticación](#4-seguridad-y-autenticación)
5. [Roles del sistema](#5-roles-del-sistema)
6. [Modelo de datos (Entidades JPA)](#6-modelo-de-datos-entidades-jpa)
7. [DTOs y Enums](#7-dtos-y-enums)
8. [Repositorios](#8-repositorios)
9. [Servicios de dominio](#9-servicios-de-dominio)
10. [Controladores y endpoints](#10-controladores-y-endpoints)
11. [Vistas Thymeleaf por rol](#11-vistas-thymeleaf-por-rol)
12. [Servicios PDF](#12-servicios-pdf)
13. [Almacenamiento de archivos](#13-almacenamiento-de-archivos)
14. [Notificaciones y scheduler](#14-notificaciones-y-scheduler)
15. [Recursos estáticos](#15-recursos-estáticos)
16. [Reglas de negocio](#16-reglas-de-negocio)
17. [Inicialización automática (DataSeeder)](#17-inicialización-automática-dataseeder)
18. [Estadísticas del proyecto](#18-estadísticas-del-proyecto)

---

## 1. Resumen ejecutivo

Sistema web de gestión del **Programa Institucional de Tutorías (PIT)** del Tecnológico Nacional de México — Campus Chilpancingo. Administra el ciclo completo de tutorías: planeación (PAT/actividades), ejecución (sesiones/asistencias/evidencias), detección de necesidades, generación de reportes y documentos oficiales (constancias, carnets, nombramientos).

**Propósito principal:** Digitalizar y centralizar el seguimiento académico de estudiantes por parte de tutores, bajo supervisión de coordinadores, subdirector y la Dirección de Desarrollo Académico (DDA).

---

## 2. Stack tecnológico

| Capa | Tecnología | Versión |
|---|---|---|
| Lenguaje | Java | 17 |
| Framework | Spring Boot | 3.x |
| ORM | Spring Data JPA / Hibernate | — |
| Seguridad | Spring Security | 6.x |
| Plantillas | Thymeleaf 3 + Extras Spring Security | — |
| Base de datos | MySQL | — |
| Driver | `com.mysql.cj.jdbc.Driver` | — |
| Generación PDF | OpenPDF (lowagie / iText 5) | 2.0.3 |
| Email | Spring Mail (Gmail SMTP) | — |
| Build | Apache Maven | — |
| Despliegue | Docker (contenedor) | — |
| Frontend CSS | Bootstrap | 5.3.8 (dark theme) |
| Fuentes | Google Fonts — Inter | 400/500/600/700 |
| Date picker | Flatpickr | CDN |
| Validación | Jakarta Bean Validation (`@Valid`) | — |
| Lombok | `@Getter`, `@Setter`, `@Builder`, `@Slf4j` | — |

---

## 3. Configuración del sistema

**Archivo:** `src/main/resources/application.properties`

| Propiedad | Valor / Placeholder | Descripción |
|---|---|---|
| `spring.application.name` | `app-tutorias` | Nombre de la app |
| `server.port` | `${SERVER_PORT:8080}` | Puerto (default 8080) |
| `spring.datasource.url` | `jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` | URL de conexión |
| `spring.datasource.username` | `${DB_USER}` | Usuario de BD |
| `spring.datasource.password` | `${DB_PASSWORD}` | Contraseña de BD |
| `spring.jpa.hibernate.ddl-auto` | `update` | Auto-update del schema |
| `spring.jpa.show-sql` | `true` | Muestra SQL en logs |
| `spring.jpa.database-platform` | `org.hibernate.dialect.MySQLDialect` | Dialecto Hibernate |
| `file.upload.dir` | `${FILE_UPLOAD_PATH:/app/uploads}` | Directorio de subidas |
| `spring.web.resources.static-locations` | `classpath:/…, file:${FILE_UPLOAD_PATH:/app/uploads}/` | Sirve archivos subidos como estáticos |
| `spring.servlet.multipart.max-file-size` | `10MB` | Límite por archivo |
| `spring.servlet.multipart.max-request-size` | `20MB` | Límite por request |
| `server.tomcat.max-parameter-count` | `1000` | Parámetros máximos |
| `server.tomcat.max-connections` | `200` | Conexiones máximas |
| `server.tomcat.threads.max` | `200` | Threads máximos |
| `server.tomcat.accept-count` | `100` | Cola de aceptación |
| `spring.mail.host` | `smtp.gmail.com` | SMTP Gmail |
| `spring.mail.port` | `587` | Puerto STARTTLS |
| `spring.mail.username` | `${MAIL_USER}` | Cuenta de envío |
| `spring.mail.password` | `${MAIL_PASSWORD}` | Contraseña de correo |
| `notificacion.remitente.nombre` | `Sistema de Tutorias - TecNM Chilpancingo` | Nombre remitente |
| `notificacion.recordatorio.cron` | `0 0 8 ? * MON` | Cron: lunes 08:00 |
| `notificacion.recordatorio.enabled` | `true` | Activa el scheduler |

> **Archivos estáticos de subidas:** Los archivos guardados en `{FILE_UPLOAD_PATH}/tutor/abc.jpg` son accesibles en `/tutor/abc.jpg` gracias a la configuración de `static-locations`.

---

## 4. Seguridad y autenticación

**Archivo:** `src/main/java/com/bumh3r/config/SecurityConfig.java`

### 4.1 Mecanismo

- `DaoAuthenticationProvider` + `UsuarioDetailsServiceImpl`
- Contraseñas con `BCryptPasswordEncoder`
- `formLogin` en `/login` con procesamiento en `/login`
- `AuthenticationSuccessHandler` con redirección por rol

### 4.2 Redirecciones post-login

| Rol | Destino |
|---|---|
| `ROLE_DDA`, `ROLE_CIT` | `/admin/dashboard` |
| `ROLE_SUBDIRECTOR` | `/subdirector` |
| `ROLE_COORDINADOR` | `/coordinador` |
| `ROLE_TUTOR` | `/tutor` |
| `ROLE_TUTORADO` | `/tutorado` |

### 4.3 Reglas de autorización (en orden de evaluación)

```
1. /css/**, /js/**, /images/**, /styles/**, /webjars/**   → permitAll
   /login, /error, /error/**, /public/**                 → permitAll

2. Imágenes subidas (cualquier usuario autenticado):
   /tutor/*.{jpg,jpeg,png,gif,webp,JPG,JPEG,PNG}         → authenticated
   /tutorado/*.{jpg,jpeg,png,gif,webp,JPG,JPEG,PNG}      → authenticated
   /coordinador/*.{jpg,jpeg,png,gif,JPG,JPEG,PNG}        → authenticated
   /pat/*.{jpg,jpeg,png,gif}                             → authenticated
   /actividad/*.{jpg,jpeg,png,gif}                       → authenticated
   /evidencia/*.{jpg,jpeg,png,gif,pdf}                   → authenticated

3. /admin/**                → hasAnyRole("DDA", "CIT")
   /dashboard               → hasAnyRole("DDA", "CIT")
   /api/search/**           → hasAnyRole("DDA", "CIT")
   /subdirector/**          → hasRole("SUBDIRECTOR")
   /coordinador/**          → hasRole("COORDINADOR")
   /tutor/**                → hasRole("TUTOR")
   /tutorado/**             → hasRole("TUTORADO")
   /mi-cuenta/**            → authenticated

4. anyRequest             → authenticated
```

### 4.4 Logout

- URL: `POST /logout`
- Post-logout: `/login?logout`
- Invalida sesión HTTP, elimina cookie `JSESSIONID`
- Acceso denegado: `/error/403`

### 4.5 UsuarioDetailsServiceImpl

- Busca usuario por `username` (trimmed)
- Valida `u.getActivo() == true`
- Construye `UserDetails` con autoridad única `ROLE_{Rol.name()}`
- Lanza `UsernameNotFoundException` si no existe o está inactivo

---

## 5. Roles del sistema

**Enum:** `com.bumh3r.entity.Rol`

| Valor | Significado | Panel |
|---|---|---|
| `DDA` | Director de Desarrollo Académico | `/admin/**` |
| `CIT` | Coordinador Institucional de Tutorías | `/admin/**` |
| `SUBDIRECTOR` | Subdirector Académico | `/subdirector/**` |
| `COORDINADOR` | Coordinador de Carrera | `/coordinador/**` |
| `TUTOR` | Docente tutor | `/tutor/**` |
| `TUTORADO` | Estudiante tutorado | `/tutorado/**` |

---

## 6. Modelo de datos (Entidades JPA)

### 6.1 Usuario

**Tabla:** `usuario`

| Campo | Tipo Java | Columna DB | Notas |
|---|---|---|---|
| `id` | Integer | `id` | PK, auto |
| `username` | String | `username` | UNIQUE, NOT NULL, max 150 |
| `passwordHash` | String | `password_hash` | NOT NULL, max 255, BCrypt |
| `rol` | Rol (enum) | `rol` | @Enumerated(STRING), NOT NULL |
| `activo` | Boolean | `activo` | NOT NULL, default true |
| `fechaCreacion` | LocalDateTime | `fecha_creacion` | @CreationTimestamp, no actualizable |
| `tutor` | Tutor | `id_tutor` | @OneToOne, UNIQUE |
| `tutorado` | Tutorado | `id_tutorado` | @OneToOne, UNIQUE |
| `coordinador` | CoordinadorCarrera | `id_coordinador` | @OneToOne, UNIQUE |

> Un usuario tiene exactamente uno de los tres vínculos (tutor, tutorado o coordinador), o ninguno (DDA/CIT/Subdirector).

---

### 6.2 Tutor

**Tabla:** `tutor`

| Campo | Tipo Java | Columna DB | Validaciones |
|---|---|---|---|
| `id` | Integer | `id` | PK |
| `nombre` | String | `nombre` | @NotBlank, @Pattern(solo letras+espacios) |
| `apellido` | String | `apellido` | @NotBlank, @Pattern |
| `numeroControl` | String | `numero_control` | @NotBlank, @Pattern(solo dígitos), UNIQUE activo |
| `email` | String | `email` | @NotBlank, @Email, UNIQUE activo |
| `foto` | String | `foto` | nullable, nombre de archivo |
| `activo` | Integer | `activo` | soft-delete flag |
| `fechaRegistro` | Date | `fecha_registro` | @CreationTimestamp |
| `usuario` | Usuario | — | @OneToOne(mappedBy="tutor") LAZY |

---

### 6.3 Tutorado

**Tabla:** `tutorado`

| Campo | Tipo Java | Columna DB | Validaciones |
|---|---|---|---|
| `id` | Integer | `id` | PK |
| `nombre` | String | `nombre` | @NotBlank, @Pattern |
| `apellido` | String | `apellido` | @NotBlank, @Pattern |
| `numeroControl` | String | `numero_control` | @NotBlank, @Pattern(dígitos), UNIQUE activo |
| `email` | String | `email` | @NotBlank, @Email, UNIQUE activo |
| `foto` | String | `foto` | nullable |
| `carrera` | Carrera | `id_carrera` | @ManyToOne |
| `grado` | Integer | `grado` | — |
| `sexo` | String | `sexo` | — |
| `activo` | Integer | `activo` | — |
| `fechaRegistro` | Date | `fecha_registro` | @CreationTimestamp |
| `usuario` | Usuario | — | @OneToOne(mappedBy="tutorado") LAZY |

---

### 6.4 CoordinadorCarrera

**Tabla:** `coordinador_carrera`

| Campo | Tipo Java | Columna DB | Validaciones |
|---|---|---|---|
| `id` | Integer | `id` | PK |
| `nombre` | String | `nombre` | @NotBlank, @Pattern |
| `apellido` | String | `apellido` | @NotBlank, @Pattern |
| `numeroControl` | String | `numero_control` | @NotBlank |
| `email` | String | `email` | @NotBlank, @Email |
| `foto` | String | `foto` | nullable |
| `cargo` | String | `cargo` | — |
| `carrera` | Carrera | `id_carrera` | @ManyToOne |
| `semestre` | Semestre | `id_semestre` | @ManyToOne |
| `activo` | Integer | `activo` | — |
| `fechaRegistro` | Date | `fecha_registro` | @CreationTimestamp |
| `usuario` | Usuario | — | @OneToOne(mappedBy="coordinador") LAZY |

---

### 6.5 Carrera

**Tabla:** `carrera`

| Campo | Tipo Java | Columna DB | Validaciones |
|---|---|---|---|
| `id` | Integer | `id` | PK |
| `nombre` | String | `nombre` | @NotBlank, @Pattern(letras+espacios) |
| `clave` | String | `clave` | @NotBlank, @Pattern(solo letras), UNIQUE activo |
| `activo` | Integer | `activo` | — |
| `fechaRegistro` | Date | `fecha_registro` | @CreationTimestamp |

---

### 6.6 Semestre

**Tabla:** `semestre`

| Campo | Tipo Java | Columna DB | Validaciones |
|---|---|---|---|
| `id` | Integer | `id` | PK |
| `periodo` | String | `periodo` | @NotBlank |
| `anio` | Integer | `anio` | @NotNull, @Min(2000), @Max(2100) |
| `activo` | Integer | `activo` | — |
| `fechaRegistro` | Date | `fecha_registro` | @CreationTimestamp |

> **Semestre vigente:** `findFirstByActivoOrderByIdDesc(1)` — el más reciente por ID.

---

### 6.7 Grupo

**Tabla:** `grupo`

| Campo | Tipo Java | Columna DB | Validaciones |
|---|---|---|---|
| `id` | Integer | `id` | PK |
| `nombre` | String | `nombre` | @NotBlank, @Pattern |
| `tutor` | Tutor | `id_tutor` | @ManyToOne, nullable |
| `semestre` | Semestre | `id_semestre` | @ManyToOne |
| `carrera` | Carrera | `id_carrera` | @ManyToOne |
| `aula` | String | `aula` | — |
| `diaSemana` | String | `dia_semana` | — |
| `horario` | String | `horario` | — |
| `activo` | Integer | `activo` | — |
| `fechaRegistro` | Date | `fecha_registro` | @CreationTimestamp |

---

### 6.8 GrupoTutorado

**Tabla:** `grupo_tutorado` *(tabla de unión)*

| Campo | Tipo Java | Columna DB | Notas |
|---|---|---|---|
| `id` | Integer | `id` | PK |
| `grupo` | Grupo | `id_grupo` | @ManyToOne |
| `tutorado` | Tutorado | `id_tutorado` | @ManyToOne |
| `activo` | Integer | `activo` | soft-delete |

---

### 6.9 PAT (Plan de Acción Tutorial)

**Tabla:** `pat`

| Campo | Tipo Java | Columna DB | Validaciones |
|---|---|---|---|
| `id` | Integer | `id` | PK |
| `nombre` | String | `nombre` | @NotBlank, @Pattern |
| `descripcion` | String | `descripcion` | nullable |
| `foto` | String | `foto` | nullable |
| `semestre` | Semestre | `id_semestre` | @ManyToOne |
| `carrera` | Carrera | `id_carrera` | @ManyToOne |
| `esGeneral` | Integer | `es_general` | @NotNull; 1=general, 0=específico de carrera |
| `activo` | Integer | `activo` | — |
| `fechaRegistro` | Date | `fecha_registro` | @CreationTimestamp |

---

### 6.10 Actividad

**Tabla:** `actividad`

| Campo | Tipo Java | Columna DB | Validaciones |
|---|---|---|---|
| `id` | Integer | `id` | PK |
| `nombre` | String | `nombre` | @NotBlank, @Pattern |
| `descripcion` | String | `descripcion` | nullable |
| `fecha` | LocalDate | `fecha` | @DateTimeFormat(ISO.DATE), opcional |
| `semana` | Integer | `semana` | @NotNull, @Min(1), @Max(10) |
| `foto` | String | `foto` | nullable |
| `pat` | PAT | `id_pat` | @ManyToOne |
| `activo` | Integer | `activo` | — |
| `fechaRegistro` | Date | `fecha_registro` | @CreationTimestamp |

---

### 6.11 Sesion

**Tabla:** `sesion`

| Campo | Tipo Java | Columna DB | Validaciones |
|---|---|---|---|
| `id` | Integer | `id` | PK |
| `grupo` | Grupo | `id_grupo` | @ManyToOne |
| `actividad` | Actividad | `id_actividad` | @ManyToOne, nullable |
| `semana` | Integer | `semana` | @NotNull, @Min(1), @Max(10) |
| `fechaImparticion` | Date | `fecha_imparticion` | @NotNull |
| `estatusRegistro` | String | `estatus_registro` | PENDIENTE / REALIZADA / CANCELADA |
| `activo` | Integer | `activo` | — |
| `fechaRegistro` | Date | `fecha_registro` | @CreationTimestamp |

---

### 6.12 Asistencia

**Tabla:** `asistencia`

| Campo | Tipo Java | Columna DB | Notas |
|---|---|---|---|
| `id` | Integer | `id` | PK |
| `sesion` | Sesion | `id_sesion` | @ManyToOne |
| `tutorado` | Tutorado | `id_tutorado` | @ManyToOne |
| `presente` | Integer | `presente` | 1=sí, 0=no |
| `recuperada` | Integer | `recuperada` | 1=recuperada, null/0=no |
| `fechaRegistro` | Date | `fecha_registro` | @CreationTimestamp |

---

### 6.13 EvidenciaSesion

**Tabla:** `evidencia_sesion`

| Campo | Tipo Java | Columna DB | Notas |
|---|---|---|---|
| `id` | Integer | `id` | PK |
| `sesion` | Sesion | `id_sesion` | @ManyToOne (1 evidencia por sesión) |
| `archivoUrl` | String | `archivo_url` | nombre de archivo |
| `notasCoordinador` | String | `notas_coordinador` | — |
| `estatusValidacion` | String | `estatus_validacion` | PENDIENTE / VALIDADA / RECHAZADA |
| `fechaSubida` | Date | `fecha_subida` | — |
| `activo` | Integer | `activo` | — |
| `fechaRegistro` | Date | `fecha_registro` | @CreationTimestamp |

---

### 6.14 ReporteSesion

**Tabla:** `reporte_sesion`

| Campo | Tipo Java | Columna DB | Notas |
|---|---|---|---|
| `id` | Integer | `id` | PK |
| `sesion` | Sesion | `id_sesion` | @OneToOne |
| `descripcionActividad` | String | `descripcion_actividad` | TEXT |
| `observaciones` | String | `observaciones` | TEXT |
| `alumnosPresentes` | Integer | `alumnos_presentes` | — |
| `fechaEntrega` | Date | `fecha_entrega` | — |
| `estatusRevision` | String | `estatus_revision` | — |
| `activo` | Integer | `activo` | — |
| `fechaRegistro` | Date | `fecha_registro` | @CreationTimestamp |

---

### 6.15 DeteccionNecesidades

**Tabla:** `deteccion_necesidades`

| Campo | Tipo Java | Columna DB |
|---|---|---|
| `id` | Integer | `id` |
| `tutorado` | Tutorado | `id_tutorado` |
| `sesion` | Sesion | `id_sesion` (nullable) |
| `necesidadAlgebra` | Integer | `necesidad_algebra` |
| `necesidadCalculo` | Integer | `necesidad_calculo` |
| `necesidadDerecho` | Integer | `necesidad_derecho` |
| `necesidadOtra` | String | `necesidad_otra` |
| `necesidadEconomica` | Integer | `necesidad_economica` |
| `necesidadPsicologica` | Integer | `necesidad_psicologica` |
| `tieneBeca` | Integer | `tiene_beca` |
| `nombreBeca` | String | `nombre_beca` |
| `tieneEscasezMateriales` | Integer | `tiene_escasez_materiales` |
| `materialesRequeridos` | String | `materiales_requeridos` |
| `tieneAtencionMedica` | Integer | `tiene_atencion_medica` |
| `especificacionMedica` | String | `especificacion_medica` |
| `tieneVinculacionFamilia` | Integer | `tiene_vinculacion_familia` |
| `razonVinculacion` | String | `razon_vinculacion` |
| `temaPsicologico` | String | `tema_psicologico` |
| `observaciones` | String | `observaciones` |
| `activo` | Integer | `activo` |
| `fechaRegistro` | Date | `fecha_registro` |

> Los campos booleanos (Integer 0/1) se inicializan en 0 si son null al guardar.

---

### 6.16 Mapa de relaciones

```
Usuario ──1:1──► Tutor
Usuario ──1:1──► Tutorado
Usuario ──1:1──► CoordinadorCarrera

Grupo ──N:1──► Tutor
Grupo ──N:1──► Semestre
Grupo ──N:1──► Carrera

GrupoTutorado ──N:1──► Grupo
GrupoTutorado ──N:1──► Tutorado

Sesion ──N:1──► Grupo
Sesion ──N:1──► Actividad (nullable)
ReporteSesion ──1:1──► Sesion

Asistencia ──N:1──► Sesion
Asistencia ──N:1──► Tutorado

EvidenciaSesion ──N:1──► Sesion  (1 por sesión en regla de negocio)

DeteccionNecesidades ──N:1──► Tutorado
DeteccionNecesidades ──N:1──► Sesion (nullable)

Actividad ──N:1──► PAT
PAT ──N:1──► Semestre
PAT ──N:1──► Carrera

CoordinadorCarrera ──N:1──► Carrera
CoordinadorCarrera ──N:1──► Semestre
Tutorado ──N:1──► Carrera
```

---

## 7. DTOs y Enums

### 7.1 ResumenAsistenciaDTO

**Clase:** `com.bumh3r.dto.ResumenAsistenciaDTO`

| Campo | Tipo | Descripción |
|---|---|---|
| `idTutorado` | Integer | ID del tutorado |
| `nombreTutorado` | String | Nombre completo |
| `totalSesiones` | long | Total (default 10 si grupo sin sesiones) |
| `asistenciasPresente` | long | Sesiones marcadas presente=1 |
| `asistenciasRecuperadas` | long | Sesiones recuperadas=1 |
| `totalAcreditadas` | long | presente + recuperadas |
| `porcentaje` | double | (acreditadas/totalSesiones)*100, redondeado a 1 decimal |
| `acreditado` | boolean | porcentaje >= 80.0 |

### 7.2 SearchResultDTO

**Clase:** `com.bumh3r.dto.SearchResultDTO` *(Java Record)*

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | Integer | ID de la entidad |
| `nombre` | String | Nombre principal |
| `sub` | String | Subtítulo/descripción corta |
| `foto` | String | Nombre de archivo de foto |

Usado por `ApiSearchController` para los entity pickers modales.

### 7.3 CoordinadorNotificacionDTO

| Campo | Tipo | Descripción |
|---|---|---|
| `coordinador` | CoordinadorCarrera | Objeto completo |
| `totalActividades` | int | Total de actividades para la semana |
| `actividadesAplicables` | List\<Actividad\> | Actividades filtradas por carrera |
| `puedeRecibirCorreo` | boolean | Tiene email y hay actividades |

### 7.4 PublicDeteccionForm

DTO para el formulario público de detección (sin autenticación). Contiene `idTutorado`, `idSesion`, `sexo`, y todos los campos de necesidades de `DeteccionNecesidades`.

### 7.5 FileType (Enum)

| Valor | Directorio |
|---|---|
| `TUTOR` | `tutor/` |
| `TUTORADO` | `tutorado/` |
| `PAT` | `pat/` |
| `ACTIVIDAD` | `actividad/` |
| `GRUPO` | `grupo/` |
| `EVIDENCIA` | `evidencia/` |
| `COORDINADOR` | `coordinador/` |

---

## 8. Repositorios

Todos extienden `JpaRepository<Entidad, Integer>`. Ubicación: `com.bumh3r.repository`.

### 8.1 IUsuarioRepository
- `findByUsername(String)` → `Optional<Usuario>`
- `existsByUsername(String)` → boolean
- `findByRol(Rol)` → `List<Usuario>`
- `findByActivoAndRol(Boolean, Rol)` → `List<Usuario>`
- `findByActivo(Boolean)` → `List<Usuario>`

### 8.2 ITutorRepository
- Derivados: `findByActivo`, `existsByNumeroControlAndActivo`, `existsByEmailAndActivo` (con variantes `AndIdNot`)
- Queries: `searchByName`, `searchByNumeroControl`, `searchByEmail`, `searchByFechaRegistro`, `searchBySemestre` (via `EXISTS ... grupo.tutor`)

### 8.3 ITutoradoRepository
- Derivados: `findByActivo`, `existsByNumeroControlAndActivo`, `existsByEmailAndActivo`, `findByNumeroControlAndActivo`
- Queries: `searchByName`, `searchByNumeroControl`, `searchByEmail`, `findByCarreraId`, `searchByFechaRegistro`

### 8.4 IGrupoRepository
- Derivados: `findByActivo`, `findByActivoAndTutor`, `findByActivoAndSemestre`, variantes combinadas
- `existsByAulaAndDiaSemanaAndHorarioAndSemestreAndActivo` — detecta conflictos de aula
- `existsByAulaAndDiaSemanaAndHorarioAndSemestreAndActivoAndIdNot` — idem excluyendo registro actual
- `countByActivoAndTutorAndSemestre` — cuenta grupos de un tutor en semestre
- `findByActivoAndTutorIsNull` — grupos sin tutor
- Queries: `countByTutorAndSemestreExcludingId`, `existsByNombreAndSemestreAndCarreraAndActivo`, `searchByName`, `searchSinTutorByName`, `findSinTutorBySemestre`, `findSinTutorByCarrera`, `findByFechaRegistroRange`

### 8.5 IGrupoTutoradoRepository
- Derivados: `findByActivoAndGrupo`, `findByActivoAndTutorado`, `existsByGrupoAndTutoradoAndActivo`, `countByTutoradoAndActivo`, `countByGrupoAndActivo`
- Queries:
  - `findByTutoradoAndGrupoSemestreAndActivo` — asignaciones del tutorado en un semestre
  - `countActivoByGrupo` — `SELECT gt.grupo.id, COUNT(gt) FROM GrupoTutorado gt WHERE gt.activo=1 GROUP BY gt.grupo.id`
  - `buscarHistorial(q, idSemestre, idCarrera, idGrupo, estatusAcreditacion, page, pageSize)` — SQL nativo con cálculo de porcentaje: `(presentes+recuperadas)*100/total_sesiones >= 80`
  - `findTutoradosDisponibles(idCarrera, idGrupo)` — tutorados misma carrera, activos, < 2 grupos, no en el grupo actual

### 8.6 ISemestreRepository
- `findFirstByActivoOrderByIdDesc(Integer activo)` → `Optional<Semestre>` — **semestre vigente**
- `existsByPeriodoAndAnioAndActivo`, `existsByPeriodoAndAnioAndActivoExcludingId`

### 8.7 ISesionRepository
- Derivados: búsqueda por grupo, semana, estatus, combinaciones
- `existsByGrupoAndSemanaAndActivo` — sin duplicados grupo+semana
- `findTopByGrupoAndActivoOrderBySemanaDesc` — última sesión del grupo
- Queries: `existsByGrupoAndSemanaAndActivoExcludingId`, `searchByName`, `countByGruposAndEstatusRegistroIn`, `findByFechaRegistroRange`, `findByTutorId`

### 8.8 IActividadRepository
- `existsByNombreAndSemanaAndActivo`, `existsByPatAndSemanaAndActivo` (con variantes ExcludingId)
- Queries: `searchByName`, `findActividadesByTutorado` (via Asistencia→Sesion→Actividad), `findActividadesByTutor` (via Sesion→Grupo→Tutor)

### 8.9 IAsistenciaRepository
- `findBySesion`, `findByTutorado`, `countByTutoradoAndPresente`, `countByTutoradoAndRecuperada`, `existsBySesionAndTutorado`, `findByFechaRegistroBetween`
- Query: `existsByTutoradoAndSesionGrupoSemestreId`

### 8.10 IDeteccionNecesidadesRepository
- `findByActivoAndTutorado`, `findByActivoAndSesion`, filtros por necesidad específica
- `existsByTutoradoAndSesionAndActivo`, `existsByTutoradoAndActivo`, `findFirstByTutoradoAndActivo`

### 8.11 IEvidenciaSesionRepository
- `existsBySesionAndActivo`, `existsBySesionAndActivoExcludingId`
- `findByActivoAndEstatusValidacion`

### 8.12 IReporteSesionRepository
- `findBySesionAndActivo`, `existsBySesionAndActivo`, `findBySesionId`
- `existsBySesionGrupoIn` — verifica si grupos tienen reportes

### 8.13 ICarreraRepository, IPATRepository, ICoordinadorCarreraRepository
- Patrones estándar: `findByActivo`, `searchByName`, `findByFechaRegistroRange`
- Validación unicidad: `existsByClaveAndActivo` (Carrera), `existsByNombreAndActivo` (PAT)

---

## 9. Servicios de dominio

Ubicación: `com.bumh3r.service` (interfaces) y `com.bumh3r.service.impl` (implementaciones).

### 9.1 ActividadService
- CRUD completo + búsquedas paginadas por nombre, fecha, rango, PAT
- `guardarLoteActividades(idPat, actividades)` — inserción por lote, retorna lista de errores sin abortar
- Valida unicidad PAT+semana (no dos actividades en misma semana del mismo PAT)
- `buscarActividadesPorTutorado/Tutor` — navega la jerarquía via asistencias/sesiones

### 9.2 AsistenciaService
- `registrarAsistenciaMasiva(idSesion, idsTutorados[])` — crea/actualiza registros para todos los alumnos del grupo, marca sesión como REALIZADA, lanza `IllegalStateException` si sesión CANCELADA
- `calcularResumenAsistencia(idTutorado)` → `ResumenAsistenciaDTO` — porcentaje = (presentes+recuperadas)/total\*100; PORCENTAJE_MINIMO = 80.0; default totalSesiones=10 si no hay sesiones
- Búsquedas: por sesión, tutorado, rango de fechas

### 9.3 CarreraService
- CRUD con validación de `clave` única entre activos
- Soft-delete (activo=0)

### 9.4 CoordinadorCarreraService
- CRUD + búsquedas por carrera, semestre, nombre, fecha
- Resolución de relaciones (carrera, semestre) antes de persistir

### 9.5 DeteccionNecesidadesService
- `guardarDeteccion` — inicializa flags null a 0, activo=1
- Búsquedas por tutorado, sesión, tipo de necesidad específica

### 9.6 EvidenciaSesionService
- Regla: **una sola evidencia por sesión** (lanza `IllegalStateException` si ya existe)
- `validarEvidencia(id, notas)` — cambia estatusValidacion a VALIDADA
- `rechazarEvidencia(id, notas)` — cambia a RECHAZADA
- Default estatusValidacion = PENDIENTE al crear

### 9.7 GrupoService
- `guardarGrupo` / `actualizarGrupo` — valida:
  1. Nombre único en semestre+carrera
  2. Tutor máximo 2 grupos por semestre (`IllegalArgumentException`)
  3. Sin conflicto de aula+día+horario+semestre (`IllegalStateException`)
- `asignarTutor(idGrupo, idTutor)` — operación separada; también valida max 2 grupos
- `quitarTutor(idGrupo)` — desvincula tutor

### 9.8 GrupoTutoradoService
- `asignarTutorados(idGrupo, ids[])` — por lote; omite asignaciones ya existentes; valida que tutorado tenga < 2 asignaciones activas (`IllegalStateException`)
- `obtenerTutoradosDisponibles(idGrupo)` — misma carrera, activos, < 2 asignaciones, no ya en el grupo
- `contarAlumnosPorGrupo()` → `Map<Integer, Long>` — un query para todos los grupos

### 9.9 PATService
- CRUD paginado + filtros por esGeneral, carrera+semestre, nombre, fecha
- PATs generales (`esGeneral=1`) aplican a todos los coordinadores

### 9.10 ReporteSesionService
- CRUD + búsqueda por estatus de revisión
- `obtenerReportePorSesion(idSesion)` — null si no existe (manejado en controlador)

### 9.11 SemestreService
- Unicidad en par (periodo, anio)
- `obtenerSemestreVigente()` → `findFirstByActivoOrderByIdDesc(1)`

### 9.12 SesionService
- `guardarSesion` — valida semana 1-10, unicidad grupo+semana, default estatus PENDIENTE
- `buscarSesionesPorTutorPage(idTutor, pageable)` — `findByTutorId`

### 9.13 TutorService / TutoradoService
- CRUD paginado + búsquedas por nombre, numeroControl, email, semestre/carrera, fecha
- Unicidad de `numeroControl` y `email` en activos
- Foto opcional (no se borra si no se proporciona nueva)

### 9.14 UsuarioService
- `crearParaTutor/Tutorado/Coordinador` — username = email, password = numeroControl; auto-provisioning
- `cambiarPassword(idUsuario, rawPassword)` — BCrypt encode
- `desactivar(idUsuario)` — activo=false

### 9.15 PaginationUtil
`com.bumh3r.service.utils.PaginationUtil`
- `getPageable(page, pageSize, sortBy, sort)` → `Pageable` con dirección ASC/DESC

---

## 10. Controladores y endpoints

Ubicación: `com.bumh3r.controller`. Total: **23 controladores**, ~180 endpoints.

### 10.1 Panel Administrativo (DDA / CIT) — `/admin/**`

#### MainController
| Método | URL | Vista |
|---|---|---|
| GET | `/login` | `public/viewLogin` |
| GET | `/error/403` | `error/403` |
| GET | `/` | redirect `/admin/dashboard` |
| GET | `/admin/dashboard` | `index` |

#### ActividadController — `/admin/actividad`
| Método | URL | Descripción |
|---|---|---|
| GET | `/admin/actividad` | Lista paginada con filtros (fecha, PAT, nombre) |
| GET | `/admin/actividad/agregar` | Formulario (puede pre-seleccionar PAT) |
| POST | `/admin/actividad/guardar` | Crear actividad |
| GET | `/admin/actividad/api/por-pat/{idPat}` | JSON: actividades de un PAT |
| POST | `/admin/actividad/api/guardar-lote` | JSON: inserción por lote |
| PUT | `/admin/actividad/api/actualizar/{id}` | JSON: actualizar actividad |
| DELETE | `/admin/actividad/api/eliminar/{id}` | JSON: eliminar actividad |
| GET | `/admin/actividad/ver/{id}` | Detalle |
| GET/POST | `/admin/actividad/actualizar/{id}` | Editar |
| GET/POST | `/admin/actividad/delete/{id}` / `/confirm/delete/{id}` | Eliminar |

#### AsistenciaController — `/admin/asistencia`
| Método | URL | Descripción |
|---|---|---|
| GET | `/admin/asistencia` | Lista con filtros (sesión, tutorado, fecha) |
| GET | `/admin/asistencia/registrar/{idSesion}` | Formulario de registro masivo |
| POST | `/admin/asistencia/registrar/{idSesion}` | Guarda registro masivo |
| GET | `/admin/asistencia/resumen/{idTutorado}` | Resumen de asistencia de un tutorado |
| GET/POST | `/admin/asistencia/agregar`, `/guardar` | Registro individual |
| GET/POST | `/admin/asistencia/actualizar/{id}` | Editar registro |
| POST | `/admin/asistencia/recuperar/{id}` | Marcar recuperación |
| GET/POST | `/admin/asistencia/delete/{id}` / `/confirm/delete/{id}` | Eliminar |

#### CarreraController — `/admin/carrera`
CRUD estándar: lista → agregar/guardar → ver → actualizar → delete/confirm.

#### CoordinadorCarreraController — `/admin/coordinador`
CRUD + `GET /admin/coordinador/pdf/nombramiento/{id}` → PDF.

#### DeteccionNecesidadesController — `/admin/deteccion`
CRUD + `GET /admin/deteccion/pdf/{id}` → PDF.

#### EvidenciaSesionController — `/admin/evidencia`
CRUD + `POST /admin/evidencia/validar/{id}` + `POST /admin/evidencia/rechazar/{id}`.

#### GrupoController — `/admin/grupo`
| Método | URL | Descripción |
|---|---|---|
| GET | `/admin/grupo` | Lista con filtros |
| GET/POST | `/admin/grupo/agregar`, `/guardar` | Crear |
| GET | `/admin/grupo/ver/{id}` | Detalle |
| GET/POST | `/admin/grupo/actualizar/{id}` | Editar |
| GET/POST | `/admin/grupo/delete/{id}` / `/confirm/delete/{id}` | Eliminar |
| GET/POST | `/admin/grupo/asignar/{idGrupo}` | Asignar tutorados |
| POST | `/admin/grupo/tutorado/quitar/{id}` | Quitar tutorado |
| GET | `/admin/grupo/asignar-tutor` | Lista grupos para asignar tutor |
| GET/POST | `/admin/grupo/asignar-tutor/{idGrupo}` | Asignar tutor |
| POST | `/admin/grupo/quitar-tutor/{idGrupo}` | Quitar tutor |

#### HistorialController — `/admin/historial`
`GET /admin/historial` — tabla paginada con filtros (nombre, semestre, carrera, grupo, acreditación).

#### NotificacionController — `/admin/notificacion`
| Método | URL | Descripción |
|---|---|---|
| GET | `/admin/notificacion` | Centro de notificaciones con resumen |
| POST | `/admin/notificacion/enviar/todos` | Envío masivo a todos los coordinadores |
| POST | `/admin/notificacion/enviar/{idCoordinador}` | Envío individual |
| GET | `/admin/notificacion/preview/{idCoordinador}` | Preview HTML del email |

#### PATController — `/admin/pat`
CRUD estándar con upload de foto.

#### ReporteSesionController — `/admin/reporte`
CRUD + `GET /admin/reporte/sesion/{idSesion}` (redirige a formulario o muestra existente) + `GET /admin/reporte/pdf/{id}` → PDF.

#### SemestreController — `/admin/semestre`
CRUD estándar.

#### SesionController — `/admin/sesion`
CRUD estándar con filtros por grupo, tutor, semana, estatus, fecha.

#### TutorController — `/admin/tutor`
CRUD + upload de foto + `GET /admin/tutor/pdf/constancia/{id}?idSemestre=` → PDF.

#### TutoradoController — `/admin/tutorado`
CRUD + upload de foto + `GET /admin/tutorado/pdf/carnet/{id}` + `GET /admin/tutorado/pdf/constancia/{id}?idSemestre=` → PDF.

#### ApiSearchController — `/api/search` *(RestController)*
| URL | Entidades buscables |
|---|---|
| `/api/search/tutor` | Tutores |
| `/api/search/tutorado` | Tutorados |
| `/api/search/grupo` | Grupos |
| `/api/search/pat` | PATs |
| `/api/search/semestre` | Semestres |
| `/api/search/carrera` | Carreras |
| `/api/search/actividad` | Actividades |
| `/api/search/sesion` | Sesiones |

Todos aceptan `?q=&page=&size=` y retornan `{content: [SearchResultDTO], totalPages, number, totalElements}`.

---

### 10.2 Panel Subdirector — `/subdirector/**`

#### SubdirectorController
| Método | URL | Vista |
|---|---|---|
| GET | `/subdirector`, `/subdirector/`, `/subdirector/dashboard` | `subdirector/dashboard` |
| GET | `/subdirector/tutores` | `subdirector/tutores` |
| GET | `/subdirector/coordinadores` | `subdirector/coordinadores` |
| GET | `/subdirector/documentos` | `subdirector/documentos` |
| GET | `/subdirector/documentos/constancia-tutor/{idTutor}` | PDF constancia tutor |
| GET | `/subdirector/documentos/nombramiento-coordinador/{idCoordinador}` | PDF nombramiento |

---

### 10.3 Panel Coordinador — `/coordinador/**`

#### CoordinadorDashboardController
| Método | URL | Vista | Modelo |
|---|---|---|---|
| GET | `/coordinador`, `/coordinador/` | `coordinador/dashboard` | coordinador, tutores de su carrera |

---

### 10.4 Panel Tutor — `/tutor/**`

#### TutorDashboardController
| Método | URL | Vista | Seguridad |
|---|---|---|---|
| GET | `/tutor`, `/tutor/`, `/tutor/dashboard` | `tutor/dashboard` | Solo tutor autenticado |
| GET | `/tutor/grupos` | `tutor/grupos` | Solo grupos del tutor |
| GET | `/tutor/grupos/{idGrupo}` | `tutor/grupo-detalle` | Valida que el grupo sea del tutor |
| GET | `/tutor/sesiones` | `tutor/sesiones` | Solo sesiones del tutor |
| GET | `/tutor/sesiones/{idSesion}` | `tutor/sesion-detalle` | Valida que la sesión sea del tutor |
| GET | `/tutor/deteccion` | `tutor/deteccion` | Solo tutorados del tutor |
| GET | `/tutor/deteccion/{idTutorado}` | `tutor/deteccion-form` | Valida que tutorado sea del tutor |
| POST | `/tutor/deteccion/{idTutorado}` | redirect `/tutor/deteccion` | Guarda detección |
| GET | `/tutor/asistencias` | `tutor/asistencias` | Solo tutorados del tutor |

**Datos del dashboard:**
- 4 stat-cards: grupos vigentes, total tutorados, total sesiones, sesiones realizadas
- Tabla de grupos con próxima sesión por grupo (`Map<Integer, Sesion>`)

**Helpers de seguridad:**
- `resolverTutor(auth)` — obtiene Tutor del usuario autenticado
- `validarGrupoDelTutor(grupo, tutor)` — lanza `AccessDeniedException` si no es del tutor
- `validarSesionDelTutor(sesion, tutor)` — idem para sesiones
- `validarTutoradoDelTutor(tutorado, tutor)` — verifica via GrupoTutorado

---

### 10.5 Panel Tutorado — `/tutorado/**`

#### TutoradoDashboardController
| Método | URL | Descripción | Seguridad |
|---|---|---|---|
| GET | `/tutorado`, `/tutorado/`, `/tutorado/mi-tutoria` | Dashboard personal | Solo tutorado autenticado |
| GET | `/tutorado/sesiones` | Mis sesiones con asistencia | Solo datos propios |
| GET | `/tutorado/deteccion` | Mi detección de necesidades | Solo propia |
| GET | `/tutorado/deteccion/pdf` | PDF de detección (ignora param `?id=`) | SIEMPRE usa ID del autenticado |
| GET | `/tutorado/documentos` | Carnet y constancia | — |
| GET | `/tutorado/documentos/carnet` | Descarga carnet PDF | Valida antes de generar |
| GET | `/tutorado/documentos/constancia` | Descarga constancia PDF | Valida asistencia >= 80% |

**Principio de defensa en profundidad:** el endpoint `/tutorado/deteccion/pdf` ignora cualquier `?id=` en la URL y siempre usa el ID del tutorado autenticado.

---

### 10.6 Cuenta y público

#### MiCuentaController — `/mi-cuenta`
- `GET /mi-cuenta` — Perfil del usuario autenticado
- `POST /mi-cuenta/cambiar-password` — Cambio de contraseña (passwordActual, passwordNueva, passwordConfirm)

#### PublicController — `/public`
- `GET /public/actividades` — Catálogo público de actividades (sin login)
- `GET /public/actividades/{id}` — Detalle de actividad

---

## 11. Vistas Thymeleaf por rol

**Total de archivos HTML:** ~73 vistas + 1 fragmento maestro

### 11.1 Fragmento maestro

**Archivo:** `templates/fragments/fragment.html`

Contiene 3 fragmentos:
- **`sidebar`** — navegación lateral dinámica por rol con `sec:authorize`, toggle collapse, auto-marca ruta activa
- **`footer`** — copyright institucional
- **`toast`** — notificaciones Bootstrap 5 flotantes

Secciones del sidebar por rol:
- DDA/CIT: Catálogos (Semestres, Carreras) + Tutorías (Tutores, Tutorados, Grupos, PAT, Actividades) + Seguimiento (Historial, Asistencia, Detección, Reportes, Evidencias, Notificaciones)
- Subdirector: Dashboard, Personal (Tutores, Coordinadores), Documentos
- Coordinador: Dashboard
- Tutor: Dashboard, Mis Grupos, Sesiones, Detección, Asistencias
- Tutorado: Mi Tutoría, Mis Sesiones, Mi Detección, Mis Documentos

### 11.2 Vistas por carpeta

| Carpeta | Archivos | Descripción |
|---|---|---|
| `/` (raíz) | `index.html`, `mi-cuenta.html` | Dashboard admin, perfil |
| `error/` | `403.html` | Acceso denegado |
| `public/` | `viewLogin.html`, `viewPublicActividades.html`, `viewPublicDetalleActividad.html`, `viewPublicDeteccion.html`, `viewPublicDeteccionInicio.html`, `viewPublicDeteccionConfirmacion.html` | Sin autenticación |
| `emails/` | `recordatorioSemanal.html` | Plantilla de email |
| `fragments/` | `fragment.html` | Sidebar, footer, toast |
| `carrera/` | 4 archivos | Lista, form, info, confirm-delete |
| `semestre/` | 4 archivos | CRUD completo |
| `tutor/` | `viewListaTutor.html`, `viewFormTutor.html`, `viewInfoTutor.html`, `viewConfirmDeleteTutor.html`, `dashboard.html`, `grupos.html`, `grupo-detalle.html`, `sesiones.html`, `sesion-detalle.html`, `deteccion.html`, `deteccion-form.html`, `asistencias.html` | Admin + panel tutor |
| `tutorado/` | `viewListaTutorado.html`, `viewFormTutorado.html`, `viewInfoTutorado.html`, `viewConfirmDeleteTutorado.html`, `mi-tutoria.html`, `sesiones.html`, `deteccion.html`, `documentos.html` | Admin + panel tutorado |
| `coordinador/` | `viewListaCoordinador.html`, `viewFormCoordinador.html`, `viewInfoCoordinador.html`, `viewConfirmDeleteCoordinador.html`, `dashboard.html` | Admin + panel coordinador |
| `subdirector/` | `dashboard.html`, `tutores.html`, `coordinadores.html`, `documentos.html` | Panel subdirector |
| `grupo/` | `viewListaGrupo.html`, `viewFormGrupo.html`, `viewInfoGrupo.html`, `viewConfirmDeleteGrupo.html`, `viewAsignarTutorados.html`, `viewAsignarTutor.html`, `viewFormAsignarTutor.html` | CRUD + asignaciones |
| `sesion/` | 4 archivos + `viewInfoSesion.html` | CRUD con panel de acciones |
| `asistencia/` | 5 archivos | Lista, form, registro masivo, resumen |
| `actividad/` | 5 archivos + `viewAgregarActividades.html` | CRUD + builder por lote |
| `evidencia/` | 4 archivos | CRUD con validación/rechazo |
| `reporte/` | 4 archivos | CRUD + generación PDF |
| `deteccion/` | 4 archivos | CRUD detección de necesidades |
| `pat/` | 4 archivos | CRUD PAT |
| `historial/` | 1 archivo | Historial de tutorías con paginación |
| `notificacion/` | 1 archivo | Centro de notificaciones |

### 11.3 Características comunes de las vistas

- **Dark theme Bootstrap 5.3** (`data-bs-theme="dark"`) en todas las páginas
- **Entity pickers modales** (`entity-picker.js`) para selección de relaciones (Tutor, Tutorado, Grupo, PAT, etc.)
- **Paginación consistente** con `fn_pagination.js` y parámetros de URL
- **Timestamps relativos** con `rel-time.js` (`data-ts` attribute)
- **Date pickers** con Flatpickr + soporte español
- **Dual view** (cards/tabla) en grupos con persistencia `localStorage`
- **Avatares dinámicos** — foto real o fallback `ui-avatars.com`
- **Flash messages** — `msg_success`, `msg_error`, `msg_warning` con Bootstrap alerts
- **CSRF token** en todos los forms POST

### 11.4 Vista de detalle de sesión (tutor) — `tutor/sesion-detalle.html`
4 action-cards con badges de estado:
1. Registrar asistencia → `/asistencia/registrar/{id}`
2. Subir evidencia → `/evidencia/agregar?idSesion={id}`
3. Reporte de sesión (Anexo 19) → `/reporte/sesion/{id}`
4. Recuperación de asistencia → `/asistencia?tipoBusqueda=sesion&idSesion={id}`

### 11.5 Vista principal tutorado — `tutorado/mi-tutoria.html`
- Ring SVG de porcentaje de asistencia (verde ≥80%, amarillo ≥60%, rojo <60%)
- Badge Acreditado/En riesgo
- Contadores: sesiones, presentes, recuperaciones
- Card de tutor con foto o iniciales (fallback)
- Card de datos personales (nombre, NC, correo, grupo, carrera)
- Accesos rápidos a Sesiones, Detección, Documentos

---

## 12. Servicios PDF

Todos usan **OpenPDF (lowagie/iText 5)**. Retornan `byte[]`. Ubicación: `com.bumh3r.service.impl.*PdfServiceImpl`.

### 12.1 CarnetPdfService

**Método:** `generarCarnetTutorado(Integer idTutorado)`

**Validación previa (`validar`):** tutorado existe y activo, tiene grupo asignado, grupo tiene sesiones.

**Layout:** Landscape LETTER, 3 columnas:
- **Izquierda:** Logo TecNM, instrucciones (requisito 80%), área de firma
- **Centro:** Tabla de 10 actividades (semana, nombre, fecha, estatus color-coded), datos del estudiante
- **Derecha:** Asesorías/talleres obligatorios, 5 filas de recuperación, resumen numérico

**Colores:** Azul TecNM (0,51,102) · Amarillo PAT (245,200,0) · Verde presente (46,125,50) · Rojo ausente (198,40,40) · Azul recuperada (21,101,192)

**Código identificador:** `dda-{carrera}-ej{año}-{grupoId} E-{numeroControl}`

---

### 12.2 ConstanciaTutoradoPdfService

**Método:** `generarConstanciaTutorado(Integer idTutorado, Integer idSemestre)`

**Validación previa:** tutorado existe y activo, semestre existe, tiene grupo en ese semestre, hay sesiones realizadas, hay registros de asistencia, **porcentaje ≥ 80%**.

**Contenido:** Encabezado institucional (logos SEP, TecNM, emblema) · "ANEXO XVI. CONSTANCIA DE CUMPLIMIENTO DE ACTIVIDAD COMPLEMENTARIA" · Nivel de desempeño según porcentaje:

| Rango | Nivel | Calificación |
|---|---|---|
| ≥ 100% | EXCELENTE | 4.00 |
| ≥ 90% | BUENO | 3.50 |
| ≥ 80% | REGULAR | 3.00 |
| < 80% | NO ACREDITADO | 0.00 |

Firmas: ADRIANA MALDONADO BRAVO (DDA) + SERGIO RICARDO ZAGAL BARRERA (Subdirector) · C.c.p. archivo · Fecha en letras (método `convertirFechaALetras` para 2024-2030).

---

### 12.3 ConstanciaTutorPdfService

**Método:** `generarConstanciaTutor(Integer idTutor, Integer idSemestre)`

**Validación previa:** tutor existe y activo, semestre existe, tutor tiene grupos en el semestre, hay tutorados activos, hay sesiones realizadas, hay reportes de sesión generados.

**Contenido:** Encabezado igual · párrafo catedrático · sección "HACE CONSTAR" · tabla Semestre | Número de Estudiantes Tutorados · Firmas: DDA + Subdirector + SUSANA PINEDA MILLÁN (CIT).

---

### 12.4 DeteccionPdfService

**Método:** `generarPdfDeteccion(Integer idDeteccion)` (sin `validar` previo)

**Contenido:** 2 páginas — Encabezado institucional · Datos del tutorado (nombre, carrera checkboxes: ISIC/IINF/ICIV/COPU/IGEM, NC, sexo, fecha, tutor) · 7 preguntas con checkboxes/campos de texto:
- P1: Asesoría académica (Álgebra, Cálculo, Derecho, Apoyo Económico, Otra)
- P2: Beca (nombre)
- P3: Escasez de materiales
- P4: Atención médica
- P5: Vinculación familiar
- P6: Orientación psicológica
- P7: Observaciones

---

### 12.5 ReporteSesionPdfService

**Método:** `generarReporteSesion(Integer idReporte)` + `validar(Integer idReporte)`

Genera el **Anexo 19** con información de la sesión, grupo, tutor, actividad, semestre, carrera y lista de asistencias con estatus por tutorado.

---

### 12.6 NombramientoCoordinadorPdfService

**Método:** `generarNombramiento(Integer idCoordinador)` + `validar(Integer idCoordinador)`

**Contenido:** Encabezado institucional · ADRIANA MALDONADO BRAVO "N O M B R A" · datos del coordinador (nombre, NC, cargo, carrera, semestre) · fecha en letras.

---

## 13. Almacenamiento de archivos

**Servicio:** `com.bumh3r.service.impl.FileStoreServiceImp`

### Flujo de guardado
1. Recibe `MultipartFile` + `FileType`
2. Construye ruta: `{file.upload.dir}/{fileType.value}/`
3. Crea directorio si no existe
4. Genera nombre: `{UUID}.{extensión_original}`
5. Guarda y retorna **solo el nombre del archivo** (no la ruta completa)

### Acceso HTTP
Los archivos son accesibles como estáticos gracias a `spring.web.resources.static-locations`:
- Archivo en `/app/uploads/tutor/abc.jpg` → URL `/tutor/abc.jpg`
- Archivo en `/app/uploads/tutorado/xyz.png` → URL `/tutorado/xyz.png`

### Seguridad de acceso
Spring Security permite el acceso a archivos de imagen con cualquier usuario autenticado mediante reglas explícitas antes de las restricciones de rol (p.ej. `/tutor/*.jpg` → `authenticated`, antes de `/tutor/**` → `hasRole("TUTOR")`).

### Eliminación
`delete(String ruta, FileType fileType)` — construye la ruta completa, normaliza, y llama `Files.deleteIfExists`. Incluye protección contra path traversal.

---

## 14. Notificaciones y scheduler

### 14.1 NotificacionService

**Propósito:** Enviar recordatorios semanales por email a coordinadores sobre actividades del PAT programadas para la semana en curso.

**Filtrado de actividades por coordinador:**
- PATs generales (`esGeneral=1`) → aplican a todos
- PATs específicos → solo si `pat.carrera == coordinador.carrera`

**Generación del email:**
- Thymeleaf `TemplateEngine` genera HTML
- Imágenes institucionales embebidas como Base64 (lazy-loaded, cacheadas en campos estáticos)
- Semana: lunes (`previousOrSame(MONDAY)`) a domingo (`lunes.plusDays(6)`)

**Variables del template `emails/recordatorioSemanal.html`:**
`coordinador`, `actividades`, `fechaInicio`, `fechaFin`, `totalActividades`, `anioActual`, `imgLogoTecnm`, `imgSep`, `imgEmblema`, `imgTecnologicoNacional`

### 14.2 RecordatorioScheduler

**Archivo:** `src/main/scheduler/RecordatorioScheduler.java`

```
@Scheduled(cron = "0 0 8 ? * MON", zone = "America/Mexico_City")
```
- Se ejecuta **todos los lunes a las 08:00** hora de México
- Habilitable/deshabilitables via `${notificacion.recordatorio.enabled}`
- Llama a `enviarRecordatoriosSemanales()` y loguea el resultado (`Map<String, Integer>`)

---

## 15. Recursos estáticos

### 15.1 CSS

**`/styles/sidebar.css`** — sistema de diseño completo:
- Variables CSS: `--color-primary: #6366f1`, `--color-secondary: #8b5cf6`
- Dark theme Bootstrap overrides
- Clases del layout: `.app-layout`, `.app-content`, `.app-main`
- Sidebar: `.sidebar`, `.collapsed`, `.nav-item`, `.nav-label`, `.nav-group`, `.nav-children`, `.nav-toggle`, `.section-label`
- Componentes: `.glass-surface`, `.stat-card`, `.form-card`, `.page-header`, `.empty-state`, `.table-wrapper`, `.avatar`
- Badges: `.badge-active`, `.badge-inactive`, `.badge-sin-asignar`, `.badge-pendiente`
- Keyframes: `fadeInUp`, `fadeSlide`, `floatOrb`
- Footer: `.footer-sidebar`

### 15.2 JavaScript

| Archivo | Propósito |
|---|---|
| `sidebar.js` | Toggle grupos, collapse sidebar (localStorage), marca ruta activa |
| `entity-picker.js` | Modal searchable para selección de entidades (Tutor, Tutorado, Grupo, PAT, etc.); paginación interna; avatar fallback UI-Avatars |
| `fn_pagination.js` | Utilidades para paginación y cambio de pageSize |
| `custom-select.js` | Estilización de selects nativos |
| `rel-time.js` | Convierte `data-ts` a texto relativo ("hace 3 minutos") |
| `date-picker.js` | Wrapper Flatpickr en español para `data-datepicker` y `data-datepicker-range` |
| `file-picker.js` | Gestión de subida de archivos con preview |

---

## 16. Reglas de negocio

| # | Regla | Implementación |
|---|---|---|
| 1 | **Tutor máx. 2 grupos por semestre** | `GrupoService.guardarGrupo` / `asignarTutor` + `IGrupoRepository.countByActivoAndTutorAndSemestre` → `IllegalArgumentException` |
| 2 | **Tutorado máx. 2 asignaciones de grupo** | `GrupoTutoradoService.asignarTutorados` + `IGrupoTutoradoRepository.countByTutoradoAndActivo` → `IllegalStateException` |
| 3 | **Sin conflicto de aula** | `IGrupoRepository.existsByAulaAndDiaSemanaAndHorarioAndSemestreAndActivo` — conflicto scoped por semestre → `IllegalStateException` |
| 4 | **Sesión única por semana por grupo** | `ISesionRepository.existsByGrupoAndSemanaAndActivo` → `IllegalStateException` |
| 5 | **Una evidencia por sesión** | `IEvidenciaSesionRepository.existsBySesionAndActivo` → `IllegalStateException` |
| 6 | **Sesión CANCELADA no acepta asistencia** | `AsistenciaServiceImpl.registrarAsistenciaMasiva` → `IllegalStateException` |
| 7 | **Constancia tutorado requiere ≥ 80%** | `ConstanciaTutoradoPdfService.validar` verifica porcentaje antes de generar |
| 8 | **Acreditación de tutoría** | `(asistenciasPresente + asistenciasRecuperadas) / totalSesiones * 100 >= 80.0` |
| 9 | **Clave de carrera única** | `ICarreraRepository.existsByClaveAndActivo` → `IllegalArgumentException` |
| 10 | **Nombre PAT único** | `IPATRepository.existsByNombreAndActivo` → `IllegalArgumentException` |
| 11 | **Par (periodo, anio) único en semestre** | `ISemestreRepository.existsByPeriodoAndAnioAndActivo` → `IllegalArgumentException` |
| 12 | **Tutor no puede ver datos de otro tutor** | `TutorDashboardController` lanza `AccessDeniedException` → `/error/403` |
| 13 | **Tutorado solo accede a sus propios datos** | `TutoradoDashboardController` siempre usa el ID del Authentication; ignora parámetros externos |
| 14 | **Soft delete global** | Campo `activo` en todas las entidades (1=activo, 0=eliminado) |
| 15 | **Auto-provisioning de usuarios** | `DataSeeder` crea cuentas automáticamente al arrancar para tutores, tutorados y coordinadores sin usuario |
| 16 | **Tutorados disponibles para asignación** | Misma carrera que el grupo, activos, < 2 asignaciones, no ya en el grupo |

---

## 17. Inicialización automática (DataSeeder)

**Archivo:** `com.bumh3r.config.DataSeeder` (`CommandLineRunner`)

Se ejecuta al iniciar la aplicación y garantiza:

### Cuentas administrativas

| Username | Password (inicial) | Rol |
|---|---|---|
| `dda@chilpancingo.tecnm.mx` | `dda2026` | DDA |
| `cit@chilpancingo.tecnm.mx` | `cit2026` | CIT |
| `subdirector@chilpancingo.tecnm.mx` | `subdirector2026` | SUBDIRECTOR |

### Sincronización automática

Para cada **Tutor** con email y sin usuario → `usuarioService.crearParaTutor(tutor)`
- Username: email del tutor (lowercase)
- Password: numeroControl (o random si no tiene)
- Rol: TUTOR

Idem para **Tutorados** (ROLE_TUTORADO) y **Coordinadores** (ROLE_COORDINADOR).

---

## 18. Estadísticas del proyecto

| Métrica | Cantidad |
|---|---|
| Entidades JPA | 16 |
| DTOs | 4 |
| Enums | 2 (Rol, FileType) |
| Interfaces de servicio | 21 |
| Implementaciones de servicio | 21 |
| Interfaces de repositorio | 15 |
| Controladores | 23 |
| Endpoints HTTP (aprox.) | ~185 |
| Endpoints que generan PDF | 8 |
| Endpoints REST JSON | ~10 |
| Vistas Thymeleaf | ~73 |
| Archivos JavaScript | 7 |
| Archivos CSS | 1 (+ Bootstrap CDN) |
| Roles de usuario | 6 |
| Tipos de PDF generados | 6 |
| Tipos de archivo soportados (upload) | 7 (FileType enum) |
| Reglas de negocio documentadas | 16 |

---

*Documento generado automáticamente mediante análisis exhaustivo del código fuente — app-tutorias · TecNM Chilpancingo · 2026-05-25*
