# Sistema de Tutorías — Documentación del Proyecto

Sistema de gestión de tutorías académicas construido con **Spring Boot 3**, **Thymeleaf**, **Spring Data JPA** y **Bootstrap 5**. Permite registrar tutores, grupos, tutorados, sesiones, asistencia, detección de necesidades y generación de documentos PDF.

---

## Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Spring Boot 3.5.13 · Java 21 |
| Persistencia | Spring Data JPA · Hibernate · MySQL |
| Vistas | Thymeleaf 3.1.3 · Bootstrap 5.3 · Inter font |
| Validación | Jakarta Validation (Bean Validation 3) |
| Utilidades | Lombok · Spring DevTools · Spring Multipart |
| PDF | iText |
| Build | Maven |

**Paquete base:** `com.bumh3r`  
**Puerto por defecto:** `8080`

---

## Estructura de Paquetes

```
com.bumh3r/
├── controller/          17 controladores MVC
├── dto/                 2 objetos de transferencia
├── entity/              14 entidades JPA
├── repository/          14 interfaces JPA
├── service/             15 interfaces de servicio
│   ├── enums/           FileType enum
│   ├── impl/            15 implementaciones
│   └── utils/           PaginationUtil
└── AppTutoriasApplication.java
```

---

## Entidades

### Tutor
**Tabla:** `tutor`

| Campo | Tipo | Restricciones |
|---|---|---|
| id | Integer | PK, autoincremental |
| nombre | String | NotBlank, solo letras |
| apellido | String | NotBlank, solo letras |
| numeroControl | String | NotBlank, solo dígitos |
| email | String | NotBlank, formato email |
| foto | String | Nullable (nombre de archivo) |
| activo | Integer | 1=activo, 0=inactivo |
| fechaRegistro | Date | CreationTimestamp, no editable |

Relaciones: un Tutor puede ser asignado a múltiples Grupos.

---

### Tutorado
**Tabla:** `tutorado`

| Campo | Tipo | Restricciones |
|---|---|---|
| id | Integer | PK, autoincremental |
| nombre | String | NotBlank, solo letras |
| apellido | String | NotBlank, solo letras |
| numeroControl | String | NotBlank, solo dígitos |
| email | String | NotBlank, formato email |
| foto | String | Nullable |
| carrera | Carrera | ManyToOne (id_carrera) |
| grado | Integer | Nullable |
| sexo | String | Nullable |
| activo | Integer | 1=activo, 0=inactivo |
| fechaRegistro | Date | CreationTimestamp |

Puede pertenecer a hasta 2 grupos activos simultáneos (validado en servicio).

---

### Grupo
**Tabla:** `grupo`

| Campo | Tipo | Restricciones |
|---|---|---|
| id | Integer | PK |
| nombre | String | NotBlank |
| tutor | Tutor | ManyToOne (id_tutor), nullable |
| semestre | Semestre | ManyToOne (id_semestre) |
| carrera | Carrera | ManyToOne (id_carrera) |
| aula | String | Nullable |
| diaSemana | String | Nullable |
| horario | String | Nullable |
| activo | Integer | 1=activo |
| fechaRegistro | Date | CreationTimestamp |

---

### GrupoTutorado
**Tabla:** `grupo_tutorado` — Relación muchos-a-muchos entre Grupo y Tutorado

| Campo | Tipo | Notas |
|---|---|---|
| id | Integer | PK |
| grupo | Grupo | ManyToOne (id_grupo) |
| tutorado | Tutorado | ManyToOne (id_tutorado) |
| activo | Integer | 1=activo; permite baja sin borrar registro |

---

### Sesion
**Tabla:** `sesion`

| Campo | Tipo | Restricciones |
|---|---|---|
| id | Integer | PK |
| grupo | Grupo | ManyToOne (id_grupo) |
| actividad | Actividad | ManyToOne (id_actividad) |
| semana | Integer | NotNull, 1-10 |
| fechaImparticion | Date | NotNull |
| estatusRegistro | String | `PENDIENTE` / `REALIZADA` / `CANCELADA` |
| activo | Integer | |
| fechaRegistro | Date | CreationTimestamp |

---

### PAT (Plan de Acción Tutorial)
**Tabla:** `pat`

| Campo | Tipo | Restricciones |
|---|---|---|
| id | Integer | PK |
| nombre | String | NotBlank |
| descripcion | String | Nullable |
| foto | String | Nullable |
| semestre | Semestre | ManyToOne (id_semestre) |
| carrera | Carrera | ManyToOne (id_carrera), nullable si esGeneral=1 |
| esGeneral | Integer | NotNull — 1=general, 0=por carrera |
| activo | Integer | |
| fechaRegistro | Date | CreationTimestamp |

---

### Actividad
**Tabla:** `actividad`

| Campo | Tipo | Restricciones |
|---|---|---|
| id | Integer | PK |
| nombre | String | NotBlank |
| descripcion | String | Nullable |
| fecha | Date | NotNull (LocalDate) |
| semana | Integer | NotNull, 1-10 |
| foto | String | Nullable |
| pat | PAT | ManyToOne (id_pat) |
| activo | Integer | |
| fechaRegistro | Date | CreationTimestamp |

---

### Asistencia
**Tabla:** `asistencia`

| Campo | Tipo | Notas |
|---|---|---|
| id | Integer | PK |
| sesion | Sesion | ManyToOne (id_sesion) |
| tutorado | Tutorado | ManyToOne (id_tutorado) |
| presente | Integer | 1=presente, 0=ausente |
| recuperada | Integer | 1=asistencia recuperada |
| fechaRegistro | Date | CreationTimestamp |

> **Nota:** El campo `activo` fue eliminado de esta tabla. La unicidad se garantiza con constraint `uq_asistencia_sesion_tutorado (id_sesion, id_tutorado)`. Migración: `ALTER TABLE asistencia DROP COLUMN activo; ALTER TABLE asistencia ADD CONSTRAINT uq_asistencia_sesion_tutorado UNIQUE (id_sesion, id_tutorado);`

---

### DeteccionNecesidades
**Tabla:** `deteccion_necesidades`

| Campo | Tipo | Notas |
|---|---|---|
| id | Integer | PK |
| tutorado | Tutorado | ManyToOne (id_tutorado) |
| sesion | Sesion | ManyToOne (id_sesion) |
| necesidadAlgebra | Integer | 1=necesita apoyo |
| necesidadCalculo | Integer | 1=necesita apoyo |
| necesidadDerecho | Integer | 1=necesita apoyo en Intro. Derecho |
| necesidadOtra | String | Texto libre, nullable |
| necesidadEconomica | Integer | 1=apoyo económico |
| necesidadPsicologica | Integer | 1=apoyo psicológico |
| tieneBeca | Integer | 1=solicita beca |
| nombreBeca | String | Nombre/tipo de beca solicitada, nullable |
| tieneEscasezMateriales | Integer | 1=solicita material |
| materialesRequeridos | String | Descripción del material requerido, nullable |
| tieneAtencionMedica | Integer | 1=solicita atención médica |
| especificacionMedica | String | Detalle de la atención médica requerida, nullable |
| tieneVinculacionFamilia | Integer | 1=requiere vinculación familiar |
| razonVinculacion | String | Motivo de la vinculación familiar, nullable |
| temaPsicologico | String | Descripción del tema psicológico a atender, nullable |
| observaciones | String | Texto libre, nullable |
| activo | Integer | |
| fechaRegistro | Date | CreationTimestamp |

