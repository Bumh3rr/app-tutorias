package com.bumh3r.controller;

import com.bumh3r.dto.ResumenAsistenciaDTO;
import com.bumh3r.entity.*;
import com.bumh3r.repository.ISemestreRepository;
import com.bumh3r.repository.IUsuarioRepository;
import com.bumh3r.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/tutorado")
public class TutoradoDashboardController {

    @Autowired private IUsuarioRepository usuarioRepository;
    @Autowired private ISemestreRepository semestreRepository;
    @Autowired private GrupoTutoradoService grupoTutoradoService;
    @Autowired private SesionService sesionService;
    @Autowired private AsistenciaService asistenciaService;
    @Autowired private DeteccionNecesidadesService deteccionNecesidadesService;
    @Autowired private CarnetPdfService carnetPdfService;
    @Autowired private ConstanciaTutoradoPdfService constanciaPdfService;
    @Autowired private DeteccionPdfService deteccionPdfService;

    private Tutorado resolverTutorado(Authentication auth) {
        return usuarioRepository.findByUsername(auth.getName())
                .map(Usuario::getTutorado)
                .orElseThrow(() -> new IllegalStateException("No se encontró tutorado asociado a la sesión"));
    }

    private Semestre semestreVigente() {
        return semestreRepository.findFirstByActivoOrderByIdDesc(1).orElse(null);
    }

    @GetMapping({"", "/", "/mi-tutoria"})
    public String miTutoria(Authentication auth, Model model) {
        Tutorado tutorado = resolverTutorado(auth);
        Semestre vigente = semestreVigente();
        model.addAttribute("tutorado", tutorado);
        model.addAttribute("semestreVigente", vigente);

        ResumenAsistenciaDTO resumen = null;
        try {
            resumen = asistenciaService.calcularResumenAsistencia(tutorado.getId());
        } catch (Exception e) {
            log.warn("No se pudo calcular resumen de asistencia para tutorado {}", tutorado.getId());
        }
        model.addAttribute("resumen", resumen);

        List<GrupoTutorado> tutorias = grupoTutoradoService.buscarTutoriasPorTutorado(tutorado.getId());
        GrupoTutorado tutoriaVigente = tutorias.stream()
                .filter(gt -> vigente != null && gt.getGrupo() != null
                        && gt.getGrupo().getSemestre() != null
                        && vigente.getId().equals(gt.getGrupo().getSemestre().getId())
                        && Integer.valueOf(1).equals(gt.getActivo()))
                .findFirst().orElse(null);
        model.addAttribute("tutoriaVigente", tutoriaVigente);
        model.addAttribute("tutor", tutoriaVigente != null ? tutoriaVigente.getGrupo().getTutor() : null);
        return "tutorado/mi-tutoria";
    }

    @GetMapping("/sesiones")
    public String misSesiones(Authentication auth, Model model) {
        Tutorado tutorado = resolverTutorado(auth);
        model.addAttribute("semestreVigente", semestreVigente());

        List<GrupoTutorado> tutorias = grupoTutoradoService.buscarTutoriasPorTutorado(tutorado.getId());
        List<Sesion> sesiones = tutorias.stream()
                .filter(gt -> gt.getGrupo() != null)
                .flatMap(gt -> sesionService.buscarSesionesPorGrupo(gt.getGrupo().getId()).stream())
                .sorted(Comparator.comparing(Sesion::getSemana, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        Map<Integer, Asistencia> mapaAsistencias = asistenciaService
                .buscarAsistenciasPorTutorado(tutorado.getId()).stream()
                .filter(a -> a.getSesion() != null)
                .collect(Collectors.toMap(a -> a.getSesion().getId(), a -> a, (a, b) -> a));

        model.addAttribute("sesiones", sesiones);
        model.addAttribute("mapaAsistencias", mapaAsistencias);
        return "tutorado/sesiones";
    }

    @GetMapping("/deteccion")
    public String miDeteccion(Authentication auth, Model model) {
        Tutorado tutorado = resolverTutorado(auth);
        model.addAttribute("tutorado", tutorado);
        model.addAttribute("detecciones", deteccionNecesidadesService.buscarPorTutorado(tutorado.getId()));
        return "tutorado/deteccion";
    }

    @GetMapping("/deteccion/pdf")
    @ResponseBody
    public ResponseEntity<byte[]> deteccionPdf(Authentication auth) {
        Tutorado tutorado = resolverTutorado(auth);
        List<DeteccionNecesidades> detecciones = deteccionNecesidadesService.buscarPorTutorado(tutorado.getId());
        if (detecciones.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        DeteccionNecesidades deteccion = detecciones.get(0);
        if (deteccion.getTutorado() == null || !tutorado.getId().equals(deteccion.getTutorado().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            byte[] pdf = deteccionPdfService.generarPdfDeteccion(deteccion.getId());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"deteccion-" + tutorado.getId() + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            log.error("Error generando PDF detección para tutorado {}", tutorado.getId(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/documentos")
    public String documentos(Authentication auth, Model model) {
        Tutorado tutorado = resolverTutorado(auth);
        Semestre vigente = semestreVigente();
        model.addAttribute("tutorado", tutorado);
        model.addAttribute("semestreVigente", vigente);

        ResumenAsistenciaDTO resumen = null;
        try {
            resumen = asistenciaService.calcularResumenAsistencia(tutorado.getId());
        } catch (Exception e) {
            log.warn("No se pudo calcular resumen para documentos del tutorado {}", tutorado.getId());
        }
        model.addAttribute("resumen", resumen);
        model.addAttribute("carnetError", carnetPdfService.validar(tutorado.getId()));
        model.addAttribute("constanciaError", vigente != null
                ? constanciaPdfService.validar(tutorado.getId(), vigente.getId())
                : "No hay semestre vigente.");
        return "tutorado/documentos";
    }

    @GetMapping("/documentos/carnet")
    @ResponseBody
    public ResponseEntity<byte[]> descargarCarnet(Authentication auth) {
        Tutorado tutorado = resolverTutorado(auth);
        String error = carnetPdfService.validar(tutorado.getId());
        if (error != null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            byte[] pdf = carnetPdfService.generarCarnetTutorado(tutorado.getId());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"carnet-" + tutorado.getId() + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            log.error("Error generando carnet para tutorado {}", tutorado.getId(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/documentos/constancia")
    @ResponseBody
    public ResponseEntity<byte[]> descargarConstancia(Authentication auth) {
        Tutorado tutorado = resolverTutorado(auth);
        Semestre vigente = semestreVigente();
        if (vigente == null) {
            return ResponseEntity.badRequest().build();
        }
        String error = constanciaPdfService.validar(tutorado.getId(), vigente.getId());
        if (error != null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            byte[] pdf = constanciaPdfService.generarConstanciaTutorado(tutorado.getId(), vigente.getId());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"constancia-" + tutorado.getId() + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            log.error("Error generando constancia para tutorado {}", tutorado.getId(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
