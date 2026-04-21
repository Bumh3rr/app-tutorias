package com.bumh3r.repository;

import com.bumh3r.entity.Carrera;
import com.bumh3r.entity.Semestre;
import com.bumh3r.entity.Tutorado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ITutoradoRepository extends JpaRepository<Tutorado, Integer> {

    List<Tutorado> findByActivo(Integer activo);

    // Extra útil: tutorados por carrera y semestre
    List<Tutorado> findByActivoAndSemestreAndCarrera(Integer activo, Semestre semestre, Carrera carrera);
}