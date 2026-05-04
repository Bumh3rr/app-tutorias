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
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sort", defaultValue = "desc") String sort,
            Model model) {

        Page<Actividad> actividades;
        List<PAT> pats = this.patService.obtenerTodosPAT();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            if ("fecha".equals(tipoBusqueda) && fecha != null && !fecha.isEmpty()) {
                Date fechaDate = sdf.parse(fecha);
                actividades = this.actividadService.buscarActividadesPorFechaPaginado(fechaDate, page, pageSize, sortBy, sort);
                model.addAttribute("filtro", "Fecha: " + fecha);

            } else if ("rango".equals(tipoBusqueda) && fechaInicio != null && fechaFin != null
                    && !fechaInicio.isEmpty() && !fechaFin.isEmpty()) {
                Date fInicio = sdf.parse(fechaInicio);
                Date fFin = sdf.parse(fechaFin);
                actividades = this.actividadService.buscarActividadesPorRangoFechasPaginado(fInicio, fFin, page, pageSize, sortBy, sort);
                model.addAttribute("filtro", "Rango: " + fechaInicio + " a " + fechaFin);

            } else if ("pat".equals(tipoBusqueda) && idPat != null) {
                actividades = this.actividadService.buscarActividadesPorPATPaginado(idPat, page, pageSize, sortBy, sort);
                model.addAttribute("filtro", "Por PAT seleccionado");

            } else {
                actividades = this.actividadService.obtenerTodasActividadesPaginado(page, pageSize, sortBy, sort);
                model.addAttribute("filtro", null);
            }
        } catch (Exception e) {
            actividades = this.actividadService.obtenerTodasActividadesPaginado(0, pageSize, "id", "desc");
            model.addAttribute("msg_error", "Error en la búsqueda: " + e.getMessage());
        }

        HashMap<String, String> mapSort = new LinkedHashMap<>();
        mapSort.put("id", "ID");
        mapSort.put("nombre", "Nombre");
        mapSort.put("semana", "Semana");
        mapSort.put("fecha", "Fecha");

        model.addAttribute("actividades", actividades.getContent());
        model.addAttribute("paginaActual", actividades.getNumber());
        model.addAttribute("totalElementos", actividades.getTotalElements());
        model.addAttribute("totalPaginas", actividades.getTotalPages());
        model.addAttribute("pageSize", pageSize);

        model.addAttribute("pats", pats);
        model.addAttribute("idPatSeleccionado", idPat);
        model.addAttribute("fechaSeleccionada", fecha);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("tipoBusqueda", tipoBusqueda);

        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sort", sort);
        model.addAttribute("mapSort", mapSort);

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
            @Valid Actividad actividad,
            BindingResult result,
            @RequestParam(value = "fotoFile", required = false) MultipartFile fotoFile,
            Model model,
            RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("pats", this.patService.obtenerTodosPAT());
            model.addAttribute("isEdit", false);
            return "actividad/viewFormActividad";
        }
        try {
            if (fotoFile != null && !fotoFile.isEmpty()) {
                String foto = this.fileStoreService.save(fotoFile, FileType.ACTIVIDAD);
                actividad.setFoto(foto);
            }
            log.info("Guardar actividad: {}", actividad);
            this.actividadService.guardarActividad(actividad);
            attributes.addFlashAttribute("msg_success", "Actividad guardada correctamente");
        } catch (Exception e) {
            model.addAttribute("msg_error", "Error al guardar la actividad: " + e.getMessage());
            model.addAttribute("pats", this.patService.obtenerTodosPAT());
            model.addAttribute("isEdit", false);
            return "actividad/viewFormActividad";
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
            @Valid Actividad actividad,
            BindingResult result,
            @RequestParam(value = "fotoFile", required = false) MultipartFile fotoFile,
            Model model,
            RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("pats", this.patService.obtenerTodosPAT());
            model.addAttribute("isEdit", true);
            return "actividad/viewFormActividad";
        }
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
            model.addAttribute("msg_error", "Error al actualizar la actividad: " + e.getMessage());
            model.addAttribute("pats", this.patService.obtenerTodosPAT());
            model.addAttribute("isEdit", true);
            return "actividad/viewFormActividad";
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