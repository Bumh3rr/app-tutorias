package com.bumh3r.controller;

import com.bumh3r.entity.Actividad;
import com.bumh3r.entity.AsignacionTutorado;
import com.bumh3r.entity.Tutor;
import com.bumh3r.entity.Tutorado;
import com.bumh3r.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    @Autowired
    private TutorService tutorService;
    @Autowired
    private TutoradoService tutoradoService;
    @Autowired
    private ActividadService actividadService;
    @Autowired
    private AsignacionTutoradoService asignacionTutoradoService;
    @Autowired
    private PATService patService;

    @GetMapping({"/", ""})
    public String dashboard(Model model) {
        try {
            List<Tutor> tutores = tutorService.obtenerTodosTutores();
            List<Tutorado> tutorados = tutoradoService.obtenerTodosTutorados();
            List<Actividad> actividades = actividadService.obtenerTodasActividades();
            List<AsignacionTutorado> asignaciones = asignacionTutoradoService.obtenerTodasAsignaciones();

            // Próximas 5 actividades (ya vienen ordenadas por fecha desde el repo)
            List<Actividad> proximasActividades = actividades.stream()
                    .filter(a -> a.getFecha() != null)
                    .sorted((a, b) -> a.getFecha().compareTo(b.getFecha()))
                    .limit(5)
                    .toList();

            log.info("Dashboard — tutores: {}, tutorados: {}, actividades: {}, asignaciones: {}",
                    tutores.size(), tutorados.size(), actividades.size(), asignaciones.size());

            model.addAttribute("totalTutores", tutores.size());
            model.addAttribute("totalTutorados", tutorados.size());
            model.addAttribute("totalActividades", actividades.size());
            model.addAttribute("totalAsignaciones", asignaciones.size());
            model.addAttribute("proximasActividades", proximasActividades);

        } catch (Exception e) {
            log.error("Error al cargar el dashboard: {}", e.getMessage());
            model.addAttribute("totalTutores", 0);
            model.addAttribute("totalTutorados", 0);
            model.addAttribute("totalActividades", 0);
            model.addAttribute("totalAsignaciones", 0);
            model.addAttribute("proximasActividades", List.of());
            model.addAttribute("msg_error", "Error al cargar el dashboard: " + e.getMessage());
        }

        return "index";
    }
}