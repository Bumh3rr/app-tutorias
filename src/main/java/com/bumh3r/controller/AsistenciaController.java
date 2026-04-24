package com.bumh3r.controller;

import com.bumh3r.dto.ResumenAsistenciaDTO;
import com.bumh3r.entity.Asistencia;
import com.bumh3r.entity.Sesion;
import com.bumh3r.entity.Tutorado;
import com.bumh3r.service.AsistenciaService;
import com.bumh3r.service.SesionService;
import com.bumh3r.service.TutoradoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping(value = "asistencia")
public class AsistenciaController {

    @Autowired
    private AsistenciaService asistenciaService;
    @Autowired
    private SesionService sesionService;
    @Autowired
    private TutoradoService tutoradoService;

    private static final Logger log = LoggerFactory.getLogger(AsistenciaController.class);

    @GetMapping()
    public String obtenerVistaListaAsistencias(
            @RequestParam(value = "idSesion", required = false) Integer idSesion,
            @RequestParam(value = "idTutorado", required = false) Integer idTutorado,
            @RequestParam(value = "tipoBusqueda", required = false, defaultValue = "todos") String tipoBusqueda,
            Model model) {

        List<Asistencia> asistencias;
        List<Sesion> sesiones = this.sesionService.obtenerTodasSesiones();
        List<Tutorado> tutorados = this.tutoradoService.obtenerTodosTutorados();

        try {
            if ("sesion".equals(tipoBusqueda) && idSesion != null) {
                asistencias = this.asistenciaService.buscarAsistenciasPorSesion(idSesion);
                model.addAttribute("filtro", "Sesión seleccionada");

            } else if ("tutorado".equals(tipoBusqueda) && idTutorado != null) {
                asistencias = this.asistenciaService.buscarAsistenciasPorTutorado(idTutorado);
                model.addAttribute("filtro", "Tutorado seleccionado");

            } else {
                asistencias = this.asistenciaService.obtenerTodasAsistencias();
                model.addAttribute("filtro", null);
            }
        } catch (Exception e) {
            asistencias = this.asistenciaService.obtenerTodasAsistencias();
            model.addAttribute("msg_error", "Error en la búsqueda: " + e.getMessage());
        }

        model.addAttribute("asistencias", asistencias);
        model.addAttribute("sesiones", sesiones);
        model.addAttribute("tutorados", tutorados);
        model.addAttribute("idSesionSeleccionada", idSesion);
        model.addAttribute("idTutoradoSeleccionado", idTutorado);
        return "asistencia/viewListaAsistencia";
    }

    // Vista especial para registrar asistencia masiva de una sesión
    @GetMapping(value = "registrar/{idSesion}")
    public String obtenerVistaRegistrarAsistencia(
            @PathVariable Integer idSesion, Model model) {

        Sesion sesion = this.sesionService.obtenerSesion(idSesion);
        List<Tutorado> tutorados = this.tutoradoService.obtenerTodosTutorados();
        List<Asistencia> asistenciasExistentes = this.asistenciaService
                .buscarAsistenciasPorSesion(idSesion);

        log.info("Registrando asistencia para sesion: {}", idSesion);
        model.addAttribute("sesion", sesion);
        model.addAttribute("tutorados", tutorados);
        model.addAttribute("asistenciasExistentes", asistenciasExistentes);
        return "asistencia/viewRegistrarAsistencia";
    }

    // POST para registrar asistencia masiva
    @PostMapping(value = "registrar/{idSesion}")
    public String registrarAsistenciaMasiva(
            @PathVariable Integer idSesion,
            @RequestParam(value = "idsTutoradosPresentes", required = false) Integer[] idsTutoradosPresentes,
            RedirectAttributes attributes) {
        try {
            log.info("Asistencia masiva sesion {}: {} presentes", idSesion,
                    idsTutoradosPresentes != null ? idsTutoradosPresentes.length : 0);
            this.asistenciaService.registrarAsistenciaMasiva(idSesion, idsTutoradosPresentes);
            attributes.addFlashAttribute("msg_success", "Asistencia registrada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al registrar asistencia: " + e.getMessage());
        }
        return "redirect:/sesion/ver/" + idSesion;
    }

