package com.bumh3r.controller;

import com.bumh3r.entity.Actividad;
import com.bumh3r.entity.Grupo;
import com.bumh3r.entity.Tutor;
import com.bumh3r.entity.Tutorado;
import com.bumh3r.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    @Autowired private TutorService tutorService;
    @Autowired private TutoradoService tutoradoService;
    @Autowired private ActividadService actividadService;
    @Autowired private GrupoService grupoService;

    @GetMapping("/login")
    public String login(Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String role = auth.getAuthorities().iterator().next().getAuthority();
            return switch (role) {
                case "ROLE_DDA", "ROLE_CIT" -> "redirect:/admin/dashboard";
                case "ROLE_SUBDIRECTOR"      -> "redirect:/subdirector";
                case "ROLE_COORDINADOR"      -> "redirect:/coordinador";
                case "ROLE_TUTOR"            -> "redirect:/tutor";
                case "ROLE_TUTORADO"         -> "redirect:/tutorado";
                default                      -> "redirect:/";
            };
        }
        return "public/viewLogin";
    }

    @GetMapping("/error/403")
    public String error403(Authentication auth, Model model) {
        String homeUrl = "/";
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String role = auth.getAuthorities().iterator().next().getAuthority();
            homeUrl = switch (role) {
                case "ROLE_DDA", "ROLE_CIT" -> "/admin/dashboard";
                case "ROLE_SUBDIRECTOR"      -> "/subdirector";
                case "ROLE_COORDINADOR"      -> "/coordinador";
                case "ROLE_TUTOR"            -> "/tutor";
                case "ROLE_TUTORADO"         -> "/tutorado";
                default                      -> "/";
            };
        }
        model.addAttribute("homeUrl", homeUrl);
        return "error/403";
    }

    @GetMapping({"", "/"})
    public String root(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int pageSize,
            Model model) {

        Page<Actividad> actividades;
        String filtro = null;
        try {
            if (!q.isBlank()) {
                actividades = actividadService.buscarActividadesPorNombrePaginado(q, page, pageSize, "semana", "asc");
                filtro = "Nombre: " + q;
            } else if (fechaDesde != null && !fechaDesde.isBlank()
                    && fechaHasta != null && !fechaHasta.isBlank()) {
                actividades = actividadService.buscarActividadesPorRangoFechasPaginado(
                        LocalDate.parse(fechaDesde), LocalDate.parse(fechaHasta),
                        page, pageSize, "semana", "asc");
                filtro = "Rango: " + fechaDesde + " — " + fechaHasta;
            } else {
                actividades = actividadService.obtenerTodasActividadesPaginado(page, pageSize, "semana", "asc");
            }
        } catch (Exception e) {
            log.warn("Error al cargar actividades en raíz: {}", e.getMessage());
            actividades = actividadService.obtenerTodasActividadesPaginado(0, pageSize, "semana", "asc");
        }

        model.addAttribute("actividades", actividades.getContent());
        model.addAttribute("paginaActual", actividades.getNumber());
        model.addAttribute("totalPaginas", actividades.getTotalPages());
        model.addAttribute("totalElementos", actividades.getTotalElements());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("q", q);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("filtro", filtro);
        return "public/viewPublicActividades";
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        try {
            List<Tutor> tutores = tutorService.obtenerTodosTutores();
            List<Tutorado> tutorados = tutoradoService.obtenerTodosTutorados();
            List<Actividad> actividades = actividadService.obtenerTodasActividades();
            List<Grupo> grupos = grupoService.obtenerTodosGrupos();

            List<Actividad> proximasActividades = actividades.stream()
                    .filter(a -> a.getFecha() != null)
                    .sorted((a, b) -> a.getFecha().compareTo(b.getFecha()))
                    .limit(5)
                    .toList();

            log.info("Dashboard — tutores: {}, tutorados: {}, actividades: {}, grupos: {}",
                    tutores.size(), tutorados.size(), actividades.size(), grupos.size());

            model.addAttribute("totalTutores", tutores.size());
            model.addAttribute("totalTutorados", tutorados.size());
            model.addAttribute("totalActividades", actividades.size());
            model.addAttribute("totalGrupos", grupos.size());
            model.addAttribute("proximasActividades", proximasActividades);

        } catch (Exception e) {
            log.error("Error al cargar el dashboard: {}", e.getMessage());
            model.addAttribute("totalTutores", 0);
            model.addAttribute("totalTutorados", 0);
            model.addAttribute("totalActividades", 0);
            model.addAttribute("totalGrupos", 0);
            model.addAttribute("proximasActividades", List.of());
            model.addAttribute("msg_error", "Error al cargar el dashboard: " + e.getMessage());
        }

        return "index";
    }
}
