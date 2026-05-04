package com.bumh3r.service;

import com.bumh3r.entity.ReporteSesion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ReporteSesionService {
    List<ReporteSesion> obtenerTodosReportes();
    Page<ReporteSesion> obtenerTodosReportesPage(Pageable pageable);
    Page<ReporteSesion> buscarPorEstatusPage(String estatus, Pageable pageable);
    void guardarReporte(ReporteSesion reporte);
    void actualizarReporte(Integer id, ReporteSesion reporte);
    ReporteSesion obtenerReporte(Integer id);
    ReporteSesion obtenerReportePorSesion(Integer idSesion);
    void eliminarReporte(Integer id);

    // Búsquedas
    List<ReporteSesion> buscarPorEstatus(String estatus);
}