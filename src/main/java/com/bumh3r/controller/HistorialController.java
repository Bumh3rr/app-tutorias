package com.bumh3r.controller;

import com.bumh3r.entity.GrupoTutorado;
import com.bumh3r.service.CarreraService;
import com.bumh3r.service.GrupoTutoradoService;
import com.bumh3r.service.SemestreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = "historial")
public class HistorialController {

    @Autowired
    private GrupoTutoradoService grupoTutoradoService;
    @Autowired
    private SemestreService semestreService;
    @Autowired
    private CarreraService carreraService;

    @GetMapping
    public String obtenerVistaHistorial(
            @RequestParam(value = "q", required = false, defaultValue = "") String q,
            @RequestParam(value = "idSemestre", required = false) Integer idSemestre,
            @RequestParam(value = "idCarrera", required = false) Integer idCarrera,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            Model model) {

        Page<GrupoTutorado> pagina = this.grupoTutoradoService.buscarHistorial(q, idSemestre, idCarrera, page, pageSize);

        model.addAttribute("historial", pagina.getContent());
        model.addAttribute("totalPaginas", pagina.getTotalPages());
        model.addAttribute("totalElementos", pagina.getTotalElements());
        model.addAttribute("paginaActual", page);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("q", q);
        model.addAttribute("idSemestreSeleccionado", idSemestre);
        model.addAttribute("idCarreraSeleccionada", idCarrera);
        model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
        model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());

        return "historial/viewHistorialTutorias";
    }
}
