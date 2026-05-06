# Sistema de Tutorías — Documentación del Proyecto

## Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Spring Boot 3.5.13 · Java 17 |
| Persistencia | Spring Data JPA · Hibernate · MySQL |
| Vistas | Thymeleaf 3.1.3 · Bootstrap 5.3 · Inter font |
| Validación | Jakarta Validation (Bean Validation 3) |
| Utilidades | Lombok · Spring DevTools · Spring Multipart |
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
| activo | Integer | 1=activo, 0=inactivo |
| fechaRegistro | Date | CreationTimestamp |

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
| activo | Integer | 1=activo |

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
| fecha | Date | NotNull |
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

> **Nota:** El campo `activo` fue eliminado de esta tabla. La unicidad se garantiza con constraint `uq_asistencia_sesion_tutorado (id_sesion, id_tutorado)`. Ejecutar migración: `ALTER TABLE asistencia DROP COLUMN activo; ALTER TABLE asistencia ADD CONSTRAINT uq_asistencia_sesion_tutorado UNIQUE (id_sesion, id_tutorado);`

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
| necesidadEconomica | Integer | 1=apoyo economico |
| necesidadPsicologica | Integer | 1=apoyo psicologico |
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
| estatusRevision | String | Estado de revision del reporte |
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

## Modulos — Rutas y Atributos de Modelo

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

#### Agregar — `GET /tutor/agregar`
**Vista:** `tutor/viewFormTutor.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| tutor | Tutor | Objeto nuevo vacio |
| isEdit | boolean | `false` |

#### Guardar — `POST /tutor/guardar`
Redirige a `/tutor`. En error vuelve al form con los mismos atributos mas `msg_error`.
Acepta `fotoFile` (MultipartFile) para subir foto.

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

#### Actualizar — `GET /tutor/actualizar/{id}`
**Vista:** `tutor/viewFormTutor.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| tutor | Tutor | Entidad cargada desde BD |
| isEdit | boolean | `true` |

#### Confirmar eliminar — `GET /tutor/delete/{id}`
**Vista:** `tutor/viewConfirmDeleteTutor.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| tutor | Tutor | Entidad a eliminar |

#### Eliminar — `POST /tutor/confirm/delete/{id}`
Redirige a `/tutor`.

---

### Modulo Tutorado (`/tutorado`)
**Controlador:** `TutoradoController`

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

#### Agregar — `GET /tutorado/agregar`
**Vista:** `tutorado/viewFormTutorado.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| tutorado | Tutorado | Objeto vacio |
| carreras | List\<Carrera\> | Para el select de carrera |
| isEdit | boolean | `false` |

#### Ver detalle — `GET /tutorado/ver/{id}`
**Vista:** `tutorado/viewInfoTutorado.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| tutorado | Tutorado | Entidad completa |
| gruposTutorado | List\<GrupoTutorado\> | Grupos a los que pertenece |
| detecciones | List\<DeteccionNecesidades\> | Historial de detecciones del tutorado |
| resumen | ResumenAsistenciaDTO | Resumen de asistencia calculado |

#### Actualizar — `GET /tutorado/actualizar/{id}`
**Vista:** `tutorado/viewFormTutorado.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| tutorado | Tutorado | Entidad cargada |
| carreras | List\<Carrera\> | Para el select |
| isEdit | boolean | `true` |

#### Confirmar eliminar — `GET /tutorado/delete/{id}`
**Vista:** `tutorado/viewConfirmDeleteTutorado.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| tutorado | Tutorado | Entidad a eliminar |

#### Carnet PDF — `GET /tutorado/pdf/carnet/{id}`
Genera y devuelve el carnet de asistencia del tutorado en PDF (LETTER landscape, 3 columnas).
Implementado en `CarnetPdfService` / `CarnetPdfServiceImpl`. Devuelve `ResponseEntity<byte[]>`.

---

### Modulo Grupo (`/grupo`)
**Controlador:** `GrupoController`

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

#### Agregar — `GET /grupo/agregar`
**Vista:** `grupo/viewFormGrupo.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| grupo | Grupo | Objeto vacio |
| semestres | List\<Semestre\> | |
| carreras | List\<Carrera\> | |
| isEdit | boolean | `false` |

