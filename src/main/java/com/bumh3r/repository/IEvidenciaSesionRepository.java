package com.bumh3r.repository;

import com.bumh3r.entity.EvidenciaSesion;
import com.bumh3r.entity.Sesion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IEvidenciaSesionRepository extends JpaRepository<EvidenciaSesion, Integer> {

    List<EvidenciaSesion> findByActivo(Integer activo);

    Page<EvidenciaSesion> findByActivo(Integer activo, Pageable pageable);

    // Evidencias de una sesión
    List<EvidenciaSesion> findByActivoAndSesion(Integer activo, Sesion sesion);

    // Evidencias por estatus de validación
    List<EvidenciaSesion> findByActivoAndEstatusValidacion(Integer activo, String estatusValidacion);

    boolean existsBySesionAndActivo(Sesion sesion, int activo);

    @Query("SELECT COUNT(e) > 0 FROM EvidenciaSesion e WHERE e.sesion = :sesion AND e.activo = 1 AND e.id <> :id")
    boolean existsBySesionAndActivoExcludingId(@Param("sesion") Sesion sesion, @Param("id") Integer id);
}