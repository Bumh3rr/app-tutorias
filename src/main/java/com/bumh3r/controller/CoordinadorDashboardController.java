package com.bumh3r.controller;

import com.bumh3r.dto.ResumenAsistenciaDTO;
import com.bumh3r.entity.*;
import com.bumh3r.repository.ISemestreRepository;
import com.bumh3r.repository.IUsuarioRepository;
import com.bumh3r.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/coordinador")
public class CoordinadorDashboardController {

    @Autowired private IUsuarioRepository usuarioRepository;
    @Autowired private ISemestreRepository semestreRepository;
    @Autowired private TutorService tutorService;
    @Autowired private TutoradoService tutoradoService;
    @Autowired private GrupoService grupoService;
    @Autowired private GrupoTutoradoService grupoTutoradoService;
    @Autowired private SesionService sesionService;
    @Autowired private AsistenciaService asistenciaService;
    @Autowired private EvidenciaSesionService evidenciaSesionService;
    @Autowired private PATService patService;
    @Autowired private ActividadService actividadService;

    private CoordinadorCarrera resolverCoordinador(Authentication auth) {
        return usuarioRepository.findByUsername(auth.getName())
                .map(Usuario::getCoordinador)
                .filter(Objects::nonNull)
                .orElseThrow(() -> new AccessDeniedException("Sin perfil de coordinador"));
    }

    private Semestre semestreVigente() {
        return semestreRepository.findFirstByActivoOrderByIdDesc(1).orElse(null);
    }

    private List<Grupo> gruposDeCarrera(CoordinadorCarrera coord, Semestre vigente) {
        if (coord.getCarrera() == null || vigente == null) return List.of();
        return grupoService.buscarPorCarreraYSemestre(coord.getCarrera().getId(), vigente.getId());
    }

    private boolean evidenciaPerteneceACarrera(EvidenciaSesion evidencia, CoordinadorCarrera coord) {
        if (coord.getCarrera() == null) return false;
        return evidencia.getSesion() != null
                && evidencia.getSesion().getGrupo() != null
                && evidencia.getSesion().getGrupo().getCarrera() != null
                && coord.getCarrera().getId().equals(evidencia.getSesion().getGrupo().getCarrera().getId());
    }

    // ─── Dashboard ──────────────────────────────────────────────────────────────

    @GetMapping({"", "/"})
    public String dashboard(Authentication auth, Model model) {
        CoordinadorCarrera coord = resolverCoordinador(auth);
        Semestre vigente = semestreVigente();
        model.addAttribute("coordinador", coord);
        model.addAttribute("semestreVigente", vigente);

        List<Grupo> grupos = gruposDeCarrera(coord, vigente);

        List<Tutor> tutores = grupos.stream()
                .filter(g -> g.getTutor() != null && Integer.valueOf(1).equals(g.getTutor().getActivo()))
                .map(Grupo::getTutor)
                .distinct()
                .collect(Collectors.toList());

        long totalTutorados = grupos.stream()
                .flatMap(g -> grupoTutoradoService.buscarPorGrupo(g.getId()).stream())
                .filter(gt -> gt.getTutorado() != null)
                .count();

        List<EvidenciaSesion> evidenciasPendientes = grupos.stream()
                .flatMap(g -> sesionService.buscarSesionesPorGrupo(g.getId()).stream())
                .flatMap(s -> evidenciaSesionService.buscarEvidenciasPorSesion(s.getId()).stream())
                .filter(e -> "PENDIENTE".equals(e.getEstatusValidacion()))
                .collect(Collectors.toList());

        model.addAttribute("totalGrupos", grupos.size());
        model.addAttribute("totalTutores", tutores.size());
        model.addAttribute("totalTutorados", totalTutorados);
        model.addAttribute("totalEvidenciasPendientes", evidenciasPendientes.size());
        return "coordinador/dashboard";
    }

    // ─── Tutores ────────────────────────────────────────────────────────────────

    @GetMapping("/tutores")
    public String tutores(Authentication auth, Model model) {
        CoordinadorCarrera coord = resolverCoordinador(auth);
        Semestre vigente = semestreVigente();
        List<Grupo> grupos = gruposDeCarrera(coord, vigente);

        List<Tutor> tutores = grupos.stream()
                .filter(g -> g.getTutor() != null && Integer.valueOf(1).equals(g.getTutor().getActivo()))
                .map(Grupo::getTutor)
                .distinct()
                .collect(Collectors.toList());

        // Map tutor id → groups
        Map<Integer, List<Grupo>> gruposPorTutor = grupos.stream()
                .filter(g -> g.getTutor() != null)
                .collect(Collectors.groupingBy(g -> g.getTutor().getId()));

        model.addAttribute("coordinador", coord);
        model.addAttribute("tutores", tutores);
        model.addAttribute("gruposPorTutor", gruposPorTutor);
        model.addAttribute("semestreVigente", vigente);
        return "coordinador/tutores";
    }

