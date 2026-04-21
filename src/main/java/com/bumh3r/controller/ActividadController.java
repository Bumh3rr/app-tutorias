package com.bumh3r.controller;

import com.bumh3r.entity.Actividad;
import com.bumh3r.entity.PAT;
import com.bumh3r.service.ActividadService;
import com.bumh3r.service.FileStoreService;
import com.bumh3r.service.PATService;
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
@RequestMapping(value = "actividad")
public class ActividadController {

    @Autowired
    private ActividadService actividadService;
    @Autowired
    private PATService patService;
    @Autowired
    private FileStoreService fileStoreService;

    private final Logger log = LoggerFactory.getLogger(ActividadController.class);

    @GetMapping()
    public String obtenerVistaListaActividades(
            @RequestParam(value = "fecha", required = false) String fecha,
            @RequestParam(value = "fechaInicio", required = false) String fechaInicio,
            @RequestParam(value = "fechaFin", required = false) String fechaFin,
            @RequestParam(value = "idPat", required = false) Integer idPat,
            @RequestParam(value = "tipoBusqueda", required = false, defaultValue = "todos") String tipoBusqueda,
            Model model) {

        List<Actividad> actividades;
        List<PAT> pats = this.patService.obtenerTodosPAT();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            if ("fecha".equals(tipoBusqueda) && fecha != null && !fecha.isEmpty()) {
                Date fechaDate = sdf.parse(fecha);
                actividades = this.actividadService.buscarActividadesPorFecha(fechaDate);
                model.addAttribute("filtro", "Fecha: " + fecha);

            } else if ("rango".equals(tipoBusqueda) && fechaInicio != null && fechaFin != null
                    && !fechaInicio.isEmpty() && !fechaFin.isEmpty()) {
                Date fInicio = sdf.parse(fechaInicio);
                Date fFin = sdf.parse(fechaFin);
                actividades = this.actividadService.buscarActividadesPorRangoFechas(fInicio, fFin);
                model.addAttribute("filtro", "Rango: " + fechaInicio + " a " + fechaFin);

            } else if ("pat".equals(tipoBusqueda) && idPat != null) {
                actividades = this.actividadService.buscarActividadesPorPAT(idPat);
                model.addAttribute("filtro", "Por PAT seleccionado");

            } else {
                actividades = this.actividadService.obtenerTodasActividades();
                model.addAttribute("filtro", null);
            }
        } catch (Exception e) {
            actividades = this.actividadService.obtenerTodasActividades();
            model.addAttribute("msg_error", "Error en la búsqueda: " + e.getMessage());
        }

        model.addAttribute("actividades", actividades);
        model.addAttribute("pats", pats);
        model.addAttribute("idPatSeleccionado", idPat);
        return "actividad/viewListaActividad";
    }

    @GetMapping(value = "agregar")
    public String obtenerVistaAgregarActividad(Model model) {
        model.addAttribute("actividad", new Actividad());
        model.addAttribute("pats", this.patService.obtenerTodosPAT());
        model.addAttribute("isEdit", false);
        return "actividad/viewFormActividad";
    }

    @PostMapping(value = "guardar")
    public String guardarActividad(
            Actividad actividad,
            @RequestParam(value = "fotoFile", required = false) MultipartFile fotoFile,
            RedirectAttributes attributes) {
        try {
            if (fotoFile != null && !fotoFile.isEmpty()) {
                String foto = this.fileStoreService.save(fotoFile, FileType.ACTIVIDAD);
                actividad.setFoto(foto);
            }
            log.info("Guardar actividad: {}", actividad);
            this.actividadService.guardarActividad(actividad);
            attributes.addFlashAttribute("msg_success", "Actividad guardada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al guardar la actividad: " + e.getMessage());
        }
        return "redirect:/actividad";
    }

    @GetMapping(value = "ver/{id}")
    public String obtenerVistaVerActividad(@PathVariable Integer id, Model model) {
        Actividad actividad = this.actividadService.obtenerActividad(id);
        log.info("Actividad: {}", actividad);
        model.addAttribute("actividad", actividad);
        return "actividad/viewInfoActividad";
    }

    @GetMapping(value = "actualizar/{id}")
    public String obtenerVistaActualizarActividad(@PathVariable Integer id, Model model) {
        Actividad actividad = this.actividadService.obtenerActividad(id);
        log.info("Actividad a actualizar: {}", actividad);
        model.addAttribute("actividad", actividad);
        model.addAttribute("pats", this.patService.obtenerTodosPAT());
        model.addAttribute("isEdit", true);
        return "actividad/viewFormActividad";
    }

    @PostMapping(value = "actualizar/{id}")
    public String actualizarActividad(
            @PathVariable Integer id,
            Actividad actividad,
            @RequestParam(value = "fotoFile", required = false) MultipartFile fotoFile,
            RedirectAttributes attributes) {
        try {
            if (fotoFile != null && !fotoFile.isEmpty()) {
                this.fileStoreService.delete(actividad.getFoto(), FileType.ACTIVIDAD);
                String foto = this.fileStoreService.save(fotoFile, FileType.ACTIVIDAD);
                actividad.setFoto(foto);
            }
            log.info("Actualizar actividad {}: {}", id, actividad);
            this.actividadService.actualizarActividad(id, actividad);
            attributes.addFlashAttribute("msg_success", "Actividad actualizada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al actualizar la actividad: " + e.getMessage());
        }
        return "redirect:/actividad";
    }

    @GetMapping(value = "delete/{id}")
    public String obtenerVistaConfirmarEliminarActividad(@PathVariable Integer id, Model model) {
        Actividad actividad = this.actividadService.obtenerActividad(id);
        model.addAttribute("actividad", actividad);
        return "actividad/viewConfirmDeleteActividad";
    }

    @PostMapping(value = "confirm/delete/{id}")
    public String eliminarActividad(@PathVariable Integer id, RedirectAttributes attributes) {
        try {
            this.actividadService.eliminarActividad(id);
            attributes.addFlashAttribute("msg_success", "Actividad eliminada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al eliminar la actividad: " + e.getMessage());
        }
        return "redirect:/actividad";
    }

    @InitBinder
    public void initBinder(WebDataBinder webDataBinder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }
}