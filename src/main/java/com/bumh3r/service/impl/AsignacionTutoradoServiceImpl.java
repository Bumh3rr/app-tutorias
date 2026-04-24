package com.bumh3r.service.impl;

import com.bumh3r.entity.AsignacionTutorado;
import com.bumh3r.entity.Semestre;
import com.bumh3r.entity.Tutor;
import com.bumh3r.entity.Tutorado;
import com.bumh3r.repository.IAsignacionTutoradoRepository;
import com.bumh3r.repository.ISemestreRepository;
import com.bumh3r.repository.ITutorRepository;
import com.bumh3r.repository.ITutoradoRepository;
import com.bumh3r.service.AsignacionTutoradoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Primary
@Service
public class AsignacionTutoradoServiceImpl implements AsignacionTutoradoService {

    @Autowired
    private IAsignacionTutoradoRepository iAsignacionTutoradoRepository;
    @Autowired
    private ITutorRepository iTutorRepository;
    @Autowired
    private ITutoradoRepository iTutoradoRepository;
    @Autowired
    private ISemestreRepository iSemestreRepository;

    @Override
    public List<AsignacionTutorado> obtenerTodasAsignaciones() {
        return this.iAsignacionTutoradoRepository.findByActivo(1);
    }

    @Override
    public void guardarAsignacion(AsignacionTutorado asignacion) {
        resolverRelaciones(asignacion);

        // Verificar que el tutorado no tenga más de 2 asignaciones activas
        long totalAsignaciones = this.iAsignacionTutoradoRepository
                .countByTutoradoAndActivo(asignacion.getTutorado(), 1);

        if (totalAsignaciones >= 2) {
            throw new IllegalStateException("El tutorado ya completó sus dos créditos de tutoría (1° y 2° semestre).");
        }

        // Verificar que no haya duplicado en el mismo semestre
        boolean yaAsignado = this.iAsignacionTutoradoRepository
                .existsByTutoradoAndSemestreAndActivo(asignacion.getTutorado(), asignacion.getSemestre(), 1);

        if (yaAsignado) {
            throw new IllegalStateException("El tutorado ya tiene una asignación activa en este semestre.");
        }

        asignacion.setActivo(1);
        this.iAsignacionTutoradoRepository.save(asignacion);
    }

    @Override
    public void actualizarAsignacion(Integer id, AsignacionTutorado asignacion) {
        AsignacionTutorado asignacionDB = this.iAsignacionTutoradoRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Asignación no encontrada"));

        resolverRelaciones(asignacion);

        asignacionDB.setTutor(asignacion.getTutor());
        asignacionDB.setTutorado(asignacion.getTutorado());
        asignacionDB.setSemestre(asignacion.getSemestre());

        this.iAsignacionTutoradoRepository.save(asignacionDB);
    }

    @Override
    public AsignacionTutorado obtenerAsignacion(Integer id) {
        return this.iAsignacionTutoradoRepository.findById(id).orElse(null);
    }

    @Override
    public void eliminarAsignacion(Integer id) {
        AsignacionTutorado asignacion = this.iAsignacionTutoradoRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Asignación no encontrada"));
        asignacion.setActivo(0);
        this.iAsignacionTutoradoRepository.save(asignacion);
    }

    @Override
    public List<AsignacionTutorado> buscarTutoriasPorTutorado(Integer idTutorado) {
        Tutorado tutorado = this.iTutoradoRepository.findById(idTutorado).orElseThrow(() -> new NoSuchElementException("Tutorado no encontrado"));
        return this.iAsignacionTutoradoRepository.findByActivoAndTutorado(1, tutorado);
    }

    @Override
    public List<AsignacionTutorado> buscarAsignacionesPorTutorYSemestre(Integer idTutor, Integer idSemestre) {
        Tutor tutor = this.iTutorRepository.findById(idTutor).orElseThrow(() -> new NoSuchElementException("Tutor no encontrado"));
        Semestre semestre = this.iSemestreRepository.findById(idSemestre).orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));
        return this.iAsignacionTutoradoRepository.findByActivoAndTutorAndSemestre(1, tutor, semestre);
    }

    @Override
    public List<AsignacionTutorado> buscarAsignacionesPorSemestre(Integer idSemestre) {
        Semestre semestre = this.iSemestreRepository.findById(idSemestre).orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));
        return this.iAsignacionTutoradoRepository.findByActivoAndSemestre(1, semestre);
    }

    private void resolverRelaciones(AsignacionTutorado asignacion) {
        if (asignacion.getTutor() != null && asignacion.getTutor().getId() != null) {
            Tutor tutor = this.iTutorRepository.findById(asignacion.getTutor().getId()).orElseThrow(() -> new NoSuchElementException("Tutor no encontrado"));
            asignacion.setTutor(tutor);
        } else {
            asignacion.setTutor(null);
        }

        if (asignacion.getTutorado() != null && asignacion.getTutorado().getId() != null) {
            Tutorado tutorado = this.iTutoradoRepository.findById(asignacion.getTutorado().getId()).orElseThrow(() -> new NoSuchElementException("Tutorado no encontrado"));
            asignacion.setTutorado(tutorado);
        } else {
            asignacion.setTutorado(null);
        }

        if (asignacion.getSemestre() != null && asignacion.getSemestre().getId() != null) {
            Semestre semestre = this.iSemestreRepository.findById(asignacion.getSemestre().getId()).orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));
            asignacion.setSemestre(semestre);
        } else {
            asignacion.setSemestre(null);
        }
    }
}