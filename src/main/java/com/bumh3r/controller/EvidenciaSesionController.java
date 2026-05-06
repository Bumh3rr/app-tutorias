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
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.validation.BindingResult;
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
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "sort", defaultValue = "desc") String sort,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "fechaInicio", required = false, defaultValue = "") String fechaInicio,
            @RequestParam(value = "fechaFin", required = false, defaultValue = "") String fechaFin,
            Model model) {

        if (!"asc".equals(sort) && !"desc".equals(sort)) sort = "desc";
        if (!"id".equals(sortBy)) sortBy = "id";

        Sort.Direction direction = "desc".equals(sort) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(direction, sortBy));
        List<Sesion> sesiones = this.sesionService.obtenerTodasSesiones();

        Page<EvidenciaSesion> pageResult;
        try {
            if ("sesion".equals(tipoBusqueda) && idSesion != null) {
                List<EvidenciaSesion> lista = this.evidenciaSesionService.buscarEvidenciasPorSesion(idSesion);
                pageResult = new PageImpl<>(paginate(lista, pageable), pageable, lista.size());
                model.addAttribute("filtro", "Sesión seleccionada");
            } else if ("fecha".equals(tipoBusqueda) && !fechaInicio.isBlank() && !fechaFin.isBlank()) {
                try { Date ini = new SimpleDateFormat("yyyy-MM-dd").parse(fechaInicio); Date fin2 = new Date(new SimpleDateFormat("yyyy-MM-dd").parse(fechaFin).getTime() + 86399999L); java.util.List<com.bumh3r.entity.EvidenciaSesion> lista = this.evidenciaSesionService.buscarEvidenciasPorFechaRegistro(ini, fin2); pageResult = new org.springframework.data.domain.PageImpl<>(paginate(lista, pageable), pageable, lista.size()); } catch (Exception ex) { pageResult = this.evidenciaSesionService.obtenerTodasEvidenciasPage(pageable); }
                model.addAttribute("filtro", "Fecha: " + fechaInicio + " – " + fechaFin);
            } else {
                pageResult = this.evidenciaSesionService.obtenerTodasEvidenciasPage(pageable);
                model.addAttribute("filtro", null);
            }
        } catch (Exception e) {
            pageResult = this.evidenciaSesionService.obtenerTodasEvidenciasPage(pageable);
            model.addAttribute("msg_error", "Error en la búsqueda: " + e.getMessage());
        }

        model.addAttribute("evidencias", pageResult.getContent());
        model.addAttribute("paginaActual", pageResult.getNumber());
        model.addAttribute("totalPaginas", pageResult.getTotalPages());
        model.addAttribute("totalElementos", pageResult.getTotalElements());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("sort", sort);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sesiones", sesiones);
        model.addAttribute("idSesionSeleccionada", idSesion);
        model.addAttribute("tipoBusqueda", tipoBusqueda);
                model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        return "evidencia/viewListaEvidencia";
    }

    private <T> List<T> paginate(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), list.size());
        return start >= list.size() ? List.of() : list.subList(start, end);
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
            @Valid EvidenciaSesion evidencia,
            BindingResult result,
            @RequestParam(value = "archivoFile", required = false) MultipartFile archivoFile,
            Model model,
            RedirectAttributes attributes) {
        if (evidencia.getSesion() == null || evidencia.getSesion().getId() == null) {
            result.rejectValue("sesion", "required", "La sesión es obligatoria");
        }
        if (result.hasErrors()) {
            model.addAttribute("sesiones", this.sesionService.obtenerTodasSesiones());
            model.addAttribute("isEdit", false);
            return "evidencia/viewFormEvidencia";
        }
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
            model.addAttribute("msg_error", "Error al guardar la evidencia: " + e.getMessage());
            model.addAttribute("sesiones", this.sesionService.obtenerTodasSesiones());
            model.addAttribute("isEdit", false);
            return "evidencia/viewFormEvidencia";
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
            @Valid EvidenciaSesion evidencia,
            BindingResult result,
            @RequestParam(value = "archivoFile", required = false) MultipartFile archivoFile,
            Model model,
            RedirectAttributes attributes) {
        if (evidencia.getSesion() == null || evidencia.getSesion().getId() == null) {
            result.rejectValue("sesion", "required", "La sesión es obligatoria");
        }
        if (result.hasErrors()) {
            model.addAttribute("sesiones", this.sesionService.obtenerTodasSesiones());
            model.addAttribute("isEdit", true);
            return "evidencia/viewFormEvidencia";
        }
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
            model.addAttribute("msg_error", "Error al actualizar la evidencia: " + e.getMessage());
            model.addAttribute("sesiones", this.sesionService.obtenerTodasSesiones());
            model.addAttribute("isEdit", true);
            return "evidencia/viewFormEvidencia";
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