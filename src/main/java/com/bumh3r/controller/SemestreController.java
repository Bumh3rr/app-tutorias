package com.bumh3r.controller;

import com.bumh3r.entity.Semestre;
import com.bumh3r.service.SemestreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    public String obtenerVistaListaSemestres(Model model) {
        List<Semestre> semestres = this.semestreService.obtenerTodosSemestres();
        log.info("semestres: {}", semestres);
        model.addAttribute("semestres", semestres);
        return "semestre/viewListaSemestre";
    }

    @GetMapping(value = "agregar")
    public String obtenerVistaAgregarSemestre(Model model) {
        model.addAttribute("semestre", new Semestre());
        model.addAttribute("isEdit", false);
        return "semestre/viewFormSemestre";
    }

    @PostMapping(value = "guardar")
    public String guardarSemestre(Semestre semestre, RedirectAttributes attributes) {
        try {
            log.info("Guardar semestre: {}", semestre);
            this.semestreService.guardarSemestre(semestre);
            attributes.addFlashAttribute("msg_success", "Semestre guardado correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al guardar el semestre: " + e.getMessage());
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
    public String actualizarSemestre(@PathVariable Integer id, Semestre semestre, RedirectAttributes attributes) {
        try {
            log.info("Actualizar semestre {}: {}", id, semestre);
            this.semestreService.actualizarSemestre(id, semestre);
            attributes.addFlashAttribute("msg_success", "Semestre actualizado correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al actualizar el semestre: " + e.getMessage());
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