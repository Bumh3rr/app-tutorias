package com.bumh3r.controller;

import com.bumh3r.entity.Carrera;
import com.bumh3r.entity.Semestre;
import com.bumh3r.entity.Tutorado;
import com.bumh3r.service.CarreraService;
import com.bumh3r.service.FileStoreService;
import com.bumh3r.service.SemestreService;
import com.bumh3r.service.TutoradoService;
import com.bumh3r.service.enums.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "tutorado")
public class TutoradoController {

    @Autowired
    private TutoradoService tutoradoService;
    @Autowired
    private CarreraService carreraService;
    @Autowired
    private SemestreService semestreService;
    @Autowired
    private FileStoreService fileStoreService;

    private final Logger log = LoggerFactory.getLogger(TutoradoController.class);

    @GetMapping()
    public String obtenerVistaListaTutorados(
            @RequestParam(value = "idSemestre", required = false) Integer idSemestre,
            @RequestParam(value = "idCarrera", required = false) Integer idCarrera,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sort", defaultValue = "desc") String sort,
            Model model) {

        Page<Tutorado> paginaTutorados;
        List<Semestre> semestres = this.semestreService.obtenerTodosSemestres();
        List<Carrera> carreras = this.carreraService.obtenerTodasCarreras();

        try {
            if (idSemestre != null && idCarrera != null) {
                paginaTutorados = this.tutoradoService.buscarTutoradosPorSemestreYCarreraPaginado(idSemestre, idCarrera, page, pageSize, sortBy, sort);
                model.addAttribute("filtro", "Semestre y carrera seleccionados");
            } else {
                paginaTutorados = this.tutoradoService.obtenerTodosTutoradosPaginado(page, pageSize, sortBy, sort);
                model.addAttribute("filtro", null);
            }
        } catch (Exception e) {
            paginaTutorados = this.tutoradoService.obtenerTodosTutoradosPaginado(0, pageSize, "id", "desc");
            model.addAttribute("msg_error", "Error en la búsqueda: " + e.getMessage());
        }

        HashMap<String, String> mapSort = new LinkedHashMap<>();
        mapSort.put("id", "ID");
        mapSort.put("nombre", "Nombre");
        mapSort.put("numeroControl", "No. Control");

        model.addAttribute("tutorados", paginaTutorados.getContent());
        model.addAttribute("paginaActual", paginaTutorados.getNumber());
        model.addAttribute("totalPaginas", paginaTutorados.getTotalPages());
        model.addAttribute("totalElementos", paginaTutorados.getTotalElements());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("semestres", semestres);
        model.addAttribute("carreras", carreras);
        model.addAttribute("idSemestreSeleccionado", idSemestre);
        model.addAttribute("idCarreraSeleccionada", idCarrera);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sort", sort);
        model.addAttribute("mapSort", mapSort);

        return "tutorado/viewListaTutorado";
    }

    @GetMapping(value = "agregar")
    public String obtenerVistaAgregarTutorado(Model model) {
        model.addAttribute("tutorado", new Tutorado());
        model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
        model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
        model.addAttribute("isEdit", false);
        return "tutorado/viewFormTutorado";
    }

    @PostMapping(value = "guardar")
    public String guardarTutorado(
            Tutorado tutorado,
            @RequestParam(value = "fotoFile", required = false) MultipartFile fotoFile,
            RedirectAttributes attributes) {
        try {
            if (fotoFile != null && !fotoFile.isEmpty()) {
                String foto = this.fileStoreService.save(fotoFile, FileType.TUTORADO);
                tutorado.setFoto(foto);
            }
            log.info("Guardar tutorado: {}", tutorado);
            this.tutoradoService.guardarTutorado(tutorado);
            attributes.addFlashAttribute("msg_success", "Tutorado guardado correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al guardar el tutorado: " + e.getMessage());
        }
        return "redirect:/tutorado";
    }

    @GetMapping(value = "ver/{id}")
    public String obtenerVistaVerTutorado(@PathVariable Integer id, Model model) {
        Tutorado tutorado = this.tutoradoService.obtenerTutorado(id);
        log.info("Tutorado: {}", tutorado);
        model.addAttribute("tutorado", tutorado);
        return "tutorado/viewInfoTutorado";
    }

    @GetMapping(value = "actualizar/{id}")
    public String obtenerVistaActualizarTutorado(@PathVariable Integer id, Model model) {
        Tutorado tutorado = this.tutoradoService.obtenerTutorado(id);
        log.info("Tutorado a actualizar: {}", tutorado);
        model.addAttribute("tutorado", tutorado);
        model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
        model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
        model.addAttribute("isEdit", true);
        return "tutorado/viewFormTutorado";
    }

    @PostMapping(value = "actualizar/{id}")
    public String actualizarTutorado(
            @PathVariable Integer id,
            Tutorado tutorado,
            @RequestParam(value = "fotoFile", required = false) MultipartFile fotoFile,
            RedirectAttributes attributes) {
        try {
            if (fotoFile != null && !fotoFile.isEmpty()) {
                this.fileStoreService.delete(tutorado.getFoto(), FileType.TUTORADO);
                String foto = this.fileStoreService.save(fotoFile, FileType.TUTORADO);
                tutorado.setFoto(foto);
            }
            log.info("Actualizar tutorado {}: {}", id, tutorado);
            this.tutoradoService.actualizarTutorado(id, tutorado);
            attributes.addFlashAttribute("msg_success", "Tutorado actualizado correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al actualizar el tutorado: " + e.getMessage());
        }
        return "redirect:/tutorado";
    }

    @GetMapping(value = "delete/{id}")
    public String obtenerVistaConfirmarEliminarTutorado(@PathVariable Integer id, Model model) {
        Tutorado tutorado = this.tutoradoService.obtenerTutorado(id);
        model.addAttribute("tutorado", tutorado);
        return "tutorado/viewConfirmDeleteTutorado";
    }

    @PostMapping(value = "confirm/delete/{id}")
    public String eliminarTutorado(@PathVariable Integer id, RedirectAttributes attributes) {
        try {
            this.tutoradoService.eliminarTutorado(id);
            attributes.addFlashAttribute("msg_success", "Tutorado eliminado correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al eliminar el tutorado: " + e.getMessage());
        }
        return "redirect:/tutorado";
    }
}