#### Ver detalle — `GET /grupo/ver/{id}`
**Vista:** `grupo/viewInfoGrupo.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| grupo | Grupo | Entidad con tutor, semestre y carrera |
| tutorados | List\<GrupoTutorado\> | Tutorados asignados al grupo |
| sesiones | List\<Sesion\> | Sesiones del grupo (con actividad embebida) |

#### Actualizar — `GET /grupo/actualizar/{id}`
**Vista:** `grupo/viewFormGrupo.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| grupo | Grupo | Entidad cargada |
| semestres | List\<Semestre\> | |
| carreras | List\<Carrera\> | |
| isEdit | boolean | `true` |

#### Confirmar eliminar — `GET /grupo/delete/{id}`
**Vista:** `grupo/viewConfirmDeleteGrupo.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| grupo | Grupo | Entidad a eliminar |

#### Asignar Tutorados — `GET /grupo/asignar/{idGrupo}`
**Vista:** `grupo/viewAsignarTutorados.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| grupo | Grupo | Grupo destino |
| tutoradosDisponibles | List\<Tutorado\> | Tutorados sin asignacion a este grupo |

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

#### Form Asignar Tutor — `GET /grupo/asignar-tutor/{idGrupo}`
**Vista:** `grupo/viewFormAsignarTutor.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| grupo | Grupo | Grupo a asignar |
| tutores | List\<Tutor\> | Todos los tutores disponibles |

---

### Modulo Sesion (`/sesion`)
**Controlador:** `SesionController`

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

#### Agregar / Actualizar — `GET /sesion/agregar` y `GET /sesion/actualizar/{id}`
**Vista:** `sesion/viewFormSesion.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| sesion | Sesion | Nuevo o cargado |
| grupos | List\<Grupo\> | Para el select |
| actividades | List\<Actividad\> | Para el select |
| isEdit | boolean | `false` / `true` |

#### Ver detalle — `GET /sesion/ver/{id}`
**Vista:** `sesion/viewInfoSesion.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| sesion | Sesion | Entidad con grupo y actividad |

#### Confirmar eliminar — `GET /sesion/delete/{id}`
**Vista:** `sesion/viewConfirmDeleteSesion.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| sesion | Sesion | Entidad a eliminar |

---

### Modulo PAT (`/pat`)
**Controlador:** `PATController`

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

#### Agregar / Actualizar — `GET /pat/agregar` y `GET /pat/actualizar/{id}`
**Vista:** `pat/viewFormPAT.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| pat | PAT | Nuevo o cargado |
| carreras | List\<Carrera\> | |
| semestres | List\<Semestre\> | |
| isEdit | boolean | |

Acepta `fotoFile` (MultipartFile).

#### Ver detalle — `GET /pat/ver/{id}`
**Vista:** `pat/viewInfoPAT.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| pat | PAT | Entidad con semestre y carrera |

#### Confirmar eliminar — `GET /pat/delete/{id}`
**Vista:** `pat/viewConfirmDeletePAT.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| pat | PAT | Entidad a eliminar |

---

### Modulo Actividad (`/actividad`)
**Controlador:** `ActividadController`

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

#### Vista Agregar Lote — `GET /actividad/agregar`
**Vista:** `actividad/viewAgregarActividades.html`
*(Builder visual de actividades para un PAT)*

| Atributo | Tipo | Descripcion |
|---|---|---|
| pats | List\<PAT\> | Para el selector de PAT |
| preselectedPatId | Integer | PAT preseleccionado (desde URL) |

#### Form Actividad — `GET /actividad/actualizar/{id}`
**Vista:** `actividad/viewFormActividad.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| actividad | Actividad | Entidad cargada |
| pats | List\<PAT\> | Para el select |
| isEdit | boolean | `true` |

Acepta `fotoFile` (MultipartFile).

#### Ver detalle — `GET /actividad/ver/{id}`
**Vista:** `actividad/viewInfoActividad.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| actividad | Actividad | Entidad con PAT embebido |

#### Confirmar eliminar — `GET /actividad/delete/{id}`
**Vista:** `actividad/viewConfirmDeleteActividad.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| actividad | Actividad | Entidad a eliminar |

#### APIs REST (JSON) del Builder

