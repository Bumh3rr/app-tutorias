package com.bumh3r.controller;

import com.bumh3r.entity.Grupo;
import com.bumh3r.entity.Sesion;
import com.bumh3r.service.ActividadService;
import com.bumh3r.service.GrupoService;
import com.bumh3r.service.SesionService;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
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
@RequestMapping(value = "sesion")
public class SesionController {

    @Autowired
    private SesionService sesionService;
    @Autowired
    private GrupoService grupoService;
    @Autowired
    private ActividadService actividadService;

    private static final Logger log = LoggerFactory.getLogger(SesionController.class);

    @GetMapping()
    public String obtenerVistaListaSesiones(
            @RequestParam(value = "idGrupo", required = false) Integer idGrupo,
            @RequestParam(value = "semana", required = false) Integer semana,
            @RequestParam(value = "estatus", required = false) String estatus,
            @RequestParam(value = "tipoBusqueda", required = false, defaultValue = "todos") String tipoBusqueda,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "sort", required = false, defaultValue = "desc") String sort,
            @RequestParam(value = "sortBy", required = false, defaultValue = "id") String sortBy,
            Model model) {

        List<String> validSortFields = List.of("id", "semana");
        if (!validSortFields.contains(sortBy)) sortBy = "semana";
        if (!"asc".equals(sort) && !"desc".equals(sort)) sort = "asc";

        Sort.Direction direction = sort.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(direction, sortBy));
        List<Grupo> grupos = this.grupoService.obtenerTodosGrupos();

        org.springframework.data.domain.Page<Sesion> pageResult;
        try {
            if ("grupo".equals(tipoBusqueda) && idGrupo != null) {
                pageResult = this.sesionService.buscarSesionesPorGrupoPage(idGrupo, pageable);
                model.addAttribute("filtro", "Grupo seleccionado");
            } else if ("semana".equals(tipoBusqueda) && semana != null) {
                pageResult = this.sesionService.buscarSesionesPorSemanaPage(semana, pageable);
                model.addAttribute("filtro", "Semana " + semana);
            } else if ("grupoSemana".equals(tipoBusqueda) && idGrupo != null && semana != null) {
                pageResult = this.sesionService.buscarSesionesPorGrupoYSemanaPage(idGrupo, semana, pageable);
                model.addAttribute("filtro", "Grupo y semana seleccionados");
            } else if ("estatus".equals(tipoBusqueda) && estatus != null && !estatus.isEmpty()) {
                pageResult = this.sesionService.buscarSesionesPorEstatusPage(estatus, pageable);
                model.addAttribute("filtro", "Estatus: " + estatus);
            } else {
                pageResult = this.sesionService.obtenerTodasSesionesPage(pageable);
                model.addAttribute("filtro", null);
            }
        } catch (Exception e) {
            pageResult = this.sesionService.obtenerTodasSesionesPage(pageable);
            model.addAttribute("msg_error", "Error en la búsqueda: " + e.getMessage());
        }

        model.addAttribute("sesiones", pageResult.getContent());
        model.addAttribute("paginaActual", pageResult.getNumber());
        model.addAttribute("totalPaginas", pageResult.getTotalPages());
        model.addAttribute("totalElementos", pageResult.getTotalElements());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("sort", sort);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("grupos", grupos);
        model.addAttribute("idGrupoSeleccionado", idGrupo);
        model.addAttribute("semanaSeleccionada", semana);
        model.addAttribute("estatusSeleccionado", estatus);
        return "sesion/viewListaSesion";
    }

    @GetMapping(value = "agregar")
    public String obtenerVistaAgregarSesion(Model model) {
        model.addAttribute("sesion", new Sesion());
        model.addAttribute("grupos", this.grupoService.obtenerTodosGrupos());
        model.addAttribute("actividades", this.actividadService.obtenerTodasActividades());
        model.addAttribute("isEdit", false);
        return "sesion/viewFormSesion";
    }

    @PostMapping(value = "guardar")
    public String guardarSesion(@Valid Sesion sesion, BindingResult result, Model model, RedirectAttributes attributes) {
        if (sesion.getGrupo() == null || sesion.getGrupo().getId() == null) {
            result.rejectValue("grupo", "required", "El grupo es obligatorio");
        }
        if (result.hasErrors()) {
            model.addAttribute("grupos", this.grupoService.obtenerTodosGrupos());
            model.addAttribute("actividades", this.actividadService.obtenerTodasActividades());
            model.addAttribute("isEdit", false);
            return "sesion/viewFormSesion";
        }
        try {
            log.info("Guardar sesion: {}", sesion);
            this.sesionService.guardarSesion(sesion);
            attributes.addFlashAttribute("msg_success", "Sesión guardada correctamente");
        } catch (Exception e) {
            model.addAttribute("msg_error", "Error al guardar la sesión: " + e.getMessage());
            model.addAttribute("grupos", this.grupoService.obtenerTodosGrupos());
            model.addAttribute("actividades", this.actividadService.obtenerTodasActividades());
            model.addAttribute("isEdit", false);
            return "sesion/viewFormSesion";
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
        model.addAttribute("grupos", this.grupoService.obtenerTodosGrupos());
        model.addAttribute("actividades", this.actividadService.obtenerTodasActividades());
        model.addAttribute("isEdit", true);
        return "sesion/viewFormSesion";
    }

    @PostMapping(value = "actualizar/{id}")
    public String actualizarSesion(
            @PathVariable Integer id,
            @Valid Sesion sesion,
            BindingResult result,
            Model model,
            RedirectAttributes attributes) {
        if (sesion.getGrupo() == null || sesion.getGrupo().getId() == null) {
            result.rejectValue("grupo", "required", "El grupo es obligatorio");
        }
        if (result.hasErrors()) {
            model.addAttribute("grupos", this.grupoService.obtenerTodosGrupos());
            model.addAttribute("actividades", this.actividadService.obtenerTodasActividades());
            model.addAttribute("isEdit", true);
            return "sesion/viewFormSesion";
        }
        try {
            log.info("Actualizar sesion {}: {}", id, sesion);
            this.sesionService.actualizarSesion(id, sesion);
            attributes.addFlashAttribute("msg_success", "Sesión actualizada correctamente");
        } catch (Exception e) {
            model.addAttribute("msg_error", "Error al actualizar la sesión: " + e.getMessage());
            model.addAttribute("grupos", this.grupoService.obtenerTodosGrupos());
            model.addAttribute("actividades", this.actividadService.obtenerTodasActividades());
            model.addAttribute("isEdit", true);
            return "sesion/viewFormSesion";
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

    @GetMapping(value = "pdf/anexo19/{id}")
    public void generarAnexo19(@PathVariable Integer id, HttpServletResponse response) throws Exception {
        Sesion sesion = this.sesionService.obtenerSesion(id);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=anexo19-sesion-" + id + ".pdf");
        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();
        document.add(new Paragraph("Tecnológico Nacional de México — Campus Chilpancingo"));
        document.add(new Paragraph("Anexo 19 — Reporte de Sesión de Tutoría"));
        document.add(new Paragraph("Sesión #" + sesion.getId()
                + (sesion.getGrupo() != null ? " — Grupo: " + sesion.getGrupo().getNombre() : "")));
        document.add(new Paragraph("En desarrollo."));
        document.close();
    }
}
