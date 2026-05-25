package com.bumh3r.controller;

import com.bumh3r.dto.CoordinadorNotificacionDTO;
import com.bumh3r.entity.Actividad;
import com.bumh3r.entity.CoordinadorCarrera;
import com.bumh3r.entity.Semestre;
import com.bumh3r.service.CoordinadorCarreraService;
import com.bumh3r.service.NotificacionService;
import com.bumh3r.service.SemestreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = "notificacion")
public class NotificacionController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired private NotificacionService notificacionService;
    @Autowired private CoordinadorCarreraService coordinadorCarreraService;
    @Autowired private SemestreService semestreService;

    @GetMapping()
    public String obtenerVistaNotificaciones(Model model) {
        LocalDate lunes   = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate domingo = lunes.plusDays(6);

        List<Actividad> actividades = notificacionService.obtenerActividadesSemanaActual();
        List<CoordinadorNotificacionDTO> resumen = notificacionService.obtenerResumenCoordinadores();
        Semestre vigente = semestreService.obtenerSemestreVigente();

        // Actividades del PAT general (llegan a todos los coordinadores del semestre)
        List<Actividad> actividadesGenerales = actividades.stream()
                .filter(a -> a.getPat() != null && Integer.valueOf(1).equals(a.getPat().getEsGeneral()))
                .collect(Collectors.toList());

        // Actividades por carrera (específicas de cada PAT de carrera)
        Map<String, List<Actividad>> actividadesPorCarrera = actividades.stream()
                .filter(a -> a.getPat() != null
                        && !Integer.valueOf(1).equals(a.getPat().getEsGeneral())
                        && a.getPat().getCarrera() != null)
                .collect(Collectors.groupingBy(a -> a.getPat().getCarrera().getNombre()));

        long coordinadoresConActividades = resumen.stream()
                .filter(CoordinadorNotificacionDTO::isPuedeRecibirCorreo).count();
        long coordinadoresOmitidos = resumen.stream()
                .filter(d -> !d.isPuedeRecibirCorreo()).count();

        model.addAttribute("resumenCoordinadores",      resumen);
        model.addAttribute("actividadesGenerales",      actividadesGenerales);
        model.addAttribute("actividadesPorCarrera",     actividadesPorCarrera);
        model.addAttribute("coordinadoresConActividades", coordinadoresConActividades);
        model.addAttribute("coordinadoresOmitidos",     coordinadoresOmitidos);
        model.addAttribute("semestreVigente",           vigente);
        model.addAttribute("fechaInicio",               lunes.format(FMT));
        model.addAttribute("fechaFin",                  domingo.format(FMT));
        model.addAttribute("totalActividades",          actividades.size());
        return "notificacion/viewNotificaciones";
    }

    @PostMapping(value = "enviar/todos")
    public String enviarATodos(RedirectAttributes attributes) {
        try {
            Map<String, Integer> r = notificacionService.enviarRecordatoriosSemanales();
            int enviados   = r.getOrDefault("enviados", 0);
            int fallidos   = r.getOrDefault("fallidos", 0);
            int omitidos   = r.getOrDefault("omitidos", 0);
            int procesados = r.getOrDefault("coordinadoresProcesados", 0);

            if (procesados == 0) {
                attributes.addFlashAttribute("msg_error",
                        "No hay coordinadores en el semestre vigente o MAIL_USER no está configurado.");
            } else if (enviados == 0 && fallidos == 0) {
                attributes.addFlashAttribute("msg_warning",
                        "No se envió ningún correo: los " + omitidos
                        + " coordinador(es) del semestre vigente no tienen actividades aplicables esta semana.");
            } else if (fallidos == 0) {
                String msg = "Recordatorios enviados correctamente a " + enviados + " coordinador(es).";
                if (omitidos > 0) msg += " " + omitidos + " coordinador(es) omitido(s) por no tener actividades aplicables.";
                attributes.addFlashAttribute("msg_success", msg);
            } else {
                String msg = "Envío parcial: " + enviados + " exitosos, " + fallidos + " fallidos";
                if (omitidos > 0) msg += ", " + omitidos + " omitido(s) por sin actividades";
                msg += ". Revise los logs para más detalles.";
                attributes.addFlashAttribute("msg_error", msg);
            }
        } catch (Exception e) {
            log.error("Error al enviar recordatorios masivos: {}", e.getMessage(), e);
            attributes.addFlashAttribute("msg_error", "Error al enviar recordatorios: " + e.getMessage());
        }
        return "redirect:/notificacion";
    }

    @PostMapping(value = "enviar/{idCoordinador}")
    public String enviarACoordinador(@PathVariable Integer idCoordinador, RedirectAttributes attributes) {
        try {
            List<Actividad> aplicables = notificacionService.obtenerActividadesAplicablesParaCoordinador(idCoordinador);

            if (aplicables == null) {
                attributes.addFlashAttribute("msg_error",
                        "Coordinador no encontrado, inactivo o no pertenece al semestre vigente.");
                return "redirect:/notificacion";
            }
            if (aplicables.isEmpty()) {
                CoordinadorCarrera coord = coordinadorCarreraService.obtenerCoordinador(idCoordinador);
                String nombre = coord != null
                        ? coord.getNombre() + " " + coord.getApellido()
                        : "#" + idCoordinador;
                attributes.addFlashAttribute("msg_warning",
                        "El coordinador " + nombre
                        + " no tiene actividades aplicables esta semana, por lo que no se envió correo.");
                return "redirect:/notificacion";
            }

            boolean ok = notificacionService.enviarRecordatorioCoordinador(idCoordinador);
            if (ok) {
                CoordinadorCarrera coord = coordinadorCarreraService.obtenerCoordinador(idCoordinador);
                String nombre = coord != null
                        ? coord.getNombre() + " " + coord.getApellido()
                        : "#" + idCoordinador;
                attributes.addFlashAttribute("msg_success",
                        "Recordatorio enviado correctamente a " + nombre + ".");
            } else {
                attributes.addFlashAttribute("msg_error",
                        "No se pudo enviar el recordatorio. Verifique que MAIL_USER esté configurado.");
            }
        } catch (Exception e) {
            log.error("Error enviando recordatorio al coordinador {}: {}", idCoordinador, e.getMessage(), e);
            attributes.addFlashAttribute("msg_error", "Error al enviar: " + e.getMessage());
        }
        return "redirect:/notificacion";
    }

    @GetMapping(value = "preview/{idCoordinador}")
    @ResponseBody
    public String previewCorreo(@PathVariable Integer idCoordinador) {
        return notificacionService.renderPreviewHtml(idCoordinador);
    }
}
