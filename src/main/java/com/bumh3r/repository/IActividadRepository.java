package com.bumh3r.repository;

import com.bumh3r.entity.Actividad;
import com.bumh3r.entity.PAT;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IActividadRepository extends JpaRepository<Actividad, Integer> {

    List<Actividad> findByActivo(Integer activo);
    Page<Actividad> findByActivo(Integer activo, Pageable pageable);

    // Búsqueda: actividades por fecha
    List<Actividad> findByActivoAndFecha(Integer activo, LocalDate fecha);
    Page<Actividad> findByActivoAndFecha(Integer activo, LocalDate fecha, Pageable pageable);

    // Búsqueda: actividades por rango de fechas
    List<Actividad> findByActivoAndFechaBetween(Integer activo, LocalDate fechaInicio, LocalDate fechaFin);
    Page<Actividad> findByActivoAndFechaBetween(Integer activo, LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable);


    // Actividades de un PAT específico
    List<Actividad> findByActivoAndPat(Integer activo, PAT pat);
    Page<Actividad> findByActivoAndPat(Integer activo, PAT pat, Pageable pageable);

    // Actividades por semana
    List<Actividad> findByActivoAndSemana(Integer activo, Integer semana);

    boolean existsByNombreAndSemanaAndActivo(String nombre, Integer semana, Integer activo);

    @Query("SELECT COUNT(a) > 0 FROM Actividad a WHERE a.nombre = :nombre AND a.semana = :semana AND a.activo = 1 AND a.id <> :id")
    boolean existsByNombreAndSemanaAndActivoExcludingId(@Param("nombre") String nombre, @Param("semana") Integer semana, @Param("id") Integer id);

    // Validación: semana duplicada dentro del mismo PAT
    boolean existsByPatAndSemanaAndActivo(PAT pat, Integer semana, Integer activo);

    @Query("SELECT COUNT(a) > 0 FROM Actividad a WHERE a.pat = :pat AND a.semana = :semana AND a.activo = 1 AND a.id <> :id")
    boolean existsByPatAndSemanaAndActivoExcludingId(@Param("pat") PAT pat, @Param("semana") Integer semana, @Param("id") Integer id);

    @Query("SELECT a FROM Actividad a WHERE a.activo = 1 AND (:q IS NULL OR :q = '' OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Actividad> searchByName(@Param("q") String q, Pageable pageable);
}