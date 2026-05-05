package com.bumh3r.controller;

import com.bumh3r.entity.Carrera;
import com.bumh3r.entity.GrupoTutorado;
import com.bumh3r.entity.Tutorado;
import com.bumh3r.service.AsistenciaService;
import com.bumh3r.service.CarreraService;
import com.bumh3r.service.DeteccionNecesidadesService;
import com.bumh3r.service.FileStoreService;
import com.bumh3r.service.GrupoTutoradoService;
import com.bumh3r.service.TutoradoService;
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
import java.util.Map;

@Controller
@RequestMapping(value = "tutorado")
public class TutoradoController {

    @Autowired
    private TutoradoService tutoradoService;
    @Autowired
    private CarreraService carreraService;
    @Autowired
    private FileStoreService fileStoreService;
    @Autowired
    private GrupoTutoradoService grupoTutoradoService;
    @Autowired
    private DeteccionNecesidadesService deteccionNecesidadesService;
    @Autowired
    private AsistenciaService asistenciaService;

    private final Logger log = LoggerFactory.getLogger(TutoradoController.class);

    @GetMapping()
    public String obtenerVistaListaTutorados(
            @RequestParam(value = "tipoBusqueda", required = false, defaultValue = "todos") String tipoBusqueda,
            @RequestParam(value = "q", required = false, defaultValue = "") String q,
            @RequestParam(value = "idCarrera", required = false) Integer idCarrera,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sort", defaultValue = "desc") String sort,
            Model model) {

        Page<Tutorado> paginaTutorados;
        List<Carrera> carreras = this.carreraService.obtenerTodasCarreras();

        try {
            if ("nombre".equals(tipoBusqueda) && !q.isBlank()) {
                paginaTutorados = this.tutoradoService.buscarPorNombre(q, page, pageSize, sortBy, sort);
                model.addAttribute("filtro", "Nombre: " + q);
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
        model.addAttribute("carreras", carreras);
        model.addAttribute("idCarreraSeleccionada", idCarrera);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sort", sort);
        model.addAttribute("mapSort", mapSort);
        model.addAttribute("tipoBusqueda", tipoBusqueda);
        model.addAttribute("q", q);

        return "tutorado/viewListaTutorado";
    }

    @GetMapping(value = "agregar")
    public String obtenerVistaAgregarTutorado(Model model) {
        model.addAttribute("tutorado", new Tutorado());
        model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
        model.addAttribute("isEdit", false);
        return "tutorado/viewFormTutorado";
    }

    @PostMapping(value = "guardar")
    public String guardarTutorado(
            @Valid Tutorado tutorado,
            BindingResult result,
            @RequestParam(value = "fotoFile", required = false) MultipartFile fotoFile,
            Model model,
            RedirectAttributes attributes) {
        tutorado.setFoto(tutorado.getFoto() != null && !tutorado.getFoto().isEmpty() ? tutorado.getFoto() : null);
        if (result.hasErrors()) {
            model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
            model.addAttribute("isEdit", false);
            return "tutorado/viewFormTutorado";
        }
        try {
            if (fotoFile != null && !fotoFile.isEmpty()) {
                String foto = this.fileStoreService.save(fotoFile, FileType.TUTORADO);
                tutorado.setFoto(foto);
            }
            log.info("Guardar tutorado: {}", tutorado);
            this.tutoradoService.guardarTutorado(tutorado);
            attributes.addFlashAttribute("msg_success", "Tutorado guardado correctamente");
        } catch (Exception e) {
            model.addAttribute("msg_error", "Error al guardar el tutorado: " + e.getMessage());
            model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
            model.addAttribute("isEdit", false);
            return "tutorado/viewFormTutorado";
        }
        return "redirect:/tutorado";
    }

    @GetMapping(value = "ver/{id}")
    public String obtenerVistaVerTutorado(@PathVariable Integer id, Model model) {
        Tutorado tutorado = this.tutoradoService.obtenerTutorado(id);
        List<GrupoTutorado> gruposTutorado = this.grupoTutoradoService.buscarTutoriasPorTutorado(id);
        java.util.List<com.bumh3r.entity.DeteccionNecesidades> detecciones =
                this.deteccionNecesidadesService.buscarPorTutorado(id);
        com.bumh3r.dto.ResumenAsistenciaDTO resumen = this.asistenciaService.calcularResumenAsistencia(id);
        log.info("Tutorado: {}", tutorado);
        model.addAttribute("tutorado", tutorado);
        model.addAttribute("gruposTutorado", gruposTutorado);
        model.addAttribute("detecciones", detecciones);
        model.addAttribute("resumen", resumen);
        return "tutorado/viewInfoTutorado";
    }

    @GetMapping(value = "actualizar/{id}")
    public String obtenerVistaActualizarTutorado(@PathVariable Integer id, Model model) {
        Tutorado tutorado = this.tutoradoService.obtenerTutorado(id);
        log.info("Tutorado a actualizar: {}", tutorado);
        model.addAttribute("tutorado", tutorado);
        model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
        model.addAttribute("isEdit", true);
        return "tutorado/viewFormTutorado";
    }

    @PostMapping(value = "actualizar/{id}")
    public String actualizarTutorado(
            @PathVariable Integer id,
            @Valid Tutorado tutorado,
            BindingResult result,
            @RequestParam(value = "fotoFile", required = false) MultipartFile fotoFile,
            Model model,
            RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
            model.addAttribute("isEdit", true);
            return "tutorado/viewFormTutorado";
        }
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
            model.addAttribute("msg_error", "Error al actualizar el tutorado: " + e.getMessage());
            model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
            model.addAttribute("isEdit", true);
            return "tutorado/viewFormTutorado";
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