package com.bumh3r.service;

import com.bumh3r.entity.PAT;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PATService {
    List<PAT> obtenerTodosPAT();
    void guardarPAT(PAT pat);
    void actualizarPAT(Integer id, PAT pat);
    PAT obtenerPAT(Integer id);
    void eliminarPAT(Integer id);

    // PAT generales
    List<PAT> obtenerPATGenerales();

    // PAT por carrera y semestre
    List<PAT> buscarPATporCarreraYSemestre(Integer idCarrera, Integer idSemestre);

    // Por paginación
    Page<PAT> obtenerPATGeneralesPaginacion(Integer page, Integer pageSize, String sortBy, String sort);
    Page<PAT> buscarPATporCarreraYSemestrePaginacion(Integer idCarrera, Integer idSemestre, Integer page, Integer pageSize, String sortBy, String sort);
    Page<PAT> obtenerTodosPATPaginacion(Integer page, Integer pageSize, String sortBy, String sort);

    Page<PAT> buscarPorFechaRegistroPaginacion(java.util.Date inicio, java.util.Date fin, Integer page, Integer pageSize, String sortBy, String sort);
}
