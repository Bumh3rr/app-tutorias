package com.bumh3r.repository;

import com.bumh3r.entity.Asistencia;
import com.bumh3r.entity.Sesion;
import com.bumh3r.entity.Tutorado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IAsistenciaRepository extends JpaRepository<Asistencia, Integer> {

    List<Asistencia> findByActivo(Integer activo);

    Page<Asistencia> findByActivo(Integer activo, Pageable pageable);

    // Asistencias de una sesión
    List<Asistencia> findByActivoAndSesion(Integer activo, Sesion sesion);

    // Historial de asistencias de un tutorado
    List<Asistencia> findByActivoAndTutorado(Integer activo, Tutorado tutorado);

    // Asistencias presentes de un tutorado
    List<Asistencia> findByActivoAndTutoradoAndPresente(Integer activo, Tutorado tutorado, Integer presente);

    // Contar asistencias presentes de un tutorado
    long countByTutoradoAndPresenteAndActivo(Tutorado tutorado, Integer presente, Integer activo);

    // Contar asistencias recuperadas de un tutorado
    long countByTutoradoAndRecuperadaAndActivo(Tutorado tutorado, Integer recuperada, Integer activo);

    // Verificar si ya existe asistencia de un tutorado en una sesión
    boolean existsBySesionAndTutoradoAndActivo(Sesion sesion, Tutorado tutorado, Integer activo);
}