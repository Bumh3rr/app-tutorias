package com.bumh3r.controller;

import com.bumh3r.dto.ResumenAsistenciaDTO;
import com.bumh3r.entity.*;
import com.bumh3r.repository.IUsuarioRepository;
import com.bumh3r.repository.ISemestreRepository;
import com.bumh3r.service.*;
import com.bumh3r.service.enums.FileType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/tutor")
public class TutorDashboardController {

    @Autowired private IUsuarioRepository usuarioRepository;
    @Autowired private ISemestreRepository semestreRepository;
    @Autowired private GrupoService grupoService;
    @Autowired private SesionService sesionService;
    @Autowired private GrupoTutoradoService grupoTutoradoService;
    @Autowired private AsistenciaService asistenciaService;
    @Autowired private DeteccionNecesidadesService deteccionService;
    @Autowired private EvidenciaSesionService evidenciaSesionService;
    @Autowired private ReporteSesionService reporteSesionService;
    @Autowired private FileStoreService fileStoreService;

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Tutor resolverTutor(Authentication auth) {
        return usuarioRepository.findByUsername(auth.getName())
                .map(Usuario::getTutor)
                .orElseThrow(() -> new AccessDeniedException("No se encontró un tutor asociado a tu cuenta."));
    }

    private Semestre semestreVigente() {
        return semestreRepository.findFirstByActivoOrderByIdDesc(1).orElse(null);
    }

    private void validarGrupoDelTutor(Grupo grupo, Tutor tutor) {
        if (grupo == null || grupo.getTutor() == null
                || !grupo.getTutor().getId().equals(tutor.getId())) {
            throw new AccessDeniedException("No tienes acceso a este grupo.");
        }
    }

    private void validarSesionDelTutor(Sesion sesion, Tutor tutor) {
        if (sesion == null || sesion.getGrupo() == null
                || sesion.getGrupo().getTutor() == null
                || !sesion.getGrupo().getTutor().getId().equals(tutor.getId())) {
            throw new AccessDeniedException("No tienes acceso a esta sesión.");
        }
    }

    /** Verifica que el tutorado esté asignado a algún grupo del tutor. */
    private void validarTutoradoDelTutor(Tutorado tutorado, Tutor tutor) {
        List<GrupoTutorado> grupos = grupoTutoradoService.buscarTutoriasPorTutorado(tutorado.getId());
        boolean perteneceAlTutor = grupos.stream()
                .anyMatch(gt -> gt.getGrupo().getTutor() != null
                        && gt.getGrupo().getTutor().getId().equals(tutor.getId()));
        if (!perteneceAlTutor) {
            throw new AccessDeniedException("Este tutorado no pertenece a tus grupos.");
        }
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Authentication auth, Model model) {
        Tutor tutor = resolverTutor(auth);
        Semestre vigente = semestreVigente();

        List<Grupo> gruposVigentes = vigente != null
                ? grupoService.buscarPorTutorYSemestre(tutor.getId(), vigente.getId())
                : grupoService.buscarPorTutor(tutor.getId());

        Map<Integer, Long> conteoAlumnos = grupoTutoradoService.contarAlumnosPorGrupo();
        long totalTutorados = gruposVigentes.stream()
                .mapToLong(g -> conteoAlumnos.getOrDefault(g.getId(), 0L))
                .sum();

        List<Sesion> sesionesVigentes = gruposVigentes.stream()
                .flatMap(g -> sesionService.buscarSesionesPorGrupo(g.getId()).stream())
                .toList();

        long totalSesiones   = sesionesVigentes.size();
        long sesionesRealizadas = sesionesVigentes.stream()
                .filter(s -> "REALIZADA".equals(s.getEstatusRegistro()))
                .count();

        // Próxima sesión por grupo (primera PENDIENTE/PROGRAMADA ordenada por semana)
        Map<Integer, Sesion> proximaSesionPorGrupo = new HashMap<>();
        for (Grupo g : gruposVigentes) {
            sesionService.buscarSesionesPorGrupo(g.getId()).stream()
                    .filter(s -> !"REALIZADA".equals(s.getEstatusRegistro()))
                    .min(Comparator.comparingInt(Sesion::getSemana))
                    .ifPresent(s -> proximaSesionPorGrupo.put(g.getId(), s));
        }

        model.addAttribute("tutor", tutor);
        model.addAttribute("semestreVigente", vigente);
        model.addAttribute("grupos", gruposVigentes);
        model.addAttribute("conteoAlumnos", conteoAlumnos);
        model.addAttribute("totalTutorados", totalTutorados);
        model.addAttribute("totalSesiones", totalSesiones);
        model.addAttribute("sesionesRealizadas", sesionesRealizadas);
        model.addAttribute("proximaSesionPorGrupo", proximaSesionPorGrupo);
        return "tutor/dashboard";
    }

