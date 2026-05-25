package com.bumh3r.controller;

import com.bumh3r.entity.Grupo;
import com.bumh3r.entity.Tutor;
import com.bumh3r.entity.Usuario;
import com.bumh3r.repository.IUsuarioRepository;
import com.bumh3r.service.GrupoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("tutor")
public class TutorDashboardController {

    @Autowired private IUsuarioRepository usuarioRepository;
    @Autowired private GrupoService grupoService;

    @GetMapping({"", "/"})
    public String dashboard(Authentication auth, Model model) {
        Usuario u = usuarioRepository.findByUsername(auth.getName()).orElse(null);
        Tutor tutor = (u != null) ? u.getTutor() : null;

        if (tutor != null) {
            List<Grupo> grupos = grupoService.obtenerTodosGrupos().stream()
                    .filter(g -> g.getTutor() != null && g.getTutor().getId().equals(tutor.getId())
                                 && Integer.valueOf(1).equals(g.getActivo()))
                    .toList();
            model.addAttribute("tutor", tutor);
            model.addAttribute("grupos", grupos);
        }
        return "tutor/dashboard";
    }
}
