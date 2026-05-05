package com.bumh3r.service;

import com.bumh3r.entity.Actividad;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface ActividadService {
    List<Actividad> obtenerTodasActividades();
    void guardarActividad(Actividad actividad);
    void actualizarActividad(Integer id, Actividad actividad);
    Actividad obtenerActividad(Integer id);
    void eliminarActividad(Integer id);

    List<Actividad> buscarActividadesPorFecha(LocalDate fecha);

    List<Actividad> buscarActividadesPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin);

    List<Actividad> buscarActividadesPorPAT(Integer idPat);

    List<String> guardarLoteActividades(Integer idPat, List<Actividad> actividades);

    Page<Actividad> buscarActividadesPorNombrePaginado(String q, Integer page, Integer pageSize, String sortBy, String sort);
    Page<Actividad> buscarActividadesPorFechaPaginado(LocalDate fechaDate, Integer page, Integer pageSize, String sortBy, String sort);
    Page<Actividad> buscarActividadesPorRangoFechasPaginado(LocalDate fInicio, LocalDate fFin, Integer page, Integer pageSize, String sortBy, String sort);
    Page<Actividad> buscarActividadesPorPATPaginado(Integer idPat, Integer page, Integer pageSize, String sortBy, String sort);
    Page<Actividad> obtenerTodasActividadesPaginado(Integer page, Integer pageSize, String sortBy, String sort);
}