    // ── Mis Grupos ────────────────────────────────────────────────────────────

    @GetMapping("/grupos")
    public String misGrupos(Authentication auth, Model model) {
        Tutor tutor = resolverTutor(auth);
        Semestre vigente = semestreVigente();

        List<Grupo> grupos = vigente != null
                ? grupoService.buscarPorTutorYSemestre(tutor.getId(), vigente.getId())
                : grupoService.buscarPorTutor(tutor.getId());

        Map<Integer, Long> conteoAlumnos = grupoTutoradoService.contarAlumnosPorGrupo();

        model.addAttribute("tutor", tutor);
        model.addAttribute("grupos", grupos);
        model.addAttribute("semestreVigente", vigente);
        model.addAttribute("conteoAlumnos", conteoAlumnos);
        return "tutor/grupos";
    }

    @GetMapping("/grupos/{idGrupo}")
    public String grupoDetalle(@PathVariable Integer idGrupo, Authentication auth, Model model) {
        Tutor tutor = resolverTutor(auth);
        Grupo grupo = grupoService.obtenerGrupo(idGrupo);
        validarGrupoDelTutor(grupo, tutor);

        List<GrupoTutorado> tutorados = grupoTutoradoService.buscarPorGrupo(idGrupo);
        List<Sesion> sesiones = sesionService.buscarSesionesPorGrupo(idGrupo)
                .stream()
                .sorted(Comparator.comparingInt(Sesion::getSemana))
                .toList();

        model.addAttribute("tutor", tutor);
        model.addAttribute("grupo", grupo);
        model.addAttribute("tutorados", tutorados);
        model.addAttribute("sesiones", sesiones);
        return "tutor/grupo-detalle";
    }

    // ── Sesiones ──────────────────────────────────────────────────────────────

    @GetMapping("/sesiones")
    public String misSesiones(Authentication auth, Model model) {
        Tutor tutor = resolverTutor(auth);
        Semestre vigente = semestreVigente();

        List<Grupo> grupos = vigente != null
                ? grupoService.buscarPorTutorYSemestre(tutor.getId(), vigente.getId())
                : grupoService.buscarPorTutor(tutor.getId());

        List<Sesion> sesiones = grupos.stream()
                .flatMap(g -> sesionService.buscarSesionesPorGrupo(g.getId()).stream())
                .sorted(Comparator
                        .comparing((Sesion s) -> s.getGrupo().getNombre())
                        .thenComparingInt(Sesion::getSemana))
                .toList();

        model.addAttribute("tutor", tutor);
        model.addAttribute("sesiones", sesiones);
        model.addAttribute("semestreVigente", vigente);
        return "tutor/sesiones";
    }

    @GetMapping("/sesiones/{idSesion}")
    public String sesionDetalle(@PathVariable Integer idSesion, Authentication auth, Model model) {
        Tutor tutor = resolverTutor(auth);
        Sesion sesion = sesionService.obtenerSesion(idSesion);
        validarSesionDelTutor(sesion, tutor);

        List<GrupoTutorado> tutorados = grupoTutoradoService.buscarPorGrupo(sesion.getGrupo().getId());
        List<Asistencia> asistencias  = asistenciaService.buscarAsistenciasPorSesion(idSesion);
        List<EvidenciaSesion> evidencias = evidenciaSesionService.buscarEvidenciasPorSesion(idSesion);
        ReporteSesion reporte = reporteSesionService.obtenerReportePorSesion(idSesion);

        // Mapa tutoradoId → asistencia para saber si ya se registró
        Map<Integer, Asistencia> mapaAsistencias = asistencias.stream()
                .collect(Collectors.toMap(a -> a.getTutorado().getId(), a -> a));

        boolean asistenciaRegistrada = !asistencias.isEmpty();
        boolean evidenciaSubida      = !evidencias.isEmpty();
        boolean reporteRegistrado    = reporte != null;

        model.addAttribute("tutor", tutor);
        model.addAttribute("sesion", sesion);
        model.addAttribute("tutorados", tutorados);
        model.addAttribute("mapaAsistencias", mapaAsistencias);
        model.addAttribute("evidencias", evidencias);
        model.addAttribute("reporte", reporte);
        model.addAttribute("asistenciaRegistrada", asistenciaRegistrada);
        model.addAttribute("evidenciaSubida", evidenciaSubida);
        model.addAttribute("reporteRegistrado", reporteRegistrado);
        return "tutor/sesion-detalle";
    }

