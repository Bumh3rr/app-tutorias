package com.bumh3r.service.impl;

import com.bumh3r.entity.Carrera;
import com.bumh3r.entity.Grupo;
import com.bumh3r.entity.Semestre;
import com.bumh3r.entity.Tutor;
import com.bumh3r.repository.ICarreraRepository;
import com.bumh3r.repository.IGrupoRepository;
import com.bumh3r.repository.ISemestreRepository;
import com.bumh3r.repository.ITutorRepository;
import com.bumh3r.service.GrupoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Primary
@Service
public class GrupoServiceImpl implements GrupoService {

    @Autowired
    private IGrupoRepository iGrupoRepository;
    @Autowired
    private ITutorRepository iTutorRepository;
    @Autowired
    private ISemestreRepository iSemestreRepository;
    @Autowired
    private ICarreraRepository iCarreraRepository;

    @Override
    public List<Grupo> obtenerTodosGrupos() {
        return this.iGrupoRepository.findByActivo(1);
    }

    @Override
    public void guardarGrupo(Grupo grupo) {
        resolverRelaciones(grupo);

        if (this.iGrupoRepository.existsByNombreAndSemestreAndCarreraAndActivo(
                grupo.getNombre(), grupo.getSemestre().getId(), grupo.getCarrera().getId())) {
            throw new IllegalArgumentException(
                "Ya existe un grupo activo con el nombre \"" + grupo.getNombre() + "\" en ese semestre y carrera.");
        }

        if (grupo.getAula() != null && grupo.getDiaSemana() != null && grupo.getHorario() != null
                && !grupo.getAula().isBlank() && !grupo.getDiaSemana().isBlank() && !grupo.getHorario().isBlank()) {
            if (this.iGrupoRepository.existsByAulaAndDiaSemanaAndHorarioAndActivo(
                    grupo.getAula(), grupo.getDiaSemana(), grupo.getHorario(), 1)) {
                throw new IllegalStateException(
                        "El aula " + grupo.getAula() + " ya está ocupada en ese día y horario.");
            }
        }

        grupo.setActivo(1);
        this.iGrupoRepository.save(grupo);
    }

    @Override
    public void actualizarGrupo(Integer id, Grupo grupo) {
        Grupo grupoDB = this.iGrupoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Grupo no encontrado"));

        resolverRelaciones(grupo);

        if (this.iGrupoRepository.existsByNombreAndSemestreAndCarreraAndActivoExcludingId(
                grupo.getNombre(), grupo.getSemestre().getId(), grupo.getCarrera().getId(), id)) {
            throw new IllegalArgumentException(
                "Ya existe un grupo activo con el nombre \"" + grupo.getNombre() + "\" en ese semestre y carrera.");
        }

        if (grupo.getAula() != null && grupo.getDiaSemana() != null && grupo.getHorario() != null
                && !grupo.getAula().isBlank() && !grupo.getDiaSemana().isBlank() && !grupo.getHorario().isBlank()) {
            if (this.iGrupoRepository.existsByAulaAndDiaSemanaAndHorarioAndActivoAndIdNot(
                    grupo.getAula(), grupo.getDiaSemana(), grupo.getHorario(), 1, id)) {
                throw new IllegalStateException(
                        "El aula " + grupo.getAula() + " ya está ocupada en ese día y horario.");
            }
        }

        grupoDB.setNombre(grupo.getNombre());
        // El tutor se gestiona exclusivamente desde el módulo "Asignar Tutor" — no se sobreescribe aquí
        grupoDB.setSemestre(grupo.getSemestre());
        grupoDB.setCarrera(grupo.getCarrera());
        grupoDB.setAula(grupo.getAula());
        grupoDB.setDiaSemana(grupo.getDiaSemana());
        grupoDB.setHorario(grupo.getHorario());

        this.iGrupoRepository.save(grupoDB);
    }

    @Override
    public Grupo obtenerGrupo(Integer id) {
        return this.iGrupoRepository.findById(id).orElse(null);
    }

    @Override
    public void eliminarGrupo(Integer id) {
        Grupo grupo = this.iGrupoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Grupo no encontrado"));
        grupo.setActivo(0);
        this.iGrupoRepository.save(grupo);
    }

    @Override
    public List<Grupo> buscarPorTutor(Integer idTutor) {
        Tutor tutor = this.iTutorRepository.findById(idTutor)
                .orElseThrow(() -> new NoSuchElementException("Tutor no encontrado"));
        return this.iGrupoRepository.findByActivoAndTutor(1, tutor);
    }

