package com.bumh3r.controller;

import com.bumh3r.dto.PublicDeteccionForm;
import com.bumh3r.entity.*;
import com.bumh3r.repository.*;
import com.bumh3r.service.ActividadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "public")
public class PublicController {

    private static final Logger log = LoggerFactory.getLogger(PublicController.class);

    @Autowired private ActividadService actividadService;
    @Autowired private ITutoradoRepository tutoradoRepository;
    @Autowired private IGrupoTutoradoRepository grupoTutoradoRepository;
    @Autowired private ISesionRepository sesionRepository;
    @Autowired private IDeteccionNecesidadesRepository deteccionRepository;

    // ── Actividades públicas ──────────────────────────────────────────────────

    @GetMapping(value = "actividades")
    public String vistaActividades(
            @RequestParam(value = "q", required = false, defaultValue = "") String q,
            @RequestParam(value = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(value = "fechaHasta", required = false) String fechaHasta,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "12") int pageSize,
            Model model) {

        Page<Actividad> actividades;
        String filtro = null;
        try {
            if (!q.isBlank()) {
                actividades = actividadService.buscarActividadesPorNombrePaginado(q, page, pageSize, "semana", "asc");
                filtro = "Nombre: " + q;
            } else if (fechaDesde != null && !fechaDesde.isBlank() && fechaHasta != null && !fechaHasta.isBlank()) {
                LocalDate dDesde = LocalDate.parse(fechaDesde);
                LocalDate dHasta = LocalDate.parse(fechaHasta);
                actividades = actividadService.buscarActividadesPorRangoFechasPaginado(dDesde, dHasta, page, pageSize, "semana", "asc");
                filtro = "Rango: " + fechaDesde + " — " + fechaHasta;
            } else {
                actividades = actividadService.obtenerTodasActividadesPaginado(page, pageSize, "semana", "asc");
            }
        } catch (Exception e) {
            actividades = actividadService.obtenerTodasActividadesPaginado(0, pageSize, "semana", "asc");
        }

        model.addAttribute("actividades", actividades.getContent());
        model.addAttribute("paginaActual", actividades.getNumber());
        model.addAttribute("totalPaginas", actividades.getTotalPages());
        model.addAttribute("totalElementos", actividades.getTotalElements());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("q", q);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("filtro", filtro);
        return "public/viewPublicActividades";
    }

    @GetMapping(value = "actividades/{id}")
    public String vistaDetalleActividad(@PathVariable Integer id, Model model) {
        model.addAttribute("actividad", actividadService.obtenerActividad(id));
        return "public/viewPublicDetalleActividad";
    }

    // ── Detección pública ─────────────────────────────────────────────────────

    @GetMapping(value = "deteccion")
    public String vistaInicio(
            @ModelAttribute("msg_error") String msgError,
            Model model) {
        return "public/viewPublicDeteccionInicio";
    }

    @PostMapping(value = "deteccion/buscar")
    public String buscarTutorado(
            @RequestParam("numeroControl") String numeroControl,
            Model model,
            RedirectAttributes attrs) {

        String nc = numeroControl.trim();
        Optional<Tutorado> opt = tutoradoRepository.findByNumeroControlAndActivo(nc, 1);
        if (opt.isEmpty()) {
            model.addAttribute("msg_error", "No se encontró ningún tutorado activo con el número de control: " + nc);
            model.addAttribute("numeroControl", nc);
            return "public/viewPublicDeteccionInicio";
        }
        return "redirect:/public/deteccion/" + opt.get().getId();
    }

    @GetMapping(value = "deteccion/{idTutorado}")
    public String vistaFormulario(@PathVariable Integer idTutorado, Model model) {
        Optional<Tutorado> optTutorado = tutoradoRepository.findById(idTutorado);
        if (optTutorado.isEmpty()) return "redirect:/public/deteccion";
        Tutorado tutorado = optTutorado.get();
        if (tutorado.getActivo() == null || tutorado.getActivo() != 1) return "redirect:/public/deteccion";

        // Find grupo
        List<GrupoTutorado> gts = grupoTutoradoRepository.findByActivoAndTutorado(1, tutorado);
        Grupo grupo = gts.isEmpty() ? null : gts.get(0).getGrupo();

        // Find sesion
        Sesion sesion = null;
        if (grupo != null) {
            List<Sesion> s1 = sesionRepository.findByActivoAndGrupoAndSemana(1, grupo, 1);
            if (!s1.isEmpty()) {
                sesion = s1.get(0);
            } else {
                sesion = sesionRepository
                        .findTopByGrupoAndEstatusRegistroAndActivoOrderBySemanaDesc(grupo, "REALIZADA", 1)
                        .orElse(null);
                if (sesion == null) {
                    sesion = sesionRepository.findTopByGrupoAndActivoOrderBySemanaDesc(grupo, 1).orElse(null);
                }
            }
        }

        // Check if already answered
        boolean yaContesto = deteccionRepository.existsByTutoradoAndActivo(tutorado, 1);
        DeteccionNecesidades deteccion = yaContesto
                ? deteccionRepository.findFirstByTutoradoAndActivo(tutorado, 1).orElse(new DeteccionNecesidades())
                : new DeteccionNecesidades();

        Tutor tutor = (grupo != null) ? grupo.getTutor() : null;
        Semestre semestre = (grupo != null) ? grupo.getSemestre() : null;

        model.addAttribute("tutorado", tutorado);
        model.addAttribute("sesion", sesion);
        model.addAttribute("grupo", grupo);
        model.addAttribute("tutor", tutor);
        model.addAttribute("semestre", semestre);
        model.addAttribute("deteccion", deteccion);
        model.addAttribute("yaContesto", yaContesto);
        return "public/viewPublicDeteccion";
    }