    // ── Detección de Necesidades ──────────────────────────────────────────────

    @GetMapping("/deteccion")
    public String deteccion(Authentication auth, Model model) {
        Tutor tutor = resolverTutor(auth);
        Semestre vigente = semestreVigente();

        List<Grupo> grupos = vigente != null
                ? grupoService.buscarPorTutorYSemestre(tutor.getId(), vigente.getId())
                : grupoService.buscarPorTutor(tutor.getId());

        // Tutorados únicos de todos los grupos, sin duplicados
        List<GrupoTutorado> todasAsignaciones = grupos.stream()
                .flatMap(g -> grupoTutoradoService.buscarPorGrupo(g.getId()).stream())
                .toList();

        // Para cada tutorado, verificar si ya tiene detección aplicada
        Map<Integer, Boolean> deteccionAplicada = new LinkedHashMap<>();
        Map<Integer, GrupoTutorado> asignacionPorTutorado = new LinkedHashMap<>();
        for (GrupoTutorado gt : todasAsignaciones) {
            Integer idTutorado = gt.getTutorado().getId();
            if (!deteccionAplicada.containsKey(idTutorado)) {
                boolean aplicada = !deteccionService.buscarPorTutorado(idTutorado).isEmpty();
                deteccionAplicada.put(idTutorado, aplicada);
                asignacionPorTutorado.put(idTutorado, gt);
            }
        }

        long deteccionAplicadasCount = deteccionAplicada.values().stream().filter(Boolean::booleanValue).count();
        long deteccionPendientesCount = deteccionAplicada.values().stream().filter(v -> !v).count();

        model.addAttribute("tutor", tutor);
        model.addAttribute("asignaciones", new java.util.ArrayList<>(asignacionPorTutorado.values()));
        model.addAttribute("deteccionAplicada", deteccionAplicada);
        model.addAttribute("deteccionAplicadasCount", deteccionAplicadasCount);
        model.addAttribute("deteccionPendientesCount", deteccionPendientesCount);
        model.addAttribute("semestreVigente", vigente);
        return "tutor/deteccion";
    }

    @GetMapping("/deteccion/{idTutorado}")
    public String deteccionForm(@PathVariable Integer idTutorado, Authentication auth, Model model) {
        Tutor tutor = resolverTutor(auth);

        List<GrupoTutorado> asignaciones = grupoTutoradoService.buscarTutoriasPorTutorado(idTutorado);
        boolean perteneceAlTutor = asignaciones.stream()
                .anyMatch(gt -> gt.getGrupo().getTutor() != null
                        && gt.getGrupo().getTutor().getId().equals(tutor.getId()));
        if (!perteneceAlTutor) throw new AccessDeniedException("Este tutorado no pertenece a tus grupos.");

        // Grupos del tutor donde está este tutorado → sesiones disponibles
        List<Sesion> sesionesDisponibles = asignaciones.stream()
                .filter(gt -> gt.getGrupo().getTutor() != null
                        && gt.getGrupo().getTutor().getId().equals(tutor.getId()))
                .flatMap(gt -> sesionService.buscarSesionesPorGrupo(gt.getGrupo().getId()).stream())
                .sorted(Comparator.comparingInt(Sesion::getSemana))
                .toList();

        Tutorado tutorado = asignaciones.get(0).getTutorado();
        DeteccionNecesidades deteccion = new DeteccionNecesidades();
        deteccion.setTutorado(tutorado);

        model.addAttribute("tutor", tutor);
        model.addAttribute("tutorado", tutorado);
        model.addAttribute("deteccion", deteccion);
        model.addAttribute("sesiones", sesionesDisponibles);
        return "tutor/deteccion-form";
    }

