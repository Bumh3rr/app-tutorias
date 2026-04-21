package com.bumh3r.service.impl;

import com.bumh3r.entity.Carrera;
import com.bumh3r.entity.Semestre;
import com.bumh3r.entity.Tutor;
import com.bumh3r.repository.ICarreraRepository;
import com.bumh3r.repository.ISemestreRepository;
import com.bumh3r.repository.ITutorRepository;
import com.bumh3r.service.TutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Primary
@Service
public class TutorServiceImpl implements TutorService {

    @Autowired
    private ITutorRepository iTutorRepository;
    @Autowired
    private ICarreraRepository iCarreraRepository;
    @Autowired
    private ISemestreRepository iSemestreRepository;

    @Override
    public List<Tutor> obtenerTodosTutores() {
        return this.iTutorRepository.findByActivo(1);
    }

    @Override
    public void guardarTutor(Tutor tutor) {
        resolverRelaciones(tutor);
        tutor.setActivo(1);
        this.iTutorRepository.save(tutor);
    }

    @Override
    public void actualizarTutor(Integer id, Tutor tutor) {
        Tutor tutorDB = this.iTutorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tutor no encontrado"));

        resolverRelaciones(tutor);

        tutorDB.setNombre(tutor.getNombre());
        tutorDB.setApellido(tutor.getApellido());
        tutorDB.setNumeroControl(tutor.getNumeroControl());
        tutorDB.setEmail(tutor.getEmail());
        tutorDB.setFoto(tutor.getFoto());
        tutorDB.setAula(tutor.getAula());
        tutorDB.setHorario(tutor.getHorario());
        tutorDB.setCarrera(tutor.getCarrera());
        tutorDB.setSemestre(tutor.getSemestre());
        tutorDB.setActivo(tutor.getActivo());

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
    public List<Tutor> buscarTutoresPorSemestre(Integer idSemestre) {
        Semestre semestre = this.iSemestreRepository.findById(idSemestre)
                .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));
        return this.iTutorRepository.findByActivoAndSemestre(1, semestre);
    }

    @Override
    public List<Tutor> buscarTutoresPorSemestreYCarrera(Integer idSemestre, Integer idCarrera) {
        Semestre semestre = this.iSemestreRepository.findById(idSemestre)
                .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));
        Carrera carrera = this.iCarreraRepository.findById(idCarrera)
                .orElseThrow(() -> new NoSuchElementException("Carrera no encontrada"));
        return this.iTutorRepository.findByActivoAndSemestreAndCarrera(1, semestre, carrera);
    }

    private void resolverRelaciones(Tutor tutor) {
        if (tutor.getCarrera() != null && tutor.getCarrera().getId() != null) {
            Carrera carrera = this.iCarreraRepository.findById(tutor.getCarrera().getId())
                    .orElseThrow(() -> new NoSuchElementException("Carrera no encontrada"));
            tutor.setCarrera(carrera);
        } else {
            tutor.setCarrera(null);
        }

        if (tutor.getSemestre() != null && tutor.getSemestre().getId() != null) {
            Semestre semestre = this.iSemestreRepository.findById(tutor.getSemestre().getId())
                    .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));
            tutor.setSemestre(semestre);
        } else {
            tutor.setSemestre(null);
        }
    }
}