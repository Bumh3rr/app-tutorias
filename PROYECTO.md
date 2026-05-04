# App Tutorias — Documentación del Proyecto

Sistema de gestión de tutorías para el Tecnológico de Chilpancingo, construido con Spring Boot 3.5.13 y MySQL.

---

## Tecnologías

| Componente      | Tecnología                          |
|-----------------|-------------------------------------|
| Backend         | Java 17 + Spring Boot 3.5.13        |
| Persistencia    | Spring Data JPA + Hibernate + MySQL |
| Vistas          | Thymeleaf + Bootstrap 5.3.8         |
| Build           | Maven                               |
| Boilerplate     | Lombok                              |
| Archivos        | Almacenamiento local configurable   |

**Configuración clave (`application.properties`):**
- Puerto: `$PORT` (default 8080)
- Base de datos: variables de entorno `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DB`, `MYSQL_USER`, `MYSQL_PASSWORD`
- DDL: `create-drop` (regenera esquema en cada inicio)
- Zona horaria: `America/Mexico_City`
- Subida de archivos: máx 10 MB por archivo, 20 MB por request
- Ruta de archivos locales: `$FILE_UPLOAD_PATH`

---

## Arquitectura General

```
Controller → Service (Interface + Impl) → Repository (JpaRepository) → Entidad (JPA)
                                                                          ↕
                                                               MySQL Database
```

**Patrón de soft delete:** todas las entidades tienen campo `activo` (Integer 0/1). Los registros eliminados se marcan con `activo=0` y se excluyen de las búsquedas.

---

## Entidades

### Carrera
**Tabla:** `carrera`

| Campo    | Tipo    | Descripción           |
|----------|---------|-----------------------|
| id       | Integer | PK, autoincremental   |
| nombre   | String  | Nombre de la carrera  |
| clave    | String  | Clave institucional   |
| activo   | Integer | Soft delete (0/1)     |

**Relaciones:** 1-N con Tutor, Tutorado, PAT, CoordinadorCarrera

---

### Semestre
**Tabla:** `semestre`

| Campo   | Tipo    | Descripción          |
|---------|---------|----------------------|
| id      | Integer | PK, autoincremental  |
| periodo | String  | Ej: "Enero-Junio"    |
| anio    | Integer | Año del semestre     |
| activo  | Integer | Soft delete (0/1)    |

**Relaciones:** 1-N con Tutor, Tutorado, PAT, AsignacionTutorado, CoordinadorCarrera

---

### Tutor
**Tabla:** `tutor`

| Campo         | Tipo     | Descripción                              |
|---------------|----------|------------------------------------------|
| id            | Integer  | PK, autoincremental                      |
| nombre        | String   | Nombre(s)                                |
| apellido      | String   | Apellidos                                |
| numeroControl | String   | Número de control institucional          |
| email         | String   | Correo electrónico                       |
| foto          | String   | Nombre del archivo de foto               |
| aula          | String   | Aula asignada para tutoría               |
| horario       | String   | Horario de atención                      |
| diaSemana     | Enum     | LUNES, MARTES, MIERCOLES, JUEVES, VIERNES|
| activo        | Integer  | Soft delete (0/1)                        |

**Relaciones:**
- `@ManyToOne` → Carrera (`id_carrera`)
- `@ManyToOne` → Semestre (`id_semestre`)

**Regla de negocio:** no pueden existir dos tutores con el mismo aula + día + horario activos simultáneamente.

---

### Tutorado
**Tabla:** `tutorado`

| Campo         | Tipo    | Descripción                       |
|---------------|---------|-----------------------------------|
| id            | Integer | PK, autoincremental               |
| nombre        | String  | Nombre(s)                         |
| apellido      | String  | Apellidos                         |
| numeroControl | String  | Número de control                 |
| email         | String  | Correo electrónico                |
| foto          | String  | Nombre del archivo de foto        |
| grado         | Integer | 1=primer semestre, 2=segundo      |
| activo        | Integer | Soft delete (0/1)                 |

