package com.bumh3r.service.impl;

import com.bumh3r.entity.Carrera;
import com.bumh3r.repository.ICarreraRepository;
import com.bumh3r.service.CarreraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        if (this.iCarreraRepository.existsByClaveAndActivo(carrera.getClave(), 1)) {
            throw new IllegalArgumentException("Ya existe una carrera activa con la clave \"" + carrera.getClave() + "\"");
        }
        carrera.setActivo(1);
        this.iCarreraRepository.save(carrera);
    }

    @Override
    public void actualizarCarrera(Integer id, Carrera carrera) {
        Carrera carreraDB = this.iCarreraRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Carrera no encontrada"));

        if (this.iCarreraRepository.existsByClaveAndActivoAndIdNot(carrera.getClave(), 1, id)) {
            throw new IllegalArgumentException("Ya existe una carrera activa con la clave \"" + carrera.getClave() + "\"");
        }

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
}