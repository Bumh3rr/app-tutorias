package com.bumh3r.service;

import com.bumh3r.entity.Carrera;
import java.util.List;

public interface CarreraService {
    List<Carrera> obtenerTodasCarreras();
    void guardarCarrera(Carrera carrera);
    void actualizarCarrera(Integer id, Carrera carrera);
    Carrera obtenerCarrera(Integer id);
    void eliminarCarrera(Integer id);
}