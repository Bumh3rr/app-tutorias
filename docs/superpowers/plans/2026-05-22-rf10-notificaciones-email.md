# RF10 — Sistema de Recordatorios Semanales por Correo

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enviar cada lunes a las 8:00 AM un correo HTML institucional a todos los coordinadores activos con las actividades del PAT programadas para esa semana.

**Architecture:** `NotificacionService` (interface + Impl) orquesta la consulta de actividades y el envío via `JavaMailSender`+Thymeleaf; `RecordatorioScheduler` lo dispara con `@Scheduled`; `NotificacionController` expone una UI de prueba manual en `/notificacion`.

**Tech Stack:** Spring Boot Mail, Thymeleaf TemplateEngine (modo off-context), SLF4J/Lombok @Slf4j, `@Scheduled` con cron, Bootstrap 5 (vista admin), tablas HTML con CSS inline (correo).

**Pre-condiciones ya cumplidas:**
- `spring-boot-starter-mail` ya está en `pom.xml`
- `@EnableScheduling` ya está en `AppTutoriasApplication`

---

## Archivos del plan

| Archivo | Acción |
|---|---|
| `src/main/resources/application.properties` | MODIFICAR — agregar timeouts y propiedades notificacion |
| `src/main/java/com/bumh3r/service/NotificacionService.java` | CREAR |
| `src/main/java/com/bumh3r/service/impl/NotificacionServiceImpl.java` | CREAR |
| `src/main/java/com/bumh3r/scheduler/RecordatorioScheduler.java` | CREAR |
| `src/main/java/com/bumh3r/controller/NotificacionController.java` | CREAR |
| `src/main/resources/templates/emails/recordatorioSemanal.html` | CREAR |
| `src/main/resources/templates/notificacion/viewNotificaciones.html` | CREAR |
| `src/main/resources/templates/fragments/fragment.html` | MODIFICAR — enlace sidebar |
| `NOTIFICACIONES.md` | CREAR |

---

## Task 1: Propiedades de configuración

**Files:**
- Modify: `src/main/resources/application.properties`

- [ ] Agregar al final de `application.properties`:

```properties
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000

# Notificacion config
notificacion.remitente.nombre=Sistema de Tutorías - TecNM Chilpancingo
notificacion.recordatorio.cron=0 0 8 ? * MON
notificacion.recordatorio.enabled=true
```

- [ ] Commit: `feat: add mail timeout and notificacion scheduler properties`

---

## Task 2: Interface NotificacionService

**Files:**
- Create: `src/main/java/com/bumh3r/service/NotificacionService.java`

- [ ] Crear el archivo:

```java
package com.bumh3r.service;

import com.bumh3r.entity.Actividad;

import java.util.List;
import java.util.Map;

public interface NotificacionService {
    Map<String, Integer> enviarRecordatoriosSemanales();
    boolean enviarRecordatorioCoordinador(Integer idCoordinador);
    List<Actividad> obtenerActividadesSemanaActual();
}
```

---

## Task 3: NotificacionServiceImpl

**Files:**
- Create: `src/main/java/com/bumh3r/service/impl/NotificacionServiceImpl.java`

- [ ] Crear el archivo:

