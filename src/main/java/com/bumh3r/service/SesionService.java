package com.bumh3r.service;

import com.bumh3r.entity.Sesion;
import java.util.List;

public interface SesionService {
    List<Sesion> obtenerTodasSesiones();
    void guardarSesion(Sesion sesion);
    void actualizarSesion(Integer id, Sesion sesion);
    Sesion obtenerSesion(Integer id);
    void eliminarSesion(Integer id);

    // Búsquedas
    List<Sesion> buscarSesionesPorTutor(Integer idTutor);
    List<Sesion> buscarSesionesPorSemana(Integer semana);
    List<Sesion> buscarSesionesPorTutorYSemana(Integer idTutor, Integer semana);
    List<Sesion> buscarSesionesPorEstatus(String estatus);
}