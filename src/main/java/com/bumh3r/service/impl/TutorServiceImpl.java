package com.bumh3r.service.impl;

import com.bumh3r.entity.Tutor;
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
        if (this.iTutorRepository.existsByNumeroControlAndActivo(tutor.getNumeroControl(), 1)) {
            throw new IllegalArgumentException("Ya existe un tutor activo con el número de control " + tutor.getNumeroControl());
        }
        if (this.iTutorRepository.existsByEmailAndActivo(tutor.getEmail(), 1)) {
            throw new IllegalArgumentException("Ya existe un tutor activo con el email " + tutor.getEmail());
        }
        tutor.setFoto(tutor.getFoto() != null && !tutor.getFoto().isEmpty() ? tutor.getFoto() : null);
        tutor.setActivo(1);
        this.iTutorRepository.save(tutor);
    }

    @Override
    public void actualizarTutor(Integer id, Tutor tutor) {
        Tutor tutorDB = this.iTutorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tutor no encontrado"));

        if (this.iTutorRepository.existsByNumeroControlAndActivoAndIdNot(tutor.getNumeroControl(), 1, id)) {
            throw new IllegalArgumentException("Ya existe un tutor activo con el número de control " + tutor.getNumeroControl());
        }
        if (this.iTutorRepository.existsByEmailAndActivoAndIdNot(tutor.getEmail(), 1, id)) {
            throw new IllegalArgumentException("Ya existe un tutor activo con el email " + tutor.getEmail());
        }

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
    public List<Tutor> obtenerTodosTutores() {
        return this.iTutorRepository.findByActivo(1);
    }
}