---

### EvidenciaSesion
**Tabla:** `evidencia_sesion`

| Campo | Tipo | Notas |
|---|---|---|
| id | Integer | PK |
| sesion | Sesion | ManyToOne (id_sesion) |
| archivoUrl | String | Nombre de archivo almacenado |
| notasCoordinador | String | Nullable |
| estatusValidacion | String | `PENDIENTE` / `VALIDADA` / `RECHAZADA` |
| fechaSubida | Date | Se asigna en el controller |
| activo | Integer | |
| fechaRegistro | Date | CreationTimestamp |

---

### ReporteSesion
**Tabla:** `reporte_sesion`

| Campo | Tipo | Notas |
|---|---|---|
| id | Integer | PK |
| sesion | Sesion | OneToOne (id_sesion) |
| descripcionActividad | String | TEXT |
| observaciones | String | TEXT |
| alumnosPresentes | Integer | |
| fechaEntrega | Date | |
| estatusRevision | String | Estado de revisión del reporte |
| activo | Integer | |
| fechaRegistro | Date | CreationTimestamp |

---

### Semestre
**Tabla:** `semestre`

| Campo | Tipo | Restricciones |
|---|---|---|
| id | Integer | PK |
| periodo | String | NotBlank — ej. "Enero-Junio" |
| anio | Integer | NotNull, 2000-2100 |
| activo | Integer | |
| fechaRegistro | Date | CreationTimestamp |

---

### Carrera
**Tabla:** `carrera`

| Campo | Tipo | Restricciones |
|---|---|---|
| id | Integer | PK |
| nombre | String | NotBlank, solo letras |
| clave | String | NotBlank, solo letras — ej. "ISC" |
| activo | Integer | |
| fechaRegistro | Date | CreationTimestamp |

---

### CoordinadorCarrera
**Tabla:** `coordinador_carrera`

| Campo | Tipo | Restricciones |
|---|---|---|
| id | Integer | PK |
| nombre | String | NotBlank, solo letras |
| apellido | String | NotBlank, solo letras |
| numeroControl | String | NotBlank |
| email | String | NotBlank, formato email |
| foto | String | Nullable |
| cargo | String | Nullable |
| carrera | Carrera | ManyToOne (id_carrera) |
| semestre | Semestre | ManyToOne (id_semestre) |
| activo | Integer | |
| fechaRegistro | Date | CreationTimestamp |

Coordinador de una carrera que puede revisar reportes y evidencias.

---

## DTOs

### ResumenAsistenciaDTO

| Campo | Tipo | Descripcion |
|---|---|---|
| idTutorado | Integer | ID del tutorado |
| nombreTutorado | String | Nombre completo |
| totalSesiones | long | Sesiones en las que estuvo inscrito |
| asistenciasPresente | long | Sesiones donde presente=1 |
| asistenciasRecuperadas | long | Sesiones donde recuperada=1 |
| totalAcreditadas | long | presente + recuperadas |
| porcentaje | double | (totalAcreditadas / totalSesiones) x 100 |
| acreditado | boolean | porcentaje >= 80 |

---

### PublicDeteccionForm

Formulario público (sin autenticación) para que un alumno registre su propia detección de necesidades.

| Campo | Notas |
|---|---|
| idTutorado | ID del tutorado |
| idSesion | ID de la sesión |
| sexo | Género del alumno |
| (campos DeteccionNecesidades) | Todos los campos booleanos/texto de la entidad |

---

## Repositorios

Todos extienden `JpaRepository<Entity, Integer>`. Se listan los métodos personalizados más relevantes.

### ITutorRepository
- `findByNombreContainingIgnoreCaseAndActivo(...)` — búsqueda por nombre
- `findByNumeroControlContainingIgnoreCaseAndActivo(...)` — búsqueda por número de control
- `findByEmailContainingIgnoreCaseAndActivo(...)` — búsqueda por email
- `findByFechaRegistroBetween(...)` — búsqueda por rango de fecha

### ITutoradoRepository
- Similar a ITutorRepository más:
- `findByCarreraAndActivo(Carrera, Integer, Pageable)` — filtro por carrera
- `findByCarreraIdAndActivoLessThan(...)` — para validaciones de asignación

### IGrupoRepository
- `findBySemestreAndActivo(...)` — filtro por semestre
- `findByTutorAndActivo(...)` — filtro por tutor
- `findByCarreraAndActivo(...)` — filtro por carrera
- `findByTutorIsNullAndActivo(...)` — grupos sin tutor asignado (paginado)

### IGrupoTutoradoRepository
- `findByActivo(Integer)` — todos los activos
- `findByActivoAndGrupo(Integer, Grupo)` — tutorados de un grupo
- `findByActivoAndTutorado(Integer, Tutorado)` — grupos de un tutorado
- `existsByGrupoAndTutoradoAndActivo(...)` — validar asignación duplicada
- `countByTutoradoAndActivo(...)` — límite de 2 grupos por tutorado
- `countActivoByGrupo()` — @Query para contar alumnos por grupo
- `buscarHistorial(...)` — @Query paginado con filtros múltiples
- `findTutoradosDisponibles(idCarrera, idGrupo)` — tutorados elegibles para un grupo

### ISesionRepository
- `findByGrupoAndActivo(Grupo, Integer, Pageable)`
- `findBySemanaAndActivo(Integer, Integer, Pageable)`
- `findByEstatusRegistroAndActivo(String, Integer, Pageable)`
- `findByGrupoAndSemanaAndActivo(...)` — combinado grupo+semana
- `findByFechaRegistroRange(...)` — @Query por rango de fechas
- `findByTutorId(Integer, Pageable)` — @Query sesiones del tutor por su ID
- `findTopByGrupoAndActivoOrderBySemanaDesc(...)` — última sesión del grupo
- `findTopByGrupoAndEstatusRegistroAndActivoOrderBySemanaDesc(...)` — última sesión con estatus específico

### IAsistenciaRepository
- `findBySesion(Sesion)` — asistencias de una sesión
- `findByTutorado(Tutorado)` — historial de un tutorado
- `countByTutoradoAndPresente(Tutorado, Integer)` — contar presencias para el cálculo del 80%
- `countByTutoradoAndRecuperada(Tutorado, Integer)` — contar recuperadas
- `existsBySesionAndTutorado(...)` — evitar duplicados
- `findByFechaRegistroBetween(...)` — filtro por fecha
- `existsByTutoradoAndSesionGrupoSemestreId(...)` — @Query para validar existencia en semestre

