package com.bumh3r.repository;

import com.bumh3r.entity.Asistencia;
import com.bumh3r.entity.Sesion;
import com.bumh3r.entity.Tutorado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT COUNT(a) > 0 FROM Asistencia a WHERE a.tutorado = :tutorado AND a.sesion.grupo.semestre.id = :semestreId")
    boolean existsByTutoradoAndSesionGrupoSemestreId(
            @Param("tutorado") Tutorado tutorado,
            @Param("semestreId") Integer semestreId);
}
