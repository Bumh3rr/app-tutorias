package com.bumh3r.controller;

import com.bumh3r.entity.Carrera;
import com.bumh3r.service.CarreraService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping(value = "carrera")
public class CarreraController {

    @Autowired
    private CarreraService carreraService;

    private final Logger log = LoggerFactory.getLogger(CarreraController.class);

    @GetMapping()
    public String obtenerVistaListaCarreras(Model model) {
        List<Carrera> carreras = this.carreraService.obtenerTodasCarreras();
        log.info("carreras: {}", carreras);
        model.addAttribute("carreras", carreras);
        return "carrera/viewListaCarrera";
    }

    @GetMapping(value = "agregar")
    public String obtenerVistaAgregarCarrera(Model model) {
        model.addAttribute("carrera", new Carrera());
        model.addAttribute("isEdit", false);
        return "carrera/viewFormCarrera";
    }

    @PostMapping(value = "guardar")
    public String guardarCarrera(Carrera carrera, RedirectAttributes attributes) {
        try {
            log.info("Guardar carrera: {}", carrera);
            this.carreraService.guardarCarrera(carrera);
            attributes.addFlashAttribute("msg_success", "Carrera guardada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al guardar la carrera: " + e.getMessage());
        }
        return "redirect:/carrera";
    }

    @GetMapping(value = "ver/{id}")
    public String obtenerVistaVerCarrera(@PathVariable Integer id, Model model) {
        Carrera carrera = this.carreraService.obtenerCarrera(id);
        log.info("Carrera: {}", carrera);
        model.addAttribute("carrera", carrera);
        return "carrera/viewInfoCarrera";
    }

    @GetMapping(value = "actualizar/{id}")
    public String obtenerVistaActualizarCarrera(@PathVariable Integer id, Model model) {
        Carrera carrera = this.carreraService.obtenerCarrera(id);
        log.info("Carrera a actualizar: {}", carrera);
        model.addAttribute("carrera", carrera);
        model.addAttribute("isEdit", true);
        return "carrera/viewFormCarrera";
    }

    @PostMapping(value = "actualizar/{id}")
    public String actualizarCarrera(@PathVariable Integer id, Carrera carrera, RedirectAttributes attributes) {
        try {
            log.info("Actualizar carrera {}: {}", id, carrera);
            this.carreraService.actualizarCarrera(id, carrera);
            attributes.addFlashAttribute("msg_success", "Carrera actualizada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al actualizar la carrera: " + e.getMessage());
        }
        return "redirect:/carrera";
    }

    @GetMapping(value = "delete/{id}")
    public String obtenerVistaConfirmarEliminarCarrera(@PathVariable Integer id, Model model) {
        Carrera carrera = this.carreraService.obtenerCarrera(id);
        model.addAttribute("carrera", carrera);
        return "carrera/viewConfirmDeleteCarrera";
    }

    @PostMapping(value = "confirm/delete/{id}")
    public String eliminarCarrera(@PathVariable Integer id, RedirectAttributes attributes) {
        try {
            this.carreraService.eliminarCarrera(id);
            attributes.addFlashAttribute("msg_success", "Carrera eliminada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al eliminar la carrera: " + e.getMessage());
        }
        return "redirect:/carrera";
    }
}