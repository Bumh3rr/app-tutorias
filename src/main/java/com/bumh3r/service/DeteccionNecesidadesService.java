package com.bumh3r.service;

import com.bumh3r.entity.DeteccionNecesidades;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface DeteccionNecesidadesService {
    List<DeteccionNecesidades> obtenerTodasDetecciones();
    Page<DeteccionNecesidades> obtenerTodasDeteccionesPage(Pageable pageable);
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

    List<DeteccionNecesidades> buscarPorFechaRegistro(java.util.Date inicio, java.util.Date fin);
}
