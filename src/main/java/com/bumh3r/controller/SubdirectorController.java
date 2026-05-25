package com.bumh3r.controller;

import com.bumh3r.service.GrupoService;
import com.bumh3r.service.TutorService;
import com.bumh3r.service.TutoradoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("subdirector")
public class SubdirectorController {

    @Autowired private TutorService tutorService;
    @Autowired private TutoradoService tutoradoService;
    @Autowired private GrupoService grupoService;

    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        model.addAttribute("totalTutores",   tutorService.obtenerTodosTutores().size());
        model.addAttribute("totalTutorados", tutoradoService.obtenerTodosTutorados().size());
        model.addAttribute("totalGrupos",    grupoService.obtenerTodosGrupos().size());
        return "subdirector/dashboard";
    }
}
