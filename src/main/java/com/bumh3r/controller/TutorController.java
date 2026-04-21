package com.bumh3r.controller;

import com.bumh3r.entity.Carrera;
import com.bumh3r.entity.Semestre;
import com.bumh3r.entity.Tutor;
import com.bumh3r.service.CarreraService;
import com.bumh3r.service.FileStoreService;
import com.bumh3r.service.SemestreService;
import com.bumh3r.service.TutorService;
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
@RequestMapping(value = "tutor")
public class TutorController {

    @Autowired
    private TutorService tutorService;
    @Autowired
    private CarreraService carreraService;
    @Autowired
    private SemestreService semestreService;
    @Autowired
    private FileStoreService fileStoreService;

    private final Logger log = LoggerFactory.getLogger(TutorController.class);

    @GetMapping()
    public String obtenerVistaListaTutores(
            @RequestParam(value = "idSemestre", required = false) Integer idSemestre,
            @RequestParam(value = "idCarrera", required = false) Integer idCarrera,
            Model model) {

        List<Tutor> tutores;
        List<Semestre> semestres = this.semestreService.obtenerTodosSemestres();
        List<Carrera> carreras = this.carreraService.obtenerTodasCarreras();

        try {
            if (idSemestre != null && idCarrera != null) {
                tutores = this.tutorService.buscarTutoresPorSemestreYCarrera(idSemestre, idCarrera);
                model.addAttribute("filtro", "Semestre y carrera seleccionados");
            } else if (idSemestre != null) {
                tutores = this.tutorService.buscarTutoresPorSemestre(idSemestre);
                model.addAttribute("filtro", "Semestre seleccionado");
            } else {
                tutores = this.tutorService.obtenerTodosTutores();
                model.addAttribute("filtro", null);
            }
        } catch (Exception e) {
            tutores = this.tutorService.obtenerTodosTutores();
            model.addAttribute("msg_error", "Error en la búsqueda: " + e.getMessage());
        }

        model.addAttribute("tutores", tutores);
        model.addAttribute("semestres", semestres);
        model.addAttribute("carreras", carreras);
        model.addAttribute("idSemestreSeleccionado", idSemestre);
        model.addAttribute("idCarreraSeleccionada", idCarrera);
        return "tutor/viewListaTutor";
    }

    @GetMapping(value = "agregar")
    public String obtenerVistaAgregarTutor(Model model) {
        model.addAttribute("tutor", new Tutor());
        model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
        model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
        model.addAttribute("isEdit", false);
        return "tutor/viewFormTutor";
    }

    @PostMapping(value = "guardar")
    public String guardarTutor(
            Tutor tutor,
            @RequestParam(value = "fotoFile", required = false) MultipartFile fotoFile,
            RedirectAttributes attributes) {
        try {
            if (fotoFile != null && !fotoFile.isEmpty()) {
                String foto = this.fileStoreService.save(fotoFile, FileType.TUTOR);
                tutor.setFoto(foto);
            }
            log.info("Guardar tutor: {}", tutor);
            this.tutorService.guardarTutor(tutor);
            attributes.addFlashAttribute("msg_success", "Tutor guardado correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al guardar el tutor: " + e.getMessage());
        }
        return "redirect:/tutor";
    }

    @GetMapping(value = "ver/{id}")
    public String obtenerVistaVerTutor(@PathVariable Integer id, Model model) {
        Tutor tutor = this.tutorService.obtenerTutor(id);
        log.info("Tutor: {}", tutor);
        model.addAttribute("tutor", tutor);
        return "tutor/viewInfoTutor";
    }

    @GetMapping(value = "actualizar/{id}")
    public String obtenerVistaActualizarTutor(@PathVariable Integer id, Model model) {
        Tutor tutor = this.tutorService.obtenerTutor(id);
        log.info("Tutor a actualizar: {}", tutor);
        model.addAttribute("tutor", tutor);
        model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
        model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
        model.addAttribute("isEdit", true);
        return "tutor/viewFormTutor";
    }

    @PostMapping(value = "actualizar/{id}")
    public String actualizarTutor(
            @PathVariable Integer id,
            Tutor tutor,
            @RequestParam(value = "fotoFile", required = false) MultipartFile fotoFile,
            RedirectAttributes attributes) {
        try {
            if (fotoFile != null && !fotoFile.isEmpty()) {
                this.fileStoreService.delete(tutor.getFoto(), FileType.TUTOR);
                String foto = this.fileStoreService.save(fotoFile, FileType.TUTOR);
                tutor.setFoto(foto);
            }
            log.info("Actualizar tutor {}: {}", id, tutor);
            this.tutorService.actualizarTutor(id, tutor);
            attributes.addFlashAttribute("msg_success", "Tutor actualizado correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al actualizar el tutor: " + e.getMessage());
        }
        return "redirect:/tutor";
    }

    @GetMapping(value = "delete/{id}")
    public String obtenerVistaConfirmarEliminarTutor(@PathVariable Integer id, Model model) {
        Tutor tutor = this.tutorService.obtenerTutor(id);
        model.addAttribute("tutor", tutor);
        return "tutor/viewConfirmDeleteTutor";
    }

    @PostMapping(value = "confirm/delete/{id}")
    public String eliminarTutor(@PathVariable Integer id, RedirectAttributes attributes) {
        try {
            this.tutorService.eliminarTutor(id);
            attributes.addFlashAttribute("msg_success", "Tutor eliminado correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al eliminar el tutor: " + e.getMessage());
        }
        return "redirect:/tutor";
    }
}