package com.bumh3r.repository;

import com.bumh3r.entity.Carrera;
import com.bumh3r.entity.PAT;
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
public interface IPATRepository extends JpaRepository<PAT, Integer> {

    List<PAT> findByActivo(Integer activo);
    Page<PAT> findByActivo(Integer activo, Pageable pageable);

    // PAT general o por carrera
    List<PAT> findByActivoAndEsGeneral(Integer activo, Integer esGeneral);
    Page<PAT> findByActivoAndEsGeneral(Integer activo, Integer esGeneral, Pageable pageable);

    // PAT de una carrera y semestre específicos
    List<PAT> findByActivoAndCarreraAndSemestre(Integer activo, Carrera carrera, Semestre semestre);
    Page<PAT> findByActivoAndCarreraAndSemestre(Integer activo, Carrera carrera, Semestre semestre, Pageable pageable);

    boolean existsByNombreAndActivo(String nombre, Integer activo);
    boolean existsByNombreAndActivoAndIdNot(String nombre, Integer activo, Integer id);

    @Query("SELECT p FROM PAT p WHERE (:q IS NULL OR :q = '' OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<PAT> searchByName(@Param("q") String q, Pageable pageable);

    @Query("SELECT e FROM PAT e WHERE e.activo = 1 AND e.fechaRegistro BETWEEN :inicio AND :fin")
    Page<PAT> findByFechaRegistroRange(@Param("inicio") java.util.Date inicio, @Param("fin") java.util.Date fin, Pageable pageable);

    // ── Inactive-aware validation (no activo filter) ──────────────────────────
    // PAT con esGeneral=1 es único por (nombre, semestre).
    // PAT con esGeneral=0 es único por (nombre, semestre, carrera).
    @Query("SELECT p FROM PAT p WHERE p.nombre = :nombre AND p.semestre.id = :idSemestre AND (p.esGeneral = 1 OR p.carrera.id = :idCarrera)")
    Optional<PAT> findByUniqueCombo(@Param("nombre") String nombre, @Param("idSemestre") Integer idSemestre, @Param("idCarrera") Integer idCarrera);

    @Query("SELECT p FROM PAT p WHERE p.nombre = :nombre AND p.semestre.id = :idSemestre AND (p.esGeneral = 1 OR p.carrera.id = :idCarrera) AND p.id <> :id")
    Optional<PAT> findByUniqueComboAndIdNot(@Param("nombre") String nombre, @Param("idSemestre") Integer idSemestre, @Param("idCarrera") Integer idCarrera, @Param("id") Integer id);

    @Query("SELECT p FROM PAT p WHERE p.activo = 1 AND p.esGeneral = 1 AND p.semestre = :semestre")
    List<PAT> findGeneralesBySemestre(@Param("semestre") Semestre semestre);
}
