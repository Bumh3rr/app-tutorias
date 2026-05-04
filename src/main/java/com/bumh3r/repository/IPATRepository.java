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
}