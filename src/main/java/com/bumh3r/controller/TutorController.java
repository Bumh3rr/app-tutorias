package com.bumh3r.controller;

import com.bumh3r.entity.Grupo;
import com.bumh3r.entity.Tutor;
import com.bumh3r.service.FileStoreService;
import com.bumh3r.service.GrupoService;
import com.bumh3r.service.GrupoTutoradoService;
import com.bumh3r.service.SesionService;
import com.bumh3r.service.TutorService;
import com.bumh3r.service.enums.FileType;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@Controller
@RequestMapping(value = "tutor")
public class TutorController {

    @Autowired
    private TutorService tutorService;
    @Autowired
    private GrupoService grupoService;
    @Autowired
    private GrupoTutoradoService grupoTutoradoService;
    @Autowired
    private SesionService sesionService;
    @Autowired
    private FileStoreService fileStoreService;

    private final Logger log = LoggerFactory.getLogger(TutorController.class);

    @GetMapping()
    public String obtenerVistaListaTutores(
            @RequestParam(value = "tipoBusqueda", required = false, defaultValue = "todos") String tipoBusqueda,
            @RequestParam(value = "q", required = false, defaultValue = "") String q,
            @RequestParam(value = "fechaInicio", required = false, defaultValue = "") String fechaInicio,
            @RequestParam(value = "fechaFin", required = false, defaultValue = "") String fechaFin,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sort", defaultValue = "desc") String sort,
            Model model) {

        Page<Tutor> paginaTutores;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            switch (tipoBusqueda) {
                case "nombre" -> {
                    if (!q.isBlank()) {
                        paginaTutores = this.tutorService.buscarPorNombre(q, page, pageSize, sortBy, sort);
                        model.addAttribute("filtro", "Nombre: " + q);
                    } else {
                        paginaTutores = this.tutorService.obtenerTodosTutoresPaginado(page, pageSize, sortBy, sort);
                        model.addAttribute("filtro", null);
                    }
                }
                case "numeroControl" -> {
                    if (!q.isBlank()) {
                        paginaTutores = this.tutorService.buscarPorNumeroControl(q, page, pageSize, sortBy, sort);
                        model.addAttribute("filtro", "No. Control: " + q);
                    } else {
                        paginaTutores = this.tutorService.obtenerTodosTutoresPaginado(page, pageSize, sortBy, sort);
                        model.addAttribute("filtro", null);
                    }
                }
                case "correo" -> {
                    if (!q.isBlank()) {
                        paginaTutores = this.tutorService.buscarPorEmail(q, page, pageSize, sortBy, sort);
                        model.addAttribute("filtro", "Correo: " + q);
                    } else {
                        paginaTutores = this.tutorService.obtenerTodosTutoresPaginado(page, pageSize, sortBy, sort);
                        model.addAttribute("filtro", null);
                    }
                }
                case "fecha" -> {
                    if (!fechaInicio.isBlank() && !fechaFin.isBlank()) {
                        Date inicio = sdf.parse(fechaInicio);
                        Date fin = new Date(sdf.parse(fechaFin).getTime() + 86399999L);
                        paginaTutores = this.tutorService.buscarPorFechaRegistro(inicio, fin, page, pageSize, sortBy, sort);
                        model.addAttribute("filtro", "Fecha: " + fechaInicio + " – " + fechaFin);
                    } else {
                        paginaTutores = this.tutorService.obtenerTodosTutoresPaginado(page, pageSize, sortBy, sort);
                        model.addAttribute("filtro", null);
                    }
                }
                default -> {
                    paginaTutores = this.tutorService.obtenerTodosTutoresPaginado(page, pageSize, sortBy, sort);
                    model.addAttribute("filtro", null);
                }
            }
        } catch (Exception e) {
            paginaTutores = this.tutorService.obtenerTodosTutoresPaginado(0, pageSize, "id", "desc");
            model.addAttribute("msg_error", "Error en la búsqueda: " + e.getMessage());
        }

        HashMap<String, String> mapSort = new LinkedHashMap<>();
        mapSort.put("id", "ID");
        mapSort.put("nombre", "Nombre");
        mapSort.put("numeroControl", "No. Control");

