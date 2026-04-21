package com.bumh3r.service.impl;

import com.bumh3r.entity.Semestre;
import com.bumh3r.repository.ISemestreRepository;
import com.bumh3r.service.SemestreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
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
        semestre.setActivo(1);
        this.iSemestreRepository.save(semestre);
    }

    @Override
    public void actualizarSemestre(Integer id, Semestre semestre) {
        Semestre semestreDB = this.iSemestreRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));

        semestreDB.setPeriodo(semestre.getPeriodo());
        semestreDB.setAnio(semestre.getAnio());
        semestreDB.setActivo(semestre.getActivo());

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
}