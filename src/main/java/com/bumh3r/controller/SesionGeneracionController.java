package com.bumh3r.controller;

import com.bumh3r.dto.PreviewGeneracionDTO;
import com.bumh3r.service.SesionGeneracionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("admin/grupo")
public class SesionGeneracionController {

    @Autowired
    private SesionGeneracionService sesionGeneracionService;

    @PostMapping("/{idGrupo}/sesiones/preview")
    @ResponseBody
    public ResponseEntity<PreviewGeneracionDTO> preview(
            @PathVariable Integer idGrupo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false, defaultValue = "") String idsPats) {
        try {
            List<Integer> patIds = parseIds(idsPats);
            PreviewGeneracionDTO dto = sesionGeneracionService.previewGeneracion(idGrupo, fechaInicio, patIds);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            PreviewGeneracionDTO err = new PreviewGeneracionDTO();
            err.setError("Error al calcular preview: " + e.getMessage());
            return ResponseEntity.ok(err);
        }
    }

    @PostMapping("/{idGrupo}/sesiones/generar")
    public String generar(
            @PathVariable Integer idGrupo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false, defaultValue = "") String idsPats,
            RedirectAttributes attributes) {
        try {
            List<Integer> patIds = parseIds(idsPats);
            sesionGeneracionService.generarSesiones(idGrupo, fechaInicio, patIds);
            attributes.addFlashAttribute("msg_success", "Se generaron 10 sesiones correctamente para el grupo.");
        } catch (IllegalStateException e) {
            attributes.addFlashAttribute("msg_error", e.getMessage());
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al generar sesiones: " + e.getMessage());
        }
        return "redirect:/admin/grupo/ver/" + idGrupo;
    }

    private List<Integer> parseIds(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}