        model.addAttribute("paginaActual", paginaTutores.getNumber());
        model.addAttribute("tutores", paginaTutores.getContent());
        model.addAttribute("totalElementos", paginaTutores.getTotalElements());
        model.addAttribute("totalPaginas", paginaTutores.getTotalPages());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sort", sort);
        model.addAttribute("mapSort", mapSort);
        model.addAttribute("tipoBusqueda", tipoBusqueda);
        model.addAttribute("q", q);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);

        return "tutor/viewListaTutor";
    }

    @GetMapping(value = "agregar")
    public String obtenerVistaAgregarTutor(Model model) {
        model.addAttribute("tutor", new Tutor());
        model.addAttribute("isEdit", false);
        return "tutor/viewFormTutor";
    }

    @PostMapping(value = "guardar")
    public String guardarTutor(
            @Valid Tutor tutor,
            BindingResult result,
            @RequestParam(value = "fotoFile", required = false) MultipartFile fotoFile,
            Model model,
            RedirectAttributes attributes) {
            tutor.setFoto(tutor.getFoto() != null && !tutor.getFoto().isEmpty() ? tutor.getFoto() : null);
        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "tutor/viewFormTutor";
        }
        try {
            if (fotoFile != null && !fotoFile.isEmpty()) {
                String foto = this.fileStoreService.save(fotoFile, FileType.TUTOR);
                tutor.setFoto(foto);
            }
            log.info("Guardar tutor: {}", tutor);
            this.tutorService.guardarTutor(tutor);
            attributes.addFlashAttribute("msg_success", "Tutor guardado correctamente");
        } catch (Exception e) {
            model.addAttribute("msg_error", "Error al guardar el tutor: " + e.getMessage());
            model.addAttribute("isEdit", false);
            return "tutor/viewFormTutor";
        }
        return "redirect:/tutor";
    }

    @GetMapping(value = "ver/{id}")
    public String obtenerVistaVerTutor(@PathVariable Integer id, Model model) {
        Tutor tutor = this.tutorService.obtenerTutor(id);
        List<Grupo> grupos = this.grupoService.buscarPorTutor(id);
        java.util.Map<Integer, Long> alumnosPorGrupo = this.grupoTutoradoService.contarAlumnosPorGrupo();
        java.util.Map<Integer, Long> sesionesPorGrupo = new java.util.HashMap<>();
        for (Grupo g : grupos) {
            long count = this.sesionService.buscarSesionesPorGrupo(g.getId()).size();
            sesionesPorGrupo.put(g.getId(), count);
        }
        long totalAlumnos = grupos.stream().mapToLong(g -> alumnosPorGrupo.getOrDefault(g.getId(), 0L)).sum();
        long totalSesiones = sesionesPorGrupo.values().stream().mapToLong(Long::longValue).sum();
        log.info("Tutor: {}", tutor);
        model.addAttribute("tutor", tutor);
        model.addAttribute("grupos", grupos);
        model.addAttribute("alumnosPorGrupo", alumnosPorGrupo);
        model.addAttribute("sesionesPorGrupo", sesionesPorGrupo);
        model.addAttribute("totalAlumnos", totalAlumnos);
        model.addAttribute("totalSesiones", totalSesiones);
        return "tutor/viewInfoTutor";
    }

    @GetMapping(value = "actualizar/{id}")
    public String obtenerVistaActualizarTutor(@PathVariable Integer id, Model model) {
        Tutor tutor = this.tutorService.obtenerTutor(id);
        log.info("Tutor a actualizar: {}", tutor);
        model.addAttribute("tutor", tutor);
        model.addAttribute("isEdit", true);
        return "tutor/viewFormTutor";
    }

    @PostMapping(value = "actualizar/{id}")
    public String actualizarTutor(
            @PathVariable Integer id,
            @Valid Tutor tutor,
            BindingResult result,
            @RequestParam(value = "fotoFile", required = false) MultipartFile fotoFile,
            Model model,
            RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "tutor/viewFormTutor";
        }
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
            model.addAttribute("msg_error", "Error al actualizar el tutor: " + e.getMessage());
            model.addAttribute("isEdit", true);
            return "tutor/viewFormTutor";
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

    @GetMapping(value = "pdf/constancia/{id}")
    public void generarConstanciaTutor(@PathVariable Integer id, HttpServletResponse response) throws Exception {
        Tutor tutor = this.tutorService.obtenerTutor(id);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=constancia-tutor-" + id + ".pdf");
        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();
        document.add(new Paragraph("Tecnológico Nacional de México — Campus Chilpancingo"));
        document.add(new Paragraph("Constancia de Participación como Tutor"));
        document.add(new Paragraph("Tutor: " + tutor.getNombre() + " " + tutor.getApellido()));
        document.add(new Paragraph("En desarrollo."));
        document.close();
    }
}
