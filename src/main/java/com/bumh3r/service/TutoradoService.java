package com.bumh3r.service;

import com.bumh3r.entity.Tutorado;
import java.util.List;

public interface TutoradoService {
    List<Tutorado> obtenerTodosTutorados();
    void guardarTutorado(Tutorado tutorado);
    void actualizarTutorado(Integer id, Tutorado tutorado);
    Tutorado obtenerTutorado(Integer id);
    void eliminarTutorado(Integer id);

    // Búsqueda: tutorados por carrera y semestre
    List<Tutorado> buscarTutoradosPorSemestreYCarrera(Integer idSemestre, Integer idCarrera);
}