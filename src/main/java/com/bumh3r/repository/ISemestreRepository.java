package com.bumh3r.repository;

import com.bumh3r.entity.Semestre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ISemestreRepository extends JpaRepository<Semestre, Integer> {

    Optional<Semestre> findFirstByActivoOrderByIdDesc(Integer activo);
    List<Semestre> findByActivo(Integer activo);
    Page<Semestre> findByActivo(Integer activo, Pageable pageable);

    @Query("SELECT s FROM Semestre s WHERE (:q IS NULL OR :q = '' OR LOWER(s.periodo) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Semestre> searchByName(@Param("q") String q, Pageable pageable);

    boolean existsByPeriodoAndAnioAndActivo(String periodo, Integer anio, Integer activo);

    @Query("SELECT COUNT(s) > 0 FROM Semestre s WHERE s.periodo = :periodo AND s.anio = :anio AND s.activo = 1 AND s.id <> :id")
    boolean existsByPeriodoAndAnioAndActivoExcludingId(@Param("periodo") String periodo, @Param("anio") Integer anio, @Param("id") Integer id);

    // ── Inactive-aware validation (no activo filter) ──────────────────────────
    Optional<Semestre> findByPeriodoAndAnio(String periodo, Integer anio);

    @Query("SELECT s FROM Semestre s WHERE s.periodo = :periodo AND s.anio = :anio AND s.id <> :id")
    Optional<Semestre> findByPeriodoAndAnioAndIdNot(@Param("periodo") String periodo, @Param("anio") Integer anio, @Param("id") Integer id);
}