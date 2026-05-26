# Flujo del Tutor en el Sistema de Tutorías

## Descripción general

El módulo de tutor permite a los docentes asignados como tutores gestionar sus grupos de tutorados, registrar asistencias, subir evidencias, llenar reportes de sesión y aplicar detecciones de necesidades. Todo el módulo está protegido bajo el rol `ROLE_TUTOR` y el tutor únicamente puede ver y operar los grupos, sesiones y tutorados que le fueron asignados por el administrador.

---

## 1. Autenticación

**Ruta:** `/login`

El tutor accede al sistema con las credenciales que el administrador le genera automáticamente al crearlo:

| Campo    | Valor                         |
|----------|-------------------------------|
| Usuario  | Correo electrónico del tutor  |
| Contraseña | Número de control del tutor |

Una vez autenticado correctamente, Spring Security detecta el rol `ROLE_TUTOR` y redirige automáticamente a `/tutor` (el dashboard).

> Si el tutor intenta acceder a una ruta de otro rol (por ejemplo `/admin/**`), el sistema devuelve un error 403.

---

## 2. Dashboard

**Ruta:** `/tutor` o `/tutor/dashboard`

Pantalla principal que ofrece una vista general del semestre vigente.

**Información que muestra:**
- Nombre del tutor autenticado
- Semestre vigente (periodo y año)
- **Tarjetas de estadísticas:**
  - Total de tutorados asignados en el semestre
  - Total de sesiones programadas
  - Sesiones marcadas como `REALIZADA`
- **Lista de grupos** del semestre vigente con:
  - Nombre del grupo
  - Número de alumnos
  - Próxima sesión pendiente (la de menor número de semana que aún no está realizada)
- Accesos rápidos a los módulos principales

---

## 3. Mis Grupos

**Ruta:** `/tutor/grupos`

Lista todos los grupos que el tutor tiene asignados en el semestre vigente.

**Por cada grupo se muestra:**
- Nombre del grupo
- Número de tutorados asignados
- Semestre y carrera

Al hacer clic en un grupo se navega al detalle del mismo.

---

## 4. Detalle de Grupo

**Ruta:** `/tutor/grupos/{idGrupo}`

Vista completa de un grupo específico del tutor. Si el tutor intenta acceder a un grupo que no le pertenece, el sistema lanza un `AccessDeniedException` (error 403).

**Información del grupo:**
- Aula, día de la semana, horario
- Carrera y semestre

**Panel de tutorados:**
- Lista de todos los tutorados asignados al grupo (activos)
- Nombre completo y número de control de cada uno

**Panel de sesiones:**
- Tabla con todas las sesiones del semestre ordenadas por semana
- Columnas: semana, actividad PAT, fecha, estatus (`PENDIENTE`, `REALIZADA`, `CANCELADA`)
- Botón "Operar" en cada fila → navega al detalle de esa sesión

**Accesos rápidos:**
- Ver sesiones del semestre
- Estado de detección de tutorados
- Resumen de asistencias

---

## 5. Mis Sesiones

**Ruta:** `/tutor/sesiones`

Lista completa de todas las sesiones del tutor, agrupando todos sus grupos del semestre vigente.

**Ordenamiento:** por nombre de grupo y luego por número de semana.

**Columnas de la tabla:**
- Grupo (nombre + carrera)
- Semana (badge numérico)
- Fecha de impartición
- Actividad PAT asignada
- Estatus (verde: `REALIZADA`, rojo: `CANCELADA`, amarillo: `PENDIENTE`)
- Botón "Operar" → navega al detalle de la sesión

---

## 6. Detalle de Sesión

**Ruta:** `/tutor/sesiones/{idSesion}`

Centro de operaciones de una sesión. Verifica que la sesión pertenezca al tutor autenticado; si no, lanza 403.

**Información de la sesión:**
- Grupo, actividad PAT, fecha, estatus

**Cuatro tarjetas de acción:**

| # | Acción | Descripción | Estado |
|---|--------|-------------|--------|
| 1 | Registrar asistencia | Marca qué tutorados asistieron | Badge "✓ Hecho" si ya se registró |
| 2 | Subir evidencia | Adjunta foto o PDF como evidencia | Badge "✓ Hecho" si ya hay archivos |
| 3 | Reporte de sesión | Llena el Anexo 19 con descripción y observaciones | Badge "✓ Hecho" si ya existe reporte |
| 4 | Recuperación de asistencia | Marca tutorados que recuperaron una falta en taller | Sin badge de estado |

