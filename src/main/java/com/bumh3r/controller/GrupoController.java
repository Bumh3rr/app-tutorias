package com.bumh3r.controller;

import com.bumh3r.entity.Grupo;
import com.bumh3r.entity.GrupoTutorado;
import com.bumh3r.entity.Tutor;
import com.bumh3r.entity.Tutorado;
import com.bumh3r.service.CarreraService;
import com.bumh3r.service.GrupoService;
import com.bumh3r.service.GrupoTutoradoService;
import com.bumh3r.service.SemestreService;
import com.bumh3r.service.TutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping(value = "grupo")
public class GrupoController {

    @Autowired
    private GrupoService grupoService;
    @Autowired
    private GrupoTutoradoService grupoTutoradoService;
    @Autowired
    private TutorService tutorService;
    @Autowired
    private SemestreService semestreService;
    @Autowired
    private CarreraService carreraService;

    private static final Logger log = LoggerFactory.getLogger(GrupoController.class);

    @GetMapping()
    public String obtenerVistaListaGrupos(
            @RequestParam(value = "idSemestre", required = false) Integer idSemestre,
            @RequestParam(value = "idTutor", required = false) Integer idTutor,
            @RequestParam(value = "idCarrera", required = false) Integer idCarrera,
            @RequestParam(value = "tipoBusqueda", required = false, defaultValue = "todos") String tipoBusqueda,
            @RequestParam(value = "q", required = false, defaultValue = "") String q,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "sort", required = false, defaultValue = "desc") String sort,
            @RequestParam(value = "sortBy", required = false, defaultValue = "id") String sortBy,
            Model model) {

        // Valid sort fields
        List<String> validSortFields = List.of("id", "nombre");
        if (!validSortFields.contains(sortBy)) sortBy = "nombre";
        if (!"asc".equals(sort) && !"desc".equals(sort)) sort = "asc";

        Sort.Direction direction = sort.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(direction, sortBy));

        org.springframework.data.domain.Page<Grupo> pageResult;
        try {
            if ("nombre".equals(tipoBusqueda) && !q.isBlank()) {
                pageResult = this.grupoService.buscarPorNombrePage(q, pageable);
                model.addAttribute("filtro", "Nombre: " + q);
            } else if ("semestre".equals(tipoBusqueda) && idSemestre != null) {
                pageResult = this.grupoService.buscarPorSemestrePage(idSemestre, pageable);
                model.addAttribute("filtro", "Semestre seleccionado");
            } else if ("tutorSemestre".equals(tipoBusqueda) && idTutor != null && idSemestre != null) {
                pageResult = this.grupoService.buscarPorTutorYSemestrePage(idTutor, idSemestre, pageable);
                model.addAttribute("filtro", "Tutor y semestre seleccionados");
            } else if ("carreraSemestre".equals(tipoBusqueda) && idCarrera != null && idSemestre != null) {
                pageResult = this.grupoService.buscarPorCarreraYSemestrePage(idCarrera, idSemestre, pageable);
                model.addAttribute("filtro", "Carrera y semestre seleccionados");
            } else {
                pageResult = this.grupoService.obtenerTodosGruposPage(pageable);
                model.addAttribute("filtro", null);
            }
        } catch (Exception e) {
            pageResult = this.grupoService.obtenerTodosGruposPage(pageable);
            model.addAttribute("msg_error", "Error en la búsqueda: " + e.getMessage());
        }

        model.addAttribute("grupos", pageResult.getContent());
        model.addAttribute("paginaActual", pageResult.getNumber());
        model.addAttribute("totalPaginas", pageResult.getTotalPages());
        model.addAttribute("totalElementos", pageResult.getTotalElements());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("sort", sort);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("tutores", this.tutorService.obtenerTodosTutores());
        model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
        model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
        model.addAttribute("idSemestreSeleccionado", idSemestre);
        model.addAttribute("idTutorSeleccionado", idTutor);
        model.addAttribute("idCarreraSeleccionada", idCarrera);
        model.addAttribute("q", q);
        return "grupo/viewListaGrupo";
    }

    @GetMapping(value = "agregar")
    public String obtenerVistaAgregarGrupo(
            @RequestParam(value = "idTutor", required = false) Integer idTutor,
            Model model) {

        Grupo grupo = new Grupo();
        boolean tutorLocked = false;
        Tutor tutorFijo = null;

        if (idTutor != null) {
            tutorFijo = this.tutorService.obtenerTutor(idTutor);
            if (tutorFijo != null) {
                Tutor ref = new Tutor();
                ref.setId(idTutor);
                grupo.setTutor(ref);
                tutorLocked = true;
            }
        }

        model.addAttribute("grupo", grupo);
        model.addAttribute("tutores", this.tutorService.obtenerTodosTutores());
        model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
        model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
        model.addAttribute("isEdit", false);
        model.addAttribute("tutorLocked", tutorLocked);
        model.addAttribute("tutorFijo", tutorFijo);
        return "grupo/viewFormGrupo";
    }

    @PostMapping(value = "guardar")
    public String guardarGrupo(@Valid Grupo grupo, BindingResult result, Model model, RedirectAttributes attributes) {
        if (grupo.getSemestre() == null || grupo.getSemestre().getId() == null) {
            result.rejectValue("semestre", "required", "El semestre es obligatorio");
        }
        if (grupo.getCarrera() == null || grupo.getCarrera().getId() == null) {
            result.rejectValue("carrera", "required", "La carrera es obligatoria");
        }
        if (result.hasErrors()) {
            model.addAttribute("tutores", this.tutorService.obtenerTodosTutores());
            model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
            model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
            model.addAttribute("isEdit", false);
            model.addAttribute("tutorLocked", false);
            model.addAttribute("tutorFijo", null);
            return "grupo/viewFormGrupo";
        }
        try {
            log.info("Guardar grupo: {}", grupo);
            this.grupoService.guardarGrupo(grupo);
            attributes.addFlashAttribute("msg_success", "Grupo guardado correctamente");
        } catch (Exception e) {
            model.addAttribute("msg_error", "Error al guardar el grupo: " + e.getMessage());
            model.addAttribute("tutores", this.tutorService.obtenerTodosTutores());
            model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
            model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
            model.addAttribute("isEdit", false);
            model.addAttribute("tutorLocked", false);
            model.addAttribute("tutorFijo", null);
            return "grupo/viewFormGrupo";
        }
        return "redirect:/grupo";
    }

    @GetMapping(value = "ver/{id}")
    public String obtenerVistaVerGrupo(@PathVariable Integer id, Model model) {
        Grupo grupo = this.grupoService.obtenerGrupo(id);
        List<GrupoTutorado> tutorados = this.grupoTutoradoService.buscarPorGrupo(id);
        log.info("Grupo: {}", grupo);
        model.addAttribute("grupo", grupo);
        model.addAttribute("tutorados", tutorados);
        return "grupo/viewInfoGrupo";
    }

    @GetMapping(value = "actualizar/{id}")
    public String obtenerVistaActualizarGrupo(@PathVariable Integer id, Model model) {
        Grupo grupo = this.grupoService.obtenerGrupo(id);
        log.info("Grupo a actualizar: {}", grupo);
        model.addAttribute("grupo", grupo);
        model.addAttribute("tutores", this.tutorService.obtenerTodosTutores());
        model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
        model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
        model.addAttribute("isEdit", true);
        model.addAttribute("tutorLocked", false);
        model.addAttribute("tutorFijo", null);
        return "grupo/viewFormGrupo";
    }

    @PostMapping(value = "actualizar/{id}")
    public String actualizarGrupo(
            @PathVariable Integer id,
            @Valid Grupo grupo,
            BindingResult result,
            Model model,
            RedirectAttributes attributes) {
        if (grupo.getSemestre() == null || grupo.getSemestre().getId() == null) {
            result.rejectValue("semestre", "required", "El semestre es obligatorio");
        }
        if (grupo.getCarrera() == null || grupo.getCarrera().getId() == null) {
            result.rejectValue("carrera", "required", "La carrera es obligatoria");
        }
        if (result.hasErrors()) {
            model.addAttribute("tutores", this.tutorService.obtenerTodosTutores());
            model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
            model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
            model.addAttribute("isEdit", true);
            model.addAttribute("tutorLocked", false);
            model.addAttribute("tutorFijo", null);
            return "grupo/viewFormGrupo";
        }
        try {
            log.info("Actualizar grupo {}: {}", id, grupo);
            this.grupoService.actualizarGrupo(id, grupo);
            attributes.addFlashAttribute("msg_success", "Grupo actualizado correctamente");
        } catch (Exception e) {
            model.addAttribute("msg_error", "Error al actualizar el grupo: " + e.getMessage());
            model.addAttribute("tutores", this.tutorService.obtenerTodosTutores());
            model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
            model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
            model.addAttribute("isEdit", true);
            model.addAttribute("tutorLocked", false);
            model.addAttribute("tutorFijo", null);
            return "grupo/viewFormGrupo";
        }
        return "redirect:/grupo";
    }

    @GetMapping(value = "delete/{id}")
    public String obtenerVistaConfirmarEliminarGrupo(@PathVariable Integer id, Model model) {
        Grupo grupo = this.grupoService.obtenerGrupo(id);
        model.addAttribute("grupo", grupo);
        return "grupo/viewConfirmDeleteGrupo";
    }

    @PostMapping(value = "confirm/delete/{id}")
    public String eliminarGrupo(@PathVariable Integer id, RedirectAttributes attributes) {
        try {
            this.grupoService.eliminarGrupo(id);
            attributes.addFlashAttribute("msg_success", "Grupo eliminado correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al eliminar el grupo: " + e.getMessage());
        }
        return "redirect:/grupo";
    }

    @GetMapping(value = "asignar/{idGrupo}")
    public String obtenerVistaAsignarTutorados(@PathVariable Integer idGrupo, Model model) {
        Grupo grupo = this.grupoService.obtenerGrupo(idGrupo);
        List<Tutorado> disponibles = this.grupoTutoradoService.obtenerTutoradosDisponibles(idGrupo);
        model.addAttribute("grupo", grupo);
        model.addAttribute("tutoradosDisponibles", disponibles);
        return "grupo/viewAsignarTutorados";
    }

    @PostMapping(value = "tutorado/quitar/{id}")
    public String quitarTutorado(
            @PathVariable Integer id,
            @RequestParam Integer idGrupo,
            RedirectAttributes attributes) {
        try {
            this.grupoTutoradoService.eliminarTutoradoDeGrupo(id);
            attributes.addFlashAttribute("msg_success", "Tutorado removido del grupo correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al quitar el tutorado: " + e.getMessage());
        }
        return "redirect:/grupo/ver/" + idGrupo;
    }

    @PostMapping(value = "asignar/{idGrupo}")
    public String asignarTutorados(
            @PathVariable Integer idGrupo,
            @RequestParam(value = "idsTutorados", required = false) Integer[] idsTutorados,
            RedirectAttributes attributes) {
        try {
            this.grupoTutoradoService.asignarTutorados(idGrupo, idsTutorados);
            attributes.addFlashAttribute("msg_success", "Tutorados asignados correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al asignar tutorados: " + e.getMessage());
        }
        return "redirect:/grupo/ver/" + idGrupo;
    }
}
