package com.bumh3r.service.impl;

import com.bumh3r.entity.DeteccionNecesidades;
import com.bumh3r.entity.Sesion;
import com.bumh3r.entity.Tutorado;
import com.bumh3r.repository.IDeteccionNecesidadesRepository;
import com.bumh3r.repository.ISesionRepository;
import com.bumh3r.repository.ITutoradoRepository;
import com.bumh3r.service.DeteccionNecesidadesService;
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
public class DeteccionNecesidadesServiceImpl implements DeteccionNecesidadesService {

    @Autowired
    private IDeteccionNecesidadesRepository iDeteccionNecesidadesRepository;
    @Autowired
    private ITutoradoRepository iTutoradoRepository;
    @Autowired
    private ISesionRepository iSesionRepository;

    @Override
    public List<DeteccionNecesidades> obtenerTodasDetecciones() {
        return this.iDeteccionNecesidadesRepository.findByActivo(1);
    }

    @Override
    public Page<DeteccionNecesidades> obtenerTodasDeteccionesPage(Pageable pageable) {
        return this.iDeteccionNecesidadesRepository.findByActivo(1, pageable);
    }

    @Override
    public void guardarDeteccion(DeteccionNecesidades deteccion) {
        resolverRelaciones(deteccion);

        if (deteccion.getFechaAplicacion() == null) {
            deteccion.setFechaAplicacion(new Date());
        }

        // Valores por defecto si vienen nulos
        if (deteccion.getNecesidadAlgebra() == null) deteccion.setNecesidadAlgebra(0);
        if (deteccion.getNecesidadCalculo() == null) deteccion.setNecesidadCalculo(0);
        if (deteccion.getNecesidadDerecho() == null) deteccion.setNecesidadDerecho(0);
        if (deteccion.getNecesidadEconomica() == null) deteccion.setNecesidadEconomica(0);
        if (deteccion.getNecesidadPsicologica() == null) deteccion.setNecesidadPsicologica(0);

        deteccion.setActivo(1);
        this.iDeteccionNecesidadesRepository.save(deteccion);
    }

    @Override
    public void actualizarDeteccion(Integer id, DeteccionNecesidades deteccion) {
        DeteccionNecesidades deteccionDB = this.iDeteccionNecesidadesRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Detección no encontrada"));

        resolverRelaciones(deteccion);

        deteccionDB.setTutorado(deteccion.getTutorado());
        deteccionDB.setSesion(deteccion.getSesion());
        deteccionDB.setNecesidadAlgebra(deteccion.getNecesidadAlgebra());
        deteccionDB.setNecesidadCalculo(deteccion.getNecesidadCalculo());
        deteccionDB.setNecesidadDerecho(deteccion.getNecesidadDerecho());
        deteccionDB.setNecesidadOtra(deteccion.getNecesidadOtra());
        deteccionDB.setNecesidadEconomica(deteccion.getNecesidadEconomica());
        deteccionDB.setNecesidadPsicologica(deteccion.getNecesidadPsicologica());
        deteccionDB.setObservaciones(deteccion.getObservaciones());
        deteccionDB.setFechaAplicacion(deteccion.getFechaAplicacion());
        deteccionDB.setActivo(deteccion.getActivo());

        this.iDeteccionNecesidadesRepository.save(deteccionDB);
    }

    @Override
    public DeteccionNecesidades obtenerDeteccion(Integer id) {
        return this.iDeteccionNecesidadesRepository.findById(id).orElse(null);
    }

    @Override
    public void eliminarDeteccion(Integer id) {
        DeteccionNecesidades deteccion = this.iDeteccionNecesidadesRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Detección no encontrada"));
        deteccion.setActivo(0);
        this.iDeteccionNecesidadesRepository.save(deteccion);
    }

    @Override
    public List<DeteccionNecesidades> buscarPorTutorado(Integer idTutorado) {
        Tutorado tutorado = this.iTutoradoRepository.findById(idTutorado)
                .orElseThrow(() -> new NoSuchElementException("Tutorado no encontrado"));
        return this.iDeteccionNecesidadesRepository.findByActivoAndTutorado(1, tutorado);
    }

    @Override
    public List<DeteccionNecesidades> buscarPorSesion(Integer idSesion) {
        Sesion sesion = this.iSesionRepository.findById(idSesion)
                .orElseThrow(() -> new NoSuchElementException("Sesión no encontrada"));
        return this.iDeteccionNecesidadesRepository.findByActivoAndSesion(1, sesion);
    }

    @Override
    public List<DeteccionNecesidades> buscarPorNecesidadAlgebra() {
        return this.iDeteccionNecesidadesRepository.findByActivoAndNecesidadAlgebra(1, 1);
    }

    @Override
    public List<DeteccionNecesidades> buscarPorNecesidadCalculo() {
        return this.iDeteccionNecesidadesRepository.findByActivoAndNecesidadCalculo(1, 1);
    }

    @Override
    public List<DeteccionNecesidades> buscarPorNecesidadEconomica() {
        return this.iDeteccionNecesidadesRepository.findByActivoAndNecesidadEconomica(1, 1);
    }

    @Override
    public List<DeteccionNecesidades> buscarPorNecesidadPsicologica() {
        return this.iDeteccionNecesidadesRepository.findByActivoAndNecesidadPsicologica(1, 1);
    }

    private void resolverRelaciones(DeteccionNecesidades deteccion) {
        if (deteccion.getTutorado() != null && deteccion.getTutorado().getId() != null) {
            Tutorado tutorado = this.iTutoradoRepository.findById(deteccion.getTutorado().getId())
                    .orElseThrow(() -> new NoSuchElementException("Tutorado no encontrado"));
            deteccion.setTutorado(tutorado);
        } else {
            deteccion.setTutorado(null);
        }

        if (deteccion.getSesion() != null && deteccion.getSesion().getId() != null) {
            Sesion sesion = this.iSesionRepository.findById(deteccion.getSesion().getId())
                    .orElseThrow(() -> new NoSuchElementException("Sesión no encontrada"));
            deteccion.setSesion(sesion);
        } else {
            deteccion.setSesion(null);
        }
    }
}