package com.bumh3r.service.impl;

import com.bumh3r.entity.Carrera;
import com.bumh3r.entity.PAT;
import com.bumh3r.entity.Semestre;
import com.bumh3r.exception.RegistroInactivoExistenteException;
import com.bumh3r.repository.ICarreraRepository;
import com.bumh3r.repository.IPATRepository;
import com.bumh3r.repository.ISemestreRepository;
import com.bumh3r.service.PATService;
import com.bumh3r.service.utils.PaginationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @Autowired
    private PaginationUtil paginationUtil;

    @Override
    public void guardarPAT(PAT pat) {
        resolverRelaciones(pat);
        Integer idCarrera = (pat.getCarrera() != null) ? pat.getCarrera().getId() : null;
        Integer idSemestre = (pat.getSemestre() != null) ? pat.getSemestre().getId() : null;
        if (idSemestre != null) {
            this.iPATRepository.findByUniqueCombo(pat.getNombre(), idSemestre, idCarrera).ifPresent(existente -> {
                if (existente.getActivo() == 1) throw new IllegalArgumentException("Ya existe un PAT activo con ese nombre, semestre y carrera.");
                throw new RegistroInactivoExistenteException("PAT", "Nombre/Semestre", pat.getNombre(), existente.getId());
            });
        }
        pat.setActivo(1);
        this.iPATRepository.save(pat);
    }

    @Override
    public void actualizarPAT(Integer id, PAT pat) {
        PAT patDB = this.iPATRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("PAT no encontrado"));

        resolverRelaciones(pat);

        Integer idCarreraUpd = (pat.getCarrera() != null) ? pat.getCarrera().getId() : null;
        Integer idSemestreUpd = (pat.getSemestre() != null) ? pat.getSemestre().getId() : null;
        if (idSemestreUpd != null) {
            this.iPATRepository.findByUniqueComboAndIdNot(pat.getNombre(), idSemestreUpd, idCarreraUpd, id).ifPresent(existente -> {
                if (existente.getActivo() == 1) throw new IllegalArgumentException("Ya existe un PAT activo con ese nombre, semestre y carrera.");
                throw new RegistroInactivoExistenteException("PAT", "Nombre/Semestre", pat.getNombre(), existente.getId());
            });
        }

        patDB.setNombre(pat.getNombre());
        patDB.setDescripcion(pat.getDescripcion());
        patDB.setFoto(pat.getFoto());
        patDB.setSemestre(pat.getSemestre());
        patDB.setCarrera(pat.getCarrera());
        patDB.setEsGeneral(pat.getEsGeneral());

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
    public Page<PAT> obtenerPATGeneralesPaginacion(Integer page, Integer pageSize, String sortBy, String sort) {
        Pageable pageable = this.paginationUtil.getPageable(page, pageSize, sortBy, sort);
        return this.iPATRepository.findByActivoAndEsGeneral(1, 1, pageable);
    }

    @Override
    public List<PAT> buscarPATporCarreraYSemestre(Integer idCarrera, Integer idSemestre) {
        Carrera carrera = this.iCarreraRepository.findById(idCarrera)
                .orElseThrow(() -> new NoSuchElementException("Carrera no encontrada"));
        Semestre semestre = this.iSemestreRepository.findById(idSemestre)
                .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));
        return this.iPATRepository.findByActivoAndCarreraAndSemestre(1, carrera, semestre);
    }

    @Override
    public Page<PAT> buscarPATporCarreraYSemestrePaginacion(Integer idCarrera, Integer idSemestre, Integer page, Integer pageSize, String sortBy, String sort) {
        Carrera carrera = this.iCarreraRepository.findById(idCarrera)
                .orElseThrow(() -> new NoSuchElementException("Carrera no encontrada"));
        Semestre semestre = this.iSemestreRepository.findById(idSemestre)
                .orElseThrow(() -> new NoSuchElementException("Semestre no encontrado"));
        Pageable pageable = this.paginationUtil.getPageable(page, pageSize, sortBy, sort);
        return this.iPATRepository.findByActivoAndCarreraAndSemestre(1, carrera, semestre, pageable);
    }

    @Override
    public List<PAT> obtenerTodosPAT() {
        return this.iPATRepository.findByActivo(1);
    }

    @Override
    public Page<PAT> obtenerTodosPATPaginacion(Integer page, Integer pageSize, String sortBy, String sort) {
        Pageable pageable = this.paginationUtil.getPageable(page, pageSize, sortBy, sort);
        return this.iPATRepository.findByActivo(1,pageable);
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

    @Override
    public org.springframework.data.domain.Page<com.bumh3r.entity.PAT> buscarPorFechaRegistroPaginacion(java.util.Date inicio, java.util.Date fin, Integer page, Integer pageSize, String sortBy, String sort) {
        org.springframework.data.domain.Pageable pageable = this.paginationUtil.getPageable(page, pageSize, sortBy, sort);
        return this.iPATRepository.findByFechaRegistroRange(inicio, fin, pageable);
    }

    @Override
    public void reactivar(Integer id) {
        PAT pat = this.iPATRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("PAT no encontrado"));
        if (pat.getActivo() == 1)
            throw new IllegalStateException("Este PAT ya está activo.");
        Integer idCarrera = (pat.getCarrera() != null) ? pat.getCarrera().getId() : null;
        Integer idSemestre = (pat.getSemestre() != null) ? pat.getSemestre().getId() : null;
        if (idSemestre != null) {
            this.iPATRepository.findByUniqueComboAndIdNot(pat.getNombre(), idSemestre, idCarrera, id).ifPresent(otro -> {
                if (otro.getActivo() == 1)
                    throw new IllegalStateException("No se puede reactivar: ya existe otro PAT activo con ese nombre, semestre y carrera.");
            });
        }
        // Nota: soft-delete de PAT no afecta a Actividades relacionadas.
        // Si se requiere validación en cascada, implementarla en una fase posterior.
        pat.setActivo(1);
        this.iPATRepository.save(pat);
    }

    @Override
    public Page<PAT> obtenerPorEstadoPaginado(String filtroEstado, int page, int pageSize, String sortBy, String sort) {
        Pageable pageable = this.paginationUtil.getPageable(page, pageSize, sortBy, sort);
        return switch (filtroEstado) {
            case "inactivos" -> this.iPATRepository.findByActivo(0, pageable);
            case "todos"     -> this.iPATRepository.findAll(pageable);
            default          -> this.iPATRepository.findByActivo(1, pageable);
        };
    }
}
