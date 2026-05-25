package com.bumh3r.controller;

import com.bumh3r.entity.CoordinadorCarrera;
import com.bumh3r.entity.Usuario;
import com.bumh3r.repository.IUsuarioRepository;
import com.bumh3r.service.EvidenciaSesionService;
import com.bumh3r.service.TutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("coordinador")
public class CoordinadorDashboardController {

    @Autowired private IUsuarioRepository usuarioRepository;
    @Autowired private TutorService tutorService;
    @Autowired private EvidenciaSesionService evidenciaSesionService;

    @GetMapping({"", "/"})
    public String dashboard(Authentication auth, Model model) {
        Usuario u = usuarioRepository.findByUsername(auth.getName()).orElse(null);
        CoordinadorCarrera coord = (u != null) ? u.getCoordinador() : null;

        if (coord != null) {
            model.addAttribute("coordinador", coord);
            if (coord.getCarrera() != null) {
                var tutores = tutorService.obtenerTodosTutores().stream()
                        .filter(t -> Integer.valueOf(1).equals(t.getActivo()))
                        .toList();
                model.addAttribute("tutores", tutores);
            }
        }
        return "coordinador/dashboard";
    }
}
