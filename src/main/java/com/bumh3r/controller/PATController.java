package com.bumh3r.controller;

import com.bumh3r.entity.Carrera;
import com.bumh3r.entity.PAT;
import com.bumh3r.entity.Semestre;
import com.bumh3r.service.CarreraService;
import com.bumh3r.service.FileStoreService;
import com.bumh3r.service.PATService;
import com.bumh3r.service.SemestreService;
import com.bumh3r.service.enums.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@Controller
@RequestMapping(value = "pat")
public class PATController {

    @Autowired
    private PATService patService;
    @Autowired
    private CarreraService carreraService;
    @Autowired
    private SemestreService semestreService;
    @Autowired
    private FileStoreService fileStoreService;

    private final Logger log = LoggerFactory.getLogger(PATController.class);

    @GetMapping()
    public String obtenerVistaListaPAT(
            @RequestParam(value = "idCarrera", required = false) Integer idCarrera,
            @RequestParam(value = "idSemestre", required = false) Integer idSemestre,
            @RequestParam(value = "soloGenerales", required = false) Boolean soloGenerales,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sort", defaultValue = "desc") String sort,
            Model model) {

        Page<PAT> pats;
        List<Carrera> carreras = this.carreraService.obtenerTodasCarreras();
        List<Semestre> semestres = this.semestreService.obtenerTodosSemestres();

        try {
            if (Boolean.TRUE.equals(soloGenerales)) {
                pats = this.patService.obtenerPATGeneralesPaginacion(page, pageSize, sortBy, sort);
                model.addAttribute("filtro", "Solo PAT generales");
            } else if (idCarrera != null && idSemestre != null) {
                pats = this.patService.buscarPATporCarreraYSemestrePaginacion(idCarrera, idSemestre, page, pageSize, sortBy, sort);
                model.addAttribute("filtro", "Carrera y semestre seleccionados");
            } else {
                pats = this.patService.obtenerTodosPATPaginacion(page, pageSize, sortBy, sort);
                model.addAttribute("filtro", null);
            }
        } catch (Exception e) {
            pats = this.patService.obtenerTodosPATPaginacion(0,pageSize,sortBy, "desc");
            model.addAttribute("msg_error", "Error en la búsqueda: " + e.getMessage());
        }

        HashMap<String, String> mapSort = new LinkedHashMap<>();
        mapSort.put("id", "ID");

        model.addAttribute("pats", pats.getContent());
        model.addAttribute("paginaActual", pats.getNumber());
        model.addAttribute("totalPaginas", pats.getTotalPages());
        model.addAttribute("totalElementos", pats.getTotalElements());
        model.addAttribute("pageSize", pageSize);

        model.addAttribute("carreras", carreras);
        model.addAttribute("semestres", semestres);
        model.addAttribute("idCarreraSeleccionada", idCarrera);
        model.addAttribute("idSemestreSeleccionado", idSemestre);
        model.addAttribute("soloGenerales", soloGenerales);

        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sort", sort);
        model.addAttribute("mapSort", mapSort);

        return "pat/viewListaPAT";
    }

    @GetMapping(value = "agregar")
    public String obtenerVistaAgregarPAT(Model model) {
        model.addAttribute("pat", new PAT());
        model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
        model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
        model.addAttribute("isEdit", false);
        return "pat/viewFormPAT";
    }

    @PostMapping(value = "guardar")
    public String guardarPAT(
            @Valid PAT pat,
            BindingResult result,
            @RequestParam(value = "fotoFile", required = false) MultipartFile fotoFile,
            Model model,
            RedirectAttributes attributes) {
        pat.setFoto(pat.getFoto() != null && !pat.getFoto().isEmpty() ? pat.getFoto() : null);
        if (result.hasErrors()) {
            model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
            model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
            model.addAttribute("isEdit", false);
            return "pat/viewFormPAT";
        }
        try {
            if (fotoFile != null && !fotoFile.isEmpty()) {
                String foto = this.fileStoreService.save(fotoFile, FileType.PAT);
                pat.setFoto(foto);
            }
            log.info("Guardar PAT: {}", pat);
            this.patService.guardarPAT(pat);
            attributes.addFlashAttribute("msg_success", "PAT guardado correctamente");
        } catch (Exception e) {
            model.addAttribute("msg_error", "Error al guardar el PAT: " + e.getMessage());
            model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
            model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
            model.addAttribute("isEdit", false);
            return "pat/viewFormPAT";
        }
        return "redirect:/pat";
    }

    @GetMapping(value = "ver/{id}")
    public String obtenerVistaVerPAT(@PathVariable Integer id, Model model) {
        PAT pat = this.patService.obtenerPAT(id);
        log.info("PAT: {}", pat);
        model.addAttribute("pat", pat);
        return "pat/viewInfoPAT";
    }

    @GetMapping(value = "actualizar/{id}")
    public String obtenerVistaActualizarPAT(@PathVariable Integer id, Model model) {
        PAT pat = this.patService.obtenerPAT(id);
        log.info("PAT a actualizar: {}", pat);
        model.addAttribute("pat", pat);
        model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
        model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
        model.addAttribute("isEdit", true);
        return "pat/viewFormPAT";
    }

    @PostMapping(value = "actualizar/{id}")
    public String actualizarPAT(
            @PathVariable Integer id,
            @Valid PAT pat,
            BindingResult result,
            @RequestParam(value = "fotoFile", required = false) MultipartFile fotoFile,
            Model model,
            RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
            model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
            model.addAttribute("isEdit", true);
            return "pat/viewFormPAT";
        }
        try {
            if (fotoFile != null && !fotoFile.isEmpty()) {
                this.fileStoreService.delete(pat.getFoto(), FileType.PAT);
                String foto = this.fileStoreService.save(fotoFile, FileType.PAT);
                pat.setFoto(foto);
            }
            log.info("Actualizar PAT {}: {}", id, pat);
            this.patService.actualizarPAT(id, pat);
            attributes.addFlashAttribute("msg_success", "PAT actualizado correctamente");
        } catch (Exception e) {
            model.addAttribute("msg_error", "Error al actualizar el PAT: " + e.getMessage());
            model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
            model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
            model.addAttribute("isEdit", true);
            return "pat/viewFormPAT";
        }
        return "redirect:/pat";
    }

    @GetMapping(value = "delete/{id}")
    public String obtenerVistaConfirmarEliminarPAT(@PathVariable Integer id, Model model) {
        PAT pat = this.patService.obtenerPAT(id);
        model.addAttribute("pat", pat);
        return "pat/viewConfirmDeletePAT";
    }

    @PostMapping(value = "confirm/delete/{id}")
    public String eliminarPAT(@PathVariable Integer id, RedirectAttributes attributes) {
        try {
            this.patService.eliminarPAT(id);
            attributes.addFlashAttribute("msg_success", "PAT eliminado correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al eliminar el PAT: " + e.getMessage());
        }
        return "redirect:/pat";
    }
}