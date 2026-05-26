package com.bumh3r.service.impl;

import com.bumh3r.entity.Carrera;
import com.bumh3r.entity.CoordinadorCarrera;
import com.bumh3r.entity.Semestre;
import com.bumh3r.exception.RegistroInactivoExistenteException;
import com.bumh3r.repository.ICarreraRepository;
import com.bumh3r.repository.ICoordinadorCarreraRepository;
import com.bumh3r.repository.ISemestreRepository;
import com.bumh3r.service.CoordinadorCarreraService;
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
public class CoordinadorCarreraServiceImpl implements CoordinadorCarreraService {

    @Autowired
    private ICoordinadorCarreraRepository iCoordinadorCarreraRepository;
    @Autowired
    private ICarreraRepository iCarreraRepository;
    @Autowired
    private ISemestreRepository iSemestreRepository;
    @Autowired
    private PaginationUtil paginationUtil;

    @Override
    public List<CoordinadorCarrera> obtenerTodosCoordinadores() {
        return this.iCoordinadorCarreraRepository.findByActivo(1);
    }

    @Override
    public Page<CoordinadorCarrera> obtenerTodosCoordinadoresPage(Pageable pageable) {
        return this.iCoordinadorCarreraRepository.findByActivo(1, pageable);
    }

    @Override
    public void guardarCoordinador(CoordinadorCarrera coordinador) {
        resolverRelaciones(coordinador);
        this.iCoordinadorCarreraRepository.findByNumeroControl(coordinador.getNumeroControl()).ifPresent(existente -> {
            if (existente.getActivo() == 1) throw new IllegalArgumentException("Ya existe un Coordinador activo con el número de control " + coordinador.getNumeroControl());
            throw new RegistroInactivoExistenteException("Coordinador", "Número de control", coordinador.getNumeroControl(), existente.getId());
        });
        this.iCoordinadorCarreraRepository.findByEmail(coordinador.getEmail()).ifPresent(existente -> {
            if (existente.getActivo() == 1) throw new IllegalArgumentException("Ya existe un Coordinador activo con el email " + coordinador.getEmail());
            throw new RegistroInactivoExistenteException("Coordinador", "Email", coordinador.getEmail(), existente.getId());
        });
        coordinador.setActivo(1);
        this.iCoordinadorCarreraRepository.save(coordinador);
    }

    @Override
    public void actualizarCoordinador(Integer id, CoordinadorCarrera coordinador) {
        CoordinadorCarrera coordinadorDB = this.iCoordinadorCarreraRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Coordinador no encontrado"));

        resolverRelaciones(coordinador);

        this.iCoordinadorCarreraRepository.findByNumeroControlAndIdNot(coordinador.getNumeroControl(), id).ifPresent(existente -> {
            if (existente.getActivo() == 1) throw new IllegalArgumentException("Ya existe un Coordinador activo con el número de control " + coordinador.getNumeroControl());
            throw new RegistroInactivoExistenteException("Coordinador", "Número de control", coordinador.getNumeroControl(), existente.getId());
        });
        this.iCoordinadorCarreraRepository.findByEmailAndIdNot(coordinador.getEmail(), id).ifPresent(existente -> {
            if (existente.getActivo() == 1) throw new IllegalArgumentException("Ya existe un Coordinador activo con el email " + coordinador.getEmail());
            throw new RegistroInactivoExistenteException("Coordinador", "Email", coordinador.getEmail(), existente.getId());
        });

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

    @Override
    public Page<CoordinadorCarrera> buscarPorNombrePage(String q, Pageable pageable) {
        return this.iCoordinadorCarreraRepository.searchByName(q, pageable);
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

    @Override
    public org.springframework.data.domain.Page<com.bumh3r.entity.CoordinadorCarrera> buscarPorFechaRegistroPage(java.util.Date inicio, java.util.Date fin, org.springframework.data.domain.Pageable pageable) {
        return this.iCoordinadorCarreraRepository.findByFechaRegistroRange(inicio, fin, pageable);
    }

    @Override
    public void reactivar(Integer id) {
        CoordinadorCarrera coordinador = this.iCoordinadorCarreraRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Coordinador no encontrado"));
        if (coordinador.getActivo() == 1)
            throw new IllegalStateException("Este Coordinador ya está activo.");
        this.iCoordinadorCarreraRepository.findByNumeroControlAndIdNot(coordinador.getNumeroControl(), id).ifPresent(otro -> {
            if (otro.getActivo() == 1)
                throw new IllegalStateException("No se puede reactivar: ya existe otro Coordinador activo con el número de control " + coordinador.getNumeroControl());
        });
        this.iCoordinadorCarreraRepository.findByEmailAndIdNot(coordinador.getEmail(), id).ifPresent(otro -> {
            if (otro.getActivo() == 1)
                throw new IllegalStateException("No se puede reactivar: ya existe otro Coordinador activo con el email " + coordinador.getEmail());
        });
        coordinador.setActivo(1);
        this.iCoordinadorCarreraRepository.save(coordinador);
    }

    @Override
    public Page<CoordinadorCarrera> obtenerPorEstadoPaginado(String filtroEstado, int page, int pageSize, String sortBy, String sort) {
        Pageable pageable = this.paginationUtil.getPageable(page, pageSize, sortBy, sort);
        return switch (filtroEstado) {
            case "inactivos" -> this.iCoordinadorCarreraRepository.findByActivo(0, pageable);
            case "todos"     -> this.iCoordinadorCarreraRepository.findAll(pageable);
            default          -> this.iCoordinadorCarreraRepository.findByActivo(1, pageable);
        };
    }
}