**Relaciones:**
- `@ManyToOne` → Carrera (`id_carrera`)
- `@ManyToOne` → Semestre (`id_semestre`)

---

### PAT (Plan de Asesorías y Tutorías)
**Tabla:** `pat`

| Campo       | Tipo    | Descripción                       |
|-------------|---------|-----------------------------------|
| id          | Integer | PK, autoincremental               |
| nombre      | String  | Nombre del PAT                    |
| descripcion | String  | Descripción                       |
| foto        | String  | Nombre del archivo de imagen      |
| esGeneral   | Integer | 1=aplica a todas las carreras     |
| activo      | Integer | Soft delete (0/1)                 |

**Relaciones:**
- `@ManyToOne` → Semestre (`id_semestre`)
- `@ManyToOne` → Carrera (`id_carrera`)
- 1-N con Actividad

---

### Actividad
**Tabla:** `actividad`

| Campo       | Tipo    | Descripción                  |
|-------------|---------|------------------------------|
| id          | Integer | PK, autoincremental          |
| nombre      | String  | Nombre de la actividad       |
| descripcion | String  | Descripción                  |
| fecha       | Date    | Fecha programada             |
| semana      | Integer | Semana del semestre          |
| foto        | String  | Nombre del archivo de imagen |
| activo      | Integer | Soft delete (0/1)            |

**Relaciones:**
- `@ManyToOne` → PAT (`id_pat`)

---

### Sesion
**Tabla:** `sesion`

| Campo            | Tipo    | Descripción                                |
|------------------|---------|--------------------------------------------|
| id               | Integer | PK, autoincremental                        |
| semana           | Integer | Semana del semestre                        |
| fechaImparticion | Date    | Fecha en que se impartió                   |
| estatusRegistro  | String  | Estado: PROGRAMADA, IMPARTIDA, CANCELADA   |
| activo           | Integer | Soft delete (0/1)                          |

**Relaciones:**
- `@ManyToOne` → Tutor (`id_tutor`)
- `@ManyToOne` → Actividad (`id_actividad`)

---

### AsignacionTutorado
**Tabla:** `asignacion_tutorado`

| Campo  | Tipo    | Descripción         |
|--------|---------|---------------------|
| id     | Integer | PK, autoincremental |
| activo | Integer | Soft delete (0/1)   |

**Relaciones:**
- `@ManyToOne` → Tutor (`id_tutor`)
- `@ManyToOne` → Tutorado (`id_tutorado`)
- `@ManyToOne` → Semestre (`id_semestre`)

**Regla de negocio:** no se puede asignar el mismo tutorado al mismo tutor en el mismo semestre dos veces.

---

### Asistencia
**Tabla:** `asistencia`

| Campo       | Tipo    | Descripción                              |
|-------------|---------|------------------------------------------|
| id          | Integer | PK, autoincremental                      |
| presente    | Integer | 1=estuvo presente en la sesión           |
| recuperada  | Integer | 1=asistencia recuperada posteriormente   |
| activo      | Integer | Soft delete (0/1)                        |

**Relaciones:**
- `@ManyToOne` → Sesion (`id_sesion`)
- `@ManyToOne` → Tutorado (`id_tutorado`)

**Regla de negocio:** para acreditar se requiere ≥ 80% de asistencia sobre 10 sesiones (presentes + recuperadas).

---

### DeteccionNecesidades
**Tabla:** `deteccion_necesidades`

| Campo               | Tipo    | Descripción                         |
|---------------------|---------|-------------------------------------|
| id                  | Integer | PK, autoincremental                 |
| necesidadAlgebra    | Integer | 1=detectada                         |
| necesidadCalculo    | Integer | 1=detectada                         |
| necesidadDerecho    | Integer | 1=detectada                         |
| necesidadOtra       | String  | Descripción de otra necesidad       |
| necesidadEconomica  | Integer | 1=detectada                         |
| necesidadPsicologica| Integer | 1=detectada                         |
| observaciones       | String  | Observaciones generales             |
| fechaAplicacion     | Date    | Fecha de la evaluación              |
| activo              | Integer | Soft delete (0/1)                   |

