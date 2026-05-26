package com.bumh3r.service.impl;

import com.bumh3r.entity.Tutor;
import com.bumh3r.exception.RegistroInactivoExistenteException;
import com.bumh3r.repository.ITutorRepository;
import com.bumh3r.service.TutorService;
import com.bumh3r.service.utils.PaginationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Primary
@Service
public class TutorServiceImpl implements TutorService {

    @Autowired
    private ITutorRepository iTutorRepository;
    @Autowired
    private PaginationUtil paginationUtil;

    @Override
    public void guardarTutor(Tutor tutor) {
        this.iTutorRepository.findByNumeroControl(tutor.getNumeroControl()).ifPresent(existente -> {
            if (existente.getActivo() == 1)
                throw new IllegalArgumentException("Ya existe un Tutor activo con el número de control " + tutor.getNumeroControl());
            throw new RegistroInactivoExistenteException("Tutor", "Número de control", tutor.getNumeroControl(), existente.getId());
        });
        this.iTutorRepository.findByEmail(tutor.getEmail()).ifPresent(existente -> {
            if (existente.getActivo() == 1)
                throw new IllegalArgumentException("Ya existe un Tutor activo con el email " + tutor.getEmail());
            throw new RegistroInactivoExistenteException("Tutor", "Email", tutor.getEmail(), existente.getId());
        });
        tutor.setActivo(1);
        this.iTutorRepository.save(tutor);
    }

    @Override
    public void actualizarTutor(Integer id, Tutor tutor) {
        Tutor tutorDB = this.iTutorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tutor no encontrado"));

        this.iTutorRepository.findByNumeroControlAndIdNot(tutor.getNumeroControl(), id).ifPresent(existente -> {
            if (existente.getActivo() == 1)
                throw new IllegalArgumentException("Ya existe un Tutor activo con el número de control " + tutor.getNumeroControl());
            throw new RegistroInactivoExistenteException("Tutor", "Número de control", tutor.getNumeroControl(), existente.getId());
        });
        this.iTutorRepository.findByEmailAndIdNot(tutor.getEmail(), id).ifPresent(existente -> {
            if (existente.getActivo() == 1)
                throw new IllegalArgumentException("Ya existe un Tutor activo con el email " + tutor.getEmail());
            throw new RegistroInactivoExistenteException("Tutor", "Email", tutor.getEmail(), existente.getId());
        });

        tutorDB.setNombre(tutor.getNombre());
        tutorDB.setApellido(tutor.getApellido());
        tutorDB.setNumeroControl(tutor.getNumeroControl());
        tutorDB.setEmail(tutor.getEmail());
        if (tutor.getFoto() != null) {
            tutorDB.setFoto(tutor.getFoto());
        }

        this.iTutorRepository.save(tutorDB);
    }

    @Override
    public Tutor obtenerTutor(Integer id) {
        return this.iTutorRepository.findById(id).orElse(null);
    }

    @Override
    public void eliminarTutor(Integer id) {
        Tutor tutor = this.iTutorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tutor no encontrado"));
        tutor.setActivo(0);
        this.iTutorRepository.save(tutor);
    }

    @Override
    public Page<Tutor> obtenerTodosTutoresPaginado(Integer page, Integer pageSize, String sortBy, String sort) {
        Pageable pageable = this.paginationUtil.getPageable(page, pageSize, sortBy, sort);
        return this.iTutorRepository.findByActivo(1, pageable);
    }

    @Override
    public Page<Tutor> buscarPorNombre(String q, Integer page, Integer pageSize, String sortBy, String sort) {
        Pageable pageable = this.paginationUtil.getPageable(page, pageSize, sortBy, sort);
        return this.iTutorRepository.searchByName(q, pageable);
    }

    @Override
    public Page<Tutor> buscarPorNumeroControl(String q, Integer page, Integer pageSize, String sortBy, String sort) {
        Pageable pageable = this.paginationUtil.getPageable(page, pageSize, sortBy, sort);
        return this.iTutorRepository.searchByNumeroControl(q, pageable);
    }

    @Override
    public Page<Tutor> buscarPorEmail(String q, Integer page, Integer pageSize, String sortBy, String sort) {
        Pageable pageable = this.paginationUtil.getPageable(page, pageSize, sortBy, sort);
        return this.iTutorRepository.searchByEmail(q, pageable);
    }

    @Override
    public Page<Tutor> buscarPorFechaRegistro(java.util.Date inicio, java.util.Date fin, Integer page, Integer pageSize, String sortBy, String sort) {
        Pageable pageable = this.paginationUtil.getPageable(page, pageSize, sortBy, sort);
        return this.iTutorRepository.searchByFechaRegistro(inicio, fin, pageable);
    }

    @Override
    public Page<Tutor> buscarPorSemestre(Integer idSemestre, Integer page, Integer pageSize, String sortBy, String sort) {
        Pageable pageable = this.paginationUtil.getPageable(page, pageSize, sortBy, sort);
        return this.iTutorRepository.searchBySemestre(idSemestre, pageable);
    }

    @Override
    public List<Tutor> obtenerTodosTutores() {
        return this.iTutorRepository.findByActivo(1);
    }

    @Override
    public void reactivar(Integer id) {
        Tutor tutor = this.iTutorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tutor no encontrado"));
        if (tutor.getActivo() == 1)
            throw new IllegalStateException("Este Tutor ya está activo.");
        this.iTutorRepository.findByNumeroControlAndIdNot(tutor.getNumeroControl(), id).ifPresent(otro -> {
            if (otro.getActivo() == 1)
                throw new IllegalStateException("No se puede reactivar: ya existe otro Tutor activo con el número de control " + tutor.getNumeroControl());
        });
        this.iTutorRepository.findByEmailAndIdNot(tutor.getEmail(), id).ifPresent(otro -> {
            if (otro.getActivo() == 1)
                throw new IllegalStateException("No se puede reactivar: ya existe otro Tutor activo con el email " + tutor.getEmail());
        });
        tutor.setActivo(1);
        this.iTutorRepository.save(tutor);
        // Nota: este soft-delete/reactivación no afecta al Usuario asociado.
        // La gestión de la cuenta de usuario es independiente y queda en manos del admin.
    }

    @Override
    public Page<Tutor> obtenerPorEstadoPaginado(String filtroEstado, int page, int pageSize, String sortBy, String sort) {
        Pageable pageable = this.paginationUtil.getPageable(page, pageSize, sortBy, sort);
        return switch (filtroEstado) {
            case "inactivos" -> this.iTutorRepository.findByActivo(0, pageable);
            case "todos"     -> this.iTutorRepository.findAll(pageable);
            default          -> this.iTutorRepository.findByActivo(1, pageable);
        };
    }
}
