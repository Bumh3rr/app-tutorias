package com.bumh3r.service;

import com.bumh3r.entity.Actividad;
import java.util.Date;
import java.util.List;

public interface ActividadService {
    List<Actividad> obtenerTodasActividades();
    void guardarActividad(Actividad actividad);
    void actualizarActividad(Integer id, Actividad actividad);
    Actividad obtenerActividad(Integer id);
    void eliminarActividad(Integer id);

    // Búsqueda: actividades por fecha exacta
    List<Actividad> buscarActividadesPorFecha(Date fecha);

    // Búsqueda: actividades por rango de fechas
    List<Actividad> buscarActividadesPorRangoFechas(Date fechaInicio, Date fechaFin);

    // Actividades de un PAT específico
    List<Actividad> buscarActividadesPorPAT(Integer idPat);
}