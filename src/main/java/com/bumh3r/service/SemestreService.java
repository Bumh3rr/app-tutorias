package com.bumh3r.service;

import com.bumh3r.entity.Semestre;
import java.util.List;

public interface SemestreService {
    List<Semestre> obtenerTodosSemestres();
    void guardarSemestre(Semestre semestre);
    void actualizarSemestre(Integer id, Semestre semestre);
    Semestre obtenerSemestre(Integer id);
    void eliminarSemestre(Integer id);
}