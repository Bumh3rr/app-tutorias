package com.bumh3r.repository;

import com.bumh3r.entity.Semestre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ISemestreRepository extends JpaRepository<Semestre, Integer> {
    List<Semestre> findByActivo(Integer activo);
    Page<Semestre> findByActivo(Integer activo, Pageable pageable);

    @Query("SELECT s FROM Semestre s WHERE (:q IS NULL OR :q = '' OR LOWER(s.periodo) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Semestre> searchByName(@Param("q") String q, Pageable pageable);

    boolean existsByPeriodoAndAnioAndActivo(String periodo, Integer anio, Integer activo);

    @Query("SELECT COUNT(s) > 0 FROM Semestre s WHERE s.periodo = :periodo AND s.anio = :anio AND s.activo = 1 AND s.id <> :id")
    boolean existsByPeriodoAndAnioAndActivoExcludingId(@Param("periodo") String periodo, @Param("anio") Integer anio, @Param("id") Integer id);
}