**Panel de asistencia registrada** (visible solo si ya hay asistencias):
- Tabla con todos los tutorados del grupo
- Por cada uno: si asistió (✓ verde), no asistió (✗ rojo), o sin dato (—)
- Columna adicional: si la falta fue recuperada

**Panel de evidencias subidas** (visible solo si hay archivos):
- Lista de URLs de archivos subidos
- Estatus de validación de cada evidencia (`PENDIENTE`, `VALIDADA`, `RECHAZADA`)

---

## 7. Registrar Asistencia

**Ruta GET:** `/tutor/sesiones/{idSesion}/asistencia`  
**Ruta POST:** `/tutor/sesiones/{idSesion}/asistencia`

Formulario interactivo para marcar la asistencia de los tutorados del grupo en esa sesión.

**Funcionamiento:**
- Muestra la lista de todos los tutorados activos del grupo
- Cada tutorado es una fila clickeable: al hacer clic la fila se vuelve verde y el checkbox se activa ("Asistió")
- Si la asistencia ya fue registrada previamente, los presentes aparecen preseleccionados para edición
- Botones auxiliares: **"Marcar todos"** / **"Desmarcar todos"**

**Al guardar (POST):**
- Se envían los IDs de los tutorados marcados como presentes
- El sistema genera un registro de `Asistencia` por cada tutorado del grupo:
  - `presente = 1` si fue seleccionado
  - `presente = 0` si no fue seleccionado
- Redirige al detalle de la sesión con mensaje de éxito

> Si el grupo no tiene tutorados asignados, se muestra el mensaje: "No hay tutorados registrados en este grupo."

---

## 8. Subir Evidencia

**Ruta GET:** `/tutor/sesiones/{idSesion}/evidencia`  
**Ruta POST:** `/tutor/sesiones/{idSesion}/evidencia`

Formulario para adjuntar archivos como evidencia de la sesión realizada.

**Formatos aceptados:** imágenes (JPG, PNG, etc.) y PDF.  
**Tamaño máximo:** 10 MB por archivo.

- Se puede subir más de una evidencia por sesión
- Cada evidencia queda en estatus `PENDIENTE` hasta que el administrador la valide o rechace
- Los archivos se almacenan en el servidor en el directorio configurado (`FILE_UPLOAD_PATH`)

---

## 9. Reporte de Sesión (Anexo 19)

**Ruta GET:** `/tutor/sesiones/{idSesion}/reporte`  
**Ruta POST:** `/tutor/sesiones/{idSesion}/reporte`

Formulario para registrar el reporte oficial de la sesión.

**Campos:**
| Campo | Descripción | Obligatorio |
|-------|-------------|-------------|
| Descripción de actividad | Detalle de lo que se realizó en la sesión | Sí |
| Observaciones | Notas adicionales relevantes | No |
| Alumnos presentes | Número de asistentes (pre-calculado de asistencia registrada) | No |
| Fecha de entrega | Fecha en que se entrega el reporte | No |

- Si ya existe un reporte para esa sesión, el formulario carga los datos existentes para edición
- El reporte queda en estatus `PENDIENTE` hasta revisión del administrador

---

## 10. Recuperación de Asistencia

**Ruta GET:** `/tutor/sesiones/{idSesion}/recuperacion`  
**Ruta POST:** `/tutor/sesiones/{idSesion}/recuperacion`

Permite marcar que un tutorado que faltó a una sesión la recuperó asistiendo a un taller o actividad equivalente.

**Muestra solo** los tutorados que:
1. Tienen `presente = 0` (faltaron) en esa sesión
2. Aún no tienen `recuperada = 1`

**Al guardar:** los tutorados seleccionados se marcan con `recuperada = 1` en su registro de asistencia. Esto los contabiliza positivamente en el porcentaje de asistencia.

---

## 11. Detección de Necesidades

### 11.1 Vista general

**Ruta:** `/tutor/deteccion`

Lista todos los tutorados únicos de todos los grupos del tutor en el semestre vigente, con el estado de su detección de necesidades.

**Cabecera:**
- Badge verde: cantidad de tutorados con detección aplicada
- Badge amarillo: cantidad de tutorados con detección pendiente

**Tabla:**
| Columna | Contenido |
|---------|-----------|
| Tutorado | Nombre completo |
| No. Control | Número de control |
| Grupo | Grupo de referencia |
| Detección | Badge "Aplicada" (verde) o "Pendiente" (amarillo) |
| Acción | Botón "Aplicar ahora" o "Ver / nueva" |

