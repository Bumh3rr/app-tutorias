package com.bumh3r.service.impl;

import com.bumh3r.entity.ReporteSesion;
import com.bumh3r.entity.Sesion;
import com.bumh3r.repository.IReporteSesionRepository;
import com.bumh3r.repository.ISesionRepository;
import com.bumh3r.service.ReporteSesionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

@Primary
@Service
public class ReporteSesionServiceImpl implements ReporteSesionService {

    @Autowired
    private IReporteSesionRepository iReporteSesionRepository;
    @Autowired
    private ISesionRepository iSesionRepository;

    @Override
    public List<ReporteSesion> obtenerTodosReportes() {
        return this.iReporteSesionRepository.findByActivo(1);
    }

    @Override
    public Page<ReporteSesion> obtenerTodosReportesPage(Pageable pageable) {
        return this.iReporteSesionRepository.findByActivo(1, pageable);
    }

    @Override
    public Page<ReporteSesion> buscarPorEstatusPage(String estatus, Pageable pageable) {
        return this.iReporteSesionRepository.findByActivoAndEstatusRevision(1, estatus, pageable);
    }

    @Override
    public void guardarReporte(ReporteSesion reporte) {
        resolverRelaciones(reporte);

        reporte.setEstatusRevision("PENDIENTE");
        reporte.setFechaEntrega(new Date());
        reporte.setActivo(1);
        this.iReporteSesionRepository.save(reporte);
    }

    @Override
    public void actualizarReporte(Integer id, ReporteSesion reporte) {
        ReporteSesion reporteDB = this.iReporteSesionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Reporte no encontrado"));

        resolverRelaciones(reporte);

        reporteDB.setSesion(reporte.getSesion());
        reporteDB.setDescripcionActividad(reporte.getDescripcionActividad());
        reporteDB.setObservaciones(reporte.getObservaciones());
        reporteDB.setAlumnosPresentes(reporte.getAlumnosPresentes());
        reporteDB.setActivo(reporte.getActivo());

        this.iReporteSesionRepository.save(reporteDB);
    }

    @Override
    public ReporteSesion obtenerReporte(Integer id) {
        return this.iReporteSesionRepository.findById(id).orElse(null);
    }

    @Override
    public ReporteSesion obtenerReportePorSesion(Integer idSesion) {
        Sesion sesion = this.iSesionRepository.findById(idSesion)
                .orElseThrow(() -> new NoSuchElementException("Sesión no encontrada"));
        return this.iReporteSesionRepository
                .findBySesionAndActivo(sesion, 1)
                .orElse(null);
    }

    @Override
    public void eliminarReporte(Integer id) {
        ReporteSesion reporte = this.iReporteSesionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Reporte no encontrado"));
        reporte.setActivo(0);
        this.iReporteSesionRepository.save(reporte);
    }

    @Override
    public List<ReporteSesion> buscarPorEstatus(String estatus) {
        return this.iReporteSesionRepository.findByActivoAndEstatusRevision(1, estatus);
    }

    private void resolverRelaciones(ReporteSesion reporte) {
        if (reporte.getSesion() != null && reporte.getSesion().getId() != null) {
            Sesion sesion = this.iSesionRepository.findById(reporte.getSesion().getId())
                    .orElseThrow(() -> new NoSuchElementException("Sesión no encontrada"));
            reporte.setSesion(sesion);
        } else {
            reporte.setSesion(null);
        }
    }

    @Override
    public org.springframework.data.domain.Page<com.bumh3r.entity.ReporteSesion> buscarPorFechaRegistroPage(java.util.Date inicio, java.util.Date fin, org.springframework.data.domain.Pageable pageable) {
        return this.iReporteSesionRepository.findByFechaRegistroRange(inicio, fin, pageable);
    }
}
