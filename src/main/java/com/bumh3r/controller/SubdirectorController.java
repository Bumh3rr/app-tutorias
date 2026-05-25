package com.bumh3r.controller;

import com.bumh3r.entity.*;
import com.bumh3r.repository.ICoordinadorCarreraRepository;
import com.bumh3r.repository.ITutorRepository;
import com.bumh3r.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("subdirector")
public class SubdirectorController {

    @Autowired private SemestreService semestreService;
    @Autowired private GrupoService grupoService;
    @Autowired private GrupoTutoradoService grupoTutoradoService;
    @Autowired private TutorService tutorService;
    @Autowired private CarreraService carreraService;
    @Autowired private ICoordinadorCarreraRepository coordinadorRepository;
    @Autowired private ITutorRepository tutorRepository;
    @Autowired private ConstanciaTutorPdfService constanciaTutorPdfService;
    @Autowired private NombramientoCoordinadorPdfService nombramientoCoordinadorPdfService;

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Semestre vigente(Model model) {
        Semestre s = semestreService.obtenerSemestreVigente();
        if (s == null) {
            model.addAttribute("msg_warning",
                "No hay un semestre activo en el sistema. Contacte al administrador.");
        }
        return s;
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        Semestre s = vigente(model);
        if (s == null) {
            model.addAttribute("totalTutores", 0);
            model.addAttribute("totalTutorados", 0);
            model.addAttribute("totalGrupos", 0);
            model.addAttribute("totalConstancias", 0);
            model.addAttribute("resumenCarreras", List.of());
            return "subdirector/dashboard";
        }

        List<Grupo> grupos = grupoService.buscarPorSemestre(s.getId());
        Map<Integer, Long> alumnosPorGrupo = grupoTutoradoService.contarAlumnosPorGrupo();

        long totalTutores = tutorRepository
                .searchBySemestre(s.getId(), PageRequest.of(0, 1))
                .getTotalElements();

        long totalTutorados = grupos.stream()
                .mapToLong(g -> alumnosPorGrupo.getOrDefault(g.getId(), 0L))
                .sum();

        long totalGrupos = grupos.size();

        // Resumen por carrera
        Map<String, long[]> porCarrera = new LinkedHashMap<>();
        for (Grupo g : grupos) {
            if (g.getCarrera() == null) continue;
            String nombre = g.getCarrera().getNombre();
            porCarrera.computeIfAbsent(nombre, k -> new long[]{0, 0, 0}); // [tutores_ids_placeholder, tutorados, gruposCount]
        }

        // Compute per carrera with unique tutor counts
        Map<String, Set<Integer>> tutoresPorCarrera = new LinkedHashMap<>();
        Map<String, Long> tutoradosPorCarrera = new LinkedHashMap<>();

        for (Grupo g : grupos) {
            if (g.getCarrera() == null) continue;
            String nombre = g.getCarrera().getNombre();
            tutoresPorCarrera.computeIfAbsent(nombre, k -> new HashSet<>());
            if (g.getTutor() != null) {
                tutoresPorCarrera.get(nombre).add(g.getTutor().getId());
            }
            tutoradosPorCarrera.merge(nombre,
                    alumnosPorGrupo.getOrDefault(g.getId(), 0L), Long::sum);
        }

        List<Map<String, Object>> resumenCarreras = new ArrayList<>();
        for (String carreraNombre : tutoresPorCarrera.keySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("carrera", carreraNombre);
            row.put("tutores", tutoresPorCarrera.get(carreraNombre).size());
            row.put("tutorados", tutoradosPorCarrera.getOrDefault(carreraNombre, 0L));
            resumenCarreras.add(row);
        }
        resumenCarreras.sort(Comparator.comparing(r -> (String) r.get("carrera")));

        model.addAttribute("semestre", s);
        model.addAttribute("totalTutores", totalTutores);
        model.addAttribute("totalTutorados", totalTutorados);
        model.addAttribute("totalGrupos", totalGrupos);
        model.addAttribute("totalConstancias", totalTutores);
        model.addAttribute("resumenCarreras", resumenCarreras);
        return "subdirector/dashboard";
    }

    // ── Tutores ──────────────────────────────────────────────────────────────

