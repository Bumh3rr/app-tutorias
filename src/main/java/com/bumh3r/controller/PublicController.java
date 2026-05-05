package com.bumh3r.controller;

import com.bumh3r.entity.Actividad;
import com.bumh3r.service.ActividadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping(value = "public")
public class PublicController {

    @Autowired
    private ActividadService actividadService;

    @GetMapping(value = "actividades")
    public String obtenerVistaActividadesPublicas(
            @RequestParam(value = "q", required = false, defaultValue = "") String q,
            @RequestParam(value = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(value = "fechaHasta", required = false) String fechaHasta,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "12") int pageSize,
            Model model) {

        Page<Actividad> actividades;
        String filtro = null;
        try {
            if (!q.isBlank()) {
                actividades = this.actividadService.buscarActividadesPorNombrePaginado(q, page, pageSize, "semana", "asc");
                filtro = "Nombre: " + q;
            } else if (fechaDesde != null && !fechaDesde.isBlank() && fechaHasta != null && !fechaHasta.isBlank()) {
                LocalDate dDesde = LocalDate.parse(fechaDesde);
                LocalDate dHasta = LocalDate.parse(fechaHasta);
                actividades = this.actividadService.buscarActividadesPorRangoFechasPaginado(dDesde, dHasta, page, pageSize, "semana", "asc");
                filtro = "Rango: " + fechaDesde + " — " + fechaHasta;
            } else {
                actividades = this.actividadService.obtenerTodasActividadesPaginado(page, pageSize, "semana", "asc");
            }
        } catch (Exception e) {
            actividades = this.actividadService.obtenerTodasActividadesPaginado(0, pageSize, "semana", "asc");
        }

        model.addAttribute("actividades", actividades.getContent());
        model.addAttribute("paginaActual", actividades.getNumber());
        model.addAttribute("totalPaginas", actividades.getTotalPages());
        model.addAttribute("totalElementos", actividades.getTotalElements());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("q", q);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("filtro", filtro);

        return "public/viewPublicActividades";
    }

    @GetMapping(value = "actividades/{id}")
    public String obtenerVistaDetalleActividadPublica(@PathVariable Integer id, Model model) {
        Actividad actividad = this.actividadService.obtenerActividad(id);
        model.addAttribute("actividad", actividad);
        return "public/viewPublicDetalleActividad";
    }
}
