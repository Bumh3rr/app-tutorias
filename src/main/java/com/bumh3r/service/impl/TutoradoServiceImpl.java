package com.bumh3r.service.impl;

import com.bumh3r.entity.Carrera;
import com.bumh3r.entity.Semestre;
import com.bumh3r.entity.Tutorado;
import com.bumh3r.repository.ICarreraRepository;
import com.bumh3r.repository.ISemestreRepository;
import com.bumh3r.repository.ITutoradoRepository;
import com.bumh3r.service.TutoradoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
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
    private ISemestreRepository iSemestreRepository;

    @Override
    public List<Tutorado> obtenerTodosTutorados() {
        return this.iTutoradoRepository.findByActivo(1);
    }

    @Override
    public void guardarTutorado(Tutorado tutorado) {
        resolverRelaciones(tutorado);
        tutorado.setActivo(1);
        this.iTutoradoRepository.save(tutorado);
    }

    @Override
    public void actualizarTutorado(Integer id, Tutorado tutorado) {
        Tutorado tutoradoDB = this.iTutoradoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tutorado no encontrado"));

        resolverRelaciones(tutorado);

        tutoradoDB.setNombre(tutorado.getNombre());
        tutoradoDB.setApellido(tutorado.getApellido());
        tutoradoDB.setNumeroControl(tutorado.getNumeroControl());
        tutoradoDB.setEmail(tutorado.getEmail());
        tutoradoDB.setFoto(tutorado.getFoto());
        tutoradoDB.setCarrera(tutorado.getCarrera());
        tutoradoDB.setSemestre(tutorado.getSemestre());
        tutoradoDB.setActivo(tutorado.getActivo());

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
    public List<Tutorado> buscarTutoradosPorSemestreYCarrera(Integer idSemestre, Integer idCarrera) {
        Semestre semestre = this.iSemestreRepository.findById(idSemestre)
                .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));
        Carrera carrera = this.iCarreraRepository.findById(idCarrera)
                .orElseThrow(() -> new NoSuchElementException("Carrera no encontrada"));
        return this.iTutoradoRepository.findByActivoAndSemestreAndCarrera(1, semestre, carrera);
    }

    private void resolverRelaciones(Tutorado tutorado) {
        if (tutorado.getCarrera() != null && tutorado.getCarrera().getId() != null) {
            Carrera carrera = this.iCarreraRepository.findById(tutorado.getCarrera().getId())
                    .orElseThrow(() -> new NoSuchElementException("Carrera no encontrada"));
            tutorado.setCarrera(carrera);
        } else {
            tutorado.setCarrera(null);
        }

        if (tutorado.getSemestre() != null && tutorado.getSemestre().getId() != null) {
            Semestre semestre = this.iSemestreRepository.findById(tutorado.getSemestre().getId())
                    .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));
            tutorado.setSemestre(semestre);
        } else {
            tutorado.setSemestre(null);
        }
    }
}