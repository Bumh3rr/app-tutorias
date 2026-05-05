package com.bumh3r.controller;

import com.bumh3r.entity.DeteccionNecesidades;
import com.bumh3r.entity.Sesion;
import com.bumh3r.entity.Tutorado;
import com.bumh3r.service.DeteccionNecesidadesService;
import com.bumh3r.service.SesionService;
import com.bumh3r.service.TutoradoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "sort", defaultValue = "desc") String sort,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            Model model) {

        if (!"asc".equals(sort) && !"desc".equals(sort)) sort = "desc";
        if (!"id".equals(sortBy)) sortBy = "id";

        Sort.Direction direction = "desc".equals(sort) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(direction, sortBy));
        List<Tutorado> tutorados = this.tutoradoService.obtenerTodosTutorados();
        List<Sesion> sesiones = this.sesionService.obtenerTodasSesiones();

        Page<DeteccionNecesidades> pageResult;
        try {
            if ("tutorado".equals(tipoBusqueda) && idTutorado != null) {
                List<DeteccionNecesidades> lista = this.deteccionNecesidadesService.buscarPorTutorado(idTutorado);
                pageResult = new PageImpl<>(paginate(lista, pageable), pageable, lista.size());
                model.addAttribute("filtro", "Tutorado seleccionado");
            } else if ("sesion".equals(tipoBusqueda) && idSesion != null) {
                List<DeteccionNecesidades> lista = this.deteccionNecesidadesService.buscarPorSesion(idSesion);
                pageResult = new PageImpl<>(paginate(lista, pageable), pageable, lista.size());
                model.addAttribute("filtro", "Sesión seleccionada");
            } else if ("necesidad".equals(tipoBusqueda) && necesidad != null) {
                List<DeteccionNecesidades> lista;
                String filtroMsg;
                switch (necesidad) {
                    case "algebra"     -> { lista = this.deteccionNecesidadesService.buscarPorNecesidadAlgebra();     filtroMsg = "Necesidad: Álgebra"; }
                    case "calculo"     -> { lista = this.deteccionNecesidadesService.buscarPorNecesidadCalculo();     filtroMsg = "Necesidad: Cálculo"; }
                    case "economica"   -> { lista = this.deteccionNecesidadesService.buscarPorNecesidadEconomica();   filtroMsg = "Necesidad: Económica"; }
                    case "psicologica" -> { lista = this.deteccionNecesidadesService.buscarPorNecesidadPsicologica(); filtroMsg = "Necesidad: Psicológica"; }
                    default            -> { lista = this.deteccionNecesidadesService.obtenerTodasDetecciones();       filtroMsg = null; }
                }
                pageResult = new PageImpl<>(paginate(lista, pageable), pageable, lista.size());
                model.addAttribute("filtro", filtroMsg);
            } else {
                pageResult = this.deteccionNecesidadesService.obtenerTodasDeteccionesPage(pageable);
                model.addAttribute("filtro", null);
            }
        } catch (Exception e) {
            pageResult = this.deteccionNecesidadesService.obtenerTodasDeteccionesPage(pageable);
            model.addAttribute("msg_error", "Error en la búsqueda: " + e.getMessage());
        }

        model.addAttribute("detecciones", pageResult.getContent());
        model.addAttribute("paginaActual", pageResult.getNumber());
        model.addAttribute("totalPaginas", pageResult.getTotalPages());
        model.addAttribute("totalElementos", pageResult.getTotalElements());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("sort", sort);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("tutorados", tutorados);
        model.addAttribute("sesiones", sesiones);
        model.addAttribute("idTutoradoSeleccionado", idTutorado);
        model.addAttribute("idSesionSeleccionada", idSesion);
        model.addAttribute("tipoBusqueda", tipoBusqueda);
        return "deteccion/viewListaDeteccion";
    }

    private <T> List<T> paginate(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), list.size());
        return start >= list.size() ? List.of() : list.subList(start, end);
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
    public String guardarDeteccion(
            @Valid DeteccionNecesidades deteccion,
            BindingResult result,
            Model model,
            RedirectAttributes attributes) {
        if (deteccion.getTutorado() == null || deteccion.getTutorado().getId() == null) {
            result.rejectValue("tutorado", "required", "El tutorado es obligatorio");
        }
        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "deteccion/viewFormDeteccion";
        }
        try {
            log.info("Guardar deteccion: {}", deteccion);
            this.deteccionNecesidadesService.guardarDeteccion(deteccion);
            attributes.addFlashAttribute("msg_success", "Detección de necesidades guardada correctamente");
        } catch (Exception e) {
            model.addAttribute("msg_error", "Error al guardar la detección: " + e.getMessage());
            model.addAttribute("isEdit", false);
            return "deteccion/viewFormDeteccion";
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
            @Valid DeteccionNecesidades deteccion,
            BindingResult result,
            Model model,
            RedirectAttributes attributes) {
        if (deteccion.getTutorado() == null || deteccion.getTutorado().getId() == null) {
            result.rejectValue("tutorado", "required", "El tutorado es obligatorio");
        }
        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "deteccion/viewFormDeteccion";
        }
        try {
            log.info("Actualizar deteccion {}: {}", id, deteccion);
            this.deteccionNecesidadesService.actualizarDeteccion(id, deteccion);
            attributes.addFlashAttribute("msg_success", "Detección actualizada correctamente");
        } catch (Exception e) {
            model.addAttribute("msg_error", "Error al actualizar la detección: " + e.getMessage());
            model.addAttribute("isEdit", true);
            return "deteccion/viewFormDeteccion";
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