    @PostMapping("/deteccion/{idTutorado}")
    public String deteccionGuardar(
            @PathVariable Integer idTutorado,
            @RequestParam(value = "idSesion", required = false) Integer idSesion,
            @ModelAttribute DeteccionNecesidades deteccion,
            Authentication auth,
            RedirectAttributes attrs) {

        Tutor tutor = resolverTutor(auth);

        List<GrupoTutorado> asignaciones = grupoTutoradoService.buscarTutoriasPorTutorado(idTutorado);
        boolean perteneceAlTutor = asignaciones.stream()
                .anyMatch(gt -> gt.getGrupo().getTutor() != null
                        && gt.getGrupo().getTutor().getId().equals(tutor.getId()));
        if (!perteneceAlTutor) throw new AccessDeniedException("Este tutorado no pertenece a tus grupos.");

        try {
            Tutorado tutorado = asignaciones.get(0).getTutorado();
            deteccion.setTutorado(tutorado);
            if (idSesion != null) {
                Sesion sesion = sesionService.obtenerSesion(idSesion);
                validarSesionDelTutor(sesion, tutor);
                deteccion.setSesion(sesion);
            } else {
                deteccion.setSesion(null);
            }
            deteccion.setActivo(1);
            deteccionService.guardarDeteccion(deteccion);
            attrs.addFlashAttribute("msg_success", "Detección registrada correctamente.");
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al guardar detección: {}", e.getMessage());
            attrs.addFlashAttribute("msg_error", "Error al guardar la detección: " + e.getMessage());
            return "redirect:/tutor/deteccion/" + idTutorado;
        }
        return "redirect:/tutor/deteccion";
    }

    // ── Asistencias ───────────────────────────────────────────────────────────

    @GetMapping("/asistencias")
    public String asistencias(Authentication auth, Model model) {
        Tutor tutor = resolverTutor(auth);
        Semestre vigente = semestreVigente();

        List<Grupo> grupos = vigente != null
                ? grupoService.buscarPorTutorYSemestre(tutor.getId(), vigente.getId())
                : grupoService.buscarPorTutor(tutor.getId());

        // Tutorados únicos con su grupo de referencia
        List<GrupoTutorado> todasAsignaciones = grupos.stream()
                .flatMap(g -> grupoTutoradoService.buscarPorGrupo(g.getId()).stream())
                .toList();

        // Calcular resumen por tutorado (deduplicado)
        List<ResumenAsistenciaDTO> resumenes = new ArrayList<>();
        Set<Integer> vistos = new HashSet<>();
        for (GrupoTutorado gt : todasAsignaciones) {
            Integer idTutorado = gt.getTutorado().getId();
            if (vistos.add(idTutorado)) {
                try {
                    resumenes.add(asistenciaService.calcularResumenAsistencia(idTutorado));
                } catch (Exception e) {
                    log.warn("No se pudo calcular asistencia para tutorado {}: {}", idTutorado, e.getMessage());
                }
            }
        }

        // Mapa tutoradoId → grupo para mostrar en tabla
        Map<Integer, Grupo> grupoPorTutorado = new LinkedHashMap<>();
        for (GrupoTutorado gt : todasAsignaciones) {
            grupoPorTutorado.putIfAbsent(gt.getTutorado().getId(), gt.getGrupo());
        }

        model.addAttribute("tutor", tutor);
        model.addAttribute("resumenes", resumenes);
        model.addAttribute("grupoPorTutorado", grupoPorTutorado);
        model.addAttribute("semestreVigente", vigente);
        return "tutor/asistencias";
    }

    // ── Registro masivo de asistencia ─────────────────────────────────────────

    @GetMapping("/sesiones/{idSesion}/asistencia")
    public String asistenciaForm(@PathVariable Integer idSesion, Authentication auth, Model model) {
        Tutor tutor = resolverTutor(auth);
        Sesion sesion = sesionService.obtenerSesion(idSesion);
        validarSesionDelTutor(sesion, tutor);

        List<GrupoTutorado> tutorados = grupoTutoradoService.buscarPorGrupo(sesion.getGrupo().getId());
        List<Asistencia> asistencias = asistenciaService.buscarAsistenciasPorSesion(idSesion);
        Map<Integer, Integer> mapaPresente = asistencias.stream()
                .collect(Collectors.toMap(a -> a.getTutorado().getId(), Asistencia::getPresente));

        model.addAttribute("sesion", sesion);
        model.addAttribute("tutorados", tutorados);
        model.addAttribute("mapaPresente", mapaPresente);
        return "tutor/asistencia-form";
    }

