package com.bumh3r.repository;

import com.bumh3r.entity.EvidenciaSesion;
import com.bumh3r.entity.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IEvidenciaSesionRepository extends JpaRepository<EvidenciaSesion, Integer> {

    List<EvidenciaSesion> findByActivo(Integer activo);

    // Evidencias de una sesión
    List<EvidenciaSesion> findByActivoAndSesion(Integer activo, Sesion sesion);

    // Evidencias por estatus de validación
    List<EvidenciaSesion> findByActivoAndEstatusValidacion(Integer activo, String estatusValidacion);
}