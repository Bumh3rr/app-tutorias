package com.bumh3r.service;

import com.bumh3r.entity.Tutor;
import java.util.List;

public interface TutorService {
    List<Tutor> obtenerTodosTutores();
    void guardarTutor(Tutor tutor);
    void actualizarTutor(Integer id, Tutor tutor);
    Tutor obtenerTutor(Integer id);
    void eliminarTutor(Integer id);

    // Búsqueda: tutores por semestre
    List<Tutor> buscarTutoresPorSemestre(Integer idSemestre);

    // Búsqueda: tutores por semestre y carrera
    List<Tutor> buscarTutoresPorSemestreYCarrera(Integer idSemestre, Integer idCarrera);
}