### IDeteccionNecesidadesRepository
- `findByTutorado(Tutorado)`, `findBySesion(Sesion)`
- `findByAlgebra(Integer)`, `findByCalculo(Integer)`, `findByNecesidadEconomica(Integer)`, etc.
- `findByFechaRegistroBetween(...)`, `findByTutoradoAndSesion(...)`

### IReporteSesionRepository
- `findBySesion(Sesion)` — reporte 1:1 con sesión
- `findByEstatusRevision(String)` — filtrar por estado de revisión

### ICoordinadorCarreraRepository
- `findByCarreraAndActivo(...)`, `findByCarreraAndSemestreAndActivo(...)`

---

## Servicios

### TutorService / TutorServiceImpl
Gestión CRUD de tutores con búsqueda paginada.
- `guardarTutor(Tutor)` — persiste un nuevo tutor
- `actualizarTutor(Integer id, Tutor)` — actualiza campos del tutor por ID
- `obtenerTutor(Integer id)` — obtiene por ID
- `eliminarTutor(Integer id)` — soft delete (`activo = 0`)
- `obtenerTodosTutoresPaginado(Pageable)` — lista paginada
- `buscarPorNombre/NumeroControl/Email/Fecha(...)` — búsquedas filtradas paginadas

### TutoradoService / TutoradoServiceImpl
Gestión CRUD de tutorados, análogo a TutorService.
- Agrega: `buscarPorCarrera(Integer idCarrera, Pageable)`
- Agrega: `obtenerTodosTutorados()` — lista completa sin paginar (para dropdowns)

### GrupoService / GrupoServiceImpl
Gestión de grupos.
- `guardarGrupo(Grupo)`, `actualizarGrupo(Integer, Grupo)`, `obtenerGrupo(Integer)`, `eliminarGrupo(Integer)`
- `buscarPorSemestre(Integer, Pageable)`, `buscarPorTutor(Integer, Pageable)`, `buscarPorCarrera(Integer, Pageable)`
- `obtenerGruposSinTutorPage(Pageable)` — grupos disponibles para asignar tutor
- `asignarTutor(Integer idGrupo, Integer idTutor)` — asigna tutor al grupo
- `quitarTutor(Integer idGrupo)` — desvincula tutor del grupo

### GrupoTutoradoService / GrupoTutoradoServiceImpl
Gestión de asignaciones alumno-grupo.
- `asignarTutorados(Integer idGrupo, Integer[] idsTutorados)` — asigna múltiples alumnos validando límite de 2 grupos
- `eliminarTutoradoDeGrupo(Integer id)` — soft delete de asignación
- `buscarPorGrupo(Integer idGrupo)` — lista de GrupoTutorado activos del grupo
- `buscarTutoriasPorTutorado(Integer idTutorado)` — grupos activos de un alumno
- `obtenerTutoradosDisponibles(Integer idGrupo)` — alumnos elegibles para el grupo (misma carrera, menos de 2 grupos)
- `contarAlumnosPorGrupo()` — mapa `{idGrupo → cantidad}` para vistas de lista
- `buscarHistorial(q, idSemestre, idCarrera, idGrupo, page, pageSize)` — historial paginado con filtros

### SesionService / SesionServiceImpl
Gestión de sesiones.
- `guardarSesion(Sesion)`, `actualizarSesion(Integer, Sesion)`, `obtenerSesion(Integer)`, `eliminarSesion(Integer)`
- `obtenerTodasSesiones()` — lista completa sin paginar
- `buscarSesionesPorGrupoPage(Integer, Pageable)`
- `buscarSesionesPorTutorPage(Integer, Pageable)` — @Query por ID del tutor
- `buscarSesionesPorSemanaPage(Integer, Pageable)`
- `buscarSesionesPorEstatusPage(String, Pageable)`
- `buscarSesionesPorGrupoYSemanaPage(Integer, Integer, Pageable)`
- `buscarSesionesPorFechaRegistroPage(Date, Date, Pageable)`

### AsistenciaService / AsistenciaServiceImpl
Gestión de asistencia con cálculo del umbral del 80%.
- `guardarAsistencia(Asistencia)`, `actualizarAsistencia(Integer, Asistencia)`, `eliminarAsistencia(Integer)`
- `registrarAsistenciaMasiva(Integer idSesion, Integer[] idsTutoradosPresentes)` — registra presencia para múltiples alumnos; los no incluidos se marcan como ausentes
- `buscarAsistenciasPorSesion(Integer idSesion)` — todas las asistencias de una sesión
- `buscarAsistenciasPorTutorado(Integer idTutorado)` — historial completo del alumno
- `calcularResumenAsistencia(Integer idTutorado)` → `ResumenAsistenciaDTO` — calcula total de sesiones, presencias, recuperadas, porcentaje y si acredita (≥80%)
- `obtenerTodasAsistenciasPage(Pageable)`, `buscarAsistenciasPorFechaRegistro(Date, Date)`

### DeteccionNecesidadesService / DeteccionNecesidadesServiceImpl
Gestión de formularios de detección.
- `guardarDeteccion(DeteccionNecesidades)`, `actualizarDeteccion(Integer, DeteccionNecesidades)`, `eliminarDeteccion(Integer)`
- `buscarPorTutorado(Integer)`, `buscarPorSesion(Integer)`
- `buscarPorNecesidadAlgebra/Calculo/Economica/Psicologica(Integer)` — filtros por tipo de necesidad
- `buscarPorFechaRegistro(Date, Date)`
- `obtenerTodasDeteccionesPage(Pageable)`

### CarreraService / CarreraServiceImpl
CRUD básico de carreras.
- `obtenerTodasCarreras()`, `guardarCarrera(Carrera)`, `actualizarCarrera(Integer, Carrera)`, `obtenerCarrera(Integer)`, `eliminarCarrera(Integer)`

### SemestreService / SemestreServiceImpl
CRUD básico de semestres, análogo a CarreraService.

### ActividadService / ActividadServiceImpl
Gestión de actividades con carga masiva.
- CRUD estándar más:
- `buscarActividadesPorFecha(LocalDate)`, `buscarActividadesPorRangoFechas(LocalDate, LocalDate)`
- `buscarPorPAT(Integer idPat)` — actividades de un PAT específico
- `guardarLoteActividades(List<Actividad>)` — carga masiva desde el constructor de PAT

### PATService / PATServiceImpl
Gestión de Planes de Acción Tutorial.
- `obtenerTodosPAT()`, `obtenerPATGenerales()` — distingue entre generales y por carrera
- `buscarPATporCarreraYSemestre(Integer, Integer)`
- CRUD estándar

### EvidenciaSesionService / EvidenciaSesionServiceImpl
Gestión de evidencias de sesión con flujo de validación.
- CRUD estándar más:
- `validarEvidencia(Integer id, String notasCoordinador)` — cambia estatus a VALIDADA
- `rechazarEvidencia(Integer id, String notasCoordinador)` — cambia estatus a RECHAZADA
- `buscarEvidenciasPorSesion(Integer idSesion)`

### ReporteSesionService / ReporteSesionServiceImpl
Gestión de reportes de sesión.
- `obtenerReportePorSesion(Integer idSesion)` — relación 1:1
- `buscarPorEstatus(String)` — filtra por `estatusRevision`
- CRUD estándar