| Ruta | Metodo | Descripcion |
|---|---|---|
| `/actividad/api/por-pat/{idPat}` | GET | Lista actividades de un PAT ordenadas por semana |
| `/actividad/api/guardar-lote` | POST | Guarda un lote de actividades para un PAT |
| `/actividad/api/actualizar/{id}` | PUT | Actualiza nombre/descripcion/semana de una actividad |
| `/actividad/api/eliminar/{id}` | DELETE | Elimina una actividad |

---

### Modulo Asistencia (`/asistencia`)
**Controlador:** `AsistenciaController`

#### Lista — `GET /asistencia`
**Vista:** `asistencia/viewListaAsistencia.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| asistencias | List\<Asistencia\> | Pagina actual |
| paginaActual / totalPaginas / totalElementos / pageSize | | |
| sort / sortBy | String | |
| sesiones | List\<Sesion\> | Para filtro |
| tutorados | List\<Tutorado\> | Para filtro |
| idSesionSeleccionada / idTutoradoSeleccionado | Integer | |
| tipoBusqueda | String | `todos` / `sesion` / `tutorado` |
| filtro | String | |

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

#### Agregar / Actualizar — `GET /asistencia/agregar` y `GET /asistencia/actualizar/{id}`
**Vista:** `asistencia/viewFormAsistencia.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| asistencia | Asistencia | Nueva o cargada |
| sesiones | List\<Sesion\> | Para el select |
| tutorados | List\<Tutorado\> | Para el select |
| isEdit | boolean | |

#### Confirmar eliminar — `GET /asistencia/delete/{id}`
**Vista:** `asistencia/viewConfirmDeleteAsistencia.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| asistencia | Asistencia | Entidad a eliminar |

---

### Modulo Deteccion de Necesidades (`/deteccion`)
**Controlador:** `DeteccionNecesidadesController`

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

#### Agregar / Actualizar — `GET /deteccion/agregar` y `GET /deteccion/actualizar/{id}`
**Vista:** `deteccion/viewFormDeteccion.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| deteccion | DeteccionNecesidades | Nueva (pre-llenada si viene de URL) o cargada |
| tutorados | List\<Tutorado\> | Para el select |
| sesiones | List\<Sesion\> | Para el select |
| isEdit | boolean | |

`agregar` acepta `?idTutorado=` y `?idSesion=` para prellenar.

#### Ver detalle — `GET /deteccion/ver/{id}`
**Vista:** `deteccion/viewInfoDeteccion.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| deteccion | DeteccionNecesidades | Entidad con tutorado y sesion |

#### Confirmar eliminar — `GET /deteccion/delete/{id}`
**Vista:** `deteccion/viewConfirmDeleteDeteccion.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| deteccion | DeteccionNecesidades | Entidad a eliminar |

---

### Modulo Evidencia de Sesion (`/evidencia`)
**Controlador:** `EvidenciaSesionController`

#### Lista — `GET /evidencia`
**Vista:** `evidencia/viewListaEvidencia.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| evidencias | List\<EvidenciaSesion\> | Pagina actual |
| paginaActual / totalPaginas / totalElementos / pageSize | | |
| sort / sortBy | String | |
| sesiones | List\<Sesion\> | Para filtro |
| idSesionSeleccionada | Integer | |
| tipoBusqueda | String | `todos` / `sesion` |
| filtro | String | |

#### Agregar / Actualizar — `GET /evidencia/agregar` y `GET /evidencia/actualizar/{id}`
**Vista:** `evidencia/viewFormEvidencia.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| evidencia | EvidenciaSesion | Nueva (prellenada si `?idSesion=`) o cargada |
| sesiones | List\<Sesion\> | Para el select |
| isEdit | boolean | |

Acepta `archivoFile` (MultipartFile).

#### Ver detalle — `GET /evidencia/ver/{id}`
**Vista:** `evidencia/viewInfoEvidencia.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| evidencia | EvidenciaSesion | Entidad completa |

Acciones adicionales desde esta vista:
- `POST /evidencia/validar/{id}` — cambia `estatusValidacion` a `VALIDADA`, acepta `?notas=`
- `POST /evidencia/rechazar/{id}` — cambia `estatusValidacion` a `RECHAZADA`, acepta `?notas=`

#### Confirmar eliminar — `GET /evidencia/delete/{id}`
**Vista:** `evidencia/viewConfirmDeleteEvidencia.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| evidencia | EvidenciaSesion | Entidad a eliminar |

