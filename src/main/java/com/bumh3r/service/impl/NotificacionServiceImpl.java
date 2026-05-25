package com.bumh3r.service.impl;

import com.bumh3r.dto.CoordinadorNotificacionDTO;
import com.bumh3r.entity.Actividad;
import com.bumh3r.entity.CoordinadorCarrera;
import com.bumh3r.entity.PAT;
import com.bumh3r.entity.Semestre;
import com.bumh3r.repository.ICoordinadorCarreraRepository;
import com.bumh3r.service.ActividadService;
import com.bumh3r.service.NotificacionService;
import com.bumh3r.service.SemestreService;
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

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificacionServiceImpl implements NotificacionService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private String imgLogoTecnm;
    private String imgSep;
    private String imgEmblema;
    private String imgTecnologicoNacional;

    @Autowired private JavaMailSender mailSender;
    @Autowired private TemplateEngine templateEngine;
    @Autowired private ActividadService actividadService;
    @Autowired private ICoordinadorCarreraRepository coordinadorRepository;
    @Autowired private SemestreService semestreService;

    @Value("${spring.mail.username:}")
    private String mailUser;

    @Value("${notificacion.remitente.nombre:Sistema de Tutorias - TecNM Chilpancingo}")
    private String remitenteNombre;

    // ── Imágenes Base64 ───────────────────────────────────────────────────

    private String cargarBase64(String resourcePath) {
        try {
            byte[] bytes = new ClassPathResource(resourcePath).getInputStream().readAllBytes();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            log.warn("No se pudo cargar la imagen '{}': {}", resourcePath, e.getMessage());
            return "";
        }
    }

    private void asegurarImagenesCargadas() {
        if (imgLogoTecnm == null) {
            imgLogoTecnm           = cargarBase64("static/images/tecnm/logoTecnm.png");
            imgSep                 = cargarBase64("static/images/tecnm/sep_logo.png");
            imgEmblema             = cargarBase64("static/images/tecnm/emblema.png");
            imgTecnologicoNacional = cargarBase64("static/images/tecnm/tecnologico_nacional.png");
        }
    }

    // ── Contexto Thymeleaf ────────────────────────────────────────────────

    private Context buildContext(CoordinadorCarrera coord, List<Actividad> actividades) {
        asegurarImagenesCargadas();

        LocalDate lunes   = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate domingo = lunes.plusDays(6);

        Context ctx = new Context();
        ctx.setVariable("coordinador",            coord);
        ctx.setVariable("actividades",            actividades);
        ctx.setVariable("fechaInicio",            lunes.format(FMT));
        ctx.setVariable("fechaFin",               domingo.format(FMT));
        ctx.setVariable("anioActual",             LocalDate.now().getYear());
        ctx.setVariable("totalActividades",       actividades.size());
        ctx.setVariable("imgLogoTecnm",           imgLogoTecnm);
        ctx.setVariable("imgSep",                 imgSep);
        ctx.setVariable("imgEmblema",             imgEmblema);
        ctx.setVariable("imgTecnologicoNacional", imgTecnologicoNacional);
        return ctx;
    }

    // ── Actividades de la semana ──────────────────────────────────────────

    @Override
    public List<Actividad> obtenerActividadesSemanaActual() {
        LocalDate lunes   = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate domingo = lunes.plusDays(6);
        return actividadService.buscarActividadesPorRangoFechas(lunes, domingo);
    }

    // ── Filtrado inteligente ──────────────────────────────────────────────

    @Override
    public List<Actividad> filtrarActividadesPorCoordinador(CoordinadorCarrera coord,
                                                             List<Actividad> todasActividades) {
        return todasActividades.stream()
                .filter(act -> act.getPat() != null)
                .filter(act -> {
                    PAT pat = act.getPat();
                    if (Integer.valueOf(1).equals(pat.getEsGeneral())) return true;
                    if (pat.getCarrera() != null && coord.getCarrera() != null) {
                        return pat.getCarrera().getId().equals(coord.getCarrera().getId());
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoordinadorCarrera> obtenerCoordinadoresVigentes() {
        Semestre vigente = semestreService.obtenerSemestreVigente();
        if (vigente == null) {
            log.warn("No hay semestre vigente configurado — no se obtendrán coordinadores");
            return Collections.emptyList();
        }
        return coordinadorRepository.findByActivoAndSemestre(1, vigente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoordinadorNotificacionDTO> obtenerResumenCoordinadores() {
        List<Actividad> actividades = obtenerActividadesSemanaActual();
        return obtenerCoordinadoresVigentes().stream()
                .map(coord -> {
                    List<Actividad> aplicables = filtrarActividadesPorCoordinador(coord, actividades);
                    return CoordinadorNotificacionDTO.builder()
                            .coordinador(coord)
                            .totalActividades(aplicables.size())
                            .actividadesAplicables(aplicables)
                            .puedeRecibirCorreo(!aplicables.isEmpty())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Actividad> obtenerActividadesAplicablesParaCoordinador(Integer idCoordinador) {
        Semestre vigente = semestreService.obtenerSemestreVigente();
        if (vigente == null) return null;

        CoordinadorCarrera coord = coordinadorRepository.findById(idCoordinador).orElse(null);
        if (coord == null || !Integer.valueOf(1).equals(coord.getActivo())) return null;
        if (coord.getSemestre() == null || !coord.getSemestre().getId().equals(vigente.getId())) return null;

        return filtrarActividadesPorCoordinador(coord, obtenerActividadesSemanaActual());
    }

    // ── Envío masivo ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Map<String, Integer> enviarRecordatoriosSemanales() {
        Map<String, Integer> resultado = new HashMap<>();

        if (mailUser == null || mailUser.isBlank()) {
            log.warn("MAIL_USER no configurado — se omite el envio de recordatorios");
            resultado.put("enviados", 0);
            resultado.put("fallidos", 0);
            resultado.put("omitidos", 0);
            resultado.put("sinEmail", 0);
            resultado.put("coordinadoresProcesados", 0);
            return resultado;
        }

        List<Actividad> actividades = obtenerActividadesSemanaActual();
        List<CoordinadorCarrera> coordinadores = obtenerCoordinadoresVigentes();

        log.info("Procesando recordatorios: {} coordinadores vigentes, {} actividades en la semana",
                coordinadores.size(), actividades.size());

        int enviados = 0, fallidos = 0, omitidos = 0, sinEmail = 0;

        for (CoordinadorCarrera coord : coordinadores) {
            if (coord.getEmail() == null || coord.getEmail().isBlank()) {
                log.warn("Coordinador id={} sin email, omitido", coord.getId());
                sinEmail++;
                continue;
            }
            List<Actividad> aplicables = filtrarActividadesPorCoordinador(coord, actividades);
            if (aplicables.isEmpty()) {
                log.info("Coordinador id={} ({} {}) sin actividades aplicables esta semana, omitido",
                        coord.getId(), coord.getNombre(), coord.getApellido());
                omitidos++;
                continue;
            }
            boolean ok = enviarCorreo(coord, aplicables);
            if (ok) enviados++; else fallidos++;
        }

        resultado.put("enviados", enviados);
        resultado.put("fallidos", fallidos);
        resultado.put("omitidos", omitidos);
        resultado.put("sinEmail", sinEmail);
        resultado.put("coordinadoresProcesados", coordinadores.size());
        return resultado;
    }

    // ── Envío individual ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public boolean enviarRecordatorioCoordinador(Integer idCoordinador) {
        if (mailUser == null || mailUser.isBlank()) {
            log.warn("MAIL_USER no configurado — se omite el envio individual");
            return false;
        }
        List<Actividad> aplicables = obtenerActividadesAplicablesParaCoordinador(idCoordinador);
        if (aplicables == null) {
            log.warn("Coordinador {} no encontrado, inactivo o no pertenece al semestre vigente", idCoordinador);
            return false;
        }
        if (aplicables.isEmpty()) {
            log.warn("Coordinador {} no tiene actividades aplicables esta semana, no se envía correo", idCoordinador);
            return false;
        }
        CoordinadorCarrera coord = coordinadorRepository.findById(idCoordinador).orElse(null);
        if (coord == null) return false;
        return enviarCorreo(coord, aplicables);
    }

    // ── Preview HTML ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public String renderPreviewHtml(Integer idCoordinador) {
        CoordinadorCarrera coord = coordinadorRepository.findById(idCoordinador).orElse(null);
        if (coord == null) {
            return "<html><body><p style='color:red;font-family:sans-serif;padding:20px;'>Coordinador no encontrado.</p></body></html>";
        }
        List<Actividad> aplicables = filtrarActividadesPorCoordinador(coord, obtenerActividadesSemanaActual());
        Context ctx = buildContext(coord, aplicables);
        return templateEngine.process("emails/recordatorioSemanal", ctx);
    }

    // ── Envío interno ─────────────────────────────────────────────────────

    private boolean enviarCorreo(CoordinadorCarrera coord, List<Actividad> actividades) {
        LocalDate lunes   = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate domingo = lunes.plusDays(6);
        String asunto = "Recordatorio Semanal de Tutorias - Semana del "
                + lunes.format(FMT) + " al " + domingo.format(FMT);
        try {
            String html = templateEngine.process("emails/recordatorioSemanal",
                    buildContext(coord, actividades));

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");
            helper.setFrom(mailUser, remitenteNombre);
            helper.setTo(coord.getEmail());
            helper.setSubject(asunto);
            helper.setText(html, true);

            mailSender.send(msg);
            log.info("Recordatorio enviado a {} <{}>",
                    coord.getNombre() + " " + coord.getApellido(), coord.getEmail());
            return true;

        } catch (MessagingException e) {
            log.error("MessagingException enviando a {} <{}>: {}",
                    coord.getNombre(), coord.getEmail(), e.getMessage(), e);
            return false;
        } catch (MailException e) {
            log.error("MailException enviando a {} <{}>: {}",
                    coord.getNombre(), coord.getEmail(), e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Error inesperado enviando a {} <{}>: {}",
                    coord.getNombre(), coord.getEmail(), e.getMessage(), e);
            return false;
        }
    }
}
