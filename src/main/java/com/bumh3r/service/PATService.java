package com.bumh3r.service;

import com.bumh3r.entity.PAT;
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
}