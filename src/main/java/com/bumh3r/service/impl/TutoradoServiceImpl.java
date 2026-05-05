package com.bumh3r.service.impl;

import com.bumh3r.entity.Carrera;
import com.bumh3r.entity.Tutorado;
import com.bumh3r.repository.ICarreraRepository;
import com.bumh3r.repository.ITutoradoRepository;
import com.bumh3r.service.TutoradoService;
import com.bumh3r.service.utils.PaginationUtil;
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
public class TutoradoServiceImpl implements TutoradoService {

    @Autowired
    private ITutoradoRepository iTutoradoRepository;
    @Autowired
    private ICarreraRepository iCarreraRepository;
    @Autowired
    private PaginationUtil paginationUtil;

    @Override
    public List<Tutorado> obtenerTodosTutorados() {
        return this.iTutoradoRepository.findByActivo(1);
    }

    @Override
    public void guardarTutorado(Tutorado tutorado) {
        resolverRelaciones(tutorado);
        if (this.iTutoradoRepository.existsByNumeroControlAndActivo(tutorado.getNumeroControl(), 1)) {
            throw new IllegalArgumentException("Ya existe un tutorado activo con el número de control " + tutorado.getNumeroControl());
        }
        if (this.iTutoradoRepository.existsByEmailAndActivo(tutorado.getEmail(), 1)) {
            throw new IllegalArgumentException("Ya existe un tutorado activo con el email " + tutorado.getEmail());
        }
        tutorado.setActivo(1);
        this.iTutoradoRepository.save(tutorado);
    }

    @Override
    public void actualizarTutorado(Integer id, Tutorado tutorado) {
        Tutorado tutoradoDB = this.iTutoradoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tutorado no encontrado"));

        resolverRelaciones(tutorado);

        if (this.iTutoradoRepository.existsByNumeroControlAndActivoAndIdNot(tutorado.getNumeroControl(), 1, id)) {
            throw new IllegalArgumentException("Ya existe un tutorado activo con el número de control " + tutorado.getNumeroControl());
        }
        if (this.iTutoradoRepository.existsByEmailAndActivoAndIdNot(tutorado.getEmail(), 1, id)) {
            throw new IllegalArgumentException("Ya existe un tutorado activo con el email " + tutorado.getEmail());
        }

        tutoradoDB.setNombre(tutorado.getNombre());
        tutoradoDB.setApellido(tutorado.getApellido());
        tutoradoDB.setNumeroControl(tutorado.getNumeroControl());
        tutoradoDB.setEmail(tutorado.getEmail());
        tutoradoDB.setFoto(tutorado.getFoto());
        tutoradoDB.setCarrera(tutorado.getCarrera());

        this.iTutoradoRepository.save(tutoradoDB);
    }

    @Override
    public Tutorado obtenerTutorado(Integer id) {
        return this.iTutoradoRepository.findById(id).orElse(null);
    }

    @Override
    public void eliminarTutorado(Integer id) {
        Tutorado tutorado = this.iTutoradoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tutorado no encontrado"));
        tutorado.setActivo(0);
        this.iTutoradoRepository.save(tutorado);
    }

    @Override
    public Page<Tutorado> obtenerTodosTutoradosPaginado(int page, int pageSize, String sortBy, String sort) {
        Pageable pageable = this.paginationUtil.getPageable(page, pageSize, sortBy, sort);
        return this.iTutoradoRepository.findByActivo(1, pageable);
    }

    @Override
    public Page<Tutorado> buscarPorNombre(String q, int page, int pageSize, String sortBy, String sort) {
        Pageable pageable = this.paginationUtil.getPageable(page, pageSize, sortBy, sort);
        return this.iTutoradoRepository.searchByName(q, pageable);
    }

    private void resolverRelaciones(Tutorado tutorado) {
        if (tutorado.getCarrera() != null && tutorado.getCarrera().getId() != null) {
            Carrera carrera = this.iCarreraRepository.findById(tutorado.getCarrera().getId())
                    .orElseThrow(() -> new NoSuchElementException("Carrera no encontrada"));
            tutorado.setCarrera(carrera);
        } else {
            tutorado.setCarrera(null);
        }

    }
}