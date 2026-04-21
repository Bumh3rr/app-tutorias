package com.bumh3r.controller;

import com.bumh3r.entity.Sesion;
import com.bumh3r.entity.Tutor;
import com.bumh3r.service.ActividadService;
import com.bumh3r.service.SesionService;
import com.bumh3r.service.TutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(value = "sesion")
public class SesionController {

    @Autowired
    private SesionService sesionService;
    @Autowired
    private TutorService tutorService;
    @Autowired
    private ActividadService actividadService;

    private static final Logger log = LoggerFactory.getLogger(SesionController.class);

    @GetMapping()
    public String obtenerVistaListaSesiones(
            @RequestParam(value = "idTutor", required = false) Integer idTutor,
            @RequestParam(value = "semana", required = false) Integer semana,
            @RequestParam(value = "estatus", required = false) String estatus,
            @RequestParam(value = "tipoBusqueda", required = false, defaultValue = "todos") String tipoBusqueda,
            Model model) {

        List<Sesion> sesiones;
        List<Tutor> tutores = this.tutorService.obtenerTodosTutores();

        try {
            if ("tutor".equals(tipoBusqueda) && idTutor != null) {
                sesiones = this.sesionService.buscarSesionesPorTutor(idTutor);
                model.addAttribute("filtro", "Tutor seleccionado");

            } else if ("semana".equals(tipoBusqueda) && semana != null) {
                sesiones = this.sesionService.buscarSesionesPorSemana(semana);
                model.addAttribute("filtro", "Semana " + semana);

            } else if ("tutorSemana".equals(tipoBusqueda) && idTutor != null && semana != null) {
                sesiones = this.sesionService.buscarSesionesPorTutorYSemana(idTutor, semana);
                model.addAttribute("filtro", "Tutor y semana seleccionados");

            } else if ("estatus".equals(tipoBusqueda) && estatus != null && !estatus.isEmpty()) {
                sesiones = this.sesionService.buscarSesionesPorEstatus(estatus);
                model.addAttribute("filtro", "Estatus: " + estatus);

            } else {
                sesiones = this.sesionService.obtenerTodasSesiones();
                model.addAttribute("filtro", null);
            }
        } catch (Exception e) {
            sesiones = this.sesionService.obtenerTodasSesiones();
            model.addAttribute("msg_error", "Error en la búsqueda: " + e.getMessage());
        }

        model.addAttribute("sesiones", sesiones);
        model.addAttribute("tutores", tutores);
        model.addAttribute("idTutorSeleccionado", idTutor);
        model.addAttribute("semanaSeleccionada", semana);
        model.addAttribute("estatusSeleccionado", estatus);
        return "sesion/viewListaSesion";
    }

    @GetMapping(value = "agregar")
    public String obtenerVistaAgregarSesion(Model model) {
        model.addAttribute("sesion", new Sesion());
        model.addAttribute("tutores", this.tutorService.obtenerTodosTutores());
        model.addAttribute("actividades", this.actividadService.obtenerTodasActividades());
        model.addAttribute("isEdit", false);
        return "sesion/viewFormSesion";
    }

    @PostMapping(value = "guardar")
    public String guardarSesion(Sesion sesion, RedirectAttributes attributes) {
        try {
            log.info("Guardar sesion: {}", sesion);
            this.sesionService.guardarSesion(sesion);
            attributes.addFlashAttribute("msg_success", "Sesión guardada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al guardar la sesión: " + e.getMessage());
        }
        return "redirect:/sesion";
    }

    @GetMapping(value = "ver/{id}")
    public String obtenerVistaVerSesion(@PathVariable Integer id, Model model) {
        Sesion sesion = this.sesionService.obtenerSesion(id);
        log.info("Sesion: {}", sesion);
        model.addAttribute("sesion", sesion);
        return "sesion/viewInfoSesion";
    }

    @GetMapping(value = "actualizar/{id}")
    public String obtenerVistaActualizarSesion(@PathVariable Integer id, Model model) {
        Sesion sesion = this.sesionService.obtenerSesion(id);
        log.info("Sesion a actualizar: {}", sesion);
        model.addAttribute("sesion", sesion);
        model.addAttribute("tutores", this.tutorService.obtenerTodosTutores());
        model.addAttribute("actividades", this.actividadService.obtenerTodasActividades());
        model.addAttribute("isEdit", true);
        return "sesion/viewFormSesion";
    }

    @PostMapping(value = "actualizar/{id}")
    public String actualizarSesion(
            @PathVariable Integer id,
            Sesion sesion,
            RedirectAttributes attributes) {
        try {
            log.info("Actualizar sesion {}: {}", id, sesion);
            this.sesionService.actualizarSesion(id, sesion);
            attributes.addFlashAttribute("msg_success", "Sesión actualizada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al actualizar la sesión: " + e.getMessage());
        }
        return "redirect:/sesion";
    }

    @GetMapping(value = "delete/{id}")
    public String obtenerVistaConfirmarEliminarSesion(@PathVariable Integer id, Model model) {
        Sesion sesion = this.sesionService.obtenerSesion(id);
        model.addAttribute("sesion", sesion);
        return "sesion/viewConfirmDeleteSesion";
    }

    @PostMapping(value = "confirm/delete/{id}")
    public String eliminarSesion(@PathVariable Integer id, RedirectAttributes attributes) {
        try {
            this.sesionService.eliminarSesion(id);
            attributes.addFlashAttribute("msg_success", "Sesión eliminada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al eliminar la sesión: " + e.getMessage());
        }
        return "redirect:/sesion";
    }

    @InitBinder
    public void initBinder(WebDataBinder webDataBinder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }
}