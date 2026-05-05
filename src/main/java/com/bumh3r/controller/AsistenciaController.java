package com.bumh3r.controller;

import com.bumh3r.dto.ResumenAsistenciaDTO;
import com.bumh3r.entity.Asistencia;
import com.bumh3r.entity.GrupoTutorado;
import com.bumh3r.entity.Sesion;
import com.bumh3r.entity.Tutorado;
import com.bumh3r.service.AsistenciaService;
import com.bumh3r.service.GrupoTutoradoService;
import com.bumh3r.service.SesionService;
import com.bumh3r.service.TutoradoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "asistencia")
public class AsistenciaController {

    @Autowired
    private AsistenciaService asistenciaService;
    @Autowired
    private SesionService sesionService;
    @Autowired
    private TutoradoService tutoradoService;
    @Autowired
    private GrupoTutoradoService grupoTutoradoService;

    private static final Logger log = LoggerFactory.getLogger(AsistenciaController.class);

    @GetMapping()
    public String obtenerVistaListaAsistencias(
            @RequestParam(value = "idSesion", required = false) Integer idSesion,
            @RequestParam(value = "idTutorado", required = false) Integer idTutorado,
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
        List<Sesion> sesiones = this.sesionService.obtenerTodasSesiones();
        List<Tutorado> tutorados = this.tutoradoService.obtenerTodosTutorados();

        Page<Asistencia> pageResult;
        try {
            if ("sesion".equals(tipoBusqueda) && idSesion != null) {
                List<Asistencia> lista = this.asistenciaService.buscarAsistenciasPorSesion(idSesion);
                pageResult = new PageImpl<>(paginate(lista, pageable), pageable, lista.size());
                model.addAttribute("filtro", "Sesión seleccionada");
            } else if ("tutorado".equals(tipoBusqueda) && idTutorado != null) {
                List<Asistencia> lista = this.asistenciaService.buscarAsistenciasPorTutorado(idTutorado);
                pageResult = new PageImpl<>(paginate(lista, pageable), pageable, lista.size());
                model.addAttribute("filtro", "Tutorado seleccionado");
            } else {
                pageResult = this.asistenciaService.obtenerTodasAsistenciasPage(pageable);
                model.addAttribute("filtro", null);
            }
        } catch (Exception e) {
            pageResult = this.asistenciaService.obtenerTodasAsistenciasPage(pageable);
            model.addAttribute("msg_error", "Error en la búsqueda: " + e.getMessage());
        }

        model.addAttribute("asistencias", pageResult.getContent());
        model.addAttribute("paginaActual", pageResult.getNumber());
        model.addAttribute("totalPaginas", pageResult.getTotalPages());
        model.addAttribute("totalElementos", pageResult.getTotalElements());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("sort", sort);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sesiones", sesiones);
        model.addAttribute("tutorados", tutorados);
        model.addAttribute("idSesionSeleccionada", idSesion);
        model.addAttribute("idTutoradoSeleccionado", idTutorado);
        model.addAttribute("tipoBusqueda", tipoBusqueda);
        return "asistencia/viewListaAsistencia";
    }

    private <T> List<T> paginate(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), list.size());
        return start >= list.size() ? List.of() : list.subList(start, end);
    }

    // Vista especial para registrar asistencia masiva de una sesión
    @GetMapping(value = "registrar/{idSesion}")
    public String obtenerVistaRegistrarAsistencia(
            @PathVariable Integer idSesion, Model model) {

        Sesion sesion = this.sesionService.obtenerSesion(idSesion);

        List<GrupoTutorado> grupoTutorados = (sesion != null && sesion.getGrupo() != null)
                ? this.grupoTutoradoService.buscarPorGrupo(sesion.getGrupo().getId())
                : List.of();

        List<Asistencia> asistenciasExistentes = this.asistenciaService
                .buscarAsistenciasPorSesion(idSesion);

        Map<Integer, Asistencia> mapaAsistencias = new HashMap<>();
        for (Asistencia a : asistenciasExistentes) {
            if (a.getTutorado() != null) {
                mapaAsistencias.put(a.getTutorado().getId(), a);
            }
        }

        log.info("Registrando asistencia para sesion: {}, tutorados del grupo: {}", idSesion, grupoTutorados.size());
        model.addAttribute("sesion", sesion);
        model.addAttribute("grupoTutorados", grupoTutorados);
        model.addAttribute("mapaAsistencias", mapaAsistencias);
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