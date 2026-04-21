package com.bumh3r.controller;

import com.bumh3r.entity.AsignacionTutorado;
import com.bumh3r.entity.Semestre;
import com.bumh3r.entity.Tutor;
import com.bumh3r.entity.Tutorado;
import com.bumh3r.service.*;
import com.bumh3r.service.enums.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping(value = "asignacion")
public class AsignacionTutoradoController {

    @Autowired
    private AsignacionTutoradoService asignacionTutoradoService;
    @Autowired
    private TutorService tutorService;
    @Autowired
    private TutoradoService tutoradoService;
    @Autowired
    private SemestreService semestreService;
    @Autowired
    private FileStoreService fileStoreService;

    private final Logger log = LoggerFactory.getLogger(AsignacionTutoradoController.class);

    @GetMapping()
    public String obtenerVistaListaAsignaciones(
            @RequestParam(value = "idTutorado", required = false) Integer idTutorado,
            @RequestParam(value = "idTutor", required = false) Integer idTutor,
            @RequestParam(value = "idSemestre", required = false) Integer idSemestre,
            @RequestParam(value = "tipoBusqueda", required = false, defaultValue = "todos") String tipoBusqueda,
            Model model) {

        List<AsignacionTutorado> asignaciones;
        List<Tutor> tutores = this.tutorService.obtenerTodosTutores();
        List<Tutorado> tutorados = this.tutoradoService.obtenerTodosTutorados();
        List<Semestre> semestres = this.semestreService.obtenerTodosSemestres();

        try {
            if ("tutorado".equals(tipoBusqueda) && idTutorado != null) {
                // Búsqueda: tutorías cursadas por un tutorado
                asignaciones = this.asignacionTutoradoService.buscarTutoriasPorTutorado(idTutorado);
                model.addAttribute("filtro", "Tutorías del tutorado seleccionado");

            } else if ("tutorSemestre".equals(tipoBusqueda) && idTutor != null && idSemestre != null) {
                // Búsqueda: asignaciones de un tutor en un semestre
                asignaciones = this.asignacionTutoradoService.buscarAsignacionesPorTutorYSemestre(idTutor, idSemestre);
                model.addAttribute("filtro", "Tutor y semestre seleccionados");

            } else if ("semestre".equals(tipoBusqueda) && idSemestre != null) {
                // Búsqueda: asignaciones por semestre
                asignaciones = this.asignacionTutoradoService.buscarAsignacionesPorSemestre(idSemestre);
                model.addAttribute("filtro", "Semestre seleccionado");

            } else {
                asignaciones = this.asignacionTutoradoService.obtenerTodasAsignaciones();
                model.addAttribute("filtro", null);
            }
        } catch (Exception e) {
            asignaciones = this.asignacionTutoradoService.obtenerTodasAsignaciones();
            model.addAttribute("msg_error", "Error en la búsqueda: " + e.getMessage());
        }

        model.addAttribute("asignaciones", asignaciones);
        model.addAttribute("tutores", tutores);
        model.addAttribute("tutorados", tutorados);
        model.addAttribute("semestres", semestres);
        model.addAttribute("idTutoradoSeleccionado", idTutorado);
        model.addAttribute("idTutorSeleccionado", idTutor);
        model.addAttribute("idSemestreSeleccionado", idSemestre);
        return "asignacion/viewListaAsignacion";
    }

    @GetMapping(value = "agregar")
    public String obtenerVistaAgregarAsignacion(Model model) {
        model.addAttribute("asignacion", new AsignacionTutorado());
        model.addAttribute("tutores", this.tutorService.obtenerTodosTutores());
        model.addAttribute("tutorados", this.tutoradoService.obtenerTodosTutorados());
        model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
        model.addAttribute("isEdit", false);
        return "asignacion/viewFormAsignacion";
    }

    @PostMapping(value = "guardar")
    public String guardarAsignacion(
            AsignacionTutorado asignacion,
            @RequestParam(value = "fotoFile", required = false) MultipartFile fotoFile,
            RedirectAttributes attributes) {
        try {
            if (fotoFile != null && !fotoFile.isEmpty()) {
                String foto = this.fileStoreService.save(fotoFile, FileType.ASIGNACION);
                asignacion.setFoto(foto);
            }
            log.info("Guardar asignacion: {}", asignacion);
            this.asignacionTutoradoService.guardarAsignacion(asignacion);
            attributes.addFlashAttribute("msg_success", "Asignación guardada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al guardar la asignación: " + e.getMessage());
        }
        return "redirect:/asignacion";
    }

    @GetMapping(value = "ver/{id}")
    public String obtenerVistaVerAsignacion(@PathVariable Integer id, Model model) {
        AsignacionTutorado asignacion = this.asignacionTutoradoService.obtenerAsignacion(id);
        log.info("Asignacion: {}", asignacion);
        model.addAttribute("asignacion", asignacion);
        return "asignacion/viewInfoAsignacion";
    }

    @GetMapping(value = "actualizar/{id}")
    public String obtenerVistaActualizarAsignacion(@PathVariable Integer id, Model model) {
        AsignacionTutorado asignacion = this.asignacionTutoradoService.obtenerAsignacion(id);
        log.info("Asignacion a actualizar: {}", asignacion);
        model.addAttribute("asignacion", asignacion);
        model.addAttribute("tutores", this.tutorService.obtenerTodosTutores());
        model.addAttribute("tutorados", this.tutoradoService.obtenerTodosTutorados());
        model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
        model.addAttribute("isEdit", true);
        return "asignacion/viewFormAsignacion";
    }

    @PostMapping(value = "actualizar/{id}")
    public String actualizarAsignacion(
            @PathVariable Integer id,
            AsignacionTutorado asignacion,
            @RequestParam(value = "fotoFile", required = false) MultipartFile fotoFile,
            RedirectAttributes attributes) {
        try {
            if (fotoFile != null && !fotoFile.isEmpty()) {
                this.fileStoreService.delete(asignacion.getFoto(), FileType.ASIGNACION);
                String foto = this.fileStoreService.save(fotoFile, FileType.ASIGNACION);
                asignacion.setFoto(foto);
            }
            log.info("Actualizar asignacion {}: {}", id, asignacion);
            this.asignacionTutoradoService.actualizarAsignacion(id, asignacion);
            attributes.addFlashAttribute("msg_success", "Asignación actualizada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al actualizar la asignación: " + e.getMessage());
        }
        return "redirect:/asignacion";
    }

    @GetMapping(value = "delete/{id}")
    public String obtenerVistaConfirmarEliminarAsignacion(@PathVariable Integer id, Model model) {
        AsignacionTutorado asignacion = this.asignacionTutoradoService.obtenerAsignacion(id);
        model.addAttribute("asignacion", asignacion);
        return "asignacion/viewConfirmDeleteAsignacion";
    }

    @PostMapping(value = "confirm/delete/{id}")
    public String eliminarAsignacion(@PathVariable Integer id, RedirectAttributes attributes) {
        try {
            this.asignacionTutoradoService.eliminarAsignacion(id);
            attributes.addFlashAttribute("msg_success", "Asignación eliminada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al eliminar la asignación: " + e.getMessage());
        }
        return "redirect:/asignacion";
    }
}