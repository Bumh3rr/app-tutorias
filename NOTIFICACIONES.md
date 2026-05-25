# Sistema de Recordatorios Semanales (RF10)

El sistema envía automáticamente un correo HTML institucional a todos los coordinadores de carrera activos cada lunes a las 8:00 AM (hora México), informando las actividades del PAT programadas para esa semana.

---

## 1. Configurar variables de entorno

El sistema usa Gmail con autenticación SMTP. Se requieren dos variables de entorno:

| Variable | Descripción |
|---|---|
| `MAIL_USER` | Dirección de correo Gmail que envía los recordatorios (ej. `tutorias@gmail.com`) |
| `MAIL_PASSWORD` | Contraseña de aplicación de Google (NO la contraseña normal de Gmail) |

### Generar una contraseña de aplicación en Google

1. Ir a [myaccount.google.com](https://myaccount.google.com) → **Seguridad**
2. Habilitar **Verificación en dos pasos** (requerido)
3. Ir a **Contraseñas de aplicaciones**
4. Crear una nueva → seleccionar "Otra (nombre personalizado)" → poner "Tutorias TecNM"
5. Copiar la contraseña generada de 16 caracteres (sin espacios)

### Configurar las variables

**En desarrollo local (terminal antes de lanzar la app):**
```bash
export MAIL_USER=tu-correo@gmail.com
export MAIL_PASSWORD=abcdabcdabcdabcd
```

**En producción (Railway, Render, Heroku, etc.):**
Agregar `MAIL_USER` y `MAIL_PASSWORD` como variables de entorno en el panel de la plataforma.

**En IntelliJ IDEA:**
Run → Edit Configurations → Environment variables:
```
MAIL_USER=tu-correo@gmail.com;MAIL_PASSWORD=abcdabcdabcdabcd
```

> **Importante:** Si `MAIL_USER` está vacío, el sistema registra un warning en los logs y omite el envío sin lanzar excepciones. La aplicación sigue funcionando normalmente.

---

## 2. Probar manualmente desde el navegador

### Acceder a la pantalla de notificaciones

Ir a `http://localhost:8080/notificacion`

Desde ahí puedes:

- **Ver** las actividades programadas para la semana actual
- **Ver** el listado de coordinadores activos
- **Vista previa** del correo de un coordinador sin enviarlo (botón "Preview" → abre en nueva pestaña)
- **Enviar a uno** usando el botón "Enviar" junto a cada coordinador
- **Enviar a todos** con el botón grande y confirmación modal

### Prueba de vista previa (sin enviar)

```
GET http://localhost:8080/notificacion/preview/{idCoordinador}
```

Muestra el HTML del correo renderizado en el navegador, incluyendo los datos reales de la base de datos.

---

## 3. Cambiar el horario del cron

El horario está en `application.properties`:

```properties
notificacion.recordatorio.cron=0 0 8 ? * MON
```

Formato: `segundos minutos horas díaMes mes díaSemana`

| Expresión | Significado |
|---|---|
| `0 0 8 ? * MON` | Cada lunes a las 8:00 AM (configuración actual) |
| `0 0 9 ? * MON` | Cada lunes a las 9:00 AM |
| `0 30 7 ? * MON` | Cada lunes a las 7:30 AM |
| `0 0 8 ? * MON,FRI` | Lunes y viernes a las 8:00 AM |
| `0 0/30 * ? * *` | Cada 30 minutos (para pruebas) |

La zona horaria está fijada en `America/Mexico_City` en el código del scheduler.

---

## 4. Deshabilitar temporalmente los envíos automáticos

Cambiar en `application.properties`:

```properties
notificacion.recordatorio.enabled=false
```

Con `enabled=false`, el scheduler se ejecuta según el cron pero registra un mensaje en los logs y no envía ningún correo. Los envíos manuales desde `/notificacion` siguen funcionando normalmente.

Para rehabilitar:
```properties
notificacion.recordatorio.enabled=true
```

---

## 5. Limitaciones conocidas

### Gmail personal
- **Límite de 500 correos/día** con cuentas Gmail personales (@gmail.com)
- **Límite de 100 destinatarios por envío** (no aplica aquí ya que se envía uno a uno)
- Si el sistema tiene más de 500 coordinadores activos en un semestre, considerar Google Workspace (límite de 2,000/día) o un servicio dedicado como SendGrid

### Gmail con contraseña de aplicación
- La contraseña de aplicación debe regenerarse si se deshabilita la verificación en dos pasos
- El correo podría llegar a carpeta de spam en el primer envío; pedir al coordinador que marque como "No es spam"

### Actividades sin fecha
- Las actividades sin `fecha` asignada **no aparecerán** en el recordatorio aunque su semana coincida con la semana actual, ya que el filtro es por rango de fechas (`buscarActividadesPorRangoFechas`)

### Coordinadores sin email
- Los coordinadores con campo `email` vacío o nulo son omitidos del envío automático y del envío manual (botón deshabilitado)
- El contador de enviados/fallidos en los logs indica cuántos fueron omitidos por este motivo

### Entorno de desarrollo sin credenciales
- Si `MAIL_USER` no está configurado, el sistema registra un warning y retorna sin enviar
- La aplicación **no falla** por falta de credenciales de correo

---

## Archivos del sistema

| Archivo | Descripción |
|---|---|
| `service/NotificacionService.java` | Interfaz del servicio |
| `service/impl/NotificacionServiceImpl.java` | Lógica de envío con JavaMailSender + Thymeleaf |
| `scheduler/RecordatorioScheduler.java` | Componente `@Scheduled` que dispara el envío automático |
| `controller/NotificacionController.java` | Endpoints de la UI de prueba manual |
| `templates/notificacion/viewNotificaciones.html` | Vista de administración `/notificacion` |
| `templates/emails/recordatorioSemanal.html` | Template HTML del correo (solo tablas + CSS inline) |
