package com.bumh3r.controller;

import com.bumh3r.entity.EvidenciaSesion;
import com.bumh3r.entity.Sesion;
import com.bumh3r.service.EvidenciaSesionService;
import com.bumh3r.service.FileStoreService;
import com.bumh3r.service.SesionService;
import com.bumh3r.service.enums.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(value = "evidencia")
public class EvidenciaSesionController {

    @Autowired
    private EvidenciaSesionService evidenciaSesionService;
    @Autowired
    private SesionService sesionService;
    @Autowired
    private FileStoreService fileStoreService;

    private static final Logger log = LoggerFactory.getLogger(EvidenciaSesionController.class);

    @GetMapping()
    public String obtenerVistaListaEvidencias(
            @RequestParam(value = "idSesion", required = false) Integer idSesion,
            @RequestParam(value = "estatus", required = false) String estatus,
            @RequestParam(value = "tipoBusqueda", required = false, defaultValue = "todos") String tipoBusqueda,
            Model model) {

        List<EvidenciaSesion> evidencias;
        List<Sesion> sesiones = this.sesionService.obtenerTodasSesiones();

        try {
            if ("sesion".equals(tipoBusqueda) && idSesion != null) {
                evidencias = this.evidenciaSesionService.buscarEvidenciasPorSesion(idSesion);
                model.addAttribute("filtro", "Sesión seleccionada");

            } else {
                evidencias = this.evidenciaSesionService.obtenerTodasEvidencias();
                model.addAttribute("filtro", null);
            }
        } catch (Exception e) {
            evidencias = this.evidenciaSesionService.obtenerTodasEvidencias();
            model.addAttribute("msg_error", "Error en la búsqueda: " + e.getMessage());
        }

        model.addAttribute("evidencias", evidencias);
        model.addAttribute("sesiones", sesiones);
        model.addAttribute("idSesionSeleccionada", idSesion);
        return "evidencia/viewListaEvidencia";
    }

    @GetMapping(value = "agregar")
    public String obtenerVistaAgregarEvidencia(
            @RequestParam(value = "idSesion", required = false) Integer idSesion,
            Model model) {

        EvidenciaSesion evidencia = new EvidenciaSesion();
        if (idSesion != null) {
            Sesion sesion = this.sesionService.obtenerSesion(idSesion);
            evidencia.setSesion(sesion);
        }
        model.addAttribute("evidencia", evidencia);
        model.addAttribute("sesiones", this.sesionService.obtenerTodasSesiones());
        model.addAttribute("isEdit", false);
        return "evidencia/viewFormEvidencia";
    }

    @PostMapping(value = "guardar")
    public String guardarEvidencia(
            EvidenciaSesion evidencia,
            @RequestParam(value = "archivoFile", required = false) MultipartFile archivoFile,
            RedirectAttributes attributes) {
        try {
            if (archivoFile != null && !archivoFile.isEmpty()) {
                String archivo = this.fileStoreService.save(archivoFile, FileType.EVIDENCIA);
                evidencia.setArchivoUrl(archivo);
            }
            evidencia.setFechaSubida(new Date());
            log.info("Guardar evidencia: {}", evidencia);
            this.evidenciaSesionService.guardarEvidencia(evidencia);
            attributes.addFlashAttribute("msg_success", "Evidencia guardada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al guardar la evidencia: " + e.getMessage());
        }
        return "redirect:/evidencia";
    }

    @GetMapping(value = "ver/{id}")
    public String obtenerVistaVerEvidencia(@PathVariable Integer id, Model model) {
        EvidenciaSesion evidencia = this.evidenciaSesionService.obtenerEvidencia(id);
        log.info("Evidencia: {}", evidencia);
        model.addAttribute("evidencia", evidencia);
        return "evidencia/viewInfoEvidencia";
    }

    @GetMapping(value = "actualizar/{id}")
    public String obtenerVistaActualizarEvidencia(@PathVariable Integer id, Model model) {
        EvidenciaSesion evidencia = this.evidenciaSesionService.obtenerEvidencia(id);
        log.info("Evidencia a actualizar: {}", evidencia);
        model.addAttribute("evidencia", evidencia);
        model.addAttribute("sesiones", this.sesionService.obtenerTodasSesiones());
        model.addAttribute("isEdit", true);
        return "evidencia/viewFormEvidencia";
    }

    @PostMapping(value = "actualizar/{id}")
    public String actualizarEvidencia(
            @PathVariable Integer id,
            EvidenciaSesion evidencia,
            @RequestParam(value = "archivoFile", required = false) MultipartFile archivoFile,
            RedirectAttributes attributes) {
        try {
            if (archivoFile != null && !archivoFile.isEmpty()) {
                this.fileStoreService.delete(evidencia.getArchivoUrl(), FileType.EVIDENCIA);
                String archivo = this.fileStoreService.save(archivoFile, FileType.EVIDENCIA);
                evidencia.setArchivoUrl(archivo);
            }
            log.info("Actualizar evidencia {}: {}", id, evidencia);
            this.evidenciaSesionService.actualizarEvidencia(id, evidencia);
            attributes.addFlashAttribute("msg_success", "Evidencia actualizada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al actualizar la evidencia: " + e.getMessage());
        }
        return "redirect:/evidencia";
    }

    // Validar evidencia
    @PostMapping(value = "validar/{id}")
    public String validarEvidencia(
            @PathVariable Integer id,
            @RequestParam(value = "notas", required = false) String notas,
            RedirectAttributes attributes) {
        try {
            this.evidenciaSesionService.validarEvidencia(id, notas);
            attributes.addFlashAttribute("msg_success", "Evidencia validada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al validar la evidencia: " + e.getMessage());
        }
        return "redirect:/evidencia/ver/" + id;
    }

    // Rechazar evidencia
    @PostMapping(value = "rechazar/{id}")
    public String rechazarEvidencia(
            @PathVariable Integer id,
            @RequestParam(value = "notas", required = false) String notas,
            RedirectAttributes attributes) {
        try {
            this.evidenciaSesionService.rechazarEvidencia(id, notas);
            attributes.addFlashAttribute("msg_success", "Evidencia rechazada");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al rechazar la evidencia: " + e.getMessage());
        }
        return "redirect:/evidencia/ver/" + id;
    }

    @GetMapping(value = "delete/{id}")
    public String obtenerVistaConfirmarEliminarEvidencia(@PathVariable Integer id, Model model) {
        EvidenciaSesion evidencia = this.evidenciaSesionService.obtenerEvidencia(id);
        model.addAttribute("evidencia", evidencia);
        return "evidencia/viewConfirmDeleteEvidencia";
    }

    @PostMapping(value = "confirm/delete/{id}")
    public String eliminarEvidencia(@PathVariable Integer id, RedirectAttributes attributes) {
        try {
            this.evidenciaSesionService.eliminarEvidencia(id);
            attributes.addFlashAttribute("msg_success", "Evidencia eliminada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al eliminar la evidencia: " + e.getMessage());
        }
        return "redirect:/evidencia";
    }

    @InitBinder
    public void initBinder(WebDataBinder webDataBinder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }
}