**Relaciones:**
- `@ManyToOne` → Tutorado (`id_tutorado`)
- `@ManyToOne` → Sesion (`id_sesion`)

---

### EvidenciaSesion
**Tabla:** `evidencia_sesion`

| Campo              | Tipo    | Descripción                              |
|--------------------|---------|------------------------------------------|
| id                 | Integer | PK, autoincremental                      |
| archivoUrl         | String  | Ruta del archivo subido                  |
| notasCoordinador   | String  | Notas de revisión del coordinador        |
| estatusValidacion  | String  | PENDIENTE, VALIDADA, RECHAZADA           |
| fechaSubida        | Date    | Fecha de carga del archivo               |
| activo             | Integer | Soft delete (0/1)                        |

**Relaciones:**
- `@ManyToOne` → Sesion (`id_sesion`)

---

### ReporteSesion
**Tabla:** `reporte_sesion`

| Campo                | Tipo    | Descripción                              |
|----------------------|---------|------------------------------------------|
| id                   | Integer | PK, autoincremental                      |
| descripcionActividad | TEXT    | Descripción de lo realizado              |
| observaciones        | TEXT    | Observaciones del tutor                  |
| alumnosPresentes     | Integer | Número de alumnos que asistieron         |
| fechaEntrega         | Date    | Fecha de entrega del reporte             |
| estatusRevision      | String  | PENDIENTE, EN_REVISION, APROBADO, RECHAZADO |
| activo               | Integer | Soft delete (0/1)                        |

**Relaciones:**
- `@OneToOne` → Sesion (`id_sesion`)

---

### CoordinadorCarrera
**Tabla:** `coordinador_carrera`

| Campo         | Tipo    | Descripción                  |
|---------------|---------|------------------------------|
| id            | Integer | PK, autoincremental          |
| nombre        | String  | Nombre(s)                    |
| apellido      | String  | Apellidos                    |
| numeroControl | String  | Número de control            |
| email         | String  | Correo electrónico           |
| foto          | String  | Nombre del archivo de foto   |
| cargo         | String  | Cargo institucional          |
| activo        | Integer | Soft delete (0/1)            |

**Relaciones:**
- `@ManyToOne` → Carrera (`id_carrera`)
- `@ManyToOne` → Semestre (`id_semestre`)

---

### DTO: ResumenAsistenciaDTO
Objeto de transferencia para la vista de resumen de asistencia.

| Campo               | Tipo    | Descripción                          |
|---------------------|---------|--------------------------------------|
| idTutorado          | Integer | ID del tutorado                      |
| nombreTutorado      | String  | Nombre completo                      |
| totalSesiones       | long    | Total de sesiones del semestre (=10) |
| asistenciasPresente | long    | Sesiones con presente=1              |
| asistenciasRecuperadas | long | Sesiones con recuperada=1            |
| totalAcreditadas    | long    | Presentes + Recuperadas              |
| porcentaje          | double  | (totalAcreditadas / 10) * 100        |
| acreditado          | boolean | true si porcentaje >= 80.0           |

---

## Diagrama de Relaciones

```
Carrera ──────┬──── Tutor ─────────────┬──── Sesion ───────────┬──── Asistencia
              │                        │         │              │
              ├──── Tutorado ──────────┤         ├──── EvidenciaSesion
              │                        │         │
              ├──── PAT ──── Actividad ─┘         └──── ReporteSesion
              │
              └──── CoordinadorCarrera

Semestre ─────┬──── Tutor
              ├──── Tutorado
              ├──── PAT
              ├──── AsignacionTutorado
              └──── CoordinadorCarrera

AsignacionTutorado: Tutor + Tutorado + Semestre (N:M con metadata)
Asistencia: Sesion + Tutorado
DeteccionNecesidades: Sesion + Tutorado
```

---

## Repositorios

Todos extienden `JpaRepository<Entidad, Integer>` y se ubican en `com.bumh3r.repository`.