    @PostMapping("/sesiones/{idSesion}/asistencia")
    public String asistenciaGuardar(
            @PathVariable Integer idSesion,
            @RequestParam(value = "idsTutoradosPresentes", required = false) Integer[] idsTutoradosPresentes,
            Authentication auth,
            RedirectAttributes attrs) {

        Tutor tutor = resolverTutor(auth);
        Sesion sesion = sesionService.obtenerSesion(idSesion);
        validarSesionDelTutor(sesion, tutor);

        try {
            asistenciaService.registrarAsistenciaMasiva(idSesion,
                    idsTutoradosPresentes != null ? idsTutoradosPresentes : new Integer[0]);
            attrs.addFlashAttribute("msg_success", "Asistencia registrada correctamente.");
        } catch (Exception e) {
            log.error("Error al registrar asistencia: {}", e.getMessage());
            attrs.addFlashAttribute("msg_error", "Error al registrar asistencia: " + e.getMessage());
        }
        return "redirect:/tutor/sesiones/" + idSesion;
    }

    // ── Evidencia ──────────────────────────────────────────────────────────────

    @GetMapping("/sesiones/{idSesion}/evidencia")
    public String evidenciaForm(@PathVariable Integer idSesion, Authentication auth, Model model) {
        Tutor tutor = resolverTutor(auth);
        Sesion sesion = sesionService.obtenerSesion(idSesion);
        validarSesionDelTutor(sesion, tutor);

        List<EvidenciaSesion> evidencias = evidenciaSesionService.buscarEvidenciasPorSesion(idSesion);

        model.addAttribute("sesion", sesion);
        model.addAttribute("evidencias", evidencias);
        return "tutor/evidencia-form";
    }

    @PostMapping("/sesiones/{idSesion}/evidencia")
    public String evidenciaGuardar(
            @PathVariable Integer idSesion,
            @RequestParam(value = "archivoEvidencia", required = false) MultipartFile archivoEvidencia,
            Authentication auth,
            RedirectAttributes attrs) {

        Tutor tutor = resolverTutor(auth);
        Sesion sesion = sesionService.obtenerSesion(idSesion);
        validarSesionDelTutor(sesion, tutor);

        if (archivoEvidencia == null || archivoEvidencia.isEmpty()) {
            attrs.addFlashAttribute("msg_error", "Debes seleccionar un archivo para subir.");
            return "redirect:/tutor/sesiones/" + idSesion + "/evidencia";
        }

        try {
            String url = fileStoreService.save(archivoEvidencia, FileType.EVIDENCIA);
            EvidenciaSesion evidencia = new EvidenciaSesion();
            evidencia.setSesion(sesion);
            evidencia.setArchivoUrl(url);
            evidencia.setFechaSubida(new java.util.Date());
            evidencia.setEstatusValidacion("PENDIENTE");
            evidencia.setActivo(1);
            evidenciaSesionService.guardarEvidencia(evidencia);
            attrs.addFlashAttribute("msg_success", "Evidencia subida correctamente.");
        } catch (Exception e) {
            log.error("Error al subir evidencia: {}", e.getMessage());
            attrs.addFlashAttribute("msg_error", "Error al subir la evidencia: " + e.getMessage());
            return "redirect:/tutor/sesiones/" + idSesion + "/evidencia";
        }
        return "redirect:/tutor/sesiones/" + idSesion;
    }

    // ── Reporte de sesión ──────────────────────────────────────────────────────

    @GetMapping("/sesiones/{idSesion}/reporte")
    public String reporteForm(@PathVariable Integer idSesion, Authentication auth, Model model) {
        Tutor tutor = resolverTutor(auth);
        Sesion sesion = sesionService.obtenerSesion(idSesion);
        validarSesionDelTutor(sesion, tutor);

        ReporteSesion reporte = reporteSesionService.obtenerReportePorSesion(idSesion);
        List<Asistencia> asistencias = asistenciaService.buscarAsistenciasPorSesion(idSesion);
        long presentes = asistencias.stream()
                .filter(a -> a.getPresente() != null && a.getPresente() == 1)
                .count();

        model.addAttribute("sesion", sesion);
        model.addAttribute("reporte", reporte);
        model.addAttribute("alumnosPresentesCount", presentes);
        return "tutor/reporte-form";
    }

