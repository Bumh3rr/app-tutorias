package com.bumh3r.repository;

import com.bumh3r.entity.Actividad;
import com.bumh3r.entity.PAT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface IActividadRepository extends JpaRepository<Actividad, Integer> {

    List<Actividad> findByActivo(Integer activo);

    // Búsqueda: actividades por fecha
    List<Actividad> findByActivoAndFecha(Integer activo, Date fecha);

    // Búsqueda: actividades por rango de fechas
    List<Actividad> findByActivoAndFechaBetween(Integer activo, Date fechaInicio, Date fechaFin);

    // Actividades de un PAT específico
    List<Actividad> findByActivoAndPat(Integer activo, PAT pat);

    // Actividades por semana
    List<Actividad> findByActivoAndSemana(Integer activo, Integer semana);
}