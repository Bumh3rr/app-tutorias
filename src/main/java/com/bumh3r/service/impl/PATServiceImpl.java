package com.bumh3r.service.impl;

import com.bumh3r.entity.Carrera;
import com.bumh3r.entity.PAT;
import com.bumh3r.entity.Semestre;
import com.bumh3r.repository.ICarreraRepository;
import com.bumh3r.repository.IPATRepository;
import com.bumh3r.repository.ISemestreRepository;
import com.bumh3r.service.PATService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Primary
@Service
public class PATServiceImpl implements PATService {

    @Autowired
    private IPATRepository iPATRepository;
    @Autowired
    private ICarreraRepository iCarreraRepository;
    @Autowired
    private ISemestreRepository iSemestreRepository;

    @Override
    public List<PAT> obtenerTodosPAT() {
        return this.iPATRepository.findByActivo(1);
    }

    @Override
    public void guardarPAT(PAT pat) {
        resolverRelaciones(pat);
        pat.setActivo(1);
        this.iPATRepository.save(pat);
    }

    @Override
    public void actualizarPAT(Integer id, PAT pat) {
        PAT patDB = this.iPATRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("PAT no encontrado"));

        resolverRelaciones(pat);

        patDB.setNombre(pat.getNombre());
        patDB.setDescripcion(pat.getDescripcion());
        patDB.setFoto(pat.getFoto());
        patDB.setSemestre(pat.getSemestre());
        patDB.setCarrera(pat.getCarrera());
        patDB.setEsGeneral(pat.getEsGeneral());
        patDB.setActivo(pat.getActivo());

        this.iPATRepository.save(patDB);
    }

    @Override
    public PAT obtenerPAT(Integer id) {
        return this.iPATRepository.findById(id).orElse(null);
    }

    @Override
    public void eliminarPAT(Integer id) {
        PAT pat = this.iPATRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("PAT no encontrado"));
        pat.setActivo(0);
        this.iPATRepository.save(pat);
    }

    @Override
    public List<PAT> obtenerPATGenerales() {
        return this.iPATRepository.findByActivoAndEsGeneral(1, 1);
    }

    @Override
    public List<PAT> buscarPATporCarreraYSemestre(Integer idCarrera, Integer idSemestre) {
        Carrera carrera = this.iCarreraRepository.findById(idCarrera)
                .orElseThrow(() -> new NoSuchElementException("Carrera no encontrada"));
        Semestre semestre = this.iSemestreRepository.findById(idSemestre)
                .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));
        return this.iPATRepository.findByActivoAndCarreraAndSemestre(1, carrera, semestre);
    }

    private void resolverRelaciones(PAT pat) {
        if (pat.getCarrera() != null && pat.getCarrera().getId() != null) {
            Carrera carrera = this.iCarreraRepository.findById(pat.getCarrera().getId())
                    .orElseThrow(() -> new NoSuchElementException("Carrera no encontrada"));
            pat.setCarrera(carrera);
        } else {
            pat.setCarrera(null);
        }

        if (pat.getSemestre() != null && pat.getSemestre().getId() != null) {
            Semestre semestre = this.iSemestreRepository.findById(pat.getSemestre().getId())
                    .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));
            pat.setSemestre(semestre);
        } else {
            pat.setSemestre(null);
        }
    }
}