```java
package com.bumh3r.service.impl;

import com.bumh3r.entity.Actividad;
import com.bumh3r.entity.CoordinadorCarrera;
import com.bumh3r.repository.ICoordinadorCarreraRepository;
import com.bumh3r.service.ActividadService;
import com.bumh3r.service.NotificacionService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class NotificacionServiceImpl implements NotificacionService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired private JavaMailSender mailSender;
    @Autowired private TemplateEngine templateEngine;
    @Autowired private ActividadService actividadService;
    @Autowired private ICoordinadorCarreraRepository coordinadorRepository;

    @Value("${spring.mail.username:}") private String mailUser;
    @Value("${notificacion.remitente.nombre:Sistema de Tutorías - TecNM Chilpancingo}")
    private String remitenteNombre;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Integer> enviarRecordatoriosSemanales() {
        int enviados = 0, fallidos = 0, sinEmail = 0;

        if (mailUser == null || mailUser.isBlank()) {
            log.warn("MAIL_USER no configurado — se omite el envío de recordatorios");
            Map<String, Integer> r = new HashMap<>();
            r.put("enviados", 0); r.put("fallidos", 0); r.put("sinActividades", 0);
            r.put("coordinadoresProcesados", 0);
            return r;
        }

        List<Actividad> actividades = obtenerActividadesSemanaActual();
        List<CoordinadorCarrera> coordinadores = coordinadorRepository.findByActivo(1);
        log.info("Enviando recordatorios a {} coordinadores, {} actividades en la semana",
                coordinadores.size(), actividades.size());

        for (CoordinadorCarrera coord : coordinadores) {
            if (coord.getEmail() == null || coord.getEmail().isBlank()) {
                log.warn("Coordinador id={} sin email, omitido", coord.getId());
                sinEmail++;
                continue;
            }
            boolean ok = enviarCorreo(coord, actividades);
            if (ok) enviados++; else fallidos++;
        }

        Map<String, Integer> resultado = new HashMap<>();
        resultado.put("enviados", enviados);
        resultado.put("fallidos", fallidos);
        resultado.put("sinActividades", actividades.isEmpty() ? 1 : 0);
        resultado.put("coordinadoresProcesados", coordinadores.size());
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean enviarRecordatorioCoordinador(Integer idCoordinador) {
        if (mailUser == null || mailUser.isBlank()) {
            log.warn("MAIL_USER no configurado — se omite el envío");
            return false;
        }
        CoordinadorCarrera coord = coordinadorRepository.findById(idCoordinador).orElse(null);
        if (coord == null) { log.warn("Coordinador {} no encontrado", idCoordinador); return false; }
        List<Actividad> actividades = obtenerActividadesSemanaActual();
        return enviarCorreo(coord, actividades);
    }

    @Override
    public List<Actividad> obtenerActividadesSemanaActual() {
        LocalDate lunes = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate domingo = lunes.plusDays(6);
        return actividadService.buscarActividadesPorRangoFechas(lunes, domingo);
    }

    // ── Renderiza y envía un correo a un coordinador ──────────────────────
    private boolean enviarCorreo(CoordinadorCarrera coord, List<Actividad> actividades) {
        LocalDate lunes = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate domingo = lunes.plusDays(6);
        String asunto = "Recordatorio Semanal de Tutorías - Semana del "
                + lunes.format(FMT) + " al " + domingo.format(FMT);
        try {
            Context ctx = new Context();
            ctx.setVariable("coordinador", coord);
            ctx.setVariable("actividades", actividades);
            ctx.setVariable("fechaInicio", lunes.format(FMT));
            ctx.setVariable("fechaFin", domingo.format(FMT));
            ctx.setVariable("anioActual", LocalDate.now().getYear());
            ctx.setVariable("totalActividades", actividades.size());

            String html = templateEngine.process("emails/recordatorioSemanal", ctx);

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(mailUser, remitenteNombre);
            helper.setTo(coord.getEmail());
            helper.setSubject(asunto);
            helper.setText(html, true);

            helper.addInline("logoTecnm",
                    new ClassPathResource("static/images/tecnm/logoTecnm.png"));
            helper.addInline("sepLogo",
                    new ClassPathResource("static/images/tecnm/sep_logo.png"));
            helper.addInline("emblema",
                    new ClassPathResource("static/images/tecnm/emblema.png"));
            helper.addInline("tecnologicoNacional",
                    new ClassPathResource("static/images/tecnm/tecnologico_nacional.png"));

            mailSender.send(msg);
            log.info("Recordatorio enviado a {} <{}>", coord.getNombre(), coord.getEmail());
            return true;
        } catch (MessagingException | MailException e) {
            log.error("Error enviando correo a {} <{}>: {}", coord.getNombre(), coord.getEmail(), e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Error inesperado enviando correo a {}: {}", coord.getEmail(), e.getMessage(), e);
            return false;
        }
    }
}
```

---

## Task 4: RecordatorioScheduler

**Files:**
- Create: `src/main/java/com/bumh3r/scheduler/RecordatorioScheduler.java`

- [ ] Crear directorio y archivo:

```java
package com.bumh3r.scheduler;

import com.bumh3r.service.NotificacionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class RecordatorioScheduler {

    @Autowired private NotificacionService notificacionService;

    @Value("${notificacion.recordatorio.enabled:true}")
    private boolean enabled;

    @Scheduled(cron = "${notificacion.recordatorio.cron}", zone = "America/Mexico_City")
    public void ejecutarRecordatoriosSemanales() {
        if (!enabled) {
            log.info("Recordatorios semanales deshabilitados por configuración");
            return;
        }
        log.info("=== Iniciando envío de recordatorios semanales ===");
        Map<String, Integer> resultado = notificacionService.enviarRecordatoriosSemanales();
        log.info("=== Recordatorios completados: {} ===", resultado);
    }
}
```

---

## Task 5: Template HTML del correo

**Files:**
- Create: `src/main/resources/templates/emails/recordatorioSemanal.html`

- [ ] Crear el directorio `templates/emails/` y el archivo (ver código completo en la implementación — usa solo tablas HTML e inline CSS para compatibilidad con clientes de correo).

---

## Task 6: NotificacionController

**Files:**
- Create: `src/main/java/com/bumh3r/controller/NotificacionController.java`

- [ ] Crear el archivo siguiendo el patrón del proyecto.

---

## Task 7: Vista viewNotificaciones.html

**Files:**
- Create: `src/main/resources/templates/notificacion/viewNotificaciones.html`

- [ ] Crear la vista con sidebar estándar, tabla de coordinadores y tabla de actividades.

---

## Task 8: Enlace en el sidebar

**Files:**
- Modify: `src/main/resources/templates/fragments/fragment.html`

- [ ] Insertar antes del botón "Toggle collapse" (línea ~271):

```html
<!-- Notificaciones -->
<span class="section-label">Notificaciones</span>
<a th:href="@{/notificacion}" class="nav-item">
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
        <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
    </svg>
    <span class="nav-label">Recordatorios</span>
</a>
```

---

## Task 9: NOTIFICACIONES.md

**Files:**
- Create: `NOTIFICACIONES.md`

- [ ] Crear documentación de configuración y uso.
