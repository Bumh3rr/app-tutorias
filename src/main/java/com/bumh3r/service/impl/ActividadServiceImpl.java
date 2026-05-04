package com.bumh3r.service.impl;

import com.bumh3r.entity.Actividad;
import com.bumh3r.entity.PAT;
import com.bumh3r.repository.IActividadRepository;
import com.bumh3r.repository.IPATRepository;
import com.bumh3r.service.ActividadService;
import com.bumh3r.service.utils.PaginationUtil;
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
public class ActividadServiceImpl implements ActividadService {

    @Autowired
    private IActividadRepository iActividadRepository;
    @Autowired
    private IPATRepository iPATRepository;
    @Autowired
    private PaginationUtil paginationUtil;

    @Override
    public List<Actividad> obtenerTodasActividades() {
        return this.iActividadRepository.findByActivo(1);
    }

    @Override
    public void guardarActividad(Actividad actividad) {
        resolverRelaciones(actividad);
        if (this.iActividadRepository.existsByNombreAndSemanaAndActivo(actividad.getNombre(), actividad.getSemana(), 1)) {
            throw new IllegalArgumentException(
                "Ya existe una actividad activa con el nombre \"" + actividad.getNombre() + "\" en la semana " + actividad.getSemana());
        }
        actividad.setActivo(1);
        this.iActividadRepository.save(actividad);
    }

    @Override
    public void actualizarActividad(Integer id, Actividad actividad) {
        Actividad actividadDB = this.iActividadRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Actividad no encontrada"));

        resolverRelaciones(actividad);

        if (this.iActividadRepository.existsByNombreAndSemanaAndActivoExcludingId(actividad.getNombre(), actividad.getSemana(), id)) {
            throw new IllegalArgumentException(
                "Ya existe una actividad activa con el nombre \"" + actividad.getNombre() + "\" en la semana " + actividad.getSemana());
        }

        actividadDB.setNombre(actividad.getNombre());
        actividadDB.setDescripcion(actividad.getDescripcion());
        actividadDB.setFecha(actividad.getFecha());
        actividadDB.setSemana(actividad.getSemana());
        actividadDB.setFoto(actividad.getFoto());
        actividadDB.setPat(actividad.getPat());

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

    @Override
    public Page<Actividad> buscarActividadesPorFechaPaginado(Date fechaDate, Integer page, Integer pageSize, String sortBy, String sort) {
        Pageable pageable = this.paginationUtil.getPageable(page, pageSize, sortBy, sort);
        return this.iActividadRepository.findByActivoAndFecha(1, fechaDate, pageable);
    }

    @Override
    public Page<Actividad> buscarActividadesPorRangoFechasPaginado(Date fInicio, Date fFin, Integer page, Integer pageSize, String sortBy, String sort) {
        Pageable pageable = this.paginationUtil.getPageable(page, pageSize, sortBy, sort);
        return this.iActividadRepository.findByActivoAndFechaBetween(1, fInicio, fFin, pageable);
    }

    @Override
    public Page<Actividad> buscarActividadesPorPATPaginado(Integer idPat, Integer page, Integer pageSize, String sortBy, String sort) {
        PAT pat = this.iPATRepository.findById(idPat)
                .orElseThrow(() -> new NoSuchElementException("PAT no encontrado"));
        Pageable pageable = this.paginationUtil.getPageable(page, pageSize, sortBy, sort);
        return this.iActividadRepository.findByActivoAndPat(1, pat, pageable);
    }

    @Override
    public Page<Actividad> obtenerTodasActividadesPaginado(Integer page, Integer pageSize, String sortBy, String sort) {
        Pageable pageable = this.paginationUtil.getPageable(page, pageSize, sortBy, sort);
        return this.iActividadRepository.findByActivo(1, pageable);
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