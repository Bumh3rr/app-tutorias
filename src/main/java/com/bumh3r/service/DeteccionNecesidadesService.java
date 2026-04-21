package com.bumh3r.service;

import com.bumh3r.entity.DeteccionNecesidades;
import java.util.List;

public interface DeteccionNecesidadesService {
    List<DeteccionNecesidades> obtenerTodasDetecciones();
    void guardarDeteccion(DeteccionNecesidades deteccion);
    void actualizarDeteccion(Integer id, DeteccionNecesidades deteccion);
    DeteccionNecesidades obtenerDeteccion(Integer id);
    void eliminarDeteccion(Integer id);

    // Búsquedas
    List<DeteccionNecesidades> buscarPorTutorado(Integer idTutorado);
    List<DeteccionNecesidades> buscarPorSesion(Integer idSesion);
    List<DeteccionNecesidades> buscarPorNecesidadAlgebra();
    List<DeteccionNecesidades> buscarPorNecesidadCalculo();
    List<DeteccionNecesidades> buscarPorNecesidadEconomica();
    List<DeteccionNecesidades> buscarPorNecesidadPsicologica();
}