### CoordinadorCarreraService / CoordinadorCarreraServiceImpl
Gestión de coordinadores de carrera.
- `buscarPorCarrera(Integer)`, `buscarPorCarreraYSemestre(Integer, Integer)`
- CRUD estándar

### Servicios PDF

#### CarnetPdfService / CarnetPdfServiceImpl
Genera el carnet de identificación de un tutorado en PDF (LETTER landscape, 3 columnas).
- `generarCarnet(Integer idTutorado)` → `byte[]`
- Incluye foto del alumno, datos personales, carrera, código.

#### ConstanciaTutorPdfService / ConstanciaTutorPdfServiceImpl
Genera constancia "Hace Constar" del tutor en PDF (LETTER portrait, 3 columnas de firma).
- `generarConstancia(Integer idTutor, Integer idSemestre)` → `byte[]`
- Nombres institucionales hardcodeados: Jefa DDA = ADRIANA MALDONADO BRAVO, Subdirector = SERGIO RICARDO ZAGAL BARRERA, CIT = SUSANA PINEDA MILLÁN. Código CCP: SRZB/AMB/spm.

#### ConstanciaTutoradoPdfService / ConstanciaTutoradoPdfServiceImpl
Genera el Anexo XVI (Constancia de Cumplimiento de Actividad Complementaria) en PDF (LETTER portrait).
- `generarConstancia(Integer idTutorado, Integer idSemestre)` → `byte[]`
- Calcula nivel de desempeño por porcentaje de asistencia.
- Nombres institucionales hardcodeados: Jefa DDA = ADRIANA MALDONADO BRAVO, Subdirector = SERGIO RICARDO ZAGAL BARRERA, Jefa Servicios Escolares = MARGARITA ALCOCER SOLACHE. Código CCP: SRZB/AMB/cacc.

#### DeteccionPdfService / DeteccionPdfServiceImpl
Genera reporte PDF de una detección de necesidades.
- `generarPdf(Integer idDeteccion)` → `byte[]`

#### ReporteSesionPdfService / ReporteSesionPdfServiceImpl
Genera el Reporte de Sesión (Anexo 19) en PDF (LETTER portrait).
- `generarPdf(Integer idReporte)` → `byte[]`
- Incluye: encabezado institucional, datos de sesión, datos del tutor/grupo, descripción de actividad, tabla de asistencia con estadísticas, observaciones y bloque de firmas (Tutor + Coordinador/a de Carrera).

#### FileStoreService / FileStoreServiceImpl
Servicio de almacenamiento de archivos (fotos, evidencias).
- `guardar(MultipartFile, FileType)` → `String` (nombre del archivo guardado)
- `eliminar(String nombre, FileType)` — borra el archivo del disco
- FileType enum: `TUTOR`, `TUTORADO`, `PAT`, `ACTIVIDAD`, `GRUPO`, `EVIDENCIA`, `COORDINADOR`

---

## Módulos — Rutas y Atributos de Modelo

---

### Dashboard (`/`)
**Controlador:** `MainController`  
**Vista:** `index.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| totalTutores | int | Conteo total de tutores |
| totalTutorados | int | Conteo total de tutorados |
| totalActividades | int | Conteo total de actividades |
| totalGrupos | int | Conteo total de grupos |
| proximasActividades | List\<Actividad\> | Proximas 5 actividades ordenadas por fecha |
| msg_error | String | Mensaje de error si el dashboard falla |

---

### Modulo Tutor (`/tutor`)
**Controlador:** `TutorController`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/tutor` | Lista paginada con filtros: nombre, numeroControl, correo, tipoBusqueda, fechaInicio/fechaFin |
| GET | `/tutor/agregar` | Formulario vacío para nuevo tutor |
| POST | `/tutor/guardar` | Persiste nuevo tutor; sube foto si se adjunta |
| GET | `/tutor/ver/{id}` | Detalle del tutor: grupos, conteo de alumnos y sesiones |
| GET | `/tutor/actualizar/{id}` | Formulario pre-cargado para edición |
| POST | `/tutor/actualizar/{id}` | Actualiza tutor; reemplaza foto si se sube una nueva |
| GET | `/tutor/delete/{id}` | Pantalla de confirmación de eliminación |
| POST | `/tutor/confirm/delete/{id}` | Soft delete del tutor |
| GET | `/tutor/pdf/constancia/{id}` | Descarga PDF de constancia (requiere `?idSemestre=`) |

#### Lista — `GET /tutor`
**Vista:** `tutor/viewListaTutor.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| tutores | List\<Tutor\> | Pagina actual de tutores |
| paginaActual | int | Numero de pagina (0-indexed) |
| totalElementos | long | Total de registros |
| totalPaginas | int | Total de paginas |
| pageSize | int | Elementos por pagina |
| sortBy | String | Campo de ordenacion (`id`, `nombre`, `numeroControl`) |
| sort | String | Direccion (`asc`/`desc`) |
| mapSort | Map\<String,String\> | Opciones de ordenacion para el select |
| tipoBusqueda | String | Tipo de filtro activo (`todos`, `nombre`) |
| q | String | Texto de busqueda |
| filtro | String | Descripcion del filtro activo (null si ninguno) |
| msg_error | String | Error de busqueda |

#### Ver detalle — `GET /tutor/ver/{id}`
**Vista:** `tutor/viewInfoTutor.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| tutor | Tutor | Entidad completa |
| grupos | List\<Grupo\> | Grupos asignados al tutor |
| alumnosPorGrupo | Map\<Integer,Long\> | Mapa grupoId -> cantidad de tutorados |
| sesionesPorGrupo | Map\<Integer,Long\> | Mapa grupoId -> cantidad de sesiones |
| totalAlumnos | long | Suma total de tutorados en todos sus grupos |
| totalSesiones | long | Suma total de sesiones en todos sus grupos |

---

### Modulo Tutorado (`/tutorado`)
**Controlador:** `TutoradoController`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/tutorado` | Lista paginada con filtros: nombre, numeroControl, correo, carrera, fecha |
| GET | `/tutorado/agregar` | Formulario con dropdown de carreras |
| POST | `/tutorado/guardar` | Persiste tutorado con foto opcional |
| GET | `/tutorado/ver/{id}` | Detalle: grupos activos, historial de detecciones, resumen de asistencia |
| GET | `/tutorado/actualizar/{id}` | Formulario de edición |
| POST | `/tutorado/actualizar/{id}` | Actualiza tutorado |
| GET | `/tutorado/delete/{id}` | Confirmación de eliminación |
| POST | `/tutorado/confirm/delete/{id}` | Soft delete |
| GET | `/tutorado/pdf/carnet/{id}` | Descarga carnet en PDF |
| GET | `/tutorado/pdf/constancia/{id}` | Descarga constancia en PDF (requiere `?idSemestre=`) |

#### Lista — `GET /tutorado`
**Vista:** `tutorado/viewListaTutorado.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| tutorados | List\<Tutorado\> | Pagina actual |
| paginaActual | int | |
| totalPaginas | int | |
| totalElementos | long | |
| pageSize | int | |
| carreras | List\<Carrera\> | Para filtro por carrera |
| idCarreraSeleccionada | Integer | Carrera seleccionada en el filtro |
| sortBy | String | Campo de ordenacion |
| sort | String | Direccion |
| mapSort | Map\<String,String\> | Opciones de ordenacion |
| tipoBusqueda | String | `todos` / `nombre` |
| q | String | Texto de busqueda |
| filtro | String | Descripcion del filtro activo |

