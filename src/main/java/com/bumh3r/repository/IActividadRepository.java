package com.bumh3r.repository;

import com.bumh3r.entity.Actividad;
import com.bumh3r.entity.PAT;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface IActividadRepository extends JpaRepository<Actividad, Integer> {

    List<Actividad> findByActivo(Integer activo);
    Page<Actividad> findByActivo(Integer activo, Pageable pageable);

    // Búsqueda: actividades por fecha
    List<Actividad> findByActivoAndFecha(Integer activo, Date fecha);
    Page<Actividad> findByActivoAndFecha(Integer activo, Date fecha, Pageable pageable);

    // Búsqueda: actividades por rango de fechas
    List<Actividad> findByActivoAndFechaBetween(Integer activo, Date fechaInicio, Date fechaFin);
    Page<Actividad> findByActivoAndFechaBetween(Integer activo, Date fechaInicio, Date fechaFin, Pageable pageable);


    // Actividades de un PAT específico
    List<Actividad> findByActivoAndPat(Integer activo, PAT pat);
    Page<Actividad> findByActivoAndPat(Integer activo, PAT pat, Pageable pageable);

    // Actividades por semana
    List<Actividad> findByActivoAndSemana(Integer activo, Integer semana);
}