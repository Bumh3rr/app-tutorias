package com.bumh3r.service.impl;

import com.bumh3r.entity.Carrera;
import com.bumh3r.entity.Semestre;
import com.bumh3r.entity.Tutor;
import com.bumh3r.repository.ICarreraRepository;
import com.bumh3r.repository.ISemestreRepository;
import com.bumh3r.repository.ITutorRepository;
import com.bumh3r.service.TutorService;
import com.bumh3r.service.utils.PaginationUtil;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Primary
@Service
public class TutorServiceImpl implements TutorService {

    @Autowired
    private ITutorRepository iTutorRepository;
    @Autowired
    private ICarreraRepository iCarreraRepository;
    @Autowired
    private ISemestreRepository iSemestreRepository;
    @Autowired
    private PaginationUtil paginationUtil;

    @Override
    public void guardarTutor(Tutor tutor) {
        resolverRelaciones(tutor);

        if (tutor.getAula() != null && tutor.getDiaSemana() != null && tutor.getHorario() != null) {
            boolean ocupado = this.iTutorRepository
                    .existsByAulaAndDiaSemanaAndHorarioAndActivo(
                            tutor.getAula(), tutor.getDiaSemana(), tutor.getHorario(), 1);
            if (ocupado) {
                throw new IllegalStateException(
                        "El aula " + tutor.getAula() + " ya está ocupada en ese día y horario.");
            }
        }

        tutor.setActivo(1);
        this.iTutorRepository.save(tutor);
    }

    @Override
    public void actualizarTutor(Integer id, Tutor tutor) {
        Tutor tutorDB = this.iTutorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tutor no encontrado"));

        resolverRelaciones(tutor);

        if (tutor.getAula() != null && tutor.getDiaSemana() != null && tutor.getHorario() != null) {
            boolean ocupado = this.iTutorRepository
                    .existsByAulaAndDiaSemanaAndHorarioAndActivoAndIdNot(
                            tutor.getAula(), tutor.getDiaSemana(), tutor.getHorario(), 1, id);
            if (ocupado) {
                throw new IllegalStateException(
                        "El aula " + tutor.getAula() + " ya está ocupada en ese día y horario.");
            }
        }

        tutorDB.setNombre(tutor.getNombre());
        tutorDB.setApellido(tutor.getApellido());
        tutorDB.setNumeroControl(tutor.getNumeroControl());
        tutorDB.setEmail(tutor.getEmail());
        tutorDB.setFoto(tutor.getFoto());
        tutorDB.setAula(tutor.getAula());
        tutorDB.setDiaSemana(tutor.getDiaSemana());
        tutorDB.setHorario(tutor.getHorario());
        tutorDB.setCarrera(tutor.getCarrera());
        tutorDB.setSemestre(tutor.getSemestre());

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
    public Page<Tutor> buscarTutoresPorSemestreYCarreraPaginado(Integer idSemestre, Integer idCarrera, Integer page, Integer pageSize, String sortBy, String sort) {
        Semestre semestre = this.iSemestreRepository.findById(idSemestre)
                .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));
        Carrera carrera = this.iCarreraRepository.findById(idCarrera)
                .orElseThrow(() -> new NoSuchElementException("Carrera no encontrada"));

        Pageable pageable = this.paginationUtil.getPageable(page, pageSize, sortBy, sort);
        return this.iTutorRepository.findByActivoAndSemestreAndCarrera(1, semestre, carrera, pageable);
    }

    @Override
    public Page<Tutor> buscarTutoresPorSemestrePaginado(Integer idSemestre, Integer page, Integer pageSize, String sortBy, String sort) {
        Semestre semestre = this.iSemestreRepository.findById(idSemestre)
                .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));

        Pageable pageable = this.paginationUtil.getPageable(page, pageSize, sortBy, sort);
        return this.iTutorRepository.findByActivoAndSemestre(1, semestre, pageable);
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