package com.bumh3r.service.impl;

import com.bumh3r.entity.Actividad;
import com.bumh3r.entity.PAT;
import com.bumh3r.repository.IActividadRepository;
import com.bumh3r.repository.IPATRepository;
import com.bumh3r.service.ActividadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

@Primary
@Service
public class ActividadServiceImpl implements ActividadService {

    @Autowired
    private IActividadRepository iActividadRepository;
    @Autowired
    private IPATRepository iPATRepository;

    @Override
    public List<Actividad> obtenerTodasActividades() {
        return this.iActividadRepository.findByActivo(1);
    }

    @Override
    public void guardarActividad(Actividad actividad) {
        resolverRelaciones(actividad);
        actividad.setActivo(1);
        this.iActividadRepository.save(actividad);
    }

    @Override
    public void actualizarActividad(Integer id, Actividad actividad) {
        Actividad actividadDB = this.iActividadRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Actividad no encontrada"));

        resolverRelaciones(actividad);

        actividadDB.setNombre(actividad.getNombre());
        actividadDB.setDescripcion(actividad.getDescripcion());
        actividadDB.setFecha(actividad.getFecha());
        actividadDB.setSemana(actividad.getSemana());
        actividadDB.setFoto(actividad.getFoto());
        actividadDB.setPat(actividad.getPat());
        actividadDB.setActivo(actividad.getActivo());

        this.iActividadRepository.save(actividadDB);
    }

    @Override
    public Actividad obtenerActividad(Integer id) {
        return this.iActividadRepository.findById(id).orElse(null);
    }

    @Override
    public void eliminarActividad(Integer id) {
        Actividad actividad = this.iActividadRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Actividad no encontrada"));
        actividad.setActivo(0);
        this.iActividadRepository.save(actividad);
    }

    @Override
    public List<Actividad> buscarActividadesPorFecha(Date fecha) {
        return this.iActividadRepository.findByActivoAndFecha(1, fecha);
    }

    @Override
    public List<Actividad> buscarActividadesPorRangoFechas(Date fechaInicio, Date fechaFin) {
        return this.iActividadRepository.findByActivoAndFechaBetween(1, fechaInicio, fechaFin);
    }

    @Override
    public List<Actividad> buscarActividadesPorPAT(Integer idPat) {
        PAT pat = this.iPATRepository.findById(idPat)
                .orElseThrow(() -> new NoSuchElementException("PAT no encontrado"));
        return this.iActividadRepository.findByActivoAndPat(1, pat);
    }

    private void resolverRelaciones(Actividad actividad) {
        if (actividad.getPat() != null && actividad.getPat().getId() != null) {
            PAT pat = this.iPATRepository.findById(actividad.getPat().getId())
                    .orElseThrow(() -> new NoSuchElementException("PAT no encontrado"));
            actividad.setPat(pat);
        } else {
            actividad.setPat(null);
        }
    }
}