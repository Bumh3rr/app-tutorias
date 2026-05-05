package com.bumh3r.controller;

import com.bumh3r.entity.Actividad;
import com.bumh3r.entity.PAT;
import com.bumh3r.service.ActividadService;
import com.bumh3r.service.FileStoreService;
import com.bumh3r.service.PATService;
import com.bumh3r.service.enums.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "actividad")
public class ActividadController {

    @Autowired
    private ActividadService actividadService;
    @Autowired
    private PATService patService;
    @Autowired
    private FileStoreService fileStoreService;

    private final Logger log = LoggerFactory.getLogger(ActividadController.class);

    @GetMapping()
    public String obtenerVistaListaActividades(
            @RequestParam(value = "fecha", required = false) String fecha,
            @RequestParam(value = "fechaInicio", required = false) String fechaInicio,
            @RequestParam(value = "fechaFin", required = false) String fechaFin,
            @RequestParam(value = "idPat", required = false) Integer idPat,
            @RequestParam(value = "tipoBusqueda", required = false, defaultValue = "todos") String tipoBusqueda,
            @RequestParam(value = "q", required = false, defaultValue = "") String q,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sort", defaultValue = "desc") String sort,
            Model model) {

        Page<Actividad> actividades;
        List<PAT> pats = this.patService.obtenerTodosPAT();

        try {
            if ("nombre".equals(tipoBusqueda) && !q.isBlank()) {
                actividades = this.actividadService.buscarActividadesPorNombrePaginado(q, page, pageSize, sortBy, sort);
                model.addAttribute("filtro", "Nombre: " + q);
            } else if ("fecha".equals(tipoBusqueda) && fecha != null && !fecha.isEmpty()) {
                LocalDate fechaDate = LocalDate.parse(fecha);
                actividades = this.actividadService.buscarActividadesPorFechaPaginado(fechaDate, page, pageSize, sortBy, sort);
                model.addAttribute("filtro", "Fecha: " + fecha);

            } else if ("rango".equals(tipoBusqueda) && fechaInicio != null && fechaFin != null
                    && !fechaInicio.isEmpty() && !fechaFin.isEmpty()) {
                LocalDate fInicio = LocalDate.parse(fechaInicio);
                LocalDate fFin = LocalDate.parse(fechaFin);
                actividades = this.actividadService.buscarActividadesPorRangoFechasPaginado(fInicio, fFin, page, pageSize, sortBy, sort);
                model.addAttribute("filtro", "Rango: " + fechaInicio + " a " + fechaFin);

            } else if ("pat".equals(tipoBusqueda) && idPat != null) {
                actividades = this.actividadService.buscarActividadesPorPATPaginado(idPat, page, pageSize, sortBy, sort);
                model.addAttribute("filtro", "Por PAT seleccionado");

            } else {
                actividades = this.actividadService.obtenerTodasActividadesPaginado(page, pageSize, sortBy, sort);
                model.addAttribute("filtro", null);
            }
        } catch (Exception e) {
            actividades = this.actividadService.obtenerTodasActividadesPaginado(0, pageSize, "id", "desc");
            model.addAttribute("msg_error", "Error en la búsqueda: " + e.getMessage());
        }

        HashMap<String, String> mapSort = new LinkedHashMap<>();
        mapSort.put("id", "ID");
        mapSort.put("nombre", "Nombre");
        mapSort.put("semana", "Semana");
        mapSort.put("fecha", "Fecha");

        model.addAttribute("actividades", actividades.getContent());
        model.addAttribute("paginaActual", actividades.getNumber());
        model.addAttribute("totalElementos", actividades.getTotalElements());
        model.addAttribute("totalPaginas", actividades.getTotalPages());
        model.addAttribute("pageSize", pageSize);

        model.addAttribute("pats", pats);
        model.addAttribute("idPatSeleccionado", idPat);
        model.addAttribute("fechaSeleccionada", fecha);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("tipoBusqueda", tipoBusqueda);
        model.addAttribute("q", q);

        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sort", sort);
        model.addAttribute("mapSort", mapSort);

        return "actividad/viewListaActividad";
    }

    @GetMapping(value = "agregar")
    public String obtenerVistaAgregarActividad(
            @RequestParam(value = "idPat", required = false) Integer idPat,
            Model model) {
        model.addAttribute("pats", this.patService.obtenerTodosPAT());
        model.addAttribute("preselectedPatId", idPat);
        return "actividad/viewAgregarActividades";
    }

    @PostMapping(value = "guardar")
    public String guardarActividad(
            @Valid Actividad actividad,
            BindingResult result,
            @RequestParam(value = "fotoFile", required = false) MultipartFile fotoFile,
            Model model,
            RedirectAttributes attributes) {
        actividad.setFoto(actividad.getFoto() != null && !actividad.getFoto().isEmpty() ? actividad.getFoto() : null);
        if (result.hasErrors()) {
            model.addAttribute("pats", this.patService.obtenerTodosPAT());
            model.addAttribute("isEdit", false);
            return "actividad/viewFormActividad";
        }
        try {
            if (fotoFile != null && !fotoFile.isEmpty()) {
                String foto = this.fileStoreService.save(fotoFile, FileType.ACTIVIDAD);
                actividad.setFoto(foto);
            }
            log.info("Guardar actividad: {}", actividad);
            this.actividadService.guardarActividad(actividad);
            attributes.addFlashAttribute("msg_success", "Actividad guardada correctamente");
        } catch (Exception e) {
            model.addAttribute("msg_error", "Error al guardar la actividad: " + e.getMessage());
            model.addAttribute("pats", this.patService.obtenerTodosPAT());
            model.addAttribute("isEdit", false);
            return "actividad/viewFormActividad";
        }
        Integer idPat = actividad.getPat() != null ? actividad.getPat().getId() : null;
        return idPat != null ? "redirect:/actividad/agregar?idPat=" + idPat : "redirect:/actividad";
    }

