package com.bumh3r.controller;

import com.bumh3r.dto.SearchResultDTO;
import com.bumh3r.entity.*;
import com.bumh3r.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class ApiSearchController {

    @Autowired private ITutorRepository tutorRepository;
    @Autowired private ITutoradoRepository tutoradoRepository;
    @Autowired private IGrupoRepository grupoRepository;
    @Autowired private IPATRepository patRepository;
    @Autowired private ISemestreRepository semestreRepository;
    @Autowired private ICarreraRepository carreraRepository;
    @Autowired private IActividadRepository actividadRepository;
    @Autowired private ISesionRepository sesionRepository;

    private Map<String, Object> toResponse(Page<?> page, List<SearchResultDTO> content) {
        return Map.of(
            "content", content,
            "totalPages", page.getTotalPages(),
            "number", page.getNumber(),
            "totalElements", page.getTotalElements()
        );
    }

    @GetMapping("/tutor")
    public Map<String, Object> searchTutor(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        Page<Tutor> pg = tutorRepository.searchByName(q, PageRequest.of(page, size, Sort.by("nombre")));
        List<SearchResultDTO> content = pg.getContent().stream()
            .map(t -> new SearchResultDTO(
                t.getId(),
                t.getNombre() + " " + t.getApellido(),
                t.getNumeroControl(),
                t.getFoto() != null ? "/tutor/" + t.getFoto() : null
            )).toList();
        return toResponse(pg, content);
    }

    @GetMapping("/tutorado")
    public Map<String, Object> searchTutorado(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        Page<Tutorado> pg = tutoradoRepository.searchByName(q, PageRequest.of(page, size, Sort.by("nombre")));
        List<SearchResultDTO> content = pg.getContent().stream()
            .map(t -> new SearchResultDTO(
                t.getId(),
                t.getNombre() + " " + t.getApellido(),
                t.getNumeroControl(),
                t.getFoto() != null ? "/tutorado/" + t.getFoto() : null
            )).toList();
        return toResponse(pg, content);
    }

    @GetMapping("/grupo")
    public Map<String, Object> searchGrupo(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        Page<Grupo> pg = grupoRepository.searchByName(q, PageRequest.of(page, size, Sort.by("nombre")));
        List<SearchResultDTO> content = pg.getContent().stream()
            .map(g -> {
                StringBuilder sub = new StringBuilder();
                if (g.getTutor() != null)
                    sub.append(g.getTutor().getNombre()).append(" ").append(g.getTutor().getApellido());
                if (g.getSemestre() != null) {
                    if (!sub.isEmpty()) sub.append(" — ");
                    sub.append(g.getSemestre().getPeriodo()).append(" ").append(g.getSemestre().getAnio());
                }
                return new SearchResultDTO(g.getId(), g.getNombre(), sub.isEmpty() ? null : sub.toString(), null);
            }).toList();
        return toResponse(pg, content);
    }

    @GetMapping("/pat")
    public Map<String, Object> searchPAT(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        Page<PAT> pg = patRepository.searchByName(q, PageRequest.of(page, size, Sort.by("nombre")));
        List<SearchResultDTO> content = pg.getContent().stream()
            .map(p -> {
                String desc = p.getDescripcion();
                if (desc != null && desc.length() > 60) desc = desc.substring(0, 60) + "…";
                return new SearchResultDTO(p.getId(), p.getNombre(), desc,
                    p.getFoto() != null ? "/pat/" + p.getFoto() : null);
            }).toList();
        return toResponse(pg, content);
    }

    @GetMapping("/semestre")
    public Map<String, Object> searchSemestre(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        Page<Semestre> pg = semestreRepository.searchByName(q,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "anio").and(Sort.by("periodo"))));
        List<SearchResultDTO> content = pg.getContent().stream()
            .map(s -> new SearchResultDTO(s.getId(),
                s.getPeriodo() + " " + s.getAnio(),
                s.getActivo() == 1 ? "Activo" : "Inactivo",
                null))
            .toList();
        return toResponse(pg, content);
    }

    @GetMapping("/carrera")
    public Map<String, Object> searchCarrera(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        Page<Carrera> pg = carreraRepository.searchByName(q, PageRequest.of(page, size, Sort.by("nombre")));
        List<SearchResultDTO> content = pg.getContent().stream()
            .map(c -> new SearchResultDTO(c.getId(), c.getNombre(), c.getClave(), null))
            .toList();
        return toResponse(pg, content);
    }

    @GetMapping("/actividad")
    public Map<String, Object> searchActividad(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        Page<Actividad> pg = actividadRepository.searchByName(q,
            PageRequest.of(page, size, Sort.by("semana").and(Sort.by("nombre"))));
        List<SearchResultDTO> content = pg.getContent().stream()
            .map(a -> new SearchResultDTO(a.getId(), a.getNombre(), "Semana " + a.getSemana(),
                a.getFoto() != null ? "/actividad/" + a.getFoto() : null))
            .toList();
        return toResponse(pg, content);
    }

    @GetMapping("/sesion")
    public Map<String, Object> searchSesion(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        Page<Sesion> pg = sesionRepository.searchByName(q,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "semana")));
        List<SearchResultDTO> content = pg.getContent().stream()
            .map(s -> {
                String nombre = "Semana " + s.getSemana();
                if (s.getGrupo() != null) nombre += " — " + s.getGrupo().getNombre();
                return new SearchResultDTO(s.getId(), nombre, s.getEstatusRegistro(), null);
            }).toList();
        return toResponse(pg, content);
    }
}