    @GetMapping("/tutores")
    public String tutores(@RequestParam(required = false) Integer idCarrera, Model model) {
        Semestre s = vigente(model);
        List<Carrera> carreras = carreraService.obtenerTodasCarreras();
        model.addAttribute("carreras", carreras);
        model.addAttribute("idCarreraSeleccionada", idCarrera);

        if (s == null) {
            model.addAttribute("tutorGrupos", List.of());
            return "subdirector/tutores";
        }

        List<Grupo> grupos = (idCarrera != null)
                ? grupoService.buscarPorCarreraYSemestre(idCarrera, s.getId())
                : grupoService.buscarPorSemestre(s.getId());

        // Flatten: one row per grupo with its tutor (skip groups without tutor)
        List<Map<String, Object>> tutorGrupos = grupos.stream()
                .filter(g -> g.getTutor() != null)
                .map(g -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    Tutor t = g.getTutor();
                    row.put("nombre", t.getNombre() + " " + t.getApellido());
                    row.put("numeroControl", t.getNumeroControl() != null ? t.getNumeroControl() : "—");
                    row.put("carrera", g.getCarrera() != null ? g.getCarrera().getNombre() : "—");
                    row.put("grupo", g.getNombre());
                    row.put("activo", Integer.valueOf(1).equals(t.getActivo()));
                    row.put("idTutor", t.getId());
                    return row;
                })
                .sorted(Comparator.comparing(r -> (String) r.get("nombre")))
                .collect(Collectors.toList());

        model.addAttribute("semestre", s);
        model.addAttribute("tutorGrupos", tutorGrupos);
        return "subdirector/tutores";
    }

    // ── Coordinadores ────────────────────────────────────────────────────────

    @GetMapping("/coordinadores")
    public String coordinadores(@RequestParam(required = false) Integer idCarrera, Model model) {
        Semestre s = vigente(model);
        List<Carrera> carreras = carreraService.obtenerTodasCarreras();
        model.addAttribute("carreras", carreras);
        model.addAttribute("idCarreraSeleccionada", idCarrera);

        if (s == null) {
            model.addAttribute("coordinadores", List.of());
            return "subdirector/coordinadores";
        }

        List<CoordinadorCarrera> coordinadores;
        if (idCarrera != null) {
            Carrera carrera = carreraService.obtenerCarrera(idCarrera);
            coordinadores = coordinadorRepository.findByActivoAndCarreraAndSemestre(1, carrera, s);
        } else {
            coordinadores = coordinadorRepository.findByActivoAndSemestre(1, s);
        }

        model.addAttribute("semestre", s);
        model.addAttribute("coordinadores", coordinadores);
        return "subdirector/coordinadores";
    }

    // ── Documentos ───────────────────────────────────────────────────────────

    @GetMapping("/documentos")
    public String documentos(Model model) {
        Semestre s = vigente(model);
        if (s == null) {
            model.addAttribute("tutores", List.of());
            model.addAttribute("coordinadores", List.of());
            return "subdirector/documentos";
        }

        List<Grupo> grupos = grupoService.buscarPorSemestre(s.getId());

        // Unique tutors with an active group in this semester
        List<Tutor> tutores = grupos.stream()
                .filter(g -> g.getTutor() != null)
                .map(Grupo::getTutor)
                .filter(t -> Integer.valueOf(1).equals(t.getActivo()))
                .distinct()
                .sorted(Comparator.comparing(t -> t.getNombre() + t.getApellido()))
                .toList();

        List<CoordinadorCarrera> coordinadores = coordinadorRepository.findByActivoAndSemestre(1, s);

        model.addAttribute("semestre", s);
        model.addAttribute("tutores", tutores);
        model.addAttribute("coordinadores", coordinadores);
        return "subdirector/documentos";
    }

    // ── PDF: Constancia de Tutor ──────────────────────────────────────────────

    @GetMapping("/documentos/constancia-tutor/{idTutor}")
    public ResponseEntity<byte[]> constanciaTutor(@PathVariable Integer idTutor) {
        Semestre s = semestreService.obtenerSemestreVigente();
        if (s == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        String error = constanciaTutorPdfService.validar(idTutor, s.getId());
        if (error != null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            byte[] pdf = constanciaTutorPdfService.generarConstanciaTutor(idTutor, s.getId());
            Tutor t = tutorService.obtenerTutor(idTutor);
            String filename = "constancia_" + t.getNombre().toLowerCase().replace(" ", "_")
                    + "_" + s.getPeriodo().toLowerCase().replace(" ", "_") + "_" + s.getAnio() + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            log.error("Error generando constancia de tutor id={}: {}", idTutor, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ── PDF: Nombramiento de Coordinador ────────────────────────────────────

    @GetMapping("/documentos/nombramiento-coordinador/{idCoordinador}")
    public ResponseEntity<byte[]> nombramientoCoordinador(@PathVariable Integer idCoordinador) {
        String error = nombramientoCoordinadorPdfService.validar(idCoordinador);
        if (error != null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            byte[] pdf = nombramientoCoordinadorPdfService.generarNombramiento(idCoordinador);
            CoordinadorCarrera c = coordinadorRepository.findById(idCoordinador)
                    .orElseThrow();
            String filename = "nombramiento_" + c.getNombre().toLowerCase().replace(" ", "_")
                    + "_" + c.getApellido().toLowerCase().replace(" ", "_") + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            log.error("Error generando nombramiento coordinador id={}: {}", idCoordinador, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