    @GetMapping("/tutores/{idTutor}")
    public String tutorDetalle(@PathVariable Integer idTutor, Authentication auth, Model model) {
        CoordinadorCarrera coord = resolverCoordinador(auth);
        Semestre vigente = semestreVigente();
        List<Grupo> grupos = gruposDeCarrera(coord, vigente);

        boolean tutorValido = grupos.stream()
                .anyMatch(g -> g.getTutor() != null && g.getTutor().getId().equals(idTutor));
        if (!tutorValido) return "redirect:/coordinador/tutores";

        Tutor tutor = tutorService.obtenerTutor(idTutor);
        List<Grupo> gruposTutor = grupos.stream()
                .filter(g -> g.getTutor() != null && g.getTutor().getId().equals(idTutor))
                .collect(Collectors.toList());

        // Evidencias del tutor (sus sesiones)
        List<EvidenciaSesion> evidencias = gruposTutor.stream()
                .flatMap(g -> sesionService.buscarSesionesPorGrupo(g.getId()).stream())
                .flatMap(s -> evidenciaSesionService.buscarEvidenciasPorSesion(s.getId()).stream())
                .sorted(Comparator.comparing(EvidenciaSesion::getFechaSubida, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        model.addAttribute("coordinador", coord);
        model.addAttribute("tutor", tutor);
        model.addAttribute("grupos", gruposTutor);
        model.addAttribute("evidencias", evidencias);
        model.addAttribute("semestreVigente", vigente);
        return "coordinador/tutor-detalle";
    }

    // ─── Tutorados ──────────────────────────────────────────────────────────────

    @GetMapping("/tutorados")
    public String tutorados(Authentication auth, Model model) {
        CoordinadorCarrera coord = resolverCoordinador(auth);
        Semestre vigente = semestreVigente();
        List<Grupo> grupos = gruposDeCarrera(coord, vigente);

        List<GrupoTutorado> grupoTutorados = grupos.stream()
                .flatMap(g -> grupoTutoradoService.buscarPorGrupo(g.getId()).stream())
                .filter(gt -> gt.getTutorado() != null)
                .collect(Collectors.toList());

        Map<Integer, ResumenAsistenciaDTO> resumenMap = new HashMap<>();
        for (GrupoTutorado gt : grupoTutorados) {
            try {
                resumenMap.put(gt.getTutorado().getId(),
                        asistenciaService.calcularResumenAsistencia(gt.getTutorado().getId()));
            } catch (Exception e) {
                log.warn("No se pudo calcular resumen para tutorado {}", gt.getTutorado().getId());
            }
        }

        model.addAttribute("coordinador", coord);
        model.addAttribute("grupoTutorados", grupoTutorados);
        model.addAttribute("resumenMap", resumenMap);
        model.addAttribute("semestreVigente", vigente);
        return "coordinador/tutorados";
    }

    @GetMapping("/tutorados/{idTutorado}")
    public String tutoradoDetalle(@PathVariable Integer idTutorado, Authentication auth, Model model) {
        CoordinadorCarrera coord = resolverCoordinador(auth);
        Semestre vigente = semestreVigente();
        List<Grupo> grupos = gruposDeCarrera(coord, vigente);

        Set<Integer> grupoIds = grupos.stream().map(Grupo::getId).collect(Collectors.toSet());
        List<GrupoTutorado> tutoriasDelTutorado = grupoTutoradoService.buscarTutoriasPorTutorado(idTutorado);
        boolean valido = tutoriasDelTutorado.stream()
                .anyMatch(gt -> gt.getGrupo() != null && grupoIds.contains(gt.getGrupo().getId()));
        if (!valido) return "redirect:/coordinador/tutorados";

        Tutorado tutorado = tutoradoService.obtenerTutorado(idTutorado);
        ResumenAsistenciaDTO resumen = null;
        try {
            resumen = asistenciaService.calcularResumenAsistencia(idTutorado);
        } catch (Exception e) {
            log.warn("No se pudo calcular resumen para tutorado {}", idTutorado);
        }

        model.addAttribute("coordinador", coord);
        model.addAttribute("tutorado", tutorado);
        model.addAttribute("resumen", resumen);
        model.addAttribute("grupoTutorados", tutoriasDelTutorado);
        model.addAttribute("semestreVigente", vigente);
        return "coordinador/tutorado-detalle";
    }

    // ─── Evidencias ─────────────────────────────────────────────────────────────

    @GetMapping("/evidencias")
    public String evidencias(@RequestParam(required = false) String estatus,
                             Authentication auth, Model model) {
        CoordinadorCarrera coord = resolverCoordinador(auth);
        Semestre vigente = semestreVigente();
        List<Grupo> grupos = gruposDeCarrera(coord, vigente);

        List<EvidenciaSesion> evidencias = grupos.stream()
                .flatMap(g -> sesionService.buscarSesionesPorGrupo(g.getId()).stream())
                .flatMap(s -> evidenciaSesionService.buscarEvidenciasPorSesion(s.getId()).stream())
                .filter(e -> estatus == null || estatus.isBlank() || estatus.equals(e.getEstatusValidacion()))
                .sorted(Comparator.comparing(EvidenciaSesion::getFechaSubida,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        model.addAttribute("coordinador", coord);
        model.addAttribute("evidencias", evidencias);
        model.addAttribute("estatusFiltro", estatus);
        model.addAttribute("semestreVigente", vigente);
        return "coordinador/evidencias";
    }

    @GetMapping("/evidencias/{idEvidencia}")
    public String evidenciaDetalle(@PathVariable Integer idEvidencia,
                                   Authentication auth, Model model) {
        CoordinadorCarrera coord = resolverCoordinador(auth);
        EvidenciaSesion evidencia = evidenciaSesionService.obtenerEvidencia(idEvidencia);
        if (evidencia == null || !evidenciaPerteneceACarrera(evidencia, coord)) {
            return "redirect:/coordinador/evidencias";
        }
        model.addAttribute("coordinador", coord);
        model.addAttribute("evidencia", evidencia);
        model.addAttribute("validated", false);
        model.addAttribute("rejected", false);
        return "coordinador/evidencia-detalle";
    }

    @PostMapping("/evidencias/{idEvidencia}/validar")
    public String validarEvidencia(@PathVariable Integer idEvidencia,
                                   @RequestParam(required = false) String notas,
                                   Authentication auth) {
        CoordinadorCarrera coord = resolverCoordinador(auth);
        EvidenciaSesion evidencia = evidenciaSesionService.obtenerEvidencia(idEvidencia);
        if (evidencia == null || !evidenciaPerteneceACarrera(evidencia, coord)) {
            return "redirect:/coordinador/evidencias";
        }
        evidenciaSesionService.validarEvidencia(idEvidencia, notas);
        return "redirect:/coordinador/evidencias/" + idEvidencia;
    }

    @PostMapping("/evidencias/{idEvidencia}/rechazar")
    public String rechazarEvidencia(@PathVariable Integer idEvidencia,
                                    @RequestParam(required = false) String notas,
                                    Authentication auth) {
        CoordinadorCarrera coord = resolverCoordinador(auth);
        EvidenciaSesion evidencia = evidenciaSesionService.obtenerEvidencia(idEvidencia);
        if (evidencia == null || !evidenciaPerteneceACarrera(evidencia, coord)) {
            return "redirect:/coordinador/evidencias";
        }
        evidenciaSesionService.rechazarEvidencia(idEvidencia, notas);
        return "redirect:/coordinador/evidencias/" + idEvidencia;
    }

    // ─── PAT ────────────────────────────────────────────────────────────────────

    @GetMapping("/pat")
    public String pat(Authentication auth, Model model) {
        CoordinadorCarrera coord = resolverCoordinador(auth);
        Semestre vigente = semestreVigente();

        List<PAT> pats = new ArrayList<>();
        if (coord.getCarrera() != null && vigente != null) {
            pats.addAll(patService.buscarPATporCarreraYSemestre(coord.getCarrera().getId(), vigente.getId()));
        }
        pats.addAll(patService.obtenerPATGenerales());

        model.addAttribute("coordinador", coord);
        model.addAttribute("pats", pats);
        model.addAttribute("semestreVigente", vigente);
        return "coordinador/pat";
    }

    @GetMapping("/pat/{idPat}/actividades")
    public String patActividades(@PathVariable Integer idPat, Authentication auth, Model model) {
        CoordinadorCarrera coord = resolverCoordinador(auth);
        Semestre vigente = semestreVigente();

        PAT pat = patService.obtenerPAT(idPat);
        if (pat == null) return "redirect:/coordinador/pat";

        boolean esGeneral = Integer.valueOf(1).equals(pat.getEsGeneral());
        boolean esDeCarrera = coord.getCarrera() != null
                && pat.getCarrera() != null
                && coord.getCarrera().getId().equals(pat.getCarrera().getId());
        if (!esGeneral && !esDeCarrera) return "redirect:/coordinador/pat";

        List<Actividad> actividades = actividadService.buscarActividadesPorPAT(idPat);

        model.addAttribute("coordinador", coord);
        model.addAttribute("pat", pat);
        model.addAttribute("actividades", actividades);
        model.addAttribute("semestreVigente", vigente);
        return "coordinador/pat-actividades";
    }
}
