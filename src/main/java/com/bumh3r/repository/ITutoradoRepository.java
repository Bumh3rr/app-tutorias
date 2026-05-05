package com.bumh3r.repository;

import com.bumh3r.entity.Carrera;
import com.bumh3r.entity.Semestre;
import com.bumh3r.entity.Tutorado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ITutoradoRepository extends JpaRepository<Tutorado, Integer> {
    // Búsqueda de tutorados activos
    List<Tutorado> findByActivo(Integer activo);

    // Lista paginada de tutores activos
    Page<Tutorado> findByActivo(Integer activo, Pageable  pageable);

    // Búsqueda paginada: tutores por semestre y carrera
    Page<Tutorado> findByActivoAndSemestreAndCarrera(Integer activo, Semestre semestre, Carrera carrera, Pageable pageable);

    boolean existsByNumeroControlAndActivo(String numeroControl, Integer activo);
    boolean existsByNumeroControlAndActivoAndIdNot(String numeroControl, Integer activo, Integer id);

    boolean existsByEmailAndActivo(String email, Integer activo);
    boolean existsByEmailAndActivoAndIdNot(String email, Integer activo, Integer id);

    @Query("SELECT t FROM Tutorado t WHERE t.activo = 1 AND (:q IS NULL OR :q = '' OR LOWER(CONCAT(t.nombre, ' ', t.apellido)) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Tutorado> searchByName(@Param("q") String q, Pageable pageable);
}