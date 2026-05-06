package com.bumh3r.repository;

import com.bumh3r.entity.ReporteSesion;
import com.bumh3r.entity.Sesion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IReporteSesionRepository extends JpaRepository<ReporteSesion, Integer> {

    List<ReporteSesion> findByActivo(Integer activo);

    Page<ReporteSesion> findByActivo(Integer activo, Pageable pageable);

    // Reporte de una sesión específica
    Optional<ReporteSesion> findBySesionAndActivo(Sesion sesion, Integer activo);

    // Reportes por estatus de revisión
    List<ReporteSesion> findByActivoAndEstatusRevision(Integer activo, String estatusRevision);

    Page<ReporteSesion> findByActivoAndEstatusRevision(Integer activo, String estatusRevision, Pageable pageable);

    // Verificar si ya existe reporte para una sesión
    boolean existsBySesionAndActivo(Sesion sesion, Integer activo);

    @Query("SELECT e FROM ReporteSesion e WHERE e.activo = 1 AND e.fechaRegistro BETWEEN :inicio AND :fin")
    Page<ReporteSesion> findByFechaRegistroRange(@Param("inicio") java.util.Date inicio, @Param("fin") java.util.Date fin, Pageable pageable);
}
