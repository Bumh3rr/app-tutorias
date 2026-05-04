package com.bumh3r.service;

import com.bumh3r.entity.Semestre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SemestreService {
    List<Semestre> obtenerTodosSemestres();
    void guardarSemestre(Semestre semestre);
    void actualizarSemestre(Integer id, Semestre semestre);
    Semestre obtenerSemestre(Integer id);
    void eliminarSemestre(Integer id);

    Page<Semestre> obtenerTodosSemestresPage(Pageable pageable);
}