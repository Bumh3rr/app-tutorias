package com.bumh3r.service.impl;

import com.bumh3r.entity.Actividad;
import com.bumh3r.entity.Sesion;
import com.bumh3r.entity.Tutor;
import com.bumh3r.repository.IActividadRepository;
import com.bumh3r.repository.ISesionRepository;
import com.bumh3r.repository.ITutorRepository;
import com.bumh3r.service.SesionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Primary
@Service
public class SesionServiceImpl implements SesionService {

    @Autowired
    private ISesionRepository iSesionRepository;
    @Autowired
    private ITutorRepository iTutorRepository;
    @Autowired
    private IActividadRepository iActividadRepository;

    @Override
    public List<Sesion> obtenerTodasSesiones() {
        return this.iSesionRepository.findByActivo(1);
    }

    @Override
    public void guardarSesion(Sesion sesion) {
        resolverRelaciones(sesion);

        boolean duplicada = this.iSesionRepository
                .existsByTutorAndSemanaAndActivo(sesion.getTutor(), sesion.getSemana(), 1);
        if (duplicada) {
            throw new IllegalStateException(
                    "El tutor ya tiene una sesión registrada en la semana " + sesion.getSemana() + ".");
        }

        sesion.setActivo(1);
        if (sesion.getEstatusRegistro() == null) {
            sesion.setEstatusRegistro("PENDIENTE");
        }
        this.iSesionRepository.save(sesion);
    }

    @Override
    public void actualizarSesion(Integer id, Sesion sesion) {
        Sesion sesionDB = this.iSesionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Sesión no encontrada"));

        resolverRelaciones(sesion);

        sesionDB.setTutor(sesion.getTutor());
        sesionDB.setActividad(sesion.getActividad());
        sesionDB.setSemana(sesion.getSemana());
        sesionDB.setFechaImparticion(sesion.getFechaImparticion());
        sesionDB.setEstatusRegistro(sesion.getEstatusRegistro());

        this.iSesionRepository.save(sesionDB);
    }

    @Override
    public Sesion obtenerSesion(Integer id) {
        return this.iSesionRepository.findById(id).orElse(null);
    }

    @Override
    public void eliminarSesion(Integer id) {
        Sesion sesion = this.iSesionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Sesión no encontrada"));
        sesion.setActivo(0);
        this.iSesionRepository.save(sesion);
    }

    @Override
    public List<Sesion> buscarSesionesPorTutor(Integer idTutor) {
        Tutor tutor = this.iTutorRepository.findById(idTutor)
                .orElseThrow(() -> new NoSuchElementException("Tutor no encontrado"));
        return this.iSesionRepository.findByActivoAndTutor(1, tutor);
    }

    @Override
    public List<Sesion> buscarSesionesPorSemana(Integer semana) {
        return this.iSesionRepository.findByActivoAndSemana(1, semana);
    }

    @Override
    public List<Sesion> buscarSesionesPorTutorYSemana(Integer idTutor, Integer semana) {
        Tutor tutor = this.iTutorRepository.findById(idTutor)
                .orElseThrow(() -> new NoSuchElementException("Tutor no encontrado"));
        return this.iSesionRepository.findByActivoAndTutorAndSemana(1, tutor, semana);
    }

    @Override
    public List<Sesion> buscarSesionesPorEstatus(String estatus) {
        return this.iSesionRepository.findByActivoAndEstatusRegistro(1, estatus);
    }

    private void resolverRelaciones(Sesion sesion) {
        if (sesion.getTutor() != null && sesion.getTutor().getId() != null) {
            Tutor tutor = this.iTutorRepository.findById(sesion.getTutor().getId())
                    .orElseThrow(() -> new NoSuchElementException("Tutor no encontrado"));
            sesion.setTutor(tutor);
        } else {
            sesion.setTutor(null);
        }

        if (sesion.getActividad() != null && sesion.getActividad().getId() != null) {
            Actividad actividad = this.iActividadRepository.findById(sesion.getActividad().getId())
                    .orElseThrow(() -> new NoSuchElementException("Actividad no encontrada"));
            sesion.setActividad(actividad);
        } else {
            sesion.setActividad(null);
        }
    }
}