package com.bumh3r.service;

import com.bumh3r.entity.Tutor;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TutorService {
    void guardarTutor(Tutor tutor);
    void actualizarTutor(Integer id, Tutor tutor);
    Tutor obtenerTutor(Integer id);
    void eliminarTutor(Integer id);

    Page<Tutor> obtenerTodosTutoresPaginado(Integer page, Integer pageSize, String sortBy, String sort);

    List<Tutor> obtenerTodosTutores();
}
