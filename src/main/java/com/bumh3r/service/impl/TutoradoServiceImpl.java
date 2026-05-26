package com.bumh3r.service.impl;

import com.bumh3r.entity.Carrera;
import com.bumh3r.entity.Tutorado;
import com.bumh3r.exception.RegistroInactivoExistenteException;
import com.bumh3r.repository.ICarreraRepository;
import com.bumh3r.repository.ITutoradoRepository;
import com.bumh3r.service.TutoradoService;
import com.bumh3r.service.UsuarioService;
import com.bumh3r.service.utils.PaginationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private UsuarioService usuarioService;
    @Autowired
    private PaginationUtil paginationUtil;

    @Override
    public List<Tutorado> obtenerTodosTutorados() {
        return this.iTutoradoRepository.findByActivo(1);
    }

    @Override
    @Transactional
    public void guardarTutorado(Tutorado tutorado) {
        resolverRelaciones(tutorado);
        this.iTutoradoRepository.findByNumeroControl(tutorado.getNumeroControl()).ifPresent(existente -> {
            if (existente.getActivo() == 1)
                throw new IllegalArgumentException("Ya existe un Tutorado activo con el número de control " + tutorado.getNumeroControl());
            throw new RegistroInactivoExistenteException("Tutorado", "Número de control", tutorado.getNumeroControl(), existente.getId());
        });
        this.iTutoradoRepository.findByEmail(tutorado.getEmail()).ifPresent(existente -> {
            if (existente.getActivo() == 1)
                throw new IllegalArgumentException("Ya existe un Tutorado activo con el email " + tutorado.getEmail());
            throw new RegistroInactivoExistenteException("Tutorado", "Email", tutorado.getEmail(), existente.getId());
        });
        tutorado.setActivo(1);
        this.iTutoradoRepository.save(tutorado);
        this.usuarioService.crearParaTutorado(tutorado);
    }

    @Override
    public void actualizarTutorado(Integer id, Tutorado tutorado) {
        Tutorado tutoradoDB = this.iTutoradoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tutorado no encontrado"));

        resolverRelaciones(tutorado);

        this.iTutoradoRepository.findByNumeroControlAndIdNot(tutorado.getNumeroControl(), id).ifPresent(existente -> {
            if (existente.getActivo() == 1)
                throw new IllegalArgumentException("Ya existe un Tutorado activo con el número de control " + tutorado.getNumeroControl());
            throw new RegistroInactivoExistenteException("Tutorado", "Número de control", tutorado.getNumeroControl(), existente.getId());
        });
        this.iTutoradoRepository.findByEmailAndIdNot(tutorado.getEmail(), id).ifPresent(existente -> {
            if (existente.getActivo() == 1)
                throw new IllegalArgumentException("Ya existe un Tutorado activo con el email " + tutorado.getEmail());
            throw new RegistroInactivoExistenteException("Tutorado", "Email", tutorado.getEmail(), existente.getId());
        });

        tutoradoDB.setNombre(tutorado.getNombre());
        tutoradoDB.setApellido(tutorado.getApellido());
        tutoradoDB.setNumeroControl(tutorado.getNumeroControl());
        tutoradoDB.setEmail(tutorado.getEmail());
        tutoradoDB.setFoto(tutorado.getFoto());
        tutoradoDB.setCarrera(tutorado.getCarrera());
        tutoradoDB.setGrado(tutorado.getGrado());
        tutoradoDB.setSexo(tutorado.getSexo());

        this.iTutoradoRepository.save(tutoradoDB);
    }

    @Override
    public Tutorado obtenerTutorado(Integer id) {
        return this.iTutoradoRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void eliminarTutorado(Integer id) {
        Tutorado tutorado = this.iTutoradoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tutorado no encontrado"));
        tutorado.setActivo(0);
        this.iTutoradoRepository.save(tutorado);
        this.usuarioService.desactivarPorTutorado(id);
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

    @Override
    public Page<Tutorado> buscarPorNumeroControl(String q, int page, int pageSize, String sortBy, String sort) {
        Pageable pageable = this.paginationUtil.getPageable(page, pageSize, sortBy, sort);
        return this.iTutoradoRepository.searchByNumeroControl(q, pageable);
    }

    @Override
    public Page<Tutorado> buscarPorEmail(String q, int page, int pageSize, String sortBy, String sort) {
        Pageable pageable = this.paginationUtil.getPageable(page, pageSize, sortBy, sort);
        return this.iTutoradoRepository.searchByEmail(q, pageable);
    }

    @Override
    public Page<Tutorado> buscarPorCarrera(Integer idCarrera, int page, int pageSize, String sortBy, String sort) {
        Pageable pageable = this.paginationUtil.getPageable(page, pageSize, sortBy, sort);
        return this.iTutoradoRepository.findByCarreraId(idCarrera, pageable);
    }

    @Override
    public Page<Tutorado> buscarPorFechaRegistro(java.util.Date inicio, java.util.Date fin, int page, int pageSize, String sortBy, String sort) {
        Pageable pageable = this.paginationUtil.getPageable(page, pageSize, sortBy, sort);
        return this.iTutoradoRepository.searchByFechaRegistro(inicio, fin, pageable);
    }

    @Override
    public void reactivar(Integer id) {
        Tutorado tutorado = this.iTutoradoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tutorado no encontrado"));
        if (tutorado.getActivo() == 1)
            throw new IllegalStateException("Este Tutorado ya está activo.");
        // Verificar que no exista otro activo con el mismo número de control
        this.iTutoradoRepository.findByNumeroControlAndIdNot(tutorado.getNumeroControl(), id).ifPresent(otro -> {
            if (otro.getActivo() == 1)
                throw new IllegalStateException("No se puede reactivar: ya existe otro Tutorado activo con el número de control " + tutorado.getNumeroControl());
        });
        this.iTutoradoRepository.findByEmailAndIdNot(tutorado.getEmail(), id).ifPresent(otro -> {
            if (otro.getActivo() == 1)
                throw new IllegalStateException("No se puede reactivar: ya existe otro Tutorado activo con el email " + tutorado.getEmail());
        });
        tutorado.setActivo(1);
        this.iTutoradoRepository.save(tutorado);
        this.usuarioService.reactivarPorTutorado(id);
    }

    @Override
    public Page<Tutorado> obtenerPorEstadoPaginado(String filtroEstado, int page, int pageSize, String sortBy, String sort) {
        Pageable pageable = this.paginationUtil.getPageable(page, pageSize, sortBy, sort);
        return switch (filtroEstado) {
            case "inactivos" -> this.iTutoradoRepository.findByActivo(0, pageable);
            case "todos"     -> this.iTutoradoRepository.findAll(pageable);
            default          -> this.iTutoradoRepository.findByActivo(1, pageable);
        };
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