#### Ver detalle — `GET /tutorado/ver/{id}`
**Vista:** `tutorado/viewInfoTutorado.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| tutorado | Tutorado | Entidad completa |
| gruposTutorado | List\<GrupoTutorado\> | Grupos a los que pertenece |
| detecciones | List\<DeteccionNecesidades\> | Historial de detecciones del tutorado |
| resumen | ResumenAsistenciaDTO | Resumen de asistencia calculado |

---

### Modulo Grupo (`/grupo`)
**Controlador:** `GrupoController`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/grupo` | Lista paginada con filtros: semestre, tutor, carrera, nombre, fecha |
| GET | `/grupo/agregar` | Formulario con dropdowns de semestres y carreras |
| POST | `/grupo/guardar` | Persiste grupo |
| GET | `/grupo/ver/{id}` | Detalle: lista de tutorados, sesiones del grupo |
| GET | `/grupo/actualizar/{id}` | Formulario de edición |
| POST | `/grupo/actualizar/{id}` | Actualiza grupo |
| GET | `/grupo/delete/{id}` | Confirmación |
| POST | `/grupo/confirm/delete/{id}` | Soft delete |
| GET | `/grupo/asignar/{idGrupo}` | Lista de tutorados disponibles para asignar |
| POST | `/grupo/asignar/{idGrupo}` | Asigna los tutorados seleccionados |
| POST | `/grupo/tutorado/quitar/{id}` | Elimina asignación GrupoTutorado por ID |
| GET | `/grupo/asignar-tutor` | Lista de grupos sin tutor con filtros |
| GET | `/grupo/asignar-tutor/{idGrupo}` | Formulario para seleccionar tutor |
| POST | `/grupo/asignar-tutor/{idGrupo}` | Guarda asignación de tutor |
| POST | `/grupo/quitar-tutor/{idGrupo}` | Desvincula tutor del grupo |

#### Lista — `GET /grupo`
**Vista:** `grupo/viewListaGrupo.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| grupos | List\<Grupo\> | Pagina actual |
| paginaActual | int | |
| totalPaginas | int | |
| totalElementos | long | |
| pageSize | int | |
| sort / sortBy | String | Ordenacion (campos validos: `id`, `nombre`) |
| tutores | List\<Tutor\> | Para filtro por tutor |
| semestres | List\<Semestre\> | Para filtro por semestre |
| carreras | List\<Carrera\> | Para filtro por carrera |
| idSemestreSeleccionado | Integer | |
| idTutorSeleccionado | Integer | |
| idCarreraSeleccionada | Integer | |
| q | String | Busqueda por nombre |
| conteoAlumnos | Map\<Integer,Long\> | grupoId -> cantidad de tutorados |
| tipoBusqueda | String | `todos` / `nombre` / `semestre` / `tutorSemestre` / `carreraSemestre` |
| filtro | String | |

#### Ver detalle — `GET /grupo/ver/{id}`
**Vista:** `grupo/viewInfoGrupo.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| grupo | Grupo | Entidad con tutor, semestre y carrera |
| tutorados | List\<GrupoTutorado\> | Tutorados asignados al grupo |
| sesiones | List\<Sesion\> | Sesiones del grupo (con actividad embebida) |

#### Vista Asignar Tutor — `GET /grupo/asignar-tutor`
**Vista:** `grupo/viewAsignarTutor.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| grupos | List\<Grupo\> | Grupos paginados (por defecto sin tutor) |
| paginaActual / totalPaginas / totalElementos / pageSize | | Paginacion |
| sort / sortBy | String | |
| tipoBusqueda | String | `sinTutor` / `sinTutorNombre` / `sinTutorSemestre` / `sinTutorCarrera` / `todos` |
| q | String | |
| semestres | List\<Semestre\> | |
| carreras | List\<Carrera\> | |
| idSemestreSeleccionado / idCarreraSeleccionada | Integer | |
| conteoAlumnos | Map\<Integer,Long\> | |

---

### Modulo Sesion (`/sesion`)
**Controlador:** `SesionController`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/sesion` | Lista paginada con filtros: grupo, tutor, semana, estatus, fecha |
| GET | `/sesion/agregar` | Formulario con dropdowns de grupos y actividades |
| POST | `/sesion/guardar` | Persiste sesión; valida que tenga grupo |
| GET | `/sesion/ver/{id}` | Detalle: info de la sesión + reporte asociado si existe |
| GET | `/sesion/actualizar/{id}` | Formulario de edición |
| POST | `/sesion/actualizar/{id}` | Actualiza sesión |
| GET | `/sesion/delete/{id}` | Confirmación |
| POST | `/sesion/confirm/delete/{id}` | Soft delete |

#### Lista — `GET /sesion`
**Vista:** `sesion/viewListaSesion.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| sesiones | List\<Sesion\> | Pagina actual |
| paginaActual / totalPaginas / totalElementos / pageSize | | |
| sort / sortBy | String | Campos validos: `id`, `semana` |
| grupos | List\<Grupo\> | Para filtro |
| idGrupoSeleccionado | Integer | |
| semanaSeleccionada | Integer | |
| estatusSeleccionado | String | |
| tipoBusqueda | String | `todos` / `grupo` / `semana` / `grupoSemana` / `estatus` |
| filtro | String | |

#### Ver detalle — `GET /sesion/ver/{id}`
**Vista:** `sesion/viewInfoSesion.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| sesion | Sesion | Entidad con grupo y actividad |
| reporte | ReporteSesion | Reporte asociado a la sesion (puede ser null) |

---

### Modulo PAT (`/pat`)
**Controlador:** `PATController`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/pat` | Lista paginada con filtros: semestre, carrera, soloGenerales |
| GET | `/pat/agregar` | Formulario con selects de semestre y carrera |
| POST | `/pat/guardar` | Persiste PAT con foto opcional |
| GET | `/pat/ver/{id}` | Detalle del PAT |
| GET | `/pat/actualizar/{id}` | Formulario de edición |
| POST | `/pat/actualizar/{id}` | Actualiza PAT |
| GET | `/pat/delete/{id}` | Confirmación |
| POST | `/pat/confirm/delete/{id}` | Soft delete |

#### Lista — `GET /pat`
**Vista:** `pat/viewListaPAT.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| pats | List\<PAT\> | Pagina actual |
| paginaActual / totalPaginas / totalElementos / pageSize | | |
| carreras | List\<Carrera\> | Para filtro |
| semestres | List\<Semestre\> | Para filtro |
| idCarreraSeleccionada / idSemestreSeleccionado | Integer | |
| soloGenerales | Boolean | Filtro de solo PAT generales |
| sortBy / sort / mapSort | | Ordenacion |
| filtro | String | |

