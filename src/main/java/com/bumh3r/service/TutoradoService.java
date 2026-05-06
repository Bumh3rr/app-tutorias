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
    Page<Tutorado> buscarPorNombre(String q, int page, int pageSize, String sortBy, String sort);
    Page<Tutorado> buscarPorNumeroControl(String q, int page, int pageSize, String sortBy, String sort);
    Page<Tutorado> buscarPorEmail(String q, int page, int pageSize, String sortBy, String sort);
    Page<Tutorado> buscarPorCarrera(Integer idCarrera, int page, int pageSize, String sortBy, String sort);
    Page<Tutorado> buscarPorFechaRegistro(java.util.Date inicio, java.util.Date fin, int page, int pageSize, String sortBy, String sort);
}