package com.bumh3r.service;

import com.bumh3r.entity.Sesion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SesionService {
    List<Sesion> obtenerTodasSesiones();
    void guardarSesion(Sesion sesion);
    void actualizarSesion(Integer id, Sesion sesion);
    Sesion obtenerSesion(Integer id);
    void eliminarSesion(Integer id);

    List<Sesion> buscarSesionesPorGrupo(Integer idGrupo);
    List<Sesion> buscarSesionesPorSemana(Integer semana);
    List<Sesion> buscarSesionesPorGrupoYSemana(Integer idGrupo, Integer semana);
    List<Sesion> buscarSesionesPorEstatus(String estatus);

    Page<Sesion> obtenerTodasSesionesPage(Pageable pageable);
    Page<Sesion> buscarSesionesPorGrupoPage(Integer idGrupo, Pageable pageable);
    Page<Sesion> buscarSesionesPorSemanaPage(Integer semana, Pageable pageable);
    Page<Sesion> buscarSesionesPorGrupoYSemanaPage(Integer idGrupo, Integer semana, Pageable pageable);
    Page<Sesion> buscarSesionesPorEstatusPage(String estatus, Pageable pageable);

    Page<Sesion> buscarSesionesPorFechaRegistroPage(java.util.Date inicio, java.util.Date fin, Pageable pageable);
}
