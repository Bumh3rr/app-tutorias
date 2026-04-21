package com.bumh3r.repository;

import com.bumh3r.entity.Carrera;
import com.bumh3r.entity.Semestre;
import com.bumh3r.entity.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ITutorRepository extends JpaRepository<Tutor, Integer> {

    List<Tutor> findByActivo(Integer activo);

    // Búsqueda: tutores de un semestre determinado
    List<Tutor> findByActivoAndSemestre(Integer activo, Semestre semestre);

    // Extra útil: tutores por carrera y semestre
    List<Tutor> findByActivoAndSemestreAndCarrera(Integer activo, Semestre semestre, Carrera carrera);
}