    @PostMapping(value = "deteccion/{idTutorado}")
    public String guardarDeteccion(
            @PathVariable Integer idTutorado,
            @ModelAttribute PublicDeteccionForm form,
            RedirectAttributes attrs) {

        try {
            Optional<Tutorado> optTutorado = tutoradoRepository.findById(idTutorado);
            if (optTutorado.isEmpty()) {
                attrs.addFlashAttribute("msg_error", "Tutorado no encontrado.");
                return "redirect:/public/deteccion";
            }
            Tutorado tutorado = optTutorado.get();

            if (deteccionRepository.existsByTutoradoAndActivo(tutorado, 1)) {
                attrs.addFlashAttribute("msg_error", "Ya enviaste tu formulario anteriormente.");
                return "redirect:/public/deteccion/" + idTutorado;
            }

            // Update sexo
            if (form.getSexo() != null && !form.getSexo().isBlank()) {
                tutorado.setSexo(form.getSexo());
                tutoradoRepository.save(tutorado);
            }

            DeteccionNecesidades d = new DeteccionNecesidades();
            d.setTutorado(tutorado);

            if (form.getIdSesion() != null) {
                sesionRepository.findById(form.getIdSesion()).ifPresent(d::setSesion);
            }

            d.setNecesidadAlgebra(coalesce(form.getNecesidadAlgebra(), 0));
            d.setNecesidadCalculo(coalesce(form.getNecesidadCalculo(), 0));
            d.setNecesidadDerecho(coalesce(form.getNecesidadDerecho(), 0));
            d.setNecesidadOtra(form.getNecesidadOtra());
            d.setNecesidadEconomica(coalesce(form.getNecesidadEconomica(), 0));
            d.setNecesidadPsicologica(coalesce(form.getNecesidadPsicologica(), 0));
            d.setTemaPsicologico(form.getTemaPsicologico());
            d.setTieneBeca(coalesce(form.getTieneBeca(), 0));
            d.setNombreBeca(form.getNombreBeca());
            d.setTieneEscasezMateriales(coalesce(form.getTieneEscasezMateriales(), 0));
            d.setMaterialesRequeridos(form.getMaterialesRequeridos());
            d.setTieneAtencionMedica(coalesce(form.getTieneAtencionMedica(), 0));
            d.setEspecificacionMedica(form.getEspecificacionMedica());
            d.setTieneVinculacionFamilia(coalesce(form.getTieneVinculacionFamilia(), 0));
            d.setRazonVinculacion(form.getRazonVinculacion());
            d.setObservaciones(form.getObservaciones());
            d.setActivo(1);

            DeteccionNecesidades saved = deteccionRepository.save(d);
            log.info("Detección pública guardada id={} tutorado={}", saved.getId(), tutorado.getNumeroControl());
            return "redirect:/public/deteccion/confirmacion/" + saved.getId();

        } catch (Exception e) {
            log.error("Error guardando detección pública: {}", e.getMessage(), e);
            attrs.addFlashAttribute("msg_error", "Ocurrió un error. Intenta de nuevo.");
            return "redirect:/public/deteccion/" + idTutorado;
        }
    }

    @GetMapping(value = "deteccion/confirmacion/{idDeteccion}")
    public String vistaConfirmacion(@PathVariable Integer idDeteccion, Model model) {
        deteccionRepository.findById(idDeteccion).ifPresent(d -> {
            model.addAttribute("deteccion", d);
            model.addAttribute("idDeteccion", idDeteccion);
        });
        return "public/viewPublicDeteccionConfirmacion";
    }

    private Integer coalesce(Integer v, Integer def) { return v != null ? v : def; }
}