---

### Modulo Actividad (`/actividad`)
**Controlador:** `ActividadController`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/actividad` | Lista con filtros: nombre, fecha, rango, PAT |
| GET | `/actividad/agregar` | Builder de lote de actividades vinculado a PAT |
| POST | `/actividad/guardar` | Persiste actividad individual |
| GET | `/actividad/ver/{id}` | Detalle de actividad con foto |
| GET | `/actividad/actualizar/{id}` | Formulario de edición |
| POST | `/actividad/actualizar/{id}` | Actualiza actividad |
| GET | `/actividad/delete/{id}` | Confirmación |
| POST | `/actividad/confirm/delete/{id}` | Soft delete |
| GET | `/actividad/api/por-pat/{idPat}` | API REST — lista de actividades de un PAT (JSON) |
| POST | `/actividad/api/guardar-lote` | API REST — carga masiva; devuelve resultado parcial si hay errores |
| PUT | `/actividad/api/actualizar/{id}` | API REST — actualización AJAX de actividad |
| DELETE | `/actividad/api/eliminar/{id}` | API REST — eliminación AJAX de actividad |

#### Lista — `GET /actividad`
**Vista:** `actividad/viewListaActividad.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| actividades | List\<Actividad\> | Pagina actual |
| paginaActual / totalPaginas / totalElementos / pageSize | | |
| pats | List\<PAT\> | Para filtro por PAT |
| idPatSeleccionado | Integer | |
| fechaSeleccionada / fechaInicio / fechaFin | String | Para filtros de fecha |
| tipoBusqueda | String | `todos` / `nombre` / `fecha` / `rango` / `pat` |
| q | String | |
| sortBy / sort / mapSort | | Ordenacion |
| filtro | String | |

---

### Modulo Asistencia (`/asistencia`)
**Controlador:** `AsistenciaController`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/asistencia` | Lista paginada con filtros: sesion, tutorado, fecha |
| GET | `/asistencia/registrar/{idSesion}` | Vista de registro masivo con tabla de tutorados del grupo |
| POST | `/asistencia/registrar/{idSesion}` | Guarda asistencia masiva; retorna a vista de sesión |
| GET | `/asistencia/resumen/{idTutorado}` | ResumenAsistenciaDTO + historial completo del alumno |
| GET | `/asistencia/agregar` | Formulario individual con dropdowns |
| POST | `/asistencia/guardar` | Persiste asistencia individual |
| GET | `/asistencia/actualizar/{id}` | Formulario de edición |
| POST | `/asistencia/actualizar/{id}` | Actualiza asistencia |
| POST | `/asistencia/recuperar/{id}` | Marca asistencia como recuperada (solo si `presente == 0`) |
| GET | `/asistencia/delete/{id}` | Confirmación |
| POST | `/asistencia/confirm/delete/{id}` | Elimina asistencia |

#### Registrar Asistencia Masiva — `GET /asistencia/registrar/{idSesion}`
**Vista:** `asistencia/viewRegistrarAsistencia.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| sesion | Sesion | Sesion a la que se registra asistencia |
| grupoTutorados | List\<GrupoTutorado\> | Tutorados del grupo de la sesion |
| mapaAsistencias | Map\<Integer,Asistencia\> | tutoradoId -> asistencia existente |

#### Resumen de Asistencia — `GET /asistencia/resumen/{idTutorado}`
**Vista:** `asistencia/viewResumenAsistencia.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| resumen | ResumenAsistenciaDTO | Estadisticas calculadas |
| historial | List\<Asistencia\> | Registro detallado sesion por sesion |
| msg_error | String | Si hay error en el calculo |

---

### Modulo Deteccion de Necesidades (`/deteccion`)
**Controlador:** `DeteccionNecesidadesController`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/deteccion` | Lista paginada con filtros: tutorado, sesion, tipo de necesidad, fecha |
| GET | `/deteccion/agregar` | Formulario; acepta `?idTutorado=` e `?idSesion=` para pre-cargar |
| POST | `/deteccion/guardar` | Persiste detección |
| GET | `/deteccion/ver/{id}` | Detalle de la detección |
| GET | `/deteccion/actualizar/{id}` | Formulario de edición |
| POST | `/deteccion/actualizar/{id}` | Actualiza detección |
| GET | `/deteccion/delete/{id}` | Confirmación |
| POST | `/deteccion/confirm/delete/{id}` | Soft delete |
| GET | `/deteccion/pdf/{id}` | Descarga PDF de la detección |

#### Lista — `GET /deteccion`
**Vista:** `deteccion/viewListaDeteccion.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| detecciones | List\<DeteccionNecesidades\> | Pagina actual |
| paginaActual / totalPaginas / totalElementos / pageSize | | |
| sort / sortBy | String | |
| tutorados | List\<Tutorado\> | Para filtro |
| sesiones | List\<Sesion\> | Para filtro |
| idTutoradoSeleccionado / idSesionSeleccionada | Integer | |
| tipoBusqueda | String | `todos` / `tutorado` / `sesion` / `necesidad` |
| filtro | String | |

---

### Modulo Evidencia de Sesion (`/evidencia`)
**Controlador:** `EvidenciaSesionController`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/evidencia` | Lista paginada con filtros: sesion |
| GET | `/evidencia/agregar` | Formulario; acepta `?idSesion=` para prellenar |
| POST | `/evidencia/guardar` | Persiste evidencia con archivo adjunto |
| GET | `/evidencia/ver/{id}` | Detalle con archivo y formulario de validación/rechazo |
| GET | `/evidencia/actualizar/{id}` | Formulario de edición |
| POST | `/evidencia/actualizar/{id}` | Actualiza evidencia |
| POST | `/evidencia/validar/{id}` | Cambia `estatusValidacion` a `VALIDADA`; acepta `?notas=` |
| POST | `/evidencia/rechazar/{id}` | Cambia `estatusValidacion` a `RECHAZADA`; acepta `?notas=` |
| GET | `/evidencia/delete/{id}` | Confirmación |
| POST | `/evidencia/confirm/delete/{id}` | Soft delete |

---

### Modulo Reporte de Sesion (`/reporte`)
**Controlador:** `ReporteSesionController`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/reporte` | Lista paginada con filtros: estatus |
| GET | `/reporte/agregar` | Formulario; acepta `?idSesion=` para prellenar |
| POST | `/reporte/guardar` | Persiste reporte |
| GET | `/reporte/ver/{id}` | Detalle del reporte |
| GET | `/reporte/sesion/{idSesion}` | Redirige a `agregar?idSesion=` si no existe reporte para esa sesión |
| GET | `/reporte/actualizar/{id}` | Formulario de edición |
| POST | `/reporte/actualizar/{id}` | Actualiza reporte |
| GET | `/reporte/pdf/{id}` | Genera Anexo 19 en PDF |
| GET | `/reporte/delete/{id}` | Confirmación |
| POST | `/reporte/confirm/delete/{id}` | Soft delete |

#### Lista — `GET /reporte`
**Vista:** `reporte/viewListaReporte.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| reportes | List\<ReporteSesion\> | Pagina actual |
| paginaActual / totalPaginas / totalElementos / pageSize | | |
| sort / sortBy | String | |
| estatusSeleccionado | String | Filtro de estatus |
| tipoBusqueda | String | `todos` / `estatus` |
| filtro | String | |