| Repositorio                     | Métodos destacados                                                              |
|---------------------------------|---------------------------------------------------------------------------------|
| `ICarreraRepository`            | `findByActivo(Integer)`                                                         |
| `ISemestreRepository`           | `findByActivo(Integer)`                                                         |
| `ITutorRepository`              | `findByActivo*` paginado, `existsByAulaAndDiaSemanaAndHorarioAndActivo*`        |
| `ITutoradoRepository`           | `findByActivo*` paginado, filtro por semestre y carrera                         |
| `IPATRepository`                | `findByActivoAndEsGeneral*`, `findByActivoAndCarreraAndSemestre*`                |
| `IActividadRepository`          | `findByActivoAndFecha*`, `findByActivoAndFechaBetween*`, `findByActivoAndPat*`  |
| `ISesionRepository`             | `findByActivoAndTutor*`, `findByActivoAndSemana*`, `existsByTutorAndSemana*`    |
| `IAsignacionTutoradoRepository` | `findByActivoAndTutorAndSemestre*`, `existsByTutoradoAndSemestreAndActivo`      |
| `IAsistenciaRepository`         | `countByTutoradoAndPresenteAndActivo`, `existsBySesionAndTutoradoAndActivo`     |
| `IDeteccionNecesidadesRepository`| `findByActivoAndNecesidadAlgebra*`, `existsByTutoradoAndSesionAndActivo`       |
| `IEvidenciaSesionRepository`    | `findByActivoAndEstatusValidacion*`, `existsBySesionAndActivo`                  |
| `IReporteSesionRepository`      | `findBySesionAndActivo` (Optional), `existsBySesionAndActivo`                   |
| `ICoordinadorCarreraRepository` | `findByActivoAndCarreraAndSemestre*`                                            |

---

## Servicios

Todos siguen el patrón Interface + Implementación en `com.bumh3r.service` y `com.bumh3r.service.impl`.

### CarreraService
- `obtenerTodasCarreras()` → `List<Carrera>`
- `guardarCarrera(Carrera)`, `actualizarCarrera(id, Carrera)`, `obtenerCarrera(id)`, `eliminarCarrera(id)`

### SemestreService
- CRUD completo idéntico al patrón de CarreraService

### TutorService
- CRUD completo
- `obtenerTodosTutoresPaginado(page, size, sortBy, sort)` → `Page<Tutor>`
- `buscarTutoresPorSemestrePaginado(idSemestre, ...)` → `Page<Tutor>`
- `buscarTutoresPorSemestreYCarreraPaginado(idSemestre, idCarrera, ...)` → `Page<Tutor>`

### TutoradoService
- CRUD completo
- `obtenerTodosTutoradosPaginado(...)`, `buscarTutoradosPorSemestreYCarreraPaginado(...)` → `Page<Tutorado>`

### PATService
- CRUD completo
- `obtenerPATGenerales()` → `List<PAT>`
- `buscarPATporCarreraYSemestre(idCarrera, idSemestre)` → `List<PAT>`
- Variantes paginadas de cada búsqueda

### ActividadService
- CRUD completo
- `buscarActividadesPorFecha(Date)`, `buscarActividadesPorRangoFechas(inicio, fin)`, `buscarActividadesPorPAT(idPat)`
- Variantes paginadas

### SesionService
- CRUD completo
- `buscarSesionesPorTutor(idTutor)`, `buscarSesionesPorSemana(semana)`, `buscarSesionesPorTutorYSemana(...)`, `buscarSesionesPorEstatus(estatus)`

### AsignacionTutoradoService
- CRUD completo
- `buscarAsignacionesPorTutorYSemestre(idTutor, idSemestre)`, `buscarAsignacionesPorSemestre(idSemestre)`

### AsistenciaService
- CRUD completo
- `registrarAsistenciaMasiva(idSesion, idsTutoradosPresentes[])` — actualiza/crea registros en bloque
- `calcularResumenAsistencia(idTutorado)` → `ResumenAsistenciaDTO`
- Constantes: `TOTAL_SESIONES=10`, `PORCENTAJE_MINIMO=80.0`

