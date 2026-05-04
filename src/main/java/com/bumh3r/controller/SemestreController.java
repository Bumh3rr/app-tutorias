package com.bumh3r.controller;

import com.bumh3r.entity.Semestre;
import com.bumh3r.service.SemestreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping(value = "semestre")
public class SemestreController {

    @Autowired
    private SemestreService semestreService;

    private final Logger log = LoggerFactory.getLogger(SemestreController.class);

    @GetMapping()
    public String obtenerVistaListaSemestres(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "sort", required = false, defaultValue = "desc") String sort,
            @RequestParam(value = "sortBy", required = false, defaultValue = "anio") String sortBy,
            Model model) {

        if (!"asc".equals(sort) && !"desc".equals(sort)) sort = "desc";
        List<String> validSortFields = List.of("id", "anio", "periodo");
        if (!validSortFields.contains(sortBy)) sortBy = "anio";

        Sort.Direction direction = sort.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(direction, sortBy));
        Page<Semestre> pageResult = this.semestreService.obtenerTodosSemestresPage(pageable);

        log.info("semestres page: {}", pageResult.getContent());
        model.addAttribute("semestres", pageResult.getContent());
        model.addAttribute("paginaActual", pageResult.getNumber());
        model.addAttribute("totalPaginas", pageResult.getTotalPages());
        model.addAttribute("totalElementos", pageResult.getTotalElements());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("sort", sort);
        model.addAttribute("sortBy", sortBy);
        return "semestre/viewListaSemestre";
    }

    @GetMapping(value = "agregar")
    public String obtenerVistaAgregarSemestre(Model model) {
        model.addAttribute("semestre", new Semestre());
        model.addAttribute("isEdit", false);
        return "semestre/viewFormSemestre";
    }

    @PostMapping(value = "guardar")
    public String guardarSemestre(@Valid Semestre semestre, BindingResult result, Model model, RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "semestre/viewFormSemestre";
        }
        try {
            log.info("Guardar semestre: {}", semestre);
            this.semestreService.guardarSemestre(semestre);
            attributes.addFlashAttribute("msg_success", "Semestre guardado correctamente");
        } catch (Exception e) {
            model.addAttribute("msg_error", "Error al guardar el semestre: " + e.getMessage());
            model.addAttribute("isEdit", false);
            return "semestre/viewFormSemestre";
        }
        return "redirect:/semestre";
    }

    @GetMapping(value = "ver/{id}")
    public String obtenerVistaVerSemestre(@PathVariable Integer id, Model model) {
        Semestre semestre = this.semestreService.obtenerSemestre(id);
        log.info("Semestre: {}", semestre);
        model.addAttribute("semestre", semestre);
        return "semestre/viewInfoSemestre";
    }

    @GetMapping(value = "actualizar/{id}")
    public String obtenerVistaActualizarSemestre(@PathVariable Integer id, Model model) {
        Semestre semestre = this.semestreService.obtenerSemestre(id);
        log.info("Semestre a actualizar: {}", semestre);
        model.addAttribute("semestre", semestre);
        model.addAttribute("isEdit", true);
        return "semestre/viewFormSemestre";
    }

    @PostMapping(value = "actualizar/{id}")
    public String actualizarSemestre(@PathVariable Integer id, @Valid Semestre semestre, BindingResult result, Model model, RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "semestre/viewFormSemestre";
        }
        try {
            log.info("Actualizar semestre {}: {}", id, semestre);
            this.semestreService.actualizarSemestre(id, semestre);
            attributes.addFlashAttribute("msg_success", "Semestre actualizado correctamente");
        } catch (Exception e) {
            model.addAttribute("msg_error", "Error al actualizar el semestre: " + e.getMessage());
            model.addAttribute("isEdit", true);
            return "semestre/viewFormSemestre";
        }
        return "redirect:/semestre";
    }

    @GetMapping(value = "delete/{id}")
    public String obtenerVistaConfirmarEliminarSemestre(@PathVariable Integer id, Model model) {
        Semestre semestre = this.semestreService.obtenerSemestre(id);
        model.addAttribute("semestre", semestre);
        return "semestre/viewConfirmDeleteSemestre";
    }

    @PostMapping(value = "confirm/delete/{id}")
    public String eliminarSemestre(@PathVariable Integer id, RedirectAttributes attributes) {
        try {
            this.semestreService.eliminarSemestre(id);
            attributes.addFlashAttribute("msg_success", "Semestre eliminado correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al eliminar el semestre: " + e.getMessage());
        }
        return "redirect:/semestre";
    }
}