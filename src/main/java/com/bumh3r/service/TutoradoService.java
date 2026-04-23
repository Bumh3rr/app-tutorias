package com.bumh3r.service;

import com.bumh3r.entity.Tutorado;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TutoradoService {
    List<Tutorado> obtenerTodosTutorados();
    void guardarTutorado(Tutorado tutorado);
    void actualizarTutorado(Integer id, Tutorado tutorado);
    Tutorado obtenerTutorado(Integer id);
    void eliminarTutorado(Integer id);

    // Paginados
    Page<Tutorado> obtenerTodosTutoradosPaginado(int page, int pageSize, String sortBy, String sort);
    Page<Tutorado> buscarTutoradosPorSemestreYCarreraPaginado(Integer idSemestre, Integer idCarrera, int page, int pageSize, String sortBy, String sort);
}