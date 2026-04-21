package com.bumh3r.repository;

import com.bumh3r.entity.Carrera;
import com.bumh3r.entity.PAT;
import com.bumh3r.entity.Semestre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPATRepository extends JpaRepository<PAT, Integer> {

    List<PAT> findByActivo(Integer activo);

    // PAT general o por carrera
    List<PAT> findByActivoAndEsGeneral(Integer activo, Integer esGeneral);

    // PAT de una carrera y semestre específicos
    List<PAT> findByActivoAndCarreraAndSemestre(Integer activo, Carrera carrera, Semestre semestre);
}