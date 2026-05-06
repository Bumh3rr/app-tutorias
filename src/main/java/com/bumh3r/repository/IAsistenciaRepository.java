package com.bumh3r.repository;

import com.bumh3r.entity.Asistencia;
import com.bumh3r.entity.Sesion;
import com.bumh3r.entity.Tutorado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IAsistenciaRepository extends JpaRepository<Asistencia, Integer> {

    List<Asistencia> findBySesion(Sesion sesion);

    List<Asistencia> findByTutorado(Tutorado tutorado);

    long countByTutoradoAndPresente(Tutorado tutorado, Integer presente);

    long countByTutoradoAndRecuperada(Tutorado tutorado, Integer recuperada);

    boolean existsBySesionAndTutorado(Sesion sesion, Tutorado tutorado);

    List<Asistencia> findByFechaRegistroBetween(java.util.Date inicio, java.util.Date fin);
}
