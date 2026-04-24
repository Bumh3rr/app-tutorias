package com.bumh3r.repository;

import com.bumh3r.entity.Carrera;
import com.bumh3r.entity.Semestre;
import com.bumh3r.entity.Tutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ITutorRepository extends JpaRepository<Tutor, Integer> {

    List<Tutor> findByActivo(Integer activo);

    Page<Tutor> findByActivo(Integer activo, Pageable pageable);

    // Búsqueda: tutores de un semestre determinado
    Page<Tutor> findByActivoAndSemestre(Integer activo, Semestre semestre, Pageable pageable);

    // Extra útil: tutores por carrera y semestre
    Page<Tutor> findByActivoAndSemestreAndCarrera(Integer activo, Semestre semestre, Carrera carrera, Pageable pageable);

    // En ITutorRepository agregar:
    boolean existsByAulaAndDiaSemanaAndHorarioAndActivo(
            String aula, Tutor.DiaSemana diaSemana, String horario, Integer activo);

    boolean existsByAulaAndDiaSemanaAndHorarioAndActivoAndIdNot(
            String aula, Tutor.DiaSemana diaSemana, String horario, Integer activo, Integer id);

}