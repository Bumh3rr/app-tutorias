package com.bumh3r.service;

import com.bumh3r.entity.CoordinadorCarrera;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CoordinadorCarreraService {
    List<CoordinadorCarrera> obtenerTodosCoordinadores();
    Page<CoordinadorCarrera> obtenerTodosCoordinadoresPage(Pageable pageable);
    void guardarCoordinador(CoordinadorCarrera coordinador);
    void actualizarCoordinador(Integer id, CoordinadorCarrera coordinador);
    CoordinadorCarrera obtenerCoordinador(Integer id);
    void eliminarCoordinador(Integer id);

    // Búsquedas
    List<CoordinadorCarrera> buscarPorCarrera(Integer idCarrera);
    List<CoordinadorCarrera> buscarPorSemestre(Integer idSemestre);
    List<CoordinadorCarrera> buscarPorCarreraYSemestre(Integer idCarrera, Integer idSemestre);
    Page<CoordinadorCarrera> buscarPorNombrePage(String q, Pageable pageable);
}