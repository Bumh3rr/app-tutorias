package com.bumh3r.service.impl;

import com.bumh3r.entity.Carrera;
import com.bumh3r.entity.CoordinadorCarrera;
import com.bumh3r.entity.Semestre;
import com.bumh3r.repository.ICarreraRepository;
import com.bumh3r.repository.ICoordinadorCarreraRepository;
import com.bumh3r.repository.ISemestreRepository;
import com.bumh3r.service.CoordinadorCarreraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Primary
@Service
public class CoordinadorCarreraServiceImpl implements CoordinadorCarreraService {

    @Autowired
    private ICoordinadorCarreraRepository iCoordinadorCarreraRepository;
    @Autowired
    private ICarreraRepository iCarreraRepository;
    @Autowired
    private ISemestreRepository iSemestreRepository;

    @Override
    public List<CoordinadorCarrera> obtenerTodosCoordinadores() {
        return this.iCoordinadorCarreraRepository.findByActivo(1);
    }

    @Override
    public void guardarCoordinador(CoordinadorCarrera coordinador) {
        resolverRelaciones(coordinador);
        coordinador.setActivo(1);
        this.iCoordinadorCarreraRepository.save(coordinador);
    }

    @Override
    public void actualizarCoordinador(Integer id, CoordinadorCarrera coordinador) {
        CoordinadorCarrera coordinadorDB = this.iCoordinadorCarreraRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Coordinador no encontrado"));

        resolverRelaciones(coordinador);

        coordinadorDB.setNombre(coordinador.getNombre());
        coordinadorDB.setApellido(coordinador.getApellido());
        coordinadorDB.setNumeroControl(coordinador.getNumeroControl());
        coordinadorDB.setEmail(coordinador.getEmail());
        coordinadorDB.setFoto(coordinador.getFoto());
        coordinadorDB.setCargo(coordinador.getCargo());
        coordinadorDB.setCarrera(coordinador.getCarrera());
        coordinadorDB.setSemestre(coordinador.getSemestre());

        this.iCoordinadorCarreraRepository.save(coordinadorDB);
    }

    @Override
    public CoordinadorCarrera obtenerCoordinador(Integer id) {
        return this.iCoordinadorCarreraRepository.findById(id).orElse(null);
    }

    @Override
    public void eliminarCoordinador(Integer id) {
        CoordinadorCarrera coordinador = this.iCoordinadorCarreraRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Coordinador no encontrado"));
        coordinador.setActivo(0);
        this.iCoordinadorCarreraRepository.save(coordinador);
    }

    @Override
    public List<CoordinadorCarrera> buscarPorCarrera(Integer idCarrera) {
        Carrera carrera = this.iCarreraRepository.findById(idCarrera)
                .orElseThrow(() -> new NoSuchElementException("Carrera no encontrada"));
        return this.iCoordinadorCarreraRepository.findByActivoAndCarrera(1, carrera);
    }

    @Override
    public List<CoordinadorCarrera> buscarPorSemestre(Integer idSemestre) {
        Semestre semestre = this.iSemestreRepository.findById(idSemestre)
                .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));
        return this.iCoordinadorCarreraRepository.findByActivoAndSemestre(1, semestre);
    }

    @Override
    public List<CoordinadorCarrera> buscarPorCarreraYSemestre(Integer idCarrera, Integer idSemestre) {
        Carrera carrera = this.iCarreraRepository.findById(idCarrera)
                .orElseThrow(() -> new NoSuchElementException("Carrera no encontrada"));
        Semestre semestre = this.iSemestreRepository.findById(idSemestre)
                .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));
        return this.iCoordinadorCarreraRepository
                .findByActivoAndCarreraAndSemestre(1, carrera, semestre);
    }

    private void resolverRelaciones(CoordinadorCarrera coordinador) {
        if (coordinador.getCarrera() != null && coordinador.getCarrera().getId() != null) {
            Carrera carrera = this.iCarreraRepository.findById(coordinador.getCarrera().getId())
                    .orElseThrow(() -> new NoSuchElementException("Carrera no encontrada"));
            coordinador.setCarrera(carrera);
        } else {
            coordinador.setCarrera(null);
        }

        if (coordinador.getSemestre() != null && coordinador.getSemestre().getId() != null) {
            Semestre semestre = this.iSemestreRepository.findById(coordinador.getSemestre().getId())
                    .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));
            coordinador.setSemestre(semestre);
        } else {
            coordinador.setSemestre(null);
        }
    }
}