### DeteccionNecesidadesService
- CRUD completo
- `buscarPorTutorado(idTutorado)`, `buscarPorSesion(idSesion)`
- `buscarPorNecesidadAlgebra()`, `...Calculo()`, `...Economica()`, `...Psicologica()`

### EvidenciaSesionService
- CRUD completo
- `validarEvidencia(id, notas)` — cambia estatus a VALIDADA
- `rechazarEvidencia(id, notas)` — cambia estatus a RECHAZADA

### ReporteSesionService
- CRUD completo
- `obtenerReportePorSesion(idSesion)` → `ReporteSesion`
- `buscarPorEstatus(estatus)` → `List<ReporteSesion>`

### CoordinadorCarreraService
- CRUD completo
- `buscarPorCarrera(idCarrera)`, `buscarPorSemestre(idSemestre)`, `buscarPorCarreraYSemestre(...)`

### FileStoreService
- `save(MultipartFile, FileType)` → `String` (nombre del archivo con UUID)
- `delete(ruta, FileType)` → `void`
- `getUrlBase()` → `String`
- Organiza archivos en subdirectorios por `FileType`: TUTOR, TUTORADO, PAT, ACTIVIDAD, ASIGNACION, EVIDENCIA, COORDINADOR

### Utilidades
- `PaginationUtil.getPageable(page, pageSize, sortBy, sort)` → `Pageable`

---

## Controladores

Todos en `com.bumh3r.controller`. Rutas base y sus vistas:

### MainController — `/`
| Método | Ruta | Vista          | Descripción                              |
|--------|------|----------------|------------------------------------------|
| GET    | `/`  | `index`        | Dashboard con 5 estadísticas + próximas actividades |

---

### CarreraController — `/carrera`
| Método | Ruta                  | Vista                       |
|--------|-----------------------|-----------------------------|
| GET    | `/`                   | `carrera/viewListaCarrera`  |
| GET    | `/agregar`            | `carrera/viewFormCarrera`   |
| POST   | `/guardar`            | redirect `/carrera`         |
| GET    | `/ver/{id}`           | `carrera/viewInfoCarrera`   |
| GET    | `/actualizar/{id}`    | `carrera/viewFormCarrera`   |
| POST   | `/actualizar/{id}`    | redirect `/carrera`         |
| GET    | `/delete/{id}`        | redirect `/carrera`         |

---

### SemestreController — `/semestre`
Mismo patrón CRUD que CarreraController.

---

### TutorController — `/tutor`
| Método | Ruta               | Vista                    | Params                                          |
|--------|--------------------|--------------------------|--------------------------------------------------|
| GET    | `/`                | `tutor/viewListaTutor`   | `idSemestre`, `idCarrera`, `page`, `pageSize`, `sortBy`, `sort` |
| GET    | `/agregar`         | `tutor/viewFormTutor`    | carga carreras, semestres, días semana           |
| POST   | `/guardar`         | redirect `/tutor`        | valida unicidad aula+día+horario                 |
| GET    | `/ver/{id}`        | `tutor/viewInfoTutor`    |                                                  |
| GET    | `/actualizar/{id}` | `tutor/viewFormTutor`    |                                                  |
| POST   | `/actualizar/{id}` | redirect `/tutor`        | valida unicidad excluyendo el tutor actual       |
| GET    | `/delete/{id}`     | redirect `/tutor`        |                                                  |

---

### TutoradoController — `/tutorado`
Mismo patrón que TutorController con filtros por semestre y carrera.

---

### PATController — `/pat`
| Método | Ruta               | Vista               | Params                               |
|--------|--------------------|---------------------|--------------------------------------|
| GET    | `/`                | `pat/viewListaPAT`  | `idCarrera`, `idSemestre`, `soloGenerales`, paginación |
| GET    | `/agregar`         | `pat/viewFormPAT`   |                                      |
| POST   | `/guardar`         | redirect `/pat`     |                                      |
| GET    | `/ver/{id}`        | `pat/viewInfoPAT`   |                                      |
| GET    | `/actualizar/{id}` | `pat/viewFormPAT`   |                                      |
| POST   | `/actualizar/{id}` | redirect `/pat`     |                                      |
| GET    | `/delete/{id}`     | redirect `/pat`     |                                      |