---

### Modulo Reporte de Sesion (`/reporte`)
**Controlador:** `ReporteSesionController`

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

#### Agregar / Actualizar — `GET /reporte/agregar` y `GET /reporte/actualizar/{id}`
**Vista:** `reporte/viewFormReporte.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| reporte | ReporteSesion | Nuevo (prellenado si `?idSesion=`) o cargado |
| sesiones | List\<Sesion\> | Para el select |
| isEdit | boolean | |

#### Ver detalle — `GET /reporte/ver/{id}` y `GET /reporte/sesion/{idSesion}`
**Vista:** `reporte/viewInfoReporte.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| reporte | ReporteSesion | Entidad con sesion embebida |

`/reporte/sesion/{idSesion}` redirige a `agregar?idSesion=` si no existe reporte para esa sesion.

#### Confirmar eliminar — `GET /reporte/delete/{id}`
**Vista:** `reporte/viewConfirmDeleteReporte.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| reporte | ReporteSesion | Entidad a eliminar |

---

### Modulo Semestre (`/semestre`)
**Controlador:** `SemestreController`

#### Lista — `GET /semestre`
**Vista:** `semestre/viewListaSemestre.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| semestres | List\<Semestre\> | Pagina actual |
| paginaActual / totalPaginas / totalElementos / pageSize | | |
| sort / sortBy | String | Campos validos: `id`, `anio`, `periodo` |

#### Agregar / Actualizar — `GET /semestre/agregar` y `GET /semestre/actualizar/{id}`
**Vista:** `semestre/viewFormSemestre.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| semestre | Semestre | Nuevo o cargado |
| isEdit | boolean | |

#### Ver detalle — `GET /semestre/ver/{id}`
**Vista:** `semestre/viewInfoSemestre.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| semestre | Semestre | Entidad cargada |

#### Confirmar eliminar — `GET /semestre/delete/{id}`
**Vista:** `semestre/viewConfirmDeleteSemestre.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| semestre | Semestre | Entidad a eliminar |

---

### Modulo Carrera (`/carrera`)
**Controlador:** `CarreraController`

#### Lista — `GET /carrera`
**Vista:** `carrera/viewListaCarrera.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| carreras | List\<Carrera\> | Pagina actual |
| paginaActual / totalPaginas / totalElementos / pageSize | | |
| sort / sortBy | String | Campos validos: `id`, `nombre`, `clave` |

#### Agregar / Actualizar — `GET /carrera/agregar` y `GET /carrera/actualizar/{id}`
**Vista:** `carrera/viewFormCarrera.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| carrera | Carrera | Nueva o cargada |
| isEdit | boolean | |

#### Ver detalle — `GET /carrera/ver/{id}`
**Vista:** `carrera/viewInfoCarrera.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| carrera | Carrera | Entidad cargada |

#### Confirmar eliminar — `GET /carrera/delete/{id}`
**Vista:** `carrera/viewConfirmDeleteCarrera.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| carrera | Carrera | Entidad a eliminar |

---

### Modulo Coordinador de Carrera (`/coordinador`)
**Controlador:** `CoordinadorCarreraController`

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

#### Agregar / Actualizar — `GET /coordinador/agregar` y `GET /coordinador/actualizar/{id}`
**Vista:** `coordinador/viewFormCoordinador.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| coordinador | CoordinadorCarrera | Nuevo o cargado |
| carreras | List\<Carrera\> | |
| semestres | List\<Semestre\> | |
| isEdit | boolean | |

Acepta `fotoFile` (MultipartFile).

#### Ver detalle — `GET /coordinador/ver/{id}`
**Vista:** `coordinador/viewInfoCoordinador.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| coordinador | CoordinadorCarrera | Entidad con carrera y semestre |

#### Confirmar eliminar — `GET /coordinador/delete/{id}`
**Vista:** `coordinador/viewConfirmDeleteCoordinador.html`

| Atributo | Tipo | Descripcion |
|---|---|---|
| coordinador | CoordinadorCarrera | Entidad a eliminar |

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

*Generado el 2026-05-05*