    // ── REST API para el builder de actividades ────────────────────────────

    @GetMapping(value = "api/por-pat/{idPat}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> getActividadesPorPAT(@PathVariable Integer idPat) {
        try {
            List<Actividad> lista = this.actividadService.buscarActividadesPorPAT(idPat);
            List<Map<String, Object>> result = lista.stream()
                .sorted(Comparator.comparingInt(Actividad::getSemana))
                .map(a -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", a.getId());
                    m.put("nombre", a.getNombre());
                    m.put("descripcion", a.getDescripcion() != null ? a.getDescripcion() : "");
                    m.put("semana", a.getSemana());
                    return m;
                }).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = "api/guardar-lote", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> guardarLoteActividades(@RequestBody Map<String, Object> body) {
        try {
            Object idPatRaw = body.get("idPat");
            if (idPatRaw == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Se requiere seleccionar un PAT"));
            }
            Integer idPat = Integer.valueOf(idPatRaw.toString());

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("actividades");
            if (items == null || items.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No hay actividades para guardar"));
            }

            List<Actividad> actividades = new ArrayList<>();
            for (Map<String, Object> item : items) {
                Actividad a = new Actividad();
                a.setNombre((String) item.get("nombre"));
                a.setDescripcion(item.get("descripcion") != null ? (String) item.get("descripcion") : null);
                a.setSemana(Integer.valueOf(item.get("semana").toString()));
                actividades.add(a);
            }

            List<String> errores = this.actividadService.guardarLoteActividades(idPat, actividades);

            int guardadas = actividades.size() - errores.size();
            if (!errores.isEmpty()) {
                return ResponseEntity.status(207).body(Map.of(
                    "guardadas", guardadas,
                    "errores", errores,
                    "parcial", true
                ));
            }
            return ResponseEntity.ok(Map.of("guardadas", guardadas, "parcial", false));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping(value = "api/actualizar/{id}", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> actualizarActividadAjax(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body) {
        try {
            Actividad existing = this.actividadService.obtenerActividad(id);
            if (existing == null) return ResponseEntity.notFound().build();

            Actividad updated = new Actividad();
            updated.setNombre((String) body.get("nombre"));
            updated.setDescripcion(body.get("descripcion") != null ? (String) body.get("descripcion") : null);
            updated.setSemana(Integer.valueOf(body.get("semana").toString()));
            updated.setPat(existing.getPat());
            updated.setFoto(existing.getFoto());
            updated.setActivo(1);

            this.actividadService.actualizarActividad(id, updated);
            return ResponseEntity.ok(Map.of("message", "Actualizada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping(value = "api/eliminar/{id}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> eliminarActividadAjax(@PathVariable Integer id) {
        try {
            this.actividadService.eliminarActividad(id);
            return ResponseEntity.ok(Map.of("message", "Eliminada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping(value = "ver/{id}")
    public String obtenerVistaVerActividad(@PathVariable Integer id, Model model) {
        Actividad actividad = this.actividadService.obtenerActividad(id);
        log.info("Actividad: {}", actividad);
        model.addAttribute("actividad", actividad);
        return "actividad/viewInfoActividad";
    }

    @GetMapping(value = "actualizar/{id}")
    public String obtenerVistaActualizarActividad(@PathVariable Integer id, Model model) {
        Actividad actividad = this.actividadService.obtenerActividad(id);
        log.info("Actividad a actualizar: {}", actividad);
        model.addAttribute("actividad", actividad);
        model.addAttribute("pats", this.patService.obtenerTodosPAT());
        model.addAttribute("isEdit", true);
        return "actividad/viewFormActividad";
    }

    @PostMapping(value = "actualizar/{id}")
    public String actualizarActividad(
            @PathVariable Integer id,
            @Valid Actividad actividad,
            BindingResult result,
            @RequestParam(value = "fotoFile", required = false) MultipartFile fotoFile,
            Model model,
            RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("pats", this.patService.obtenerTodosPAT());
            model.addAttribute("isEdit", true);
            return "actividad/viewFormActividad";
        }
        try {
            if (fotoFile != null && !fotoFile.isEmpty()) {
                this.fileStoreService.delete(actividad.getFoto(), FileType.ACTIVIDAD);
                String foto = this.fileStoreService.save(fotoFile, FileType.ACTIVIDAD);
                actividad.setFoto(foto);
            }
            log.info("Actualizar actividad {}: {}", id, actividad);
            this.actividadService.actualizarActividad(id, actividad);
            attributes.addFlashAttribute("msg_success", "Actividad actualizada correctamente");
        } catch (Exception e) {
            model.addAttribute("msg_error", "Error al actualizar la actividad: " + e.getMessage());
            model.addAttribute("pats", this.patService.obtenerTodosPAT());
            model.addAttribute("isEdit", true);
            return "actividad/viewFormActividad";
        }
        return "redirect:/actividad";
    }

    @GetMapping(value = "delete/{id}")
    public String obtenerVistaConfirmarEliminarActividad(@PathVariable Integer id, Model model) {
        Actividad actividad = this.actividadService.obtenerActividad(id);
        model.addAttribute("actividad", actividad);
        return "actividad/viewConfirmDeleteActividad";
    }

    @PostMapping(value = "confirm/delete/{id}")
    public String eliminarActividad(@PathVariable Integer id, RedirectAttributes attributes) {
        try {
            this.actividadService.eliminarActividad(id);
            attributes.addFlashAttribute("msg_success", "Actividad eliminada correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg_error", "Error al eliminar la actividad: " + e.getMessage());
        }
        return "redirect:/actividad";
    }

}
