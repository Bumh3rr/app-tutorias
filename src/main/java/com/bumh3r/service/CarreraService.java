package com.bumh3r.service;

import com.bumh3r.entity.Carrera;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CarreraService {
    List<Carrera> obtenerTodasCarreras();
    void guardarCarrera(Carrera carrera);
    void actualizarCarrera(Integer id, Carrera carrera);
    Carrera obtenerCarrera(Integer id);
    void eliminarCarrera(Integer id);

    Page<Carrera> obtenerTodasCarrerasPage(Pageable pageable);
}