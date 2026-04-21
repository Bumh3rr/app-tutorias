package com.bumh3r.controller;

import com.bumh3r.entity.DeteccionNecesidades;
import com.bumh3r.entity.Sesion;
import com.bumh3r.entity.Tutorado;
import com.bumh3r.service.DeteccionNecesidadesService;
import com.bumh3r.service.SesionService;
import com.bumh3r.service.TutoradoService;
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
@RequestMapping(value = "deteccion")
public class DeteccionNecesidadesController {

    @Autowired
    private DeteccionNecesidadesService deteccionNecesidadesService;
    @Autowired
    private TutoradoService tutoradoService;
    @Autowired
    private SesionService sesionService;

    private static final Logger log = LoggerFactory.getLogger(DeteccionNecesidadesController.class);

    @GetMapping()
    public String obtenerVistaListaDetecciones(
            @RequestParam(value = "idTutorado", required = false) Integer idTutorado,
            @RequestParam(value = "idSesion", required = false) Integer idSesion,
            @RequestParam(value = "necesidad", required = false) String necesidad,
            @RequestParam(value = "tipoBusqueda", required = false, defaultValue = "todos") String tipoBusqueda,
            Model model) {

        List<DeteccionNecesidades> detecciones;
        List<Tutorado> tutorados = this.tutoradoService.obtenerTodosTutorados();
        List<Sesion> sesiones = this.sesionService.obtenerTodasSesiones();

        try {
            if ("tutorado".equals(tipoBusqueda) && idTutorado != null) {
                detecciones = this.deteccionNecesidadesService.buscarPorTutorado(idTutorado);
                model.addAttribute("filtro", "Tutorado seleccionado");

            } else if ("sesion".equals(tipoBusqueda) && idSesion != null) {
                detecciones = this.deteccionNecesidadesService.buscarPorSesion(idSesion);
                model.addAttribute("filtro", "Sesión seleccionada");

            } else if ("necesidad".equals(tipoBusqueda) && necesidad != null) {
                switch (necesidad) {
                    case "algebra"      -> { detecciones = this.deteccionNecesidadesService.buscarPorNecesidadAlgebra();      model.addAttribute("filtro", "Necesidad: Álgebra"); }
                    case "calculo"      -> { detecciones = this.deteccionNecesidadesService.buscarPorNecesidadCalculo();      model.addAttribute("filtro", "Necesidad: Cálculo"); }
                    case "economica"    -> { detecciones = this.deteccionNecesidadesService.buscarPorNecesidadEconomica();    model.addAttribute("filtro", "Necesidad: Económica"); }
                    case "psicologica"  -> { detecciones = this.deteccionNecesidadesService.buscarPorNecesidadPsicologica();  model.addAttribute("filtro", "Necesidad: Psicológica"); }
                    default             -> { detecciones = this.deteccionNecesidadesService.obtenerTodasDetecciones();        model.addAttribute("filtro", null); }
                }
            } else {
                detecciones = this.deteccionNecesidadesService.obtenerTodasDetecciones();
                model.addAttribute("filtro", null);
            }
        } catch (Exception e) {
            detecciones = this.deteccionNecesidadesService.obtenerTodasDetecciones();
            model.addAttribute("msg_error", "Error en la búsqueda: " + e.getMessage());
        }

        model.addAttribute("detecciones", detecciones);
        model.addAttribute("tutorados", tutorados);
        model.addAttribute("sesiones", sesiones);
        model.addAttribute("idTutoradoSeleccionado", idTutorado);
        model.addAttribute("idSesionSeleccionada", idSesion);
        return "deteccion/viewListaDeteccion";
    }

    @GetMapping(value = "agregar")
    public String obtenerVistaAgregarDeteccion(
            @RequestParam(value = "idTutorado", required = false) Integer idTutorado,
            @RequestParam(value = "idSesion", required = false) Integer idSesion,
            Model model) {

        DeteccionNecesidades deteccion = new DeteccionNecesidades();

        if (idTutorado != null) {
            Tutorado tutorado = this.tutoradoService.obtenerTutorado(idTutorado);
            deteccion.setTutorado(tutorado);
        }
        if (idSesion != null) {
            Sesion sesion = this.sesionService.obtenerSesion(idSesion);
            deteccion.setSesion(sesion);
        }

        model.addAttribute("deteccion", deteccion);
        model.addAttribute("tutorados", this.tutoradoService.obtenerTodosTutorados());
        model.addAttribute("sesiones", this.sesionService.obtenerTodasSesiones());
        model.addAttribute("isEdit", false);
        return "deteccion/viewFormDeteccion";
    }

    @PostMapping(value = "guardar")
    public String guardarDeteccion(DeteccionNecesidades deteccion, RedirectAttributes attributes) {
        try {
            log.info("Guardar deteccion: {}", deteccion);
            this.deteccionNecesidadesService.guardarDeteccion(deteccion);
            attributes.addFlashAttribute("msg_success", "Detección de necesidades guardada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al guardar la detección: " + e.getMessage());
        }
        return "redirect:/deteccion";
    }

    @GetMapping(value = "ver/{id}")
    public String obtenerVistaVerDeteccion(@PathVariable Integer id, Model model) {
        DeteccionNecesidades deteccion = this.deteccionNecesidadesService.obtenerDeteccion(id);
        log.info("Deteccion: {}", deteccion);
        model.addAttribute("deteccion", deteccion);
        return "deteccion/viewInfoDeteccion";
    }

    @GetMapping(value = "actualizar/{id}")
    public String obtenerVistaActualizarDeteccion(@PathVariable Integer id, Model model) {
        DeteccionNecesidades deteccion = this.deteccionNecesidadesService.obtenerDeteccion(id);
        log.info("Deteccion a actualizar: {}", deteccion);
        model.addAttribute("deteccion", deteccion);
        model.addAttribute("tutorados", this.tutoradoService.obtenerTodosTutorados());
        model.addAttribute("sesiones", this.sesionService.obtenerTodasSesiones());
        model.addAttribute("isEdit", true);
        return "deteccion/viewFormDeteccion";
    }

    @PostMapping(value = "actualizar/{id}")
    public String actualizarDeteccion(
            @PathVariable Integer id,
            DeteccionNecesidades deteccion,
            RedirectAttributes attributes) {
        try {
            log.info("Actualizar deteccion {}: {}", id, deteccion);
            this.deteccionNecesidadesService.actualizarDeteccion(id, deteccion);
            attributes.addFlashAttribute("msg_success", "Detección actualizada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al actualizar la detección: " + e.getMessage());
        }
        return "redirect:/deteccion";
    }

    @GetMapping(value = "delete/{id}")
    public String obtenerVistaConfirmarEliminarDeteccion(@PathVariable Integer id, Model model) {
        DeteccionNecesidades deteccion = this.deteccionNecesidadesService.obtenerDeteccion(id);
        model.addAttribute("deteccion", deteccion);
        return "deteccion/viewConfirmDeleteDeteccion";
    }

    @PostMapping(value = "confirm/delete/{id}")
    public String eliminarDeteccion(@PathVariable Integer id, RedirectAttributes attributes) {
        try {
            this.deteccionNecesidadesService.eliminarDeteccion(id);
            attributes.addFlashAttribute("msg_success", "Detección eliminada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al eliminar la detección: " + e.getMessage());
        }
        return "redirect:/deteccion";
    }

    @InitBinder
    public void initBinder(WebDataBinder webDataBinder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }
}