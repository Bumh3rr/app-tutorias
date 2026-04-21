package com.bumh3r.repository;

import com.bumh3r.entity.AsignacionTutorado;
import com.bumh3r.entity.Semestre;
import com.bumh3r.entity.Tutor;
import com.bumh3r.entity.Tutorado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IAsignacionTutoradoRepository extends JpaRepository<AsignacionTutorado, Integer> {

    List<AsignacionTutorado> findByActivo(Integer activo);

    // Búsqueda: tutorías que ha cursado un tutorado
    List<AsignacionTutorado> findByActivoAndTutorado(Integer activo, Tutorado tutorado);

    // Asignaciones de un tutor en un semestre
    List<AsignacionTutorado> findByActivoAndTutorAndSemestre(Integer activo, Tutor tutor, Semestre semestre);

    // Asignaciones por semestre
    List<AsignacionTutorado> findByActivoAndSemestre(Integer activo, Semestre semestre);
}