---

### Modulo Semestre (`/semestre`)
**Controlador:** `SemestreController`

CRUD estándar. Rutas: `/semestre`, `/semestre/agregar`, `/semestre/guardar`, `/semestre/ver/{id}`, `/semestre/actualizar/{id}`, `/semestre/delete/{id}`, `/semestre/confirm/delete/{id}`.

#### Lista — `GET /semestre`
**Vista:** `semestre/viewListaSemestre.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| semestres | List\<Semestre\> | Pagina actual |
| paginaActual / totalPaginas / totalElementos / pageSize | | |
| sort / sortBy | String | Campos validos: `id`, `anio`, `periodo` |

---

### Modulo Carrera (`/carrera`)
**Controlador:** `CarreraController`

CRUD estándar. Rutas: `/carrera`, `/carrera/agregar`, `/carrera/guardar`, `/carrera/ver/{id}`, `/carrera/actualizar/{id}`, `/carrera/delete/{id}`, `/carrera/confirm/delete/{id}`.

#### Lista — `GET /carrera`
**Vista:** `carrera/viewListaCarrera.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| carreras | List\<Carrera\> | Pagina actual |
| paginaActual / totalPaginas / totalElementos / pageSize | | |
| sort / sortBy | String | Campos validos: `id`, `nombre`, `clave` |

---

### Modulo Coordinador de Carrera (`/coordinador`)
**Controlador:** `CoordinadorCarreraController`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/coordinador` | Lista paginada con filtros: nombre, carrera, semestre |
| GET | `/coordinador/agregar` | Formulario con selects de carrera y semestre |
| POST | `/coordinador/guardar` | Persiste coordinador con foto opcional |
| GET | `/coordinador/ver/{id}` | Detalle del coordinador |
| GET | `/coordinador/actualizar/{id}` | Formulario de edición |
| POST | `/coordinador/actualizar/{id}` | Actualiza coordinador |
| GET | `/coordinador/delete/{id}` | Confirmación |
| POST | `/coordinador/confirm/delete/{id}` | Soft delete |

#### Lista — `GET /coordinador`
**Vista:** `coordinador/viewListaCoordinador.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| coordinadores | List\<CoordinadorCarrera\> | Pagina actual |
| paginaActual / totalPaginas / totalElementos / pageSize | | |
| sort / sortBy | String | Campos validos: `id`, `nombre` |
| carreras | List\<Carrera\> | Para filtro |
| semestres | List\<Semestre\> | Para filtro |
| idCarreraSeleccionada / idSemestreSeleccionado | Integer | |
| tipoBusqueda | String | `todos` / `nombre` / `carrera` / `semestre` / `carreraSemestre` |
| q | String | |
| filtro | String | |

---

### Modulo Historial de Tutorias (`/historial`)
**Controlador:** `HistorialController`

#### Vista unica — `GET /historial`
**Vista:** `historial/viewHistorialTutorias.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| historial | List\<GrupoTutorado\> | Registros de asignacion paginados |
| totalPaginas / totalElementos / paginaActual / pageSize | | |
| q | String | Busqueda por nombre de tutorado o tutor |
| idSemestreSeleccionado / idCarreraSeleccionada | Integer | Filtros |
| semestres | List\<Semestre\> | Para el select de filtro |
| carreras | List\<Carrera\> | Para el select de filtro |

---

### PublicController (`/public`)
**Controlador:** `PublicController`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/public/deteccion` | Landing page del flujo público; el alumno ingresa su número de control |
| GET | `/public/deteccion/form` | Formulario completo de detección de necesidades (sin sidebar) |
| POST | `/public/deteccion/form` | Guarda la detección pública |
| GET | `/public/deteccion/confirmacion` | Pantalla de éxito tras enviar la detección |
| GET | `/public/actividades` | Listado público de actividades del PAT activo |

---

### ApiSearchController (`/api/search`)
**Controlador:** `ApiSearchController`

Endpoints REST de búsqueda para autocompletar en formularios (tutorados, tutores, grupos).

---

## Vistas (Thymeleaf)

Todas las vistas usan Bootstrap 5 con un layout de sidebar (`fragments/fragment.html`).

### Convenciones de nombrado
- `viewLista*.html` — tabla paginada con filtros en el header, paginación en el footer.
- `viewForm*.html` — formulario de alta/edición; el flag `isEdit` controla el título y la acción del form.
- `viewInfo*.html` — detalle de un registro con cards y badges de estado.
- `viewConfirmDelete*.html` — confirmación simple con botón cancelar y confirmar.

### Fragmentos reutilizables (`fragments/fragment.html`)
- `::sidebar` — barra lateral de navegación con links a todas las secciones.
- `::footer` — pie de página.

---

### Tutor (`/tutor/`)
| Archivo | Descripción |
|---|---|
| `viewListaTutor.html` | Tabla de tutores con buscador multi-tipo y paginación. |
| `viewFormTutor.html` | Alta/edición de tutor con upload de foto. |
| `viewInfoTutor.html` | Detalle: tarjeta con datos, lista de grupos con conteo de alumnos, últimas sesiones del semestre activo. |
| `viewConfirmDeleteTutor.html` | Confirmación de eliminación. |

### Tutorado (`/tutorado/`)
| Archivo | Descripción |
|---|---|
| `viewListaTutorado.html` | Tabla con filtro de carrera y búsqueda por nombre/número. |
| `viewFormTutorado.html` | Alta/edición con dropdowns de carrera y upload de foto. |
| `viewInfoTutorado.html` | Detalle: datos personales, grupos activos, detecciones recientes, resumen de asistencia con barra de progreso al 80%. |
| `viewConfirmDeleteTutorado.html` | Confirmación de eliminación. |

### Grupo (`/grupo/`)
| Archivo | Descripción |
|---|---|
| `viewListaGrupo.html` | Tabla con filtros de semestre, tutor y carrera. Muestra conteo de alumnos por grupo. |
| `viewFormGrupo.html` | Alta/edición con selects de semestre, tutor y carrera. |
| `viewInfoGrupo.html` | Detalle: info del grupo, tabla de tutorados asignados con botón de quitar, lista de sesiones. |
| `viewAsignarTutorados.html` | Lista de tutorados disponibles (misma carrera, menos de 2 grupos) con checkboxes para selección masiva. |
| `viewAsignarTutor.html` | Lista de grupos sin tutor con filtros. |
| `viewFormAsignarTutor.html` | Formulario para seleccionar el tutor a asignar. |
| `viewConfirmDeleteGrupo.html` | Confirmación de eliminación. |