    @Override
    public List<Grupo> buscarPorSemestre(Integer idSemestre) {
        Semestre semestre = this.iSemestreRepository.findById(idSemestre)
                .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));
        return this.iGrupoRepository.findByActivoAndSemestre(1, semestre);
    }

    @Override
    public List<Grupo> buscarPorTutorYSemestre(Integer idTutor, Integer idSemestre) {
        Tutor tutor = this.iTutorRepository.findById(idTutor)
                .orElseThrow(() -> new NoSuchElementException("Tutor no encontrado"));
        Semestre semestre = this.iSemestreRepository.findById(idSemestre)
                .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));
        return this.iGrupoRepository.findByActivoAndTutorAndSemestre(1, tutor, semestre);
    }

    @Override
    public List<Grupo> buscarPorCarreraYSemestre(Integer idCarrera, Integer idSemestre) {
        Carrera carrera = this.iCarreraRepository.findById(idCarrera)
                .orElseThrow(() -> new NoSuchElementException("Carrera no encontrada"));
        Semestre semestre = this.iSemestreRepository.findById(idSemestre)
                .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));
        return this.iGrupoRepository.findByActivoAndCarreraAndSemestre(1, carrera, semestre);
    }

    @Override
    public Page<Grupo> obtenerTodosGruposPage(Pageable pageable) {
        return this.iGrupoRepository.findByActivo(1, pageable);
    }

    @Override
    public Page<Grupo> buscarPorSemestrePage(Integer idSemestre, Pageable pageable) {
        Semestre semestre = this.iSemestreRepository.findById(idSemestre)
                .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));
        return this.iGrupoRepository.findByActivoAndSemestre(1, semestre, pageable);
    }

    @Override
    public Page<Grupo> buscarPorTutorYSemestrePage(Integer idTutor, Integer idSemestre, Pageable pageable) {
        Tutor tutor = this.iTutorRepository.findById(idTutor)
                .orElseThrow(() -> new NoSuchElementException("Tutor no encontrado"));
        Semestre semestre = this.iSemestreRepository.findById(idSemestre)
                .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));
        return this.iGrupoRepository.findByActivoAndTutorAndSemestre(1, tutor, semestre, pageable);
    }

    @Override
    public Page<Grupo> buscarPorCarreraYSemestrePage(Integer idCarrera, Integer idSemestre, Pageable pageable) {
        Carrera carrera = this.iCarreraRepository.findById(idCarrera)
                .orElseThrow(() -> new NoSuchElementException("Carrera no encontrada"));
        Semestre semestre = this.iSemestreRepository.findById(idSemestre)
                .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));
        return this.iGrupoRepository.findByActivoAndCarreraAndSemestre(1, carrera, semestre, pageable);
    }

    @Override
    public Page<Grupo> buscarPorNombrePage(String q, Pageable pageable) {
        return this.iGrupoRepository.searchByName(q, pageable);
    }

    @Override
    public void asignarTutor(Integer idGrupo, Integer idTutor) {
        Grupo grupo = this.iGrupoRepository.findById(idGrupo)
                .orElseThrow(() -> new NoSuchElementException("Grupo no encontrado"));
        Tutor tutor = this.iTutorRepository.findById(idTutor)
                .orElseThrow(() -> new NoSuchElementException("Tutor no encontrado"));
        grupo.setTutor(tutor);
        this.iGrupoRepository.save(grupo);
    }

    @Override
    public void quitarTutor(Integer idGrupo) {
        Grupo grupo = this.iGrupoRepository.findById(idGrupo)
                .orElseThrow(() -> new NoSuchElementException("Grupo no encontrado"));
        grupo.setTutor(null);
        this.iGrupoRepository.save(grupo);
    }

    @Override
    public Page<Grupo> obtenerGruposSinTutorPage(Pageable pageable) {
        return this.iGrupoRepository.findByActivoAndTutorIsNull(1, pageable);
    }

    @Override
    public Page<Grupo> buscarSinTutorPorNombrePage(String q, Pageable pageable) {
        return this.iGrupoRepository.searchSinTutorByName(q, pageable);
    }

    @Override
    public Page<Grupo> buscarSinTutorPorSemestrePage(Integer idSemestre, Pageable pageable) {
        return this.iGrupoRepository.findSinTutorBySemestre(idSemestre, pageable);
    }

    @Override
    public Page<Grupo> buscarSinTutorPorCarreraPage(Integer idCarrera, Pageable pageable) {
        return this.iGrupoRepository.findSinTutorByCarrera(idCarrera, pageable);
    }

    private void resolverRelaciones(Grupo grupo) {
        if (grupo.getTutor() != null && grupo.getTutor().getId() != null) {
            Tutor tutor = this.iTutorRepository.findById(grupo.getTutor().getId())
                    .orElseThrow(() -> new NoSuchElementException("Tutor no encontrado"));
            grupo.setTutor(tutor);
        } else {
            grupo.setTutor(null);
        }

        if (grupo.getSemestre() != null && grupo.getSemestre().getId() != null) {
            Semestre semestre = this.iSemestreRepository.findById(grupo.getSemestre().getId())
                    .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));
            grupo.setSemestre(semestre);
        } else {
            grupo.setSemestre(null);
        }

        if (grupo.getCarrera() != null && grupo.getCarrera().getId() != null) {
            Carrera carrera = this.iCarreraRepository.findById(grupo.getCarrera().getId())
                    .orElseThrow(() -> new NoSuchElementException("Carrera no encontrada"));
            grupo.setCarrera(carrera);
        } else {
            grupo.setCarrera(null);
        }
    }
}
