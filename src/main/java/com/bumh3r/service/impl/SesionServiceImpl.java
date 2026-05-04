package com.bumh3r.service.impl;

import com.bumh3r.entity.Actividad;
import com.bumh3r.entity.Grupo;
import com.bumh3r.entity.Sesion;
import com.bumh3r.repository.IActividadRepository;
import com.bumh3r.repository.IGrupoRepository;
import com.bumh3r.repository.ISesionRepository;
import com.bumh3r.service.SesionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Primary
@Service
public class SesionServiceImpl implements SesionService {

    @Autowired
    private ISesionRepository iSesionRepository;
    @Autowired
    private IGrupoRepository iGrupoRepository;
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
                .existsByGrupoAndSemanaAndActivo(sesion.getGrupo(), sesion.getSemana(), 1);
        if (duplicada) {
            throw new IllegalStateException(
                    "El grupo ya tiene una sesión registrada en la semana " + sesion.getSemana() + ".");
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

        if (this.iSesionRepository.existsByGrupoAndSemanaAndActivoExcludingId(sesion.getGrupo(), sesion.getSemana(), id)) {
            throw new IllegalStateException(
                "El grupo ya tiene una sesión registrada en la semana " + sesion.getSemana() + ".");
        }

        sesionDB.setGrupo(sesion.getGrupo());
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
    public List<Sesion> buscarSesionesPorGrupo(Integer idGrupo) {
        Grupo grupo = this.iGrupoRepository.findById(idGrupo)
                .orElseThrow(() -> new NoSuchElementException("Grupo no encontrado"));
        return this.iSesionRepository.findByActivoAndGrupo(1, grupo);
    }

    @Override
    public List<Sesion> buscarSesionesPorSemana(Integer semana) {
        return this.iSesionRepository.findByActivoAndSemana(1, semana);
    }

    @Override
    public List<Sesion> buscarSesionesPorGrupoYSemana(Integer idGrupo, Integer semana) {
        Grupo grupo = this.iGrupoRepository.findById(idGrupo)
                .orElseThrow(() -> new NoSuchElementException("Grupo no encontrado"));
        return this.iSesionRepository.findByActivoAndGrupoAndSemana(1, grupo, semana);
    }

    @Override
    public List<Sesion> buscarSesionesPorEstatus(String estatus) {
        return this.iSesionRepository.findByActivoAndEstatusRegistro(1, estatus);
    }

    @Override
    public Page<Sesion> obtenerTodasSesionesPage(Pageable pageable) {
        return this.iSesionRepository.findByActivo(1, pageable);
    }

    @Override
    public Page<Sesion> buscarSesionesPorGrupoPage(Integer idGrupo, Pageable pageable) {
        Grupo grupo = this.iGrupoRepository.findById(idGrupo)
                .orElseThrow(() -> new NoSuchElementException("Grupo no encontrado"));
        return this.iSesionRepository.findByActivoAndGrupo(1, grupo, pageable);
    }

    @Override
    public Page<Sesion> buscarSesionesPorSemanaPage(Integer semana, Pageable pageable) {
        return this.iSesionRepository.findByActivoAndSemana(1, semana, pageable);
    }

    @Override
    public Page<Sesion> buscarSesionesPorGrupoYSemanaPage(Integer idGrupo, Integer semana, Pageable pageable) {
        Grupo grupo = this.iGrupoRepository.findById(idGrupo)
                .orElseThrow(() -> new NoSuchElementException("Grupo no encontrado"));
        return this.iSesionRepository.findByActivoAndGrupoAndSemana(1, grupo, semana, pageable);
    }

    @Override
    public Page<Sesion> buscarSesionesPorEstatusPage(String estatus, Pageable pageable) {
        return this.iSesionRepository.findByActivoAndEstatusRegistro(1, estatus, pageable);
    }

    private void resolverRelaciones(Sesion sesion) {
        if (sesion.getGrupo() != null && sesion.getGrupo().getId() != null) {
            Grupo grupo = this.iGrupoRepository.findById(sesion.getGrupo().getId())
                    .orElseThrow(() -> new NoSuchElementException("Grupo no encontrado"));
            sesion.setGrupo(grupo);
        } else {
            sesion.setGrupo(null);
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