### Sesion (`/sesion/`)
| Archivo | Descripción |
|---|---|
| `viewListaSesion.html` | Tabla con filtros de grupo, tutor, semana y estatus. Badges de color por estatus. |
| `viewFormSesion.html` | Alta/edición con selects de grupo y actividad. |
| `viewInfoSesion.html` | Detalle: info de la sesión, card de reporte si existe, botón para ir a registro de asistencia. |
| `viewConfirmDeleteSesion.html` | Confirmación de eliminación. |

### Asistencia (`/asistencia/`)
| Archivo | Descripción |
|---|---|
| `viewListaAsistencia.html` | Tabla de asistencias con filtros de sesión, tutorado y fecha. |
| `viewRegistrarAsistencia.html` | Registro masivo: info de la sesión, tabla de tutorados del grupo con checkboxes (deshabilitado si ya tiene asistencia registrada), badges de estado. Botones "Marcar todos / Desmarcar todos". |
| `viewResumenAsistencia.html` | Resumen con porcentaje, barra de progreso, badge "Acredita / No acredita" y tabla del historial completo. |
| `viewFormAsistencia.html` | Alta/edición individual con selects de sesión y tutorado. |
| `viewConfirmDeleteAsistencia.html` | Confirmación de eliminación. |

### Deteccion (`/deteccion/`)
| Archivo | Descripción |
|---|---|
| `viewListaDeteccion.html` | Tabla con filtros por tutorado, sesión y tipo de necesidad. |
| `viewFormDeteccion.html` | Formulario completo: checkboxes por categoría (académica, económica, psicológica, apoyos), campo de observaciones. Se puede pre-cargar con tutorado y sesión desde la URL. |
| `viewInfoDeteccion.html` | Vista de solo lectura de la detección. |
| `viewConfirmDeleteDeteccion.html` | Confirmación de eliminación. |

### Carrera, Semestre (`/carrera/`, `/semestre/`)
CRUDs simples. Cada uno tiene lista, formulario de alta/edición, detalle y confirmación de eliminación.

### Actividad (`/actividad/`)
| Archivo | Descripción |
|---|---|
| `viewListaActividad.html` | Tabla con filtros de PAT, nombre, fecha. |
| `viewFormActividad.html` | Alta/edición individual. |
| `viewAgregarActividades.html` | Constructor masivo de actividades para un PAT: interfaz dinámica con JS para agregar/editar/eliminar actividades antes de guardar el lote. |
| `viewInfoActividad.html` | Detalle de actividad con foto si existe. |
| `viewConfirmDeleteActividad.html` | Confirmación de eliminación. |

### PAT (`/pat/`)
CRUD estándar. El formulario distingue entre PAT general (sin carrera) y PAT por carrera con select condicional.

### Evidencia (`/evidencia/`)
CRUD estándar. La vista de detalle muestra el archivo y el formulario de validación/rechazo con campo para notas del coordinador.

### Reporte (`/reporte/`)
CRUD estándar. La vista de detalle muestra datos del reporte con el estatus de revisión y permite edición.

### Coordinador (`/coordinador/`)
CRUD estándar con filtros por carrera y semestre.

### Historial (`/historial/`)
| Archivo | Descripción |
|---|---|
| `viewHistorialTutorias.html` | Historial paginado de asignaciones alumno-grupo con filtros de búsqueda, semestre, carrera y grupo. |

### Público (`/public/`)
| Archivo | Descripción |
|---|---|
| `viewPublicDeteccionInicio.html` | Landing page del flujo público; el alumno ingresa su número de control. |
| `viewPublicDeteccion.html` | Formulario completo de detección de necesidades (sin sidebar, diseño público). |
| `viewPublicDeteccionConfirmacion.html` | Pantalla de éxito tras enviar la detección. |
| `viewPublicActividades.html` | Listado público de actividades del PAT activo. |
| `viewPublicDetalleActividad.html` | Detalle público de una actividad. |

---

## Convenciones del Proyecto

### Mensajes Flash
Todas las operaciones de guardado/actualizacion/eliminacion usan `RedirectAttributes`:
- `msg_success` — operacion exitosa (verde)
- `msg_error` — error de operacion (rojo)

### Paginacion
La mayoria de listas soporta: `page`, `pageSize`, `sort` (`asc`/`desc`), `sortBy`.
Implementada con Spring Data `Pageable` + `Page<T>`.

### Subida de Archivos
Los modulos Tutor, Tutorado, PAT, Actividad, Coordinador y Evidencia aceptan un `MultipartFile` via `FileStoreService`. El tipo de carpeta lo determina el enum `FileType` (`TUTOR`, `TUTORADO`, `PAT`, `ACTIVIDAD`, `COORDINADOR`, `EVIDENCIA`).

### Busqueda por Tipo (`tipoBusqueda`)
El patron estandar en los listados es un `<select>` con `tipoBusqueda` + campos especificos que se activan segun la opcion seleccionada. El controlador evalua la combinacion y llama al metodo de servicio correspondiente.

### Estatus de Registros
- Sesion: `PENDIENTE` / `REALIZADA` / `CANCELADA`
- Evidencia: `PENDIENTE` / `VALIDADA` / `RECHAZADA`
- Campo `activo` = 1 en todos los registros activos (borrado logico preparado)

### Regla del 80% de Asistencia
Calculada en `AsistenciaServiceImpl.calcularResumenAsistencia(idTutorado)`.
Un tutorado acredita si `(presente + recuperadas) / totalSesiones >= 0.80`.

### Notas Thymeleaf — Errores Conocidos
- La variable de iteracion `gt` en `th:each` esta reservada por Thymeleaf como alias del operador `>`. Usar siempre nombres alternativos (`memb`, `item`, `elem`).
- Acceso a lista por indice: usar `#lists.get(lista, 0)` en lugar de `lista[0]`.
- Ternarios: siempre dentro de un solo `${}` — `${A == 'X' ? 'y' : 'z'}`.
- Proyecciones SpEL `lista.![campo]` solo pueden acceder a propiedades del objeto de la coleccion, no a variables del modelo externo.

---

## Flujo General del Sistema

```
1. Catálogos base
   Carrera + Semestre

2. Plan de Acción Tutorial
   PAT (general o por carrera) → Actividades (semanas 1-10)

3. Actores
   Tutor + Tutorado (con foto, carrera)

4. Grupos
   Grupo (tutor + semestre + carrera) → GrupoTutorado (asignar tutorados)

5. Sesiones
   Sesion (grupo + actividad + semana + estatus)
       → Asistencia masiva (registrar presencia por sesión)
       → DeteccionNecesidades (por tutorado + sesión)
       → EvidenciaSesion (adjuntos validados por coordinador)
       → ReporteSesion (1:1 con sesión)

6. Reportes y PDFs
   Carnet del tutorado
   Constancia del tutor (por semestre)
   Constancia del tutorado (por semestre)
   PDF de detección de necesidades
   PDF de reporte de sesión (Anexo 19)
   Resumen de asistencia (umbral 80%)
```

---

*Actualizado: 2026-05-13*
