package com.bumh3r.service.impl;

import com.bumh3r.entity.Semestre;
import com.bumh3r.exception.RegistroInactivoExistenteException;
import com.bumh3r.repository.ISemestreRepository;
import com.bumh3r.service.SemestreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Primary
@Service
public class SemestreServiceImpl implements SemestreService {

    @Autowired
    private ISemestreRepository iSemestreRepository;

    @Override
    public List<Semestre> obtenerTodosSemestres() {
        return this.iSemestreRepository.findByActivo(1);
    }

    @Override
    public void guardarSemestre(Semestre semestre) {
        this.iSemestreRepository.findByPeriodoAndAnio(semestre.getPeriodo(), semestre.getAnio()).ifPresent(existente -> {
            if (existente.getActivo() == 1) throw new IllegalArgumentException("Ya existe un Semestre activo con el periodo \"" + semestre.getPeriodo() + "\" y el año " + semestre.getAnio());
            throw new RegistroInactivoExistenteException("Semestre", "Periodo/Año", semestre.getPeriodo() + " " + semestre.getAnio(), existente.getId());
        });
        semestre.setActivo(1);
        this.iSemestreRepository.save(semestre);
    }

    @Override
    public void actualizarSemestre(Integer id, Semestre semestre) {
        Semestre semestreDB = this.iSemestreRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));

        this.iSemestreRepository.findByPeriodoAndAnioAndIdNot(semestre.getPeriodo(), semestre.getAnio(), id).ifPresent(existente -> {
            if (existente.getActivo() == 1) throw new IllegalArgumentException("Ya existe un Semestre activo con el periodo \"" + semestre.getPeriodo() + "\" y el año " + semestre.getAnio());
            throw new RegistroInactivoExistenteException("Semestre", "Periodo/Año", semestre.getPeriodo() + " " + semestre.getAnio(), existente.getId());
        });

        semestreDB.setPeriodo(semestre.getPeriodo());
        semestreDB.setAnio(semestre.getAnio());

        this.iSemestreRepository.save(semestreDB);
    }

    @Override
    public Semestre obtenerSemestre(Integer id) {
        return this.iSemestreRepository.findById(id).orElse(null);
    }

    @Override
    public void eliminarSemestre(Integer id) {
        Semestre semestre = this.iSemestreRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));
        semestre.setActivo(0);
        this.iSemestreRepository.save(semestre);
    }

    @Override
    public Page<Semestre> obtenerTodosSemestresPage(Pageable pageable) {
        return this.iSemestreRepository.findByActivo(1, pageable);
    }

    @Override
    public Semestre obtenerSemestreVigente() {
        return iSemestreRepository.findFirstByActivoOrderByIdDesc(1).orElse(null);
    }

    @Override
    public void reactivar(Integer id) {
        Semestre semestre = this.iSemestreRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));
        if (semestre.getActivo() == 1)
            throw new IllegalStateException("Este Semestre ya está activo.");
        this.iSemestreRepository.findByPeriodoAndAnioAndIdNot(semestre.getPeriodo(), semestre.getAnio(), id).ifPresent(otro -> {
            if (otro.getActivo() == 1)
                throw new IllegalStateException("No se puede reactivar: ya existe otro Semestre activo con el periodo \"" + semestre.getPeriodo() + "\" y el año " + semestre.getAnio());
        });
        // Nota: reactivar un semestre NO lo convierte en vigente.
        // obtenerSemestreVigente() usa findFirstByActivoOrderByIdDesc que retorna el de mayor ID, no el recién reactivado.
        semestre.setActivo(1);
        this.iSemestreRepository.save(semestre);
    }

    @Override
    public Page<Semestre> obtenerPorEstadoPaginado(String filtroEstado, int page, int pageSize, String sortBy, String sort) {
        Sort.Direction dir = "asc".equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(dir, sortBy));
        return switch (filtroEstado) {
            case "inactivos" -> this.iSemestreRepository.findByActivo(0, pageable);
            case "todos"     -> this.iSemestreRepository.findAll(pageable);
            default          -> this.iSemestreRepository.findByActivo(1, pageable);
        };
    }
}