> Un mismo tutorado no aparece duplicado aunque esté en múltiples grupos.

### 11.2 Aplicar detección

**Ruta GET:** `/tutor/deteccion/{idTutorado}`  
**Ruta POST:** `/tutor/deteccion/{idTutorado}`

Formulario de Detección de Necesidades dividido en tres secciones. Solo accesible si el tutorado pertenece a algún grupo del tutor.

**Sección 1 — Necesidades académicas:**
- Álgebra
- Cálculo
- Introducción al Derecho
- Apoyo psicológico (con campo adicional: tema a atender)
- Otra necesidad (campo libre)

**Sección 2 — Situación económica:**
- Solicita apoyo económico
- Solicita beca (con campo: nombre/tipo de beca)
- Escasez de materiales (con campo: materiales requeridos)

**Sección 3 — Salud y familia:**
- Requiere atención médica (con campo: especificación)
- Vinculación familiar (con campo: motivo)

**Campo adicional:**
- Observaciones generales

**Sesión asociada (opcional):** dropdown con las sesiones del tutor en los grupos donde está ese tutorado. Permite vincular la detección a una sesión específica.

Los campos de tipo checkbox usan tarjetas interactivas: al hacer clic en la tarjeta se activa/desactiva el checkbox y aparecen campos de detalle si aplican.

---

## 12. Resumen de Asistencias

**Ruta:** `/tutor/asistencias`

Vista consolidada del estado de asistencia de todos los tutorados del tutor en el semestre vigente.

**Tarjetas de estadísticas:**
| Tarjeta | Descripción |
|---------|-------------|
| Acreditados (≥ 80%) | Tutorados que cumplen el umbral mínimo de asistencia |
| En riesgo (< 80%) | Tutorados por debajo del umbral |
| Total de tutorados | Suma total de tutorados únicos |

**Tabla detallada por tutorado:**
| Columna | Descripción |
|---------|-------------|
| Tutorado | Nombre completo |
| Grupo | Grupo de referencia |
| Sesiones | Total de sesiones contabilizadas |
| Asistió | Número de sesiones con `presente = 1` |
| Recuperó | Número de sesiones con `recuperada = 1` |
| Porcentaje | Barra de progreso + valor numérico |
| Estatus | Badge "Acreditado" (verde) o "En riesgo" (amarillo) |

La fila completa de los tutorados en riesgo se resalta en amarillo. El porcentaje se calcula incluyendo tanto presencias directas como recuperaciones.

---

## Seguridad y control de acceso

Todas las rutas del módulo tutor están protegidas por Spring Security y verificadas en el controlador:

| Validación | Descripción |
|------------|-------------|
| `resolverTutor()` | Obtiene el `Tutor` vinculado al usuario autenticado. Si el usuario no tiene tutor asociado, lanza `AccessDeniedException`. |
| `validarGrupoDelTutor()` | Verifica que `grupo.tutor.id == tutor.id`. Lanza 403 si el grupo no pertenece al tutor. |
| `validarSesionDelTutor()` | Verifica que `sesion.grupo.tutor.id == tutor.id`. Lanza 403 si la sesión no pertenece al tutor. |
| `validarTutoradoDelTutor()` | Verifica que el tutorado esté en algún grupo del tutor antes de aplicar una detección. |

El tutor **no puede**:
- Ver grupos de otros tutores
- Registrar asistencia en sesiones que no son suyas
- Aplicar detección a tutorados que no son suyos
- Acceder a rutas del panel de administración (`/admin/**`)

---

## Diagrama de navegación simplificado

```
/login
  └── [autenticación exitosa con ROLE_TUTOR]
        └── /tutor/dashboard
              ├── /tutor/grupos
              │     └── /tutor/grupos/{idGrupo}  (detalle de grupo)
              │
              ├── /tutor/sesiones
              │     └── /tutor/sesiones/{idSesion}  (detalle de sesión)
              │           ├── /tutor/sesiones/{idSesion}/asistencia
              │           ├── /tutor/sesiones/{idSesion}/evidencia
              │           ├── /tutor/sesiones/{idSesion}/reporte
              │           └── /tutor/sesiones/{idSesion}/recuperacion
              │
              ├── /tutor/deteccion
              │     └── /tutor/deteccion/{idTutorado}  (aplicar detección)
              │
              └── /tutor/asistencias
```
