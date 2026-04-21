package com.bumh3r.repository;

import com.bumh3r.entity.Sesion;
import com.bumh3r.entity.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ISesionRepository extends JpaRepository<Sesion, Integer> {

    List<Sesion> findByActivo(Integer activo);

    // Sesiones de un tutor específico
    List<Sesion> findByActivoAndTutor(Integer activo, Tutor tutor);

    // Sesiones por semana
    List<Sesion> findByActivoAndSemana(Integer activo, Integer semana);

    // Sesiones de un tutor en una semana específica
    List<Sesion> findByActivoAndTutorAndSemana(Integer activo, Tutor tutor, Integer semana);

    // Sesiones por estatus
    List<Sesion> findByActivoAndEstatusRegistro(Integer activo, String estatusRegistro);
}