---

### ActividadController — `/actividad`
| Método | Ruta               | Vista                       | Params                                                |
|--------|--------------------|-----------------------------|-------------------------------------------------------|
| GET    | `/`                | `actividad/viewListaActividad` | `tipoBusqueda` (fecha/rango/pat/todos), `fecha`, `fechaInicio`, `fechaFin`, `idPat` |
| GET    | `/agregar`         | `actividad/viewFormActividad` | carga PATs                                           |
| POST   | `/guardar`         | redirect `/actividad`       | valida fecha futura                                   |
| GET    | `/ver/{id}`        | `actividad/viewInfoActividad` |                                                      |
| GET    | `/actualizar/{id}` | `actividad/viewFormActividad` |                                                      |
| POST   | `/actualizar/{id}` | redirect `/actividad`       |                                                      |
| GET    | `/delete/{id}`     | redirect `/actividad`       |                                                      |

---

### SesionController — `/sesion`
| Método | Ruta               | Vista                   | Params                                               |
|--------|--------------------|-------------------------|------------------------------------------------------|
| GET    | `/`                | `sesion/viewListaSesion` | `tipoBusqueda` (tutor/semana/tutorSemana/estatus/todos), `idTutor`, `semana`, `estatus` |
| GET    | `/agregar`         | `sesion/viewFormSesion` | carga tutores y actividades                          |
| POST   | `/guardar`         | redirect `/sesion`      |                                                      |
| GET    | `/ver/{id}`        | `sesion/viewInfoSesion` |                                                      |
| GET    | `/actualizar/{id}` | `sesion/viewFormSesion` |                                                      |
| POST   | `/actualizar/{id}` | redirect `/sesion`      |                                                      |
| GET    | `/delete/{id}`     | redirect `/sesion`      |                                                      |

---

### AsignacionTutoradoController — `/asignacion`
CRUD completo. Valida duplicados tutor+tutorado+semestre. Carga listas de tutores, tutorados y semestres.

---

### AsistenciaController — `/asistencia`
| Método | Ruta                       | Vista                           | Descripción                                 |
|--------|----------------------------|---------------------------------|---------------------------------------------|
| GET    | `/`                        | `asistencia/viewListaAsistencia`| Filtros: por sesión, por tutorado, todos    |
| GET    | `/agregar`                 | `asistencia/viewFormAsistencia` | Formulario individual                       |
| POST   | `/guardar`                 | redirect `/asistencia`          |                                             |
| GET    | `/registrar/{idSesion}`    | `asistencia/viewRegistrarAsistencia` | Lista de tutorados con checkbox presente |
| POST   | `/registrarMasivo/{idSesion}` | redirect `/asistencia`       | Array de IDs de tutorados presentes         |
| GET    | `/ver/{id}`                | vista detalle                   |                                             |
| GET    | `/actualizar/{id}`         | `asistencia/viewFormAsistencia` |                                             |
| POST   | `/actualizar/{id}`         | redirect `/asistencia`          |                                             |
| GET    | `/resumen/{idTutorado}`    | `asistencia/viewResumenAsistencia` | Muestra ResumenAsistenciaDTO             |

---

### DeteccionNecesidadesController — `/deteccion`
CRUD completo. Formulario con checkboxes para necesidades académicas (álgebra, cálculo, derecho) y socioemocionales (económica, psicológica) más campo libre.

---

### EvidenciaSesionController — `/evidencia`
| Método | Ruta            | Vista                       | Descripción                          |
|--------|-----------------|-----------------------------|--------------------------------------|
| GET    | `/`             | `evidencia/viewListaEvidencia` | Filtros: por sesión, por estatus  |
| GET    | `/agregar`      | `evidencia/viewFormEvidencia` | Input file para subir evidencia    |
| POST   | `/guardar`      | redirect `/evidencia`       |                                      |
| POST   | `/validar/{id}` | redirect                    | Marca como VALIDADA con notas        |
| POST   | `/rechazar/{id}`| redirect                    | Marca como RECHAZADA con notas       |
| GET    | `/descargar/{id}`| —                          | Descarga el archivo adjunto          |

