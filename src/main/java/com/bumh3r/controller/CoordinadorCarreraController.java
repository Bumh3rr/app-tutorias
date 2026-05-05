package com.bumh3r.controller;

import com.bumh3r.entity.CoordinadorCarrera;
import com.bumh3r.service.CarreraService;
import com.bumh3r.service.CoordinadorCarreraService;
import com.bumh3r.service.FileStoreService;
import com.bumh3r.service.SemestreService;
import com.bumh3r.service.enums.FileType;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.validation.BindingResult;
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
            @RequestParam(value = "q", required = false, defaultValue = "") String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "sort", defaultValue = "desc") String sort,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            Model model) {

        if (!"asc".equals(sort) && !"desc".equals(sort)) sort = "desc";
        if (!"nombre".equals(sortBy) && !"id".equals(sortBy)) sortBy = "nombre";

        Sort.Direction direction = "desc".equals(sort) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(direction, sortBy));

        Page<CoordinadorCarrera> pageResult;
        try {
            if ("nombre".equals(tipoBusqueda) && !q.isBlank()) {
                pageResult = this.coordinadorCarreraService.buscarPorNombrePage(q, pageable);
                model.addAttribute("filtro", "Nombre: " + q);
            } else if ("carrera".equals(tipoBusqueda) && idCarrera != null) {
                List<CoordinadorCarrera> lista = this.coordinadorCarreraService.buscarPorCarrera(idCarrera);
                pageResult = new PageImpl<>(paginate(lista, pageable), pageable, lista.size());
                model.addAttribute("filtro", "Carrera seleccionada");
            } else if ("semestre".equals(tipoBusqueda) && idSemestre != null) {
                List<CoordinadorCarrera> lista = this.coordinadorCarreraService.buscarPorSemestre(idSemestre);
                pageResult = new PageImpl<>(paginate(lista, pageable), pageable, lista.size());
                model.addAttribute("filtro", "Semestre seleccionado");
            } else if ("carreraSemestre".equals(tipoBusqueda) && idCarrera != null && idSemestre != null) {
                List<CoordinadorCarrera> lista = this.coordinadorCarreraService.buscarPorCarreraYSemestre(idCarrera, idSemestre);
                pageResult = new PageImpl<>(paginate(lista, pageable), pageable, lista.size());
                model.addAttribute("filtro", "Carrera y semestre seleccionados");
            } else {
                pageResult = this.coordinadorCarreraService.obtenerTodosCoordinadoresPage(pageable);
                model.addAttribute("filtro", null);
            }
        } catch (Exception e) {
            pageResult = this.coordinadorCarreraService.obtenerTodosCoordinadoresPage(pageable);
            model.addAttribute("msg_error", "Error en la búsqueda: " + e.getMessage());
        }

        model.addAttribute("coordinadores", pageResult.getContent());
        model.addAttribute("paginaActual", pageResult.getNumber());
        model.addAttribute("totalPaginas", pageResult.getTotalPages());
        model.addAttribute("totalElementos", pageResult.getTotalElements());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("sort", sort);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
        model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
        model.addAttribute("idCarreraSeleccionada", idCarrera);
        model.addAttribute("idSemestreSeleccionado", idSemestre);
        model.addAttribute("tipoBusqueda", tipoBusqueda);
        model.addAttribute("q", q);
        return "coordinador/viewListaCoordinador";
    }

    private <T> List<T> paginate(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), list.size());
        return start >= list.size() ? List.of() : list.subList(start, end);
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
            @Valid CoordinadorCarrera coordinador,
            BindingResult result,
            @RequestParam(value = "fotoFile", required = false) MultipartFile fotoFile,
            Model model,
            RedirectAttributes attributes) {
        coordinador.setFoto(coordinador.getFoto() != null && !coordinador.getFoto().isEmpty() ? coordinador.getFoto() : null);
        if (result.hasErrors()) {
            model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
            model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
            model.addAttribute("isEdit", false);
            return "coordinador/viewFormCoordinador";
        }
        try {
            if (fotoFile != null && !fotoFile.isEmpty()) {
                String foto = this.fileStoreService.save(fotoFile, FileType.COORDINADOR);
                coordinador.setFoto(foto);
            }
            log.info("Guardar coordinador: {}", coordinador);
            this.coordinadorCarreraService.guardarCoordinador(coordinador);
            attributes.addFlashAttribute("msg_success", "Coordinador guardado correctamente");
        } catch (Exception e) {
            model.addAttribute("msg_error", "Error al guardar el coordinador: " + e.getMessage());
            model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
            model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
            model.addAttribute("isEdit", false);
            return "coordinador/viewFormCoordinador";
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
            @Valid CoordinadorCarrera coordinador,
            BindingResult result,
            @RequestParam(value = "fotoFile", required = false) MultipartFile fotoFile,
            Model model,
            RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
            model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
            model.addAttribute("isEdit", true);
            return "coordinador/viewFormCoordinador";
        }
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
            model.addAttribute("msg_error", "Error al actualizar el coordinador: " + e.getMessage());
            model.addAttribute("carreras", this.carreraService.obtenerTodasCarreras());
            model.addAttribute("semestres", this.semestreService.obtenerTodosSemestres());
            model.addAttribute("isEdit", true);
            return "coordinador/viewFormCoordinador";
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

    @GetMapping(value = "pdf/nombramiento/{id}")
    public void generarNombramientoCoordinador(@PathVariable Integer id, HttpServletResponse response) throws Exception {
        CoordinadorCarrera coordinador = this.coordinadorCarreraService.obtenerCoordinador(id);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=nombramiento-coordinador-" + id + ".pdf");
        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();
        document.add(new Paragraph("Tecnológico Nacional de México — Campus Chilpancingo"));
        document.add(new Paragraph("Nombramiento Oficial de Coordinador de Carrera"));
        document.add(new Paragraph("Coordinador: " + coordinador.getNombre() + " " + coordinador.getApellido()));
        document.add(new Paragraph("En desarrollo."));
        document.close();
    }
}