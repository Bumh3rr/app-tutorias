package com.bumh3r.controller;

import com.bumh3r.entity.ReporteSesion;
import com.bumh3r.entity.Sesion;
import com.bumh3r.service.ReporteSesionService;
import com.bumh3r.service.SesionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(value = "reporte")
public class ReporteSesionController {

    @Autowired
    private ReporteSesionService reporteSesionService;
    @Autowired
    private SesionService sesionService;

    private static final Logger log = LoggerFactory.getLogger(ReporteSesionController.class);

    @GetMapping()
    public String obtenerVistaListaReportes(
            @RequestParam(value = "estatus", required = false) String estatus,
            @RequestParam(value = "tipoBusqueda", required = false, defaultValue = "todos") String tipoBusqueda,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "sort", defaultValue = "desc") String sort,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            Model model) {

        if (!"asc".equals(sort) && !"desc".equals(sort)) sort = "desc";
        if (!"id".equals(sortBy)) sortBy = "id";

        Sort.Direction direction = "desc".equals(sort) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(direction, sortBy));

        Page<ReporteSesion> pageResult;
        try {
            if ("estatus".equals(tipoBusqueda) && estatus != null && !estatus.isEmpty()) {
                pageResult = this.reporteSesionService.buscarPorEstatusPage(estatus, pageable);
                model.addAttribute("filtro", "Estatus: " + estatus);
            } else {
                pageResult = this.reporteSesionService.obtenerTodosReportesPage(pageable);
                model.addAttribute("filtro", null);
            }
        } catch (Exception e) {
            pageResult = this.reporteSesionService.obtenerTodosReportesPage(pageable);
            model.addAttribute("msg_error", "Error en la búsqueda: " + e.getMessage());
        }

        model.addAttribute("reportes", pageResult.getContent());
        model.addAttribute("paginaActual", pageResult.getNumber());
        model.addAttribute("totalPaginas", pageResult.getTotalPages());
        model.addAttribute("totalElementos", pageResult.getTotalElements());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("sort", sort);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("estatusSeleccionado", estatus);
        model.addAttribute("tipoBusqueda", tipoBusqueda);
        return "reporte/viewListaReporte";
    }

    private <T> List<T> paginate(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), list.size());
        return start >= list.size() ? List.of() : list.subList(start, end);
    }

    // Agregar reporte desde la vista de sesión
    @GetMapping(value = "agregar")
    public String obtenerVistaAgregarReporte(
            @RequestParam(value = "idSesion", required = false) Integer idSesion,
            Model model) {

        ReporteSesion reporte = new ReporteSesion();

        if (idSesion != null) {
            Sesion sesion = this.sesionService.obtenerSesion(idSesion);
            reporte.setSesion(sesion);
            // Pre-llenar alumnos presentes si ya hay asistencias
        }

        model.addAttribute("reporte", reporte);
        model.addAttribute("sesiones", this.sesionService.obtenerTodasSesiones());
        model.addAttribute("isEdit", false);
        return "reporte/viewFormReporte";
    }

    @PostMapping(value = "guardar")
    public String guardarReporte(ReporteSesion reporte, RedirectAttributes attributes) {
        try {
            log.info("Guardar reporte: {}", reporte);
            this.reporteSesionService.guardarReporte(reporte);
            attributes.addFlashAttribute("msg_success", "Reporte guardado correctamente");

            // Si viene de una sesión, redirigir a esa sesión
            if (reporte.getSesion() != null && reporte.getSesion().getId() != null) {
                return "redirect:/sesion/ver/" + reporte.getSesion().getId();
            }
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al guardar el reporte: " + e.getMessage());
        }
        return "redirect:/reporte";
    }

    @GetMapping(value = "ver/{id}")
    public String obtenerVistaVerReporte(@PathVariable Integer id, Model model) {
        ReporteSesion reporte = this.reporteSesionService.obtenerReporte(id);
        log.info("Reporte: {}", reporte);
        model.addAttribute("reporte", reporte);
        return "reporte/viewInfoReporte";
    }

    @GetMapping(value = "sesion/{idSesion}")
    public String obtenerVistaReportePorSesion(@PathVariable Integer idSesion, Model model) {
        ReporteSesion reporte = this.reporteSesionService.obtenerReportePorSesion(idSesion);
        if (reporte == null) {
            return "redirect:/reporte/agregar?idSesion=" + idSesion;
        }
        model.addAttribute("reporte", reporte);
        return "reporte/viewInfoReporte";
    }

    @GetMapping(value = "actualizar/{id}")
    public String obtenerVistaActualizarReporte(@PathVariable Integer id, Model model) {
        ReporteSesion reporte = this.reporteSesionService.obtenerReporte(id);
        log.info("Reporte a actualizar: {}", reporte);
        model.addAttribute("reporte", reporte);
        model.addAttribute("sesiones", this.sesionService.obtenerTodasSesiones());
        model.addAttribute("isEdit", true);
        return "reporte/viewFormReporte";
    }

    @PostMapping(value = "actualizar/{id}")
    public String actualizarReporte(
            @PathVariable Integer id,
            ReporteSesion reporte,
            RedirectAttributes attributes) {
        try {
            log.info("Actualizar reporte {}: {}", id, reporte);
            this.reporteSesionService.actualizarReporte(id, reporte);
            attributes.addFlashAttribute("msg_success", "Reporte actualizado correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al actualizar el reporte: " + e.getMessage());
        }
        return "redirect:/reporte";
    }

    @GetMapping(value = "delete/{id}")
    public String obtenerVistaConfirmarEliminarReporte(@PathVariable Integer id, Model model) {
        ReporteSesion reporte = this.reporteSesionService.obtenerReporte(id);
        model.addAttribute("reporte", reporte);
        return "reporte/viewConfirmDeleteReporte";
    }

    @PostMapping(value = "confirm/delete/{id}")
    public String eliminarReporte(@PathVariable Integer id, RedirectAttributes attributes) {
        try {
            this.reporteSesionService.eliminarReporte(id);
            attributes.addFlashAttribute("msg_success", "Reporte eliminado correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al eliminar el reporte: " + e.getMessage());
        }
        return "redirect:/reporte";
    }

    @InitBinder
    public void initBinder(WebDataBinder webDataBinder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }
}