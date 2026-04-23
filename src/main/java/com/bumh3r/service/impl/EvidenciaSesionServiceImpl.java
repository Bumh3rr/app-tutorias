package com.bumh3r.service.impl;

import com.bumh3r.entity.EvidenciaSesion;
import com.bumh3r.entity.Sesion;
import com.bumh3r.repository.IEvidenciaSesionRepository;
import com.bumh3r.repository.ISesionRepository;
import com.bumh3r.service.EvidenciaSesionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Primary
@Service
public class EvidenciaSesionServiceImpl implements EvidenciaSesionService {

    @Autowired
    private IEvidenciaSesionRepository iEvidenciaSesionRepository;
    @Autowired
    private ISesionRepository iSesionRepository;

    @Override
    public List<EvidenciaSesion> obtenerTodasEvidencias() {
        return this.iEvidenciaSesionRepository.findByActivo(1);
    }

    @Override
    public void guardarEvidencia(EvidenciaSesion evidencia) {
        resolverRelaciones(evidencia);
        evidencia.setActivo(1);
        if (evidencia.getEstatusValidacion() == null) {
            evidencia.setEstatusValidacion("PENDIENTE");
        }
        this.iEvidenciaSesionRepository.save(evidencia);
    }

    @Override
    public void actualizarEvidencia(Integer id, EvidenciaSesion evidencia) {
        EvidenciaSesion evidenciaDB = this.iEvidenciaSesionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Evidencia no encontrada"));

        resolverRelaciones(evidencia);
        evidenciaDB.setSesion(evidencia.getSesion());
        evidenciaDB.setArchivoUrl(evidencia.getArchivoUrl());
        evidenciaDB.setNotasCoordinador(evidencia.getNotasCoordinador());
        evidenciaDB.setEstatusValidacion(evidencia.getEstatusValidacion());

        this.iEvidenciaSesionRepository.save(evidenciaDB);
    }

    @Override
    public EvidenciaSesion obtenerEvidencia(Integer id) {
        return this.iEvidenciaSesionRepository.findById(id).orElse(null);
    }

    @Override
    public void eliminarEvidencia(Integer id) {
        EvidenciaSesion evidencia = this.iEvidenciaSesionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Evidencia no encontrada"));
        evidencia.setActivo(0);
        this.iEvidenciaSesionRepository.save(evidencia);
    }

    @Override
    public List<EvidenciaSesion> buscarEvidenciasPorSesion(Integer idSesion) {
        Sesion sesion = this.iSesionRepository.findById(idSesion)
                .orElseThrow(() -> new NoSuchElementException("Sesión no encontrada"));
        return this.iEvidenciaSesionRepository.findByActivoAndSesion(1, sesion);
    }

    @Override
    public void validarEvidencia(Integer id, String notas) {
        EvidenciaSesion evidencia = this.iEvidenciaSesionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Evidencia no encontrada"));
        evidencia.setEstatusValidacion("VALIDADA");
        evidencia.setNotasCoordinador(notas);
        this.iEvidenciaSesionRepository.save(evidencia);
    }

    @Override
    public void rechazarEvidencia(Integer id, String notas) {
        EvidenciaSesion evidencia = this.iEvidenciaSesionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Evidencia no encontrada"));
        evidencia.setEstatusValidacion("RECHAZADA");
        evidencia.setNotasCoordinador(notas);
        this.iEvidenciaSesionRepository.save(evidencia);
    }

    private void resolverRelaciones(EvidenciaSesion evidencia) {
        if (evidencia.getSesion() != null && evidencia.getSesion().getId() != null) {
            Sesion sesion = this.iSesionRepository.findById(evidencia.getSesion().getId())
                    .orElseThrow(() -> new NoSuchElementException("Sesión no encontrada"));
            evidencia.setSesion(sesion);
        } else {
            evidencia.setSesion(null);
        }
    }
}