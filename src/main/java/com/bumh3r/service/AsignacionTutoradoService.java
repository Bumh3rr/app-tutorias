package com.bumh3r.service;

import com.bumh3r.entity.AsignacionTutorado;
import java.util.List;

public interface AsignacionTutoradoService {
    List<AsignacionTutorado> obtenerTodasAsignaciones();
    void guardarAsignacion(AsignacionTutorado asignacion);
    void actualizarAsignacion(Integer id, AsignacionTutorado asignacion);
    AsignacionTutorado obtenerAsignacion(Integer id);
    void eliminarAsignacion(Integer id);

    // Búsqueda: tutorías cursadas por un tutorado
    List<AsignacionTutorado> buscarTutoriasPorTutorado(Integer idTutorado);

    // Asignaciones de un tutor en un semestre
    List<AsignacionTutorado> buscarAsignacionesPorTutorYSemestre(Integer idTutor, Integer idSemestre);

    // Asignaciones por semestre
    List<AsignacionTutorado> buscarAsignacionesPorSemestre(Integer idSemestre);
}