---

### ReporteSesionController — `/reporte`
CRUD completo con campos: descripción de actividad, observaciones, número de alumnos presentes, fecha de entrega.
Estados del reporte: `PENDIENTE`, `EN_REVISION`, `APROBADO`, `RECHAZADO`.

---

### CoordinadorCarreraController — `/coordinador`
CRUD completo con carga de foto y asignación a carrera y semestre.

---

## Vistas

Todas en `src/main/resources/templates/`. Usa Thymeleaf + Bootstrap 5.3.8 con tema oscuro (`data-bs-theme="dark"`).

### Fragmentos reutilizables
**`fragments/fragment.html`** — sidebar de navegación con las secciones:
- Principal (Dashboard)
- Catálogos (Carreras, Semestres)
- Recursos (Tutores, Tutorados, Coordinadores)
- PAT (PATs, Actividades)
- Tutorías (Asignaciones, Sesiones)
- Seguimiento (Asistencia, Detección de Necesidades, Evidencias, Reportes)

### index.html — Dashboard
- Tarjetas: total de tutores, tutorados, actividades y asignaciones activas
- Tabla de próximas 5 actividades ordenadas por fecha

### Carpeta `carrera/`
| Archivo                      | Contenido                              |
|------------------------------|----------------------------------------|
| `viewListaCarrera.html`      | Tabla de carreras activas, botón agregar |
| `viewFormCarrera.html`       | Formulario crear/editar (nombre, clave) |
| `viewInfoCarrera.html`       | Detalle de carrera                     |
| `viewConfirmDeleteCarrera.html` | Confirmación de eliminación          |

### Carpeta `semestre/`
| Archivo                       | Contenido                               |
|-------------------------------|-----------------------------------------|
| `viewListaSemestre.html`      | Tabla de semestres                      |
| `viewFormSemestre.html`       | Formulario (período, año)               |
| `viewInfoSemestre.html`       | Detalles del semestre                   |
| `viewConfirmDeleteSemestre.html` | Confirmación                         |

### Carpeta `tutor/`
| Archivo                     | Contenido                                                         |
|-----------------------------|-------------------------------------------------------------------|
| `viewListaTutor.html`       | Tabla paginada con filtros por semestre/carrera y ordenamiento    |
| `viewFormTutor.html`        | Nombre, apellido, número control, email, foto, aula, día, horario, carrera, semestre |
| `viewInfoTutor.html`        | Detalle completo con foto                                         |
| `viewConfirmDeleteTutor.html` | Confirmación                                                    |

### Carpeta `tutorado/`
Similar a `tutor/` con campo `grado`.

### Carpeta `pat/`
| Archivo                  | Contenido                                               |
|--------------------------|---------------------------------------------------------|
| `viewListaPAT.html`      | Tabla paginada, filtros: solo generales, por carrera+semestre |
| `viewFormPAT.html`       | Nombre, descripción, foto, esGeneral, carrera, semestre |
| `viewInfoPAT.html`       | Detalle                                                 |
| `viewConfirmDeletePAT.html` | Confirmación                                         |

### Carpeta `actividad/`
| Archivo                       | Contenido                                                  |
|-------------------------------|------------------------------------------------------------|
| `viewListaActividad.html`     | Tabla paginada, búsqueda: fecha exacta, rango, por PAT     |
| `viewFormActividad.html`      | Nombre, descripción, fecha, semana, foto, PAT              |
| `viewInfoActividad.html`      | Detalle                                                    |
| `viewConfirmDeleteActividad.html` | Confirmación                                           |

### Carpeta `sesion/`
| Archivo                     | Contenido                                                   |
|-----------------------------|-------------------------------------------------------------|
| `viewListaSesion.html`      | Tabla con búsqueda por tutor, semana, tutor+semana, estatus |
| `viewFormSesion.html`       | Tutor, actividad, semana, fecha impartición, estatus        |
| `viewInfoSesion.html`       | Detalle                                                     |
| `viewConfirmDeleteSesion.html` | Confirmación                                             |

