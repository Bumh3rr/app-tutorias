package com.bumh3r.service.impl;

import com.bumh3r.entity.Carrera;
import com.bumh3r.exception.RegistroInactivoExistenteException;
import com.bumh3r.repository.ICarreraRepository;
import com.bumh3r.service.CarreraService;
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
public class CarreraServiceImpl implements CarreraService {

    @Autowired
    private ICarreraRepository iCarreraRepository;

    @Override
    public List<Carrera> obtenerTodasCarreras() {
        return this.iCarreraRepository.findByActivo(1);
    }

    @Override
    public void guardarCarrera(Carrera carrera) {
        this.iCarreraRepository.findByClave(carrera.getClave()).ifPresent(existente -> {
            if (existente.getActivo() == 1) throw new IllegalArgumentException("Ya existe una Carrera activa con la clave \"" + carrera.getClave() + "\"");
            throw new RegistroInactivoExistenteException("Carrera", "Clave", carrera.getClave(), existente.getId());
        });
        carrera.setActivo(1);
        this.iCarreraRepository.save(carrera);
    }

    @Override
    public void actualizarCarrera(Integer id, Carrera carrera) {
        Carrera carreraDB = this.iCarreraRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Carrera no encontrada"));

        this.iCarreraRepository.findByClaveAndIdNot(carrera.getClave(), id).ifPresent(existente -> {
            if (existente.getActivo() == 1) throw new IllegalArgumentException("Ya existe una Carrera activa con la clave \"" + carrera.getClave() + "\"");
            throw new RegistroInactivoExistenteException("Carrera", "Clave", carrera.getClave(), existente.getId());
        });

        carreraDB.setNombre(carrera.getNombre());
        carreraDB.setClave(carrera.getClave());

        this.iCarreraRepository.save(carreraDB);
    }

    @Override
    public Carrera obtenerCarrera(Integer id) {
        return this.iCarreraRepository.findById(id).orElse(null);
    }

    @Override
    public void eliminarCarrera(Integer id) {
        Carrera carrera = this.iCarreraRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Carrera no encontrada"));
        carrera.setActivo(0);
        this.iCarreraRepository.save(carrera);
    }

    @Override
    public Page<Carrera> obtenerTodasCarrerasPage(Pageable pageable) {
        return this.iCarreraRepository.findByActivo(1, pageable);
    }

    @Override
    public void reactivar(Integer id) {
        Carrera carrera = this.iCarreraRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Carrera no encontrada"));
        if (carrera.getActivo() == 1)
            throw new IllegalStateException("Esta Carrera ya está activa.");
        this.iCarreraRepository.findByClaveAndIdNot(carrera.getClave(), id).ifPresent(otro -> {
            if (otro.getActivo() == 1)
                throw new IllegalStateException("No se puede reactivar: ya existe otra Carrera activa con la clave \"" + carrera.getClave() + "\"");
        });
        // Nota: soft-delete de Carrera no afecta a Grupos o PATs relacionados.
        // Si se requiere validación en cascada, implementarla en una fase posterior.
        carrera.setActivo(1);
        this.iCarreraRepository.save(carrera);
    }

    @Override
    public Page<Carrera> obtenerPorEstadoPaginado(String filtroEstado, int page, int pageSize, String sortBy, String sort) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("asc".equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy));
        return switch (filtroEstado) {
            case "inactivos" -> this.iCarreraRepository.findByActivo(0, pageable);
            case "todos"     -> this.iCarreraRepository.findAll(pageable);
            default          -> this.iCarreraRepository.findByActivo(1, pageable);
        };
    }
}