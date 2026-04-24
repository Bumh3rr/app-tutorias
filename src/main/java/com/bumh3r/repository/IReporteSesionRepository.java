package com.bumh3r.repository;

import com.bumh3r.entity.ReporteSesion;
import com.bumh3r.entity.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IReporteSesionRepository extends JpaRepository<ReporteSesion, Integer> {

    List<ReporteSesion> findByActivo(Integer activo);

    // Reporte de una sesión específica
    Optional<ReporteSesion> findBySesionAndActivo(Sesion sesion, Integer activo);

    // Reportes por estatus de revisión
    List<ReporteSesion> findByActivoAndEstatusRevision(Integer activo, String estatusRevision);

    // Verificar si ya existe reporte para una sesión
    boolean existsBySesionAndActivo(Sesion sesion, Integer activo);
}