    // Ver resumen de asistencia de un tutorado con cálculo del 80%
    @GetMapping(value = "resumen/{idTutorado}")
    public String obtenerVistaResumenAsistencia(
            @PathVariable Integer idTutorado, Model model) {

        try {
            ResumenAsistenciaDTO resumen = this.asistenciaService
                    .calcularResumenAsistencia(idTutorado);
            List<Asistencia> historial = this.asistenciaService
                    .buscarAsistenciasPorTutorado(idTutorado);

            log.info("Resumen asistencia tutorado {}: {}%", idTutorado, resumen.getPorcentaje());
            model.addAttribute("resumen", resumen);
            model.addAttribute("historial", historial);
        } catch (Exception e) {
            model.addAttribute("msg_error", "Error al calcular resumen: " + e.getMessage());
        }

        return "asistencia/viewResumenAsistencia";
    }

    @GetMapping(value = "agregar")
    public String obtenerVistaAgregarAsistencia(Model model) {
        model.addAttribute("asistencia", new Asistencia());
        model.addAttribute("sesiones", this.sesionService.obtenerTodasSesiones());
        model.addAttribute("tutorados", this.tutoradoService.obtenerTodosTutorados());
        model.addAttribute("isEdit", false);
        return "asistencia/viewFormAsistencia";
    }

    @PostMapping(value = "guardar")
    public String guardarAsistencia(Asistencia asistencia, RedirectAttributes attributes) {
        try {
            log.info("Guardar asistencia: {}", asistencia);
            this.asistenciaService.guardarAsistencia(asistencia);
            attributes.addFlashAttribute("msg_success", "Asistencia guardada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al guardar la asistencia: " + e.getMessage());
        }
        return "redirect:/asistencia";
    }

    @GetMapping(value = "actualizar/{id}")
    public String obtenerVistaActualizarAsistencia(@PathVariable Integer id, Model model) {
        Asistencia asistencia = this.asistenciaService.obtenerAsistencia(id);
        log.info("Asistencia a actualizar: {}", asistencia);
        model.addAttribute("asistencia", asistencia);
        model.addAttribute("sesiones", this.sesionService.obtenerTodasSesiones());
        model.addAttribute("tutorados", this.tutoradoService.obtenerTodosTutorados());
        model.addAttribute("isEdit", true);
        return "asistencia/viewFormAsistencia";
    }

    @PostMapping(value = "actualizar/{id}")
    public String actualizarAsistencia(
            @PathVariable Integer id,
            Asistencia asistencia,
            RedirectAttributes attributes) {
        try {
            log.info("Actualizar asistencia {}: {}", id, asistencia);
            this.asistenciaService.actualizarAsistencia(id, asistencia);
            attributes.addFlashAttribute("msg_success", "Asistencia actualizada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al actualizar la asistencia: " + e.getMessage());
        }
        return "redirect:/asistencia";
    }

    @PostMapping(value = "recuperar/{id}")
    public String marcarAsistenciaRecuperada(
            @PathVariable Integer id,
            @RequestParam(value = "idTutorado") Integer idTutorado,
            RedirectAttributes attributes) {
        try {
            Asistencia asistencia = this.asistenciaService.obtenerAsistencia(id);

            if (asistencia == null) {
                attributes.addFlashAttribute("msg_error", "Asistencia no encontrada.");
                return "redirect:/asistencia/resumen/" + idTutorado;
            }

            if (asistencia.getPresente() == 1) {
                attributes.addFlashAttribute("msg_error",
                        "No se puede recuperar una asistencia que ya está presente.");
                return "redirect:/asistencia/resumen/" + idTutorado;
            }

            asistencia.setRecuperada(1);
            this.asistenciaService.actualizarAsistencia(id, asistencia);
            attributes.addFlashAttribute("msg_success",
                    "Asistencia marcada como recuperada correctamente.");

        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error",
                    "Error al recuperar asistencia: " + e.getMessage());
        }
        return "redirect:/asistencia/resumen/" + idTutorado;
    }

    @GetMapping(value = "delete/{id}")
    public String obtenerVistaConfirmarEliminarAsistencia(@PathVariable Integer id, Model model) {
        Asistencia asistencia = this.asistenciaService.obtenerAsistencia(id);
        model.addAttribute("asistencia", asistencia);
        return "asistencia/viewConfirmDeleteAsistencia";
    }

    @PostMapping(value = "confirm/delete/{id}")
    public String eliminarAsistencia(@PathVariable Integer id, RedirectAttributes attributes) {
        try {
            this.asistenciaService.eliminarAsistencia(id);
            attributes.addFlashAttribute("msg_success", "Asistencia eliminada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al eliminar la asistencia: " + e.getMessage());
        }
        return "redirect:/asistencia";
    }
}