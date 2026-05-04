package com.bumh3r.repository;

import com.bumh3r.entity.Grupo;
import com.bumh3r.entity.Sesion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ISesionRepository extends JpaRepository<Sesion, Integer> {

    List<Sesion> findByActivo(Integer activo);

    List<Sesion> findByActivoAndGrupo(Integer activo, Grupo grupo);

    List<Sesion> findByActivoAndSemana(Integer activo, Integer semana);

    List<Sesion> findByActivoAndGrupoAndSemana(Integer activo, Grupo grupo, Integer semana);

    List<Sesion> findByActivoAndEstatusRegistro(Integer activo, String estatusRegistro);

    Page<Sesion> findByActivo(Integer activo, Pageable pageable);
    Page<Sesion> findByActivoAndGrupo(Integer activo, Grupo grupo, Pageable pageable);
    Page<Sesion> findByActivoAndSemana(Integer activo, Integer semana, Pageable pageable);
    Page<Sesion> findByActivoAndGrupoAndSemana(Integer activo, Grupo grupo, Integer semana, Pageable pageable);
    Page<Sesion> findByActivoAndEstatusRegistro(Integer activo, String estatusRegistro, Pageable pageable);

    boolean existsByGrupoAndSemanaAndActivo(Grupo grupo, Integer semana, Integer activo);

    @Query("SELECT COUNT(s) > 0 FROM Sesion s WHERE s.grupo = :grupo AND s.semana = :semana AND s.activo = 1 AND s.id <> :id")
    boolean existsByGrupoAndSemanaAndActivoExcludingId(@Param("grupo") Grupo grupo, @Param("semana") Integer semana, @Param("id") Integer id);

    @Query("SELECT s FROM Sesion s WHERE s.activo = 1 AND (:q IS NULL OR :q = '' OR LOWER(s.grupo.nombre) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Sesion> searchByName(@Param("q") String q, Pageable pageable);
}
