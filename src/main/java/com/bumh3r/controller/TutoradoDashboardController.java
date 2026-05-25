package com.bumh3r.controller;

import com.bumh3r.entity.Tutorado;
import com.bumh3r.entity.Usuario;
import com.bumh3r.repository.IUsuarioRepository;
import com.bumh3r.service.AsistenciaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("tutorado")
public class TutoradoDashboardController {

    @Autowired private IUsuarioRepository usuarioRepository;
    @Autowired private AsistenciaService asistenciaService;

    @GetMapping({"", "/"})
    public String miTutoria(Authentication auth, Model model) {
        Usuario u = usuarioRepository.findByUsername(auth.getName()).orElse(null);
        Tutorado tutorado = (u != null) ? u.getTutorado() : null;

        if (tutorado != null) {
            model.addAttribute("tutorado", tutorado);
            try {
                var resumen = asistenciaService.calcularResumenAsistencia(tutorado.getId());
                model.addAttribute("resumen", resumen);
            } catch (Exception e) {
                log.warn("No se pudo cargar resumen de asistencia para tutorado id={}", tutorado.getId());
            }
        }
        return "tutorado/mi-tutoria";
    }
}