### Carpeta `asignacion/`
| Archivo                        | Contenido                            |
|--------------------------------|--------------------------------------|
| `viewListaAsignacion.html`     | Tabla de asignaciones tutor-tutorado |
| `viewFormAsignacion.html`      | Tutor, tutorado, semestre            |
| `viewInfoAsignacion.html`      | Detalle                              |
| `viewConfirmDeleteAsignacion.html` | Confirmación                     |

### Carpeta `asistencia/`
| Archivo                          | Contenido                                                    |
|----------------------------------|--------------------------------------------------------------|
| `viewListaAsistencia.html`       | Tabla con filtros por sesión o tutorado                      |
| `viewFormAsistencia.html`        | Formulario individual (sesión, tutorado, presente, recuperada)|
| `viewRegistrarAsistencia.html`   | Lista de tutorados con checkbox para marcar presentes masivamente |
| `viewResumenAsistencia.html`     | ResumenAsistenciaDTO: estadísticas, porcentaje, estado de acreditación |
| `viewConfirmDeleteAsistencia.html` | Confirmación                                               |

### Carpeta `deteccion/`
| Archivo                       | Contenido                                                         |
|-------------------------------|-------------------------------------------------------------------|
| `viewListaDeteccion.html`     | Tabla de detecciones                                              |
| `viewFormDeteccion.html`      | Checkboxes para necesidades + campo libre + observaciones         |
| `viewInfoDeteccion.html`      | Detalle                                                           |
| `viewConfirmDeleteDeteccion.html` | Confirmación                                                  |

### Carpeta `evidencia/`
| Archivo                        | Contenido                                                   |
|--------------------------------|-------------------------------------------------------------|
| `viewListaEvidencia.html`      | Tabla con filtro por sesión y estatus de validación         |
| `viewFormEvidencia.html`       | Input file para subir evidencia, selección de sesión        |
| `viewInfoEvidencia.html`       | Detalle con estatus de validación y notas del coordinador   |
| `viewConfirmDeleteEvidencia.html` | Confirmación                                             |

### Carpeta `reporte/`
| Archivo                       | Contenido                                                    |
|-------------------------------|--------------------------------------------------------------|
| `viewListaReporte.html`       | Tabla de reportes con estatus                                |
| `viewFormReporte.html`        | Descripción actividad, observaciones, alumnos presentes      |
| `viewInfoReporte.html`        | Detalle completo                                             |
| `viewConfirmDeleteReporte.html` | Confirmación                                               |

### Carpeta `coordinador/`
| Archivo                           | Contenido                                    |
|-----------------------------------|----------------------------------------------|
| `viewListaCoordinador.html`       | Tabla de coordinadores                       |
| `viewFormCoordinador.html`        | Datos personales + cargo + carrera + semestre|
| `viewInfoCoordinador.html`        | Detalle con foto                             |
| `viewConfirmDeleteCoordinador.html` | Confirmación                               |

### Patrones comunes en vistas
- Alertas para `msg_success` y `msg_error` pasados por el controlador
- Filtros activos mostrados con badge informativo
- Tablas con acciones: Ver, Editar, Eliminar en cada fila
- Subida de archivos: acepta JPG/PNG, máx 10 MB, con vista previa en edición
- Date pickers nativos de HTML5
- Selects poblados desde el modelo para relaciones (carrera, semestre, tutor, etc.)
- Campos `hidden` para IDs en formularios de edición

---

## Recursos Estáticos

```
src/main/resources/static/
├── styles/
│   └── sidebar.css       # Layout del sidebar, tema oscuro, animaciones
└── images/
    └── logo.png          # Logo institucional (Tecnológico)
```

---

## Resumen de Conteo

| Capa          | Cantidad |
|---------------|----------|
| Entidades     | 12       |
| DTOs          | 1        |
| Repositorios  | 12       |
| Servicios     | 14       |
| Controladores | 13       |
| Vistas HTML   | ~56      |
| Rutas totales | ~90+     |
