package com.bumh3r.repository;

import com.bumh3r.entity.Carrera;
import com.bumh3r.entity.CoordinadorCarrera;
import com.bumh3r.entity.Semestre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICoordinadorCarreraRepository extends JpaRepository<CoordinadorCarrera, Integer> {

    List<CoordinadorCarrera> findByActivo(Integer activo);

    Page<CoordinadorCarrera> findByActivo(Integer activo, Pageable pageable);

    // Coordinadores por carrera
    List<CoordinadorCarrera> findByActivoAndCarrera(Integer activo, Carrera carrera);

    // Coordinadores por semestre
    List<CoordinadorCarrera> findByActivoAndSemestre(Integer activo, Semestre semestre);

    // Coordinadores por carrera y semestre
    List<CoordinadorCarrera> findByActivoAndCarreraAndSemestre(Integer activo, Carrera carrera, Semestre semestre);

    @Query("SELECT c FROM CoordinadorCarrera c WHERE c.activo = 1 AND (:q IS NULL OR :q = '' OR LOWER(CONCAT(c.nombre, ' ', c.apellido)) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<CoordinadorCarrera> searchByName(@Param("q") String q, Pageable pageable);
}