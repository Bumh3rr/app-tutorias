package com.bumh3r.controller;

import com.bumh3r.entity.CoordinadorCarrera;
import com.bumh3r.service.CarreraService;
import com.bumh3r.service.CoordinadorCarreraService;
import com.bumh3r.service.FileStoreService;
import com.bumh3r.service.SemestreService;
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
@RequestMapping(value = "coordinador")
public class CoordinadorCarreraController {

    @Autowired
    private CoordinadorCarreraService coordinadorCarreraService;
    @Autowired
    private CarreraService carreraService;
    @Autowired
    private SemestreService semestreService;
    @Autowired
    private FileStoreService fileStoreService;

    private static final Logger log = LoggerFactory.getLogger(CoordinadorCarreraController.class);

    @GetMapping()
    public String obtenerVistaListaCoordinadores(
            @RequestParam(value = "idCarrera", required = false) Integer idCarrera,
            @RequestParam(value = "idSemestre", required = false) Integer idSemestre,
            @RequestParam(value = "tipoBusqueda", required = false, defaultValue = "todos") String tipoBusqueda,
            Model model) {

        List<CoordinadorCarrera> coordinadores;

        try {
            if ("carrera".equals(tipoBusqueda) && idCarrera != null) {
                coordinadores = this.coordinadorCarreraService.buscarPorCarrera(idCarrera);
                model.addAttribute("filtro", "Carrera seleccionada");

            } else if ("semestre".equals(tipoBusqueda) && idSemestre != null) {
                coordinadores = this.coordinadorCarreraService.buscarPorSemestre(idSemestre);
                model.addAttribute("filtro", "Semestre seleccionado");

            } else if ("carreraSemestre".equals(tipoBusqueda) && idCarrera != null && idSemestre != null) {
                coordinadores = this.coordinadorCarreraService.buscarPorCarreraYSemestre(idCarrera, idSemestre);
                model.addAttribute("filtro", "Carrera y semestre seleccionados");

            } else {
                coordinadores = this.coordinadorCarreraService.obtenerTodosCoordinadores();
                model.addAttribute("filtro", null);
            }
        } catch (Exception e) {
            coordinadores = this.coordinadorCarreraService.obtenerTodosCoordinadores();
            model.addAttribute("msg_error", "Error en la búsqueda: " + e.getMessage());
        }

        model.addAttribute("coordinadores", coordinadores);
        model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
        model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
        model.addAttribute("idCarreraSeleccionada", idCarrera);
        model.addAttribute("idSemestreSeleccionado", idSemestre);
        return "coordinador/viewListaCoordinador";
    }

    @GetMapping(value = "agregar")
    public String obtenerVistaAgregarCoordinador(Model model) {
        model.addAttribute("coordinador", new CoordinadorCarrera());
        model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
        model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
        model.addAttribute("isEdit", false);
        return "coordinador/viewFormCoordinador";
    }

    @PostMapping(value = "guardar")
    public String guardarCoordinador(
            CoordinadorCarrera coordinador,
            @RequestParam(value = "fotoFile", required = false) MultipartFile fotoFile,
            RedirectAttributes attributes) {
        try {
            if (fotoFile != null && !fotoFile.isEmpty()) {
                String foto = this.fileStoreService.save(fotoFile, FileType.COORDINADOR);
                coordinador.setFoto(foto);
            }
            log.info("Guardar coordinador: {}", coordinador);
            this.coordinadorCarreraService.guardarCoordinador(coordinador);
            attributes.addFlashAttribute("msg_success", "Coordinador guardado correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al guardar el coordinador: " + e.getMessage());
        }
        return "redirect:/coordinador";
    }

    @GetMapping(value = "ver/{id}")
    public String obtenerVistaVerCoordinador(@PathVariable Integer id, Model model) {
        CoordinadorCarrera coordinador = this.coordinadorCarreraService.obtenerCoordinador(id);
        log.info("Coordinador: {}", coordinador);
        model.addAttribute("coordinador", coordinador);
        return "coordinador/viewInfoCoordinador";
    }

    @GetMapping(value = "actualizar/{id}")
    public String obtenerVistaActualizarCoordinador(@PathVariable Integer id, Model model) {
        CoordinadorCarrera coordinador = this.coordinadorCarreraService.obtenerCoordinador(id);
        log.info("Coordinador a actualizar: {}", coordinador);
        model.addAttribute("coordinador", coordinador);
        model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
        model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
        model.addAttribute("isEdit", true);
        return "coordinador/viewFormCoordinador";
    }

    @PostMapping(value = "actualizar/{id}")
    public String actualizarCoordinador(
            @PathVariable Integer id,
            CoordinadorCarrera coordinador,
            @RequestParam(value = "fotoFile", required = false) MultipartFile fotoFile,
            RedirectAttributes attributes) {
        try {
            if (fotoFile != null && !fotoFile.isEmpty()) {
                this.fileStoreService.delete(coordinador.getFoto(), FileType.COORDINADOR);
                String foto = this.fileStoreService.save(fotoFile, FileType.COORDINADOR);
                coordinador.setFoto(foto);
            }
            log.info("Actualizar coordinador {}: {}", id, coordinador);
            this.coordinadorCarreraService.actualizarCoordinador(id, coordinador);
            attributes.addFlashAttribute("msg_success", "Coordinador actualizado correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al actualizar el coordinador: " + e.getMessage());
        }
        return "redirect:/coordinador";
    }

    @GetMapping(value = "delete/{id}")
    public String obtenerVistaConfirmarEliminarCoordinador(@PathVariable Integer id, Model model) {
        CoordinadorCarrera coordinador = this.coordinadorCarreraService.obtenerCoordinador(id);
        model.addAttribute("coordinador", coordinador);
        return "coordinador/viewConfirmDeleteCoordinador";
    }

    @PostMapping(value = "confirm/delete/{id}")
    public String eliminarCoordinador(@PathVariable Integer id, RedirectAttributes attributes) {
        try {
            this.coordinadorCarreraService.eliminarCoordinador(id);
            attributes.addFlashAttribute("msg_success", "Coordinador eliminado correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al eliminar el coordinador: " + e.getMessage());
        }
        return "redirect:/coordinador";
    }
}