    @PostMapping("/sesiones/{idSesion}/reporte")
    public String reporteGuardar(
            @PathVariable Integer idSesion,
            @RequestParam("descripcionActividad") String descripcionActividad,
            @RequestParam(value = "observaciones", required = false) String observaciones,
            @RequestParam(value = "alumnosPresentes", required = false) Integer alumnosPresentes,
            @RequestParam(value = "fechaEntrega", required = false) String fechaEntregaStr,
            Authentication auth,
            RedirectAttributes attrs) {

        Tutor tutor = resolverTutor(auth);
        Sesion sesion = sesionService.obtenerSesion(idSesion);
        validarSesionDelTutor(sesion, tutor);

        try {
            ReporteSesion existente = reporteSesionService.obtenerReportePorSesion(idSesion);
            ReporteSesion reporte = existente != null ? existente : new ReporteSesion();
            reporte.setSesion(sesion);
            reporte.setDescripcionActividad(descripcionActividad);
            reporte.setObservaciones(observaciones);
            reporte.setAlumnosPresentes(alumnosPresentes);
            if (fechaEntregaStr != null && !fechaEntregaStr.isBlank()) {
                try { reporte.setFechaEntrega(new SimpleDateFormat("yyyy-MM-dd").parse(fechaEntregaStr)); }
                catch (Exception ignored) {}
            }
            if (reporte.getEstatusRevision() == null) reporte.setEstatusRevision("PENDIENTE");
            if (reporte.getActivo() == null) reporte.setActivo(1);
            reporteSesionService.guardarReporte(reporte);
            attrs.addFlashAttribute("msg_success", "Reporte guardado correctamente.");
        } catch (Exception e) {
            log.error("Error al guardar reporte: {}", e.getMessage());
            attrs.addFlashAttribute("msg_error", "Error al guardar el reporte: " + e.getMessage());
            return "redirect:/tutor/sesiones/" + idSesion + "/reporte";
        }
        return "redirect:/tutor/sesiones/" + idSesion;
    }

    // ── Recuperación de asistencia ─────────────────────────────────────────────

    @GetMapping("/sesiones/{idSesion}/recuperacion")
    public String recuperacionForm(@PathVariable Integer idSesion, Authentication auth, Model model) {
        Tutor tutor = resolverTutor(auth);
        Sesion sesion = sesionService.obtenerSesion(idSesion);
        validarSesionDelTutor(sesion, tutor);

        List<Asistencia> asistencias = asistenciaService.buscarAsistenciasPorSesion(idSesion);
        List<Asistencia> ausentes = asistencias.stream()
                .filter(a -> a.getPresente() != null && a.getPresente() == 0
                        && (a.getRecuperada() == null || a.getRecuperada() != 1))
                .toList();

        model.addAttribute("sesion", sesion);
        model.addAttribute("ausentes", ausentes);
        return "tutor/recuperacion-form";
    }

    @PostMapping("/sesiones/{idSesion}/recuperacion")
    public String recuperacionGuardar(
            @PathVariable Integer idSesion,
            @RequestParam(value = "idsRecuperados", required = false) Integer[] idsRecuperados,
            Authentication auth,
            RedirectAttributes attrs) {

        Tutor tutor = resolverTutor(auth);
        Sesion sesion = sesionService.obtenerSesion(idSesion);
        validarSesionDelTutor(sesion, tutor);

        if (idsRecuperados == null || idsRecuperados.length == 0) {
            attrs.addFlashAttribute("msg_error", "No se seleccionó ningún tutorado para recuperar.");
            return "redirect:/tutor/sesiones/" + idSesion + "/recuperacion";
        }

        try {
            List<Asistencia> asistencias = asistenciaService.buscarAsistenciasPorSesion(idSesion);
            Map<Integer, Asistencia> mapaAsistencias = asistencias.stream()
                    .collect(Collectors.toMap(a -> a.getTutorado().getId(), a -> a));

            for (Integer idTutorado : idsRecuperados) {
                Asistencia a = mapaAsistencias.get(idTutorado);
                if (a != null) {
                    a.setRecuperada(1);
                    asistenciaService.actualizarAsistencia(a.getId(), a);
                }
            }
            attrs.addFlashAttribute("msg_success", "Recuperaciones registradas correctamente.");
        } catch (Exception e) {
            log.error("Error al registrar recuperaciones: {}", e.getMessage());
            attrs.addFlashAttribute("msg_error", "Error al registrar recuperaciones: " + e.getMessage());
        }
        return "redirect:/tutor/sesiones/" + idSesion;
    }
}
